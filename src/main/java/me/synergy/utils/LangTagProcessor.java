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
	            String defaultTranslation = LocalesManager.getLocales().get(LocalesManager.getDefaultLanguage()).getOrDefault(translationKey, translationKey);
	            String translatedText = locales.getOrDefault(translationKey, defaultTranslation);;
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
    	
    	return removeLangTags(input);
    }

    public static String processPortableLangTags(String string, String language) {
    	try {
            String langContentRegex = "<lang>(.*?)</lang>";
            Pattern langPattern = Pattern.compile(langContentRegex, Pattern.DOTALL);
            Matcher langMatcher = langPattern.matcher(string);
            while (langMatcher.find()) {
                String langContent = langMatcher.group(1);
                String regex = "<" + language + ">(.*?)</" + language + ">";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(langContent);

                String replacement;
                if (matcher.find()) {
                    replacement = matcher.group(1);
                } else {
                    replacement = "";
                }
                string = string.replace(langMatcher.group(0), replacement);
            }
            return string;
        } catch (Exception c) {
    		Synergy.getLogger().error(c.getLocalizedMessage());
    	}
    	return string;
    }

    public static String removeLangTags(String string) {
    	return string.replaceAll("<lang>(.*?)</lang>", "");
    }
    
}
