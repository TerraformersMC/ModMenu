package com.terraformersmc.modmenu.updates;

import com.terraformersmc.modmenu.updates.providers.GithubUpdateProvider;
import com.terraformersmc.modmenu.updates.providers.MavenUpdateProvider;
import com.terraformersmc.modmenu.updates.providers.ModrinthUpdateProvider;
import com.terraformersmc.modmenu.util.mod.fabric.FabricMod;
import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class ModUpdateProvider {

	public final String gameVersion;
	public static final Map<String, ModUpdateProvider> PROVIDERS = new HashMap<>();
	public static int availableUpdates = 0;
	public static final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

	public ModUpdateProvider(String gameVersion) {
		this.gameVersion = gameVersion;
	}

	public abstract void check(String modId, String version, FabricMod.ModUpdateData data, Consumer<AvailableUpdate> callback);

	public abstract void validateData(FabricMod.ModUpdateData data) throws RuntimeException;

	public static void initializeProviders() {
		String gameVersion = MinecraftVersion.field_25319.getName();
		ModUpdateProvider.PROVIDERS.put("modrinth", new ModrinthUpdateProvider(gameVersion));
		ModUpdateProvider.PROVIDERS.put("github", new GithubUpdateProvider(gameVersion));
		ModUpdateProvider.PROVIDERS.put("maven", new MavenUpdateProvider(gameVersion));
	}

	public static Optional<ModUpdateProvider> fromKey(String provider) {
		return Optional.ofNullable(PROVIDERS.get(provider));
	}

}

