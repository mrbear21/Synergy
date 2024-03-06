package me.synergy.modules;

import java.time.Duration;
import java.util.List;

import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;

import me.synergy.brains.Synergy;

public class OpenAi {
	
    public List<CompletionChoice> newPrompt(String args) {
    	
        OpenAiService service = new OpenAiService(Synergy.getConfig().getString("openai.token"), Duration.ofSeconds(30));

        CompletionRequest completionRequest = CompletionRequest.builder()
                .model(Synergy.getConfig().getString("openai.model"))
                .prompt(args)
                .maxTokens(512)
                .build();
        List<CompletionChoice> choices = service.createCompletion(completionRequest).getChoices();

        service.shutdownExecutor();
        
        return choices;
    }
}