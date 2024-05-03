package com.leecrafts.bowmaster.capability.livingentity;

import com.leecrafts.bowmaster.capability.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LivingEntityCapProvider implements ICapabilitySerializable<CompoundTag> {

    private final LivingEntityCap livingEntityCap = new LivingEntityCap();
    private final LazyOptional<ILivingEntityCap> livingEntityCapLazyOptional = LazyOptional.of(() -> livingEntityCap);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ModCapabilities.LIVING_ENTITY_CAPABILITY.orEmpty(cap, livingEntityCapLazyOptional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (ModCapabilities.LIVING_ENTITY_CAPABILITY == null) return nbt;
        nbt.putDouble("velocity_x", livingEntityCap.velocityX);
        nbt.putDouble("velocity_y", livingEntityCap.velocityY);
        nbt.putDouble("velocity_z", livingEntityCap.velocityZ);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (ModCapabilities.LIVING_ENTITY_CAPABILITY != null) {
            livingEntityCap.velocityX = nbt.getDouble("velocity_x");
            livingEntityCap.velocityY = nbt.getDouble("velocity_y");
            livingEntityCap.velocityZ = nbt.getDouble("velocity_z");
        }
    }

    public void invalidate() {
        livingEntityCapLazyOptional.invalidate();
    }

}
