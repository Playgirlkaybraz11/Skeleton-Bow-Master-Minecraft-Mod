package com.leecrafts.bowmaster.capability.livingentity;

import net.minecraft.world.phys.Vec3;

public class LivingEntityCap implements ILivingEntityCap {

    public double velocityX;
    public double velocityY;
    public double velocityZ;

    public LivingEntityCap() {
        this.velocityX = 0;
        this.velocityY = 0;
        this.velocityZ = 0;
    }

    @Override
    public Vec3 getVelocity() {
        return new Vec3(this.velocityX, this.velocityY, this.velocityZ);
    }

    @Override
    public void setVelocity(Vec3 velocity) {
        this.velocityX = velocity.x;
        this.velocityY = velocity.y;
        this.velocityZ = velocity.z;
    }
}
