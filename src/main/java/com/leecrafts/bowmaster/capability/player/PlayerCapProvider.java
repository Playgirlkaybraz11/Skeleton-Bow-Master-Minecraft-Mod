package com.leecrafts.bowmaster.capability.player;

import com.leecrafts.bowmaster.capability.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerCapProvider implements ICapabilitySerializable<CompoundTag> {

    private final PlayerCap playerCap = new PlayerCap();
    private final LazyOptional<IPlayerCap> playerCapLazyOptional = LazyOptional.of(() -> playerCap);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ModCapabilities.PLAYER_CAPABILITY.orEmpty(cap, playerCapLazyOptional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (ModCapabilities.PLAYER_CAPABILITY == null) return nbt;
        nbt.putIntArray("outside_dim_block_pos", playerCap.outsideDimBlockPos);
        nbt.putIntArray("arena_dim_block_pos", playerCap.arenaDimBlockPos);
        nbt.putInt("after_train_battle_counter", playerCap.afterTrainBattleCounter);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (ModCapabilities.PLAYER_CAPABILITY != null) {
            playerCap.outsideDimBlockPos = nbt.getIntArray("outside_dim_block_pos");
            playerCap.arenaDimBlockPos = nbt.getIntArray("arena_dim_block_pos");
            playerCap.afterTrainBattleCounter = nbt.getInt("after_train_battle_counter");
        }
    }

}
