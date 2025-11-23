package com.khanhromvn.healthsystem.config;

import com.khanhromvn.healthsystem.stats.StatType;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Cấu hình cho hệ thống chỉ số.
 *
 * Dùng các multiplier để dễ cân bằng mà không phải sửa Enum gốc.
 * Các giá trị regen/drain thực tế = giá trị trong StatType * multiplier tương ứng.
 */
public class ModConfigs {

    public static final ForgeConfigSpec COMMON_SPEC;

    // Chung
    public static ForgeConfigSpec.BooleanValue DEBUG_OVERLAY_ENABLED;
    public static ForgeConfigSpec.IntValue SYNC_INTERVAL_TICKS;

    // Multiplier regen
    public static ForgeConfigSpec.DoubleValue TEMPERATURE_REGEN_MULT;
    public static ForgeConfigSpec.DoubleValue FATIGUE_REGEN_MULT;
    public static ForgeConfigSpec.DoubleValue BREATH_REGEN_MULT;
    public static ForgeConfigSpec.DoubleValue BLOOD_REGEN_MULT;
    public static ForgeConfigSpec.DoubleValue THIRST_REGEN_MULT;
    public static ForgeConfigSpec.DoubleValue HUNGER_EXT_REGEN_MULT;
    public static ForgeConfigSpec.DoubleValue STRENGTH_REGEN_MULT;
    public static ForgeConfigSpec.DoubleValue AGILITY_REGEN_MULT;
    public static ForgeConfigSpec.DoubleValue STAMINA_REGEN_MULT;
    public static ForgeConfigSpec.DoubleValue PSYCHOLOGY_REGEN_MULT;

    // Multiplier drain
    public static ForgeConfigSpec.DoubleValue TEMPERATURE_DRAIN_MULT;
    public static ForgeConfigSpec.DoubleValue FATIGUE_DRAIN_MULT;
    public static ForgeConfigSpec.DoubleValue BREATH_DRAIN_MULT;
    public static ForgeConfigSpec.DoubleValue BLOOD_DRAIN_MULT;
    public static ForgeConfigSpec.DoubleValue THIRST_DRAIN_MULT;
    public static ForgeConfigSpec.DoubleValue HUNGER_EXT_DRAIN_MULT;
    public static ForgeConfigSpec.DoubleValue STRENGTH_DRAIN_MULT;
    public static ForgeConfigSpec.DoubleValue AGILITY_DRAIN_MULT;
    public static ForgeConfigSpec.DoubleValue STAMINA_DRAIN_MULT;
    public static ForgeConfigSpec.DoubleValue PSYCHOLOGY_DRAIN_MULT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Cau hinh chung").push("general");
        DEBUG_OVERLAY_ENABLED = builder
                .comment("Bat/tat overlay debug hien thi tat ca stats.")
                .define("debugOverlayEnabled", true);
        SYNC_INTERVAL_TICKS = builder
                .comment("Khoang thoi gian dong bo stats server -> client (ticks). 20 = 1 giay.")
                .defineInRange("syncIntervalTicks", 20, 5, 200);
        builder.pop();

        addMultiplierSection(builder, "regenMultiplier", true);
        addMultiplierSection(builder, "drainMultiplier", false);

        COMMON_SPEC = builder.build();
    }

    private static void addMultiplierSection(ForgeConfigSpec.Builder builder, String category, boolean regen) {
        builder.comment("He so multiplier cho " + (regen ? "regen (hoi phuc)" : "drain (hao hut)") + " - 1.0 = mac dinh").push(category);

        double def = 1.0;
        // Regen hoặc drain: có thể khác nhau nhưng mặc định =1.0
        if (regen) {
            TEMPERATURE_REGEN_MULT = builder.defineInRange("temperature", def, 0.0, 10.0);
            FATIGUE_REGEN_MULT = builder.defineInRange("fatigue", def, 0.0, 10.0);
            BREATH_REGEN_MULT = builder.defineInRange("breath", def, 0.0, 10.0);
            BLOOD_REGEN_MULT = builder.defineInRange("blood", def, 0.0, 10.0);
            THIRST_REGEN_MULT = builder.defineInRange("thirst", def, 0.0, 10.0);
            HUNGER_EXT_REGEN_MULT = builder.defineInRange("hunger_extended", def, 0.0, 10.0);
            STRENGTH_REGEN_MULT = builder.defineInRange("strength", def, 0.0, 10.0);
            AGILITY_REGEN_MULT = builder.defineInRange("agility", def, 0.0, 10.0);
            STAMINA_REGEN_MULT = builder.defineInRange("stamina", def, 0.0, 10.0);
            PSYCHOLOGY_REGEN_MULT = builder.defineInRange("psychology", def, 0.0, 10.0);
        } else {
            TEMPERATURE_DRAIN_MULT = builder.defineInRange("temperature", def, 0.0, 10.0);
            FATIGUE_DRAIN_MULT = builder.defineInRange("fatigue", def, 0.0, 10.0);
            BREATH_DRAIN_MULT = builder.defineInRange("breath", def, 0.0, 10.0);
            BLOOD_DRAIN_MULT = builder.defineInRange("blood", def, 0.0, 10.0);
            THIRST_DRAIN_MULT = builder.defineInRange("thirst", def, 0.0, 10.0);
            HUNGER_EXT_DRAIN_MULT = builder.defineInRange("hunger_extended", def, 0.0, 10.0);
            STRENGTH_DRAIN_MULT = builder.defineInRange("strength", def, 0.0, 10.0);
            AGILITY_DRAIN_MULT = builder.defineInRange("agility", def, 0.0, 10.0);
            STAMINA_DRAIN_MULT = builder.defineInRange("stamina", def, 0.0, 10.0);
            PSYCHOLOGY_DRAIN_MULT = builder.defineInRange("psychology", def, 0.0, 10.0);
        }
        builder.pop();
    }

    // Helpers để lấy multiplier cho một stat (regen hoặc drain)
    public static double getRegenMult(StatType t) {
        switch (t) {
            case TEMPERATURE: return TEMPERATURE_REGEN_MULT.get();
            case FATIGUE: return FATIGUE_REGEN_MULT.get();
            case BREATH: return BREATH_REGEN_MULT.get();
            case BLOOD: return BLOOD_REGEN_MULT.get();
            case THIRST: return THIRST_REGEN_MULT.get();
            case HUNGER_EXTENDED: return HUNGER_EXT_REGEN_MULT.get();
            case STRENGTH: return STRENGTH_REGEN_MULT.get();
            case AGILITY: return AGILITY_REGEN_MULT.get();
            case STAMINA: return STAMINA_REGEN_MULT.get();
            case PSYCHOLOGY: return PSYCHOLOGY_REGEN_MULT.get();
        }
        return 1.0;
    }

    public static double getDrainMult(StatType t) {
        switch (t) {
            case TEMPERATURE: return TEMPERATURE_DRAIN_MULT.get();
            case FATIGUE: return FATIGUE_DRAIN_MULT.get();
            case BREATH: return BREATH_DRAIN_MULT.get();
            case BLOOD: return BLOOD_DRAIN_MULT.get();
            case THIRST: return THIRST_DRAIN_MULT.get();
            case HUNGER_EXTENDED: return HUNGER_EXT_DRAIN_MULT.get();
            case STRENGTH: return STRENGTH_DRAIN_MULT.get();
            case AGILITY: return AGILITY_DRAIN_MULT.get();
            case STAMINA: return STAMINA_DRAIN_MULT.get();
            case PSYCHOLOGY: return PSYCHOLOGY_DRAIN_MULT.get();
        }
        return 1.0;
    }
}
