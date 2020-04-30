package io.github.prospector.modmenu.util;

import net.minecraft.client.resource.language.I18n;

public enum BadgeType {
	LIBRARY(I18n.translate("modmenu.library"), 0x8810d098, 0x88046146),
	CLIENTSIDE(I18n.translate("modmenu.clientsideOnly"), 0x884383E3, 0x880E4699),
	DEPRECATED(I18n.translate("modmenu.deprecated"), 0x88C10A0B, 0x88E8393B),
	PATCHWORK_FORGE(I18n.translate("modmenu.forge"), 0x887C89A3, 0x88202C43),
	MINECRAFT(I18n.translate("modmenu.minecraft"), 0x88BCBCBC, 0x88535353);
	
	private String text;
	private int outlineColor;
	private int fillColor;
	
	private BadgeType(String text, int outlineColor, int fillColor) {
		this.text = text;
		this.outlineColor = outlineColor;
		this.fillColor = fillColor;
	}
	
	public String getText() {
		return this.text;
	}
	
	public int getOutlineColor() {
		return this.outlineColor;
	}
	
	public int getFillColor() {
		return this.fillColor;
	}
}
