package me.synergy.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import me.synergy.brains.Synergy;
import net.md_5.bungee.api.ChatColor;

public class Color {

    private static String lastColor = null;

    public static void main(String[] args) {
        String dividedJson = processColorTags("");
        System.out.println(dividedJson);
    }

    public static String processColors(String json, String theme) {
        try {
        	json = processThemeTags(json, theme);
            json = customColorCodes(json);
            if (json.contains("<#")) {
            	if (Utils.isValidJson(json)) {
            		json = processColorTags(json);
            	} else {
            		json = processColorTags(Utils.convertToJson(json));
            	}
            }
            json = processColorReplace(json, theme);
            json = ChatColor.translateAlternateColorCodes('&', json);
        } catch (Exception e) {
            Synergy.getLogger().error("Error while processing colors: " + e.getLocalizedMessage());
        }
        return json;
    }

    public static String processLegacyColors(String string, String theme) {
        string = customColorCodes(string);
        string = processThemeTags(string, theme);
        Pattern pattern = Pattern.compile("<(#[A-Fa-f0-9]{6})>");
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            string = string.replace(matcher.group(), "" + ChatColor.of(matcher.group(1)));
        }
        string = ChatColor.translateAlternateColorCodes('&', string);
        return string;
    }

    public static String removeColor(String json) {
    	json = processThemeTags(json, "default");
    	json = processColorReplace(json, "default");
        Pattern pattern = Pattern.compile("<#[A-Fa-f0-9]{6}>");
        Matcher matcher = pattern.matcher(json);
        return ChatColor.stripColor(matcher.replaceAll(""));
    }

    public static String processThemeTags(String input, String theme) {
        for (String t : new String[]{theme, "default"}) {
            try {
                for (Entry<String, Object> c : Synergy.getConfig().getConfigurationSection("localizations.color-themes." + t).entrySet()) {
                    String hexCode = Synergy.getConfig().getString("localizations.color-themes." + t + "." + c.getKey());
                    input = input.replace("<" + c.getKey() + ">", hexCode);
                }
            } catch (Exception e) {
            	Synergy.getLogger().error("Error while processing theme tags: " + e.getLocalizedMessage());
            }
        }
        return input;
    }

    private static String processColorReplace(String input, String theme) {
        try {
            for (Entry<String, Object> c : Synergy.getConfig().getConfigurationSection("localizations.color-replace").entrySet()) {
                String hexCode = processThemeTags(Synergy.getConfig().getString("localizations.color-replace." + c.getKey()), theme);
                input = input.replace("\"" + c.getKey() + "\"", "\"" + hexCode.substring(1, 8) + "\"");
            }
        } catch (Exception e) {
        	Synergy.getLogger().error("Error while processing color replace: " + e.getLocalizedMessage());
        }
        return input;
    }

    public static String customColorCodes(String sentence) {
        Set<String> codes = Synergy.getSpigot().getConfig().getConfigurationSection("chat-manager.custom-color-tags").getKeys(false);
        for (String c : codes) {
            sentence = sentence.replace(c, Synergy.getSpigot().getConfig().getString("chat-manager.custom-color-tags." + c));
        }
        return sentence;
    }

    private static String processColorTags(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            JsonObject text = new JsonObject();
            text.addProperty("text", jsonObject.get("text").getAsString());
            JsonArray extraArray = Utils.insertJsonElementIntoArray(0, text, jsonObject.getAsJsonArray("extra"));
            JsonArray dividedExtra = new JsonArray();
            lastColor = null;
            for (JsonElement element : extraArray) {
                if (element instanceof JsonPrimitive && ((JsonPrimitive) element).isString()) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("text", ((JsonPrimitive) element).getAsString());
                    splitColoredText(obj, dividedExtra);
                } else if (element instanceof JsonObject) {
                    JsonObject obj = (JsonObject) element;
                    if (obj.get("text") != null && obj.get("text").isJsonPrimitive()) {
                        splitColoredText(obj, dividedExtra);
                    } else {
                        dividedExtra.add(element);
                    }
                }
            }
            jsonObject.remove("text");
            jsonObject.remove("extra");
            jsonObject.addProperty("text", "");
            jsonObject.add("extra", dividedExtra);
            return jsonObject.toString();
        } catch (Exception e) {
        	Synergy.getLogger().error("Error while processing color tags: " + e.getLocalizedMessage());
        }

        return removeColor(json);
    }

    private static List<String> findSpecialTags(String text) {
        List<String> specialTags = new ArrayList<>();
        Pattern pattern = Pattern.compile("<#[0-9a-fA-F]{6}>");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            specialTags.add(matcher.group());
        }
        return specialTags;
    }

    private static void splitColoredText(JsonObject object, JsonArray dividedExtra) {
        String text = object.get("text").getAsString();
        List<String> specialTags = findSpecialTags(text);
        if (specialTags.isEmpty()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("text", text);
            if (lastColor != null) {
                obj.addProperty("color", lastColor.substring(1, lastColor.length() - 1));
            }
            inheritProperties(object, obj);
            dividedExtra.add(obj);
        } else {
            for (String color : specialTags) {
                int startIndex = text.indexOf(color);
                int endIndex = text.indexOf(color, startIndex);
                if (endIndex == -1) {
                    endIndex = text.length();
                }
                String firstPart = text.substring(0, startIndex);
                String lastPart = text.substring(endIndex);

                if (!firstPart.isEmpty()) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("text", removeColor(firstPart));
                    if (lastColor != null) {
                        obj.addProperty("color", lastColor.substring(1, lastColor.length() - 1));
                    }
                    inheritProperties(object, obj);
                    dividedExtra.add(obj);
                }

                lastColor = color;
                text = lastPart;
            }
            if (!text.isEmpty()) {
                JsonObject remains = new JsonObject();
                remains.addProperty("text", removeColor(text));
                if (lastColor != null) {
                    remains.addProperty("color", lastColor.substring(1, lastColor.length() - 1));
                }
                inheritProperties(object, remains);
                dividedExtra.add(remains);
            }
        }
    }

    private static void inheritProperties(JsonObject source, JsonObject target) {
        for (String key : source.keySet()) {
            if (!key.equals("text") && !key.equals("color")) {
                target.add(key, source.get(key));
            }
        }
    }

}