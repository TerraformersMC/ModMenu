package com.terraformersmc.modmenu.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.AbstractList;
import java.util.List;

@Mixin(targets = "net.minecraft.client.gui.widget.EntryListWidget$Entries")
public abstract class MixinEntryListWidgetEntries extends AbstractList {

	@Shadow
	@Final
	private List<?> entries;

	@Override
	public void clear() {
		entries.clear();
	}
}
