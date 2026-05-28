package com.terraformersmc.modmenu.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class UpdateAvailableBadge {
    private static final Identifier UPDATE_ICON = Identifier.withDefaultNamespace("icon/trial_available");

    public static void renderBadge(GuiGraphicsExtractor drawContext, int x, int y) {
        drawContext.blitSprite(RenderPipelines.GUI_TEXTURED, UPDATE_ICON, x, y, 8, 8, 0xFFFFFFFF);
    }
}
