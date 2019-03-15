package io.github.prospector.modmenu.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.prospector.modmenu.ModMenu;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.SystemUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class ModListScreen extends Screen {
	private static final Logger LOGGER = LogManager.getLogger();
	protected String title;
	private String tooltip;
	protected TextFieldWidget searchBox;
	private ModListWidget modList;
	protected Screen previousGui;

	public ModListScreen(Screen previousGui) {
		this.previousGui = previousGui;
	}

	@Override
	public boolean mouseScrolled(double double_1, double double_2, double double_3) {
		return this.modList.mouseScrolled(double_1, double_2, double_3);
	}

	@Override
	public void update() {
		this.searchBox.tick();
	}

	@Override
	protected void onInitialized() {
		this.client.keyboard.enableRepeatEvents(true);
		this.title = ModMenu.noFabric ? "Mods" : I18n.translate("modmenu.title");
		int paneX = (int) (this.screenWidth * 0.48);
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

		this.addButton(new ButtonWidget(this.screenWidth / 2 - 154, this.screenHeight - 28, 150, 20, ModMenu.noFabric ? "Open Mods Folder" : I18n.translate("modmenu.modsFolder", new Object[0])) {
			@Override
			public void onPressed() {
				SystemUtil.getOperatingSystem().open(new File(FabricLoader.getInstance().getGameDirectory(), "mods"));
			}
		});
		//		this.addButton(new ButtonWidget(2, this.width / 2 + 4, this.height - 28, 150, 20, I18n.translate("modmenu.configsFolder", new Object[0])) {
		//			@Override
		//			public void onPressed(double var1, double var3) {
		//				SystemUtil.getOperatingSystem().open(FabricLoader.INSTANCE.getConfigDirectory());
		//			}
		//		});

		int configButtonWidth = 100;
		ButtonWidget configureButton = new ButtonWidget(paneX + paneWidth / 2 - configButtonWidth / 2, modList.getY1() + 36, configButtonWidth, 20, ModMenu.noFabric ? "Configure..." : I18n.translate("modmenu.configure", new Object[0])) {
			@Override
			public void onPressed() {
				ModMenu.CONFIG_RUNNABLE_MAP.get(modList.selected.info.getId()).run();
			}

			@Override
			public void draw(int var1, int var2, float var3) {
			    enabled = modList.selected != null && ModMenu.CONFIG_RUNNABLE_MAP.get(modList.selected.info.getId()) != null;
				visible = modList.selected != null;
				super.draw(var1, var2, var3);
			}
		};
		this.addButton(configureButton);
		this.addButton(new ButtonWidget(this.screenWidth / 2 + 4, this.screenHeight - 28, 150, 20, I18n.translate("gui.done", new Object[0])) {
			@Override
			public void onPressed() {
				client.openScreen(previousGui);
			}
		});
		this.listeners.add(this.searchBox);
		this.listeners.add(this.modList);
		this.searchBox.setFocused(true);
		this.searchBox.method_1856(false);
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
	public void drawBackground() {
		this.drawTextureBackground(0);
	}

	@Override
	public void draw(int var1, int var2, float var3) {
		this.drawBackground();
		this.tooltip = null;
		this.modList.draw(var1, var2, var3);
		this.searchBox.draw(var1, var2, var3);
		GlStateManager.disableBlend();
		this.drawStringCentered(this.fontRenderer, this.title, this.modList.getWidth() / 2, 8, 16777215);
		super.draw(var1, var2, var3);
		if (this.tooltip != null) {
			this.drawTooltip(Lists.newArrayList(Splitter.on("\n").split(this.tooltip)), var1, var2);
		}

	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
}
