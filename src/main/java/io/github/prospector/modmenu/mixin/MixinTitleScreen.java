package io.github.prospector.modmenu.mixin;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {

	public MixinTitleScreen(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "initWidgetsNormal(II)V")
	public void drawMenuButton(CallbackInfo info) {
		this.addButton(new ModMenuButtonWidget(this.width / 2 - 100, this.height / 4 + 48 + 24 * 3, 200, 20, new TranslatableText("modmenu.title").append(new LiteralText(" ")).append(new TranslatableText("modmenu.loaded", ModMenu.getDisplayedModCount())), this));
	}

	@ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init(Lnet/minecraft/client/MinecraftClient;II)V"), method = "init", index = 2)
	private int adjustRealmsHeight(int height) {
		return height - 51;
	}

	@Override
	protected <T extends AbstractButtonWidget> T addButton(T button) {
		if (button.y <= this.height / 4 + 48 + 24 * 3) {
			button.y -= 12;
		}
		if (button.y > this.height / 4 + 48 + 24 * 3) {
			button.y += 12;
		}
		return super.addButton(button);
	}

	@Inject(at = @At("HEAD"), method = "areRealmsNotificationsEnabled", cancellable = true)
	private void onAreRealmsNotificationsEnabled(CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(true);
		if (ModMenuConfigManager.getConfig().getTitleScreenLayout() == ModMenuConfig.TitleScreenLayout.HIDE_REALMS) {
			info.setReturnValue(false);
		}
	}
}
