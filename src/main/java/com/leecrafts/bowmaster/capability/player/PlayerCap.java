package com.leecrafts.bowmaster.capability.player;

import net.minecraft.core.BlockPos;

public class PlayerCap implements IPlayerCap {

    // the block position the player was at before teleporting to the arena dimension
    public int[] outsideDimBlockPos;
    public int[] arenaDimBlockPos;

    public PlayerCap() {
        this.outsideDimBlockPos = new int[] {0, 0, 0};
        this.arenaDimBlockPos = new int[] {0, 0, 0};
    }

    @Override
    public void setOutsideDimBlockPos(BlockPos blockPos) {
        this.outsideDimBlockPos[0] = blockPos.getX();
        this.outsideDimBlockPos[1] = blockPos.getY();
        this.outsideDimBlockPos[2] = blockPos.getZ();
    }

    @Override
    public void setArenaDimBlockPos(BlockPos blockPos) {
        this.arenaDimBlockPos[0] = blockPos.getX();
        this.arenaDimBlockPos[1] = blockPos.getY();
        this.arenaDimBlockPos[2] = blockPos.getZ();
    }

}
