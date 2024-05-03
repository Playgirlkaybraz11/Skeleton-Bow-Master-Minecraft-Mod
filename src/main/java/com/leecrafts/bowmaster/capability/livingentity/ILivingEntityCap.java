package com.leecrafts.bowmaster.capability.livingentity;

import net.minecraft.world.phys.Vec3;

public interface ILivingEntityCap {

    Vec3 getVelocity();
    void setVelocity(Vec3 velocity);

}
