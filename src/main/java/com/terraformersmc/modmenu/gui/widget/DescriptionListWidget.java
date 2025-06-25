package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.screen.option.CreditsAndAttributionScreen;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.*;

public class DescriptionListWidget extends EntryListWidget<DescriptionListWidget.DescriptionEntry> {
	private static final Text HAS_UPDATE_TEXT = Text.translatable("modmenu.hasUpdate");
	private static final Text EXPERIMENTAL_TEXT = Text.translatable("modmenu.experimental").formatted(Formatting.GOLD);
	private static final Text DOWNLOAD_TEXT = Text.translatable("modmenu.downloadLink").formatted(Formatting.BLUE).formatted(Formatting.UNDERLINE);
	private static final Text CHILD_HAS_UPDATE_TEXT = Text.translatable("modmenu.childHasUpdate");
	private static final Text LINKS_TEXT = Text.translatable("modmenu.links");
	private static final Text SOURCE_TEXT = Text.translatable("modmenu.source").formatted(Formatting.BLUE).formatted(Formatting.UNDERLINE);
	private static final Text LICENSE_TEXT = Text.translatable("modmenu.license");
	private static final Text VIEW_CREDITS_TEXT = Text.translatable("modmenu.viewCredits").formatted(Formatting.BLUE).formatted(Formatting.UNDERLINE);
	private static final Text CREDITS_TEXT = Text.translatable("modmenu.credits");

	private final ModsScreen parent;
	private final TextRenderer textRenderer;
	private Mod selectedMod = null;

	public DescriptionListWidget(
		MinecraftClient client,
		int width,
		int height,
		int y,
		int itemHeight,
		DescriptionListWidget copyFrom,
		ModsScreen parent
	) {
		super(client, width, height, y, itemHeight);
		this.parent = parent;
		this.textRenderer = client.textRenderer;

		if (copyFrom != null) {
			updateSelectedMod(copyFrom.selectedMod);
			setScrollY(copyFrom.getScrollY());
		}

		if (parent.getSelectedEntry() != null) {
			updateSelectedMod(parent.getSelectedEntry().getMod());
		}
	}

	@Override
	public DescriptionEntry getSelectedOrNull() {
		return null;
	}

	@Override
	public int getRowWidth() {
		return this.width - 10;
	}

	@Override
	protected int getScrollbarX() {
		return this.width - 6 + this.getX();
	}

	@Override
	public void appendClickableNarrations(NarrationMessageBuilder builder) {
		if (selectedMod != null) {
			builder.put(
				NarrationPart.TITLE,
				selectedMod.getTranslatedName() + " " + selectedMod.getPrefixedVersion());
		}
	}

