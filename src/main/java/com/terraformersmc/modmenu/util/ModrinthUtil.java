package com.terraformersmc.modmenu.util;

import com.google.gson.JsonParser;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModrinthData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ModrinthUtil {
	public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu/Update Checker");

	private static final HttpClient client = HttpClient.newHttpClient();
	private static boolean apiV2Deprecated = false;

	public static @Nullable void checkForUpdates(Mod mod) {
		if (!mod.allowsUpdateChecks() || !ModMenuConfig.UPDATE_CHECKER.getValue() || apiV2Deprecated) {
			return;
		}
		Util.getMainWorkerExecutor().execute(() -> {
			try {
				var localHash = mod.getSha512Hash();
				if (localHash == null) {
					LOGGER.debug("Unable to check for updates of '{}@{}' without local hash.", mod.getId(), mod.getVersion());
					return;
				}
				var userAgent = "%s/%s".formatted(
						ModMenu.GITHUB_REF,
						FabricLoader.getInstance().getModContainer(ModMenu.MOD_ID)
								.get().getMetadata().getVersion().getFriendlyString());
				var versionReq = HttpRequest.newBuilder()
						.GET()
						.header("User-Agent", userAgent)
						.uri(URI.create("https://api.modrinth.com/v2/version_file/%s?algorithm=sha512".formatted(localHash)))
						.build();
				try {
					var versionRsp = client.send(versionReq, HttpResponse.BodyHandlers.ofString());
					if (versionRsp.statusCode() == 404) {
						LOGGER.debug("Unable to find a Modrinth version that matches local file hash of '{}@{}'", mod.getId(), mod.getVersion());
					} else if (versionRsp.statusCode() == 410) {
						apiV2Deprecated = true;
						LOGGER.warn("Modrinth API v2 is deprecated, unable to check for mod updates.");
					} else if (versionRsp.statusCode() == 200) {
						LOGGER.debug("Found matching version file hash on Modrinth for '{}@{}'.", mod.getId(), mod.getVersion());
						// https://docs.modrinth.com/api-spec/#tag/version-files/operation/versionFromHash
						var versionObj = JsonParser.parseString(versionRsp.body()).getAsJsonObject();
						var modrinthVersion = versionObj.get("version_number").getAsString();
						var projectId = versionObj.get("project_id").getAsString();

						var latestReq = HttpRequest.newBuilder()
								.GET()
								.header("User-Agent", userAgent)
								.uri(URI.create("https://api.modrinth.com/v2/project/%s/version?loaders=%s&game_versions=%s".formatted(
										projectId,
										URLEncoder.encode("[\"%s\"]".formatted(ModMenu.runningQuilt ? "quilt" : "fabric"), StandardCharsets.UTF_8),
										URLEncoder.encode("[\"%s\"]".formatted(SharedConstants.getGameVersion().getName()), StandardCharsets.UTF_8)
								)))
								.build();
						var latestRsp = client.send(latestReq, HttpResponse.BodyHandlers.ofString());
						if (latestRsp.statusCode() == 404) {
							// This probably won't happen since we check earlier but better safe than sorry.
							LOGGER.debug("Unable to find versions for '{}@{}'", mod.getId(), mod.getVersion());
						} else if (latestRsp.statusCode() == 200) {
							LOGGER.debug("Getting latest version from Modrinth.");
							var versions = JsonParser.parseString(latestRsp.body()).getAsJsonArray();
							String currentLoader = ModMenu.runningQuilt ? "Quilt" : "Fabric";
							LOGGER.debug("Versions of {} for {}: {}", mod.getId(), currentLoader, versions);
							if (versions.isEmpty()) {
								LOGGER.debug("No versions of {} for {} found on Modrinth.", mod.getId(), currentLoader);
							} else {
								var latestObj = versions.get(0).getAsJsonObject();
								var latestVersion = latestObj.get("version_number").getAsString();
								var latestId = latestObj.get("id").getAsString();
								var latestHash = latestObj.get("files").getAsJsonArray().asList()
										.stream().filter(file -> file.getAsJsonObject().get("primary").getAsBoolean()).findFirst()
										.get().getAsJsonObject().get("hashes").getAsJsonObject().get("sha512").getAsString();
								if (!Objects.equals(latestHash, localHash)) {
									// hashes different, there's an update.
									LOGGER.info("Update available for '{}@{}', ({} -> {})", mod.getId(), mod.getVersion(), modrinthVersion, latestVersion);
									mod.setModrinthData(new ModrinthData(projectId, latestId, latestVersion));
									ModMenu.modUpdateAvailable = true;
								}
							}
						}
					}
				} catch (IOException | InterruptedException e) {
					LOGGER.error("Error contacting Modrinth for {}@{}.", mod.getId(), mod.getVersion(), e);
				}
			} catch (IOException e) {
				LOGGER.warn("Failed sha512 hash for {}, skipping update check.", mod.getId(), e);
			}
		});
	}

	public static void triggerV2DeprecatedToast() {
		if (apiV2Deprecated && ModMenuConfig.UPDATE_CHECKER.getValue()) {
			MinecraftClient.getInstance().getToastManager().add(new SystemToast(
					SystemToast.Type.PERIODIC_NOTIFICATION,
					Text.translatable("modmenu.modrinth.v2_deprecated.title"),
					Text.translatable("modmenu.modrinth.v2_deprecated.description")
			));
		}
	}
}
