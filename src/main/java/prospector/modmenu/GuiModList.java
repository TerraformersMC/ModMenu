package prospector.modmenu;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.widget.WidgetButton;
import net.minecraft.client.gui.widget.WidgetTextField;
import net.minecraft.client.resource.language.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiModList extends Gui {
	private static final Logger LOGGER = LogManager.getLogger();
	protected String title;
	private String tooltip;
	protected WidgetTextField searchBox;
	private WidgetModList modList;
	protected Gui previousGui;

	public GuiModList(Gui previousGui) {
		this.previousGui = previousGui;
	}

	public boolean mouseScrolled(double var1) {
		return this.modList.mouseScrolled(var1);
	}

	public void update() {
		this.searchBox.tick();
	}

	protected void onInitialized() {
		this.game.field_1774.method_1462(true);
		this.title = I18n.translate("modmenu.title");
		this.searchBox = new WidgetTextField(0, this.fontRenderer, this.width / 2 - 100, 22, 200, 20, this.searchBox) {
			public void method_1876(boolean var1) {
				super.method_1876(true);
			}
		};
		this.searchBox.method_1863((var1, var2) -> {
			this.modList.searchFilter(() -> var2, false);
		});
		this.modList = new WidgetModList(this.game, this.width, this.height, 48, this.height - 64, 36, () -> this.searchBox.getText(), this.modList);

		this.addButton(new WidgetButton(0, this.width / 2 + 82, this.height - 28, 72, 20, I18n.translate("gui.cancel", new Object[0])) {
			public void onPressed(double var1, double var3) {
				game.openGui(previousGui);
			}
		});
		this.listeners.add(this.searchBox);
		this.listeners.add(this.modList);
		this.searchBox.method_1876(true);
		this.searchBox.method_1856(false);
	}

	public boolean keyPressed(int var1, int var2, int var3) {
		return super.keyPressed(var1, var2, var3) || this.searchBox.keyPressed(var1, var2, var3);
	}

	public boolean charTyped(char var1, int var2) {
		return this.searchBox.charTyped(var1, var2);
	}

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
