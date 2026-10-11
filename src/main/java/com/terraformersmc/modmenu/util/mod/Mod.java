package com.terraformersmc.modmenu.util.mod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.TextPlaceholderApiCompat;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.fabric.FabricIconHandler;
import eu.pb4.placeholders.api.ParserContext;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public interface Mod {
    @NotNull String getId();

    @NotNull String getName();

    @NotNull
    default String getTranslatedName() {
        String translationKey = "modmenu.nameTranslation." + getId();
        if ((getId().equals("minecraft") || getId().equals("java") || ModMenuConfig.TRANSLATE_NAMES.getValue()) &&
                Language.getInstance().has(translationKey)) {
            return I18n.get(translationKey);
        } else {
            return getName();
        }
    }

    @NotNull DynamicTexture getIcon(FabricIconHandler iconHandler, int i);

    @NotNull
    default String getSummary() {
        String string = getTranslatedSummary();
        return ModMenu.TEXT_PLACEHOLDER_COMPAT ?
                TextPlaceholderApiCompat.PARSER.parseComponent(string, ParserContext.of()).getString() :
                string;
    }

    @NotNull
    default String getTranslatedSummary() {
        String translationKey = "modmenu.summaryTranslation." + getId();
        if ((getId().equals("minecraft") || getId().equals("java") || ModMenuConfig.TRANSLATE_DESCRIPTIONS.getValue()) &&
                Language.getInstance().has(translationKey)) {
            return I18n.get(translationKey);
        } else {
            return getTranslatedDescription();
        }
    }

    @NotNull String getDescription();

    @NotNull
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

    @NotNull String getVersion();

    @NotNull String getPrefixedVersion();

    @NotNull List<String> getAuthors();

    ContactInformation getContact(String author);

    /**
     * @return a mapping of contributors to their roles.
     */
    @NotNull Map<String, Collection<String>> getContributors();

    /**
     * @return a mapping of roles to each contributor with that role.
     */
    @NotNull SortedMap<String, Set<String>> getCredits();

    @NotNull Set<BadgeHolder> getBadges();

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

    void recalculateBadges();

    record Badge(Component text, int outlineColor, int fillColor, int textColor, Optional<String> searchTerms, boolean library) {

        public static final Identifier LIBRARY = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, "library");
        public static final Identifier CLIENT = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, "clientside_only");
        public static final Identifier DEPRECATED = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, "deprecated");
        public static final Identifier PATCHWORK_FORGE = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, "patchwork_forge");
        public static final Identifier MODPACK = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, "modpack");
        public static final Identifier MINECRAFT = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, "minecraft");

        public static final Codec<Badge> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("text").forGetter(Badge::getText),
                        ExtraCodecs.STRING_ARGB_COLOR.fieldOf("outline_color").forGetter(Badge::outlineColor),
                        ExtraCodecs.STRING_ARGB_COLOR.fieldOf("fill_color").forGetter(Badge::fillColor),
                        ExtraCodecs.STRING_ARGB_COLOR.optionalFieldOf("text_color", 0xFFCACACA).forGetter(Badge::textColor),
                        Codec.STRING.optionalFieldOf("search_terms").forGetter(Badge::searchTerms),
                        Codec.BOOL.optionalFieldOf("library", false).forGetter(Badge::library)
                ).apply(instance, Badge::new));

        public Component getText() {
            return this.text;
        }

        public int getOutlineColor() {
            return this.outlineColor;
        }

        public int getFillColor() {
            return this.fillColor;
        }

        public static Set<BadgeHolder> convert(Set<Identifier> badgeKeys, String modId) {
            return badgeKeys.stream().map(key -> {
                BadgeHolder badgeHolder = BadgeManager.getBadge(key);
                if (badgeHolder == null) {
                    ModMenu.LOGGER.warn("Skipping unknown badge key '{}' specified by mod '{}'", key, modId);
                }

                return badgeHolder;
            }).filter(Objects::nonNull).collect(Collectors.toSet());
        }
    }
}
