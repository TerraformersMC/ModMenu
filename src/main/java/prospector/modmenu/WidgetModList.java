package prospector.modmenu;

import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.ModInfo;
import net.minecraft.client.MinecraftGame;
import net.minecraft.client.gui.widget.WidgetListMulti;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import prospector.modmenu.util.RenderUtils;

import java.util.*;
import java.util.function.Supplier;

public class WidgetModList extends WidgetListMulti {
	private static final Logger LOGGER = LogManager.getLogger();

	private List<ModContainer> modInfoList = null;
	public ModContainer selected;

	public WidgetModList(MinecraftGame game,
	                     int width,
	                     int height,
	                     int y1,
	                     int y2,
	                     int entryHeight,
	                     Supplier<String> searchTerm, WidgetModList list) {
		super(game, width, height, y1, y2, entryHeight);
		if (list != null) {
			this.modInfoList = list.modInfoList;
		}
		this.selected = null;
		this.searchFilter(searchTerm, false);
	}

	@Override
	public void draw(int i, int i1, float v) {
		super.draw(i, i1, v);
		if (selected != null) {
			ModInfo info = selected.getInfo();
			int x = this.width / 2 - 154;
			int y = y2 + 8;
			this.game.fontRenderer.drawWithShadow(info.getName(), x, y, 0xFFFFFF);
			this.game.fontRenderer.drawWithShadow(" (Mod ID: " + info.getId() + ")", x + game.fontRenderer.method_1727(info.getName()), y, 0xAAAAAA);
			RenderUtils.drawWrappedString(info.getDescription(), x + 4, y + 10, 308, 5, 0x808080);
		}
	}

	@Override
	public int getEntryWidth() {
		return super.getEntryWidth() + 50;
	}

	public void searchFilter(Supplier<String> searchTerm, boolean var2) {
		this.method_1902();
		List<ModContainer> mods = FabricLoader.INSTANCE.getMods();
		if (this.modInfoList == null || var2) {
			this.modInfoList = new ArrayList<>();
			for (ModContainer modContainer : mods) {
				modInfoList.add(modContainer);
			}
			this.modInfoList.sort(Comparator.comparing(modContainer -> modContainer.getInfo().getName()));
		}

		String term = searchTerm.get().toLowerCase(Locale.ROOT);
		Iterator<ModContainer> iter = this.modInfoList.iterator();

		while (true) {
			ModContainer container;
			ModInfo info;
			do {
				if (!iter.hasNext()) {
					return;
				}
				container = iter.next();
				info = container.getInfo();
			} while (!info.getName().toLowerCase(Locale.ROOT).contains(term) && !info.getId().toLowerCase(Locale.ROOT).contains(term) && !info.getAuthors().stream().anyMatch(person -> person.getName().equalsIgnoreCase(term)));

			this.method_1901(new WidgetModEntry(container, this));
		}
	}
}
