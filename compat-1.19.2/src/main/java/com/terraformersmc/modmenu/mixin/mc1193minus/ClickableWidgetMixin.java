package com.terraformersmc.modmenu.mixin.mc1193minus;

import com.terraformersmc.modmenu.util.compat.ButtonCompat;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClickableWidget.class)
public abstract class ClickableWidgetMixin extends DrawableHelper implements ButtonCompat {
	@Shadow
	public int x;

	@Shadow
	public int y;

	@Override
	public int getButtonX() {
		return this.x;
	}

	@Override
	public int getButtonY() {
		return this.y;
	}

	@Override
	public void setButtonX(int x) {
		this.x = x;
	}

	@Override
	public void setButtonY(int y) {
		this.y = y;
	}
}
