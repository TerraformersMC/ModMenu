package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuButtonWidget extends Button.Plain {
    public ModMenuButtonWidget(int x, int y, int width, int height, net.minecraft.network.chat.Component text, Screen screen) {
        super(
                x,
                y,
                width,
                height,
                text,
                button -> Minecraft.getInstance().gui.setScreen(new ModsScreen(screen)),
                Button.DEFAULT_NARRATION
        );
    }

    @Override
    public void extractContents(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta) {
        super.extractContents(drawContext, mouseX, mouseY, delta);
        if (ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.areModUpdatesAvailable()) {
            UpdateAvailableBadge.renderBadge(
                    drawContext,
                    this.width + this.getX() - 13,
                    this.height / 2 + this.getY() - 5
            );
        }
    }
}
