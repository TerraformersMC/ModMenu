package com.terraformersmc.modmenu.util;

import com.google.gson.JsonElement;
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
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public final class UpdateCheckerUtil {
	private static final Logger LOGGER = LogManager.getLogger("Mod Menu/Update Checker");
	private static final String MODRINTH_API = "https://api.modrinth.com/v2";
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(15))
			.build();
	private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(runnable -> {
		Thread thread = new Thread(runnable, "Mod Menu Update Checker");
		thread.setDaemon(true);
		return thread;
	});
	private static final AtomicLong CHECK_GENERATION = new AtomicLong();
	private static volatile boolean apiUnavailable;

	private UpdateCheckerUtil() {
	}

	public static void checkForUpdates() {
		long generation = CHECK_GENERATION.incrementAndGet();
		ModMenu.modUpdateAvailable = false;
		for (Mod mod : ModMenu.MODS.values()) {
			mod.setUpdateInfo(null);
		}
		if (!ModMenuConfig.UPDATE_CHECKER.getValue()) {
			return;
		}
		EXECUTOR.execute(() -> checkForUpdates0(generation));
	}

	private static void checkForUpdates0(long generation) {
		List<Mod> modsWithoutChecker = new ArrayList<>();
		for (Mod mod : new ArrayList<>(ModMenu.MODS.values())) {
			if (!mod.isReal() || !mod.allowsUpdateChecks()) {
				continue;
			}
			UpdateChecker checker = mod.getUpdateChecker();
			if (checker == null) {
				modsWithoutChecker.add(mod);
			} else {
				EXECUTOR.execute(() -> runCustomChecker(mod, checker, generation));
			}
		}

		if (!apiUnavailable) {
			checkModrinth(modsWithoutChecker, generation);
		}
	}

	private static void runCustomChecker(Mod mod, UpdateChecker checker, long generation) {
		try {
			UpdateInfo updateInfo = checker.checkForUpdates();
			if (isCurrentCheck(generation, mod) && allowsPreferredChannel(updateInfo)) {
				mod.setUpdateInfo(updateInfo);
			}
			if (updateInfo != null && updateInfo.isUpdateAvailable()) {
				LOGGER.info("Update available for '{}@{}'", mod.getId(), mod.getVersion());
			}
		} catch (Throwable error) {
			LOGGER.error("Custom update checker failed for mod {}", mod.getId(), error);
		}
	}

	private static void checkModrinth(Collection<Mod> mods, long generation) {
		Map<String, Set<Mod>> modsByHash = new HashMap<>();
		for (Mod mod : mods) {
			try {
				String hash = mod.getSha512Hash();
				if (hash != null) {
					modsByHash.computeIfAbsent(hash, ignored -> new HashSet<>()).add(mod);
				}
			} catch (IOException error) {
				LOGGER.error("Unable to hash mod {} for update checking", mod.getId(), error);
			}
		}
		if (modsByHash.isEmpty()) {
			return;
		}

		Map<String, Instant> currentVersions = getCurrentVersions(modsByHash.keySet());
		if (currentVersions == null || apiUnavailable) {
			return;
		}
		Map<String, ModrinthVersion> updatedVersions = getUpdatedVersions(modsByHash.keySet());
		if (updatedVersions == null) {
			return;
		}

		for (Map.Entry<String, ModrinthVersion> entry : updatedVersions.entrySet()) {
			String hash = entry.getKey();
			Instant currentDate = currentVersions.get(hash);
			ModrinthVersion update = entry.getValue();
			if (currentDate == null || !update.releaseDate.isAfter(currentDate) || hash.equals(update.primaryHash)) {
				continue;
			}
			Set<Mod> matchingMods = modsByHash.get(hash);
			if (matchingMods == null) {
				continue;
			}
			for (Mod mod : matchingMods) {
				if (isCurrentCheck(generation, mod) && isNewerVersion(mod.getVersion(), update.versionNumber)) {
					mod.setUpdateInfo(new ModrinthUpdateInfo(
							update.projectId, update.versionId, update.versionNumber, update.channel));
					LOGGER.info("Update available for '{}@{}' (-> {})", mod.getId(), mod.getVersion(), update.versionNumber);
				}
			}
		}
	}

	private static Map<String, Instant> getCurrentVersions(Collection<String> hashes) {
		String body = ModMenu.GSON.toJson(new CurrentVersionsBody(hashes));
		HttpRequest request = requestBuilder("/version_files")
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();
		try {
			HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 410) {
				apiUnavailable = true;
				LOGGER.warn("Modrinth API v2 is no longer available; update checks are disabled.");
				return null;
			}
			if (response.statusCode() != 200) {
				LOGGER.warn("Modrinth returned HTTP {} while checking current versions", response.statusCode());
				return null;
			}

			Map<String, Instant> dates = new HashMap<>();
			JsonObject responseObject = new JsonParser().parse(response.body()).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : responseObject.entrySet()) {
				try {
				dates.put(entry.getKey(), Instant.parse(entry.getValue().getAsJsonObject().get("date_published").getAsString()));
				} catch (DateTimeParseException | IllegalStateException | NullPointerException error) {
					LOGGER.debug("Ignoring invalid current-version response for hash {}", entry.getKey(), error);
				}
			}
			return dates;
		} catch (IOException error) {
			LOGGER.error("Unable to fetch current Modrinth version dates", error);
		} catch (InterruptedException error) {
			Thread.currentThread().interrupt();
			LOGGER.error("Interrupted while fetching current Modrinth version dates", error);
		} catch (RuntimeException error) {
			LOGGER.error("Unable to parse current Modrinth version response", error);
		}
		return null;
	}

	private static Map<String, ModrinthVersion> getUpdatedVersions(Collection<String> hashes) {
		String body = ModMenu.GSON.toJson(new LatestVersionsBody(hashes));
		HttpRequest request = requestBuilder("/version_files/update")
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();
		try {
			HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 410) {
				apiUnavailable = true;
				LOGGER.warn("Modrinth API v2 is no longer available; update checks are disabled.");
				return null;
			}
			if (response.statusCode() != 200) {
				LOGGER.warn("Modrinth returned HTTP {} while checking for updates", response.statusCode());
				return null;
			}

			Map<String, ModrinthVersion> updates = new HashMap<>();
			JsonObject responseObject = new JsonParser().parse(response.body()).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : responseObject.entrySet()) {
				JsonObject version = entry.getValue().getAsJsonObject();
				try {
					String projectId = version.get("project_id").getAsString();
					String versionId = version.get("id").getAsString();
					String versionNumber = version.get("version_number").getAsString();
					Instant releaseDate = Instant.parse(version.get("date_published").getAsString());
					UpdateChannel channel = parseChannel(version.get("version_type").getAsString());
					JsonElement filesElement = version.get("files");
					if (filesElement == null || !filesElement.isJsonArray()) {
						continue;
					}
					String primaryHash = null;
					for (JsonElement file : filesElement.getAsJsonArray()) {
						JsonObject fileObject = file.getAsJsonObject();
						if (fileObject.has("primary") && fileObject.get("primary").getAsBoolean()) {
							primaryHash = fileObject.getAsJsonObject("hashes").get("sha512").getAsString();
							break;
						}
					}
					if (primaryHash != null) {
						updates.put(entry.getKey(), new ModrinthVersion(
								projectId, versionId, versionNumber, releaseDate, primaryHash, channel));
					}
				} catch (DateTimeParseException | IllegalStateException | NullPointerException error) {
					LOGGER.debug("Ignoring invalid update response for hash {}", entry.getKey(), error);
				}
			}
			return updates;
		} catch (IOException error) {
			LOGGER.error("Unable to fetch Modrinth updates", error);
		} catch (InterruptedException error) {
			Thread.currentThread().interrupt();
			LOGGER.error("Interrupted while fetching Modrinth updates", error);
		} catch (RuntimeException error) {
			LOGGER.error("Unable to parse Modrinth update response", error);
		}
		return null;
	}

	private static HttpRequest.Builder requestBuilder(String path) {
		String modMenuVersion = FabricLoader.getInstance().getModContainer(ModMenu.MOD_ID)
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse("unknown");
		String gameVersion = SharedConstants.getGameVersion().getName();
		String loader = FabricLoader.getInstance().isModLoaded("quilt_loader") ? "quilt" : "fabric";
		return HttpRequest.newBuilder(URI.create(MODRINTH_API + path))
				.timeout(Duration.ofSeconds(30))
				.header("User-Agent", "TerraformersMC/ModMenu/" + modMenuVersion + " (" + gameVersion + "/" + loader + ")");
	}

	private static UpdateChannel parseChannel(String value) {
		try {
			return UpdateChannel.valueOf(value.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException error) {
			return UpdateChannel.RELEASE;
		}
	}

	private static boolean isCurrentCheck(long generation, Mod mod) {
		return CHECK_GENERATION.get() == generation
				&& ModMenuConfig.UPDATE_CHECKER.getValue()
				&& mod.allowsUpdateChecks();
	}

	private static boolean allowsPreferredChannel(UpdateInfo updateInfo) {
		return updateInfo == null
				|| !updateInfo.isUpdateAvailable()
				|| updateInfo.getUpdateChannel().ordinal() >= UpdateChannel.getUserPreference().ordinal();
	}

	private static boolean isNewerVersion(String currentVersion, String updateVersion) {
		try {
			return SemanticVersion.parse(updateVersion).compareTo(SemanticVersion.parse(currentVersion)) > 0;
		} catch (VersionParsingException | IllegalArgumentException error) {
			return true;
		}
	}

	private static class CurrentVersionsBody {
		private final Collection<String> hashes;
		private final String algorithm = "sha512";

		private CurrentVersionsBody(Collection<String> hashes) {
			this.hashes = hashes;
		}
	}

	private static class LatestVersionsBody {
		private final Collection<String> hashes;
		private final String algorithm = "sha512";
		private final Collection<String> loaders;
		@SerializedName("game_versions")
		private final Collection<String> gameVersions;
		@SerializedName("version_types")
		private final Collection<String> versionTypes;

		private LatestVersionsBody(Collection<String> hashes) {
			this.hashes = hashes;
			this.loaders = FabricLoader.getInstance().isModLoaded("quilt_loader")
					? List.of("fabric", "quilt")
					: List.of("fabric");
			this.gameVersions = List.of(SharedConstants.getGameVersion().getName());
			this.versionTypes = new ArrayList<>();
			for (UpdateChannel channel : UpdateChannel.values()) {
				if (channel.ordinal() >= UpdateChannel.getUserPreference().ordinal()) {
					versionTypes.add(channel.name().toLowerCase(Locale.ROOT));
				}
			}
		}
	}

	private static class ModrinthVersion {
		private final String projectId;
		private final String versionId;
		private final String versionNumber;
		private final Instant releaseDate;
		private final String primaryHash;
		private final UpdateChannel channel;

		private ModrinthVersion(
				String projectId,
				String versionId,
				String versionNumber,
				Instant releaseDate,
				String primaryHash,
				UpdateChannel channel
		) {
			this.projectId = projectId;
			this.versionId = versionId;
			this.versionNumber = versionNumber;
			this.releaseDate = releaseDate;
			this.primaryHash = primaryHash;
			this.channel = channel;
		}
	}
}