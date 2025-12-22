package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;

import java.util.*;

public class DescriptionListWidget extends AbstractSelectionList<DescriptionListWidget.DescriptionEntry> {
	private static final Component HAS_UPDATE_TEXT = Component.translatable("modmenu.hasUpdate");
	private static final Component EXPERIMENTAL_TEXT = Component.translatable("modmenu.experimental").withStyle(ChatFormatting.GOLD);
	private static final Component DOWNLOAD_TEXT = Component.translatable("modmenu.downloadLink").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.UNDERLINE);
	private static final Component CHILD_HAS_UPDATE_TEXT = Component.translatable("modmenu.childHasUpdate");
	private static final Component LINKS_TEXT = Component.translatable("modmenu.links");
	private static final Component SOURCE_TEXT = Component.translatable("modmenu.source").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.UNDERLINE);
	private static final Component LICENSE_TEXT = Component.translatable("modmenu.license");
	private static final Component VIEW_CREDITS_TEXT = Component.translatable("modmenu.viewCredits").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.UNDERLINE);
	private static final Component CREDITS_TEXT = Component.translatable("modmenu.credits");

	private final ModsScreen parent;
	private final Font textRenderer;
	private Mod selectedMod = null;

	public DescriptionListWidget(
		Minecraft client,
		int width,
		int height,
		int y,
		int itemHeight,
		DescriptionListWidget copyFrom,
		ModsScreen parent
	) {
		super(client, width, height, y, itemHeight);
		this.parent = parent;
		this.textRenderer = client.font;

		if (copyFrom != null) {
			updateSelectedMod(copyFrom.selectedMod);
			setScrollAmount(copyFrom.scrollAmount());
		}

		if (parent.getSelectedEntry() != null) {
			updateSelectedMod(parent.getSelectedEntry().getMod());
		}
	}

	@Override
	public DescriptionEntry getSelected() {
		return null;
	}

	@Override
	public int getRowWidth() {
		return this.width - 10;
	}

	@Override
	protected int scrollBarX() {
		return this.width - 6 + this.getX();
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput builder) {
		if (selectedMod != null) {
			builder.add(
				NarratedElementType.TITLE,
				selectedMod.getTranslatedName() + " " + selectedMod.getPrefixedVersion());
		}
	}

	private void rebuildUI() {
		if (selectedMod == null) {
			return;
		}

		DescriptionEntry emptyEntry = new DescriptionEntry(FormattedCharSequence.EMPTY);
		int wrapWidth = getRowWidth() - 5;

		Mod mod = selectedMod;
		Component description = mod.getFormattedDescription();
		if (!description.getString().isEmpty()) {
			for (FormattedCharSequence line : textRenderer.split(description, wrapWidth)) {
				this.addEntry(new DescriptionEntry(line));
			}
		}

		if (ModMenuConfig.UPDATE_CHECKER.getValue() && !ModMenuConfig.DISABLE_UPDATE_CHECKER.getValue()
			.contains(mod.getId())) {
			UpdateInfo updateInfo = mod.getUpdateInfo();
			if (updateInfo != null && updateInfo.isUpdateAvailable()) {
				this.addEntry(emptyEntry);

				int index = 0;
				for (FormattedCharSequence line : textRenderer.split(HAS_UPDATE_TEXT, wrapWidth - 11)) {
					DescriptionEntry entry = new DescriptionEntry(line);
					if (index == 0) {
						entry.setUpdateTextEntry();
					}

					this.addEntry(entry);
					index += 1;
				}

				for (FormattedCharSequence line : textRenderer.split(EXPERIMENTAL_TEXT, wrapWidth - 16)) {
					this.addEntry(new DescriptionEntry(line, 8));
				}

				Component updateMessage = updateInfo.getUpdateMessage();
				String downloadLink = updateInfo.getDownloadLink();
				if (updateMessage == null) {
					updateMessage = DOWNLOAD_TEXT;
				} else {
					if (downloadLink != null) {
						updateMessage = updateMessage.copy()
							.withStyle(ChatFormatting.BLUE)
							.withStyle(ChatFormatting.UNDERLINE);
					}
				}

				for (FormattedCharSequence line : textRenderer.split(updateMessage, wrapWidth - 16)) {
					if (downloadLink != null) {
						this.addEntry(new LinkEntry(line, downloadLink, 8));
					} else {
						this.addEntry(new DescriptionEntry(line, 8));
					}
				}
			}

			if (mod.getChildHasUpdate()) {
				this.addEntry(emptyEntry);

				int index = 0;
				for (FormattedCharSequence line : textRenderer.split(CHILD_HAS_UPDATE_TEXT, wrapWidth - 11)) {
					DescriptionEntry entry = new DescriptionEntry(line);
					if (index == 0) {
						entry.setUpdateTextEntry();
					}

					this.addEntry(entry);
					index += 1;
				}
			}
		}

		Map<String, String> links = mod.getLinks();
		String sourceLink = mod.getSource();
		if ((!links.isEmpty() || sourceLink != null) && !ModMenuConfig.HIDE_MOD_LINKS.getValue()) {
			this.addEntry(emptyEntry);

			for (FormattedCharSequence line : textRenderer.split(LINKS_TEXT, wrapWidth)) {
				this.addEntry(new DescriptionEntry(line));
			}

			if (sourceLink != null) {
				int indent = 8;
				for (FormattedCharSequence line : textRenderer.split(SOURCE_TEXT, wrapWidth - 16)) {
					this.addEntry(new LinkEntry(line, sourceLink, indent));
					indent = 16;
				}
			}

			links.forEach((key, value) -> {
				int indent = 8;
				for (FormattedCharSequence line : textRenderer.split(Component.translatable(key)
						.withStyle(ChatFormatting.BLUE)
						.withStyle(ChatFormatting.UNDERLINE),
					wrapWidth - 16
				)) {
					this.addEntry(new LinkEntry(line, value, indent));
					indent = 16;
				}
			});
		}

		Set<String> licenses = mod.getLicense();
		if (!ModMenuConfig.HIDE_MOD_LICENSE.getValue() && !licenses.isEmpty()) {
			this.addEntry(emptyEntry);

			for (FormattedCharSequence line : textRenderer.split(LICENSE_TEXT, wrapWidth)) {
				this.addEntry(new DescriptionEntry(line));
			}

			for (String license : licenses) {
				int indent = 8;
				for (FormattedCharSequence line : textRenderer.split(Component.literal(license), wrapWidth - 16)) {
					this.addEntry(new DescriptionEntry(line, indent));
					indent = 16;
				}
			}
		}

		if (!ModMenuConfig.HIDE_MOD_CREDITS.getValue()) {
			if ("minecraft".equals(mod.getId())) {
				this.addEntry(emptyEntry);

				for (FormattedCharSequence line : textRenderer.split(VIEW_CREDITS_TEXT, wrapWidth)) {
					this.addEntry(new MojangCreditsEntry(line));
				}
			} else if (!"java".equals(mod.getId())) {
				var credits = mod.getCredits();

				if (!credits.isEmpty()) {
					this.addEntry(emptyEntry);

					for (FormattedCharSequence line : textRenderer.split(CREDITS_TEXT, wrapWidth)) {
						this.addEntry(new DescriptionEntry(line));
					}

					var iterator = credits.entrySet().iterator();

					while (iterator.hasNext()) {
						int indent = 8;

						var role = iterator.next();


						for (var line : textRenderer.split(this.creditsRoleText(role.getKey()),
							wrapWidth - 16
						)) {
							this.addEntry(new DescriptionEntry(line, indent));
							indent = 16;
						}

						for (var contributor : role.getValue()) {
							indent = 16;

							for (var line : textRenderer.split(Component.literal(contributor), wrapWidth - 24)) {
                                ContactInformation contact = mod.getContact(contributor);
                                if (contact != null && contact.get("email").isPresent()) {
                                    this.addEntry(new MailableContactEntry(line, contact.get("email").get(), indent));
                                } else {
									this.addEntry(new DescriptionEntry(line, indent));
                                }
								indent = 24;
							}
						}

						if (iterator.hasNext()) {
							this.addEntry(emptyEntry);
						}
					}
				}
			}
		}
	}

	public void updateSelectedMod(Mod mod) {
		selectedMod = mod;
		clearEntries();
		setScrollAmount(-Double.MAX_VALUE);
		rebuildUI();
	}

	@Override
	public void renderListItems(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
		this.enableScissor(drawContext);
		super.renderListItems(drawContext, mouseX, mouseY, delta);
		drawContext.disableScissor();
	}

	private Component creditsRoleText(String roleName) {
		// Replace spaces and dashes in role names with underscores if they exist
		// Notably Quilted Fabric API does this with FabricMC as "Upstream Owner"
		var translationKey = roleName.replaceAll("[ -]", "_").toLowerCase();
		// Add an s to the default untranslated string if it ends in r since this
		// Fixes common role names people use in English (e.g. Author -> Authors)
		var fallback = roleName.endsWith("r") ? roleName + "s" : roleName;
		return Component.translatableWithFallback("modmenu.credits.role." + translationKey, fallback).append(Component.literal(":"));
	}

	protected class DescriptionEntry extends ContainerObjectSelectionList.Entry<DescriptionEntry> {
		protected FormattedCharSequence text;
		protected int indent;
		public boolean updateTextEntry = false;

		public DescriptionEntry(FormattedCharSequence text, int indent) {
			this.text = text;
			this.indent = indent;
		}

		public DescriptionEntry(FormattedCharSequence text) {
			this(text, 0);
		}

		public DescriptionEntry setUpdateTextEntry() {
			this.updateTextEntry = true;
			return this;
		}

		@Override
		public void renderContent(
			GuiGraphics drawContext,
			int mouseX,
			int mouseY,
			boolean isSelected,
			float delta
		) {
			int x = this.getX();
			int y = this.getContentY();
			if (updateTextEntry) {
				UpdateAvailableBadge.renderBadge(drawContext, x + indent, y);
				x += 11;
			}

			drawContext.drawString(textRenderer, text, x + indent, y, 0xFFAAAAAA);
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			if (!super.isMouseOver(mouseX, mouseY)) {
				return false;
			}

			int width = DescriptionListWidget.this.textRenderer.width(text);

			if (updateTextEntry) {
				width += 11;
			}

			double relativeX = mouseX - DescriptionListWidget.this.getRowLeft() - indent;

			return relativeX >= 0 && relativeX < width;
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return Collections.emptyList();
		}
	}

	protected class MojangCreditsEntry extends DescriptionEntry {
		public MojangCreditsEntry(FormattedCharSequence text) {
			super(text);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
			if (isMouseOver(click.x(), click.y())) {
				minecraft.setScreen(new MinecraftCredits());
			}

			return super.mouseClicked(click, doubleClick);
		}

		class MinecraftCredits extends CreditsAndAttributionScreen {
			public MinecraftCredits() {
				super(parent);
			}
		}
	}

	protected class LinkEntry extends DescriptionEntry {
		private final String link;

		public LinkEntry(FormattedCharSequence text, String link, int indent) {
			super(text, indent);
			this.link = link;
		}

		public LinkEntry(FormattedCharSequence text, String link) {
			this(text, link, 0);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
			if (isMouseOver(click.x(), click.y())) {
				minecraft.setScreen(new ConfirmLinkScreen((open) -> {
					if (open) {
						Util.getPlatform().openUri(link);
					}
					minecraft.setScreen(parent);
				}, link, false));
			}

			return super.mouseClicked(click, doubleClick);
		}
	}

    protected class MailableContactEntry extends DescriptionEntry {
        private final String email;

        public MailableContactEntry(FormattedCharSequence text, String email, int indent) {
            super(text, indent);
            this.email = email;
        }

        public MailableContactEntry(FormattedCharSequence text, String link) {
            this(text, link, 0);
        }

        @Override
        public void renderContent(GuiGraphics drawContext, int mouseX, int mouseY, boolean isSelected, float delta) {
            super.renderContent(drawContext, mouseX, mouseY, isSelected, delta);
            drawContext.drawString(textRenderer, Component.literal(" ").append(Component.literal("✉")), this.getContentX() + indent + textRenderer.width(text) + 1, this.getContentY(), 0xFFAAAAAA);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (isMouseOver(event.x(), event.y())) {
                minecraft.setScreen(new ConfirmLinkScreen((open) -> {
                    if (open) {
                        Util.getPlatform().openUri("mailto:" + email);
                    }
                    minecraft.setScreen(parent);
                }, "mailto:" + email, false));
            }
            return super.mouseClicked(event, doubleClick);
        }
    }
}
