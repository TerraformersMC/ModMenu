package io.github.prospector.modmenu.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.prospector.modmenu.ModMenu;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.SystemUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class ModListScreen extends Screen {
	private static final Logger LOGGER = LogManager.getLogger();
	protected String textTitle;
	protected TextFieldWidget searchBox;
	protected DescriptionListWidget descriptionListWidget;
	protected Screen parent;
	protected ModListWidget modList;
	private String tooltip;

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
		int paneX = width / 2 - 6;
		int paneY = 48;
		int paneWidth = this.width - paneX;

		int searchBoxWidth = paneX - 32;
		this.searchBox = new TextFieldWidget(this.font, paneX / 2 - searchBoxWidth / 2, 22, searchBoxWidth, 20, this.searchBox);
		this.searchBox.setChangedListener((string_1) -> this.modList.filter(() -> string_1, false));

		this.modList = new ModListWidget(this.minecraft, paneX, this.height, paneY, this.height - 36, 36, () -> this.searchBox.getText(), this.modList, this);
		this.descriptionListWidget = new DescriptionListWidget(this.minecraft, paneX + 6, this.height, paneY + 60, this.height - 36, 12, this);
		this.descriptionListWidget.setLeftPos(paneX + 6);

		int configButtonWidth = 100;
		ButtonWidget configureButton = new ButtonWidget(paneX + paneWidth / 2 - configButtonWidth / 2, modList.getY() + 36, configButtonWidth, 20,
			ModMenu.noFabric ? "Configure..." : I18n.translate("modmenu.configure", new Object[0]), button -> ModMenu.CONFIG_OVERRIDES.get(modList.getSelectedItem().metadata.getId()).run()) {
			@Override
			public void render(int var1, int var2, float var3) {
				active = modList.getSelectedItem() != null && ModMenu.CONFIG_OVERRIDES.get(modList.getSelectedItem().metadata.getId()) != null;
				visible = modList.getSelectedItem() != null;
				super.render(var1, var2, var3);
			}
		};
		this.children.add(this.searchBox);
		this.children.add(this.modList);
		this.addButton(configureButton);
		this.children.add(this.descriptionListWidget);
		this.addButton(new ButtonWidget(this.width / 2 - 154, this.height - 28, 150, 20,
			ModMenu.noFabric ? "Open Mods Folder" : I18n.translate("modmenu.modsFolder"), button -> SystemUtil.getOperatingSystem().open(new File(FabricLoader.getInstance().getGameDirectory(), "mods"))));
		this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 28, 150, 20, I18n.translate("gui.done"), button -> minecraft.openScreen(parent)));
		this.method_20085(this.searchBox);
	}

	public ModListWidget getModList() {
		return modList;
	}

	@Override
	public boolean keyPressed(int int_1, int int_2, int int_3) {
		return super.keyPressed(int_1, int_2, int_3) ? true : this.searchBox.keyPressed(int_1, int_2, int_3);
	}

	@Override
	public boolean charTyped(char char_1, int int_1) {
		return this.searchBox.charTyped(char_1, int_1);
	}

	@Override
	public void render(int var1, int var2, float var3) {
		overlayBackground(0, 0, width, height, 64, 64, 64, 255, 255);
		this.tooltip = null;
		if (modList.getSelectedItem() != null)
			this.descriptionListWidget.render(var1, var2, var3);
		this.modList.render(var1, var2, var3);
		this.searchBox.render(var1, var2, var3);
		overlayBackground(modList.getWidth(), 0, width / 2, height, 64, 64, 64, 255, 255);
		GlStateManager.disableBlend();
		this.drawCenteredString(this.font, this.textTitle, this.modList.getWidth() / 2, 8, 16777215);
		super.render(var1, var2, var3);
		if (this.tooltip != null) {
			this.renderTooltip(Lists.newArrayList(Splitter.on("\n").split(this.tooltip)), var1, var2);
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
