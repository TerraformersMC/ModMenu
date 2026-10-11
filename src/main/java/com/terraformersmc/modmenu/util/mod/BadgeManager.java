package com.terraformersmc.modmenu.util.mod;

import com.mojang.logging.LogUtils;
import com.terraformersmc.modmenu.ModMenu;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;
import java.util.stream.Collectors;

public class BadgeManager extends SimpleJsonResourceReloadListener<Mod.Badge> {
    private static final FileToIdConverter ASSET_LISTER = FileToIdConverter.json("modmenu/badge");
    public static final BadgeManager INSTANCE = new BadgeManager();
    private Map<Identifier, BadgeHolder> badges = Map.of();

    protected BadgeManager() {
        super(Mod.Badge.CODEC, ASSET_LISTER);
    }

    @Override
    protected void apply(Map<Identifier, Mod.Badge> preparations, ResourceManager manager, ProfilerFiller profiler) {
        badges = preparations.entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> new BadgeHolder(entry.getKey(), entry.getValue())));

        ModMenu.MODS.values().forEach(Mod::recalculateBadges);
    }

    public static BadgeHolder getBadge(Identifier id) {
        return INSTANCE.badges.get(id);
    }
}
