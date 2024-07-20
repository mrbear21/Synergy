package me.synergy.utils;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMerger{


    public static String extractAndCombineText(String jsonString) {
    	if (!Utils.isValidJson(jsonString)) {
    		jsonString = Utils.convertToJson(jsonString);
    	}
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);
            StringBuilder combinedText = new StringBuilder();

            extractText(rootNode, combinedText);

            JsonNode combinedTextNode = objectMapper.createObjectNode().put("text", combinedText.toString());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(combinedTextNode);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonString;
        }
    }

    private static void extractText(JsonNode node, StringBuilder combinedText) {
        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                if (fieldName.equals("text") || fieldName.equals("extra")) {
                    JsonNode fieldValue = node.get(fieldName);
                    if (fieldValue.isTextual()) {
                        combinedText.append(fieldValue.asText());
                    } else {
                        extractText(fieldValue, combinedText);
                    }
                } else {
                    extractText(node.get(fieldName), combinedText);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                extractText(arrayElement, combinedText);
            }
        }
    }

    public static void main(String[] args) {
        String jsonString = "{\"text\":\"<translation><en>\",\"extra\":[{\"text\":\"Set fly mode\",\"extra\":[{\"text\":\" ввімкнено \",\"extra\":[{\"text\":\".</en><uk>Встановлено режим польоту\",\"extra\":[{\"text\":\" ввімкнено\",\"extra\":[{\"text\":\".</uk><lv>Uzstādija lidošanas režīmu\",\"extra\":[{\"text\":\" ввімкнено \",\"extra\":[{\"text\":\".</lv><ru>Режим полета\",\"extra\":[{\"text\":\" ввімкнено \",\"extra\":[{\"text\":\".</ru></translation>\",\"color\":\"gold\"}],\"color\":\"red\"}],\"color\":\"gold\"}],\"color\":\"red\"}],\"color\":\"gold\"}],\"color\":\"red\"}],\"color\":\"gold\"}],\"color\":\"red\"}],\"color\":\"gold\"}]}";
        String result = extractAndCombineText(jsonString);
        System.out.println(result); // {"text":"<translation><en>Set fly mode ввімкнено .</en><uk>Встановлено режим польоту ввімкнено.</uk><lv>Uzstādija lidošanas režīmu ввімкнено .</lv><ru>Режим полета ввімкнено .</ru></translation>"}
    }
}