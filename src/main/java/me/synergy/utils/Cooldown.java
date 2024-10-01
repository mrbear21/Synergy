package me.synergy.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import me.synergy.brains.Synergy;

public class Cooldown {

    private static HashMap<UUID, List<String>> cooldownMap = new HashMap<>();
    private UUID uuid;

    public Cooldown(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean hasCooldown(String name) {
        return cooldownMap.getOrDefault(uuid, new ArrayList<>()).contains(name);
    }

    public void setCooldown(String name, int seconds) {
        List<String> cooldownList = cooldownMap.getOrDefault(uuid, new ArrayList<>());
        cooldownList.add(name);
        cooldownMap.put(uuid, cooldownList);

        Synergy.getSpigot().getServer().getScheduler().scheduleSyncDelayedTask(Synergy.getSpigot(), () -> {
            List<String> updatedList = cooldownMap.getOrDefault(uuid, new ArrayList<>());
            updatedList.remove(name);
            cooldownMap.put(uuid, updatedList);
        }, 20*seconds);
    }
}