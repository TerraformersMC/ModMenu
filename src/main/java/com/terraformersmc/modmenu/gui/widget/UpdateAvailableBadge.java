package com.terraformersmc.modmenu.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import static java.lang.Math.round;
import static java.lang.Math.clamp;

public class UpdateAvailableBadge {
    private static final Identifier UPDATE_ICON = Identifier.withDefaultNamespace("icon/trial_available");

    public static void renderBadge(GuiGraphicsExtractor drawContext, int x, int y) {
        renderBadge(drawContext, x, y, 1.0F);
    }

    public static void renderBadge(GuiGraphicsExtractor drawContext, int x, int y, float alpha) {
        int alphaByte = round(clamp(alpha, 0.0F, 1.0F) * 255.0F);
        int color = (alphaByte << 24) | 0x00FFFFFF;
        drawContext.blitSprite(RenderPipelines.GUI_TEXTURED, UPDATE_ICON, x, y, 8, 8, color);
    }


}
