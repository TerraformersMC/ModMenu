package io.github.prospector.modmenu.config;

import com.google.gson.annotations.SerializedName;
import io.github.prospector.modmenu.util.HardcodedUtil;
import net.fabricmc.loader.api.ModContainer;

import java.util.Comparator;

public class ModMenuConfig {
	private boolean showLibraries = false;
	private boolean hideConfigButtons = false;
	private Sorting sorting = Sorting.ASCENDING;

	public void toggleShowLibraries() {
		this.showLibraries = !this.showLibraries;
		ModMenuConfigManager.save();
	}

	public void toggleSortMode() {
		this.sorting = Sorting.values()[(sorting.ordinal() + 1) % Sorting.values().length];
		ModMenuConfigManager.save();
	}

	public boolean showLibraries() {
		return showLibraries;
	}

	public boolean isHidingConfigurationButtons() {
		return hideConfigButtons;
	}

	public Sorting getSorting() {
		return sorting == null ? Sorting.ASCENDING : sorting;
	}

	public enum Sorting {
		@SerializedName("ascending")
		ASCENDING(Comparator.comparing(modContainer -> HardcodedUtil.formatFabricModuleName(modContainer.getMetadata().getName()).asString()), "modmenu.sorting.ascending"),
		@SerializedName("descending")
		DESCENDING(ASCENDING.getComparator().reversed(), "modmenu.sorting.decending");

		Comparator<ModContainer> comparator;
		String translationKey;

		Sorting(Comparator<ModContainer> comparator, String translationKey) {
			this.comparator = comparator;
			this.translationKey = translationKey;
		}

		public Comparator<ModContainer> getComparator() {
			return comparator;
		}

		public String getTranslationKey() {
			return translationKey;
		}
	}
}
