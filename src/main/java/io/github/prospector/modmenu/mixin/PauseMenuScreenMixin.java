package io.github.prospector.modmenu.mixin;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.menu.PauseMenuScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.resource.language.I18n;

import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseMenuScreen.class)
public class PauseMenuScreenMixin extends Screen {

	public PauseMenuScreenMixin(TextComponent title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "init()V")
	public void drawMenuButton(CallbackInfo info) {
		int i = FabricLoader.getInstance().getAllMods().size();
		this.addButton(new ModMenuButtonWidget(this.width / 2 - 102, this.height / 4 + 8 + 24 * 3, 204, 20, (ModMenu.noFabric ? "Mods" : I18n.translate("modmenu.title")) + " " + (ModMenu.noFabric ? "(" + i + " Loaded)" : I18n.translate("modmenu.loaded", i)), this));
	}


	protected <T extends AbstractButtonWidget> T addButton(T button) {
		if (button.y >= this.height / 4 - 16 + 24 * 4 - 1 && !(button instanceof ModMenuButtonWidget)) {
			button.y += 24;
		}
		button.y -= 12;
		return super.addButton(button);
	}
}