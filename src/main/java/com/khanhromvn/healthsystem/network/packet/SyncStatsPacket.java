package com.khanhromvn.healthsystem.network.packet;

import com.khanhromvn.healthsystem.stats.IPlayerStats;
import com.khanhromvn.healthsystem.stats.StatType;
import com.khanhromvn.healthsystem.client.ClientStatsData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Gói tin đồng bộ toàn bộ các chỉ số từ server -> client.
 * Được gửi định kỳ hoặc khi thay đổi lớn.
 */
public class SyncStatsPacket {

    private final Map<StatType, Integer> values = new EnumMap<>(StatType.class);

    public SyncStatsPacket(Map<StatType, Integer> source) {
        for (StatType t : StatType.values()) {
            values.put(t, source.getOrDefault(t, t.getBase()));
        }
    }

    public Map<StatType, Integer> getValues() {
        return values;
    }

    // Encode: ghi số lượng và sau đó các cặp (ordinal, value)
    public static void encode(SyncStatsPacket pkt, PacketBuffer buf) {
        buf.writeVarInt(pkt.values.size());
        for (Map.Entry<StatType, Integer> e : pkt.values.entrySet()) {
            buf.writeVarInt(e.getKey().ordinal());
            buf.writeVarInt(e.getValue());
        }
    }

    public static SyncStatsPacket decode(PacketBuffer buf) {
        int size = buf.readVarInt();
        Map<StatType, Integer> map = new EnumMap<>(StatType.class);
        for (int i = 0; i < size; i++) {
            int ord = buf.readVarInt();
            int val = buf.readVarInt();
            StatType t = StatType.values()[ord];
            map.put(t, val);
        }
        return new SyncStatsPacket(map);
    }

    public static void handle(SyncStatsPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        if (ctx.getDirection().getReceptionSide().isClient()) {
            ctx.enqueueWork(() -> {
                // Cập nhật dữ liệu client
                ClientStatsData.setAll(pkt.getValues());
            });
        }
        ctx.setPacketHandled(true);
    }

    /**
     * Helper tạo từ capability instance.
     */
    public static SyncStatsPacket fromCapability(IPlayerStats stats) {
        Map<StatType, Integer> map = new EnumMap<>(StatType.class);
        for (StatType t : StatType.values()) {
            map.put(t, stats.get(t));
        }
        return new SyncStatsPacket(map);
    }
}
