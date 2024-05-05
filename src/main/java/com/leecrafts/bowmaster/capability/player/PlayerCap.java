package com.leecrafts.bowmaster.capability.player;

import net.minecraft.core.BlockPos;

public class PlayerCap implements IPlayerCap {

    // the block position the player was at before teleporting to the arena dimension
    public int[] outsideDimBlockPos;
    public int[] arenaDimBlockPos;
    public int afterTrainBattleCounter;
    public static final int afterTrainBattleCounterLimit = 30; // 1.5 seconds

    public PlayerCap() {
        this.outsideDimBlockPos = new int[] {0, 0, 0};
        this.arenaDimBlockPos = new int[] {0, 0, 0};
        this.afterTrainBattleCounter = -1;
    }

    @Override
    public void setOutsideDimBlockPos(BlockPos blockPos) {
        if (this.outsideDimBlockPos.length == 0) {
            this.outsideDimBlockPos = new int[] {0, 0, 0};
        }
        this.outsideDimBlockPos[0] = blockPos.getX();
        this.outsideDimBlockPos[1] = blockPos.getY();
        this.outsideDimBlockPos[2] = blockPos.getZ();
    }

    @Override
    public BlockPos getOutsideDimBlockPos() {
        if (this.outsideDimBlockPos.length == 0) {
            this.outsideDimBlockPos = new int[] {0, 0, 0};
        }
        return new BlockPos(this.outsideDimBlockPos[0], this.outsideDimBlockPos[1], this.outsideDimBlockPos[2]);
    }

    @Override
    public void setArenaDimBlockPos(BlockPos blockPos) {
        if (this.arenaDimBlockPos.length == 0) { // sometimes this happens for some reason
            this.arenaDimBlockPos = new int[] {0, 0, 0};
        }
        this.arenaDimBlockPos[0] = blockPos.getX();
        this.arenaDimBlockPos[1] = blockPos.getY();
        this.arenaDimBlockPos[2] = blockPos.getZ();
    }

    @Override
    public BlockPos getArenaDimBlockPos() {
        if (this.arenaDimBlockPos.length == 0) {
            this.arenaDimBlockPos = new int[] {0, 0, 0};
        }
        return new BlockPos(this.arenaDimBlockPos[0], this.arenaDimBlockPos[1], this.arenaDimBlockPos[2]);
    }

}
