package io.github.prospector.modmenu.mixin;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class MixinGameMenuScreen extends Screen {

	public MixinGameMenuScreen(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "init()V")
	public void drawMenuButton(CallbackInfo info) {
		addButton(new ModMenuButtonWidget(this.width / 2 - 102, this.height / 4 + 8 + 24 * 3, 204, 20, I18n.translate("modmenu.title") + " " + I18n.translate("modmenu.loaded", ModMenu.getFormattedModCount()), this), 5);
	}

	private void addButton(AbstractButtonWidget button, int tabOrder) {
		addButton(button);
		//Bit of ugly code to set the tab order of a button after the fact, better than a fragile mixin
		children.remove(button);
		children.add(tabOrder, button);
	}

	protected <T extends AbstractButtonWidget> T addButton(T button) {
		if (button.y >= this.height / 4 - 16 + 24 * 4 - 1 && !(button instanceof ModMenuButtonWidget)) {
			button.y += 24;
		}
		button.y -= 12;
		return super.addButton(button);
	}
}
