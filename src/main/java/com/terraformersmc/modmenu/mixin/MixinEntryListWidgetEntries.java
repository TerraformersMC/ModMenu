package com.terraformersmc.modmenu.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.AbstractList;
import java.util.List;

@Mixin(targets = "net.minecraft.client.gui.components.AbstractSelectionList$TrackedList")
public abstract class MixinEntryListWidgetEntries extends AbstractList {

	@Shadow
	@Final
	private List<?> delegate;

	@Override
	public void clear() {
		delegate.clear();
	}
}
