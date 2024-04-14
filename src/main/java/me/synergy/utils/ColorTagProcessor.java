package me.synergy.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import me.synergy.brains.Synergy;

public class ColorTagProcessor {

    public static void main(String[] args) {
        String dividedJson = processColorTags("");
        System.out.println(dividedJson);
    }

    public static String processColorTags(String jsonString) {
        try {
            jsonString = Utils.convertToJsonIfNeeded(jsonString);
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            JsonObject text = new JsonObject();
            		   text.addProperty("text", jsonObject.get("text").getAsString());
            JsonArray extraArray = Utils.insertJsonElementIntoArray(0, text, jsonObject.getAsJsonArray("extra"));;
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
        } catch (Exception c) {
            Synergy.getLogger().error(c.getLocalizedMessage());
        }
        return removeColorTags(jsonString);
    }

    public static List<String> findSpecialTags(String text) {
        List<String> specialTags = new ArrayList<>();
        Pattern pattern = Pattern.compile("<#[0-9a-fA-F]{6}>");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            specialTags.add(matcher.group());
        }
        return specialTags;
    }

	private static String lastColor = null;
	
    public static void splitColoredText(JsonObject object, JsonArray dividedExtra) {
        String text = object.get("text").getAsString();
        List<String> specialTags = findSpecialTags(text);
        if (specialTags.size() == 0) {
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
	                obj.addProperty("text", removeColorTags(firstPart));
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
                remains.addProperty("text", removeColorTags(text));
                if (lastColor != null) {
                	remains.addProperty("color", lastColor.substring(1, lastColor.length() - 1));
                }
                inheritProperties(object, remains);
                dividedExtra.add(remains);
            }
        }
    }

    public static void inheritProperties(JsonObject source, JsonObject target) {
        for (String key : source.keySet()) {
            if (!key.equals("text") && !key.equals("color")) {
                target.add(key, source.get(key));
            }
        }
    }

    public static String removeColorTags(String string) {
        Pattern pattern = Pattern.compile("<#[A-Fa-f0-9]{6}>");
        Matcher matcher = pattern.matcher(string);
        return matcher.replaceAll("");
    }
}