package com.terraformersmc.modmenu.util.mod.fabric;

import com.mojang.blaze3d.platform.NativeImage;
import com.terraformersmc.modmenu.ModMenu;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureResources;
import net.minecraft.resources.Identifier;
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

    private final Map<Path, Identifier> modIconCache = new HashMap<>();

    public Identifier createIcon(ModContainer iconSource, String iconPath) {
        try {
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            Path path = iconSource.getPath(iconPath);
            Identifier cachedIcon = getCachedModIcon(path);

            if (cachedIcon != null) {
                return cachedIcon;
            }

            try (InputStream inputStream = Files.newInputStream(path)) {
                NativeImage image = NativeImage.read(Objects.requireNonNull(inputStream));
                Validate.validState(image.getHeight() == image.getWidth(), "Must be square icon");
                Identifier location = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, iconPath);
                textureManager.register(location, TextureResources.from2dImage(() -> "Mod icon " + location, image));
                cacheModIcon(path, location);

                return location;
            }
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("Must be square icon")) {
                LOGGER.error("Mod icon must be a square for icon source {}: {}",
                        iconSource.getMetadata().getId(),
                        iconPath
                );
            }

            return null;
        } catch (Throwable t) {
            if (!iconPath.equals("assets/" + iconSource.getMetadata().getId() + "/icon.png")) {
                LOGGER.error("Invalid mod icon for icon source {}: {}", iconSource.getMetadata().getId(), iconPath);
            }

            return null;
        }
    }

    @Override
    public void close() {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (Identifier tex : modIconCache.values()) {
            textureManager.release(tex);
        }
    }

    Identifier getCachedModIcon(Path path) {
        return modIconCache.get(path);
    }

    void cacheModIcon(Path path, Identifier tex) {
        modIconCache.put(path, tex);
    }
}
