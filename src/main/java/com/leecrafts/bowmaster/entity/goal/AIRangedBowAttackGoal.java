package com.leecrafts.bowmaster.entity.goal;

import com.leecrafts.bowmaster.capability.ModCapabilities;
import com.leecrafts.bowmaster.capability.livingentity.ILivingEntityCap;
import com.leecrafts.bowmaster.capability.livingentity.LivingEntityCap;
import com.leecrafts.bowmaster.entity.custom.SkeletonBowMasterEntity;
import com.leecrafts.bowmaster.util.NeuralNetworkUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
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

        // TODO don't create network here
//        NeuralNetworkUtil.saveModel(network);
//        System.out.println("saved test model");
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

            BasicNetwork network = this.mob.getNetwork();
//            NeuralNetworkUtil.printWeights(network);
//            network.getFlat().getWeights()[0] += 94321;
            double[] observations = getObservations(livingEntity);
            double[] actionOutputs = NeuralNetworkUtil.computeOutput(network, observations);
            handleRightClick(livingEntity, actionOutputs[0]);
            handleMovement(actionOutputs[1], actionOutputs[2], actionOutputs[3]);
            handleStrafing(actionOutputs[4], actionOutputs[5], actionOutputs[6]);
            handleJump(actionOutputs[7]);
            handleLookDirection(actionOutputs[8], actionOutputs[9]);

//            spamArrows(livingEntity);
        }
    }

    public double[] getObservations(LivingEntity target) {
        Vec3 agentPosition = this.mob.position();
        Vec3 targetPosition = target.position();
        Vec3 distance = targetPosition.subtract(agentPosition);
        Vec3 distanceNormalized = distance.normalize();

        // Distances
        double horizontalDistance = Math.sqrt(distance.x * distance.x + distance.z * distance.z);
        double verticalDistance = distance.y;

        AtomicReference<Vec3> targetVelocity = new AtomicReference<>(Vec3.ZERO);
        LazyOptional<ILivingEntityCap> capability = target.getCapability(ModCapabilities.LIVING_ENTITY_CAPABILITY);
        if (capability.isPresent()) {
            capability.ifPresent(iLivingEntityCap -> {
                LivingEntityCap livingEntityCap = (LivingEntityCap) iLivingEntityCap;
                targetVelocity.set(livingEntityCap.getVelocity());
            });
        }
        double[] target_FB_LR_UD = calculate_FB_LR_UD_ofVelocity(distance, targetVelocity.get());
        double target_fb = target_FB_LR_UD[0];
        double target_lr = target_FB_LR_UD[1];
        double target_ud = target_FB_LR_UD[2];

        // TODO observe agent velocity and opponent's projectile velocity in order to help the agent dodge. (do later)

        // Differences in pitch and yaw
        double pitchFacingTarget = Math.asin(distanceNormalized.y);
        double yawFacingTarget = Math.atan2(distanceNormalized.x, distanceNormalized.z);
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
        boolean press = output > 0; // < 0 is not press, > 0 is press
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
        if (output > 0) {
            this.mob.getJumpControl().jump();
        }
    }

    private void handleLookDirection(double x, double y) {
        // TODO normalize angle?
        this.mob.setXRot((float) (360 * x));
        this.mob.setYRot((float) (360 * y));
    }

    private void spamArrows(LivingEntity target) {
        if (this.mob.isUsingItem()) {
            int i = this.mob.getTicksUsingItem();
            if (i >= 3) {
                this.mob.stopUsingItem();
                this.mob.performRangedAttack(target, BowItem.getPowerForTime(i));
            }
        }
        else {
            this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof BowItem));
        }
    }

}
