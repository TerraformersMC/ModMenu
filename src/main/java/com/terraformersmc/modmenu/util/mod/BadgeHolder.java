package com.terraformersmc.modmenu.util.mod;

import net.minecraft.resources.Identifier;

public record BadgeHolder(Identifier id, Mod.Badge badge) {

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof BadgeHolder holder && this.id == holder.id;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return this.id.toString();
    }
}
