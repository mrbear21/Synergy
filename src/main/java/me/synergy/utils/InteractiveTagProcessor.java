package me.synergy.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Sound;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import me.synergy.brains.Synergy;
import me.synergy.objects.BreadMaker;

public class InteractiveTagProcessor {
	
    public static void main(String[] args) {
        String dividedJson = processInteractiveTags("{\"text\":\"\",\"extra\":[{\"text\":\"Hello\",\"color\":\"#a4b0be\"}]}", null);
        System.out.println(dividedJson);
    }

    private static BreadMaker bread;
    
    public static String processInteractiveTags(String jsonString, BreadMaker bread) {
    	InteractiveTagProcessor.bread = bread;
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
 
    	return removeInteractiveTags(jsonString);
    }
    
    public static String removeInteractiveTags(String string) {
    	return string.replaceAll("<interactive>(.*?)</interactive>", "");
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
            inheritProperties(object, obj);
            dividedExtra.add(obj);

            JsonObject interactiveObject = new JsonObject();
            
            Pattern pattern = Pattern.compile("<url>(.*?)</url>");
            Matcher matcher = pattern.matcher(interactivePart);
            if (matcher.find()) {
                JsonObject urlEvent = new JsonObject();
            	urlEvent.addProperty("action", "open_url");
            	urlEvent.addProperty("value", Utils.processColors(matcher.group(1)));
            	interactiveObject.add("clickEvent", urlEvent);
                interactivePart = interactivePart.replaceAll(pattern.pattern(), "");
            }
            
            pattern = Pattern.compile("<sound>(.*?)</sound>");
            matcher = pattern.matcher(interactivePart);
            if (matcher.find()) {
            	if (bread != null && bread.isOnline()) {
            		Utils.playSound(Sound.valueOf(matcher.group(1).toUpperCase()), Bukkit.getPlayer(bread.getUniqueId()));
            	}
            	//JsonObject soundEvent = new JsonObject();
            	//soundEvent.addProperty("sound", matcher.group(1));
            	//interactiveObject.add("soundEvent", soundEvent);
                interactivePart = interactivePart.replaceAll(pattern.pattern(), "");

            } 
            
            pattern = Pattern.compile("<hover>(.*?)</hover>");
            matcher = pattern.matcher(interactivePart);
            if (matcher.find()) {
                JsonObject hoverEvent = new JsonObject();
                hoverEvent.addProperty("action", "show_text");
                hoverEvent.addProperty("value", Utils.processColors(matcher.group(1)));
                interactivePart = interactivePart.replaceAll(pattern.pattern(), "");
            	interactiveObject.add("hoverEvent", hoverEvent);
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