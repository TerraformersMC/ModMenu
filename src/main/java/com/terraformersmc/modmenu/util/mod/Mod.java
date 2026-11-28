package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.TextPlaceholderApiCompat;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.fabric.FabricIconHandler;
import eu.pb4.placeholders.api.ParserContext;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.minecraft.locale.Language;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public interface Mod {
    String getId();

    String getName();

    default String getTranslatedName() {
        String translationKey = "modmenu.nameTranslation." + getId();
        if ((getId().equals("minecraft") || getId().equals("java") || ModMenuConfig.TRANSLATE_NAMES.getValue()) &&
                Language.getInstance().has(translationKey)) {
            return I18n.get(translationKey);
        } else {
            return getName();
        }
    }

    DynamicTexture getIcon(FabricIconHandler iconHandler, int i);

    default String getSummary() {
        String string = getTranslatedSummary();
        return ModMenu.TEXT_PLACEHOLDER_COMPAT ?
                TextPlaceholderApiCompat.PARSER.parseComponent(string, ParserContext.of()).getString() :
                string;
    }

    default String getTranslatedSummary() {
        String translationKey = "modmenu.summaryTranslation." + getId();
        if ((getId().equals("minecraft") || getId().equals("java") || ModMenuConfig.TRANSLATE_DESCRIPTIONS.getValue()) &&
                Language.getInstance().has(translationKey)) {
            return I18n.get(translationKey);
        } else {
            return getTranslatedDescription();
        }
    }

    String getDescription();

    default String getTranslatedDescription() {
        String translatableDescriptionKey = "modmenu.descriptionTranslation." + getId();
        if ((getId().equals("minecraft") || getId().equals("java") || ModMenuConfig.TRANSLATE_DESCRIPTIONS.getValue()) &&
                Language.getInstance().has(translatableDescriptionKey)) {
            return I18n.get(translatableDescriptionKey);
        } else {
            return getDescription();
        }
    }

    default Component getFormattedDescription() {
        String string = getTranslatedDescription();
        return ModMenu.TEXT_PLACEHOLDER_COMPAT ?
                TextPlaceholderApiCompat.PARSER.parseComponent(string, ParserContext.of()) :
                Component.literal(string);
    }

    String getVersion();

    String getPrefixedVersion();

    List<String> getAuthors();

    @Nullable ContactInformation getContact(String author);

    /**
     * @return a mapping of contributors to their roles.
     */
    Map<String, Collection<String>> getContributors();

    /**
     * @return a mapping of roles to each contributor with that role.
     */
    SortedMap<String, Set<String>> getCredits();

    Set<Badge> getBadges();

    @Nullable String getWebsite();

    @Nullable String getIssueTracker();

    @Nullable String getSource();

    @Nullable String getParent();

    Set<String> getLicense();

    Map<String, String> getLinks();

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
                0xFF107454,
                0xFF093929,
                "library"
        ),
        CLIENT(
                "modmenu.badge.clientsideOnly",
                0xFF2b4b7c,
                0xFF0e2a55,
                null
        ),
        DEPRECATED(
                "modmenu.badge.deprecated",
                0xFF841426,
                0xFF530C17,
                "deprecated"
        ),
        PATCHWORK_FORGE(
                "modmenu.badge.forge",
                0xFF1f2d42,
                0xFF101721,
                null
        ),
        MODPACK(
                "modmenu.badge.modpack",
                0xFF7a2b7c,
                0xFF510d54,
                null
        ),
        MINECRAFT(
                "modmenu.badge.minecraft",
                0xFF6f6c6a,
                0xFF31302f,
                null
        );

        private final Component text;
        private final int outlineColor, fillColor;
        private final @Nullable String key;
        private static final Map<String, Badge> KEY_MAP = new HashMap<>();

        Badge(String translationKey, int outlineColor, int fillColor, @Nullable String key) {
            this.text = Component.translatable(translationKey);
            this.outlineColor = outlineColor;
            this.fillColor = fillColor;
            this.key = key;
        }

        public Component getText() {
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
            Arrays.stream(values()).filter(badge -> badge.key != null).forEach(badge -> KEY_MAP.put(badge.key, badge));
        }
    }
}
