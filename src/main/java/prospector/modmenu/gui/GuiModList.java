package prospector.modmenu.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.SystemUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class GuiModList extends Gui {
	private static final Logger LOGGER = LogManager.getLogger();
	protected String title;
	private String tooltip;
	protected TextFieldWidget searchBox;
	private ModListWidget modList;
	public ModContainer selectedMod = null;
	protected Gui previousGui;

	public GuiModList(Gui previousGui) {
		this.previousGui = previousGui;
	}

	@Override
	public boolean mouseScrolled(double var1) {
		return this.modList.mouseScrolled(var1);
	}

	@Override
	public void update() {
		this.searchBox.tick();
	}

	@Override
	protected void onInitialized() {
		this.client.keyboard.enableRepeatEvents(true);
		this.title = I18n.translate("modmenu.title");
		this.searchBox = new TextFieldWidget(0, this.fontRenderer, this.width / 2 - 100, 22, 200, 20, this.searchBox) {
			@Override
			public void method_1876(boolean var1) {
				super.method_1876(true);
			}
		};
		this.searchBox.method_1863((var1, var2) -> {
			this.modList.searchFilter(() -> var2, false);
		});

		this.modList = new ModListWidget(this.client, this.width, this.height, 48, this.height - 118, 36, () -> this.searchBox.getText(), this.modList);

		this.addButton(new ButtonWidget(1, this.width / 2 - 154, this.height - 52, 150, 20, I18n.translate("modmenu.modsFolder", new Object[0])) {
			@Override
			public void onPressed(double var1, double var3) {
				SystemUtil.getOperatingSystem().open(new File(FabricLoader.INSTANCE.getGameDirectory(), "mods"));
			}
		});
		this.addButton(new ButtonWidget(2, this.width / 2 + 4, this.height - 52, 150, 20, I18n.translate("modmenu.configsFolder", new Object[0])) {
			@Override
			public void onPressed(double var1, double var3) {
				SystemUtil.getOperatingSystem().open(FabricLoader.INSTANCE.getConfigDirectory());
			}
		});
		ButtonWidget configureButton = new ButtonWidget(3, this.width / 2 - 154, this.height - 28, 150, 20, I18n.translate("modmenu.configure", new Object[0])) {
			@Override
			public void onPressed(double var1, double var3) {

			}
		};
		configureButton.enabled = false;
		this.addButton(configureButton);
		this.addButton(new ButtonWidget(0, this.width / 2 + 4, this.height - 28, 150, 20, I18n.translate("gui.cancel", new Object[0])) {
			@Override
			public void onPressed(double var1, double var3) {
				client.openGui(previousGui);
			}
		});
		this.listeners.add(this.searchBox);
		this.listeners.add(this.modList);
		this.searchBox.method_1876(true);
		this.searchBox.method_1856(false);
	}

	@Override
	public boolean keyPressed(int var1, int var2, int var3) {
		return super.keyPressed(var1, var2, var3) || this.searchBox.keyPressed(var1, var2, var3);
	}

	@Override
	public boolean charTyped(char var1, int var2) {
		return this.searchBox.charTyped(var1, var2);
	}

	@Override
	public void draw(int var1, int var2, float var3) {
		this.tooltip = null;
		this.modList.draw(var1, var2, var3);
		this.searchBox.render(var1, var2, var3);
		this.drawStringCentered(this.fontRenderer, this.title, this.width / 2, 8, 16777215);
		super.draw(var1, var2, var3);
		if (this.tooltip != null) {
			this.drawTooltip(Lists.newArrayList(Splitter.on("\n").split(this.tooltip)), var1, var2);
		}

	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
}
