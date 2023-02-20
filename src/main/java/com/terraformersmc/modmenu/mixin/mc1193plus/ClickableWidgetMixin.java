package com.terraformersmc.modmenu.mixin.mc1193plus;

import com.terraformersmc.modmenu.util.compat.ButtonCompat;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClickableWidget.class)
public abstract class ClickableWidgetMixin extends DrawableHelper implements ButtonCompat {
	@Shadow
	public abstract int getX();

	@Shadow
	public abstract int getY();

	@Shadow
	public abstract void setX(int par1);

	@Shadow
	public abstract void setY(int par1);

	@Override
	public int getButtonX() {
		return this.getX();
	}

	@Override
	public int getButtonY() {
		return this.getY();
	}

	@Override
	public void setButtonX(int x) {
		this.setX(x);
	}

	@Override
	public void setButtonY(int y) {
		this.setY(y);
	}
}
