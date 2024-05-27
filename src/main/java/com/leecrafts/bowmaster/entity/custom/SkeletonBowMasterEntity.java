package com.leecrafts.bowmaster.entity.custom;

import com.leecrafts.bowmaster.entity.goal.AIRangedBowAttackGoal;
import com.leecrafts.bowmaster.util.MultiOutputFreeformNetwork;
import com.leecrafts.bowmaster.util.NeuralNetworkUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SkeletonBowMasterEntity extends AbstractSkeleton {

    public static final boolean TRAINING = true;
    protected boolean shouldForwardImpulse = false;
    private final MultiOutputFreeformNetwork network;
    private final ArrayList<double[]> states = new ArrayList<>();
    private final ArrayList<double[]> actions = new ArrayList<>();
    private final ArrayList<Double> rewards = new ArrayList<>();

    public SkeletonBowMasterEntity(EntityType<? extends AbstractSkeleton> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.network = NeuralNetworkUtil.loadOrCreateModel();
        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
//        super.registerGoals();
        this.goalSelector.addGoal(2, new AIRangedBowAttackGoal<>(this));
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, SkeletonBowMasterEntity.class, false));
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 128);
    }

    @Override
    public void setZza(float pAmount) {
        if ((this.shouldForwardImpulse && pAmount != 0) ||
                (!this.shouldForwardImpulse && pAmount == 0)) {
            super.setZza(pAmount);
        }
    }

    public void forwardImpulse(float amount) {
        this.setZza(amount);
        this.shouldForwardImpulse = amount != 0;
    }

    public MultiOutputFreeformNetwork getNetwork() {
        return this.network;
    }

    @Override
    protected @NotNull SoundEvent getStepSound() {
        return SoundEvents.SKELETON_STEP;
    }

    @Override
    public void reassessWeaponGoal() {
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity pTarget, float pDistanceFactor) {
        ItemStack itemstack = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof net.minecraft.world.item.BowItem)));
        AbstractArrow abstractarrow = this.getArrow(itemstack, pDistanceFactor);
        if (this.getMainHandItem().getItem() instanceof net.minecraft.world.item.BowItem)
            abstractarrow = ((net.minecraft.world.item.BowItem)this.getMainHandItem().getItem()).customArrow(abstractarrow);
//        double d0 = pTarget.getX() - this.getX();
//        double d1 = pTarget.getY(0.3333333333333333D) - abstractarrow.getY();
//        double d2 = pTarget.getZ() - this.getZ();
//        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        Vec3 vec3 = this.getLookAngle();
        abstractarrow.shoot(vec3.x, vec3.y, vec3.z, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(abstractarrow);
    }

    public void storeStates(double[] observations) {
        this.states.add(observations);
    }

    public void storeActions(double[] actionOutputs) {
        this.actions.add(actionOutputs);
    }

    public void storeRewards(double reward) {
        if (!this.rewards.isEmpty() && reward > 0) {
            this.rewards.set(this.rewards.size() - 1, reward);
        }
        else {
            this.rewards.add(reward);
        }
    }

    public ArrayList<double[]> getStates() {
        return this.states;
    }

    public ArrayList<double[]> getActions() {
        return this.actions;
    }

    public ArrayList<Double> getRewards() {
        return this.rewards;
    }

}
