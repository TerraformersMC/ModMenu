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
import java.util.Optional;
import java.util.function.Consumer;

public class GithubUpdateProvider extends ModUpdateProvider<GithubUpdateProvider.GithubUpdateData> {
	private static final Gson gson = new GsonBuilder().create();

	public GithubUpdateProvider(String gameVersion) {
		super(gameVersion);
	}

	@Override
	public void check(String modId, GithubUpdateData data, Consumer<AvailableUpdate> callback) {
		beginUpdateCheck();
		Util.getMainWorkerExecutor().execute(() -> {
			String url = String.format("https://api.github.com/repos/%s/releases?per_page=25", data.repository);

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
							if (!githubVersionTag.equalsIgnoreCase(data.metadata.getVersion().getFriendlyString())
									&& !githubVersion.draft
									&& (data.allowPreRelease || !githubVersion.preRelease)
									&& githubVersionTag
									.matches(data.versionRegEx)) {
								AvailableUpdate update = new AvailableUpdate(
										githubVersionTag,
										githubVersion.url,
										(githubVersion.body != null && !githubVersion.body.isEmpty()) ? githubVersion.body : null,
										"github_releases"
								);
								availableUpdates.incrementAndGet();
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

	@NotNull
	@Override
	public GithubUpdateData readModUpdateData(ModMetadata metadata, String modFileName, CustomValue.CvObject object) {
		Optional<String> repository = CustomValueUtil.getString("repository", object);
		Optional<String> versionRegEx = CustomValueUtil.getString("versionRegEx", object);
		Optional<Boolean> allowPreRelease = CustomValueUtil.getBoolean("allowPreRelease", object);

		if (repository.isEmpty() || versionRegEx.isEmpty()) {
			throw new RuntimeException("Github update provider must have one of each repository, allowPreRelease, and versionRegex.");
		}

		// ModUpdater compatibility
		String repo;
		if (object.containsKey("owner") && !repository.get().contains("/")) {
			repo = CustomValueUtil.getString("owner", object).get() + "/" + repository.get();
		} else {
			repo = repository.get();
		}

		return new GithubUpdateData(
				metadata,
				modFileName,
				repo,
				versionRegEx.get(),
				allowPreRelease.orElse(false) // Defaults to false.
		);
	}

	public static class GithubResponse {
		@SerializedName("tag_name")
		private String tag;
		private String url;
		private String body;
		private boolean preRelease;
		private boolean draft;
	}

	public static class GithubUpdateData extends ModUpdateData {
		public String repository;
		public boolean allowPreRelease;
		public String versionRegEx;

		public GithubUpdateData(ModMetadata metadata, String modFileName, String repository, String versionRegEx, boolean allowPreRelease) {
			super(metadata, modFileName);
			this.repository = repository;
			this.versionRegEx = versionRegEx;
			this.allowPreRelease = allowPreRelease;
		}
	}
}
