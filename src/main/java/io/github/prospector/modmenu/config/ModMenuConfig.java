package io.github.prospector.modmenu.config;

import io.github.prospector.modmenu.util.HardcodedUtil;
import net.fabricmc.loader.api.ModContainer;

import java.util.Comparator;

public class ModMenuConfig {
	private boolean showLibraries = false;
	private TitleScreenLayout titleScreenLayout = TitleScreenLayout.CLASSIC;
	private Sorting sorting = Sorting.ASCENDING;

	public void toggleShowLibraries() {
		this.showLibraries = !this.showLibraries;
		ModMenuConfigManager.save();
	}

	public void toggleSortMode() {
		this.sorting = Sorting.values()[(sorting.ordinal() + 1) % Sorting.values().length];
		ModMenuConfigManager.save();
	}

	public void toggleTitleScreenLayout() {
		this.titleScreenLayout = TitleScreenLayout.values()[(titleScreenLayout.ordinal() + 1) % TitleScreenLayout.values().length];
		ModMenuConfigManager.save();
	}

	public boolean showLibraries() {
		return showLibraries;
	}

	public Sorting getSorting() {
		return sorting;
	}

	public TitleScreenLayout getTitleScreenLayout() {
		return titleScreenLayout;
	}

	public enum Sorting {
		ASCENDING(Comparator.comparing(modContainer -> HardcodedUtil.formatFabricModuleName(modContainer.getMetadata().getName()).asString()), "modmenu.sorting.ascending"),
		DECENDING(ASCENDING.getComparator().reversed(), "modmenu.sorting.decending");

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

	public enum TitleScreenLayout {
		CLASSIC(false),
		HIDE_REALMS(false),
		SHRINK_REALMS(true),
		SIMPLE(true);

		boolean shrinkRealms;

		TitleScreenLayout(boolean shrinkRealms) {
			this.shrinkRealms = shrinkRealms;
		}
	}
}
