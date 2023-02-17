package com.terraformersmc.modmenu.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class ModrinthUtil {
	public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu/Update Checker");

	private static final HttpClient client = HttpClient.newHttpClient();
	private static boolean apiV2Deprecated = false;

	public static void checkForUpdates() {
		Util.getMainWorkerExecutor().execute(() -> {
			LOGGER.info("Checking mod updates...");
			Map<String, Set<Mod>> HASH_TO_MOD = new HashMap<>();
			new ArrayList<>(ModMenu.MODS.values()).stream().filter(mod -> mod.allowsUpdateChecks() &&
							ModMenuConfig.UPDATE_CHECKER.getValue() &&
							!ModMenuConfig.DISABLE_UPDATE_CHECKER.getValue().contains(mod.getId()) &&
							!apiV2Deprecated)
					.forEach(mod -> {
						try {
							String hash = mod.getSha512Hash();
							if (hash != null) {
								LOGGER.debug("Hash for {} is {}", mod.getId(), hash);
								HASH_TO_MOD.putIfAbsent(hash, new HashSet<>());
								HASH_TO_MOD.get(hash).add(mod);
							}
						} catch (IOException e) {
							LOGGER.error("Error checking for updates: ", e);
						}
					});
			final var userAgent = "%s/%s".formatted(
					ModMenu.GITHUB_REF,
					FabricLoader.getInstance().getModContainer(ModMenu.MOD_ID)
							.get().getMetadata().getVersion().getFriendlyString());
			String body = ModMenu.GSON_MINIFIED.toJson(new LatestVersionsFromHashesBody(HASH_TO_MOD.keySet(), ModMenu.runningQuilt ? "quilt" : "fabric", SharedConstants.getGameVersion().getName()));
			LOGGER.debug("Body: " + body);
			var latestVersionsRequest = HttpRequest.newBuilder()
					.POST(HttpRequest.BodyPublishers.ofString(body))
					.header("User-Agent", userAgent)
					.header("Content-Type", "application/json")
					.uri(URI.create("https://api.modrinth.com/v2/version_files/update"))
					.build();

			try {
				var latestVersionsResponse = client.send(latestVersionsRequest, HttpResponse.BodyHandlers.ofString());

				int status = latestVersionsResponse.statusCode();
				LOGGER.debug("Status: " + status);
				if (status == 410) {
					apiV2Deprecated = true;
					LOGGER.warn("Modrinth API v2 is deprecated, unable to check for mod updates.");
				} else if (status == 200) {
					JsonObject responseObject = JsonParser.parseString(latestVersionsResponse.body()).getAsJsonObject();
					LOGGER.debug(String.valueOf(responseObject));
					for (var entry : responseObject.entrySet()) {
						var lookupHash = entry.getKey();
						var versionJson = entry.getValue();

						var versionObj = versionJson.getAsJsonObject();
						var projectId = versionObj.get("project_id").getAsString();
						var versionNumber = versionObj.get("version_number").getAsString();
						var versionId = versionObj.get("id").getAsString();

						var versionHash = "";
						for (var file : versionObj.get("files").getAsJsonArray()) {
							if (file.getAsJsonObject().get("primary").getAsBoolean()) {
								versionHash = file.getAsJsonObject().get("hashes").getAsJsonObject().get("sha512").getAsString();
								break;
							}
						}

						if (!versionHash.isEmpty() && !Objects.equals(versionHash, lookupHash)) {
							// hashes different, there's an update.
							HASH_TO_MOD.get(lookupHash).forEach(mod -> {
								LOGGER.info("Update available for '{}@{}', (-> {})", mod.getId(), mod.getVersion(), versionNumber);
								mod.setModrinthData(new ModrinthData(projectId, versionId, versionNumber));
								ModMenu.modUpdateAvailable = true;
							});
						}
					}
				}
			} catch (IOException | InterruptedException e) {
				LOGGER.error("Error checking for updates: ", e);
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

	public static class LatestVersionsFromHashesBody {
		public Collection<String> hashes;
		public String algorithm = "sha512";
		public Collection<String> loaders;
		@SerializedName("game_versions")
		public Collection<String> gameVersions;

		public LatestVersionsFromHashesBody(Collection<String> hashes, String loader, String mcVersion) {
			this.hashes = hashes;
			this.loaders = Set.of(loader);
			this.gameVersions = Set.of(mcVersion);
		}
	}
}
