package com.khanhromvn.healthsystem.stats;

/**
 * Định nghĩa các loại chỉ số mở rộng cho người chơi.
 * Mỗi StatType có:
 *  - min: giá trị tối thiểu
 *  - max: giá trị tối đa
 *  - base: giá trị khởi tạo mặc định
 *  - regenRate: tốc độ hồi phục (mỗi tick hoặc mỗi chu kỳ logic, đơn vị trừu tượng)
 *  - drainRate: tốc độ hao hụt cơ bản (nếu > 0)
 *
 * Các logic chi tiết sẽ được áp dụng sau trong PlayerTick handler.
 */
public enum StatType {
    TEMPERATURE(0, 100, 50, 0.05f, 0.05f),
    FATIGUE(0, 100, 0, 0.2f, 0.1f),          // Mệt mỏi tăng (drainRate dùng như tốc độ tăng), regenRate = giảm mệt mỏi
    BREATH(0, 100, 100, 2f, 1f),
    BLOOD(0, 100, 100, 0.05f, 0.02f),
    THIRST(0, 100, 100, 0.5f, 0.3f),
    HUNGER_EXTENDED(0, 100, 100, 0.4f, 0.25f), // Mở rộng đói (song song hunger vanilla)
    STRENGTH(0, 100, 100, 0.1f, 0.05f),       // Độ bền / thể lực lâu dài (không phải stamina tức thời)
    AGILITY(0, 100, 100, 0.1f, 0.05f),
    STAMINA(0, 100, 100, 1.8f, 0.8f),         // Stamina hao khi chạy, hồi khi đứng yên
    PSYCHOLOGY(0, 100, 100, 0.15f, 0.1f);     // Tâm lý giảm khi ở đêm / sự kiện, hồi khi an toàn

    private final int min;
    private final int max;
    private final int base;
    private final float regenRate;
    private final float drainRate;

    StatType(int min, int max, int base, float regenRate, float drainRate) {
        this.min = min;
        this.max = max;
        this.base = base;
        this.regenRate = regenRate;
        this.drainRate = drainRate;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getBase() {
        return base;
    }

    public float getRegenRate() {
        return regenRate;
    }

    public float getDrainRate() {
        return drainRate;
    }

    /**
     * Chuẩn hoá giá trị về trong khoảng [min, max]
     */
    public int clamp(int value) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
