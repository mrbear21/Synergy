package me.synergy.objects;

import me.clip.placeholderapi.PlaceholderAPI;
import me.synergy.brains.Synergy;
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
		if (Synergy.isRunningSpigot()) {
			Synergy.getSpigot().executeInteractive(string, bread);
		}
		return this;
	}

	public Locale setPlaceholders(BreadMaker bread) {
		if (Synergy.isDependencyAvailable("PlaceholderAPI")) {
			string = Utils.replacePlaceholderOutputs(Synergy.getSpigot().getOfflinePlayerByUniqueId(bread.getUniqueId()), string);
			string = PlaceholderAPI.setPlaceholders(Synergy.getSpigot().getOfflinePlayerByUniqueId(bread.getUniqueId()), string);
		}
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
		string = Translation.removeAllTags(string);
		string = Utils.isValidJson(string) ? Utils.extractText(string) : string;
		string = Color.removeColor(string);
		string = Interactive.removeInteractiveTags(string);
		return string;
	}


}
