package com.khanhromvn.healthsystem.network;

import com.khanhromvn.healthsystem.ExampleMod;
import com.khanhromvn.healthsystem.network.packet.SyncStatsPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/**
 * Quản lý kênh mạng đơn giản cho mod.
 * Hiện tại chỉ có gói tin đồng bộ toàn bộ stats về client.
 */
public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExampleMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextId = 0;

    public static void init() {
        CHANNEL.registerMessage(nextId++,
                SyncStatsPacket.class,
                SyncStatsPacket::encode,
                SyncStatsPacket::decode,
                SyncStatsPacket::handle);
    }
}
