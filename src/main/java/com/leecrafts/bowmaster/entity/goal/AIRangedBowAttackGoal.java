package com.leecrafts.bowmaster.entity.goal;

import com.leecrafts.bowmaster.capability.ModCapabilities;
import com.leecrafts.bowmaster.capability.livingentity.LivingEntityCap;
import com.leecrafts.bowmaster.entity.custom.SkeletonBowMasterEntity;
import com.leecrafts.bowmaster.util.NeuralNetworkUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.phys.Vec3;
import org.encog.neural.networks.BasicNetwork;

import java.util.concurrent.atomic.AtomicReference;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class AIRangedBowAttackGoal<T extends SkeletonBowMasterEntity & RangedAttackMob> extends Goal {

    private final T mob;

    public AIRangedBowAttackGoal(T mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null && this.isHoldingBow();
    }

    protected boolean isHoldingBow() {
        return this.mob.isHolding(is -> is.getItem() instanceof BowItem);
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.mob.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity != null) {

            float f = (float)this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float f1 = (float) (0.25 * f);
            this.mob.setSpeed(f1);

            // just some precalculated values
            Vec3 distance = livingEntity.position().subtract(this.mob.position());
            Vec3 distanceNormalized = distance.normalize();
            double pitchFacingTarget = Math.asin(-distanceNormalized.y); // in radians
            double yawFacingTarget = Math.atan2(-distanceNormalized.x, distanceNormalized.z); // in radians

            // observations
            BasicNetwork network = this.mob.getNetwork();
            double[] observations = getObservations(livingEntity, distance, pitchFacingTarget, yawFacingTarget);

            // actions
            double[] actionOutputs = NeuralNetworkUtil.computeOutput(network, observations);
//            handleLookDirection(actionOutputs[0], actionOutputs[1], pitchFacingTarget, yawFacingTarget);
            handleLookDirection(0.5, 0.5, pitchFacingTarget, yawFacingTarget);
//            handleRightClick(livingEntity, actionOutputs[2]);
            spamArrows(livingEntity);
            handleMovement(actionOutputs[3], actionOutputs[4], actionOutputs[5]);
            handleStrafing(actionOutputs[6], actionOutputs[7], actionOutputs[8]);
            handleJump(actionOutputs[9]);

            if (SkeletonBowMasterEntity.TRAINING) {
                double[] logProbabilities = new double[actionOutputs.length];
                for (int i = 0; i < actionOutputs.length; i++) {
                    logProbabilities[i] = Math.log(actionOutputs[i]);
                }

                // update state, action, and reward storage
                this.mob.storeStates(observations);
                this.mob.storeActions(logProbabilities);
                this.mob.storeRewards(-0.005);
            }

        }
    }

    public double[] getObservations(LivingEntity target, Vec3 distance, double pitchFacingTarget, double yawFacingTarget) {
        // Distances
        double horizontalDistance = Math.sqrt(distance.x * distance.x + distance.z * distance.z);
        double verticalDistance = distance.y;

        AtomicReference<Vec3> targetVelocity = new AtomicReference<>(Vec3.ZERO);
        target.getCapability(ModCapabilities.LIVING_ENTITY_CAPABILITY).ifPresent(iLivingEntityCap -> {
            LivingEntityCap livingEntityCap = (LivingEntityCap) iLivingEntityCap;
            targetVelocity.set(livingEntityCap.getVelocity());
        });
        double[] target_FB_LR_UD = calculate_FB_LR_UD_ofVelocity(distance, targetVelocity.get());
        double target_fb = target_FB_LR_UD[0];
        double target_lr = target_FB_LR_UD[1];
        double target_ud = target_FB_LR_UD[2];

        // TODO observe agent velocity and opponent's projectile velocity in order to help the agent dodge. (do later)

        // Differences in pitch and yaw
        double pitchDifference = pitchFacingTarget - Math.toRadians(this.mob.getXRot());
        double yawDifference = normalizeAngle(yawFacingTarget - Math.toRadians(this.mob.getYRot()));

        // Health
        double healthPercentage = this.mob.getHealth() / this.mob.getMaxHealth();

        // Bow charge
        double bowCharge = getBowChargeMeter();

        return new double[] {
                horizontalDistance, verticalDistance,
                target_fb, target_lr, target_ud,
                pitchDifference, yawDifference,
                healthPercentage, bowCharge
        };
    }

    // 3 scalar values
    // Calculate v_forwardbackward, the object's forward/backward velocity relative to the agent
    // Calculate v_leftright, the object's left/right velocity relative to the agent
    // Calculate v_updown, the object's up/down velocity relative to the agent
    private static double[] calculate_FB_LR_UD_ofVelocity(Vec3 distance, Vec3 velocity) {
        double v_fb = velocity.dot(distance.normalize());

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 vHorizontal = new Vec3(distance.x, 0, distance.z);
        Vec3 right = vHorizontal.cross(up).normalize();

        double v_lr = velocity.dot(right);
        double v_ud = velocity.dot(up);

        return new double[] {v_fb, v_lr, v_ud};
    }

    // Normalize angle to range [-pi, pi]
    private static double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    public int getBowChargeMeter() {
        // FYI bows are fully charged after 20 ticks (1 second)
        return Math.min(this.mob.getTicksUsingItem(), TICKS_PER_SECOND);
    }

    private void handleRightClick(LivingEntity target, double output) {
        boolean press = output > 0.5; // < 0.5 is not press, > 0.5 is press
        if (press) {
            this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof BowItem));
        }
        else {
            if (this.mob.isUsingItem()) {
                int i = this.mob.getTicksUsingItem();
                if (i >= 3) {
                    this.mob.performRangedAttack(target, BowItem.getPowerForTime(i));
                }
                this.mob.stopUsingItem();
            }
        }
    }

    private void handleMovement(double forward, double backward, double neither) {
        if (forward > backward && forward > neither) {
            this.mob.forwardImpulse(1.0f);
        } else if (backward > neither) {
            this.mob.forwardImpulse(-1.0f);
        }
    }

    private void handleStrafing(double left, double right, double neither) {
        // I could use MoveControl#strafe, but there are some unwanted hardcoded behaviors
        if (left > right && left > neither) {
            this.mob.setXxa(1.0f);
        } else if (right > neither) {
            this.mob.setXxa(-1.0f);
        }
    }

    private void handleJump(double output) {
        if (output > 0.5) {
            this.mob.getJumpControl().jump();
        }
    }

    private void handleLookDirection(double xRotOffset, double yRotOffset, double pitchFacingTarget, double yawFacingTarget) {
        this.mob.setXRot((float) Mth.clamp(Math.toDegrees(pitchFacingTarget) + 90 * (yRotOffset * 2 - 1), -90, 90));
        this.mob.setYRot((float) Math.toDegrees(normalizeAngle(yawFacingTarget + Math.PI * (xRotOffset * 2 - 1))));
    }

    private void spamArrows(LivingEntity target) { // for testing
        if (this.mob.isUsingItem()) {
            int i = this.mob.getTicksUsingItem();
            if (i >= 3) {
                this.mob.stopUsingItem();
                this.mob.performRangedAttack(target, BowItem.getPowerForTime(20));
            }
        }
        else {
            this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof BowItem));
        }
    }

}
