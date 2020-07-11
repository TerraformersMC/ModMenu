package io.github.prospector.modmenu.util;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public enum BadgeType {
	LIBRARY(new TranslatableText("modmenu.library"), 0xff107454, 0xff093929),
	CLIENTSIDE(new TranslatableText("modmenu.clientsideOnly"), 0xff2b4b7c, 0xff0e2a55),
	DEPRECATED(new TranslatableText("modmenu.deprecated"), 0xffff3333, 0xffb30000),
	PATCHWORK_FORGE(new TranslatableText("modmenu.forge"), 0xff1f2d42, 0xff101721),
	MINECRAFT(new TranslatableText("modmenu.minecraft"), 0xff6f6c6a, 0xff31302f);
	
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
