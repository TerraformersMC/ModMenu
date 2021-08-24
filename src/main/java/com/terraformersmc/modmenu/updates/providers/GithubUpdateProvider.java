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
import java.util.function.Consumer;

public class GithubUpdateProvider extends ModUpdateProvider {
	private static final Gson gson = new GsonBuilder().create();

	public GithubUpdateProvider(String gameVersion) {
		super(gameVersion);
	}

	@Override
	public void check(String modId, FabricMod.ModUpdateData data, Consumer<AvailableUpdate> callback) {
		beginUpdateCheck();
		Util.getMainWorkerExecutor().execute(() -> {
			String url = String.format("https://api.github.com/repos/%s/releases?per_page=25", data.getRepository().get());

			HttpGet request = new HttpGet(url);
			request.addHeader(HttpHeaders.USER_AGENT, "ModMenu (GithubUpdateProvider)");
			request.addHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json");

			try (CloseableHttpResponse response = httpClient.execute(request)) {
				if (response.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						GithubResponse[] versions = gson.fromJson(EntityUtils.toString(entity), GithubResponse[].class);

						for (GithubResponse githubVersion : versions) {
							String githubVersionTag = githubVersion.tag.startsWith("v") ? githubVersion.tag.substring(1) : githubVersion.tag;
							if (!githubVersionTag.equalsIgnoreCase(data.getCurrentVersion().getFriendlyString())
									&& !githubVersion.draft
									&& (data.getAllowPrerelease().get() || !githubVersion.preRelease)
									&& githubVersionTag
									.matches(data.getVersionRegEx().get())) {
								AvailableUpdate update = new AvailableUpdate(
										githubVersionTag,
										githubVersion.url,
										(githubVersion.body != null && !githubVersion.body.isEmpty()) ? githubVersion.body : null,
										"github_releases"
								);
								availableUpdates++;
								callback.accept(update);
								break;
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
		if (data.getRepository().isEmpty()
				&& data.getAllowPrerelease().isEmpty()
				&& data.getVersionRegEx().isEmpty()) {
			throw new RuntimeException("Github update provider must have one of each repository, allowPrerelease, and versionRegex.");
		}
	}

	public static class GithubResponse {
		@SerializedName("tag_name")
		private String tag;
		private String url;
		private String body;
		private boolean preRelease;
		private boolean draft;
	}
}
