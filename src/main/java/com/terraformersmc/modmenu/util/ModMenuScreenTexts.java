package com.terraformersmc.modmenu.util;

import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ModMenuScreenTexts {
	public static final Text CONFIGURE = Text.translatable("modmenu.configure");
	public static final Text DROP_CONFIRM = Text.translatable("modmenu.dropConfirm");
	public static final Text DROP_INFO_LINE_1 = Text.translatable("modmenu.dropInfo.line1");
	public static final Text DROP_INFO_LINE_2 = Text.translatable("modmenu.dropInfo.line2");
	public static final Text DROP_SUCCESSFUL_LINE_1 = Text.translatable("modmenu.dropSuccessful.line1");
	public static final Text DROP_SUCCESSFUL_LINE_2 = Text.translatable("modmenu.dropSuccessful.line2");
	public static final Text ISSUES = Text.translatable("modmenu.issues");
	public static final Text MODS_FOLDER = Text.translatable("modmenu.modsFolder");
	public static final Text SEARCH = Text.translatable("modmenu.search");
	public static final Text TITLE = Text.translatable("modmenu.title");
	public static final Text TOGGLE_FILTER_OPTIONS = Text.translatable("modmenu.toggleFilterOptions");
	public static final Text WEBSITE = Text.translatable("modmenu.website");

	private ModMenuScreenTexts() {
		return;
	}

	public static Text modIdTooltip(String modId) {
		return Text.translatable("modmenu.modIdToolTip", modId);
	}

	public static Text configureError(String modId, Throwable e) {
		return Text.translatable("modmenu.configure.error", modId, modId)
			.append(ScreenTexts.LINE_BREAK)
			.append(ScreenTexts.LINE_BREAK)
			.append(e.toString())
			.formatted(Formatting.RED);
	}
}
