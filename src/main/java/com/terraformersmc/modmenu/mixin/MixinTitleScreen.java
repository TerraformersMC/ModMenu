package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.updates.ModUpdateProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {
	private boolean hasShownUpdateToast = false;

	protected MixinTitleScreen(Text title) {
		super(title);
	}

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
			String outdated =  (ModUpdateProvider.availableUpdates.get() > 0 ? ".outdated" : "");
			String outdatedCount = ModUpdateProvider.availableUpdates + "";
			String newString = I18n.translate("modmenu.mods.n" + outdated, count, outdatedCount);
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

	@Inject(at = @At("TAIL"), method = "render")
	public void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (hasShownUpdateToast || client == null || ModMenuConfig.DISABLE_UPDATE_NOTIFICATION.getValue()) return;
		hasShownUpdateToast = true;
		if (ModUpdateProvider.availableUpdates.get() == 0) {
			return;
		}

		String descriptionKey = ModUpdateProvider.availableUpdates.get() == 1
				? "modmenu.updatesAvailableToast.description.1"
				: "modmenu.updatesAvailableToast.description.a";
		SystemToast.add(client.getToastManager(),
				SystemToast.Type.TUTORIAL_HINT,
				new TranslatableText("modmenu.updatesAvailableToast.title"),
				new TranslatableText(descriptionKey, ModUpdateProvider.availableUpdates.get()));
	}
}
