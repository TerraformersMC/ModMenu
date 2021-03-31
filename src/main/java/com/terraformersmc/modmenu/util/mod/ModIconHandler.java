package com.terraformersmc.modmenu.util.mod;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ModIconHandler {
	private static final Logger LOGGER = LogManager.getLogger("Mod Menu | ModIconHandler");

	private final Map<Path, DynamicTexture> modIconCache = new HashMap<>();

	public DynamicTexture createIcon(ModContainer iconSource, String iconPath) {
		try {
			Path path = iconSource.getPath(iconPath);
			DynamicTexture cachedIcon = getCachedModIcon(path);
			if (cachedIcon != null) {
				return cachedIcon;
			}
			cachedIcon = getCachedModIcon(path);
			if (cachedIcon != null) {
				return cachedIcon;
			}
			try (InputStream inputStream = Files.newInputStream(path)) {
				NativeImage image = NativeImage.read(Objects.requireNonNull(inputStream));
				Validate.validState(image.getHeight() == image.getWidth(), "Must be square icon");
				DynamicTexture tex = new DynamicTexture(image);
				cacheModIcon(path, tex);
				return tex;
			}

		} catch (Throwable t) {
			if (!iconPath.equals("assets/" + iconSource.getMetadata().getId() + "/icon.png")) {
				LOGGER.error("Invalid mod icon for icon source {}: {}", iconSource.getMetadata().getId(), iconPath, t);
			}
			return null;
		}
	}

	public void close() {
		for (DynamicTexture tex : modIconCache.values()) {
			tex.close();
		}
	}

	DynamicTexture getCachedModIcon(Path path) {
		return modIconCache.get(path);
	}

	void cacheModIcon(Path path, DynamicTexture tex) {
		modIconCache.put(path, tex);
	}
}
