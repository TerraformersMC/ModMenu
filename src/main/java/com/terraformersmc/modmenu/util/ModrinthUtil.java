package com.terraformersmc.modmenu.util;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.JsonParser;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.ModrinthData;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.QuiltLoader;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class ModrinthUtil {
	private static final HttpClient client = HttpClient.newHttpClient();
	private static boolean apiV2Deprecated = false;

	public static @Nullable ModrinthData getLatestModVersion(String modId) {
		if (apiV2Deprecated) {
			return null;
		}
		var container = FabricLoader.getInstance().getModContainer(modId);
		if (container.isPresent()) {
			var mod = container.get();
			try {
				var localHash = getSha512Hash(mod);
				if(localHash == null) {
					return null; // unable to check version without local hash
				}
				var userAgent = "TerraformersMC/ModMenu/%s".formatted(
						FabricLoader.getInstance().getModContainer("modmenu")
								.get().getMetadata().getVersion().getFriendlyString());
				var versionReq = HttpRequest.newBuilder()
						.GET()
						.header("User-Agent", userAgent)
						.uri(URI.create("https://api.modrinth.com/v2/version_file/%s?algorithm=sha512".formatted(localHash)))
						.build();
				try {
					var versionRsp = client.send(versionReq, HttpResponse.BodyHandlers.ofString());
					if (versionRsp.statusCode() == 404) {
						return null; // Unable to fine a modrinth version that matches our file hash.
					} else if (versionRsp.statusCode() == 410) {
						apiV2Deprecated = true;
						ModMenu.LOGGER.warn("Modrinth API v2 is deprecated, unable to check for mod updates.");
						return null;
					} else if (versionRsp.statusCode() == 200) {
						ModMenu.LOGGER.info("Found matching version file hash on Modrinth.");
						// https://docs.modrinth.com/api-spec/#tag/version-files/operation/versionFromHash
						var versionObj = JsonParser.parseString(versionRsp.body()).getAsJsonObject();
						var modrinthVersion = versionObj.get("version_number").getAsString();
						var projectId = versionObj.get("project_id").getAsString();

						var latestReq = HttpRequest.newBuilder()
								.GET()
								.header("User-Agent", userAgent)
								.uri(URI.create("https://api.modrinth.com/v2/project/%s/version?loaders=%s&game_versions=%s".formatted(
										projectId,
										URLEncoder.encode("[\"%s\"]".formatted(FabricLoader.getInstance().isModLoaded("quilt_loader") ? "quilt" : "fabric"), StandardCharsets.UTF_8),
										URLEncoder.encode("[\"%s\"]".formatted(SharedConstants.getGameVersion().getName()), StandardCharsets.UTF_8)
								)))
								.build();
						var latestRsp = client.send(latestReq, HttpResponse.BodyHandlers.ofString());
						if(latestRsp.statusCode() == 404) {
							return null; // unable to find versions for mod id, this probably won't happen since we check earlier but better safe than sorry
						} else if (latestRsp.statusCode() == 200) {
							ModMenu.LOGGER.info("Getting latest version from Modrinth.");
							var versions = JsonParser.parseString(latestRsp.body()).getAsJsonArray();
							var latestObj = versions.get(0).getAsJsonObject();
							var latestVersion = latestObj.get("version_name").getAsString();
							var latestId = latestObj.get("id").getAsString();
							var latestHash = latestObj.get("files").getAsJsonArray().asList()
									.stream().filter(file -> file.getAsJsonObject().get("primary").getAsBoolean()).findFirst()
									.get().getAsJsonObject().get("hashes").getAsJsonObject().get("sha512").getAsString();
							if(latestHash != localHash) {
								// hashes different, there's an update.
								ModMenu.LOGGER.info("Update available for {}, ({} -> {})", modId, modrinthVersion, latestVersion);
								return new ModrinthData(projectId, latestVersion, latestId);
							}
						}
					}
				} catch (IOException | InterruptedException e) {
					ModMenu.LOGGER.error("Error contacted modrinth for {}.", modId, e);
				}
			} catch (IOException e) {
				ModMenu.LOGGER.warn("Failed sha512 hash for {}, skipping update check.", modId, e);
			}
		}

		return null;
	}

	private static @Nullable String getSha512Hash(ModContainer mod) throws IOException {
		ModMenu.LOGGER.info(mod.getRoot().toString());
		if (mod.getOrigin().getKind() == ModOrigin.Kind.PATH) {
			ModMenu.LOGGER.info("Fetching mod data for {}.", mod.getMetadata().getId());

			var fileOptional = mod.getOrigin().getPaths().stream().filter(path -> path.endsWith(".jar")).findFirst();
			if (fileOptional.isPresent()) {
				var file = fileOptional.get().toFile();
				if (file.isFile()) {
					return Files.asByteSource(file).hash(Hashing.sha512()).toString();
				}
			}
		} else if (QuiltLoader.isModLoaded(mod.getMetadata().getId())) {
			var quiltMod = QuiltLoader.getModContainer(mod.getMetadata().getId()).get();
			if(quiltMod.getSourceType().equals(org.quiltmc.loader.api.ModContainer.BasicSourceType.NORMAL_QUILT)) {
				var path = quiltMod.getSourcePaths().stream()
						.filter(p -> p.stream().anyMatch(p2 -> p2.endsWith(".jar"))).findFirst().orElse(Collections.emptyList())
						.stream().filter(p -> p.endsWith(".jar")).findFirst();
				if(path.isPresent()) {
					var file = path.get().toFile();
					if(file.isFile()) {
						return Files.asByteSource(file).hash(Hashing.sha512()).toString();
					}
				}
			}
		}
		return null;
	}

	public static void triggerV2DeprecatedToast() {
		if(apiV2Deprecated && ModMenuConfig.UPDATE_CHECKER.getValue()) {
			MinecraftClient.getInstance().getToastManager().add(new SystemToast(
					SystemToast.Type.PERIODIC_NOTIFICATION,
					Text.translatable("modmenu.modrinth.v2_deprecated.title"),
					Text.translatable("modmenu.modrinth.v2_deprecated.description")
			));
		}
	}
}
