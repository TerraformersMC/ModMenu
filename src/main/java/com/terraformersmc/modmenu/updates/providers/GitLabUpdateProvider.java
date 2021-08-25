package com.terraformersmc.modmenu.updates.providers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.terraformersmc.modmenu.updates.AvailableUpdate;
import com.terraformersmc.modmenu.updates.ModUpdateProvider;
import com.terraformersmc.modmenu.util.mod.fabric.CustomValueUtil;
import com.terraformersmc.modmenu.util.mod.fabric.ModUpdateData;
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

public class GitLabUpdateProvider extends ModUpdateProvider<GitLabUpdateProvider.GitLabUpdateData> {
	private static final Gson gson = new GsonBuilder().create();

	public GitLabUpdateProvider(String gameVersion) {
		super(gameVersion);
	}

	@Override
	public void check(String modId, GitLabUpdateProvider.GitLabUpdateData data, Consumer<AvailableUpdate> callback) {
		beginUpdateCheck();
		Util.getMainWorkerExecutor().execute(() -> {
			// We don't want trailing slashes on the gitlabUrl.
			if (data.gitlabUrl.endsWith("/")) {
				data.gitlabUrl = data.gitlabUrl.substring(0, data.gitlabUrl.length() - 1);
			}

			// See the documentation for this endpoint here:
			// https://docs.gitlab.com/ee/api/releases/index.html#list-releases
			String repo = data.repository;
			if (repo.contains("/")) repo = repo.replace("/", "%2F"); // GitLab expects the repositories to be URL-encoded.
			String url = String.format("%s/api/v4/projects/%s/releases", data.gitlabUrl, repo);

			HttpGet request = new HttpGet(url);
			request.addHeader(HttpHeaders.USER_AGENT, String.format("ModMenu (%s)", this.getClass().getSimpleName()));

			try (CloseableHttpResponse response = httpClient.execute(request)) {
				if (response.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();

					if (entity != null) {
						// These releases should be sorted already by the GitLab API.
						GitLabReleaseResponse[] releases = gson.fromJson(EntityUtils.toString(entity), GitLabReleaseResponse[].class);

						if (releases.length > 0) {
							GitLabReleaseResponse latest = releases[0];
							String versionTag = latest.tag.startsWith("v") ? latest.tag.substring(1) : latest.tag;

							if (!latest.tag.equalsIgnoreCase(data.metadata.getVersion().getFriendlyString())
									&& !latest.draft
									&& versionTag.matches(data.versionRegEx)) {
								AvailableUpdate update = new AvailableUpdate(
										versionTag,
										String.format("%s/%s/-/releases/%s", data.gitlabUrl, data.repository, latest.tag),
										latest.body,
										"gitlab_releases"
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

	@NotNull
	@Override
	public GitLabUpdateProvider.GitLabUpdateData readModUpdateData(ModMetadata metadata, String modFileName, CustomValue.CvObject object) {
		Optional<String> repository = CustomValueUtil.getString("repository", object);
		Optional<String> versionRegEx = CustomValueUtil.getString("versionRegEx", object);
		Optional<String> gitlabUrl = CustomValueUtil.getString("gitlabUrl", object);

		if (repository.isEmpty() || versionRegEx.isEmpty()) {
			throw new RuntimeException("GitLab update provider must have a repository and versionRegEx field.");
		}

		return new GitLabUpdateProvider.GitLabUpdateData(
				metadata,
				modFileName,
				repository.get(),
				versionRegEx.get(),
				gitlabUrl.orElse("https://gitlab.com")
		);
	}

	public static class GitLabReleaseResponse {
		@SerializedName("tag_name")
		private String tag;

		@SerializedName("upcoming_release")
		private boolean draft; // If it's upcoming, it's essentially a draft.

		@SerializedName("description")
		private String body;
	}

	public static class GitLabUpdateData extends ModUpdateData {
		public String repository;
		public String versionRegEx;
		public String gitlabUrl; // Allow a mod dev to specify a URL of a self-hosted GitLab instance.

		public GitLabUpdateData(ModMetadata metadata, String modFileName, String repository, String versionRegEx, String gitlabUrl) {
			super(metadata, modFileName);
			this.repository = repository;
			this.versionRegEx = versionRegEx;
			this.gitlabUrl = gitlabUrl;
		}
	}
}
