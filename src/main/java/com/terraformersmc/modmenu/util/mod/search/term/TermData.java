package com.terraformersmc.modmenu.util.mod.search.term;

import java.util.regex.Matcher;

public class TermData {
	private final String content;
	private final String contentWithWhitespace;
	private final int start;

	public TermData(String content, String contentWithWhitespace, int start) {
		this.content = content;
		this.contentWithWhitespace = contentWithWhitespace;
		this.start = start;
	}

	public String getContent() {
		return this.content;
	}

	public String getContentWithWhitespace() {
		return this.contentWithWhitespace;
	}

	public int getStart() {
		return this.start;
	}

	@Override
	public String toString() {
		return "TermData{'" + this.contentWithWhitespace + "', " + this.start + "}";
	}

	public static TermData of(Matcher matcher, String string, int start) {
		String content = string.substring(start, matcher.start());
		String contentWithWhitespace = string.substring(start, matcher.end());

		return new TermData(content, contentWithWhitespace, start);
	}
}
