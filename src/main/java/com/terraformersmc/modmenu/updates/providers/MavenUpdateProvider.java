package com.terraformersmc.modmenu.updates.providers;

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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;

public class MavenUpdateProvider extends ModUpdateProvider<MavenUpdateProvider.MavenUpdateData> {
	DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

	DocumentBuilder builder = null;

	public MavenUpdateProvider(String gameVersion) {
		super(gameVersion);

		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void check(String modId, MavenUpdateData data, Consumer<AvailableUpdate> callback) {
		beginUpdateCheck();
		Util.getMainWorkerExecutor().execute(() -> {
			String url = String.format("%s%s/%s/maven-metadata.xml",
					(data.repository.endsWith("/") ? data.repository : data.repository + "/"),
					data.group.replaceAll("\\.", "/"),
					data.artifact);

			HttpGet request = new HttpGet(url);
			request.addHeader(HttpHeaders.USER_AGENT, "ModMenu (MavenUpdateProvider)");

			try (CloseableHttpResponse response = httpClient.execute(request)) {
				if (response.getStatusLine().getStatusCode() == 200) {

					HttpEntity entity = response.getEntity();
					if (entity != null) {
						try {
							ByteArrayInputStream stream = new ByteArrayInputStream(EntityUtils.toString(entity).getBytes(StandardCharsets.UTF_8));
							Document document = builder.parse(stream);
							document.getDocumentElement().normalize();
							NodeList versions = document.getElementsByTagName("version");
							for (int i = 0; i < versions.getLength(); i++) {
								String newVersion = versions.item(i).getTextContent();
								if (newVersion.matches(data.versionRegex) && !newVersion.equalsIgnoreCase(data.metadata.getVersion().getFriendlyString())) {
									AvailableUpdate update = new AvailableUpdate(
											newVersion,
											null,
											null,
											"maven"
									);
									callback.accept(update);
									break;
								}
							}
						} catch (SAXException | IOException e) {
							e.printStackTrace();
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
	public @NotNull MavenUpdateData readModUpdateData(ModMetadata metadata, String modFileName, CustomValue.CvObject object) {
		Optional<String> repo = CustomValueUtil.getString("repository", object);
		Optional<String> group = CustomValueUtil.getString("group", object);
		Optional<String> artifact = CustomValueUtil.getString("artifact", object);
		Optional<String> versionRegEx = CustomValueUtil.getString("versionRegEx", object);

		if (repo.isEmpty() || group.isEmpty() || artifact.isEmpty() || versionRegEx.isEmpty()) {
			throw new RuntimeException("Maven update provider must have one of each repository, group, artifact, and versionRegex.");
		}

		return new MavenUpdateData(
				metadata,
				modFileName,
				repo.get(),
				group.get(),
				artifact.get(),
				versionRegEx.get()
		);
	}

	public static class MavenUpdateData extends ModUpdateData {
		String repository;
		String group;
		String artifact;
		String versionRegex;

		public MavenUpdateData(ModMetadata metadata, String modFileName, String repository, String group, String artifact, String versionRegex) {
			super(metadata, modFileName);
			this.repository = repository;
			this.group = group;
			this.artifact = artifact;
			this.versionRegex = versionRegex;
		}
	}
}
