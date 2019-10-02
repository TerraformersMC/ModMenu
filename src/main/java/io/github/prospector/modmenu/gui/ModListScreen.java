package io.github.prospector.modmenu.gui;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.util.BadgeRenderer;
import io.github.prospector.modmenu.util.HardcodedUtil;
import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.SystemUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Supplier;

public class ModListScreen extends Screen {
	private static final Identifier FILTERS_BUTTON_LOCATION = new Identifier(ModMenu.MOD_ID, "textures/gui/filters_button.png");
	private static final Identifier CONFIGURE_BUTTON_LOCATION = new Identifier(ModMenu.MOD_ID, "textures/gui/configure_button.png");
	private static final Logger LOGGER = LogManager.getLogger();
	private final String textTitle;
	private TextFieldWidget searchBox;
	private DescriptionListWidget descriptionListWidget;
	private Screen parent;
	private ModListWidget modList;
	private String tooltip;
	private ModListEntry selected;
	private BadgeRenderer badgeRenderer;
	private double scrollPercent = 0;
	private boolean showModCount = false;
	private boolean init = false;
	private boolean filterOptionsShown = false;
	private int paneY;
	private int paneWidth;
	private int rightPaneX;
	private int searchBoxX;
	public Set<String> showModChildren = new HashSet<>();

	public ModListScreen(Screen previousGui) {
		super(new TranslatableText("modmenu.title"));
		this.parent = previousGui;
		this.textTitle = title.asFormattedString();
	}

	@Override
	public boolean mouseScrolled(double double_1, double double_2, double double_3) {
		if (modList.isMouseOver(double_1, double_2))
			return this.modList.mouseScrolled(double_1, double_2, double_3);
		if (descriptionListWidget.isMouseOver(double_1, double_2))
			return this.descriptionListWidget.mouseScrolled(double_1, double_2, double_3);
		return false;
	}

	@Override
	public void tick() {
		this.searchBox.tick();
	}

