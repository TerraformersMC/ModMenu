package com.terraformersmc.modmenu.gui;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.gui.widget.DescriptionListWidget;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.TranslationUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.render.*;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ModsScreen extends Screen {
	private static final Identifier FILTERS_BUTTON_LOCATION = new Identifier(ModMenu.MOD_ID, "textures/gui/filters_button.png");
	private static final Identifier CONFIGURE_BUTTON_LOCATION = new Identifier(ModMenu.MOD_ID, "textures/gui/configure_button.png");

	private static final Text TOGGLE_FILTER_OPTIONS = Text.translatable("modmenu.toggleFilterOptions");
	private static final Text CONFIGURE = Text.translatable("modmenu.configure");

	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu");

	private TextFieldWidget searchBox;
	private DescriptionListWidget descriptionListWidget;
	private final Screen previousScreen;
	private ModListWidget modList;
	private ModListEntry selected;
	private ModBadgeRenderer modBadgeRenderer;
	private double scrollPercent = 0;
	private boolean init = false;
	private boolean filterOptionsShown = false;
	private int paneY;
	private static final int RIGHT_PANE_Y = 48;
	private int paneWidth;
	private int rightPaneX;
	private int searchBoxX;
	private int filtersX;
	private int filtersWidth;
	private int searchRowWidth;
	public final Set<String> showModChildren = new HashSet<>();

	public final Map<String, Boolean> modHasConfigScreen = new HashMap<>();

	public ModsScreen(Screen previousScreen) {
		super(Text.translatable("modmenu.title"));
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
		paneY = ModMenuConfig.CONFIG_MODE.getValue() ? 48 : 48 + 19;
		paneWidth = this.width / 2 - 8;
		rightPaneX = width - paneWidth;

		int filtersButtonSize = (ModMenuConfig.CONFIG_MODE.getValue() ? 0 : 22);
		int searchWidthMax = paneWidth - 32 - filtersButtonSize;
		int searchBoxWidth = ModMenuConfig.CONFIG_MODE.getValue() ? Math.min(200, searchWidthMax) : searchWidthMax;
		searchBoxX = paneWidth / 2 - searchBoxWidth / 2 - filtersButtonSize / 2;
		this.searchBox = new TextFieldWidget(this.textRenderer, searchBoxX, 22, searchBoxWidth, 20, this.searchBox, Text.translatable("modmenu.search"));
		this.searchBox.setChangedListener((string_1) -> this.modList.filter(string_1, false));

		for (Mod mod : ModMenu.MODS.values()) {
			if (!modHasConfigScreen.containsKey(mod.getId())) {
				try {
					Screen configScreen = ModMenu.getConfigScreen(mod.getId(), this);
					modHasConfigScreen.put(mod.getId(), configScreen != null);
				} catch (java.lang.NoClassDefFoundError e) {
					LOGGER.warn("The '" + mod.getId() + "' mod config screen is not available because " + e.getLocalizedMessage() + " is missing.");
					modHasConfigScreen.put(mod.getId(), false);
				} catch (Throwable e) {
					LOGGER.error("Error from mod '" + mod.getId() + "'", e);
					modHasConfigScreen.put(mod.getId(), false);
				}
			}
		}

		this.modList = new ModListWidget(this.client, paneWidth, this.height, paneY, this.height - 36, ModMenuConfig.COMPACT_LIST.getValue() ? 23 : 36, this.searchBox.getText(), this.modList, this);
		this.modList.setLeftPos(0);
		modList.reloadFilters();

		this.descriptionListWidget = new DescriptionListWidget(this.client, paneWidth, this.height, RIGHT_PANE_Y + 60, this.height - 36, textRenderer.fontHeight + 1, this);
		this.descriptionListWidget.setLeftPos(rightPaneX);
		ButtonWidget configureButton = new TexturedButtonWidget(width - 24, RIGHT_PANE_Y, 20, 20, 0, 0, 20, CONFIGURE_BUTTON_LOCATION, 32, 64, button -> {
			final String modid = Objects.requireNonNull(selected).getMod().getId();
			if (modHasConfigScreen.get(modid)) {
				Screen configScreen = ModMenu.getConfigScreen(modid, this);
				client.setScreen(configScreen);
			} else {
				button.active = false;
			}
		}) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				if (selected != null) {
					String modid = selected.getMod().getId();
					active = modHasConfigScreen.get(modid);
				} else {
					active = false;
				}
				visible = active;
				super.render(matrices, mouseX, mouseY, delta);
			}

			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
				RenderSystem.setShaderColor(1, 1, 1, 1f);
				super.renderButton(matrices, mouseX, mouseY, delta);
			}
		};
		configureButton.setTooltip(Tooltip.of(CONFIGURE));
		int urlButtonWidths = paneWidth / 2 - 2;
		int cappedButtonWidth = Math.min(urlButtonWidths, 200);
		ButtonWidget websiteButton = new ButtonWidget(rightPaneX + (urlButtonWidths / 2) - (cappedButtonWidth / 2), RIGHT_PANE_Y + 36, Math.min(urlButtonWidths, 200), 20,
				Text.translatable("modmenu.website"), button -> {
			final Mod mod = Objects.requireNonNull(selected).getMod();
			this.client.setScreen(new ConfirmLinkScreen((bool) -> {
				if (bool) {
					Util.getOperatingSystem().open(mod.getWebsite());
				}
				this.client.setScreen(this);
			}, mod.getWebsite(), false));
		}, Supplier::get) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				visible = selected != null;
				active = visible && selected.getMod().getWebsite() != null;
				super.render(matrices, mouseX, mouseY, delta);
			}
		};
		ButtonWidget issuesButton = new ButtonWidget(rightPaneX + urlButtonWidths + 4 + (urlButtonWidths / 2) - (cappedButtonWidth / 2), RIGHT_PANE_Y + 36, Math.min(urlButtonWidths, 200), 20,
				Text.translatable("modmenu.issues"), button -> {
			final Mod mod = Objects.requireNonNull(selected).getMod();
			this.client.setScreen(new ConfirmLinkScreen((bool) -> {
				if (bool) {
					Util.getOperatingSystem().open(mod.getIssueTracker());
				}
				this.client.setScreen(this);
			}, mod.getIssueTracker(), false));
		}, Supplier::get) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				visible = selected != null;
				active = visible && selected.getMod().getIssueTracker() != null;
				super.render(matrices, mouseX, mouseY, delta);
			}
		};
		this.addSelectableChild(this.searchBox);
		ButtonWidget filtersButton = new TexturedButtonWidget(paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2, 22, 20, 20, 0, 0, 20, FILTERS_BUTTON_LOCATION, 32, 64, button -> filterOptionsShown = !filterOptionsShown, TOGGLE_FILTER_OPTIONS);
		filtersButton.setTooltip(Tooltip.of(TOGGLE_FILTER_OPTIONS));
		if (!ModMenuConfig.CONFIG_MODE.getValue()) {
			this.addDrawableChild(filtersButton);
		}
		Text showLibrariesText = ModMenuConfig.SHOW_LIBRARIES.getButtonText();
		Text sortingText = ModMenuConfig.SORTING.getButtonText();
		int showLibrariesWidth = textRenderer.getWidth(showLibrariesText) + 20;
		int sortingWidth = textRenderer.getWidth(sortingText) + 20;
		filtersWidth = showLibrariesWidth + sortingWidth + 2;
		searchRowWidth = searchBoxX + searchBoxWidth + 22;
		updateFiltersX();
		this.addDrawableChild(new ButtonWidget(filtersX, 45, sortingWidth, 20, sortingText, button -> {
			ModMenuConfig.SORTING.cycleValue();
			ModMenuConfigManager.save();
			modList.reloadFilters();
		}, Supplier::get) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				matrices.translate(0, 0, 1);
				visible = filterOptionsShown;
				this.setMessage(ModMenuConfig.SORTING.getButtonText());
				super.render(matrices, mouseX, mouseY, delta);
			}
		});
		this.addDrawableChild(new ButtonWidget(filtersX + sortingWidth + 2, 45, showLibrariesWidth, 20, showLibrariesText, button -> {
			ModMenuConfig.SHOW_LIBRARIES.toggleValue();
			ModMenuConfigManager.save();
			modList.reloadFilters();
		}, Supplier::get) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				matrices.translate(0, 0, 1);
				visible = filterOptionsShown;
				this.setMessage(ModMenuConfig.SHOW_LIBRARIES.getButtonText());
				super.render(matrices, mouseX, mouseY, delta);
			}
		});
		this.addSelectableChild(this.modList);
		if (!ModMenuConfig.HIDE_CONFIG_BUTTONS.getValue()) {
			this.addDrawableChild(configureButton);
		}
		this.addDrawableChild(websiteButton);
		this.addDrawableChild(issuesButton);
		this.addSelectableChild(this.descriptionListWidget);
		this.addDrawableChild(
				ButtonWidget.builder(Text.translatable("modmenu.modsFolder"), button -> Util.getOperatingSystem().open(new File(FabricLoader.getInstance().getGameDir().toFile(), "mods")))
						.position(this.width / 2 - 154, this.height - 28)
						.size(150, 20)
						.narrationSupplier(Supplier::get)
						.build());
		this.addDrawableChild(
				ButtonWidget.builder(ScreenTexts.DONE, button -> client.setScreen(previousScreen))
						.position(this.width / 2 + 4, this.height - 28)
						.size(150, 20)
						.narrationSupplier(Supplier::get)
						.build());
		this.searchBox.setFocused(true);

		init = true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return super.keyPressed(keyCode, scanCode, modifiers) || this.searchBox.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int keyCode) {
		return this.searchBox.charTyped(chr, keyCode);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		ModListEntry selectedEntry = selected;
		if (selectedEntry != null) {
			this.descriptionListWidget.render(matrices, mouseX, mouseY, delta);
		}
		this.modList.render(matrices, mouseX, mouseY, delta);
		this.searchBox.render(matrices, mouseX, mouseY, delta);
		RenderSystem.disableBlend();
		drawCenteredText(matrices, this.textRenderer, this.title, this.modList.getWidth() / 2, 8, 16777215);
		if (!ModMenuConfig.DISABLE_DRAG_AND_DROP.getValue()) {
			drawCenteredText(matrices, this.textRenderer, Text.translatable("modmenu.dropInfo.line1").formatted(Formatting.GRAY), this.width - this.modList.getWidth() / 2, RIGHT_PANE_Y / 2 - client.textRenderer.fontHeight - 1, 16777215);
			drawCenteredText(matrices, this.textRenderer, Text.translatable("modmenu.dropInfo.line2").formatted(Formatting.GRAY), this.width - this.modList.getWidth() / 2, RIGHT_PANE_Y / 2 + 1, 16777215);
		}
		if (!ModMenuConfig.CONFIG_MODE.getValue()) {
			Text fullModCount = computeModCountText(true);
			if (!ModMenuConfig.CONFIG_MODE.getValue() && updateFiltersX()) {
				if (filterOptionsShown) {
					if (!ModMenuConfig.SHOW_LIBRARIES.getValue() || textRenderer.getWidth(fullModCount) <= filtersX - 5) {
						textRenderer.draw(matrices, fullModCount.asOrderedText(), searchBoxX, 52, 0xFFFFFF);
					} else {
						textRenderer.draw(matrices, computeModCountText(false).asOrderedText(), searchBoxX, 46, 0xFFFFFF);
						textRenderer.draw(matrices, computeLibraryCountText().asOrderedText(), searchBoxX, 57, 0xFFFFFF);
					}
				} else {
					if (!ModMenuConfig.SHOW_LIBRARIES.getValue() || textRenderer.getWidth(fullModCount) <= modList.getWidth() - 5) {
						textRenderer.draw(matrices, fullModCount.asOrderedText(), searchBoxX, 52, 0xFFFFFF);
					} else {
						textRenderer.draw(matrices, computeModCountText(false).asOrderedText(), searchBoxX, 46, 0xFFFFFF);
						textRenderer.draw(matrices, computeLibraryCountText().asOrderedText(), searchBoxX, 57, 0xFFFFFF);
					}
				}
			}
		}
		if (selectedEntry != null) {
			Mod mod = selectedEntry.getMod();
			int x = rightPaneX;
			if ("java".equals(mod.getId())) {
				DrawingUtil.drawRandomVersionBackground(mod, matrices, x, RIGHT_PANE_Y, 32, 32);
			}
			this.selected.bindIconTexture();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			drawTexture(matrices, x, RIGHT_PANE_Y, 0.0F, 0.0F, 32, 32, 32, 32);
			RenderSystem.disableBlend();
			int lineSpacing = textRenderer.fontHeight + 1;
			int imageOffset = 36;
			Text name = Text.literal(mod.getTranslatedName());
			StringVisitable trimmedName = name;
			int maxNameWidth = this.width - (x + imageOffset);
			if (textRenderer.getWidth(name) > maxNameWidth) {
				StringVisitable ellipsis = StringVisitable.plain("...");
				trimmedName = StringVisitable.concat(textRenderer.trimToWidth(name, maxNameWidth - textRenderer.getWidth(ellipsis)), ellipsis);
			}
			textRenderer.draw(matrices, Language.getInstance().reorder(trimmedName), x + imageOffset, RIGHT_PANE_Y + 1, 0xFFFFFF);
			if (mouseX > x + imageOffset && mouseY > RIGHT_PANE_Y + 1 && mouseY < RIGHT_PANE_Y + 1 + textRenderer.fontHeight && mouseX < x + imageOffset + textRenderer.getWidth(trimmedName)) {
				this.setTooltip(Text.translatable("modmenu.modIdToolTip", mod.getId()));
			}
			if (init || modBadgeRenderer == null || modBadgeRenderer.getMod() != mod) {
				modBadgeRenderer = new ModBadgeRenderer(x + imageOffset + this.client.textRenderer.getWidth(trimmedName) + 2, RIGHT_PANE_Y, width - 28, selectedEntry.mod, this);
				init = false;
			}
			if (!ModMenuConfig.HIDE_BADGES.getValue()) {
				modBadgeRenderer.draw(matrices, mouseX, mouseY);
			}
			if (mod.isReal()) {
				textRenderer.draw(matrices, mod.getPrefixedVersion(), x + imageOffset, RIGHT_PANE_Y + 2 + lineSpacing, 0x808080);
			}
			String authors;
			List<String> names = mod.getAuthors();

			if (!names.isEmpty()) {
				if (names.size() > 1) {
					authors = Joiner.on(", ").join(names);
				} else {
					authors = names.get(0);
				}
				DrawingUtil.drawWrappedString(matrices, I18n.translate("modmenu.authorPrefix", authors), x + imageOffset, RIGHT_PANE_Y + 2 + lineSpacing * 2, paneWidth - imageOffset - 4, 1, 0x808080);
			}
		}
		super.render(matrices, mouseX, mouseY, delta);
	}

	private Text computeModCountText(boolean includeLibs) {
		int[] rootMods = formatModCount(ModMenu.ROOT_MODS.values().stream().filter(mod -> !mod.getBadges().contains(Mod.Badge.LIBRARY)).map(Mod::getId).collect(Collectors.toSet()));

		if (includeLibs && ModMenuConfig.SHOW_LIBRARIES.getValue()) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values().stream().filter(mod -> mod.getBadges().contains(Mod.Badge.LIBRARY)).map(Mod::getId).collect(Collectors.toSet()));
			return TranslationUtil.translateNumeric("modmenu.showingModsLibraries", rootMods, rootLibs);
		} else {
			return TranslationUtil.translateNumeric("modmenu.showingMods", rootMods);
		}
	}

	private Text computeLibraryCountText() {
		if (ModMenuConfig.SHOW_LIBRARIES.getValue()) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values().stream().filter(mod -> mod.getBadges().contains(Mod.Badge.LIBRARY)).map(Mod::getId).collect(Collectors.toSet()));
			return TranslationUtil.translateNumeric("modmenu.showingLibraries", rootLibs);
		} else {
			return Text.literal(null);
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
	public void renderBackground(MatrixStack matrices) {
		ModsScreen.overlayBackground(0, 0, this.width, this.height, 64, 64, 64, 255, 255);
	}

	static void overlayBackground(int x1, int y1, int x2, int y2, int red, int green, int blue, int startAlpha, int endAlpha) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		buffer.vertex(x1, y2, 0.0D).texture(x1 / 32.0F, y2 / 32.0F).color(red, green, blue, endAlpha).next();
		buffer.vertex(x2, y2, 0.0D).texture(x2 / 32.0F, y2 / 32.0F).color(red, green, blue, endAlpha).next();
		buffer.vertex(x2, y1, 0.0D).texture(x2 / 32.0F, y1 / 32.0F).color(red, green, blue, startAlpha).next();
		buffer.vertex(x1, y1, 0.0D).texture(x1 / 32.0F, y1 / 32.0F).color(red, green, blue, startAlpha).next();
		tessellator.draw();
	}

	@Override
	public void close() {
		this.modList.close();
		this.client.setScreen(this.previousScreen);
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
		return searchBox.getText();
	}

	private boolean updateFiltersX() {
		if ((filtersWidth + textRenderer.getWidth(computeModCountText(true)) + 20) >= searchRowWidth && ((filtersWidth + textRenderer.getWidth(computeModCountText(false)) + 20) >= searchRowWidth || (filtersWidth + textRenderer.getWidth(computeLibraryCountText()) + 20) >= searchRowWidth)) {
			filtersX = paneWidth / 2 - filtersWidth / 2;
			return !filterOptionsShown;
		} else {
			filtersX = searchRowWidth - filtersWidth + 1;
			return true;
		}
	}

	@Override
	public void filesDragged(List<Path> paths) {
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

		this.client.setScreen(new ConfirmScreen((value) -> {
			if (value) {
				boolean allSuccessful = true;

				for (Path path : mods) {
					try {
						Files.copy(path, modsDirectory.resolve(path.getFileName()));
					} catch (IOException e) {
						LOGGER.warn("Failed to copy mod from {} to {}", path, modsDirectory.resolve(path.getFileName()));
						SystemToast.addPackCopyFailure(client, path.toString());
						allSuccessful = false;
						break;
					}
				}

				if (allSuccessful) {
					SystemToast.add(client.getToastManager(), SystemToast.Type.TUTORIAL_HINT, Text.translatable("modmenu.dropSuccessful.line1"), Text.translatable("modmenu.dropSuccessful.line2"));
				}
			}
			this.client.setScreen(this);
		}, Text.translatable("modmenu.dropConfirm"), Text.literal(modList)));
	}

	private static boolean isFabricMod(Path mod) {
		try (JarFile jarFile = new JarFile(mod.toFile())) {
			return jarFile.getEntry("fabric.mod.json") != null;
		} catch (IOException e) {
			return false;
		}
	}

	public Map<String, Boolean> getModHasConfigScreen() {
		return modHasConfigScreen;
	}
}
