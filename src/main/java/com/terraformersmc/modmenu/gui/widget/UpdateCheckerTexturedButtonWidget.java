package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.resources.Identifier;

public class UpdateCheckerTexturedButtonWidget extends LegacyTexturedButtonWidget {
    public UpdateCheckerTexturedButtonWidget(
            int x,
            int y,
            int width,
            int height,
            int u,
            int v,
            int hoveredVOffset,
            Identifier texture,
            int textureWidth,
            int textureHeight,
            Button.OnPress pressAction,
            net.minecraft.network.chat.Component message
    ) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction, message);
        setTooltip(Tooltip.create(message));
    }

    @Override
    public void extractContents(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta) {
        super.extractContents(drawContext, mouseX, mouseY, delta);
        if (ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.areModUpdatesAvailable() && getAlpha() >= 1.0f) {
            UpdateAvailableBadge.renderBadge(drawContext, this.getX() + this.width - 5, this.getY() - 3);
        }
    }
}
