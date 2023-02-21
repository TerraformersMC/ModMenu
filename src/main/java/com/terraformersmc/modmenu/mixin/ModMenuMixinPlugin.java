package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.util.compat.MCCompat;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

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
		if (mixinClassName.contains("mc1194plus")) {
			return MCCompat.after23w03a;
		} else if (mixinClassName.contains("mc1193plus")) {
			return MCCompat.after22w43a;
		} else if (mixinClassName.contains("mc1193minus")) {
			return !MCCompat.after22w43a;
		} else if (mixinClassName.contains("mc1193")) {
			return MCCompat.after22w43a && !MCCompat.after23w03a;
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
