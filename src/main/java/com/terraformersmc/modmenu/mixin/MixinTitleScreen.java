package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.updates.ModUpdateProvider;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.resource.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
	@ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init(Lnet/minecraft/client/MinecraftClient;II)V"), method = "init", index = 2)
	private int adjustRealmsHeight(int height) {
		if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue() && ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.CLASSIC) {
			return height - 51;
		} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.REPLACE_REALMS || ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.SHRINK) {
			return -99999;
		}
		return height;
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/TitleScreen;drawStringWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V", ordinal = 0))
	private String onRender(String string) {
		if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue() && ModMenuConfig.MOD_COUNT_LOCATION.getValue().isOnTitleScreen()) {
			String count = ModMenu.getDisplayedModCount();
			String outdated =  (ModUpdateProvider.availableUpdates > 0 ? ".outdated" : "");
			String outdatedCount = ModUpdateProvider.availableUpdates + "";
			String newString = I18n.translate("modmenu.mods.a" + outdated, count, outdatedCount);
			String countKey = "modmenu.mods." + count + outdated;
			if ("69".equals(count) && ModMenuConfig.EASTER_EGGS.getValue()) {
				newString = I18n.translate(countKey + ".nice" + outdated, count, outdatedCount);
			} else if (I18n.hasTranslation(countKey)) {
				newString = I18n.translate(countKey + outdated, count, outdatedCount);
			}

			return string.replace(I18n.translate(I18n.translate("menu.modded")), newString);
		}
		return string;
	}
}
