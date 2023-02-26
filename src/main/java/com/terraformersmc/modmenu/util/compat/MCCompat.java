package com.terraformersmc.modmenu.util.compat;

import net.fabricmc.loader.api.FabricLoader;

public class MCCompat {
	private static MCCompat INSTANCE = null;

	private KeybindsHelper keybindsHelper;
	private TooltipHelper tooltipHelper;
	private WidgetHelper widgetHelper;
	private ConfigHelper configHelper;
	private Blaze3DHelper blaze3DHelper;
	private DescriptionListWidgetHelper<?> descriptionListWidgetHelper;
	private ButtonHelper buttonHelper;
	private MCVersionHelper mcVersionHelper;
	private FocusHelper focusHelper;
	private NarratorHelper narratorHelper;

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

	public Blaze3DHelper getBlaze3DHelper() {
		return blaze3DHelper;
	}

	public void setBlaze3DHelper(Blaze3DHelper blaze3DHelper) {
		this.blaze3DHelper = blaze3DHelper;
	}

	public DescriptionListWidgetHelper<?> getDescriptionListWidgetHelper() {
		return descriptionListWidgetHelper;
	}

	public void setDescriptionListWidgetHelper(DescriptionListWidgetHelper<?> descriptionListWidgetHelper) {
		this.descriptionListWidgetHelper = descriptionListWidgetHelper;
	}

	public ButtonHelper getButtonHelper() {
		return buttonHelper;
	}

	public void setButtonHelper(ButtonHelper buttonHelper) {
		this.buttonHelper = buttonHelper;
	}

	public MCVersionHelper getMcVersionHelper() {
		return mcVersionHelper;
	}

	public void setMcVersionHelper(MCVersionHelper mcVersionHelper) {
		this.mcVersionHelper = mcVersionHelper;
	}

	public FocusHelper getFocusHelper() {
		return focusHelper;
	}

	public void setFocusHelper(FocusHelper focusHelper) {
		this.focusHelper = focusHelper;
	}

	public NarratorHelper getNarratorHelper() {
		return narratorHelper;
	}

	public void setNarratorHelper(NarratorHelper narratorHelper) {
		this.narratorHelper = narratorHelper;
	}
}
