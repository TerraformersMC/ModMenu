package com.terraformersmc.modmenu.updates.providers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

/**
 * This update provider is specifically for checking loader versions.
 */
public class LoaderMetaUpdateProvider extends ModUpdateProvider {

	private static final Gson gson = new GsonBuilder().create();

	public LoaderMetaUpdateProvider(String gameVersion) {
		super(gameVersion);
	}

	@Override
	public void check(String modId, String version, FabricMod.ModUpdateData data, Consumer<AvailableUpdate> callback) {
		beginUpdateCheck();
		Util.getMainWorkerExecutor().execute(() -> {
			HttpGet request = new HttpGet(String.format("https://meta.fabricmc.net/v1/versions/loader/%s", gameVersion));
			request.addHeader(HttpHeaders.USER_AGENT, "ModMenu (LoaderMetaUpdateProvider)");

			try (CloseableHttpResponse response = httpClient.execute(request)) {
				if (response.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					if(entity != null) {
						MetaResponse[] versions = gson.fromJson(EntityUtils.toString(entity), MetaResponse[].class);

						for (MetaResponse metaVersion : versions) {
							if(metaVersion.loader.stable && !version.equalsIgnoreCase(metaVersion.loader.version)) {
								AvailableUpdate update = new AvailableUpdate(
										metaVersion.loader.version,
										"https://fabricmc.net/use/",
										null,
										"fabricmcNet"
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
	public void validateProviderConfig(FabricMod.ModUpdateData data) throws RuntimeException {}

	private static class MetaResponse {
		private Loader loader;

		private static class Loader {
			private String version;
			private boolean stable;
		}
	}

}
