package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.event.ModMenuEventHandler;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(GameMenuScreen.class)
public abstract class MixinGameMenu extends Screen {
	protected MixinGameMenu(Text title) {
		super(title);
	}

	@Inject(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget;forEachChild(Ljava/util/function/Consumer;)V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void onInitWidgets(CallbackInfo ci, GridWidget gridWidget, GridWidget.Adder adder, Text text) {
		if (gridWidget != null) {
			final List<Widget> buttons = ((AccessorGridWidget) gridWidget).getChildren();
			if (ModMenuConfig.MODIFY_GAME_MENU.getValue()) {
				int modsButtonIndex = -1;
				final int spacing = 24;
				int buttonsY = this.height / 4 + 8;
				ModMenuConfig.GameMenuButtonStyle style = ModMenuConfig.GAME_MENU_BUTTON_STYLE.getValue();
				int reportBugsY = this.height / 4 + 72 - 16 + 1;
				for (int i = 0; i < buttons.size(); i++) {
					Widget widget = buttons.get(i);
					if (style == ModMenuConfig.GameMenuButtonStyle.BELOW_BUGS) {
						if (!(widget instanceof ClickableWidget button) || button.visible) {
							ModMenuEventHandler.shiftButtons(widget, modsButtonIndex == -1, spacing);
							if (modsButtonIndex == -1) {
								buttonsY = widget.getY();
							}
						}
					}
					if (ModMenuEventHandler.buttonHasText(widget, "menu.sendFeedback") && style == ModMenuConfig.GameMenuButtonStyle.REPLACE_FEEDBACK) {
						Screen altScreen = createConfirmLinkScreen(SharedConstants.getGameVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game");
						buttons.set(i, new ModMenuButtonWidget(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), ModMenuApi.createModsButtonText(), this, altScreen));
					} else if (style == ModMenuConfig.GameMenuButtonStyle.REPLACE_LAN_REPORTING) {
						if (ModMenuEventHandler.buttonHasText(widget, "menu.shareToLan") || ModMenuEventHandler.buttonHasText(widget, "menu.playerReporting")) {
							Screen altScreen = ModMenuEventHandler.buttonHasText(widget, "menu.shareToLan") ? new OpenToLanScreen(this) : new SocialInteractionsScreen();
							buttons.set(i, new ModMenuButtonWidget(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), ModMenuApi.createModsButtonText(), this, altScreen));
						}
					} else if (ModMenuEventHandler.buttonHasText(widget, "menu.reportBugs")) {
						modsButtonIndex = i + 1;
						reportBugsY = widget.getY();
						if (style == ModMenuConfig.GameMenuButtonStyle.REPLACE_BUGS) {
							Screen altScreen = createConfirmLinkScreen("https://aka.ms/snapshotbugs?ref=game");
							buttons.set(i, new ModMenuButtonWidget(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), ModMenuApi.createModsButtonText(), this, altScreen));
						} else {
							modsButtonIndex = i + 1;
							if (!(widget instanceof ClickableWidget button) || button.visible) {
								buttonsY = widget.getY();
							}
						}
					}
				}
				if (modsButtonIndex != -1) {
					if (style == ModMenuConfig.GameMenuButtonStyle.BELOW_BUGS) {
						buttons.add(modsButtonIndex, new ModMenuButtonWidget(this.width / 2 - 102, buttonsY + spacing, 204, 20, ModMenuApi.createModsButtonText(), this));
					} else if (style == ModMenuConfig.GameMenuButtonStyle.ICON) {
						buttons.add(modsButtonIndex, new UpdateCheckerTexturedButtonWidget(this.width / 2 + 4 + 100 + 2, reportBugsY, 20, 20, 0, 0, 20, ModMenuEventHandler.FABRIC_ICON_BUTTON_LOCATION, 32, 64, button -> MinecraftClient.getInstance().setScreen(new ModsScreen(this)), ModMenuApi.createModsButtonText()));
					} else if (style == ModMenuConfig.GameMenuButtonStyle.ICON_LEFT) {
						buttons.add(modsButtonIndex, new UpdateCheckerTexturedButtonWidget(this.width / 2 - 4 - 100 - 22, reportBugsY, 20, 20, 0, 0, 20, ModMenuEventHandler.FABRIC_ICON_BUTTON_LOCATION, 32, 64, button -> MinecraftClient.getInstance().setScreen(new ModsScreen(this)), ModMenuApi.createModsButtonText()));
					}
				}
			}
		}
	}
	@Unique
	private ConfirmLinkScreen createConfirmLinkScreen(String url) {
		return new ConfirmLinkScreen((confirmed) -> {
			if (confirmed) {
				Util.getOperatingSystem().open(url);
			}

            assert this.client != null;
            this.client.setScreen(this);
		}, url, true);
	}
}
