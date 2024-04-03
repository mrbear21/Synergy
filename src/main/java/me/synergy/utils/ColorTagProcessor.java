package me.synergy.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class ColorTagProcessor {

	public static String replaceFirstAndLastQuotes(String input) {
	    if (input == null || input.isEmpty() || input.length() < 2) {
	        return input;
	    }
	    if (input.charAt(0) == '"') {
	        input = input.substring(1);
	    }
	    int lastIndex = input.length() - 1;
	    if (input.charAt(lastIndex) == '"') {
	        input = input.substring(0, lastIndex);
	    }
	    return input;
	}
	
    public static String convertToJsonIfNeeded(String input) {
    	input = replaceFirstAndLastQuotes(input);
        try {
            JsonElement jsonElement = JsonParser.parseString(input);
            if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
                return input;
            }
        } catch (JsonSyntaxException e) {
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", "");
        JsonArray extraArray = new JsonArray();
        extraArray.add(input);
        jsonObject.add("extra", extraArray);
        return jsonObject.toString();
    }
	
    public static String processColorTags(String inputJson) {
        JsonObject input = JsonParser.parseString(convertToJsonIfNeeded(inputJson)).getAsJsonObject();
        JsonArray extraArray = new JsonArray();

        processTextWithColorTag(input.get("text").getAsString(), extraArray);

        JsonArray originalExtra = input.getAsJsonArray("extra");
        for (JsonElement element : originalExtra) {
            if (element.isJsonObject()) {
                processJsonObject(element.getAsJsonObject(), extraArray);
            } else if (element.isJsonPrimitive()) {
                processTextWithColorTag(element.getAsString(), extraArray);
            }
        }

        JsonObject output = new JsonObject();
        output.addProperty("text", "");
        output.add("extra", extraArray);

        return output.toString();
    }

    private static void processTextWithColorTag(String text, JsonArray extraArray) {
        String[] splitText = text.split("<#");
        for (String part : splitText) {
            if (!part.isEmpty()) {
                JsonObject newObj = new JsonObject();
                if (part.contains(">")) {
                    String[] colorSplit = part.split(">");
                    String colorCode = colorSplit[0];
                    String remainingText = colorSplit.length > 1 ? colorSplit[1] : "";
                    newObj.addProperty("text", remainingText);
                    newObj.addProperty("color", "#" + colorCode);
                } else {
                    newObj.addProperty("text", part);
                }
                extraArray.add(newObj);
            }
        }
    }

    private static void processJsonObject(JsonObject obj, JsonArray extraArray) {
        String text = obj.get("text").getAsString();
        String[] splitText = text.split("<#");
        for (String part : splitText) {
            JsonObject newObj = new JsonObject();
            if (part.contains(">")) {
                String[] colorSplit = part.split(">");
                String colorCode = colorSplit[0];
                String remainingText = colorSplit.length > 1 ? colorSplit[1] : "";
                newObj.addProperty("text", remainingText);
                newObj.addProperty("color", "#" + colorCode);
            } else {
                newObj.addProperty("text", part);
            }
            for (String key : obj.keySet()) {
                if (!key.equals("text")) {
                    newObj.add(key, obj.get(key));
                }
            }
            extraArray.add(newObj);
        }
    }

    public static void main(String[] args) {
        String inputJson = "{\"text\":\"\",\"extra\":[{\"text\":\"<#f8a5c2>Earn by voting for the server: <#ea8685>&n/vote\",\"obfuscated\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"bold\":false}]}";
        System.out.println(processColorTags(inputJson));
    }
}
