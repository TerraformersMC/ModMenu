package com.terraformersmc.modmenu.util.mod.quilt;

import com.google.common.collect.Lists;
import com.terraformersmc.modmenu.util.mod.fabric.FabricMod;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModContributor;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.loader.api.QuiltLoader;

import java.util.List;
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
}
