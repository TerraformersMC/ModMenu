package com.terraformersmc.modmenu.util.mod.quilt;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.terraformersmc.modmenu.util.UpdateCheckerUtil;
import com.terraformersmc.modmenu.util.mod.fabric.FabricMod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModContributor;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.loader.api.QuiltLoader;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class QuiltMod extends FabricMod {
	protected final ModContainer container;
	protected final ModMetadata metadata;

	public QuiltMod(net.fabricmc.loader.api.ModContainer fabricModContainer, Set<String> modpackMods) {
		super(fabricModContainer, modpackMods);
		this.container = QuiltLoader.getModContainer(fabricModContainer.getMetadata().getId()).get();
		this.metadata = container.metadata();

		if ("quilt_loader".equals(metadata.id())) {
			badges.add(Badge.LIBRARY);
		}
	}

	@Override
	public @NotNull List<String> getAuthors() {
		List<String> authors = metadata.contributors().stream().filter(contributor -> contributor.role().equals("Author") || contributor.role().equals("Owner")).map(ModContributor::name).collect(Collectors.toList());
		if (authors.isEmpty()) {
			metadata.contributors().stream().findFirst().ifPresent(modContributor -> authors.add(modContributor.name()));
		}
		if (authors.isEmpty()) {
			if ("minecraft".equals(getId())) {
				return Lists.newArrayList("Mojang Studios");
			} else if ("java".equals(getId())) {
				return Lists.newArrayList(System.getProperty("java.vendor"));
			}
		}
		return authors;
	}

	@Override
	public @NotNull List<String> getContributors() {
		List<String> authors = metadata.contributors().stream().map(modContributor -> modContributor.name() + " (" + modContributor.role() + ")").collect(Collectors.toList());
		if ("minecraft".equals(getId()) && authors.isEmpty()) {
			return Lists.newArrayList();
		}
		return authors;
	}

	@Override
	public @NotNull List<String> getCredits() {
		return this.getContributors();
	}


	public @Nullable String getSha512Hash() throws IOException {
		var fabricResult = super.getSha512Hash();
		if (fabricResult == null) {
			UpdateCheckerUtil.LOGGER.debug("Checking {}", getId());
			if (container.getSourceType().equals(ModContainer.BasicSourceType.NORMAL_QUILT) || container.getSourceType().equals(ModContainer.BasicSourceType.NORMAL_FABRIC)) {
				for (var paths : container.getSourcePaths()) {
					List<Path> jars = paths.stream().filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".jar")).toList();

					if (jars.size() == 1 && jars.get(0).getFileSystem() == FileSystems.getDefault()) {
						var file = jars.get(0).toFile();

						if (file.exists()) {
							UpdateCheckerUtil.LOGGER.debug("Found {} hash", getId());
							return Files.asByteSource(file).hash(Hashing.sha512()).toString();
						}
					}
				}
			}
		}
		return fabricResult;
	}
}
