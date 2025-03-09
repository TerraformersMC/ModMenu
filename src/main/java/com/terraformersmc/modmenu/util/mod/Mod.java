package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.TextPlaceholderApiCompat;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.fabric.FabricIconHandler;
import eu.pb4.placeholders.api.ParserContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public interface Mod {
	@NotNull String getId();

	@NotNull String getName();

	@NotNull
	default String getTranslatedName() {
		String translationKey = "modmenu.nameTranslation." + getId();
		if ((getId().equals("minecraft") || getId().equals("java") || ModMenuConfig.TRANSLATE_NAMES.getValue()) && I18n.hasTranslation(
			translationKey)) {
			return I18n.translate(translationKey);
		} else {
			return getName();
		}
	}

	@NotNull NativeImageBackedTexture getIcon(FabricIconHandler iconHandler, int i);

	@NotNull
	default String getSummary() {
		String string = getTranslatedSummary();
		return ModMenu.TEXT_PLACEHOLDER_COMPAT ?
			TextPlaceholderApiCompat.PARSER.parseText(string, ParserContext.of()).getString() :
			string;
	}

	@NotNull
	default String getTranslatedSummary() {
		String translationKey = "modmenu.summaryTranslation." + getId();
		if ((getId().equals("minecraft") || getId().equals("java") || ModMenuConfig.TRANSLATE_DESCRIPTIONS.getValue()) && I18n.hasTranslation(
			translationKey)) {
			return I18n.translate(translationKey);
		} else {
			return getTranslatedDescription();
		}
	}

	@NotNull String getDescription();

	@NotNull
	default String getTranslatedDescription() {
		String translatableDescriptionKey = "modmenu.descriptionTranslation." + getId();
		if ((getId().equals("minecraft") || getId().equals("java") || ModMenuConfig.TRANSLATE_DESCRIPTIONS.getValue()) && I18n.hasTranslation(
			translatableDescriptionKey)) {
			return I18n.translate(translatableDescriptionKey);
		} else {
			return getDescription();
		}
	}

	default Text getFormattedDescription() {
		String string = getTranslatedDescription();
		return ModMenu.TEXT_PLACEHOLDER_COMPAT ?
			TextPlaceholderApiCompat.PARSER.parseText(string, ParserContext.of()) :
			Text.literal(string);
	}

	@NotNull String getVersion();

	@NotNull String getPrefixedVersion();

	@NotNull List<String> getAuthors();

	/**
	 * @return a mapping of contributors to their roles.
	 */
	@NotNull Map<String, Collection<String>> getContributors();

	/**
	 * @return a mapping of roles to each contributor with that role.
	 */
	@NotNull SortedMap<String, Set<String>> getCredits();

	@NotNull Set<Badge> getBadges();

	@Nullable String getWebsite();

	@Nullable String getIssueTracker();

	@Nullable String getSource();

	@Nullable String getParent();

	@NotNull Set<String> getLicense();

	@NotNull Map<String, String> getLinks();

	boolean isReal();

	boolean allowsUpdateChecks();

	@Nullable UpdateChecker getUpdateChecker();

	void setUpdateChecker(@Nullable UpdateChecker updateChecker);

	@Nullable UpdateInfo getUpdateInfo();

	void setUpdateInfo(@Nullable UpdateInfo updateInfo);

	default boolean hasUpdate() {
		UpdateInfo updateInfo = this.getUpdateInfo();
		if (updateInfo == null) {
			return false;
		} else {
			return updateInfo.isUpdateAvailable() && updateInfo.getUpdateChannel().compareTo(ModMenuConfig.UPDATE_CHANNEL.getValue()) >= 0;
		}
	}

	default @Nullable String getSha512Hash() throws IOException {
		return null;
	}

	void setChildHasUpdate();

	boolean getChildHasUpdate();

	boolean isHidden();

	enum Badge {
		LIBRARY(
			"modmenu.badge.library",
			0xff107454,
			0xff093929,
			"library"
		),
		CLIENT(
			"modmenu.badge.clientsideOnly",
			0xff2b4b7c,
			0xff0e2a55,
			null
		),
		DEPRECATED(
			"modmenu.badge.deprecated",
			0xff841426,
			0xff530C17,
			"deprecated"
		),
		PATCHWORK_FORGE(
			"modmenu.badge.forge",
			0xff1f2d42,
			0xff101721,
			null
		),
		MODPACK(
			"modmenu.badge.modpack",
			0xff7a2b7c,
			0xff510d54,
			null
		),
		MINECRAFT(
			"modmenu.badge.minecraft",
			0xff6f6c6a,
			0xff31302f,
			null
		);

		private final Text text;
		private final int outlineColor, fillColor;
		private final String key;
		private static final Map<String, Badge> KEY_MAP = new HashMap<>();

		Badge(String translationKey, int outlineColor, int fillColor, String key) {
			this.text = Text.translatable(translationKey);
			this.outlineColor = outlineColor;
			this.fillColor = fillColor;
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

		public static Set<Badge> convert(Set<String> badgeKeys, String modId) {
			return badgeKeys.stream().map(key -> {
				if (!KEY_MAP.containsKey(key)) {
					ModMenu.LOGGER.warn("Skipping unknown badge key '{}' specified by mod '{}'", key, modId);
				}

				return KEY_MAP.get(key);
			}).filter(Objects::nonNull).collect(Collectors.toSet());
		}

		static {
			Arrays.stream(values()).forEach(badge -> KEY_MAP.put(badge.key, badge));
		}
	}
}
