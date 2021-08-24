package com.terraformersmc.modmenu.updates;

import com.terraformersmc.modmenu.updates.providers.*;
import com.terraformersmc.modmenu.util.mod.fabric.ModUpdateData;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.MinecraftVersion;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class ModUpdateProvider<T extends ModUpdateData> {
	public static final Map<String, ModUpdateProvider<? extends ModUpdateData>> PROVIDERS = new HashMap<>();
	public static final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
	public static AtomicInteger availableUpdates = new AtomicInteger();
	private static AtomicInteger runningChecks = new AtomicInteger();

	public final String gameVersion;

	public ModUpdateProvider(String gameVersion) {
		this.gameVersion = gameVersion;
	}

	public static void initializeProviders() {
		String gameVersion = MinecraftVersion.GAME_VERSION.getName();
		ModUpdateProvider.PROVIDERS.put("modrinth", new ModrinthUpdateProvider(gameVersion));
		ModUpdateProvider.PROVIDERS.put("github", new GithubUpdateProvider(gameVersion));
		ModUpdateProvider.PROVIDERS.put("maven", new MavenUpdateProvider(gameVersion));
		ModUpdateProvider.PROVIDERS.put("loader", new LoaderMetaUpdateProvider(gameVersion));
		ModUpdateProvider.PROVIDERS.put("curseforge", new CurseForgeUpdateProvider(gameVersion));
	}

	public static Optional<ModUpdateProvider<ModUpdateData>> fromKey(String provider) {
		return Optional.ofNullable((ModUpdateProvider<ModUpdateData>) PROVIDERS.get(provider));
	}

	public static void beginUpdateCheck() {
		runningChecks.incrementAndGet();
	}

	public static void completeUpdateCheck() {
		if (runningChecks.decrementAndGet() == 0) {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public abstract void check(String modId, T data, Consumer<AvailableUpdate> callback);

	public abstract @NotNull T readModUpdateData(ModMetadata metadata, String modFileName, CustomValue.CvObject updatesObject);
}
