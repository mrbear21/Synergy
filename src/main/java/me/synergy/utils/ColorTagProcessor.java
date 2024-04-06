package me.synergy.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.synergy.brains.Synergy;

public class ColorTagProcessor {
	
    public static String processColorTags(String inputJson) {
    	try {
	        JsonObject input = JsonParser.parseString(Utils.convertToJsonIfNeeded(inputJson)).getAsJsonObject();
	        String originalText = input.get("text").getAsString();
	        JsonArray extraArray = new JsonArray();
	        lastColorCode = null;
	        processElement(originalText, input, extraArray);
	        input.remove("extra");
	        input.addProperty("text", originalText);
	        input.add("extra", extraArray);
	        return inputJson.length() > input.toString().length() ? inputJson : input.toString();
		} catch (Exception c) {
			Synergy.getLogger().error(c.getLocalizedMessage());
		}
		
		return inputJson.replaceAll("<#([0-9a-fA-F]{6})>", "");
    }

    private static String lastColorCode = null;
    
    private static void processElement(String originalText, JsonObject properties, JsonArray extraArray) {
        Pattern pattern = Pattern.compile("<#([0-9a-fA-F]{6})>([^<]*)");
        Matcher matcher = pattern.matcher(originalText);

        int lastIndex = 0;
        while (matcher.find()) {
        	lastColorCode = matcher.group(1);
            String text = matcher.group(2);

            JsonObject newObj = new JsonObject();
            newObj.addProperty("text", text);
            newObj.addProperty("color", "#" + lastColorCode);

            if (properties != null) {
                for (String key : properties.keySet()) {
                    if (!key.equals("text")) {
                        newObj.add(key, properties.get(key));
                    }
                }
            }

            extraArray.add(newObj);
            lastIndex = matcher.end();
        }

        if (lastIndex < originalText.length()) {
            String remainingText = originalText.substring(lastIndex);
            JsonObject newObj = new JsonObject();
            newObj.addProperty("text", remainingText);
            if (lastColorCode != null) {
            	newObj.addProperty("color", "#" + lastColorCode);
            }
            if (properties != null) {
                for (String key : properties.keySet()) {
                    if (!key.equals("text")) {
                        newObj.add(key, properties.get(key));
                    }
                }
            }

            extraArray.add(newObj);
        }

        if (properties != null && properties.has("extra")) {
            JsonArray nestedExtras = properties.getAsJsonArray("extra");
            nestedExtras.forEach(element -> {
                if (element.isJsonObject()) {
                    JsonObject nestedProperties = element.getAsJsonObject();
                    String nestedText = nestedProperties.get("text").getAsString();
                    processElement(nestedText, nestedProperties, extraArray);
                } else if (element.isJsonPrimitive()) {
                    String nestedText = element.getAsString();
                    processElement(nestedText, null, extraArray);
                }
            });
        }
    }

    public static void main(String[] args) {
        String inputJson = "{\"text\":\"\",\"extra\":[{\"text\":\"<#f8a5c2>Earn by voting for the server: <#ea8685>&n/vote\",\"obfuscated\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"bold\":false}]}";
        System.out.println(processColorTags(inputJson));
    }

}
