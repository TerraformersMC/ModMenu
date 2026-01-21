package com.terraformersmc.modmenu.util;

import com.terraformersmc.modmenu.ModMenu;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.util.Util;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpUtil {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final String USER_AGENT = buildUserAgent();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .executor(Util.nonCriticalIoPool())
            .connectTimeout(TIMEOUT)
            .build();

    private HttpUtil() {
    }

    public static <T> HttpResponse<T> request(
            HttpRequest.Builder builder,
            HttpResponse.BodyHandler<T> handler
    ) throws IOException, InterruptedException {
        builder.setHeader("User-Agent", USER_AGENT);
        builder.timeout(TIMEOUT);
        return HTTP_CLIENT.send(builder.build(), handler);
    }

    private static String buildUserAgent() {
        String env = ModMenu.DEV_ENVIRONMENT ? "/development" : "";
        String loader = ModMenu.RUNNING_QUILT ? "quilt" : "fabric";

        var modMenuVersion = getModMenuVersion();
        var minecraftVersion = SharedConstants.getCurrentVersion().name();

        // -> TerraformersMC/ModMenu/9.1.0 (1.20.3/quilt/development)
        return "%s/%s (%s/%s%s)".formatted(ModMenu.GITHUB_REF, modMenuVersion, minecraftVersion, loader, env);
    }

    private static String getModMenuVersion() {
        var container = FabricLoader.getInstance().getModContainer(ModMenu.MOD_ID);
        if (container.isEmpty()) {
            throw new RuntimeException("Unable to find Modmenu's own mod container!");
        }

        return VersionUtil.removeBuildMetadata(container.get().getMetadata().getVersion().getFriendlyString());
    }
}
