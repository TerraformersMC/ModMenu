package io.github.prospector.modmenu.util;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public enum BadgeType {
	LIBRARY(new TranslatableText("modmenu.library"), 0x8810d098, 0x88046146),
	CLIENTSIDE(new TranslatableText("modmenu.clientsideOnly"), 0x884383E3, 0x880E4699),
	DEPRECATED(new TranslatableText("modmenu.deprecated"), 0x88C10A0B, 0x88E8393B),
	PATCHWORK_FORGE(new TranslatableText("modmenu.forge"), 0x887C89A3, 0x88202C43),
	MINECRAFT(new TranslatableText("modmenu.minecraft"), 0x88BCBCBC, 0x88535353);
	
	private Text text;
	private int outlineColor;
	private int fillColor;
	
	private BadgeType(Text text, int outlineColor, int fillColor) {
		this.text = text;
		this.outlineColor = outlineColor;
		this.fillColor = fillColor;
	}
	
	public Text getText() {
		return this.text;
	}
	
	public int getOutlineColor() {
		return this.outlineColor;
	}
	
	public int getFillColor() {
		return this.fillColor;
	}
}
