package me.synergy.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import me.synergy.brains.Synergy;

public class InteractiveTagProcessor {
	
    public static void main(String[] args) {
        String dividedJson = processInteractiveTags("{\"text\":\"\",\"extra\":[{\"text\":\"Execute command <interactive>Separated text</interactive> to teleport\",\"color\":\"#a4b0be\"}]}");
        System.out.println(dividedJson);
        dividedJson = processInteractiveTags("{\"text\":\"\",\"extra\":[\"Hello <interactive>world</interactive>!\",\"<interactive>Another interactive</interactive> text\"]}");
        System.out.println(dividedJson);
    }

    public static String processInteractiveTags(String jsonString) {
    	try {
	    	jsonString = Utils.convertToJsonIfNeeded(jsonString);
	        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
	        JsonArray extraArray = jsonObject.getAsJsonArray("extra");
	        JsonArray dividedExtra = new JsonArray();
	
	        for (JsonElement element : extraArray) {
	            if (element instanceof JsonPrimitive && ((JsonPrimitive) element).isString()) {
	                JsonObject text = new JsonObject();
	                text.addProperty("text", ((JsonPrimitive) element).getAsString());
	                splitInteractiveText(text, dividedExtra);
	            } else if (element instanceof JsonObject) {
	                JsonObject obj = (JsonObject) element;
	                splitInteractiveText(obj, dividedExtra);
	            }
	        }
	
	        jsonObject.remove("extra");
	        jsonObject.add("extra", dividedExtra);

	        return jsonObject.toString();
    	} catch (Exception c) {
    		Synergy.getLogger().error(c.getLocalizedMessage());
    	}
    	
    	return jsonString.replaceAll("<interactive>(.*?)</interactive>", "");
    } 

    public static void splitInteractiveText(JsonObject object, JsonArray dividedExtra) {
        String text = object.get("text").getAsString();
        int startIndex = text.indexOf("<interactive>");
        if (startIndex == -1) {
            JsonObject obj = new JsonObject();
            obj.addProperty("text", text);
            inheritProperties(object, obj);
            dividedExtra.add(obj);
        } else {
            int endIndex = text.indexOf("</interactive>");
            String firstPart = text.substring(0, startIndex);
            String interactivePart = text.substring(startIndex + "<interactive>".length(), endIndex);
            String lastPart = text.substring(endIndex + "</interactive>".length());

            JsonObject obj = new JsonObject();
            obj.addProperty("text", firstPart);
            dividedExtra.add(obj);

            JsonObject interactiveObject = new JsonObject();
            
            JsonObject hoverEvent = new JsonObject();
            Pattern pattern = Pattern.compile("<hover>(.*?)</hover>");
            Matcher matcher = pattern.matcher(interactivePart);
            if (matcher.find()) {
            	Synergy.getLogger().info(matcher.group(1));
                hoverEvent.addProperty("action", "show_text");
                hoverEvent.addProperty("value", Utils.processColors(matcher.group(1)));
                interactivePart = interactivePart.replaceAll(pattern.pattern(), "");
            	interactiveObject.add("hoverEvent", hoverEvent);
            	Synergy.getLogger().info(interactivePart);
            }
            
            JsonObject commmandEvent = new JsonObject();
            pattern = Pattern.compile("<command>(.*?)</command>");
            matcher = pattern.matcher(interactivePart);
            if (matcher.find()) {
            	Synergy.getLogger().info(matcher.group(1));
            	commmandEvent.addProperty("action", "run_command");
                commmandEvent.addProperty("value", matcher.group(1));
                interactivePart = interactivePart.replaceAll(pattern.pattern(), "");
            	interactiveObject.add("clickEvent", commmandEvent);
            	Synergy.getLogger().info(interactivePart);
            }
            
            interactiveObject.addProperty("text", interactivePart);
            inheritProperties(object, interactiveObject);
            
            dividedExtra.add(interactiveObject);

            JsonObject remains = new JsonObject();
            remains.addProperty("text", lastPart);
            inheritProperties(object, remains);
            splitInteractiveText(remains, dividedExtra);
        }
    }
    
    public static void inheritProperties(JsonObject source, JsonObject target) {
        for (String key : source.keySet()) {
            if (!key.equals("text")) {
                target.add(key, source.get(key));
            }
        }
    }
}