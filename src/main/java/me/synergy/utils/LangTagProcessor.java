package me.synergy.utils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.synergy.brains.Synergy;
import me.synergy.modules.LocalesManager;

public class LangTagProcessor {
	
    public static String processLangTags(String input, String language) {
    	try {
	        String keyPattern = "<lang>(.*?)</lang>";
	        Pattern pattern = Pattern.compile(keyPattern);
	        Matcher matcher = pattern.matcher(input);
	
	        StringBuffer outputBuffer = new StringBuffer();
	        boolean found = false;
	        while (matcher.find()) {
	            found = true;
	            String translationKeyWithArgs = matcher.group(1);
	            String translationKey = translationKeyWithArgs.replaceAll("<arg>(.*?)</arg>", "");
	            HashMap<String, String> locales = LocalesManager.getLocales().getOrDefault(language, new HashMap<>());
	            String translatedText = locales.getOrDefault(translationKey, LocalesManager.getLocales().get(LocalesManager.getDefaultLanguage()).getOrDefault(translationKey, translationKey));
	            if (translatedText != null) {
	                String argsPattern = "<arg>(.*?)</arg>";
	                Pattern argsPatternPattern = Pattern.compile(argsPattern);
	                Matcher argsMatcher = argsPatternPattern.matcher(translationKeyWithArgs);
	                while (argsMatcher.find()) {
	                    String arg = argsMatcher.group(1);
	                    translatedText = translatedText.replaceFirst("%ARGUMENT%", arg);
	                }
	                matcher.appendReplacement(outputBuffer, translatedText);
	            }
	        }
	        matcher.appendTail(outputBuffer);
	        
	        if (found) {
	            return processLangTags(outputBuffer.toString(), language);
	        } else {
	            return outputBuffer.toString();
	        }
        } catch (Exception c) {
    		Synergy.getLogger().error(c.getLocalizedMessage());
    	}
    	
    	return input.replaceAll("<lang>(.*?)</lang>", "");
    }
    
}
