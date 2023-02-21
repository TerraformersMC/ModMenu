package com.terraformersmc.modmenu.util.compat;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.DescriptionListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.OrderedText;

public abstract class DescriptionListWidgetHelper<T extends EntryListWidget.Entry<T> & DescriptionListWidget.DescriptionListEntry<T>> {
	public abstract DescriptionListWidget<T> createDescriptionListWidget(MinecraftClient client, int width, int height, int top, int bottom, int entryHeight, ModsScreen parent);
	public abstract CreditsScreen createCreditsScreen(boolean endCredits, ModsScreen parent);
	public abstract T createDescriptionEntry(OrderedText text, DescriptionListWidget<T> widget, ModsScreen parent);
	public abstract T createDescriptionEntry(OrderedText text, DescriptionListWidget<T> widget, int indent, ModsScreen parent);
	public abstract T createMojangCreditsEntry(OrderedText text, DescriptionListWidget<T> widget, ModsScreen parent);
	public abstract T createLinkEntry(OrderedText text, String link, DescriptionListWidget<T> widget, int indent, ModsScreen parent);
	public abstract T createLinkEntry(OrderedText text, String link, DescriptionListWidget<T> widget, ModsScreen parent);
}
