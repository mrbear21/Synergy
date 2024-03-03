package me.synergy.modules;

import java.time.Duration;
import java.util.List;

import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;

import me.synergy.brain.BrainSpigot;
import me.synergy.brain.BrainVelocity;

public class OpenAi {
	
	private BrainSpigot spigot;
	private BrainVelocity bungee;
	private String token = null;
	
    public OpenAi(BrainSpigot spigot) {
    	this.spigot = spigot;
    	this.token = this.spigot.getConfig().getString("openai.token");
	}
    public OpenAi(BrainVelocity bungee) {
    	this.bungee = bungee;
    	this.token = this.bungee.getConfig().getString("openai.token");
	}

    public List<CompletionChoice> newPrompt(String args) {
    	
        OpenAiService service = new OpenAiService(token, Duration.ofSeconds(30));

        CompletionRequest completionRequest = CompletionRequest.builder()
                .model("text-davinci-003")
                .prompt(args)
                .echo(true)
                .user("testing")
                .n(3)
                .build();
        List<CompletionChoice> choices = service.createCompletion(completionRequest).getChoices();

        service.shutdownExecutor();
        
        return choices;
    }
}