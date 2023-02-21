package com.terraformersmc.modmenu.util.compat;

import net.minecraft.text.Text;

public interface ButtonCompat {
	int getButtonX();
	int getButtonY();

	void setButtonX(int x);
	void setButtonY(int y);

	default void setDynamicTooltip(Text text) {}
}
