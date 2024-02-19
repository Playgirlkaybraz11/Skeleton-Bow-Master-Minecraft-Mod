package com.leecrafts.bowmaster.capability;

import com.leecrafts.bowmaster.capability.player.IPlayerCap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ModCapabilities {

    public static final Capability<IPlayerCap> PLAYER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

}
