package prospector.modmenu.mixin;

import net.fabricmc.loader.FabricLoader;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.widget.WidgetButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import prospector.modmenu.WidgetButtonModMenu;

@Mixin(GuiMainMenu.class)
public class GuiMainMenuMixin extends Gui {

	@Inject(at = @At("RETURN"), method = "initWidgetsNormal(II)V")
	public void drawMenuButton(CallbackInfo info) {
		int i = FabricLoader.INSTANCE.getMods().size();
		this.addButton(new WidgetButtonModMenu(27, this.width / 2 - 100, (this.height / 4 + 48) + 24 * 3, i + (i == 1 ? " Mod" : " Mods") + " Loaded", this));
	}
}