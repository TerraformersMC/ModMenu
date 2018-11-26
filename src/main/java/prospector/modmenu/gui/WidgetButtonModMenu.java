package prospector.modmenu.gui;

import net.minecraft.client.MinecraftGame;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.widget.WidgetButton;

public class WidgetButtonModMenu extends WidgetButton {
	public Gui gui;

	public WidgetButtonModMenu(int i, int i1, int i2, String s, Gui gui) {
		super(i, i1, i2, s);
		this.gui = gui;
	}

	public WidgetButtonModMenu(int i, int i1, int i2, int i3, int i4, String s, Gui gui) {
		super(i, i1, i2, i3, i4, s);
		this.gui = gui;
	}

	public void onPressed(double var1, double var3) {
		MinecraftGame.getInstance().openGui(new GuiModList(gui));
	}
}
