package com.terraformersmc.modmenu.event;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.ModMenuTexturedButtonWidget;
import com.terraformersmc.modmenu.mixin.mc1193plus.IGridWidgetAccessor;
import com.terraformersmc.modmenu.util.ModrinthUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModMenuEventHandler {
	private static final Identifier FABRIC_ICON_BUTTON_LOCATION = new Identifier(ModMenu.MOD_ID, "textures/gui/mods_button.png");
	private static KeyBinding MENU_KEY_BIND;

	public static void register() {
		MENU_KEY_BIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.modmenu.open_menu",
				InputUtil.Type.KEYSYM,
				InputUtil.UNKNOWN_KEY.getCode(),
				"key.categories.misc"
		));
		ClientTickEvents.END_CLIENT_TICK.register(ModMenuEventHandler::onClientEndTick);

		ScreenEvents.AFTER_INIT.register(ModMenuEventHandler::afterScreenInit);
	}

	public static void afterScreenInit(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
		if (screen instanceof TitleScreen) {
			afterTitleScreenInit(screen);
		} else if (screen instanceof GameMenuScreen) {
			afterGameMenuScreenInit(screen);
		}
	}

	private static void afterTitleScreenInit(Screen screen) {
		final List<ClickableWidget> buttons = Screens.getButtons(screen);
		if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue()) {
			int modsButtonIndex = -1;
			final int spacing = 24;
			int buttonsY = screen.height / 4 + 48;
			for (int i = 0; i < buttons.size(); i++) {
				ClickableWidget button = buttons.get(i);
				if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.CLASSIC) {
					if (button.visible) {
						shiftButtons(button, modsButtonIndex == -1, spacing);
						if (modsButtonIndex == -1) {
							buttonsY = button.getButtonY();
						}
					}
				}
				if (buttonHasText(button, "menu.online")) {
					if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.REPLACE_REALMS) {
						buttons.set(i, new ModMenuButtonWidget(button.getButtonX(), button.getY(), button.getWidth(), button.getHeight(), ModMenuApi.createModsButtonText(), screen));
					} else {
						if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.SHRINK) {
							button.setWidth(98);
						}
						modsButtonIndex = i + 1;
						if (button.visible) {
							buttonsY = button.getButtonY();
						}
					}
				}
			}
			if (modsButtonIndex != -1) {
				if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.CLASSIC) {
					buttons.add(modsButtonIndex, new ModMenuButtonWidget(screen.width / 2 - 100, buttonsY + spacing, 200, 20, ModMenuApi.createModsButtonText(), screen));
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.SHRINK) {
					buttons.add(modsButtonIndex, new ModMenuButtonWidget(screen.width / 2 + 2, buttonsY, 98, 20, ModMenuApi.createModsButtonText(), screen));
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.ICON) {
					buttons.add(modsButtonIndex, new ModMenuTexturedButtonWidget(screen.width / 2 + 104, buttonsY, 20, 20, 0, 0, FABRIC_ICON_BUTTON_LOCATION, 32, 64, button -> MinecraftClient.getInstance().setScreen(new ModsScreen(screen)), ModMenuApi.createModsButtonText(), true));
				}
			}
		}
		ModrinthUtil.triggerV2DeprecatedToast();
	}

	private static void afterGameMenuScreenInit(Screen screen) {
		ClickableWidget widget = Screens.getButtons(screen).get(0);
		if (widget instanceof GridWidget) {
			final List<ClickableWidget> buttons = ((IGridWidgetAccessor) widget).getChildren();
			if (ModMenuConfig.MODIFY_GAME_MENU.getValue()) {
				int modsButtonIndex = -1;
				final int spacing = 24;
				int buttonsY = screen.height / 4 + 8;
				ModMenuConfig.ModsButtonStyle style = ModMenuConfig.MODS_BUTTON_STYLE.getValue().forGameMenu();
				for (int i = 0; i < buttons.size(); i++) {
					ClickableWidget button = buttons.get(i);
					if (style == ModMenuConfig.ModsButtonStyle.CLASSIC) {
						if (button.visible) {
							shiftButtons(button, modsButtonIndex == -1, spacing);
							if (modsButtonIndex == -1) {
								buttonsY = button.getButtonY();
							}
						}
					}
					if (buttonHasText(button, "menu.reportBugs")) {
						modsButtonIndex = i + 1;
						if (style == ModMenuConfig.ModsButtonStyle.SHRINK) {
							buttons.set(i, new ModMenuButtonWidget(button.getButtonX(), button.getButtonY(), button.getWidth(), button.getHeight(), ModMenuApi.createModsButtonText(), screen));
						} else {
							modsButtonIndex = i + 1;
							if (button.visible) {
								buttonsY = button.getButtonY();
							}
						}
					}
				}
				if (modsButtonIndex != -1) {
					if (style == ModMenuConfig.ModsButtonStyle.CLASSIC) {
						buttons.add(modsButtonIndex, new ModMenuButtonWidget(screen.width / 2 - 102, buttonsY + spacing, 204, 20, ModMenuApi.createModsButtonText(), screen));
					} else if (style == ModMenuConfig.ModsButtonStyle.ICON) {
						buttons.add(modsButtonIndex, new ModMenuTexturedButtonWidget(screen.width / 2 + 4 + 100 + 2, screen.height / 4 + 72 - 16, 20, 20, 0, 0, FABRIC_ICON_BUTTON_LOCATION, 32, 64, button -> MinecraftClient.getInstance().setScreen(new ModsScreen(screen)), ModMenuApi.createModsButtonText()));
					}
				}
			}
		}
	}

	private static void onClientEndTick(MinecraftClient client) {
		while (MENU_KEY_BIND.wasPressed()) {
			client.setScreen(new ModsScreen(client.currentScreen));
		}
	}

	private static boolean buttonHasText(ClickableWidget button, String translationKey) {
		Text text = button.getMessage();
		TextContent textContent = text.getContent();
		return textContent instanceof TranslatableTextContent && ((TranslatableTextContent) textContent).getKey().equals(translationKey);
	}

	private static void shiftButtons(ClickableWidget button, boolean shiftUp, int spacing) {
		if (shiftUp) {
			button.setButtonY(button.getButtonY() - spacing / 2);
		} else if (!button.getMessage().equals(TitleScreen.COPYRIGHT)) {
			button.setButtonY(button.getButtonY() + spacing / 2);
		}
	}
}
