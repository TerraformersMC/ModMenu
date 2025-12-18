package com.terraformersmc.modmenu.util.mod.fabric;

import com.google.gson.JsonParser;
import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.util.HttpUtil;
import com.terraformersmc.modmenu.util.JsonUtil;
import com.terraformersmc.modmenu.util.OptionalUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FabricLoaderUpdateChecker implements UpdateChecker {
	public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu/Fabric Update Checker");
	private static final URI LOADER_VERSIONS = URI.create("https://meta.fabricmc.net/v2/versions/loader");

	@Override
	public UpdateInfo checkForUpdates() {
		UpdateInfo result = null;

		try {
			result = checkForUpdates0();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			LOGGER.error("Failed Fabric Loader update check!", e);
		}

		return result;
	}

	private static UpdateInfo checkForUpdates0() throws IOException, InterruptedException {
		var preferredChannel = UpdateChannel.getUserPreference();

		var request = HttpRequest.newBuilder().GET().uri(LOADER_VERSIONS);
		var response = HttpUtil.request(request, HttpResponse.BodyHandlers.ofString());

		var status = response.statusCode();

		if (status != 200) {
			LOGGER.warn("Fabric Meta responded with a non-200 status: {}!", status);
			return null;
		}

		var contentType = response.headers().firstValue("Content-Type");

		if (contentType.isEmpty() || !contentType.get().contains("application/json")) {
			LOGGER.warn("Fabric Meta responded with a non-json content type, aborting loader update check!");
			return null;
		}

		var data = JsonParser.parseString(response.body());

		if (!data.isJsonArray()) {
			LOGGER.warn("Received invalid data from Fabric Meta, aborting loader update check!");
			return null;
		}

		SemanticVersion match = null;
		boolean stableVersion = true;

		for (var child : data.getAsJsonArray()) {
			if (!child.isJsonObject()) {
				continue;
			}

			var object = child.getAsJsonObject();
			var version = JsonUtil.getString(object, "version");

			if (version.isEmpty()) {
				continue;
			}

			SemanticVersion parsed;

			try {
				parsed = SemanticVersion.parse(version.get());
			} catch (VersionParsingException e) {
				continue;
			}

			// Why aren't betas just marked as beta in the version string ...
			var stable = OptionalUtil.isPresentAndTrue(JsonUtil.getBoolean(object, "stable"));

			if (preferredChannel == UpdateChannel.RELEASE && !stable) {
				continue;
			}

			if (match == null || isNewer(parsed, match)) {
				match = parsed;
				stableVersion = stable;
			}
		}

		Version current = getCurrentVersion();

		if (match == null || !isNewer(match, current)) {
			LOGGER.debug("Fabric Loader is up to date.");
			return null;
		}

		LOGGER.debug("Fabric Loader has a matching update available!");
		return new FabricLoaderUpdateInfo(match.getFriendlyString(), stableVersion);
	}

	private static boolean isNewer(Version self, Version other) {
		return self.compareTo(other) > 0;
	}

	private static Version getCurrentVersion() {
		return FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion();
	}

	private static class FabricLoaderUpdateInfo implements UpdateInfo {
		private final String version;
		private final boolean isStable;

		private FabricLoaderUpdateInfo(String version, boolean isStable) {
			this.version = version;
			this.isStable = isStable;
		}

		@Override
		public boolean isUpdateAvailable() {
			return true;
		}

		@Override
		public @Nullable Component getUpdateMessage() {
			return Component.translatable("modmenu.install_version", this.version);
		}

		@Override
		public String getDownloadLink() {
			return "https://fabricmc.net/use/installer";
		}

		@Override
		public UpdateChannel getUpdateChannel() {
			return this.isStable ? UpdateChannel.RELEASE : UpdateChannel.BETA;
		}
	}
}
