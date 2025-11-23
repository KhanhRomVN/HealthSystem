package com.khanhromvn.healthsystem.stats;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ArmorItem;
import java.util.EnumMap;
import java.util.Map;
import com.khanhromvn.healthsystem.config.ModConfigs;

/**
 * Interface lưu trữ & thao tác với các chỉ số mở rộng cho người chơi.
 *
 * Thiết kế:
 *  - Mỗi StatType có một giá trị int hiện tại.
 *  - Cung cấp phương thức tickUpdate(PlayerEntity) để xử lý logic thay đổi theo thời gian.
 *  - Cho phép serialize/deserialize sang NBT để đồng bộ & lưu vào player persistent data.
 *
 * Ghi chú logic (sẽ triển khai cụ thể sau):
 *  - TEMPERATURE: chịu ảnh hưởng bởi biome, độ cao, trạng thái (ướt, lửa).
 *  - FATIGUE: tăng khi mining, chạy, nhảy; giảm khi đứng yên hoặc ngủ.
 *  - BREATH: giảm nhanh khi ở dưới nước / ngạt, hồi nhanh khi trên cạn.
 *  - BLOOD: giảm khi nhận sát thương nặng, hồi rất chậm; nếu quá thấp có thể gây hiệu ứng yếu.
 *  - THIRST: giảm dần theo thời gian; hồi khi uống (event sẽ hook sau).
 *  - HUNGER_EXTENDED: có thể đồng bộ với hệ thống đói vanilla, giảm khi chạy / hồi khi ăn.
 *  - STRENGTH: giảm dần khi người chơi làm việc nặng lâu (progression dài), hồi rất chậm.
 *  - AGILITY: có thể tạm coi là luôn đầy, giảm nhẹ khi mang giáp nặng - sẽ tính sau.
 *  - STAMINA: giảm nhanh khi chạy nước rút, hồi nhanh khi đứng yên / đi bộ.
 *  - PSYCHOLOGY: giảm ở ban đêm, khi gần mob nguy hiểm, hồi khi an toàn ban ngày.
 */
public interface IPlayerStats {

    /**
     * Lấy giá trị hiện tại của stat.
     */
    int get(StatType type);

    /**
     * Đặt giá trị (đã clamp).
     */
    void set(StatType type, int value);

    /**
     * Thay đổi tương đối (delta, có thể âm hoặc dương) và tự clamp.
     */
    void change(StatType type, int delta);

    /**
     * Tick logic cập nhật tất cả stat theo thời gian.
     */
    void tickUpdate(PlayerEntity player);

    /**
     * Ghi dữ liệu ra NBT.
     */
    CompoundNBT serializeNBT();

    /**
     * Đọc dữ liệu từ NBT.
     */
    void deserializeNBT(CompoundNBT nbt);

    /**
     * Tạo instance mặc định.
     */
    static IPlayerStats create() {
        return new PlayerStatsImpl();
    }

    /**
     * Triển khai mặc định bên trong interface cho đơn giản bước đầu.
     * Sau có thể tách ra file riêng nếu muốn sạch sẽ hơn.
     */
    class PlayerStatsImpl implements IPlayerStats {
        private final Map<StatType, Integer> values = new EnumMap<>(StatType.class);

        public PlayerStatsImpl() {
            for (StatType t : StatType.values()) {
                values.put(t, t.getBase());
            }
        }

        @Override
        public int get(StatType type) {
            return values.getOrDefault(type, type.getBase());
        }

        @Override
        public void set(StatType type, int value) {
            values.put(type, type.clamp(value));
        }

        @Override
        public void change(StatType type, int delta) {
            int cur = get(type);
            set(type, cur + delta);
        }

        private void applyDrain(StatType type, double base) {
            // base là tốc độ drain/hao trong StatType; multiplier điều chỉnh
            int amt = (int) Math.round(base * ModConfigs.getDrainMult(type));
            if (amt != 0) change(type, -amt);
        }

        private void applyRegen(StatType type, double base) {
            int amt = (int) Math.round(base * ModConfigs.getRegenMult(type));
            if (amt != 0) change(type, amt);
        }

