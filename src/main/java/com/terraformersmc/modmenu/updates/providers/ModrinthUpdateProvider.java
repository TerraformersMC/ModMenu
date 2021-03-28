package com.terraformersmc.modmenu.updates.providers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.terraformersmc.modmenu.updates.AvailableUpdate;
import com.terraformersmc.modmenu.updates.ModUpdateProvider;
import com.terraformersmc.modmenu.util.mod.fabric.FabricMod;
import net.minecraft.util.Util;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModrinthUpdateProvider extends ModUpdateProvider {

	private static final Gson gson = new GsonBuilder().create();

	public ModrinthUpdateProvider(String gameVersion) {
		super(gameVersion);
	}

	@Override
	public void check(String modId, String version, FabricMod.ModUpdateData data, Consumer<AvailableUpdate> callback) {
		beginUpdateCheck();
		Util.getMainWorkerExecutor().execute(() -> {
			Map<String, String> filterParams = new HashMap<>();
			filterParams.put("game_versions", String.format("[\"%s\"]", gameVersion));
			filterParams.put("loaders", "[\"fabric\"]");

			String url = filterParams.keySet().stream()
					.map(key -> key + "=" + encodeString(filterParams.get(key)))
					.collect(Collectors.joining("&",
							String.format("https://api.modrinth.com/api/v1/mod/%s/version?", data.getProjectId().get()),
							""));

			HttpGet request = new HttpGet(url);
			request.addHeader(HttpHeaders.USER_AGENT, "ModMenu (ModrinthUpdateProvider)");

			try (CloseableHttpResponse response = httpClient.execute(request)) {
				if(response.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					if(entity != null) {
						ModrinthVersion[] versions = gson.fromJson(EntityUtils.toString(entity), ModrinthVersion[].class);
						if(versions.length > 0) {
							ModrinthVersion latest = versions[0];
							if(!latest.versionNumber.equalsIgnoreCase(version)) {
								AvailableUpdate update = new AvailableUpdate(
										latest.versionNumber,
										String.format("https://modrinth.com/mod/%s/version/%s", data.getProjectId().get(), latest.versionId),
										(latest.changeLog == null || latest.changeLog.isEmpty()) ? null : latest.changeLog,
										"modrinth"
								);
								availableUpdates++;
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
	public void validateProviderConfig(FabricMod.ModUpdateData data) throws RuntimeException {
		if(!data.getProjectId().isPresent()) {
			throw new RuntimeException("The modrinth update provider requires a single \"projectId\" field.");
		}
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
}
