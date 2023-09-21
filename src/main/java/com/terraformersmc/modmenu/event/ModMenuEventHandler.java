package com.terraformersmc.modmenu.event;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.ModrinthUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModMenuEventHandler {
	public static final Identifier FABRIC_ICON_BUTTON_LOCATION = new Identifier(ModMenu.MOD_ID, "textures/gui/mods_button.png");
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

		ClientTickEvents.END_CLIENT_TICK.register(ModMenuEventHandler::setMemorizedModScreenSearchText);
		ClientTickEvents.END_CLIENT_TICK.register(ModMenuEventHandler::setMemorizedModScreenScrollAmount);
		ClientTickEvents.END_CLIENT_TICK.register(ModMenuEventHandler::setMemorizedModScreenSelectedModListEntry);
	}

	private static String modScreenSearchText;
	private static double modScreenScrollAmount;
	private static ModListEntry modScreenModListEntry;

	public static String getMemorizedModScreenSearchText() {
		return modScreenSearchText;
	}

	public static double getMemorizedModScreenScrollAmount() {
		return modScreenScrollAmount;
	}

	public static ModListEntry getMemorizedModScreenSelectedModListEntry() {
		return modScreenModListEntry;
	}

	private static void setMemorizedModScreenSearchText(MinecraftClient client) {
		if (client.currentScreen instanceof ModsScreen screen) {
			modScreenSearchText = screen.getSearchInput();
		}
	}

	private static void setMemorizedModScreenScrollAmount(MinecraftClient client) {
		if (client.currentScreen instanceof ModsScreen screen) {
			modScreenScrollAmount = screen.getModList().getScrollAmount();
		}
	}

	private static void setMemorizedModScreenSelectedModListEntry(MinecraftClient client) {
		if (client.currentScreen instanceof ModsScreen screen) {
			modScreenModListEntry = screen.getSelectedEntry();
		}
	}

	public static void afterScreenInit(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
		if (screen instanceof TitleScreen) {
			afterTitleScreenInit(screen);
		}
	}

	private static void afterTitleScreenInit(Screen screen) {
		final List<ClickableWidget> buttons = Screens.getButtons(screen);
		if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue()) {
			int modsButtonIndex = -1;
			final int spacing = 24;
			int buttonsY = screen.height / 4 + 48;
			for (int i = 0; i < buttons.size(); i++) {
				ClickableWidget widget = buttons.get(i);
				if (widget instanceof ButtonWidget button) {
					if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
						if (button.visible) {
							shiftButtons(button, modsButtonIndex == -1, spacing);
							if (modsButtonIndex == -1) {
								buttonsY = button.getY();
							}
						}
					}
					if (buttonHasText(button, "menu.online")) {
						if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.REPLACE_REALMS) {
							buttons.set(i, new ModMenuButtonWidget(button.getX(), button.getY(), button.getWidth(), button.getHeight(), ModMenuApi.createModsButtonText(), screen));
						} else {
							if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
								button.setWidth(98);
							}
							modsButtonIndex = i + 1;
							if (button.visible) {
								buttonsY = button.getY();
							}
						}
					}
				}

			}
			if (modsButtonIndex != -1) {
				if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
					buttons.add(modsButtonIndex, new ModMenuButtonWidget(screen.width / 2 - 100, buttonsY + spacing, 200, 20, ModMenuApi.createModsButtonText(), screen));
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
					buttons.add(modsButtonIndex, new ModMenuButtonWidget(screen.width / 2 + 2, buttonsY, 98, 20, ModMenuApi.createModsButtonText(), screen));
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.ICON) {
					buttons.add(modsButtonIndex, new UpdateCheckerTexturedButtonWidget(screen.width / 2 + 104, buttonsY, 20, 20, 0, 0, 20, FABRIC_ICON_BUTTON_LOCATION, 32, 64, button -> MinecraftClient.getInstance().setScreen(new ModsScreen(screen)), ModMenuApi.createModsButtonText()));
				}
			}
		}
		ModrinthUtil.triggerV2DeprecatedToast();
	}

	private static void onClientEndTick(MinecraftClient client) {
		while (MENU_KEY_BIND.wasPressed()) {
			client.setScreen(new ModsScreen(client.currentScreen));
		}
	}

	public static boolean buttonHasText(Widget widget, String translationKey) {
		if (widget instanceof ButtonWidget button) {
			Text text = button.getMessage();
			TextContent textContent = text.getContent();
			return textContent instanceof TranslatableTextContent && ((TranslatableTextContent) textContent).getKey().equals(translationKey);
		}
		return false;
	}

	public static void shiftButtons(Widget widget, boolean shiftUp, int spacing) {
		if (shiftUp) {
			widget.setY(widget.getY() - spacing / 2);
		} else if (!(widget instanceof ClickableWidget button && button.getMessage().equals(TitleScreen.COPYRIGHT))) {
			widget.setY(widget.getY() + spacing / 2);
		}
	}
}
