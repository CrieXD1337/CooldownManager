package ru.rexlite;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import java.util.HashMap;
import java.util.Map;

public class CooldownManager extends PluginBase implements Listener {

    private Config config;
    private Map<String, Long> cooldowns = new HashMap<>();
    private String cooldownMessage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        cooldownMessage = config.getString("cooldown-message", "§7> §cWait§e {time} §cseconds before using this command");

        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info(TextFormat.GREEN + "CooldownManager has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info(TextFormat.RED + "CooldownManager has been disabled!");
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        if (config.exists("commands." + command)) {
            int cooldownTime = config.getInt("commands." + command + ".cooldown", 0);

            String cooldownKey = player.getName() + ":" + command;

            long currentTime = System.currentTimeMillis() / 1000;
            long lastUsed = cooldowns.getOrDefault(cooldownKey, 0L);

            if (currentTime - lastUsed < cooldownTime) {
                int remainingTime = (int) (cooldownTime - (currentTime - lastUsed));
                String message = cooldownMessage.replace("{time}", String.valueOf(remainingTime));
                player.sendMessage(message);
                event.setCancelled(true);
                return;
            }

            cooldowns.put(cooldownKey, currentTime);
        }
    }
}
