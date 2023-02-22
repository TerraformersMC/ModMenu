package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.util.compat.MCCompat;
import com.terraformersmc.modmenu.util.compat.MCVersions;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ModMenuMixinPlugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {

	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		String versionedPart = mixinClassName.split("\\.mixin\\.")[1];

		if (versionedPart.contains(".")) {
			String[] parts = versionedPart.split("\\.");

			parts = Arrays.copyOf(parts, parts.length - 1);

			for (String part : parts) {
				if (!MCVersions.canApplyMixin(part)) return false;
			}
		}

		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}
}
