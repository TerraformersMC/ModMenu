package io.github.prospector.modmenu.mixin;

import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import net.minecraft.client.gui.MainMenuScreen;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.menu.PauseMenuScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {

	@Shadow public int screenHeight;

	@Inject(at = @At("HEAD"), method = "addButton", cancellable = true)
	protected void addButton(AbstractButtonWidget button, CallbackInfoReturnable info) {
		if (((Object) this) instanceof MainMenuScreen) {
			if (button.y <= this.screenHeight / 4 + 48 + 24 * 3) {
				button.y -= 12;
			}
			if (button.y > this.screenHeight / 4 + 48 + 24 * 3) {
				button.y += 12;
			}
		}
		if (((Object) this) instanceof PauseMenuScreen) {
			if (button.y >= this.screenHeight / 4 - 16 + 24 * 4 - 1 && !(button instanceof ModMenuButtonWidget)) {
				button.y += 24;
			}
			button.y -= 12;
		}
	}
}