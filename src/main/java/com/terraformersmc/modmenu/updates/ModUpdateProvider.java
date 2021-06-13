package com.terraformersmc.modmenu.updates;

import com.terraformersmc.modmenu.updates.providers.*;
import com.terraformersmc.modmenu.util.mod.fabric.FabricMod;
import net.minecraft.MinecraftVersion;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class ModUpdateProvider {

	public final String gameVersion;
	public static final Map<String, ModUpdateProvider> PROVIDERS = new HashMap<>();
	public static int availableUpdates = 0;
	private static int runningChecks = 0;
	public static final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

	public ModUpdateProvider(String gameVersion) {
		this.gameVersion = gameVersion;
	}

	public abstract void check(String modId, String version, FabricMod.ModUpdateData data, Consumer<AvailableUpdate> callback);

	public abstract void validateProviderConfig(FabricMod.ModUpdateData data) throws RuntimeException;

	public static void initializeProviders() {
		String gameVersion = MinecraftVersion.field_25319.getName();
		ModUpdateProvider.PROVIDERS.put("modrinth", new ModrinthUpdateProvider(gameVersion));
		ModUpdateProvider.PROVIDERS.put("github", new GithubUpdateProvider(gameVersion));
		ModUpdateProvider.PROVIDERS.put("maven", new MavenUpdateProvider(gameVersion));
		ModUpdateProvider.PROVIDERS.put("loader", new LoaderMetaUpdateProvider(gameVersion));
		ModUpdateProvider.PROVIDERS.put("curseforge", new CurseforgeUpdateProvider(gameVersion));
	}

	public static Optional<ModUpdateProvider> fromKey(String provider) {
		return Optional.ofNullable(PROVIDERS.get(provider));
	}

	public static void beginUpdateCheck() {
		runningChecks++;
	}

	public static void completeUpdateCheck() {
		runningChecks--;
		if(runningChecks == 0) {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

