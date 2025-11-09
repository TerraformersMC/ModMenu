package com.terraformersmc.modmenu.util.mod.quilt;

import com.google.gson.JsonParser;
import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.util.HttpUtil;
import com.terraformersmc.modmenu.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.Version;
import org.quiltmc.loader.api.VersionFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import net.minecraft.network.chat.Component;

public class QuiltLoaderUpdateChecker implements UpdateChecker {
	public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu/Quilt Update Checker");
	private static final URI LOADER_VERSIONS = URI.create("https://meta.quiltmc.org/v3/versions/loader");

	@Override
	public UpdateInfo checkForUpdates() {
		UpdateInfo result = null;
		try {
			result = checkForUpdates0();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			LOGGER.error("Failed Quilt Loader update check!", e);
		}

		return result;
	}

	private static UpdateInfo checkForUpdates0() throws IOException, InterruptedException {
		var preferredChannel = UpdateChannel.getUserPreference();

		var request = HttpRequest.newBuilder().GET().uri(LOADER_VERSIONS);
		var response = HttpUtil.request(request, HttpResponse.BodyHandlers.ofString());

		var status = response.statusCode();
		if (status != 200) {
			LOGGER.warn("Quilt Meta responded with a non-200 status: {}!", status);
			return null;
		}

		var contentType = response.headers().firstValue("Content-Type");
		if (contentType.isEmpty() || !contentType.get().contains("application/json")) {
			LOGGER.warn("Quilt Meta responded with a non-json content type, aborting loader update check!");
			return null;
		}

		var data = JsonParser.parseString(response.body());
		if (!data.isJsonArray()) {
			LOGGER.warn("Received invalid data from Quilt Meta, aborting loader update check!");
			return null;
		}

		Version.Semantic match = null;
		for (var child : data.getAsJsonArray()) {
			if (!child.isJsonObject()) {
				continue;
			}

			var object = child.getAsJsonObject();
			var version = JsonUtil.getString(object, "version");
			if (version.isEmpty()) {
				continue;
			}

			Version.Semantic parsed;
			try {
				parsed = Version.Semantic.of(version.get());
			} catch (VersionFormatException e) {
				continue;
			}

			if (preferredChannel == UpdateChannel.RELEASE && !parsed.preRelease().isEmpty()) {
				continue;
			} else if (preferredChannel == UpdateChannel.BETA && !isStableOrBeta(parsed.preRelease())) {
				continue;
			}

			if (match == null || isNewer(parsed, match)) {
				match = parsed;
			}
		}

		Version.Semantic current = getCurrentVersion();
		if (match == null || !isNewer(match, current)) {
			LOGGER.debug("Quilt Loader is up to date.");
			return null;
		}

		LOGGER.debug("Quilt Loader has a matching update available!");
		return new QuiltLoaderUpdateInfo(match);
	}

	private static boolean isNewer(Version.Semantic self, Version.Semantic other) {
		return self.compareTo(other) > 0;
	}

	private static Version.Semantic getCurrentVersion() {
		return QuiltLoader.getModContainer("quilt_loader").get().metadata().version().semantic();
	}

	private static boolean isStableOrBeta(String preRelease) {
		return preRelease.isEmpty() || preRelease.startsWith("beta") || preRelease.startsWith("pre") ||
			preRelease.startsWith("rc");
	}

	private record QuiltLoaderUpdateInfo(Version.Semantic version) implements UpdateInfo {
		@Override
		public boolean isUpdateAvailable() {
			return true;
		}

		@Override
		public @NotNull Component getUpdateMessage() {
			return Component.translatable("modmenu.install_version", this.version.raw());
		}

		@Override
		public String getDownloadLink() {
			return "https://quiltmc.org/en/install/client";
		}

		@Override
		public UpdateChannel getUpdateChannel() {
			var preRelease = this.version.preRelease();
			if (preRelease.isEmpty()) {
				return UpdateChannel.RELEASE;
			} else if (isStableOrBeta(preRelease)) {
				return UpdateChannel.BETA;
			} else {
				return UpdateChannel.ALPHA;
			}
		}
	}
}
