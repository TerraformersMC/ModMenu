package com.terraformersmc.modmenu.updates;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AvailableUpdate(@NotNull String version, @Nullable String url,
							  @Nullable String changelog, @NotNull String provider) {
}
