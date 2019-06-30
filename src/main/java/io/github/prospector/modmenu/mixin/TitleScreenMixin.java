package io.github.prospector.modmenu.mixin;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.NumberFormat;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

	public TitleScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "initWidgetsNormal(II)V")
	public void drawMenuButton(CallbackInfo info) {
		int i = FabricLoader.getInstance().getAllMods().size();
		this.addButton(new ModMenuButtonWidget(this.width / 2 - 100, this.height / 4 + 48 + 24 * 3, 200, 20, I18n.translate("modmenu.title") + " " + I18n.translate("modmenu.loaded", NumberFormat.getInstance().format(i - ModMenu.PARENT_MAP.values().size())), this));
	}

	@ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init(Lnet/minecraft/client/MinecraftClient;II)V"), method = "init", index = 2)
	private int adjustRealmsHeight(int height) {
		return height - 51;
	}

	protected <T extends AbstractButtonWidget> T addButton(T button) {
		if (button.y <= this.height / 4 + 48 + 24 * 3) {
			button.y -= 12;
		}
		if (button.y > this.height / 4 + 48 + 24 * 3) {
			button.y += 12;
		}
		return super.addButton(button);
	}
}