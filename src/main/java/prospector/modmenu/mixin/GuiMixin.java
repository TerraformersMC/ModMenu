package prospector.modmenu.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.MainMenuGui;
import net.minecraft.client.gui.menu.PauseMenuGui;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import prospector.modmenu.ModMenu;

@Mixin(Gui.class)
public class GuiMixin {

	@Shadow public int height;

	@Inject(at = @At("HEAD"), method = "addButton(Lnet/minecraft/client/gui/widget/ButtonWidget;)Lnet/minecraft/client/gui/widget/ButtonWidget;", cancellable = true)
	protected void addButton(ButtonWidget var1, CallbackInfoReturnable info) {
		if (((Object) this) instanceof MainMenuGui) {
			if (ModMenu.replacesRealmsButton()) {
				if (var1.id == 14) {
					info.cancel();
				}
			} else {
				if (var1.y <= this.height / 4 + 48 + 24 * 3) {
					var1.y -= 12;
				}
				if (var1.y > this.height / 4 + 48 + 24 * 3) {
					var1.y += 12;
				}
			}
		}
		if (((Object) this) instanceof PauseMenuGui) {
			if (ModMenu.replacesMojangFeedbackButtons()) {
				if (var1.id == 8 || var1.id == 9) {
					info.cancel();
				}
			} else {
				if (var1.y >= this.height / 4 - 16 + 24 * 3 && var1.id != 8 && var1.id != 9) {
					var1.y += 24;
				}
				var1.y -= 12;
			}
		}
	}
}