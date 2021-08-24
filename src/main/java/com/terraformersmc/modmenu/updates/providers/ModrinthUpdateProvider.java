package com.terraformersmc.modmenu.updates.providers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.terraformersmc.modmenu.updates.AvailableUpdate;
import com.terraformersmc.modmenu.updates.ModUpdateProvider;
import com.terraformersmc.modmenu.util.mod.fabric.ModUpdateData;
import com.terraformersmc.modmenu.util.mod.fabric.CustomValueUtil;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.util.Util;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModrinthUpdateProvider extends ModUpdateProvider<ModrinthUpdateProvider.ModrinthUpdateData> {
	private static final Gson gson = new GsonBuilder().create();

	public ModrinthUpdateProvider(String gameVersion) {
		super(gameVersion);
	}

	@Override
	public void check(String modId, ModrinthUpdateData data, Consumer<AvailableUpdate> callback) {
		beginUpdateCheck();
		Util.getMainWorkerExecutor().execute(() -> {
			Map<String, String> filterParams = new HashMap<>();
			filterParams.put("game_versions", String.format("[\"%s\"]", this.gameVersion));
			filterParams.put("loaders", "[\"fabric\"]");

			String url = filterParams.keySet().stream()
					.map(key -> key + "=" + encodeString(filterParams.get(key)))
					.collect(Collectors.joining("&",
							String.format("https://api.modrinth.com/api/v1/mod/%s/version?", data.projectId),
							""));

			HttpGet request = new HttpGet(url);
			request.addHeader(HttpHeaders.USER_AGENT, "ModMenu (ModrinthUpdateProvider)");

			try (CloseableHttpResponse response = httpClient.execute(request)) {
				if (response.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						ModrinthVersion[] versions = gson.fromJson(EntityUtils.toString(entity), ModrinthVersion[].class);
						if (versions.length > 0) {
							ModrinthVersion latest = versions[0];
							if (!latest.versionNumber.equalsIgnoreCase(data.metadata.getVersion().getFriendlyString())) {
								AvailableUpdate update = new AvailableUpdate(
										latest.versionNumber,
										String.format("https://modrinth.com/mod/%s/version/%s", data.projectId, latest.versionId),
										(latest.changeLog == null || latest.changeLog.isEmpty()) ? null : latest.changeLog,
										"modrinth"
								);
								availableUpdates.incrementAndGet();
								callback.accept(update);
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			completeUpdateCheck();
		});
	}

	@Override
	public @NotNull ModrinthUpdateData readModUpdateData(ModMetadata metadata, String modFileName, CustomValue.CvObject object) {
		Optional<String> projectId = CustomValueUtil.getString("projectId", object);
		Optional<String> channel = CustomValueUtil.getString("channel", object);

		if (projectId.isEmpty()) {
			throw new RuntimeException("The modrinth update provider requires a single projectId field.");
		}

		return new ModrinthUpdateData(
				metadata,
				modFileName,
				projectId.get(),
				channel.orElse("release") // We will just default to the release channel.
		);
	}

	private String encodeString(String str) {
		try {
			return URLEncoder.encode(str, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "null";
	}

	public static class ModrinthVersion {
		@SerializedName("id")
		private String versionId;

		@SerializedName("version_number")
		private String versionNumber;

		@SerializedName("changelog")
		private String changeLog;
	}

	public static class ModrinthUpdateData extends ModUpdateData {
		String projectId;
		String channel;

		public ModrinthUpdateData(ModMetadata metadata, String modFileName, String projectId, String channel) {
			super(metadata, modFileName);
			this.projectId = projectId;
			this.channel = channel;
		}
	}
}
