package com.khanhromvn.healthsystem.command;

import com.khanhromvn.healthsystem.ExampleMod;
import com.khanhromvn.healthsystem.stats.IPlayerStats;
import com.khanhromvn.healthsystem.stats.PlayerStatsCapability;
import com.khanhromvn.healthsystem.stats.StatType;
import com.khanhromvn.healthsystem.network.ModNetwork;
import com.khanhromvn.healthsystem.network.packet.SyncStatsPacket;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Locale;

/**
 * Các lệnh debug để xem / chỉnh sửa chỉ số:
 *
 * /getstats                -> liệt kê toàn bộ
 * /setstat <name> <value>  -> đặt giá trị một stat
 *
 * name: TEMPERATURE, FATIGUE, BREATH, BLOOD, THIRST, HUNGER_EXTENDED, STRENGTH, AGILITY, STAMINA, PSYCHOLOGY
 * Chấp nhận viết thường.
 */
@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class StatsCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(buildRoot());
    }

    private static LiteralArgumentBuilder<CommandSource> buildRoot() {
        return Commands.literal("stats")
                .then(Commands.literal("get").executes(StatsCommand::handleGet))
 .then(Commands.literal("set")
 .then(Commands.argument("name", StringArgumentType.string())
 .then(Commands.argument("value", IntegerArgumentType.integer(-1000, 1000))
 .executes(StatsCommand::handleSet))))
 // Alias ngắn
 .then(Commands.literal("getstats").executes(StatsCommand::handleGet))
 .then(Commands.literal("setstat")
 .then(Commands.argument("name", StringArgumentType.string())
 .then(Commands.argument("value", IntegerArgumentType.integer(-1000, 1000))
 .executes(StatsCommand::handleSet))));
    }

    private static int handleGet(CommandContext<CommandSource> ctx) {
        CommandSource source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity)) {
            source.sendSuccess(new StringTextComponent("Chỉ người chơi có thể dùng lệnh này."), false);
            return 0;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        PlayerStatsCapability.get(player).ifPresent(stats -> {
            StringBuilder sb = new StringBuilder("Stats: ");
            for (StatType t : StatType.values()) {
                sb.append(t.name()).append("=").append(stats.get(t)).append(" ");
            }
            source.sendSuccess(new StringTextComponent(sb.toString()), false);
        });
        return Command.SINGLE_SUCCESS;
    }

    private static int handleSet(CommandContext<CommandSource> ctx) {
        CommandSource source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity)) {
            source.sendSuccess(new StringTextComponent("Chỉ người chơi có thể dùng lệnh này."), false);
            return 0;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        String name = StringArgumentType.getString(ctx, "name");
        int value = IntegerArgumentType.getInteger(ctx, "value");
        StatType target = parseStat(name);
        if (target == null) {
            source.sendSuccess(new StringTextComponent("Tên stat không hợp lệ: " + name), false);
            return 0;
        }
        PlayerStatsCapability.get(player).ifPresent(stats -> {
            stats.set(target, value);
            source.sendSuccess(new StringTextComponent("Đặt " + target.name() + " = " + stats.get(target)), false);
            // Gửi packet sync ngay
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), SyncStatsPacket.fromCapability(stats));
        });
        return Command.SINGLE_SUCCESS;
    }

    private static StatType parseStat(String raw) {
        String upper = raw.toUpperCase(Locale.ROOT);
        for (StatType t : StatType.values()) {
            if (t.name().equals(upper)) return t;
        }
        return null;
    }
}
