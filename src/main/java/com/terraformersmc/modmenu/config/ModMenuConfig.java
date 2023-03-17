package com.terraformersmc.modmenu.config;

import com.google.gson.annotations.SerializedName;
import com.terraformersmc.modmenu.config.option.BooleanConfigOption;
import com.terraformersmc.modmenu.config.option.EnumConfigOption;
import com.terraformersmc.modmenu.config.option.OptionConvertable;
import com.terraformersmc.modmenu.config.option.StringSetConfigOption;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.option.SimpleOption;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;

public class ModMenuConfig {
	public static final EnumConfigOption<Sorting> SORTING = new EnumConfigOption<>("sorting", Sorting.ASCENDING);
	public static final BooleanConfigOption COUNT_LIBRARIES = new BooleanConfigOption("count_libraries", true);
	public static final BooleanConfigOption COMPACT_LIST = new BooleanConfigOption("compact_list", false);
	public static final BooleanConfigOption COUNT_CHILDREN = new BooleanConfigOption("count_children", true);
	public static final EnumConfigOption<TitleMenuButtonStyle> MODS_BUTTON_STYLE = new EnumConfigOption<>("mods_button_style", TitleMenuButtonStyle.CLASSIC);
	public static final EnumConfigOption<GameMenuButtonStyle> GAME_MENU_BUTTON_STYLE = new EnumConfigOption<>("game_menu_button_style", GameMenuButtonStyle.REPLACE_BUGS);
	public static final BooleanConfigOption COUNT_HIDDEN_MODS = new BooleanConfigOption("count_hidden_mods", true);
	public static final EnumConfigOption<ModCountLocation> MOD_COUNT_LOCATION = new EnumConfigOption<>("mod_count_location", ModCountLocation.TITLE_SCREEN);
	public static final BooleanConfigOption HIDE_MOD_LINKS = new BooleanConfigOption("hide_mod_links", false);
	public static final BooleanConfigOption SHOW_LIBRARIES = new BooleanConfigOption("show_libraries", false);
	public static final BooleanConfigOption HIDE_MOD_LICENSE = new BooleanConfigOption("hide_mod_license", false);
	public static final BooleanConfigOption HIDE_BADGES = new BooleanConfigOption("hide_badges", false);
	public static final BooleanConfigOption HIDE_MOD_CREDITS = new BooleanConfigOption("hide_mod_credits", false);
	public static final BooleanConfigOption EASTER_EGGS = new BooleanConfigOption("easter_eggs", true);
	public static final BooleanConfigOption MODIFY_TITLE_SCREEN = new BooleanConfigOption("modify_title_screen", true);
	public static final BooleanConfigOption MODIFY_GAME_MENU = new BooleanConfigOption("modify_game_menu", true);
	public static final BooleanConfigOption HIDE_CONFIG_BUTTONS = new BooleanConfigOption("hide_config_buttons", false);
	public static final BooleanConfigOption RANDOM_JAVA_COLORS = new BooleanConfigOption("random_java_colors", true);
	public static final BooleanConfigOption TRANSLATE_NAMES = new BooleanConfigOption("translate_names", true);
	public static final BooleanConfigOption TRANSLATE_DESCRIPTIONS = new BooleanConfigOption("translate_descriptions", true);
	public static final BooleanConfigOption CONFIG_MODE = new BooleanConfigOption("config_mode", false);
	public static final BooleanConfigOption DISABLE_DRAG_AND_DROP = new BooleanConfigOption("disable_drag_and_drop", false);
	public static final StringSetConfigOption HIDDEN_MODS = new StringSetConfigOption("hidden_mods", new HashSet<>());
	public static final StringSetConfigOption HIDDEN_CONFIGS = new StringSetConfigOption("hidden_configs", new HashSet<>());
	public static final StringSetConfigOption DISABLE_UPDATE_CHECKER = new StringSetConfigOption("disable_update_checker", new HashSet<>());
	public static final BooleanConfigOption UPDATE_CHECKER = new BooleanConfigOption("update_checker", true);
	public static final BooleanConfigOption BUTTON_UPDATE_BADGE = new BooleanConfigOption("button_update_badge", true);
	public static final BooleanConfigOption QUICK_CONFIGURE = new BooleanConfigOption("quick_configure", true);

	public static SimpleOption<?>[] asOptions() {
		ArrayList<SimpleOption<?>> options = new ArrayList<>();
		for (Field field : ModMenuConfig.class.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers())
					&& Modifier.isFinal(field.getModifiers())
					&& OptionConvertable.class.isAssignableFrom(field.getType())
					&& !field.getName().equals("HIDE_CONFIG_BUTTONS")
					&& !field.getName().equals("MODIFY_TITLE_SCREEN")
					&& !field.getName().equals("MODIFY_GAME_MENU")
					&& !field.getName().equals("CONFIG_MODE")
					&& !field.getName().equals("DISABLE_DRAG_AND_DROP")
			) {
				try {
					options.add(((OptionConvertable) field.get(null)).asOption());
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return options.stream().toArray(SimpleOption[]::new);
	}

	public enum Sorting {
		@SerializedName("ascending")
		ASCENDING(Comparator.comparing(mod -> mod.getTranslatedName().toLowerCase(Locale.ROOT))),
		@SerializedName("descending")
		DESCENDING(ASCENDING.getComparator().reversed());

		Comparator<Mod> comparator;

		Sorting(Comparator<Mod> comparator) {
			this.comparator = comparator;
		}

		public Comparator<Mod> getComparator() {
			return comparator;
		}
	}

	public enum ModCountLocation {
		@SerializedName("title_screen")
		TITLE_SCREEN(true, false),
		@SerializedName("mods_button")
		MODS_BUTTON(false, true),
		@SerializedName("title_screen_and_mods_button")
		TITLE_SCREEN_AND_MODS_BUTTON(true, true),
		@SerializedName("none")
		NONE(false, false);

		private final boolean titleScreen, modsButton;

		ModCountLocation(boolean titleScreen, boolean modsButton) {
			this.titleScreen = titleScreen;
			this.modsButton = modsButton;
		}

		public boolean isOnTitleScreen() {
			return titleScreen;
		}

		public boolean isOnModsButton() {
			return modsButton;
		}
	}

	public enum TitleMenuButtonStyle {
		@SerializedName("classic")
		CLASSIC(),
		@SerializedName("replace_realms")
		REPLACE_REALMS(),
		@SerializedName("shrink")
		SHRINK(),
		@SerializedName("icon")
		ICON();
	}

	public enum GameMenuButtonStyle {
		@SerializedName("replace_bugs")
		REPLACE_BUGS,
		@SerializedName("below_bugs")
		BELOW_BUGS,
		@SerializedName("icon")
		ICON;
	}
}
