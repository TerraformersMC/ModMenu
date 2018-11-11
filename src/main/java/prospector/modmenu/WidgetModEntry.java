package prospector.modmenu;

import net.fabricmc.loader.ModInfo;
import net.minecraft.client.MinecraftGame;
import net.minecraft.client.gui.widget.WidgetListMulti;

public class WidgetModEntry extends WidgetListMulti.class_351 {
	private final MinecraftGame game;
	public ModInfo info;

	public WidgetModEntry(ModInfo info) {
		this.info = info;
		this.game = MinecraftGame.getInstance();
	}

	@Override
	public void drawEntry(int i, int i1, int i2, int i3, boolean b, float v) {
		int var7 = this.method_1906();
		int var8 = this.method_1907();
		this.game.fontRenderer.drawWithShadow(info.getName(), (float) (var8 + 32 + 3), (float) (var7 + 1), 0xFFFFFF);
	}
}
