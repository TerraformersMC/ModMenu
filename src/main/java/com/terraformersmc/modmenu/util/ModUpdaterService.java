package com.terraformersmc.modmenu.util;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.DownloadableUpdateInfo;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ModUpdaterService {
    public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | Updater");
    private final ModsScreen screen;
    private final Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public ModUpdaterService(ModsScreen screen) {
        this.screen = screen;
    }

    public void performUpdateAll() {
        List<Mod> toUpdate = ModMenu.MODS.values().stream()
                .filter(mod -> mod.getUpdateInfo() instanceof DownloadableUpdateInfo && mod.hasUpdate() && !mod.isDownloadingUpdate() && !mod.isUpdateDownloaded())
                .toList();
        
        if (toUpdate.isEmpty()) {
            return;
        }

        toastSuccess(
            Text.translatable("modmenu.update.toast.all.started.title"), 
            Text.translatable("modmenu.update.toast.all.started.description", toUpdate.size())
        );

        for (Mod mod : toUpdate) {
            performUpdate(mod);
        }
    }
    
    public void performUpdate(Mod mod) {
        if (!(mod.getUpdateInfo() instanceof DownloadableUpdateInfo info) || !mod.hasUpdate() || mod.isDownloadingUpdate() || mod.isUpdateDownloaded()) {
            return;
        }

        mod.setDownloadingUpdate(true);

        CompletableFuture.runAsync(() -> {
            try {
                Optional<Path> oldFile = findModJar(mod);
                if (oldFile.isEmpty()) {
                    LOGGER.error("Could not find JAR for mod '{}' to schedule cleanup.", mod.getId());
                }

                Path tempFile = downloadFile(info);

                if (info.getFileHash() != null) {
                    String downloadedHash = Files.asByteSource(tempFile.toFile()).hash(Hashing.sha512()).toString();
                    if (!info.getFileHash().equalsIgnoreCase(downloadedHash)) {
                        throw new IOException("File hash mismatch for " + info.getFileName());
                    }
                }

                Path newFilePath = modsDir.resolve(info.getFileName());
                java.nio.file.Files.move(tempFile, newFilePath, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Successfully downloaded update for {}: {}", mod.getId(), newFilePath.getFileName());

                oldFile.ifPresent(CleanupManager::scheduleForCleanup);
                
                toastSuccess(Text.translatable("modmenu.update.toast.single.success.title"), Text.translatable("modmenu.update.toast.single.success.description", mod.getName()));

                mod.setUpdateDownloaded(true);
                
                String parentId = mod.getParent();
                if (parentId != null) {
                    Mod parentMod = ModMenu.MODS.get(parentId);
                    if (parentMod != null) {
                        boolean hasOtherChildUpdates = ModMenu.PARENT_MAP.get(parentMod)
                                .stream()
                                .anyMatch(child -> child.hasUpdate() && !child.isUpdateDownloaded());
                        
                        if (!hasOtherChildUpdates) {
                            parentMod.resetChildHasUpdate();
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to download update for mod '{}'", mod.getId(), e);
                toastError(Text.translatable("modmenu.update.toast.error.title"), Text.literal(mod.getName() + ": " + e.getLocalizedMessage()));
            } finally {
                mod.setDownloadingUpdate(false);
            }
        });
    }

    private Path downloadFile(DownloadableUpdateInfo info) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(info.getDownloadUrl())).GET().build();
        Path tempFile = java.nio.file.Files.createTempFile("modmenu-download", ".jar.tmp");
        
        HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("Download failed: " + response.statusCode());
        }

        try (InputStream is = response.body()) {
            java.nio.file.Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }
    
    private Optional<Path> findModJar(Mod mod) {
        if (mod instanceof com.terraformersmc.modmenu.util.mod.fabric.FabricMod fabricMod) {
             var container = fabricMod.getContainer();
             if (container.getOrigin().getKind() == net.fabricmc.loader.api.metadata.ModOrigin.Kind.PATH) {
                return container.getOrigin().getPaths().stream()
                    .filter(p -> p.toString().toLowerCase().endsWith(".jar") && java.nio.file.Files.exists(p))
                    .findFirst();
            }
        }
        return Optional.empty();
    }

    private void toastSuccess(Text title, Text description) {
        MinecraftClient.getInstance().execute(() -> {
            SystemToast.add(MinecraftClient.getInstance().getToastManager(), SystemToast.Type.PERIODIC_NOTIFICATION, title, description);
        });
    }
    
    private void toastError(Text title, Text description) {
        MinecraftClient.getInstance().execute(() -> {
            SystemToast.add(MinecraftClient.getInstance().getToastManager(), SystemToast.Type.PACK_COPY_FAILURE, title, description);
        });
    }
}
