package com.terraformersmc.modmenu.mixin.mc1194plus;

import com.terraformersmc.modmenu.util.compat.ButtonCompat;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClickableWidget.class)
public abstract class ClickableWidgetMixin extends DrawableHelper implements ButtonCompat, Widget {
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
