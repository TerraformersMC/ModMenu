package com.terraformersmc.modmenu.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.event.ModMenuEventHandler;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class MixinPauseScreen extends Screen {
    protected MixinPauseScreen(Component title) {
        super(title);
    }

    @Definition(id = "integratedServer", local = @Local(type = IntegratedServer.class, name = "integratedServer"))
    @Expression("integratedServer = ?")
    @Inject(method = "createPauseMenu", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void insertModMenuIconButton(CallbackInfo ci, @Local(name = "iconButtonRow") LinearLayout iconButtonRow) {
        if (!ModMenuConfig.MODIFY_GAME_MENU.getValue()) return;
        ModMenuConfig.GameMenuButtonStyle style = ModMenuConfig.GAME_MENU_BUTTON_STYLE.getValue();
        if (style == ModMenuConfig.GameMenuButtonStyle.ICON) {
            iconButtonRow.addChild(new UpdateCheckerTexturedButtonWidget(
                    0,
                    0,
                    20,
                    20,
                    0,
                    0,
                    20,
                    ModMenuEventHandler.MODS_BUTTON_TEXTURE,
                    32,
                    64,
                    _ -> Minecraft.getInstance().gui.setScreen(new ModsScreen(this)),
                    ModMenuApi.createModsButtonText()
            ));
        }

    }

    @Inject(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;ILnet/minecraft/client/gui/layouts/LayoutSettings;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 1, shift = At.Shift.AFTER))
    private void insertModMenuFullButton(CallbackInfo ci, @Local(name = "helper") GridLayout.RowHelper helper) {
        if (!ModMenuConfig.MODIFY_GAME_MENU.getValue()) return;
        ModMenuConfig.GameMenuButtonStyle style = ModMenuConfig.GAME_MENU_BUTTON_STYLE.getValue();
        if (style == ModMenuConfig.GameMenuButtonStyle.INSERT) {
            final int fullWidthButton = 204; // PauseScreen.BUTTON_WIDTH_FULL
            helper.addChild(new ModMenuButtonWidget(
                    0,
                    0,
                    fullWidthButton,
                    20,
                    ModMenuApi.createModsButtonText(),
                    this
            ), 2);
        }
    }
}
