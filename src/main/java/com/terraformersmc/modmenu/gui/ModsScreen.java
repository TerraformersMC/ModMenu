package com.terraformersmc.modmenu.gui;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.gui.widget.DescriptionListWidget;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.ModMenuTexturedButtonWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.TranslationUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ModsScreen extends Screen {
	private static final ResourceLocation FILTERS_BUTTON_LOCATION = new ResourceLocation(ModMenu.MOD_ID, "textures/gui/filters_button.png");
	private static final ResourceLocation CONFIGURE_BUTTON_LOCATION = new ResourceLocation(ModMenu.MOD_ID, "textures/gui/configure_button.png");

	private static final TranslatableComponent TOGGLE_FILTER_OPTIONS = new TranslatableComponent("modmenu.toggleFilterOptions");
	private static final TranslatableComponent CONFIGURE = new TranslatableComponent("modmenu.configure");

	private static final Logger LOGGER = LogManager.getLogger();

	private EditBox searchBox;
	private DescriptionListWidget descriptionListWidget;
	private final Screen previousScreen;
	private ModListWidget modList;
	private Component tooltip;
	private ModListEntry selected;
	private ModBadgeRenderer modBadgeRenderer;
	private double scrollPercent = 0;
	private boolean init = false;
	private boolean filterOptionsShown = false;
	private int paneY;
	private int paneWidth;
	private int rightPaneX;
	private int searchBoxX;
	private int filtersX;
	private int filtersWidth;
	private int searchRowWidth;
	public final Set<String> showModChildren = new HashSet<>();

	public final Map<String, Screen> configScreenCache = new HashMap<>();

	public ModsScreen(Screen previousScreen) {
		super(new TranslatableComponent("modmenu.title"));
		this.previousScreen = previousScreen;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (modList.isMouseOver(mouseX, mouseY)) {
			return this.modList.mouseScrolled(mouseX, mouseY, amount);
		}
		if (descriptionListWidget.isMouseOver(mouseX, mouseY)) {
			return this.descriptionListWidget.mouseScrolled(mouseX, mouseY, amount);
		}
		return false;
	}

	@Override
	public void tick() {
		this.searchBox.tick();
	}

	@Override
	protected void init() {
		Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(true);
		paneY = 48;
		paneWidth = this.width / 2 - 8;
		rightPaneX = width - paneWidth;

		int searchBoxWidth = paneWidth - 32 - 22;
		searchBoxX = paneWidth / 2 - searchBoxWidth / 2 - 22 / 2;
		this.searchBox = new EditBox(this.font, searchBoxX, 22, searchBoxWidth, 20, this.searchBox, new TranslatableComponent("modmenu.search"));
		this.searchBox.setResponder((string_1) -> this.modList.filter(string_1, false));
		this.modList = new ModListWidget(this.minecraft, paneWidth, this.height, paneY + 19, this.height - 36, ModMenuConfig.COMPACT_LIST.getValue() ? 23 : 36, this.searchBox.getValue(), this.modList, this);
		this.modList.setLeftPos(0);
		modList.reloadFilters();

		for (Mod mod : ModMenu.MODS.values()) {
			if (!configScreenCache.containsKey(mod.getId())) {
				try {
					Screen configScreen = ModMenu.getConfigScreen(mod.getId(), this);
					configScreenCache.put(mod.getId(), configScreen);
				} catch (Throwable e) {
					LOGGER.error("Error from mod '" + mod.getId() + "'", e);
				}
			}
		}

		this.descriptionListWidget = new DescriptionListWidget(this.minecraft, paneWidth, this.height, paneY + 60, this.height - 36, font.lineHeight + 1, this);
		this.descriptionListWidget.setLeftPos(rightPaneX);
		Button configureButton = new ModMenuTexturedButtonWidget(width - 24, paneY, 20, 20, 0, 0, CONFIGURE_BUTTON_LOCATION, 32, 64, button -> {
			final String modid = Objects.requireNonNull(selected).getMod().getId();
			final Screen screen = configScreenCache.get(modid);
			if (screen != null) {
				minecraft.setScreen(screen);
			} else {
				button.active = false;
			}
		},
				CONFIGURE, (buttonWidget, matrices, mouseX, mouseY) -> {
			ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
			if (button.isJustHovered()) {
				this.renderTooltip(matrices, CONFIGURE, mouseX, mouseY);
			} else if (button.isFocusedButNotHovered()) {
				this.renderTooltip(matrices, CONFIGURE, button.x, button.y);
			}
		}) {
			@Override
			public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
				if (selected != null) {
					String modid = selected.getMod().getId();
					active = configScreenCache.get(modid) != null;
				} else {
					active = false;
				}
				visible = active;
				super.render(matrices, mouseX, mouseY, delta);
			}

			@Override
			public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
				RenderSystem.color4f(1, 1, 1, 1f);
				super.renderButton(matrices, mouseX, mouseY, delta);
			}
		};
		int urlButtonWidths = paneWidth / 2 - 2;
		int cappedButtonWidth = Math.min(urlButtonWidths, 200);
		Button websiteButton = new Button(rightPaneX + (urlButtonWidths / 2) - (cappedButtonWidth / 2), paneY + 36, Math.min(urlButtonWidths, 200), 20,
				new TranslatableComponent("modmenu.website"), button -> {
			final Mod mod = Objects.requireNonNull(selected).getMod();
			this.minecraft.setScreen(new ConfirmLinkScreen((bool) -> {
				if (bool) {
					Util.getPlatform().openUri(mod.getWebsite());
				}
				this.minecraft.setScreen(this);
			}, mod.getWebsite(), false));
		}) {
			@Override
			public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
				visible = selected != null;
				active = visible && selected.getMod().getWebsite() != null;
				super.render(matrices, mouseX, mouseY, delta);
			}
		};
		Button issuesButton = new Button(rightPaneX + urlButtonWidths + 4 + (urlButtonWidths / 2) - (cappedButtonWidth / 2), paneY + 36, Math.min(urlButtonWidths, 200), 20,
				new TranslatableComponent("modmenu.issues"), button -> {
			final Mod mod = Objects.requireNonNull(selected).getMod();
			this.minecraft.setScreen(new ConfirmLinkScreen((bool) -> {
				if (bool) {
					Util.getPlatform().openUri(mod.getIssueTracker());
				}
				this.minecraft.setScreen(this);
			}, mod.getIssueTracker(), false));
		}) {
			@Override
			public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
				visible = selected != null;
				active = visible && selected.getMod().getIssueTracker() != null;
				super.render(matrices, mouseX, mouseY, delta);
			}
		};
		this.children.add(this.searchBox);
		this.addButton(new ModMenuTexturedButtonWidget(paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2, 22, 20, 20, 0, 0, FILTERS_BUTTON_LOCATION, 32, 64, button -> filterOptionsShown = !filterOptionsShown, TOGGLE_FILTER_OPTIONS, (buttonWidget, matrices, mouseX, mouseY) -> {
			ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
			if (button.isJustHovered()) {
				this.renderTooltip(matrices, TOGGLE_FILTER_OPTIONS, mouseX, mouseY);
			} else if (button.isFocusedButNotHovered()) {
				this.renderTooltip(matrices, TOGGLE_FILTER_OPTIONS, button.x, button.y);
			}
		}));
		Component showLibrariesText = ModMenuConfig.SHOW_LIBRARIES.getMessage(minecraft.options);
		Component sortingText = ModMenuConfig.SORTING.getMessage(minecraft.options);
		int showLibrariesWidth = font.width(showLibrariesText) + 20;
		int sortingWidth = font.width(sortingText) + 20;
		filtersWidth = showLibrariesWidth + sortingWidth + 2;
		searchRowWidth = searchBoxX + searchBoxWidth + 22;
		updateFiltersX();
		this.addButton(new Button(filtersX, 45, sortingWidth, 20, sortingText, button -> {
			ModMenuConfig.SORTING.cycleValue();
			ModMenuConfigManager.save();
			modList.reloadFilters();
		}) {
			@Override
			public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
				matrices.translate(0, 0, 1);
				visible = filterOptionsShown;
				this.setMessage(ModMenuConfig.SORTING.getMessage(minecraft.options));
				super.render(matrices, mouseX, mouseY, delta);
			}
		});
		this.addButton(new Button(filtersX + sortingWidth + 2, 45, showLibrariesWidth, 20, showLibrariesText, button -> {
			ModMenuConfig.SHOW_LIBRARIES.toggleValue();
			ModMenuConfigManager.save();
			modList.reloadFilters();
		}) {
			@Override
			public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
				matrices.translate(0, 0, 1);
				visible = filterOptionsShown;
				this.setMessage(ModMenuConfig.SHOW_LIBRARIES.getMessage(minecraft.options));
				super.render(matrices, mouseX, mouseY, delta);
			}
		});
		this.children.add(this.modList);
		if (!ModMenuConfig.HIDE_CONFIG_BUTTONS.getValue()) {
			this.addButton(configureButton);
		}
		this.addButton(websiteButton);
		this.addButton(issuesButton);
		this.children.add(this.descriptionListWidget);
		this.addButton(new Button(this.width / 2 - 154, this.height - 28, 150, 20, new TranslatableComponent("modmenu.modsFolder"), button -> Util.getPlatform().openFile(new File(FabricLoader.getInstance().getGameDirectory(), "mods"))));
		this.addButton(new Button(this.width / 2 + 4, this.height - 28, 150, 20, CommonComponents.GUI_DONE, button -> minecraft.setScreen(previousScreen)));
		this.setInitialFocus(this.searchBox);

		init = true;
	}

	@Override
	public boolean keyPressed(int int_1, int int_2, int int_3) {
		return super.keyPressed(int_1, int_2, int_3) || this.searchBox.keyPressed(int_1, int_2, int_3);
	}

	@Override
	public boolean charTyped(char char_1, int int_1) {
		return this.searchBox.charTyped(char_1, int_1);
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.tooltip = null;
		ModListEntry selectedEntry = selected;
		if (selectedEntry != null) {
			this.descriptionListWidget.render(matrices, mouseX, mouseY, delta);
		}
		this.modList.render(matrices, mouseX, mouseY, delta);
		this.searchBox.render(matrices, mouseX, mouseY, delta);
		RenderSystem.disableBlend();
		drawCenteredString(matrices, this.font, this.title, this.modList.getWidth() / 2, 8, 16777215);
		drawCenteredString(matrices, this.font, new TranslatableComponent("modmenu.dropInfo.1").withStyle(ChatFormatting.GRAY), this.width - this.modList.getWidth() / 2, paneY / 2 - minecraft.font.lineHeight - 1, 16777215);
		drawCenteredString(matrices, this.font, new TranslatableComponent("modmenu.dropInfo.2").withStyle(ChatFormatting.GRAY), this.width - this.modList.getWidth() / 2, paneY / 2 + 1, 16777215);
		Component fullModCount = computeModCountText(true);
		if (updateFiltersX()) {
			if (filterOptionsShown) {
				if (!ModMenuConfig.SHOW_LIBRARIES.getValue() || font.width(fullModCount) <= filtersX - 5) {
					font.draw(matrices, fullModCount.getVisualOrderText(), searchBoxX, 52, 0xFFFFFF);
				} else {
					font.draw(matrices, computeModCountText(false).getVisualOrderText(), searchBoxX, 46, 0xFFFFFF);
					font.draw(matrices, computeLibraryCountText().getVisualOrderText(), searchBoxX, 57, 0xFFFFFF);
				}
			} else {
				if (!ModMenuConfig.SHOW_LIBRARIES.getValue() || font.width(fullModCount) <= modList.getWidth() - 5) {
					font.draw(matrices, fullModCount.getVisualOrderText(), searchBoxX, 52, 0xFFFFFF);
				} else {
					font.draw(matrices, computeModCountText(false).getVisualOrderText(), searchBoxX, 46, 0xFFFFFF);
					font.draw(matrices, computeLibraryCountText().getVisualOrderText(), searchBoxX, 57, 0xFFFFFF);
				}
			}
		}
		if (selectedEntry != null) {
			Mod mod = selectedEntry.getMod();
			int x = rightPaneX;
			if ("java".equals(mod.getId())) {
				DrawingUtil.drawRandomVersionBackground(mod, matrices, x, paneY, 32, 32);
			}
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.selected.bindIconTexture();
			RenderSystem.enableBlend();
			blit(matrices, x, paneY, 0.0F, 0.0F, 32, 32, 32, 32);
			RenderSystem.disableBlend();
			int lineSpacing = font.lineHeight + 1;
			int imageOffset = 36;
			Component name = new TextComponent(mod.getName());
			FormattedText trimmedName = name;
			int maxNameWidth = this.width - (x + imageOffset);
			if (font.width(name) > maxNameWidth) {
				FormattedText ellipsis = FormattedText.of("...");
				trimmedName = FormattedText.composite(font.substrByWidth(name, maxNameWidth - font.width(ellipsis)), ellipsis);
			}
			font.draw(matrices, Language.getInstance().getVisualOrder(trimmedName), x + imageOffset, paneY + 1, 0xFFFFFF);
			if (mouseX > x + imageOffset && mouseY > paneY + 1 && mouseY < paneY + 1 + font.lineHeight && mouseX < x + imageOffset + font.width(trimmedName)) {
				setTooltip(new TranslatableComponent("modmenu.modIdToolTip", mod.getId()));
			}
			if (init || modBadgeRenderer == null || modBadgeRenderer.getMod() != mod) {
				modBadgeRenderer = new ModBadgeRenderer(x + imageOffset + Objects.requireNonNull(this.minecraft).font.width(trimmedName) + 2, paneY, width - 28, selectedEntry.mod, this);
				init = false;
			}
			if (!ModMenuConfig.HIDE_BADGES.getValue()) {
				modBadgeRenderer.draw(matrices, mouseX, mouseY);
			}
			if (mod.isReal()) {
				font.draw(matrices, "v" + mod.getVersion(), x + imageOffset, paneY + 2 + lineSpacing, 0x808080);
			}
			String authors;
			List<String> names = mod.getAuthors();

			if (!names.isEmpty()) {
				if (names.size() > 1) {
					authors = Joiner.on(", ").join(names);
				} else {
					authors = names.get(0);
				}
				DrawingUtil.drawWrappedString(matrices, I18n.get("modmenu.authorPrefix", authors), x + imageOffset, paneY + 2 + lineSpacing * 2, paneWidth - imageOffset - 4, 1, 0x808080);
			}
		}
		super.render(matrices, mouseX, mouseY, delta);
		if (this.tooltip != null) {
			this.renderTooltip(matrices, font.split(this.tooltip, Integer.MAX_VALUE), mouseX, mouseY);
		}
	}

	private Component computeModCountText(boolean includeLibs) {
		int[] rootMods = formatModCount(ModMenu.ROOT_MODS.values().stream().filter(mod -> !mod.getBadges().contains(Mod.Badge.LIBRARY)).map(Mod::getId).collect(Collectors.toSet()));

		if (includeLibs && ModMenuConfig.SHOW_LIBRARIES.getValue()) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values().stream().filter(mod -> mod.getBadges().contains(Mod.Badge.LIBRARY)).map(Mod::getId).collect(Collectors.toSet()));
			return TranslationUtil.translateNumeric("modmenu.showingModsLibraries", rootMods, rootLibs);
		} else {
			return TranslationUtil.translateNumeric("modmenu.showingMods", rootMods);
		}
	}

	private Component computeLibraryCountText() {
		if (ModMenuConfig.SHOW_LIBRARIES.getValue()) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values().stream().filter(mod -> mod.getBadges().contains(Mod.Badge.LIBRARY)).map(Mod::getId).collect(Collectors.toSet()));
			return TranslationUtil.translateNumeric("modmenu.showingLibraries", rootLibs);
		} else {
			return new TextComponent(null);
		}
	}

	private int[] formatModCount(Set<String> set) {
		int visible = modList.getDisplayedCountFor(set);
		int total = set.size();
		if (visible == total) {
			return new int[]{total};
		}
		return new int[]{visible, total};
	}

	@Override
	public void renderBackground(PoseStack matrices) {
		ModsScreen.overlayBackground(0, 0, this.width, this.height, 64, 64, 64, 255, 255);
	}

	static void overlayBackground(int x1, int y1, int x2, int y2, int red, int green, int blue, int startAlpha, int endAlpha) {
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		Objects.requireNonNull(Minecraft.getInstance()).getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		buffer.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
		buffer.vertex(x1, y2, 0.0D).uv(x1 / 32.0F, y2 / 32.0F).color(red, green, blue, endAlpha).endVertex();
		buffer.vertex(x2, y2, 0.0D).uv(x2 / 32.0F, y2 / 32.0F).color(red, green, blue, endAlpha).endVertex();
		buffer.vertex(x2, y1, 0.0D).uv(x2 / 32.0F, y1 / 32.0F).color(red, green, blue, startAlpha).endVertex();
		buffer.vertex(x1, y1, 0.0D).uv(x1 / 32.0F, y1 / 32.0F).color(red, green, blue, startAlpha).endVertex();
		tessellator.end();
	}

	@Override
	public void onClose() {
		super.onClose();
		this.modList.close();
		this.minecraft.setScreen(this.previousScreen);
	}

	private void setTooltip(Component tooltip) {
		this.tooltip = tooltip;
	}

	public ModListEntry getSelectedEntry() {
		return selected;
	}

	public void updateSelectedEntry(ModListEntry entry) {
		if (entry != null) {
			this.selected = entry;
		}
	}

	public double getScrollPercent() {
		return scrollPercent;
	}

	public void updateScrollPercent(double scrollPercent) {
		this.scrollPercent = scrollPercent;
	}

	public String getSearchInput() {
		return searchBox.getValue();
	}

	private boolean updateFiltersX() {
		if ((filtersWidth + font.width(computeModCountText(true)) + 20) >= searchRowWidth && ((filtersWidth + font.width(computeModCountText(false)) + 20) >= searchRowWidth || (filtersWidth + font.width(computeLibraryCountText()) + 20) >= searchRowWidth)) {
			filtersX = paneWidth / 2 - filtersWidth / 2;
			return !filterOptionsShown;
		} else {
			filtersX = searchRowWidth - filtersWidth + 1;
			return true;
		}
	}

	@Override
	public void onFilesDrop(List<Path> paths) {
		Path modsDirectory = FabricLoader.getInstance().getGameDir().resolve("mods");

		// Filter out none mods
		List<Path> mods = paths.stream()
				.filter(ModsScreen::isFabricMod)
				.collect(Collectors.toList());

		if (mods.isEmpty()) {
			return;
		}

		String modList = mods.stream()
				.map(Path::getFileName)
				.map(Path::toString)
				.collect(Collectors.joining(", "));

		this.minecraft.setScreen(new ConfirmScreen((value) -> {
			if (value) {
				boolean allSuccessful = true;

				for (Path path : mods) {
					try {
						Files.copy(path, modsDirectory.resolve(path.getFileName()));
					} catch (IOException e) {
						LOGGER.warn("Failed to copy mod from {} to {}", path, modsDirectory.resolve(path.getFileName()));
						SystemToast.onPackCopyFailure(minecraft, path.toString());
						allSuccessful = false;
						break;
					}
				}

				if (allSuccessful) {
					SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.TUTORIAL_HINT, new TranslatableComponent("modmenu.dropSuccessful.line1"), new TranslatableComponent("modmenu.dropSuccessful.line2"));
				}
			}
			this.minecraft.setScreen(this);
		}, new TranslatableComponent("modmenu.dropConfirm"), new TextComponent(modList)));
	}

	private static boolean isFabricMod(Path mod) {
		try (JarFile jarFile = new JarFile(mod.toFile())) {
			return jarFile.getEntry("fabric.mod.json") != null;
		} catch (IOException e) {
			return false;
		}
	}

	public Map<String, Screen> getConfigScreenCache() {
		return configScreenCache;
	}
}
