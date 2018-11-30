package prospector.modmenu.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.widget.ButtonWidget;

public class ModMenuButtonWidget extends ButtonWidget {
	public Gui gui;

	public ModMenuButtonWidget(int i, int i1, int i2, String s, Gui gui) {
		super(i, i1, i2, s);
		this.gui = gui;
	}

	public ModMenuButtonWidget(int i, int i1, int i2, int i3, int i4, String s, Gui gui) {
		super(i, i1, i2, i3, i4, s);
		this.gui = gui;
	}

	@Override
	public void onPressed(double var1, double var3) {
		MinecraftClient.getInstance().openGui(new GuiModList(gui));
	}
}