	private void rebuildUI() {
		if (selectedMod == null) {
			return;
		}

		DescriptionEntry emptyEntry = new DescriptionEntry(OrderedText.EMPTY);
		int wrapWidth = getRowWidth() - 5;

		Mod mod = selectedMod;
		Text description = mod.getFormattedDescription();
		if (!description.getString().isEmpty()) {
			for (OrderedText line : textRenderer.wrapLines(description, wrapWidth)) {
				this.addEntry(new DescriptionEntry(line));
			}
		}

		if (ModMenuConfig.UPDATE_CHECKER.getValue() && !ModMenuConfig.DISABLE_UPDATE_CHECKER.getValue()
			.contains(mod.getId())) {
			UpdateInfo updateInfo = mod.getUpdateInfo();
			if (updateInfo != null && updateInfo.isUpdateAvailable()) {
				this.addEntry(emptyEntry);

				int index = 0;
				for (OrderedText line : textRenderer.wrapLines(HAS_UPDATE_TEXT, wrapWidth - 11)) {
					DescriptionEntry entry = new DescriptionEntry(line);
					if (index == 0) {
						entry.setUpdateTextEntry();
					}

					this.addEntry(entry);
					index += 1;
				}

				for (OrderedText line : textRenderer.wrapLines(EXPERIMENTAL_TEXT, wrapWidth - 16)) {
					this.addEntry(new DescriptionEntry(line, 8));
				}

				Text updateMessage = updateInfo.getUpdateMessage();
				String downloadLink = updateInfo.getDownloadLink();
				if (updateMessage == null) {
					updateMessage = DOWNLOAD_TEXT;
				} else {
					if (downloadLink != null) {
						updateMessage = updateMessage.copy()
							.formatted(Formatting.BLUE)
							.formatted(Formatting.UNDERLINE);
					}
				}

				for (OrderedText line : textRenderer.wrapLines(updateMessage, wrapWidth - 16)) {
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
				for (OrderedText line : textRenderer.wrapLines(CHILD_HAS_UPDATE_TEXT, wrapWidth - 11)) {
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

			for (OrderedText line : textRenderer.wrapLines(LINKS_TEXT, wrapWidth)) {
				this.addEntry(new DescriptionEntry(line));
			}

			if (sourceLink != null) {
				int indent = 8;
				for (OrderedText line : textRenderer.wrapLines(SOURCE_TEXT, wrapWidth - 16)) {
					this.addEntry(new LinkEntry(line, sourceLink, indent));
					indent = 16;
				}
			}

			links.forEach((key, value) -> {
				int indent = 8;
				for (OrderedText line : textRenderer.wrapLines(Text.translatable(key)
						.formatted(Formatting.BLUE)
						.formatted(Formatting.UNDERLINE),
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

			for (OrderedText line : textRenderer.wrapLines(LICENSE_TEXT, wrapWidth)) {
				this.addEntry(new DescriptionEntry(line));
			}

			for (String license : licenses) {
				int indent = 8;
				for (OrderedText line : textRenderer.wrapLines(Text.literal(license), wrapWidth - 16)) {
					this.addEntry(new DescriptionEntry(line, indent));
					indent = 16;
				}
			}
		}

		if (!ModMenuConfig.HIDE_MOD_CREDITS.getValue()) {
			if ("minecraft".equals(mod.getId())) {
				this.addEntry(emptyEntry);

				for (OrderedText line : textRenderer.wrapLines(VIEW_CREDITS_TEXT, wrapWidth)) {
					this.addEntry(new MojangCreditsEntry(line));
				}
			} else if (!"java".equals(mod.getId())) {
				var credits = mod.getCredits();

				if (!credits.isEmpty()) {
					this.addEntry(emptyEntry);

					for (OrderedText line : textRenderer.wrapLines(CREDITS_TEXT, wrapWidth)) {
						this.addEntry(new DescriptionEntry(line));
					}

					var iterator = credits.entrySet().iterator();

					while (iterator.hasNext()) {
						int indent = 8;

						var role = iterator.next();


						for (var line : textRenderer.wrapLines(this.creditsRoleText(role.getKey()),
							wrapWidth - 16
						)) {
							this.addEntry(new DescriptionEntry(line, indent));
							indent = 16;
						}

						for (var contributor : role.getValue()) {
							indent = 16;

							for (var line : textRenderer.wrapLines(Text.literal(contributor), wrapWidth - 24)) {
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
		setScrollY(-Double.MAX_VALUE);
		rebuildUI();
	}

	@Override
	public void renderList(DrawContext drawContext, int mouseX, int mouseY, float delta) {
		this.enableScissor(drawContext);
		super.renderList(drawContext, mouseX, mouseY, delta);
		drawContext.disableScissor();
	}

	private Text creditsRoleText(String roleName) {
		// Replace spaces and dashes in role names with underscores if they exist
		// Notably Quilted Fabric API does this with FabricMC as "Upstream Owner"
		var translationKey = roleName.replaceAll("[ -]", "_").toLowerCase();
		// Add an s to the default untranslated string if it ends in r since this
		// Fixes common role names people use in English (e.g. Author -> Authors)
		var fallback = roleName.endsWith("r") ? roleName + "s" : roleName;
		return Text.translatableWithFallback("modmenu.credits.role." + translationKey, fallback).append(Text.literal(":"));
	}

	protected class DescriptionEntry extends ElementListWidget.Entry<DescriptionEntry> {
		protected OrderedText text;
		protected int indent;
		public boolean updateTextEntry = false;

		public DescriptionEntry(OrderedText text, int indent) {
			this.text = text;
			this.indent = indent;
		}

		public DescriptionEntry(OrderedText text) {
			this(text, 0);
		}

		public DescriptionEntry setUpdateTextEntry() {
			this.updateTextEntry = true;
			return this;
		}

		@Override
		public void render(
			DrawContext drawContext,
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

			drawContext.drawTextWithShadow(textRenderer, text, x + indent, y, 0xFFAAAAAA);
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			if (!super.isMouseOver(mouseX, mouseY)) {
				return false;
			}

			int width = DescriptionListWidget.this.textRenderer.getWidth(text);

			if (updateTextEntry) {
				width += 11;
			}

			double relativeX = mouseX - DescriptionListWidget.this.getRowLeft() - indent;

			return relativeX >= 0 && relativeX < width;
		}

		@Override
		public List<? extends Element> children() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return Collections.emptyList();
		}
	}

	protected class MojangCreditsEntry extends DescriptionEntry {
		public MojangCreditsEntry(OrderedText text) {
			super(text);
		}

		@Override
		public boolean mouseClicked(Click click, boolean doubleClick) {
			if (isMouseOver(click.x(), click.y())) {
				client.setScreen(new MinecraftCredits());
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

		public LinkEntry(OrderedText text, String link, int indent) {
			super(text, indent);
			this.link = link;
		}

		public LinkEntry(OrderedText text, String link) {
			this(text, link, 0);
		}

		@Override
		public boolean mouseClicked(Click click, boolean doubleClick) {
			if (isMouseOver(click.x(), click.y())) {
				client.setScreen(new ConfirmLinkScreen((open) -> {
					if (open) {
						Util.getOperatingSystem().open(link);
					}
					client.setScreen(parent);
				}, link, false));
			}

			return super.mouseClicked(click, doubleClick);
		}
	}

    protected class MailableContactEntry extends DescriptionEntry {
        private final String email;

        public MailableContactEntry(OrderedText text, String email, int indent) {
            super(text, indent);
            this.email = email;
        }

        public MailableContactEntry(OrderedText text, String link) {
            this(text, link, 0);
        }

        @Override
        public void render(DrawContext drawContext, int mouseX, int mouseY, boolean isSelected, float delta) {
            super.render(drawContext, mouseX, mouseY, isSelected, delta);
            drawContext.drawTextWithShadow(textRenderer, Text.literal(" ").append(Text.literal("✉")), this.getContentX() + indent + textRenderer.getWidth(text) + 1, this.getContentY(), 0xFFAAAAAA);
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            if (isMouseOver(click.x(), click.y())) {
                client.setScreen(new ConfirmLinkScreen((open) -> {
                    if (open) {
                        Util.getOperatingSystem().open("mailto:" + email);
                    }
                    client.setScreen(parent);
                }, "mailto:" + email, false));
            }

            return super.mouseClicked(click, doubled);
        }
    }
}
