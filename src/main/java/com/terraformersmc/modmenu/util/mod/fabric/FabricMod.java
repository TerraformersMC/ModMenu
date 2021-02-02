package com.terraformersmc.modmenu.util.mod.fabric;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.OptionalUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModIconHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class FabricMod implements Mod {
	private static final Logger LOGGER = LogManager.getLogger("Mod Menu");

	private final ModContainer container;
	private final ModMetadata metadata;

	private final ModMenuData modMenuData;

	private final Set<Badge> badges;

	private final Map<String, String> links = new HashMap<>();

	public FabricMod(ModContainer modContainer) {
		this.container = modContainer;
		this.metadata = modContainer.getMetadata();

		/* Load modern mod menu custom value data */
		boolean usesModernParent = false;
		Optional<String> parentId = Optional.empty();
		ModMenuData.DummyParentData parentData = null;
		Set<String> badgeNames = new HashSet<>();
		CustomValue modMenuValue = metadata.getCustomValue("modmenu");
		if (modMenuValue != null && modMenuValue.getType() == CustomValue.CvType.OBJECT) {
			CustomValue.CvObject modMenuObject = modMenuValue.getAsObject();
			CustomValue parentCv = modMenuObject.get("parent");
			if (parentCv != null) {
				if (parentCv.getType() == CustomValue.CvType.STRING) {
					parentId = Optional.of(parentCv.getAsString());
				} else if (parentCv.getType() == CustomValue.CvType.OBJECT) {
					try {
						CustomValue.CvObject parentObj = parentCv.getAsObject();
						parentId = CustomValueUtil.getString("id", parentObj);
						parentData = new ModMenuData.DummyParentData(
								parentId.orElseThrow(() -> new RuntimeException("Parent object lacks an id")),
								CustomValueUtil.getString("name", parentObj),
								CustomValueUtil.getString("description", parentObj),
								CustomValueUtil.getString("icon", parentObj),
								CustomValueUtil.getStringSet("badges", parentObj).orElse(new HashSet<>())
						);
					} catch (Throwable t) {
						LOGGER.error("Error loading parent data from mod: " + metadata.getId(), t);
					}
				}
			}
			badgeNames.addAll(CustomValueUtil.getStringSet("badges", modMenuObject).orElse(new HashSet<>()));
			links.putAll(CustomValueUtil.getStringMap("links", modMenuObject).orElse(new HashMap<>()));
			usesModernParent = modMenuObject.containsKey("parent");
		}
		this.modMenuData = new ModMenuData(
				badgeNames,
				parentId,
				parentData
		);

		/* Load legacy mod menu custom value data */
		final boolean finalUsesModernParent = usesModernParent;
		CustomValueUtil.getString("modmenu:parent", metadata).ifPresent(parent -> {
			modMenuData.parent = Optional.of(parent);
			if (!finalUsesModernParent) {
				LOGGER.warn("WARNING! Mod " + metadata.getId() + " is only using deprecated 'modmenu:parent' custom value! This will be removed in 1.18 snapshots, so ask the author of this mod to support the new API.");
			}
		});

		CustomValueUtil.getBoolean("modmenu:clientsideOnly", metadata).ifPresent(client -> {
			if (client) {
				modMenuData.badges.add(Badge.CLIENT);
			}
			LOGGER.warn("WARNING! Mod " + metadata.getId() + " is only using deprecated 'modmenu:clientsideOnly' custom value! This is no longer needed and will be removed in 1.18 snapshots.");
		});
		CustomValueUtil.getBoolean("modmenu:api", metadata).ifPresent(library -> {
			if (library) {
				modMenuData.badges.add(Badge.LIBRARY);
			}
			if (!badgeNames.contains("library")) {
				LOGGER.warn("WARNING! Mod " + metadata.getId() + " is only using deprecated 'modmenu:api' custom value! This will be removed in 1.18 snapshots, so ask the author of this mod to support the new API.");
			}
		});
		CustomValueUtil.getBoolean("modmenu:deprecated", metadata).ifPresent(deprecated -> {
			if (deprecated) {
				modMenuData.badges.add(Badge.DEPRECATED);
			}
			if (!badgeNames.contains("deprecated")) {
				LOGGER.warn("WARNING! Mod " + metadata.getId() + " is only using deprecated 'modmenu:deprecated' custom value! This will be removed in 1.18 snapshots, so ask the author of this mod to support the new API.");
			}
		});

		/* Hardcode parents and badges for Fabric API & Fabric Loader */
		String id = metadata.getId();
		if (id.startsWith("fabric") && metadata.containsCustomValue("fabric-api:module-lifecycle")) {
			if (FabricLoader.getInstance().isModLoaded("fabric-api")) {
				modMenuData.fillParentIfEmpty("fabric-api");
			} else {
				modMenuData.fillParentIfEmpty("fabric");
			}
			modMenuData.badges.add(Badge.LIBRARY);
		}
		if (id.startsWith("fabric") && (id.equals("fabricloader") || metadata.getProvides().contains("fabricloader") || id.equals("fabric") || id.equals("fabric-api") || metadata.getProvides().contains("fabric") || metadata.getProvides().contains("fabric-api"))) {
			modMenuData.badges.add(Badge.LIBRARY);
		}

		/* Add additional badges */
		this.badges = modMenuData.badges;
		if (this.metadata.getEnvironment() == ModEnvironment.CLIENT) {
			badges.add(Badge.CLIENT);
		}
		if (OptionalUtil.isPresentAndTrue(CustomValueUtil.getBoolean("fabric-loom:generated", metadata)) || "java".equals(id)) {
			badges.add(Badge.LIBRARY);
		}
		if ("deprecated".equals(CustomValueUtil.getString("fabric-api:module-lifecycle", metadata).orElse(null))) {
			badges.add(Badge.DEPRECATED);
		}
		if (metadata.containsCustomValue("patchwork:patcherMeta")) {
			badges.add(Badge.PATCHWORK_FORGE);
		}
		if ("minecraft".equals(getId())) {
			badges.add(Badge.MINECRAFT);
		}
	}

	@Override
	public @NotNull String getId() {
		return metadata.getId();
	}

	@Override
	public @NotNull String getName() {
		return metadata.getName();
	}

	@Override
	public @NotNull NativeImageBackedTexture getIcon(ModIconHandler iconHandler, int i) {
		String iconSourceId = getId();
		String iconPath = metadata.getIconPath(i).orElse("assets/" + getId() + "/icon.png");
		if ("minecraft".equals(getId())) {
			iconSourceId = ModMenu.MOD_ID;
			iconPath = "assets/" + ModMenu.MOD_ID + "/minecraft_icon.png";
		} else if ("java".equals(getId())) {
			iconSourceId = ModMenu.MOD_ID;
			iconPath = "assets/" + ModMenu.MOD_ID + "/java_icon.png";
		}
		final String finalIconSourceId = iconSourceId;
		ModContainer iconSource = FabricLoader.getInstance().getModContainer(iconSourceId).orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Fabric mod with id " + finalIconSourceId));
		NativeImageBackedTexture icon = iconHandler.createIcon(iconSource, iconPath);
		if (icon == null) {
			LOGGER.warn("Warning! Mod {} has a broken icon, loading default icon", metadata.getId());
			return iconHandler.createIcon(FabricLoader.getInstance().getModContainer(ModMenu.MOD_ID).orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Fabric mod with id " + ModMenu.MOD_ID)), "assets/" + ModMenu.MOD_ID + "/unknown_icon.png");
		}
		return icon;
	}

	@Override
	public @NotNull String getSummary() {
		return getDescription();
	}

	@Override
	public @NotNull String getDescription() {
		String description = metadata.getDescription();
		if ("minecraft".equals(getId()) && description.isEmpty()) {
			return "The base game.";
		}
		return description;
	}

	@Override
	public @NotNull String getVersion() {
		return metadata.getVersion().getFriendlyString();
	}

	@Override
	public @NotNull List<String> getAuthors() {
		List<String> authors = metadata.getAuthors().stream().map(Person::getName).collect(Collectors.toList());
		if ("minecraft".equals(getId()) && authors.isEmpty()) {
			return Lists.newArrayList("Mojang Studios");
		}
		return authors;
	}

	@Override
	public @NotNull List<String> getContributors() {
		List<String> authors = metadata.getContributors().stream().map(Person::getName).collect(Collectors.toList());
		if ("minecraft".equals(getId()) && authors.isEmpty()) {
			return Lists.newArrayList();
		}
		return authors;
	}

	@Override
	public @NotNull Set<Badge> getBadges() {
		return badges;
	}

	@Override
	public @Nullable String getWebsite() {
		if ("minecraft".equals(getId())) {
			return "https://www.minecraft.net/";
		}
		return metadata.getContact().get("homepage").orElse(null);
	}

	@Override
	public @Nullable String getIssueTracker() {
		if ("minecraft".equals(getId())) {
			return "https://aka.ms/snapshotbugs?ref=game";
		}
		return metadata.getContact().get("issues").orElse(null);
	}

	@Override
	public @Nullable String getSource() {
		return metadata.getContact().get("sources").orElse(null);
	}

	@Override
	public @Nullable String getParent() {
		return modMenuData.parent.orElse(null);
	}

	@Override
	public @NotNull Set<String> getLicense() {
		if ("minecraft".equals(getId())) {
			return Sets.newHashSet("All Rights Reserved");
		}
		return Sets.newHashSet(metadata.getLicense());
	}

	@Override
	public @NotNull Map<String, String> getLinks() {
		return links;
	}

	@Override
	public boolean isReal() {
		return true;
	}

	public ModMenuData getModMenuData() {
		return modMenuData;
	}

	static class ModMenuData {
		private final Set<Badge> badges;
		private Optional<String> parent;
		private @Nullable
		final DummyParentData dummyParentData;

		public ModMenuData(Set<String> badges, Optional<String> parent, DummyParentData dummyParentData) {
			this.badges = Badge.convert(badges);
			this.parent = parent;
			this.dummyParentData = dummyParentData;
		}

		public Set<Badge> getBadges() {
			return badges;
		}

		public Optional<String> getParent() {
			return parent;
		}

		public @Nullable DummyParentData getDummyParentData() {
			return dummyParentData;
		}

		public void addClientBadge(boolean add) {
			if (add) {
				badges.add(Badge.CLIENT);
			}
		}

		public void addLibraryBadge(boolean add) {
			if (add) {
				badges.add(Badge.LIBRARY);
			}
		}

		public void fillParentIfEmpty(String parent) {
			if (!this.parent.isPresent()) {
				this.parent = Optional.of(parent);
			}
		}

		public static class DummyParentData {
			private final String id;
			private final Optional<String> name;
			private final Optional<String> description;
			private final Optional<String> icon;
			private final Set<Badge> badges;

			public DummyParentData(String id, Optional<String> name, Optional<String> description, Optional<String> icon, Set<String> badges) {
				this.id = id;
				this.name = name;
				this.description = description;
				this.icon = icon;
				this.badges = Badge.convert(badges);
			}

			public String getId() {
				return id;
			}

			public Optional<String> getName() {
				return name;
			}

			public Optional<String> getDescription() {
				return description;
			}

			public Optional<String> getIcon() {
				return icon;
			}

			public Set<Badge> getBadges() {
				return badges;
			}
		}
	}
}
