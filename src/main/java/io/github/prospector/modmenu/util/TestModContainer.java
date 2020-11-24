/*
package io.github.prospector.modmenu.util;

import com.google.gson.JsonElement;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.*;
import net.fabricmc.loader.util.version.VersionParsingException;
import org.apache.commons.lang3.RandomStringUtils;

import java.nio.file.Path;
import java.util.*;

public class TestModContainer implements ModContainer {

	public static final Random RAND = new Random();
	private static Collection<ModContainer> testModContainers;

	public static Collection<ModContainer> getTestModContainers() {
		if (testModContainers == null) {
			testModContainers = new ArrayList<>();
			for (int i = 0; i < 1000; i++) {
				testModContainers.add(new TestModContainer());
			}
		}
		return testModContainers;
	}

	private final ModMetadata metadata = new TestModMetadata();
	private final Path rootPath = FabricLoader.getInstance().getModContainer("fabricloader").orElseThrow(IllegalStateException::new).getRootPath();

	@Override
	public ModMetadata getMetadata() {
		return this.metadata;
	}

	@Override
	public Path getRootPath() {
		return this.rootPath;
	}

	public static class TestModMetadata implements ModMetadata {
		private final String id;
		private final String description;
		private final Version version;

		public TestModMetadata() {
			super();
			this.id = RandomStringUtils.randomAlphabetic(10, 50).toLowerCase(Locale.ROOT);
			this.description = RandomStringUtils.randomAlphabetic(500);
			try {
				this.version = SemanticVersion.parse(String.format("%d.%d.%d+%s", RAND.nextInt(10), RAND.nextInt(50), RAND.nextInt(200), RandomStringUtils.randomAlphanumeric(2, 10)));
			} catch (VersionParsingException e) {
				throw new AssertionError("Generated version is not semantic", e);
			}
		}

		@Override
		public String getType() {
			return "test";
		}

		@Override
		public String getId() {
			return this.id;
		}

		@Override
		public Version getVersion() {
			return this.version;
		}

		@Override
		public Collection<ModDependency> getDepends() {
			return Collections.emptyList();
		}

		@Override
		public Collection<ModDependency> getRecommends() {
			return Collections.emptyList();
		}

		@Override
		public Collection<ModDependency> getSuggests() {
			return Collections.emptyList();
		}

		@Override
		public Collection<ModDependency> getConflicts() {
			return Collections.emptyList();
		}

		@Override
		public Collection<ModDependency> getBreaks() {
			return Collections.emptyList();
		}

		@Override
		public String getName() {
			return this.getId();
		}

		@Override
		public String getDescription() {
			return this.description;
		}

		@Override
		public Collection<Person> getAuthors() {
			return Collections.emptyList();
		}

		@Override
		public Collection<Person> getContributors() {
			return Collections.emptyList();
		}

		@Override
		public ContactInformation getContact() {
			return ContactInformation.EMPTY;
		}

		@Override
		public Collection<String> getLicense() {
			return Collections.emptyList();
		}

		@Override
		public Optional<String> getIconPath(int size) {
			return Optional.empty();
		}

		@Override
		public boolean containsCustomElement(String key) {
			return false;
		}

		@Override
		public JsonElement getCustomElement(String key) {
			return null;
		}

		@Override
		public boolean containsCustomValue(String key) {
			return false;
		}

		@Override
		public CustomValue getCustomValue(String key) {
			return null;
		}
	}
}
*/
