package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.util.mod.fabric.FabricIconHandler;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public interface Mod {
	@NotNull
	String getId();

	@NotNull
	String getName();

	@NotNull
	NativeImageBackedTexture getIcon(FabricIconHandler iconHandler, int i);

	@NotNull
	String getSummary();

	@NotNull
	String getDescription();

	@NotNull
	String getVersion();

	@NotNull
	String getPrefixedVersion();

	@NotNull
	List<String> getAuthors();

	@NotNull
	List<String> getContributors();

	@NotNull
	List<String> getCredits();

	@NotNull
	Set<Badge> getBadges();

	@Nullable
	String getWebsite();

	@Nullable
	String getIssueTracker();

	@Nullable
	String getSource();

	@Nullable
	String getParent();

	@NotNull
	Set<String> getLicense();

	@NotNull
	Map<String, String> getLinks();

	boolean isReal();

	enum Badge {
		LIBRARY("modmenu.badge.library", "modmenu.searchTerms.library", 0xff107454, 0xff093929, 0xff4ce6b5, "library"),
		CLIENT("modmenu.badge.clientsideOnly", "modmenu.searchTerms.clientside", 0xff2b4b7c, 0xff0e2a55, 0xff3484fe, null),
		DEPRECATED("modmenu.badge.deprecated", "modmenu.searchTerms.deprecated", 0xff841426, 0xff530C17, 0xffe44e66, "deprecated"),
		PATCHWORK_FORGE("modmenu.badge.forge", "modmenu.searchTerms.patchwork", 0xff1f2d42, 0xff101721, 0xff7a93b8, null),
		MODPACK("modmenu.badge.modpack", "modmenu.searchTerms.modpack", 0xff7a2b7c, 0xff510d54, 0xffc868ca, null),
		MINECRAFT("modmenu.badge.minecraft", null, 0xff6f6c6a, 0xff31302f, 0xff9b9997, null);

		private final Text text;
		private final int outlineColor, fillColor;
		private final TextColor searchColor;
		private final String key;
		private final String searchKey;
		private static final Map<String, Badge> KEY_MAP = new HashMap<>();

		Badge(String translationKey, String searchKey, int outlineColor, int fillColor, int searchColor, String key) {
			this.text = Text.translatable(translationKey);
			this.searchKey = searchKey;
			this.outlineColor = outlineColor;
			this.fillColor = fillColor;
			this.searchColor = TextColor.fromRgb(searchColor);
			this.key = key;
		}

		public Text getText() {
			return this.text;
		}

		public int getOutlineColor() {
			return this.outlineColor;
		}

		public int getFillColor() {
			return this.fillColor;
		}

		public TextColor getSearchColor() {
			return this.searchColor;
		}

		public String getSearchKey() {
			return this.searchKey;
		}

		public static Set<Badge> convert(Set<String> badgeKeys) {
			return badgeKeys.stream().map(KEY_MAP::get).collect(Collectors.toSet());
		}

		static {
			Arrays.stream(values()).forEach(badge -> KEY_MAP.put(badge.key, badge));
		}
	}
}
