package com.terraformersmc.modmenu.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class ModMenuScreenTexts {
	public static final Component CONFIGURE = Component.translatable("modmenu.configure");
	public static final Component DROP_CONFIRM = Component.translatable("modmenu.dropConfirm");
	public static final Component DROP_INFO_LINE_1 = Component.translatable("modmenu.dropInfo.line1");
	public static final Component DROP_INFO_LINE_2 = Component.translatable("modmenu.dropInfo.line2");
	public static final Component DROP_SUCCESSFUL_LINE_1 = Component.translatable("modmenu.dropSuccessful.line1");
	public static final Component DROP_SUCCESSFUL_LINE_2 = Component.translatable("modmenu.dropSuccessful.line2");
	public static final Component ISSUES = Component.translatable("modmenu.issues");
	public static final Component MODS_FOLDER = Component.translatable("modmenu.modsFolder");
	public static final Component SEARCH = Component.translatable("modmenu.search");
	public static final Component TITLE = Component.translatable("modmenu.title");
	public static final Component TOGGLE_FILTER_OPTIONS = Component.translatable("modmenu.toggleFilterOptions");
	public static final Component WEBSITE = Component.translatable("modmenu.website");

	private ModMenuScreenTexts() {
	}

	public static Component modIdTooltip(String modId) {
		return Component.translatable("modmenu.modIdToolTip", modId);
	}

	public static Component configureError(String modId, Throwable e) {
		return Component.translatable("modmenu.configure.error", modId, modId)
			.append(CommonComponents.NEW_LINE)
			.append(CommonComponents.NEW_LINE)
			.append(e.toString())
			.withStyle(ChatFormatting.RED);
	}
}
