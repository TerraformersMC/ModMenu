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
	protected String title;
	protected TextFieldWidget searchBox;
	protected DescriptionListWidget descriptionListWidget;
	protected Screen previousGui;
	protected ModListWidget modList;
	private String tooltip;

	public ModListScreen(Screen previousGui) {
		super(ModMenu.noFabric ? new StringTextComponent("Mods") : new TranslatableTextComponent("modmenu.title"));
		this.previousGui = previousGui;
	}

	@Override
	public boolean mouseScrolled(double double_1, double double_2, double double_3) {
		if (modList.isSelected(double_1, double_2))
			return this.modList.mouseScrolled(double_1, double_2, double_3);
		if (descriptionListWidget.isSelected(double_1, double_2))
			return this.descriptionListWidget.mouseScrolled(double_1, double_2, double_3);
		return false;
	}

	@Override
	public void update() {
		this.searchBox.tick();
	}

	@Override
	protected void onInitialized() {
		this.client.keyboard.enableRepeatEvents(true);
		int paneX = screenWidth / 2 - 6;
		int paneY = 48;
		int paneWidth = this.screenWidth - paneX;

		int searchBoxWidth = paneX - 32;
		this.searchBox = new TextFieldWidget(this.fontRenderer, paneX / 2 - searchBoxWidth / 2, 22, searchBoxWidth, 20, this.searchBox) {
			@Override
			public void setFocused(boolean focused) {
				super.setFocused(true);
			}
		};
		this.searchBox.setChangedListener((string_1) -> this.modList.searchFilter(() -> string_1, false));

		this.modList = new ModListWidget(this.client, paneX, this.screenHeight, paneY, this.screenHeight - 36, 36, () -> this.searchBox.getText(), this.modList, this);
		this.descriptionListWidget = new DescriptionListWidget(this.client, paneX + 6, this.screenHeight, paneY + 60, this.screenHeight - 36, 12, this);
		this.descriptionListWidget.setX(paneX + 6);

		this.addButton(new ButtonWidget(this.screenWidth / 2 - 154, this.screenHeight - 28, 150, 20,
			ModMenu.noFabric ? "Open Mods Folder" : I18n.translate("modmenu.modsFolder"), button -> SystemUtil.getOperatingSystem().open(new File(FabricLoader.getInstance().getGameDirectory(), "mods"))));

		int configButtonWidth = 100;
		ButtonWidget configureButton = new ButtonWidget(paneX + paneWidth / 2 - configButtonWidth / 2, modList.getY() + 36, configButtonWidth, 20,
			ModMenu.noFabric ? "Configure..." : I18n.translate("modmenu.configure", new Object[0]), button -> ModMenu.CONFIG_OVERRIDES.get(modList.selected.info.getId()).run()) {
			@Override
			public void render(int var1, int var2, float var3) {
				active = modList.selected != null && ModMenu.CONFIG_OVERRIDES.get(modList.selected.info.getId()) != null;
				visible = modList.selected != null;
				super.render(var1, var2, var3);
			}
		};
		this.addButton(configureButton);
		this.addButton(new ButtonWidget(this.screenWidth / 2 + 4, this.screenHeight - 28, 150, 20, I18n.translate("gui.done"), button -> client.openScreen(previousGui)));
		this.listeners.add(this.searchBox);
		this.listeners.add(this.modList);
		this.listeners.add(this.descriptionListWidget);
		this.searchBox.setFocused(true);
		this.searchBox.method_1856(false);
	}

	public ModListWidget getModList() {
		return modList;
	}

	@Override
	public boolean keyPressed(int var1, int var2, int var3) {
		if (var1 == 256 && this.doesEscapeKeyClose()) {
			client.openScreen(previousGui);
			return true;
		}
		return super.keyPressed(var1, var2, var3) || this.searchBox.keyPressed(var1, var2, var3);
	}

	@Override
	public boolean charTyped(char var1, int var2) {
		return this.searchBox.charTyped(var1, var2);
	}

	@Override
	public void render(int var1, int var2, float var3) {
		overlayBackground(0, 0, screenWidth, screenHeight, 64, 64, 64, 255, 255);
		this.tooltip = null;
		this.descriptionListWidget.render(var1, var2, var3);
		this.modList.render(var1, var2, var3);
		this.searchBox.render(var1, var2, var3);
		overlayBackground(modList.getWidth(), 0, screenWidth / 2, screenHeight, 64, 64, 64, 255, 255);
		GlStateManager.disableBlend();
		this.drawStringCentered(this.fontRenderer, this.title, this.modList.getWidth() / 2, 8, 16777215);
		super.render(var1, var2, var3);
		if (this.tooltip != null) {
			this.drawTooltip(Lists.newArrayList(Splitter.on("\n").split(this.tooltip)), var1, var2);
		}
	}

	protected void overlayBackground(int x1, int y1, int x2, int y2, int red, int green, int blue, int startAlpha, int endAlpha) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBufferBuilder();
		client.getTextureManager().bindTexture(DrawableHelper.OPTIONS_BG);
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
