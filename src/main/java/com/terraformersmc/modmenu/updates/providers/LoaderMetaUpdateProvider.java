package com.terraformersmc.modmenu.updates.providers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.updates.AvailableUpdate;
import com.terraformersmc.modmenu.updates.ModUpdateProvider;
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
import java.util.function.Consumer;

/**
 * This update provider is specifically for checking loader versions.
 */
public class LoaderMetaUpdateProvider extends ModUpdateProvider<LoaderMetaUpdateProvider.LoaderMetaUpdateData> {
	private static final Gson gson = new GsonBuilder().create();

	public LoaderMetaUpdateProvider(String gameVersion) {
		super(gameVersion);
	}

	@Override
	public void check(String modId, LoaderMetaUpdateData data, Consumer<AvailableUpdate> callback) {
		beginUpdateCheck();
		Util.getMainWorkerExecutor().execute(() -> {
			HttpGet request = new HttpGet(String.format("https://meta.fabricmc.net/v2/versions/loader/%s", this.gameVersion));
			request.addHeader(HttpHeaders.USER_AGENT, "ModMenu (LoaderMetaUpdateProvider)");

			try (CloseableHttpResponse response = httpClient.execute(request)) {
				if (response.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						MetaResponse[] versions = gson.fromJson(EntityUtils.toString(entity), MetaResponse[].class);

						for (MetaResponse metaVersion : versions) {
							if (!metaVersion.loader.stable && data.metadata.getVersion().getFriendlyString().equalsIgnoreCase(metaVersion.loader.version)) {
								// We have a more recent unstable version (beta). Let's not show the update prompt.
								break;
							}
							if (metaVersion.loader.stable && !data.metadata.getVersion().getFriendlyString().equalsIgnoreCase(metaVersion.loader.version)) {
								AvailableUpdate update = new AvailableUpdate(
										metaVersion.loader.version,
										"https://fabricmc.net/use/",
										null,
										"fabricmcNet"
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
	public @NotNull LoaderMetaUpdateProvider.LoaderMetaUpdateData readModUpdateData(ModMetadata metadata, String modFileName, CustomValue.CvObject updatesObject) {
		return new LoaderMetaUpdateData(metadata, modFileName);
	}

	private static class MetaResponse {
		private Loader loader;

		private static class Loader {
			private String version;

			private boolean stable;
		}
	}

	public static class LoaderMetaUpdateData extends ModUpdateData {
		public LoaderMetaUpdateData(ModMetadata metadata, String modFileName) {
			super(metadata, modFileName);
		}
	}
}
