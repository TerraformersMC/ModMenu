package com.terraformersmc.modmenu.util.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

public enum MCVersions {
	MC1_19_4("1.19.4"),
	/**
	 * Replace ModMenuTexturedButtonWidget with new vanilla TexturedButtonWidget.
	 */
	MC23W05A("1.19.4-alpha.23.5.a"),
	/**
	 * RenderSystem.enable/disableTexture removal, GridWidget don't extend ClickableWidget anymore.
	 * CreditsScreen ctr takes an additional argument.
	 */
	MC23W03A("1.19.4-alpha.23.3.a"),
	MC1_19_3("1.19.3"),
	/**
	 * Keyboard.setRepeatEvents removal.
	 */
	MC22W46A("1.19.3-alpha.22.46.a"),
	/**
	 * Tooltip supplier changes.
	 */
	MC22W45A("1.19.3-alpha.22.45.a"),
	/**
	 * JOML, GridWidget, Tooltip changes.
	 */
	MC22W43A("1.19.3-alpha.22.43.a"),
	MC1_19_2("1.19.2"),
	MC1_19_1("1.19.1"),
	/**
	 * Narrator instance getter.
	 */
	MC1_19_1_PRE2("1.19.1-beta.2"),
	MC1_19_1_RC1("1.19.1-rc.1");

	final String mcVersion;
	MCVersions(String mcVersion) {
		this.mcVersion = mcVersion;
	}

	public boolean laterIncluded() {
		return testMCVersionPredicate(">=" + this.mcVersion);
	}

	public boolean beforeIncluded() {
		return testMCVersionPredicate("<=" + this.mcVersion);
	}

	public boolean later() {
		return testMCVersionPredicate(">" + this.mcVersion);
	}

	public boolean before() {
		return testMCVersionPredicate("<" + this.mcVersion);
	}

	public boolean match() {
		return testMCVersionPredicate(this.mcVersion);
	}

	public static boolean testMCVersionPredicate(String versionPredicates) {
		try {
			return VersionPredicate.parse(versionPredicates).test(FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion());
		} catch (VersionParsingException e) {
			return false;
		}
	}

	public static boolean canApplyMixin(String packagePart) {
		String predicate = packagePart.replace("minus", "").replace("plus", "");

		MCVersions versions = null;

		for (MCVersions version : values()) {
			if (version.name().toLowerCase().replace("_", "").equals(predicate)) {
				versions = version;
				break;
			}
		}

		if (versions != null) {
			return packagePart.endsWith("minus") ? versions.before() : packagePart.endsWith("plus") ? versions.laterIncluded() : versions.match();
		}

		System.err.println("Unable to find get mc version from \"" + packagePart + "\"");

		return false;
	}
}
