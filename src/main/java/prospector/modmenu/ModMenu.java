package prospector.modmenu;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.Side;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.minecraft.realms.Realms;

public class ModMenu implements ModInitializer {
	private static boolean replaceRealmsButton = false;
	private static boolean replaceMojangFeedbackButtons = true;
	private static int buttonIdMainMenu = 27;
	private static int buttonIdPauseMenu = 27;

	@Override
	public void onInitialize() {
		boolean hasServersideInstalled = false;
		for (ModContainer modContainer : FabricLoader.INSTANCE.getMods()) {
			if (modContainer.getInfo().getSide() != Side.CLIENT) {
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
