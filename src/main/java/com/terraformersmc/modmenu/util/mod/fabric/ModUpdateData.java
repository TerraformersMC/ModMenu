package com.terraformersmc.modmenu.util.mod.fabric;

import net.fabricmc.loader.api.metadata.ModMetadata;

public abstract class ModUpdateData {
	public ModMetadata metadata;
	public String modFileName;

	public ModUpdateData(ModMetadata metadata, String modFileName) {
		this.metadata = metadata;
		this.modFileName = modFileName;
	}
}
