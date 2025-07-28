package com.terraformersmc.modmenu.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModrinthUpdateInfo;
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
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class UpdateCheckerUtil {
	public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu/Update Checker");

	private static boolean modrinthApiV2Deprecated = false;

	private static boolean allowsUpdateChecks(Mod mod) {
		return mod.allowsUpdateChecks();
	}

	public static void checkForUpdates() {
		if (!ModMenuConfig.UPDATE_CHECKER.getValue()) {
			return;
		}

		LOGGER.info("Checking mod updates...");
		Util.getMainWorkerExecutor().execute(UpdateCheckerUtil::checkForUpdates0);
	}

	private static void checkForUpdates0() {
		try (var executor = Executors.newThreadPerTaskExecutor(new UpdateCheckerThreadFactory())) {
			List<Mod> withoutUpdateChecker = new ArrayList<>();

			List<Mod> updatableMods = ModMenu.MODS.values()
				.stream()
				.filter(UpdateCheckerUtil::allowsUpdateChecks)
				.toList();

			for (Mod mod : updatableMods) {
				UpdateChecker updateChecker = mod.getUpdateChecker();

				if (updateChecker == null) {
					withoutUpdateChecker.add(mod); // Fall back to update checking via Modrinth
				} else {
					executor.submit(() -> {
						// We don't know which mod the thread is for yet in the thread factory
						Thread.currentThread().setName("ModMenu/Update Checker/%s".formatted(mod.getName()));

						var update = updateChecker.checkForUpdates();
						mod.setUpdateInfo(update);

						if (update != null && update.isUpdateAvailable()) {
							LOGGER.info("Update available for '{}@{}'", mod.getId(), mod.getVersion());
						}
					});
				}
			}

			if (modrinthApiV2Deprecated) {
				return;
			}

			var modHashes = getModHashes(withoutUpdateChecker);

			var currentVersionsFuture = executor.submit(() -> getCurrentVersions(modHashes.keySet()));
			var updatedVersionsFuture = executor.submit(() -> getUpdatedVersions(modHashes.keySet()));

			Map<String, Instant> currentVersions = null;
			Map<String, VersionUpdate> updatedVersions = null;

			try {
				currentVersions = currentVersionsFuture.get();
				updatedVersions = updatedVersionsFuture.get();
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			if (currentVersions == null || updatedVersions == null) {
				return;
			}

			for (var hash : modHashes.keySet()) {
				var date = currentVersions.get(hash);
				var data = updatedVersions.get(hash);

				if (date == null || data == null) {
					continue;
				}

				// Current version is still the newest
				if (Objects.equals(hash, data.hash)) {
					continue;
				}

				// Current version is newer than what's
				// Available on our preferred update channel
				if (date.compareTo(data.releaseDate) >= 0) {
					continue;
				}

				for (var mod : modHashes.get(hash)) {
					mod.setUpdateInfo(data.asUpdateInfo());
					LOGGER.info("Update available for '{}@{}', (-> {})",
						mod.getId(),
						mod.getVersion(),
						data.versionNumber
					);
				}
			}
		}
	}

	private static Map<String, Set<Mod>> getModHashes(Collection<Mod> mods) {
		Map<String, Set<Mod>> results = new HashMap<>();

		for (var mod : mods) {
			String modId = mod.getId();

			try {
				String hash = mod.getSha512Hash();

				if (hash != null) {
					LOGGER.debug("Hash for {} is {}", modId, hash);
					results.putIfAbsent(hash, new HashSet<>());
					results.get(hash).add(mod);
				}
			} catch (IOException e) {
				LOGGER.error("Error getting mod hash for mod {}: ", modId, e);
			}
		}

		return results;
	}

	public static void triggerV2DeprecatedToast() {
		if (modrinthApiV2Deprecated && ModMenuConfig.UPDATE_CHECKER.getValue()) {
			MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION,
				Text.translatable("modmenu.modrinth.v2_deprecated.title"),
				Text.translatable("modmenu.modrinth.v2_deprecated.description")
			));
		}
	}

	/**
	 * @return a map of file hash to its release date on Modrinth.
	 */
	private static @Nullable Map<String, Instant> getCurrentVersions(Collection<String> modHashes) {
		String body = ModMenu.GSON_MINIFIED.toJson(new CurrentVersionsFromHashes(modHashes));

		var request = HttpRequest.newBuilder()
			.POST(HttpRequest.BodyPublishers.ofString(body))
			.header("Content-Type", "application/json")
			.uri(URI.create("https://api.modrinth.com/v2/version_files"));

		try {
			var response = HttpUtil.request(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 410) {
				modrinthApiV2Deprecated = true;
				LOGGER.warn("Modrinth API v2 is deprecated, unable to check for mod updates.");
			} else if (response.statusCode() == 200) {
				Map<String, Instant> results = new HashMap<>();
				JsonObject data = JsonParser.parseString(response.body()).getAsJsonObject();

				data.asMap().forEach((hash, inner) -> {
					Instant date;
					var version = inner.getAsJsonObject();

					try {
						date = Instant.parse(version.get("date_published").getAsString());
					} catch (DateTimeParseException e) {
						return;
					}

					results.put(hash, date);
				});

				return results;
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Error checking for versions: ", e);
		}

		return null;
	}

	public static class CurrentVersionsFromHashes {
		public Collection<String> hashes;
		public String algorithm = "sha512";

		public CurrentVersionsFromHashes(Collection<String> hashes) {
			this.hashes = hashes;
		}
	}

	private static UpdateChannel getUpdateChannel(String versionType) {
		try {
			return UpdateChannel.valueOf(versionType.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException | NullPointerException e) {
			return UpdateChannel.RELEASE;
		}
	}

	private static @Nullable Map<String, VersionUpdate> getUpdatedVersions(Collection<String> modHashes) {
		String mcVer = SharedConstants.getGameVersion().name();
		List<String> loaders = ModMenu.RUNNING_QUILT ? List.of("fabric", "quilt") : List.of("fabric");

		List<UpdateChannel> updateChannels;
		UpdateChannel preferredChannel = UpdateChannel.getUserPreference();

		if (preferredChannel == UpdateChannel.RELEASE) {
			updateChannels = List.of(UpdateChannel.RELEASE);
		} else if (preferredChannel == UpdateChannel.BETA) {
			updateChannels = List.of(UpdateChannel.BETA, UpdateChannel.RELEASE);
		} else {
			updateChannels = List.of(UpdateChannel.ALPHA, UpdateChannel.BETA, UpdateChannel.RELEASE);
		}

		String body = ModMenu.GSON_MINIFIED.toJson(new LatestVersionsFromHashesBody(modHashes,
			loaders,
			mcVer,
			updateChannels
		));

		LOGGER.debug("Body: {}", body);
		var latestVersionsRequest = HttpRequest.newBuilder()
			.POST(HttpRequest.BodyPublishers.ofString(body))
			.header("Content-Type", "application/json")
			.uri(URI.create("https://api.modrinth.com/v2/version_files/update"));

		try {
			var latestVersionsResponse = HttpUtil.request(latestVersionsRequest, HttpResponse.BodyHandlers.ofString());

			int status = latestVersionsResponse.statusCode();
			LOGGER.debug("Status: {}", status);
			if (status == 410) {
				modrinthApiV2Deprecated = true;
				LOGGER.warn("Modrinth API v2 is deprecated, unable to check for mod updates.");
			} else if (status == 200) {
				Map<String, VersionUpdate> results = new HashMap<>();
				JsonObject responseObject = JsonParser.parseString(latestVersionsResponse.body()).getAsJsonObject();
				LOGGER.debug(String.valueOf(responseObject));
				responseObject.asMap().forEach((lookupHash, versionJson) -> {
					var versionObj = versionJson.getAsJsonObject();
					var projectId = versionObj.get("project_id").getAsString();
					var versionType = versionObj.get("version_type").getAsString();
					var versionNumber = versionObj.get("version_number").getAsString();
					var versionId = versionObj.get("id").getAsString();
					var primaryFile = versionObj.get("files")
						.getAsJsonArray()
						.asList()
						.stream()
						.filter(file -> file.getAsJsonObject().get("primary").getAsBoolean())
						.findFirst();

					if (primaryFile.isEmpty()) {
						return;
					}

					Instant date;

					try {
						date = Instant.parse(versionObj.get("date_published").getAsString());
					} catch (DateTimeParseException e) {
						return;
					}

					var updateChannel = UpdateCheckerUtil.getUpdateChannel(versionType);
					var versionHash = primaryFile.get()
						.getAsJsonObject()
						.get("hashes")
						.getAsJsonObject()
						.get("sha512")
						.getAsString();

					results.put(lookupHash,
						new VersionUpdate(projectId, versionId, versionNumber, date, updateChannel, versionHash)
					);
				});

				return results;
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Error checking for updates: ", e);
		}

		return null;
	}

	private record VersionUpdate(
		String projectId,
		String versionId,
		String versionNumber,
		Instant releaseDate,
		UpdateChannel updateChannel,
		String hash
	) {
		private UpdateInfo asUpdateInfo() {
			return new ModrinthUpdateInfo(this.projectId, this.versionId, this.versionNumber, this.updateChannel);
		}
	}

	public static class LatestVersionsFromHashesBody {
		public Collection<String> hashes;
		public String algorithm = "sha512";
		public Collection<String> loaders;
		@SerializedName("game_versions")
		public Collection<String> gameVersions;
		@SerializedName("version_types")
		public Collection<String> versionTypes;

		public LatestVersionsFromHashesBody(
			Collection<String> hashes,
			Collection<String> loaders,
			String mcVersion,
			Collection<UpdateChannel> updateChannels
		) {
			this.hashes = hashes;
			this.loaders = loaders;
			this.gameVersions = Set.of(mcVersion);
			this.versionTypes = updateChannels.stream().map(value -> value.toString().toLowerCase()).toList();
		}
	}
}
