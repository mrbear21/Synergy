package me.synergy.objects;

import me.synergy.brains.Synergy;
import me.synergy.handlers.ChatHandler.MessageSource;

public class Chat implements MessageSource {

    private String chat;
    private static final String BASE_PATH = "chat-manager.chats.";

    public Chat(String chat) {
        this.chat = chat;
    }
    
    private String getConfigValue(String key, String defaultValue) {
        return Synergy.getConfig().getString(BASE_PATH + chat + key, defaultValue);
    }

    private int getConfigIntValue(String key, int defaultValue) {
        return Synergy.getConfig().getInt(BASE_PATH + chat + key, defaultValue);
    }

    private boolean getConfigBooleanValue(String key, boolean defaultValue) {
        return Synergy.getConfig().getBoolean(BASE_PATH + chat + key, defaultValue);
    }

    public int getRadius() {
        return getConfigIntValue(".radius", 500);
    }

    public String getName() {
        return chat;
    }

    public boolean isEnabled() {
        return getConfigBooleanValue(".enabled", false);
    }

    public Discord getDiscord() {
        return new Discord(chat);
    }

    public String getColor() {
        return getConfigValue(".color", "<#ffffff>");
    }

    public String getPermission() {
        return getConfigValue(".permission", null);
    }

    public String getSymbol() {
        return getConfigValue(".symbol", null);
    }

    public String getTag() {
        return getConfigValue(".tag", "");
    }

    public static class Discord implements MessageSource {
        private String chat;
        private static final String DISCORD_PATH = ".discord";

        public Discord(String chat) {
            this.chat = chat;
        }

        private String getConfigValue(String key, String defaultValue) {
            return Synergy.getConfig().getString(BASE_PATH + chat + DISCORD_PATH + key, defaultValue);
        }

        public String getColor() {
            return getConfigValue(".color", "<#ffffff>");
        }

        public String getPermission() {
            return getConfigValue(".permission", null);
        }

        public String getChannel() {
            return getConfigValue(".channel", "000");
        }

        public String getTag() {
            return getConfigValue(".tag", "");
        }
    }
}
