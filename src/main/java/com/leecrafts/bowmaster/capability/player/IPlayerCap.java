package com.leecrafts.bowmaster.capability.player;

import net.minecraft.core.BlockPos;

public interface IPlayerCap {

    void setOutsideDimBlockPos(BlockPos blockPos);
    void setArenaDimBlockPos(BlockPos blockPos);

}
