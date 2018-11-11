package prospector.modmenu;

import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.ModInfo;
import net.minecraft.client.MinecraftGame;
import net.minecraft.client.gui.widget.WidgetListMulti;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;

public class WidgetModList extends WidgetListMulti {
	private static final Logger LOGGER = LogManager.getLogger();

	private List<ModInfo> modInfoList = null;

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

		this.searchFilter(searchTerm, false);
	}

	public void searchFilter(Supplier<String> searchTerm, boolean var2) {
		this.method_1902();
		List<ModContainer> mods = FabricLoader.INSTANCE.getMods();
		if (this.modInfoList == null || var2) {
			this.modInfoList = new ArrayList<>();
			for (ModContainer modContainer : mods) {
				modInfoList.add(modContainer.getInfo());
			}
			this.modInfoList.sort(Comparator.comparing(ModInfo::getName));
		}

		String term = searchTerm.get().toLowerCase(Locale.ROOT);
		Iterator<ModInfo> iter = this.modInfoList.iterator();

		while (true) {
			ModInfo info;
			do {
				if (!iter.hasNext()) {
					return;
				}

				info = iter.next();
			} while (!info.getName().toLowerCase(Locale.ROOT).contains(term) && !info.getId().toLowerCase(Locale.ROOT).contains(term) && !info.getAuthors().stream().anyMatch(person -> person.getName().equalsIgnoreCase(term)));

			this.method_1901(new WidgetModEntry(info));
		}
	}
}
