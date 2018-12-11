package io.github.prospector.modmenu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.ModInfo;
import net.minecraft.realms.Realms;

public class ModMenu implements ClientModInitializer {
	private static boolean replaceRealmsButton = false;
	private static boolean replaceMojangFeedbackButtons = false;
	private static int buttonIdMainMenu = 27;
	private static int buttonIdPauseMenu = 27;

	@Override
	public void onInitializeClient() {
		boolean hasServersideInstalled = false;
		for (ModContainer modContainer : FabricLoader.INSTANCE.getMods()) {
			if (modContainer.getInfo().getSide() != ModInfo.Side.CLIENT) {
				hasServersideInstalled = true;
				break;
			}
		}
		if (Realms.sessionId() == null || hasServersideInstalled) {
			replaceRealmsButton = true;
		}
	}

	public static boolean replacesRealmsButton() {
		return replaceRealmsButton;
	}

	public static boolean replacesMojangFeedbackButtons() {
		return replaceMojangFeedbackButtons;
	}

	public static int getButtonIdMainMenu() {
		return buttonIdMainMenu;
	}

	public static int getButtonIdPauseMenu() {
		return buttonIdPauseMenu;
	}
}
