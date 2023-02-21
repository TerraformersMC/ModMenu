package com.terraformersmc.modmenu.util.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

public class MCCompat {
	/**
	 * Keyboard.setRepeatEvents removal.
	 */
	public static final boolean after22w46a = testMCVersion(">=1.19.3-alpha.22.46.a");
	/**
	 * Tooltip supplier changes.
	 */
	public static final boolean after22w45a = testMCVersion(">=1.19.3-alpha.22.45.a");
	/**
	 * JOML, GridWidget, Tooltip changes.
	 */
	public static final boolean after22w43a = testMCVersion(">=1.19.3-alpha.22.43.a");

	public static boolean testMCVersion(String versionPredicates) {
		try {
			return VersionPredicate.parse(versionPredicates).test(FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion());
		} catch (VersionParsingException e) {
			return false;
		}
	}

	private static MCCompat INSTANCE = null;

	private KeybindsHelper keybindsHelper;
	private TooltipHelper tooltipHelper;
	private WidgetHelper widgetHelper;
	private ConfigHelper configHelper;

	private static void init() {
		INSTANCE = new MCCompat();

		FabricLoader.getInstance().getEntrypointContainers("modmenu-mccompat", CompatInitializer.class).forEach(compatInitializer -> {
			compatInitializer.getEntrypoint().onInitialize(INSTANCE);
		});
	}

	public static MCCompat getInstance() {
		if (INSTANCE == null) init();
		return INSTANCE;
	}

	public KeybindsHelper getKeybindsHelper() {
		return keybindsHelper;
	}

	public void setKeybindsHelper(KeybindsHelper keybindsHelper) {
		this.keybindsHelper = keybindsHelper;
	}

	public TooltipHelper getTooltipHelper() {
		return tooltipHelper;
	}

	public void setTooltipHelper(TooltipHelper tooltipHelper) {
		this.tooltipHelper = tooltipHelper;
	}

	public WidgetHelper getWidgetHelper() {
		return widgetHelper;
	}

	public void setWidgetHelper(WidgetHelper widgetHelper) {
		this.widgetHelper = widgetHelper;
	}

	public ConfigHelper getConfigHelper() {
		return configHelper;
	}

	public void setConfigHelper(ConfigHelper configHelper) {
		this.configHelper = configHelper;
	}
}
