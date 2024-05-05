package com.leecrafts.bowmaster.capability.player;

import net.minecraft.core.BlockPos;

public interface IPlayerCap {

    void setOutsideDimBlockPos(BlockPos blockPos);
    BlockPos getOutsideDimBlockPos();
    void setArenaDimBlockPos(BlockPos blockPos);
    BlockPos getArenaDimBlockPos();

}
