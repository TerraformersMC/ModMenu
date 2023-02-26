package com.terraformersmc.modmenu.mixin.mc1191rc1minus;

import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.compat.ListWidgetCompat;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntryListWidget.class)
public abstract class MixinEntryListWidget implements ListWidgetCompat {
	@Shadow
	public abstract int getRowLeft();

	@Shadow
	protected abstract void renderList(MatrixStack par1, int par2, int par3, int par4, int par5, float par6);

	@Override
	public void renderListCompat(MatrixStack matrices, int x, int y, int mouseX, int mouseY, float delta) {
		this.renderList(matrices, x, y, mouseX, mouseY, delta);
	}

	@Inject(method = "renderList", at = @At("HEAD"), cancellable = true)
	private void invokeCompat(MatrixStack matrices, int x, int y, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (((Object)this) instanceof ModListWidget) {
			this.renderListCompat(matrices, this.getRowLeft(), -1, mouseX, mouseY, delta);
			ci.cancel();
		}
	}
}
