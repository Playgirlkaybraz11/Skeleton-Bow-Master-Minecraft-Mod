package com.leecrafts.bowmaster.packet;

import com.leecrafts.bowmaster.SkeletonBowMaster;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.SimpleChannel;

public class PacketHandler {

//    private static final String PROTOCOL_VERSION = "1";
//
//    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
//            new ResourceLocation(SkeletonBowMaster.MODID, "main"), () -> PROTOCOL_VERSION,
//            PROTOCOL_VERSION::equals,
//            PROTOCOL_VERSION::equals
//    );

    public static final SimpleChannel INSTANCE =
            ChannelBuilder.named(new ResourceLocation(SkeletonBowMaster.MODID, "main")).simpleChannel();

    private PacketHandler() {
    }

    public static void init() {
        int index = 0;
        INSTANCE.messageBuilder(ServerboundLivingEntityVelocityPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundLivingEntityVelocityPacket::encode).decoder(ServerboundLivingEntityVelocityPacket::new)
                .consumerMainThread(ServerboundLivingEntityVelocityPacket::handle).add();
    }

}
