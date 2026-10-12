package com.terraformersmc.modmenu.gui.widget;


import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ScaledWidgetSprites;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class SmallModMenuButtonWidget extends SpriteIconButton {
    public static final Identifier MODS_SPRITE_ENABLED = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, "mods/enabled");
    public static final Identifier MODS_SPRITE_DISABLED = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, "mods/disabled");
    public static final Identifier MODS_SPRITE_FOCUSED = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, "mods/focused");

    public SmallModMenuButtonWidget(
            int x,
            int y,
            int width,
            int height,
            Component message,
            int spriteWidth,
            int spriteHeight,
            int spriteOffsetX,
            int spriteOffsetY,
            WidgetSprites sprite,
            OnPress onPress,
            @Nullable Component tooltip,
            CreateNarration narration,
            boolean switchToLoadingAfterPress) {
        super(x, y, width, height, message, new ScaledWidgetSprites(sprite, spriteWidth, spriteHeight), DisplayState.ICON_ONLY, onPress, tooltip == null ? null : Tooltip.create(tooltip), narration, switchToLoadingAfterPress);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta) {
        super.extractContents(drawContext, mouseX, mouseY, delta);
        if (ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.areModUpdatesAvailable() && getAlpha() >= 1.0f) {
            UpdateAvailableBadge.renderBadge(drawContext, this.getX() + this.width - 5, this.getY() - 3);
        }
    }
}
