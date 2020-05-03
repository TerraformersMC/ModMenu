package io.github.prospector.modmenu.mixin;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class MixinGameMenuScreen extends Screen {

	public MixinGameMenuScreen(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "initWidgets()V")
	public void drawMenuButton(CallbackInfo info) {
		addButton(new ModMenuButtonWidget(this.width / 2 - 102, this.height / 4 + 8 + 24 * 3, 204, 20, new TranslatableText("modmenu.title").append(new LiteralText(" ")).append(new TranslatableText("modmenu.loaded", ModMenu.getDisplayedModCount())), this), new TranslatableText("menu.reportBugs"));
	}

	private void addButton(AbstractButtonWidget button, Text insertAfter) {
		addButton(button);
		//Bit of ugly code to set the tab order of a button after the fact, better than a fragile mixin
		for (int i = 0; i < children.size(); i++) {
			Element element = children.get(i);
			if (element instanceof ButtonWidget && ((ButtonWidget) element).getMessage().equals(insertAfter)) {
				children.remove(button);
				children.add(i, button);
			}
		}
	}

	@Override
	protected <T extends AbstractButtonWidget> T addButton(T button) {
		if (button.y >= this.height / 4 - 16 + 24 * 4 - 1 && !(button instanceof ModMenuButtonWidget)) {
			button.y += 24;
		}
		button.y -= 12;
		return super.addButton(button);
	}
}
