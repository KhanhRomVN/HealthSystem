package com.khanhromvn.healthsystem.client;

import com.khanhromvn.healthsystem.ExampleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Vẽ HUD đơn giản hiển thị dòng debug các chỉ số.
 * Tạm thời hiển thị dạng text ở góc trái trên.
 * Sau có thể thay bằng icon / thanh tiến trình đẹp hơn.
 */
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ClientOverlayEvents {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        String line = ClientStatsData.buildDebugLine();
        FontRenderer fr = mc.font;
        // Vẽ ở toạ độ (5,5) màu trắng
        event.getMatrixStack().pushPose();
        fr.drawShadow(event.getMatrixStack(), line, 5, 5, 0xFFFFFF);
        event.getMatrixStack().popPose();
    }
}