	@Override
	protected void init() {
		Objects.requireNonNull(this.minecraft).keyboard.enableRepeatEvents(true);
		paneY = 48;
		paneWidth = this.width / 2 - 8;
		rightPaneX = width - paneWidth;

		int searchBoxWidth = paneWidth - 32 - 22;
		searchBoxX = paneWidth / 2 - searchBoxWidth / 2 - 22 / 2;
		this.searchBox = new TextFieldWidget(this.font, searchBoxX, 22, searchBoxWidth, 20, this.searchBox, I18n.translate("selectWorld.search"));
		this.searchBox.setChangedListener((string_1) -> this.modList.filter(() -> string_1, false));
		this.modList = new ModListWidget(this.minecraft, paneWidth, this.height, paneY + 19, this.height - 36, 36, () -> this.searchBox.getText(), this.modList, this);
		this.modList.setLeftPos(0);
		this.descriptionListWidget = new DescriptionListWidget(this.minecraft, paneWidth, this.height, paneY + 60, this.height - 36, font.fontHeight + 1, this);
		this.descriptionListWidget.setLeftPos(rightPaneX);
		ButtonWidget configureButton = new ModMenuTexturedButtonWidget(width - 24, paneY, 20, 20, 0, 0, CONFIGURE_BUTTON_LOCATION, 32, 64, button -> {
			final String modid = Objects.requireNonNull(selected).getMetadata().getId();
			final Screen screen = ModMenu.getConfigScreen(modid, this);
			if (screen != null) {
				minecraft.openScreen(screen);
			} else {
				ModMenu.openConfigScreen(modid);
			}
		},
			I18n.translate("modmenu.configure")) {
			@Override
			public void render(int mouseX, int mouseY, float delta) {
				if (selected != null) {
					String modid = selected.getMetadata().getId();
					active = ModMenu.hasFactory(modid) || ModMenu.hasLegacyConfigScreenTask(modid);
				} else {
					active = false;
				}
				visible = active;
				super.render(mouseX, mouseY, delta);
			}

			@Override
			public void renderButton(int int_1, int int_2, float float_1) {
				GlStateManager.color4f(1, 1, 1, 1f);
				super.renderButton(int_1, int_2, float_1);
			}
		};

		int urlButtonWidths = paneWidth / 2 - 2;
		int cappedButtonWidth = urlButtonWidths > 200 ? 200 : urlButtonWidths;
		ButtonWidget websiteButton = new ButtonWidget(rightPaneX + (urlButtonWidths / 2) - (cappedButtonWidth / 2), paneY + 36, urlButtonWidths > 200 ? 200 : urlButtonWidths, 20,
			I18n.translate("modmenu.website"), button -> {
			final ModMetadata metadata = Objects.requireNonNull(selected).getMetadata();
			this.minecraft.openScreen(new ConfirmChatLinkScreen((bool) -> {
				if (bool) {
					SystemUtil.getOperatingSystem().open(metadata.getContact().get("homepage").get());
				}
				this.minecraft.openScreen(this);
			}, metadata.getContact().get("homepage").get(), true));
		}) {
			@Override
			public void render(int var1, int var2, float var3) {
				visible = selected != null;
				active = visible && selected.getMetadata().getContact().get("homepage").isPresent();
				super.render(var1, var2, var3);
			}
		};
		ButtonWidget issuesButton = new ButtonWidget(rightPaneX + urlButtonWidths + 4 + (urlButtonWidths / 2) - (cappedButtonWidth / 2), paneY + 36, urlButtonWidths > 200 ? 200 : urlButtonWidths, 20,
			I18n.translate("modmenu.issues"), button -> {
			final ModMetadata metadata = Objects.requireNonNull(selected).getMetadata();
			this.minecraft.openScreen(new ConfirmChatLinkScreen((bool) -> {
				if (bool) {
					SystemUtil.getOperatingSystem().open(metadata.getContact().get("issues").get());
				}
				this.minecraft.openScreen(this);
			}, metadata.getContact().get("issues").get(), true));
		}) {
			@Override
			public void render(int var1, int var2, float var3) {
				visible = selected != null;
				active = visible && selected.getMetadata().getContact().get("issues").isPresent();
				super.render(var1, var2, var3);
			}
		};
		this.children.add(this.searchBox);
		ButtonWidget toggleOptions;
		this.addButton(toggleOptions=new ModMenuTexturedButtonWidget(paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2, 22, 20, 20, 0, 0, FILTERS_BUTTON_LOCATION, 32, 64, button -> {
			filterOptionsShown = !filterOptionsShown;
		}) {
			@Override
			public void render(int int_1, int int_2, float float_1) {
				super.render(int_1, int_2, float_1);
				if (isHovered()) {
					setTooltip(I18n.translate("modmenu.toggleFilterOptions"));
				}
			}
		});

		ButtonWidget toggleActualModCount=new ButtonWidget(toggleOptions.x+toggleOptions.getWidth(),toggleOptions.y,20,20,"AMC",buttonWidget -> ModMenuConfigManager.getConfig().toggleActualModCount());
		addButton(toggleActualModCount);

		String showLibrariesText = I18n.translate("modmenu.showLibraries", I18n.translate("modmenu.showLibraries." + ModMenuConfigManager.getConfig().showLibraries()));
		String sortingText = I18n.translate("modmenu.sorting", I18n.translate(ModMenuConfigManager.getConfig().getSorting().getTranslationKey()));
		int showLibrariesWidth = font.getStringWidth(showLibrariesText) + 20;
		int sortingWidth = font.getStringWidth(sortingText) + 20;
		int filtersX;
		int filtersWidth = showLibrariesWidth + sortingWidth + 2;
		if ((filtersWidth + font.getStringWidth(I18n.translate("modmenu.showingMods", NumberFormat.getInstance().format(FabricLoader.getInstance().getAllMods().size()) + "/" + NumberFormat.getInstance().format(FabricLoader.getInstance().getAllMods().size()))) + 20) >= searchBoxX + searchBoxWidth + 22) {
			filtersX = paneWidth / 2 - filtersWidth / 2;
			showModCount = false;
		} else {
			filtersX = searchBoxX + searchBoxWidth + 22 - filtersWidth + 1;
			showModCount = true;
		}
		this.addButton(new ButtonWidget(filtersX, 45, sortingWidth, 20, sortingText, button -> {
			ModMenuConfigManager.getConfig().toggleSortMode();
			modList.reloadFilters();
		}) {
			@Override
			public void render(int mouseX, int mouseY, float delta) {
				visible = filterOptionsShown;
				this.setMessage(I18n.translate("modmenu.sorting", I18n.translate(ModMenuConfigManager.getConfig().getSorting().getTranslationKey())));
				super.render(mouseX, mouseY, delta);
			}
		});
		this.addButton(new ButtonWidget(filtersX + sortingWidth + 2, 45, showLibrariesWidth, 20, I18n.translate("modmenu.showLibraries", I18n.translate("modmenu.showLibraries." + ModMenuConfigManager.getConfig().showLibraries())), button -> {
			ModMenuConfigManager.getConfig().toggleShowLibraries();
			modList.reloadFilters();
		}) {
			@Override
			public void render(int mouseX, int mouseY, float delta) {
				visible = filterOptionsShown;
				this.setMessage(I18n.translate("modmenu.showLibraries", I18n.translate("modmenu.showLibraries." + ModMenuConfigManager.getConfig().showLibraries())));
				super.render(mouseX, mouseY, delta);
			}
		});
		this.children.add(this.modList);
		this.addButton(configureButton);
		this.addButton(websiteButton);
		this.addButton(issuesButton);
		this.children.add(this.descriptionListWidget);
		this.addButton(new ButtonWidget(this.width / 2 - 154, this.height - 28, 150, 20, I18n.translate("modmenu.modsFolder"), button -> SystemUtil.getOperatingSystem().open(new File(FabricLoader.getInstance().getGameDirectory(), "mods"))));
		this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 28, 150, 20, I18n.translate("gui.done"), button -> minecraft.openScreen(parent)));
		this.setInitialFocus(this.searchBox);

		init = true;
	}

	public ModListWidget getModList() {
		return modList;
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
	public void render(int mouseX, int mouseY, float delta) {
		ModListScreen.overlayBackground(paneWidth, 0, rightPaneX, height, 64, 64, 64, 255, 255);
		this.tooltip = null;
		ModListEntry selectedEntry = selected;
		if (selectedEntry != null) {
			this.descriptionListWidget.render(mouseX, mouseY, delta);
		}
		this.modList.render(mouseX, mouseY, delta);
		this.searchBox.render(mouseX, mouseY, delta);
		GlStateManager.disableBlend();
		this.drawCenteredString(this.font, this.textTitle, this.modList.getWidth() / 2, 8, 16777215);
		super.render(mouseX, mouseY, delta);
		if (showModCount || !filterOptionsShown) {
			font.draw(I18n.translate("modmenu.showingMods", NumberFormat.getInstance().format(modList.getDisplayedCount()) + "/" + NumberFormat.getInstance().format(FabricLoader.getInstance().getAllMods().size())), searchBoxX, 52, 0xFFFFFF);
		}
		if (selectedEntry != null) {
			ModMetadata metadata = selectedEntry.getMetadata();
			int x = rightPaneX;
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.selected.bindIconTexture();
			GlStateManager.enableBlend();
			blit(x, paneY, 0.0F, 0.0F, 32, 32, 32, 32);
			GlStateManager.disableBlend();
			int lineSpacing = font.fontHeight + 1;
			int imageOffset = 36;
			String name = metadata.getName();
			name = HardcodedUtil.formatFabricModuleName(name);
			String trimmedName = name;
			int maxNameWidth = this.width - (x + imageOffset);
			if (font.getStringWidth(name) > maxNameWidth) {
				trimmedName = font.trimToWidth(name, maxNameWidth - font.getStringWidth("...")) + "...";
			}
			font.draw(trimmedName, x + imageOffset, paneY + 1, 0xFFFFFF);
			if (mouseX > x + imageOffset && mouseY > paneY + 1 && mouseY < paneY + 1 + font.fontHeight && mouseX < x + imageOffset + font.getStringWidth(trimmedName)) {
				setTooltip(I18n.translate("modmenu.modIdToolTip", metadata.getId()));
			}
			if (init || badgeRenderer == null || badgeRenderer.getMetadata() != metadata) {
				badgeRenderer = new BadgeRenderer(x + imageOffset + Objects.requireNonNull(this.minecraft).textRenderer.getStringWidth(trimmedName) + 2, paneY, width - 28, selectedEntry.container, this);
				init = false;
			}
			badgeRenderer.draw(mouseX, mouseY);
			font.draw("v" + metadata.getVersion().getFriendlyString(), x + imageOffset, paneY + 2 + lineSpacing, 0x808080);
			String authors;
			List<String> names = new ArrayList<>();
			metadata.getAuthors().forEach(person -> names.add(person.getName()));
			if (!names.isEmpty()) {
				if (names.size() > 1) {
					authors = Joiner.on(", ").join(names);
				} else {
					authors = names.get(0);
				}
				RenderUtils.drawWrappedString(I18n.translate("modmenu.authorPrefix", authors), x + imageOffset, paneY + 2 + lineSpacing * 2, paneWidth - imageOffset - 4, 1, 0x808080);
			}
			if (this.tooltip != null) {
				this.renderTooltip(Lists.newArrayList(Splitter.on("\n").split(this.tooltip)), mouseX, mouseY);
			}
		}

	}

	public static void overlayBackground(int x1, int y1, int x2, int y2, int red, int green, int blue, int startAlpha, int endAlpha) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBufferBuilder();
		Objects.requireNonNull(MinecraftClient.getInstance()).getTextureManager().bindTexture(DrawableHelper.BACKGROUND_LOCATION);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		buffer.begin(7, VertexFormats.POSITION_UV_COLOR);
		buffer.vertex(x1, y2, 0.0D).texture(x1 / 32.0D, y2 / 32.0D).color(red, green, blue, endAlpha).next();
		buffer.vertex(x2, y2, 0.0D).texture(x2 / 32.0D, y2 / 32.0D).color(red, green, blue, endAlpha).next();
		buffer.vertex(x2, y1, 0.0D).texture(x2 / 32.0D, y1 / 32.0D).color(red, green, blue, startAlpha).next();
		buffer.vertex(x1, y1, 0.0D).texture(x1 / 32.0D, y1 / 32.0D).color(red, green, blue, startAlpha).next();
		tessellator.draw();
	}

	@Override
	public void onClose() {
		super.onClose();
		this.modList.close();
	}

	public void setTooltip(String tooltip) {
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

	public Supplier<String> getSearchInput() {
		return () -> searchBox.getText();
	}

	public boolean showingFilterOptions() {
		return filterOptionsShown;
	}
}
