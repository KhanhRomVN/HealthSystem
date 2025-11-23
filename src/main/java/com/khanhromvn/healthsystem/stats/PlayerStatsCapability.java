package com.khanhromvn.healthsystem.stats;

import com.khanhromvn.healthsystem.ExampleMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;


import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;
import com.khanhromvn.healthsystem.network.ModNetwork;
import com.khanhromvn.healthsystem.network.packet.SyncStatsPacket;

/**
 * Capability hệ thống chỉ số mở rộng cho người chơi.
 *
 * Quy trình:
 *  1. register() được gọi trong mod setup để đăng ký capability vào Forge.
 *  2. attachCapabilities lắng nghe sự kiện và gắn provider vào PlayerEntity.
 *  3. PlayerTickEvent gọi tickUpdate để cập nhật các chỉ số mỗi tick (server side).
 *
 * Sau:
 *  - Sẽ thêm cơ chế đồng bộ mạng đến client (packet) và HUD hiển thị.
 */
public class PlayerStatsCapability {

    public static final ResourceLocation ID = new ResourceLocation(ExampleMod.MODID, "player_stats");

 // Capability instance - sẽ được đăng ký thông qua CapabilityManager
 public static Capability<IPlayerStats> PLAYER_STATS_CAPABILITY = null;

 /**
 * Đăng ký capability với Forge (kiểu cũ cho Minecraft 1.16.5).
 */
 public static void register() {
 CapabilityManager.INSTANCE.register(
 IPlayerStats.class, 
 new Storage(), 
 IPlayerStats.PlayerStatsImpl::new
 );
 // Trong Minecraft 1.16.5, capability sẽ được tạo tự động sau khi register
 // Chúng ta sẽ lấy nó thông qua CapabilityManager.get()
 // Note: Trong 1.16.5, capability được tạo tự động sau khi register, không cần getCapability
 }

    /**
     * Provider thực thi việc expose capability ra bên ngoài.
     */
    public static class Provider implements ICapabilityProvider, ICapabilitySerializable<INBT> {

        private final IPlayerStats backend = IPlayerStats.create();
        private final LazyOptional<IPlayerStats> optional = LazyOptional.of(() -> backend);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap == PLAYER_STATS_CAPABILITY ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public INBT serializeNBT() {
            return backend.serializeNBT();
        }

        @Override
        public void deserializeNBT(INBT nbt) {
            if (nbt instanceof CompoundNBT) {
                backend.deserializeNBT((CompoundNBT) nbt);
            }
        }
    }

    /**
     * Event subscribers cho attach và tick.
     */
    @Mod.EventBusSubscriber(modid = ExampleMod.MODID)
    public static class Events {

        @SubscribeEvent
        public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof PlayerEntity) {
                event.addCapability(ID, new Provider());
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            // Chỉ xử lý ở server END phase để không nhân đôi và có trạng thái cuối ổn định.
            if (event.phase == TickEvent.Phase.END && !event.player.level.isClientSide) {
                event.player.getCapability(PLAYER_STATS_CAPABILITY).ifPresent(stats -> {
                    stats.tickUpdate(event.player);
                    // Gửi packet định kỳ mỗi 20 ticks (1s)
                    if (event.player.tickCount % 20 == 0) {
                        if (event.player instanceof ServerPlayerEntity) {
                            ModNetwork.CHANNEL.send(
                                    PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.player),
                                    SyncStatsPacket.fromCapability(stats)
                            );
                        }
                    }
                });
            }
        }
    }

    /**
     * Storage cũ (nếu cần dùng register manual). Giữ lại làm tham chiếu.
     */
    public static class Storage implements Capability.IStorage<IPlayerStats> {
        @Override
        public INBT writeNBT(Capability<IPlayerStats> capability, IPlayerStats instance, Direction side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<IPlayerStats> capability, IPlayerStats instance, Direction side, INBT nbt) {
            if (nbt instanceof CompoundNBT) {
                instance.deserializeNBT((CompoundNBT) nbt);
            }
        }
    }

    /**
     * Helper lấy stats an toàn.
     */
    public static LazyOptional<IPlayerStats> get(PlayerEntity player) {
        return player.getCapability(PLAYER_STATS_CAPABILITY);
    }
}
