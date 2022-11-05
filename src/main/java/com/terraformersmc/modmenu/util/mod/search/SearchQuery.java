package com.terraformersmc.modmenu.util.mod.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.search.term.BadgeSearchTerm;
import com.terraformersmc.modmenu.util.mod.search.term.ConfigurableSearchTerm;
import com.terraformersmc.modmenu.util.mod.search.term.ContentSearchTerm;
import com.terraformersmc.modmenu.util.mod.search.term.SearchTerm;
import com.terraformersmc.modmenu.util.mod.search.term.TermData;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;

public class SearchQuery {
	private static final Pattern TERM_SEPARATOR = Pattern.compile("(\\s+|$)");

	private final List<SearchTerm> terms;

	protected SearchQuery(List<SearchTerm> terms) {
		this.terms = terms;
	}

	protected OrderedText provideRenderText(int start, int length) {
		List<OrderedText> texts = new ArrayList<>();
		int end = start + length;

		for (SearchTerm term : this.terms) {
			TermData data = term.getData();
			String string = data.getContentWithWhitespace();
	
			int termStart = data.getStart();
			int termEnd = termStart + string.length();

			if (termEnd < start || termStart > end) {
				continue;
			} else if (termEnd > end) {
				string = string.substring(0, end - termStart);
			} else if (start > termStart) {
				string = string.substring(start - termStart);
			}

			Style style = term.getStyle();
			texts.add(OrderedText.styledForwardsVisitedString(string, style));

			start += string.length();
		}

		return OrderedText.concat(texts);
	}

	protected boolean matches(Mod mod) {
		for (SearchTerm term : this.terms) {
			if (!term.matches(mod)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "SearchQuery{terms=" + this.terms + "}";
	}

	protected static SearchQuery parse(String string, ModsScreen screen) {
		List<SearchTerm> terms = new ArrayList<>();

		Matcher matcher = TERM_SEPARATOR.matcher(string);
		int start = 0;

		while (matcher.find()) {
			TermData data = TermData.of(matcher, string, start);
			start = matcher.end();

			SearchTerm term = SearchQuery.parseTerm(data, screen);
			terms.add(term);
		}

		return new SearchQuery(terms);
	}

	protected static SearchTerm parseTerm(TermData data, ModsScreen screen) {
		String content = data.getContent();

		if (content.startsWith("@")) {
			String keyword = content.substring(1);

			BadgeSearchTerm term = getBadgeFromKeyword(data);
			if (term != null) {
				return term;
			} else if (SearchQuery.isKeyword(keyword, "modmenu.searchTerms.configurable")) {
				return new ConfigurableSearchTerm(data, screen);
			}
		}

		return new ContentSearchTerm(data);
	}

	protected static BadgeSearchTerm getBadgeFromKeyword(String keyword, TermData data) {
		for (Mod.Badge badge : Mod.Badge.values()) {
			if (isKeyword(keyword, badge.getSearchTerms())) {
				return new BadgeSearchTerm(data, badge);
			}
		}
		return null;
	}

	protected static boolean isKeyword(String keyword, String translationKey) {
		if (translationKey == null) return false;

		String translated = I18n.translate(translationKey);

		for (String option : translated.split(" ")) {
			if (keyword.equals(option)) {
				return true;
			}
		}

		return false;
	}
}
