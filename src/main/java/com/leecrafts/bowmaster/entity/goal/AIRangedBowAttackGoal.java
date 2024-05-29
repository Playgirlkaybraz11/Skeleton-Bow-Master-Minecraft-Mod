package com.leecrafts.bowmaster.entity.goal;

import com.leecrafts.bowmaster.capability.ModCapabilities;
import com.leecrafts.bowmaster.capability.livingentity.LivingEntityCap;
import com.leecrafts.bowmaster.entity.custom.SkeletonBowMasterEntity;
import com.leecrafts.bowmaster.util.MultiOutputFreeformNetwork;
import com.leecrafts.bowmaster.util.NeuralNetworkUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.phys.Vec3;

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
            MultiOutputFreeformNetwork network = this.mob.getNetwork();
            NeuralNetworkUtil.printWeights(network);
            double[] observations = getObservations(livingEntity, distance, pitchFacingTarget, yawFacingTarget);

            // actions
            double[] actionOutputs = NeuralNetworkUtil.computeOutput(network, observations);

            // random actions using epsilon
            RandomSource random = this.mob.getRandom();
            if (SkeletonBowMasterEntity.TRAINING) {
                for (int i = 0; i < actionOutputs.length; i++) {
                    if (random.nextDouble() < NeuralNetworkUtil.EPSILON) {
                        actionOutputs[i] = random.nextDouble();
                    }
                }
            }

            boolean killerModeEnabled = false; // sounds cool, but it's only for testing
            if (!killerModeEnabled) {
                handleLookDirection(actionOutputs[0], actionOutputs[1], pitchFacingTarget, yawFacingTarget);
                handleRightClick(livingEntity, actionOutputs[2], actionOutputs[3]);
                handleMovement(actionOutputs[4], actionOutputs[5], actionOutputs[6]);
            }
            else {
                handleLookDirection(0.5, 0.5, pitchFacingTarget, yawFacingTarget);
                spamArrows(livingEntity);
                handleMovement(1, 0, 0);
            }
            handleStrafing(actionOutputs[7], actionOutputs[8], actionOutputs[9]);
            handleJump(actionOutputs[10], actionOutputs[11]);

            if (SkeletonBowMasterEntity.TRAINING) {
//                double[] logProbabilities = new double[actionOutputs.length];
//                for (int i = 0; i < actionOutputs.length; i++) {
//                    logProbabilities[i] = Math.log(actionOutputs[i]);
//                }

                // update state, action, and reward storage
                this.mob.storeStates(observations);
                this.mob.storeActions(actionOutputs);
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

    private void handleLookDirection(double xRotOffset, double yRotOffset, double pitchFacingTarget, double yawFacingTarget) {
        System.out.println("xRotOffset: " + xRotOffset + ", yRotOffset: " + yRotOffset);
        this.mob.setXRot((float) Mth.clamp(Math.toDegrees(pitchFacingTarget) + 90 * xRotOffset, -90, 90));
        this.mob.setYRot((float) Math.toDegrees(normalizeAngle(yawFacingTarget + Math.PI * yRotOffset)));
    }

    private void handleRightClick(LivingEntity target, double rightClickProb, double noRightClickProb) {
        System.out.println("rightClickProb: " + rightClickProb + ", noRightClickProb: " + noRightClickProb);
        boolean press = rightClickProb > noRightClickProb;
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

    private void handleMovement(double forwardProb, double backwardProb, double neitherProb) {
        System.out.println("forwardProb: " + forwardProb + ", backwardProb: " + backwardProb + ", neitherProb: " + neitherProb);
        if (forwardProb > backwardProb && forwardProb > neitherProb) {
            this.mob.forwardImpulse(1.0f);
        } else if (backwardProb > neitherProb) {
            this.mob.forwardImpulse(-1.0f);
        }
    }

    private void handleStrafing(double leftProb, double rightProb, double neitherProb) {
        System.out.println("leftProb: " + leftProb + ", rightProb: " + rightProb + ", neitherProb: " + neitherProb);
        // I could use MoveControl#strafe, but there are some unwanted hardcoded behaviors
        if (leftProb > rightProb && leftProb > neitherProb) {
            this.mob.setXxa(1.0f);
        } else if (rightProb > neitherProb) {
            this.mob.setXxa(-1.0f);
        }
    }

    private void handleJump(double jumpProb, double noJumpProb) {
        System.out.println("jumpProb: " + jumpProb + ", noJumpProb: " + noJumpProb);
        if (jumpProb > noJumpProb) {
            this.mob.getJumpControl().jump();
        }
    }

    private void spamArrows(LivingEntity target) { // for testing
        if (this.mob.isUsingItem()) {
            int i = this.mob.getTicksUsingItem();
            if (i >= 3) {
                this.mob.stopUsingItem();
//                this.mob.performRangedAttack(target, BowItem.getPowerForTime(i));
                this.mob.performRangedAttack(target, 3);
            }
        }
        else {
            this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof BowItem));
        }
    }

}
