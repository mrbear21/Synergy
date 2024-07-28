package me.synergy.objects;

import me.synergy.utils.Color;
import me.synergy.utils.Interactive;
import me.synergy.utils.Translation;
import me.synergy.utils.Utils;

public class Locale {

	String string;
	String language;

	public Locale(String string, String language) {
		this.language = language;
		this.string = Translation.translate(string, language);
	}

	public Locale setExecuteInteractive(BreadMaker bread) {
		string = Interactive.processInteractive(string);
		Interactive.executeInteractive(string, bread);
		return this;
	}

	public String getColored(String theme) {
		string = Interactive.removeInteractiveTags(string);
		string = Color.processColors(string, theme);
		return string;
	}

	public String getLegacyColored(String theme) {
		string = Utils.isValidJson(string) ? Utils.extractText(string) : string;
		string = Interactive.removeInteractiveTags(string);
		string = Color.processLegacyColors(string, theme);
		return string;
	}

	public String getStripped() {
		string = Utils.isValidJson(string) ? Utils.extractText(string) : string;
		string = Color.removeColor(string);
		string = Interactive.removeInteractiveTags(string);
		return string;
	}


}
