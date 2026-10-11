package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.DrawingUtil;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;

public class ModBadgeRenderer {
    protected int startX, startY, badgeX, badgeY, badgeMax;
    protected Mod mod;
    protected Minecraft client;
    protected final ModsScreen screen;

    public ModBadgeRenderer(int startX, int startY, int endX, Mod mod, ModsScreen screen) {
        this.startX = startX;
        this.startY = startY;
        this.badgeMax = endX;
        this.mod = mod;
        this.screen = screen;
        this.client = Minecraft.getInstance();
    }

    public void draw(GuiGraphicsExtractor drawContext, int mouseX, int mouseY) {
        this.badgeX = startX;
        this.badgeY = startY;
        Set<BadgeHolder> badges = mod.getBadges();
        badges.forEach(badge -> drawBadge(drawContext, badge.badge(), mouseX, mouseY));
    }

    public void drawBadge(GuiGraphicsExtractor drawContext, Mod.Badge badge, int mouseX, int mouseY) {
        this.drawBadge(
                drawContext,
                badge.getText().getVisualOrderText(),
                badge.getOutlineColor(),
                badge.getFillColor(),
                badge.textColor(),
                mouseX,
                mouseY
        );
    }

    public void drawBadge(
            GuiGraphicsExtractor drawContext,
            FormattedCharSequence text,
            int outlineColor,
            int fillColor,
            int textColor,
            int mouseX,
            int mouseY
    ) {
        int width = client.font.width(text) + 6;
        if (badgeX + width < badgeMax) {
            DrawingUtil.drawBadge(drawContext, badgeX, badgeY, width, text, outlineColor, fillColor, textColor);
            badgeX += width + 3;
        }
    }

    public Mod getMod() {
        return mod;
    }
}