        @Override
        public void tickUpdate(PlayerEntity player) {
            // ========== STAMINA ==========
            if (player.isSprinting()) {
                applyDrain(StatType.STAMINA, StatType.STAMINA.getDrainRate());
                if (get(StatType.STAMINA) <= StatType.STAMINA.getMin() + 5) {
                    // Ý tưởng: thêm hiệu ứng làm chậm nếu stamina cạn
                }
            } else {
                applyRegen(StatType.STAMINA, StatType.STAMINA.getRegenRate());
            }

            // ========== FATIGUE ==========
            if (player.isSprinting() || player.swinging) {
                // mệt mỏi tăng
                applyDrain(StatType.FATIGUE, StatType.FATIGUE.getDrainRate());
            } else {
                // nghỉ ngơi hồi phục (giảm mệt mỏi)
                applyRegen(StatType.FATIGUE, StatType.FATIGUE.getRegenRate());
            }

            // ========== BREATH ==========
            if (player.isUnderWater()) {
                applyDrain(StatType.BREATH, StatType.BREATH.getDrainRate());
            } else {
                applyRegen(StatType.BREATH, StatType.BREATH.getRegenRate());
            }

            // ========== THIRST & HUNGER_EXTENDED ==========
            applyDrain(StatType.THIRST, StatType.THIRST.getDrainRate());
            if (player.isSprinting()) {
                applyDrain(StatType.HUNGER_EXTENDED, StatType.HUNGER_EXTENDED.getDrainRate());
            } else {
                // hao nền rất nhẹ không dùng multiplier để tránh về 0 nếu config quá bé
                change(StatType.HUNGER_EXTENDED, -1);
            }

            // ========== PSYCHOLOGY ==========
            if (!player.level.isDay()) {
                applyDrain(StatType.PSYCHOLOGY, StatType.PSYCHOLOGY.getDrainRate());
            } else {
                applyRegen(StatType.PSYCHOLOGY, StatType.PSYCHOLOGY.getRegenRate());
            }

            // ========== TEMPERATURE ==========
            float biomeTemp = player.level.getBiome(player.blockPosition()).getBaseTemperature();
            int currentTemp = get(StatType.TEMPERATURE);
            int targetTemp = 50 + (int) ((biomeTemp - 0.8f) * 30f);

            if (player.isOnFire()) targetTemp += 15;
            if (player.isInLava()) targetTemp += 25;
            if (player.isUnderWater()) targetTemp -= 10;
            if (player.blockPosition().getY() > 120) targetTemp -= 5;

            int armorDurabilitySum = 0;
            for (ItemStack piece : player.getArmorSlots()) armorDurabilitySum += piece.getMaxDamage();
            if (armorDurabilitySum < 500) targetTemp += 2; else targetTemp -= 2;

            if (currentTemp < targetTemp) change(StatType.TEMPERATURE, 1);
            else if (currentTemp > targetTemp) change(StatType.TEMPERATURE, -1);

            // ========== STRENGTH ==========
            if (player.isSprinting() || player.swinging) {
                applyDrain(StatType.STRENGTH, StatType.STRENGTH.getDrainRate());
            } else if (player.tickCount % 200 == 0) {
                applyRegen(StatType.STRENGTH, StatType.STRENGTH.getRegenRate());
            }

            // ========== AGILITY ==========
            if (armorDurabilitySum > 1000) {
                applyDrain(StatType.AGILITY, StatType.AGILITY.getDrainRate());
            } else {
                applyRegen(StatType.AGILITY, StatType.AGILITY.getRegenRate());
            }

            // ========== BLOOD ==========
            if (player.tickCount % 100 == 0 && get(StatType.BLOOD) < StatType.BLOOD.getMax()) {
                applyRegen(StatType.BLOOD, StatType.BLOOD.getRegenRate());
            }

            // Hiệu ứng nếu BLOOD thấp
            if (get(StatType.BLOOD) < 30) {
                player.addEffect(new EffectInstance(Effects.WEAKNESS, 40, 0, true, false));
            }
 if (get(StatType.BLOOD) < 15) {
 player.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 40, 1, true, false));
 }
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT tag = new CompoundNBT();
            for (Map.Entry<StatType, Integer> e : values.entrySet()) {
                tag.putInt(e.getKey().name(), e.getValue());
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            for (StatType t : StatType.values()) {
                if (nbt.contains(t.name())) {
                    set(t, nbt.getInt(t.name()));
                }
            }
        }
    }
}
