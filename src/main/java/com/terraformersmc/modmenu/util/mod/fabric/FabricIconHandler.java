package com.terraformersmc.modmenu.util.mod.fabric;

import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FabricIconHandler implements Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | FabricIconHandler");

	private final Map<Path, NativeImageBackedTexture> modIconCache = new HashMap<>();

	public NativeImageBackedTexture createIcon(ModContainer iconSource, String iconPath) {
		try {
			Path path = iconSource.getPath(iconPath);
			NativeImageBackedTexture cachedIcon = getCachedModIcon(path);
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
				NativeImageBackedTexture tex = new NativeImageBackedTexture(image);
				cacheModIcon(path, tex);
				return tex;
			}

		} catch (IllegalStateException e) {
			if (e.getMessage().equals("Must be square icon")) {
				LOGGER.error("Mod icon must be a square for icon source {}: {}", iconSource.getMetadata().getId(), iconPath, e);
			}

			return null;
		} catch (Throwable t) {
			if (!iconPath.equals("assets/" + iconSource.getMetadata().getId() + "/icon.png")) {
				LOGGER.error("Invalid mod icon for icon source {}: {}", iconSource.getMetadata().getId(), iconPath, t);
			}
			return null;
		}
	}

	@Override
	public void close() {
		for (NativeImageBackedTexture tex : modIconCache.values()) {
			tex.close();
		}
	}

	NativeImageBackedTexture getCachedModIcon(Path path) {
		return modIconCache.get(path);
	}

	void cacheModIcon(Path path, NativeImageBackedTexture tex) {
		modIconCache.put(path, tex);
	}
}
