package io.github.prospector.modmenu.gui;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.util.BadgeRenderer;
import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ingame.ConfirmChatLinkScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.SystemUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModListScreen extends Screen {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Identifier CONFIGURE_BUTTON_LOCATION = new Identifier("modmenu", "textures/gui/configure_button.png");
	protected String textTitle;
	protected TextFieldWidget searchBox;
	protected DescriptionListWidget descriptionListWidget;
	protected Screen parent;
	protected ModListWidget modList;
	protected String tooltip;
	protected ModItem selected;
	protected BadgeRenderer badgeRenderer;
	boolean init = false;
	int leftPaneX;
	int leftPaneRight;
	int paneY;
	int paneWidth;
	int rightPaneX;

	public ModListScreen(Screen previousGui) {
		super(ModMenu.noFabric ? new StringTextComponent("Mods") : new TranslatableTextComponent("modmenu.title"));
		this.parent = previousGui;
		this.textTitle = title.getFormattedText();
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
		this.minecraft.keyboard.enableRepeatEvents(true);
		leftPaneX = 4;
		leftPaneRight = width / 2 - 4;
		paneY = 48;
		paneWidth = this.width / 2 - 8;
		rightPaneX = width / 2 + 4;

		int searchBoxWidth = leftPaneRight - 32;
		this.searchBox = new TextFieldWidget(this.font, leftPaneRight / 2 - searchBoxWidth / 2, 22, searchBoxWidth, 20, this.searchBox, ModMenu.noFabric ? "Search for mods" : I18n.translate("selectWorld.search"));
		this.searchBox.setChangedListener((string_1) -> this.modList.filter(() -> string_1, false));

		this.modList = new ModListWidget(this.minecraft, paneWidth + 4, this.height, paneY, this.height - 36, 36, () -> this.searchBox.getText(), this.modList, this);
		this.modList.setLeftPos(leftPaneX);
		this.descriptionListWidget = new DescriptionListWidget(this.minecraft, paneWidth, this.height, paneY + 60, this.height - 36, font.fontHeight + 1, this);
		this.descriptionListWidget.setLeftPos(rightPaneX);
		ButtonWidget configureButton = new TexturedButtonWidget(width - 24, paneY, 20, 20, 0, 0, CONFIGURE_BUTTON_LOCATION, 32, 64, button -> {
			if (ModMenu.API_MAP.containsKey(modList.getSelectedItem().metadata.getId()) && ModMenu.API_MAP.get(modList.getSelectedItem().metadata.getId()).getConfigScreen(ModListScreen.this).isPresent()) {
				MinecraftClient.getInstance().openScreen(ModMenu.API_MAP.get(modList.getSelectedItem().metadata.getId()).getConfigScreen(this).get().get());
			} else {
				ModMenu.CONFIG_OVERRIDES_LEGACY.get(modList.getSelectedItem().metadata.getId()).run();
			}
		},
			ModMenu.noFabric ? "Configure..." : I18n.translate("modmenu.configure")) {
			@Override
			public void render(int var1, int var2, float var3) {
				active = modList.getSelectedItem() != null && ModMenu.API_MAP.containsKey(modList.getSelectedItem().metadata.getId()) && ModMenu.API_MAP.get(modList.getSelectedItem().metadata.getId()).getConfigScreen(ModListScreen.this).isPresent() || ModMenu.CONFIG_OVERRIDES_LEGACY.get(modList.getSelectedItem().metadata.getId()) != null;
				visible = modList.getSelectedItem() != null;
				super.render(var1, var2, var3);
				if (!active && isHovered) {
					setTooltip(ModMenu.noFabric ? "Configuration Unavailable" : I18n.translate("modmenu.configurationUnavailable"));
				}
			}
		};
		int urlButtonWidths = paneWidth / 2 - 2;
		int cappedButtonWidth = urlButtonWidths > 200 ? 200 : urlButtonWidths;
		ButtonWidget websiteButton = new ButtonWidget(rightPaneX + (urlButtonWidths / 2) - (cappedButtonWidth / 2), paneY + 36, urlButtonWidths > 200 ? 200 : urlButtonWidths, 20,
			ModMenu.noFabric ? "Website" : I18n.translate("modmenu.website"), button -> this.minecraft.openScreen(new ConfirmChatLinkScreen((bool) -> {
			if (bool) {
				SystemUtil.getOperatingSystem().open(modList.getSelectedItem().metadata.getContact().get("homepage").get());
			}
			this.minecraft.openScreen(this);
		}, modList.getSelectedItem().metadata.getContact().get("homepage").get(), true))) {
			@Override
			public void render(int var1, int var2, float var3) {
				active = modList.getSelectedItem() != null && modList.getSelectedItem().metadata.getContact().get("homepage").isPresent();
				visible = modList.getSelectedItem() != null;
				super.render(var1, var2, var3);
			}
		};
		ButtonWidget issuesButton = new ButtonWidget(rightPaneX + urlButtonWidths + 4 + (urlButtonWidths / 2) - (cappedButtonWidth / 2), paneY + 36, urlButtonWidths > 200 ? 200 : urlButtonWidths, 20,
			ModMenu.noFabric ? "Issues" : I18n.translate("modmenu.issues"), button -> this.minecraft.openScreen(new ConfirmChatLinkScreen((bool) -> {
			if (bool) {
				SystemUtil.getOperatingSystem().open(modList.getSelectedItem().metadata.getContact().get("issues").get());
			}
			this.minecraft.openScreen(this);
		}, modList.getSelectedItem().metadata.getContact().get("issues").get(), true))) {
			@Override
			public void render(int var1, int var2, float var3) {
				active = modList.getSelectedItem() != null && modList.getSelectedItem().metadata.getContact().get("issues").isPresent();
				visible = modList.getSelectedItem() != null;
				super.render(var1, var2, var3);
			}
		};
		this.children.add(this.searchBox);
		this.children.add(this.modList);
		this.

			addButton(configureButton);
		this.

			addButton(websiteButton);
		this.

			addButton(issuesButton);
		this.children.add(this.descriptionListWidget);
		this.

			addButton(new ButtonWidget(this.width / 2 - 154, this.height - 28, 150, 20,
				ModMenu.noFabric ? "Open Mods Folder" : I18n.translate("modmenu.modsFolder"), button -> SystemUtil.getOperatingSystem().

				open(new File(FabricLoader.getInstance().

					getGameDirectory(), "mods"))));
		this.

			addButton(new ButtonWidget(this.width / 2 + 4, this.height - 28, 150, 20, I18n.translate("gui.done"), button -> minecraft.openScreen(parent)));
		this.

			method_20085(this.searchBox);

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
		overlayBackground(0, 0, width, height, 64, 64, 64, 255, 255);
		this.tooltip = null;
		if (modList.getSelectedItem() != null) {
			this.descriptionListWidget.render(mouseX, mouseY, delta);
		}
		this.modList.render(mouseX, mouseY, delta);
		this.searchBox.render(mouseX, mouseY, delta);
		overlayBackground(modList.getWidth(), 0, width / 2 + 4, height, 64, 64, 64, 255, 255);
		overlayBackground(0, 0, 4, height, 64, 64, 64, 255, 255);
		overlayBackground(width - 4, 0, width, height, 64, 64, 64, 255, 255);
		GlStateManager.disableBlend();
		this.drawCenteredString(this.font, this.textTitle, this.modList.getWidth() / 2, 8, 16777215);
		super.render(mouseX, mouseY, delta);

		ModMetadata metadata = modList.getSelectedItem().metadata;
		int x = rightPaneX;
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(selected.icon != null ? selected.iconLocation : ModItem.unknownIcon);
		GlStateManager.enableBlend();
		blit(x, paneY, 0.0F, 0.0F, 32, 32, 32, 32);
		GlStateManager.disableBlend();
		int lineSpacing = font.fontHeight + 1;
		int imageOffset = 36;
		font.draw(metadata.getName(), x + imageOffset, paneY + 1, 0xFFFFFF);
		if (mouseX > x + imageOffset && mouseY > paneY + 1 && mouseY < paneY + 1 + font.fontHeight && mouseX < x + imageOffset + font.getStringWidth(metadata.getName())) {
			setTooltip(I18n.translate("modmenu.modIdToolTip", metadata.getId()));
		}
		if (init || badgeRenderer == null || badgeRenderer.metadata != metadata) {
			badgeRenderer = new BadgeRenderer(x + imageOffset + this.minecraft.textRenderer.getStringWidth(metadata.getName()) + 2, paneY, width - 28, metadata, this);
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
			RenderUtils.drawWrappedString(ModMenu.noFabric ? "By " + authors : I18n.translate("modmenu.authorPrefix", authors), x + imageOffset, paneY + 2 + lineSpacing * 2, paneWidth - imageOffset - 4, 1, 0x808080);
		}
		if (this.tooltip != null) {
			this.renderTooltip(Lists.newArrayList(Splitter.on("\n").split(this.tooltip)), mouseX, mouseY);
		}

	}

	protected void overlayBackground(int x1, int y1, int x2, int y2, int red, int green, int blue, int startAlpha, int endAlpha) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBufferBuilder();
		minecraft.getTextureManager().bindTexture(DrawableHelper.BACKGROUND_LOCATION);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		float f = 32.0F;
		buffer.begin(7, VertexFormats.POSITION_UV_COLOR);
		buffer.vertex(x1, y2, 0.0D).texture(x1 / 32.0D, y2 / 32.0D).color(red, green, blue, endAlpha).next();
		buffer.vertex(x2, y2, 0.0D).texture(x2 / 32.0D, y2 / 32.0D).color(red, green, blue, endAlpha).next();
		buffer.vertex(x2, y1, 0.0D).texture(x2 / 32.0D, y1 / 32.0D).color(red, green, blue, startAlpha).next();
		buffer.vertex(x1, y1, 0.0D).texture(x1 / 32.0D, y1 / 32.0D).color(red, green, blue, startAlpha).next();
		tessellator.draw();
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
}
