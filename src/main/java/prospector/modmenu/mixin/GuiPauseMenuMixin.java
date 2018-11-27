package prospector.modmenu.mixin;

import net.fabricmc.loader.FabricLoader;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.menu.PauseMenuGui;
import net.minecraft.client.resource.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import prospector.modmenu.ModMenu;
import prospector.modmenu.gui.ModMenuButtonWidget;

@Mixin(PauseMenuGui.class)
public class GuiPauseMenuMixin extends Gui {

	@Inject(at = @At("RETURN"), method = "onInitialized()V")
	public void drawMenuButton(CallbackInfo info) {
		int i = FabricLoader.INSTANCE.getMods().size();
		this.addButton(new ModMenuButtonWidget(ModMenu.getButtonIdPauseMenu(), this.width / 2 - 102, this.height / 4 + 8 + 24 * 2, 204, 20, I18n.translate("modmenu.title") + " " + I18n.translate("modmenu.loaded", i), this));
	}
}