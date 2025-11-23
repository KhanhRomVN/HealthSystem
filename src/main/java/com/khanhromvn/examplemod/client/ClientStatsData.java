package com.khanhromvn.healthsystem.client;

import com.khanhromvn.healthsystem.stats.StatType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Lưu trữ giá trị stats phía client (đồng bộ từ server).
 * Dùng để hiển thị HUD mà không cần request server mỗi frame.
 */
public class ClientStatsData {

    private static final Map<StatType, Integer> CLIENT_VALUES = new EnumMap<>(StatType.class);

    static {
        for (StatType t : StatType.values()) {
            CLIENT_VALUES.put(t, t.getBase());
        }
    }

    public static void setAll(Map<StatType, Integer> values) {
        for (StatType t : StatType.values()) {
            CLIENT_VALUES.put(t, values.getOrDefault(t, t.getBase()));
        }
    }

    public static int get(StatType type) {
        return CLIENT_VALUES.getOrDefault(type, type.getBase());
    }

    /**
     * Tạo chuỗi debug gọn cho overlay.
     */
    public static String buildDebugLine() {
        StringBuilder sb = new StringBuilder();
        for (StatType t : StatType.values()) {
            sb.append(t.name()).append(":").append(get(t)).append(" ");
        }
        return sb.toString();
    }
}
