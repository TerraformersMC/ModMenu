package com.terraformersmc.modmenu.gui;

import com.google.common.base.Joiner;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.gui.widget.DescriptionListWidget;
import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.ModMenuScreenTexts;
import com.terraformersmc.modmenu.util.TranslationUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonLinks;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ModsScreen extends Screen {
	private static final Identifier FILTERS_BUTTON_LOCATION = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, "textures/gui/filters_button.png");
	private static final Identifier CONFIGURE_BUTTON_LOCATION = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, "textures/gui/configure_button.png");

	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | ModsScreen");
	private final Screen previousScreen;
	private ModListEntry selected;
	private ModBadgeRenderer modBadgeRenderer;
	private double scrollPercent = 0;
	private boolean keepFilterOptionsShown = false;
	private boolean init = false;
	private boolean filterOptionsShown = false;
	private static final int RIGHT_PANE_Y = 48;
	private int paneWidth;
	private int rightPaneX;
	private int searchBoxX;
	private int filtersX;
	private int filtersWidth;
	private int searchRowWidth;
	public final Set<String> showModChildren = new HashSet<>();

	private EditBox searchBox;
	private @Nullable AbstractWidget filtersButton;
	private AbstractWidget sortingButton;
	private AbstractWidget librariesButton;
	private ModListWidget modList;
	private @Nullable AbstractWidget configureButton;
	private AbstractWidget websiteButton;
	private AbstractWidget issuesButton;
	private DescriptionListWidget descriptionListWidget;

	public final Map<String, Boolean> modHasConfigScreen = new HashMap<>();
	public final Map<String, Throwable> modScreenErrors = new HashMap<>();

	private static final Component SEND_FEEDBACK_TEXT = Component.translatable("menu.sendFeedback");
	private static final Component REPORT_BUGS_TEXT = Component.translatable("menu.reportBugs");

	public ModsScreen(Screen previousScreen) {
		super(ModMenuScreenTexts.TITLE);
		this.previousScreen = previousScreen;
	}

	@Override
	protected void init() {
		int paneY = ModMenuConfig.CONFIG_MODE.getValue() ? 48 : 48 + 19;
		this.paneWidth = this.width / 2 - 8;
		this.rightPaneX = this.width - this.paneWidth;

		// Mod list (initialized early for updateFiltersX)
		this.modList = new ModListWidget(this.minecraft,
			this.paneWidth,
			this.height - paneY - 36,
			paneY,
			ModMenuConfig.COMPACT_LIST.getValue() ? 23 : 36,
			this.modList,
			this
		);
		this.modList.setX(0);

		// Search box
		int filtersButtonSize = (ModMenuConfig.CONFIG_MODE.getValue() ? 0 : 22);
		int searchWidthMax = this.paneWidth - 32 - filtersButtonSize;
		int searchBoxWidth = ModMenuConfig.CONFIG_MODE.getValue() ? Math.min(200, searchWidthMax) : searchWidthMax;

		this.searchBoxX = this.paneWidth / 2 - searchBoxWidth / 2 - filtersButtonSize / 2;
		this.searchBox = new EditBox(this.font,
			this.searchBoxX,
			22,
			searchBoxWidth,
			20,
			this.searchBox,
			ModMenuScreenTexts.SEARCH
		);
		this.searchBox.setResponder(text -> {
			this.modList.filter(text, false);
		});

		// Filters button
		Component sortingText = ModMenuConfig.SORTING.getButtonText();
		Component librariesText = ModMenuConfig.SHOW_LIBRARIES.getButtonText();

		int sortingWidth = font.width(sortingText) + 28;
		int librariesWidth = font.width(librariesText) + 20;

		this.filtersWidth = librariesWidth + sortingWidth + 2;
		this.searchRowWidth = this.searchBoxX + searchBoxWidth + 22;

		this.updateFiltersX(true);

		if (!ModMenuConfig.CONFIG_MODE.getValue()) {
			this.filtersButton = LegacyTexturedButtonWidget.legacyTexturedBuilder(ModMenuScreenTexts.TOGGLE_FILTER_OPTIONS,
					button -> {
						this.setFilterOptionsShown(!this.filterOptionsShown);
					}
				)
				.position(this.paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2, 22)
				.size(20, 20)
				.uv(0, 0, 20)
				.texture(FILTERS_BUTTON_LOCATION, 32, 64)
				.build();

			this.filtersButton.setTooltip(Tooltip.create(ModMenuScreenTexts.TOGGLE_FILTER_OPTIONS));
		}

		// Sorting button
		this.sortingButton = Button.builder(sortingText, button -> {
			ModMenuConfig.SORTING.cycleValue(this.minecraft.hasShiftDown() ? -1 : 1);
			ModMenuConfigManager.save();
			modList.reloadFilters();
			button.setMessage(ModMenuConfig.SORTING.getButtonText());
		}).pos(this.filtersX, 45).size(sortingWidth, 20).build();

		// Show libraries button
		this.librariesButton = Button.builder(librariesText, button -> {
			ModMenuConfig.SHOW_LIBRARIES.toggleValue();
			ModMenuConfigManager.save();
			modList.reloadFilters();
			button.setMessage(ModMenuConfig.SHOW_LIBRARIES.getButtonText());
		}).pos(this.filtersX + sortingWidth + 2, 45).size(librariesWidth, 20).build();

		// Configure button
		if (!ModMenuConfig.HIDE_CONFIG_BUTTONS.getValue()) {
			this.configureButton = LegacyTexturedButtonWidget.legacyTexturedBuilder(CommonComponents.EMPTY, button -> {
					final String id = Objects.requireNonNull(selected).getMod().getId();
					if (getModHasConfigScreen(id)) {
						this.safelyOpenConfigScreen(id);
					} else {
						button.active = false;
					}
				})
				.position(width - 24, RIGHT_PANE_Y)
				.size(20, 20)
				.uv(0, 0, 20)
				.texture(CONFIGURE_BUTTON_LOCATION, 32, 64)
				.build();
		}

		// Website button
		int urlButtonWidths = this.paneWidth / 2 - 2;
		int cappedButtonWidth = Math.min(urlButtonWidths, 200);
		this.websiteButton = Button.builder(ModMenuScreenTexts.WEBSITE, button -> {
				final Mod mod = Objects.requireNonNull(selected).getMod();
				boolean isMinecraft = selected.getMod().getId().equals("minecraft");
				if (isMinecraft) {
					var url = SharedConstants.getCurrentVersion().stable() ? CommonLinks.RELEASE_FEEDBACK : CommonLinks.SNAPSHOT_FEEDBACK;
					ConfirmLinkScreen.confirmLinkNow(this, url, true);
				} else {
					var url = mod.getWebsite();
					if (url != null) {
						ConfirmLinkScreen.confirmLinkNow(this, url, false);
					}
				}
			})
			.pos(this.rightPaneX + (urlButtonWidths / 2) - (cappedButtonWidth / 2), RIGHT_PANE_Y + 36)
			.size(Math.min(urlButtonWidths, 200), 20)
			.build();

		// Issues button
		this.issuesButton = Button.builder(ModMenuScreenTexts.ISSUES, button -> {
				final Mod mod = Objects.requireNonNull(selected).getMod();
				boolean isMinecraft = selected.getMod().getId().equals("minecraft");
				if (isMinecraft) {
					ConfirmLinkScreen.confirmLinkNow(this, CommonLinks.SNAPSHOT_BUGS_FEEDBACK, true);
				} else {
					var url = mod.getIssueTracker();
					if (url != null) {
						ConfirmLinkScreen.confirmLinkNow(this, url, false);
					}
				}
			})
			.pos(this.rightPaneX + urlButtonWidths + 4 + (urlButtonWidths / 2) - (cappedButtonWidth / 2), RIGHT_PANE_Y + 36)
			.size(Math.min(urlButtonWidths, 200), 20)
			.build();

		// Description list
		this.descriptionListWidget = new DescriptionListWidget(
			this.minecraft,
			this.paneWidth,
			this.height - RIGHT_PANE_Y - 96,
			RIGHT_PANE_Y + 60,
			font.lineHeight + 1,
			this.descriptionListWidget,
			this
		);
		this.descriptionListWidget.setX(this.rightPaneX);

		// Mods folder button
		AbstractWidget modsFolderButton = Button.builder(ModMenuScreenTexts.MODS_FOLDER, button -> Util.getPlatform().openUri(getModsFolder().toUri())).pos(this.width / 2 - 154, this.height - 28).size(150, 20).build();

		// Done button
		AbstractWidget doneButton = Button.builder(CommonComponents.GUI_DONE, button -> minecraft.setScreen(previousScreen)).pos(this.width / 2 + 4, this.height - 28).size(150, 20).build();

		// Initialize data
		modList.finalizeInit();
		this.setFilterOptionsShown(this.keepFilterOptionsShown && this.filterOptionsShown);

		// Add children
		this.addWidget(this.searchBox);
		this.setInitialFocus(this.searchBox);
		if (this.filtersButton != null) {
			this.addRenderableWidget(this.filtersButton);
		}

		this.addRenderableWidget(this.sortingButton);
		this.addRenderableWidget(this.librariesButton);
		this.addWidget(this.modList);
		if (this.configureButton != null) {
			this.addRenderableWidget(this.configureButton);
		}

		this.addRenderableWidget(this.websiteButton);
		this.addRenderableWidget(this.issuesButton);
		this.addWidget(this.descriptionListWidget);
		this.addRenderableWidget(modsFolderButton);
		this.addRenderableWidget(doneButton);

		this.init = true;
		this.keepFilterOptionsShown = true;
	}

	@Override
	public boolean keyPressed(KeyEvent input) {
		return super.keyPressed(input) || this.searchBox.keyPressed(input);
	}

	@Override
	public boolean charTyped(CharacterEvent input) {
		return this.searchBox.charTyped(input);
	}

	@Override
	public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
		super.render(drawContext, mouseX, mouseY, delta);
		ModListEntry selectedEntry = selected;
		if (selectedEntry != null) {
			this.descriptionListWidget.render(drawContext, mouseX, mouseY, delta);
		}

		this.modList.render(drawContext, mouseX, mouseY, delta);
		this.searchBox.render(drawContext, mouseX, mouseY, delta);
		drawContext.drawCenteredString(this.font, this.title, this.modList.getWidth() / 2, 8, 0xFFFFFFFF);
		assert minecraft != null;
		int grayColor = 0xFFAAAAAA;
		if (!ModMenuConfig.DISABLE_DRAG_AND_DROP.getValue()) {
			drawContext.drawCenteredString(
				this.font,
				ModMenuScreenTexts.DROP_INFO_LINE_1,
				this.width - this.modList.getWidth() / 2,
				RIGHT_PANE_Y / 2 - minecraft.font.lineHeight - 1,
				grayColor
			);
			drawContext.drawCenteredString(
				this.font,
				ModMenuScreenTexts.DROP_INFO_LINE_2,
				this.width - this.modList.getWidth() / 2,
				RIGHT_PANE_Y / 2 + 1,
				grayColor
			);
		}

		if (!ModMenuConfig.CONFIG_MODE.getValue()) {
			Component fullModCount = this.computeModCountText(true, false);
			if (!ModMenuConfig.CONFIG_MODE.getValue() && this.updateFiltersX(false)) {
				if (this.filterOptionsShown) {
					if (!ModMenuConfig.SHOW_LIBRARIES.getValue() ||
						font.width(fullModCount) <= this.filtersX - 5) {
						drawContext.drawString(
							font,
							fullModCount.getVisualOrderText(),
							this.searchBoxX,
							52,
							0xFFFFFFFF,
							true
						);
					} else {
						drawContext.drawString(
							font,
							computeModCountText(false, false).getVisualOrderText(),
							this.searchBoxX,
							46,
							0xFFFFFFFF,
							true
						);
						drawContext.drawString(
							font,
							computeLibraryCountText(false).getVisualOrderText(),
							this.searchBoxX,
							57,
							0xFFFFFFFF,
							true
						);
					}
				} else {
					if (!ModMenuConfig.SHOW_LIBRARIES.getValue() ||
						font.width(fullModCount) <= modList.getWidth() - 5) {
						drawContext.drawString(font,
							fullModCount.getVisualOrderText(),
							this.searchBoxX,
							52,
							0xFFFFFFFF,
							true
						);
					} else {
						drawContext.drawString(
							font,
							computeModCountText(false, false).getVisualOrderText(),
							this.searchBoxX,
							46,
							0xFFFFFFFF,
							true
						);
						drawContext.drawString(
							font,
							computeLibraryCountText(false).getVisualOrderText(),
							this.searchBoxX,
							57,
							0xFFFFFFFF,
							true
						);
					}
				}
			}
		}

		if (selectedEntry != null) {
			Mod mod = selectedEntry.getMod();
			int x = this.rightPaneX;
			if ("java".equals(mod.getId())) {
				DrawingUtil.drawRandomVersionBackground(mod, drawContext, x, RIGHT_PANE_Y, 32, 32);
			}

			drawContext.blit(RenderPipelines.GUI_TEXTURED, this.selected.getIconTexture(), x, RIGHT_PANE_Y, 0.0F, 0.0F, 32, 32, 32, 32, 0xFFFFFFFF);
			int lineSpacing = font.lineHeight + 1;
			int imageOffset = 36;
			Component name = Component.literal(mod.getTranslatedName());
			FormattedText trimmedName = name;
			int maxNameWidth = this.width - (x + imageOffset);
			if (font.width(name) > maxNameWidth) {
				FormattedText ellipsis = FormattedText.of("...");
				trimmedName = FormattedText.composite(font.substrByWidth(name, maxNameWidth - font.width(ellipsis)), ellipsis);
			}

			drawContext.drawString(
				font,
				Language.getInstance().getVisualOrder(trimmedName),
				x + imageOffset,
				RIGHT_PANE_Y + 1,
				0xFFFFFFFF,
				true
			);

			if (mouseX > x + imageOffset && mouseY > RIGHT_PANE_Y + 1 &&
				mouseY < RIGHT_PANE_Y + 1 + font.lineHeight &&
				mouseX < x + imageOffset + font.width(trimmedName)) {
				drawContext.setTooltipForNextFrame(ModMenuScreenTexts.modIdTooltip(mod.getId()), mouseX, mouseY);
			}

			if (this.init || modBadgeRenderer == null || modBadgeRenderer.getMod() != mod) {
				modBadgeRenderer = new ModBadgeRenderer(
					x + imageOffset + minecraft.font.width(trimmedName) + 2,
					RIGHT_PANE_Y,
					width - 28,
					selectedEntry.mod,
					this
				);
				this.init = false;
			}

			if (!ModMenuConfig.HIDE_BADGES.getValue()) {
				modBadgeRenderer.draw(drawContext, mouseX, mouseY);
			}

			if (mod.isReal()) {
				drawContext.drawString(
					font,
					mod.getPrefixedVersion(),
					x + imageOffset,
					RIGHT_PANE_Y + 2 + lineSpacing,
					0xFFAAAAAA,
					true
				);
			}

			String authors;
			List<String> names = mod.getAuthors();
			if (!names.isEmpty()) {
				if (names.size() > 1) {
					authors = Joiner.on(", ").join(names);
				} else {
					authors = names.getFirst();
				}

				DrawingUtil.drawWrappedString(
					drawContext,
					I18n.get("modmenu.authorPrefix", authors),
					x + imageOffset,
					RIGHT_PANE_Y + 2 + lineSpacing * 2,
					this.paneWidth - imageOffset - 4,
					1,
					0xFFAAAAAA
				);
			}
		}
	}

	private Component computeModCountText(boolean includeLibs, boolean onInit) {
		int[] rootMods = formatModCount(ModMenu.ROOT_MODS.values()
			.stream()
			.filter(mod -> !mod.isHidden() && !mod.getBadges().contains(Mod.Badge.LIBRARY))
			.map(Mod::getId)
			.collect(Collectors.toSet()), onInit);
		if (includeLibs && ModMenuConfig.SHOW_LIBRARIES.getValue() && !onInit) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values()
				.stream()
				.filter(mod -> !mod.isHidden() && mod.getBadges().contains(Mod.Badge.LIBRARY))
				.map(Mod::getId)
				.collect(Collectors.toSet()), false);
			return TranslationUtil.translateNumeric("modmenu.showingModsLibraries", rootMods, rootLibs);
		} else {
			return TranslationUtil.translateNumeric("modmenu.showingMods", rootMods);
		}
	}

	private Component computeLibraryCountText(boolean onInit) {
		if (ModMenuConfig.SHOW_LIBRARIES.getValue() && !onInit) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values()
				.stream()
				.filter(mod -> !mod.isHidden() && mod.getBadges().contains(Mod.Badge.LIBRARY))
				.map(Mod::getId)
				.collect(Collectors.toSet()), false);
			return TranslationUtil.translateNumeric("modmenu.showingLibraries", rootLibs);
		} else {
			return Component.empty();
		}
	}

	private int[] formatModCount(Set<String> set, boolean allVisible) {
		int visible = this.modList.getDisplayedCountFor(set);
		int total = set.size();
		if (visible == total || allVisible) {
			return new int[]{total};
		} else {
			return new int[]{visible, total};
		}
	}

	@Override
	public void onClose() {
		this.modList.close();
		this.minecraft.setScreen(this.previousScreen);
	}

	private void setFilterOptionsShown(boolean filterOptionsShown) {
		this.filterOptionsShown = filterOptionsShown;
		this.sortingButton.visible = filterOptionsShown;
		this.librariesButton.visible = filterOptionsShown;
	}

	public ModListEntry getSelectedEntry() {
		return selected;
	}

	public void updateSelectedEntry(ModListEntry entry) {
		if (entry == null) {
			return;
		}

		this.selected = entry;
		String modId = selected.getMod().getId();

		this.descriptionListWidget.updateSelectedMod(selected.getMod());

		if (this.configureButton != null) {

			this.configureButton.active = getModHasConfigScreen(modId);
			this.configureButton.visible =
				selected != null && getModHasConfigScreen(modId) || modScreenErrors.containsKey(modId);

			if (modScreenErrors.containsKey(modId)) {
				Throwable e = modScreenErrors.get(modId);
				this.configureButton.setTooltip(Tooltip.create(ModMenuScreenTexts.configureError(modId, e)));
			} else {
				this.configureButton.setTooltip(Tooltip.create(ModMenuScreenTexts.CONFIGURE));
			}
		}

		boolean isMinecraft = modId.equals("minecraft");
		this.websiteButton.setMessage(isMinecraft ? SEND_FEEDBACK_TEXT : ModMenuScreenTexts.WEBSITE);
		this.issuesButton.setMessage(isMinecraft ? REPORT_BUGS_TEXT : ModMenuScreenTexts.ISSUES);

		this.websiteButton.visible = true;
		this.websiteButton.active = isMinecraft || selected.getMod().getWebsite() != null;

		this.issuesButton.visible = true;
		this.issuesButton.active = isMinecraft || selected.getMod().getIssueTracker() != null;
	}

	public void updateScrollPercent(double scrollPercent) {
		this.scrollPercent = scrollPercent;
	}

	public String getSearchInput() {
		return this.searchBox.getValue();
	}

	private boolean updateFiltersX(boolean onInit) {
		if ((this.filtersWidth + font.width(this.computeModCountText(true, onInit)) + 20) >= this.searchRowWidth &&
			((this.filtersWidth + font.width(this.computeModCountText(false, onInit)) + 20) >= this.searchRowWidth ||
				(this.filtersWidth + font.width(this.computeLibraryCountText(onInit)) + 20) >= this.searchRowWidth
			)) {
			this.filtersX = this.paneWidth / 2 - this.filtersWidth / 2;
			return !filterOptionsShown;
		} else {
			this.filtersX = this.searchRowWidth - this.filtersWidth + 1;
			return true;
		}
	}

	@Override
	public void onFilesDrop(List<Path> paths) {
		Path modsDirectory = FabricLoader.getInstance().getGameDir().resolve("mods");

		// Filter out none mods
		List<Path> mods = paths.stream().filter(ModsScreen::isValidMod).toList();
		if (mods.isEmpty()) {
			return;
		}

		String modList = mods.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
		assert this.minecraft != null;
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
					SystemToast.add(minecraft.getToastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION, ModMenuScreenTexts.DROP_SUCCESSFUL_LINE_1, ModMenuScreenTexts.DROP_SUCCESSFUL_LINE_2);
				}
			}
			this.minecraft.setScreen(this);
		}, ModMenuScreenTexts.DROP_CONFIRM, Component.literal(modList)));
	}

	private static boolean isValidMod(Path mod) {
		try (JarFile jarFile = new JarFile(mod.toFile())) {
			var isFabricMod = jarFile.getEntry("fabric.mod.json") != null;
			if (!ModMenu.RUNNING_QUILT) {
				return isFabricMod;
			} else {
				return isFabricMod || jarFile.getEntry("quilt.mod.json") != null;
			}
		} catch (IOException e) {
			return false;
		}
	}

    private static Path getModsFolder() {
        ModContainer container = FabricLoader.getInstance().getModContainer(ModMenu.MOD_ID).orElseThrow();

        while (container.getContainingMod().isPresent()) {
            container = container.getContainingMod().get();
        }

        if (container.getOrigin().getKind() == ModOrigin.Kind.PATH) {
            return container.getOrigin().getPaths().getFirst().getParent();
        } else {
            // Fall back on the old behavior
            return FabricLoader.getInstance().getGameDir().resolve("mods");
        }
    }

	public boolean getModHasConfigScreen(String modId) {
		if (this.modScreenErrors.containsKey(modId)) {
			return false;
		} else {
			return this.modHasConfigScreen.computeIfAbsent(modId, ModMenu::hasConfigScreen);
		}
	}

	public void safelyOpenConfigScreen(String modId) {
		try {
			Screen screen = ModMenu.getConfigScreen(modId, this);
			if (screen != null) {
				assert this.minecraft != null;
				this.minecraft.setScreen(screen);
			}
		} catch (java.lang.NoClassDefFoundError e) {
			LOGGER.warn("The '{}' mod config screen is not available because {} is missing.", modId, e.getLocalizedMessage());
			modScreenErrors.put(modId, e);
		} catch (Throwable e) {
			LOGGER.error("Error from mod '{}'", modId, e);
			modScreenErrors.put(modId, e);
		}
	}
}
