package com.terraformersmc.modmenu;

import com.terraformersmc.modmenu.api.ModMenuGuiProvider;
import com.terraformersmc.modmenu.api.ModMenuUpdateHandlerPreferenceProvider;
import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModMenuModMenuGuiProviderAndUpdatePreferences implements ModMenuGuiProvider, ModMenuUpdateHandlerPreferenceProvider {
	@Override
	public Screen createModsScreen(Screen previous) {
		return new ModsScreen(previous);
	}

	@Override
	public Text createModsButtonText() {
		return ModMenu.createModsButtonText(true);
	}

	@Override
	public UpdateChannel getUpdateChannelPreference() {
		return ModMenuConfig.UPDATE_CHANNEL.getValue();
	}
}
