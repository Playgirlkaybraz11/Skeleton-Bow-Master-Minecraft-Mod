package com.leecrafts.bowmaster.entity.goal;

import com.leecrafts.bowmaster.entity.custom.SkeletonBowMasterEntity;
import com.leecrafts.bowmaster.util.NeuralNetworkUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import org.joml.Vector3d;

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

            // TODO make observations

            // TODO action outputs
            float[] actionOutputs = NeuralNetworkUtil.computeOutput(network, observation);
            handleRightClick(livingEntity, actionOutputs[0]);
            handleMovement(actionOutputs[1], actionOutputs[2], actionOutputs[3]);
            handleStrafing(actionOutputs[4], actionOutputs[5], actionOutputs[6]);
            handleJump(actionOutputs[7]);
            handleLookDirection(actionOutputs[8], actionOutputs[9]);

            float f = (float)this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float f1 = (float) (0.25 * f);
            this.mob.setSpeed(f1);

//            spamArrows(livingEntity);
        }
    }

    private void handleRightClick(LivingEntity target, float output) {
        boolean press = output > 0; // < 0 is not press, > 0 is press
        if (press) {
            this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof BowItem));
        }
        else {
            if (this.mob.isUsingItem()) {
                int i = this.mob.getTicksUsingItem();
                // TODO actually 5??
                if (i >= 5) {
                    this.mob.performRangedAttack(target, BowItem.getPowerForTime(i));
                }
                this.mob.stopUsingItem();
            }
        }
    }

    private void handleMovement(float forward, float backward, float neither) {
        if (forward > backward && forward > neither) {
            this.mob.forwardImpulse(1.0f);
        } else if (backward > neither) {
            this.mob.forwardImpulse(-1.0f);
        }
    }

    private void handleStrafing(float left, float right, float neither) {
        // I could use MoveControl#strafe, but there are some unwanted hardcoded behaviors
        if (left > right && left > neither) {
            this.mob.setXxa(1.0f);
        } else if (right > neither) {
            this.mob.setXxa(-1.0f);
        }
    }

    private void handleJump(float output) {
        if (output > 0) {
            this.mob.getJumpControl().jump();
        }
    }

    private void handleLookDirection(float x, float y) {
        this.mob.setXRot(this.mob.getXRot() + 360 * x);
        this.mob.setYRot(this.mob.getYRot() + 360 * y);
    }

    private void spamArrows(LivingEntity target) {
        if (this.mob.isUsingItem()) {
            int i = this.mob.getTicksUsingItem();
            if (i >= 5) {
                this.mob.stopUsingItem();
                this.mob.performRangedAttack(target, BowItem.getPowerForTime(i));
            }
        }
        else {
            this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof BowItem));
        }
    }

}
