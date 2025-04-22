package ru.rexlite;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
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
        cooldownMessage = config.getString("cooldown-message");

        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("CooldownManager has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CooldownManager has been disabled!");
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String fullCommand = event.getMessage().substring(1);
        String[] commandParts = fullCommand.split(" ");

        String mainCommand = commandParts[0].toLowerCase();
        if (config.exists("commands." + mainCommand)) {
            String subCommand = null;
            if (commandParts.length > 1) {
                subCommand = commandParts[1].toLowerCase();
            }
            int cooldownTime = getCooldown(mainCommand, subCommand);
            String cooldownKey = player.getName() + ":" + mainCommand + (subCommand != null ? ":" + subCommand : "");
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
    /**
     * Retrieves the cooldown time for a command or subcommand.
     *
     * @param mainCommand The main command (e.g., "warp")
     * @param subCommand  The subcommand (e.g., "vip"), can be null
     * @return The cooldown time in seconds
     */
    private int getCooldown(String mainCommand, String subCommand) {
        // If a subcommand exists and is defined in the configuration, use its cooldown time
        if (subCommand != null && config.exists("commands." + mainCommand + ".subcommands." + subCommand)) {
            return config.getInt("commands." + mainCommand + ".subcommands." + subCommand, 0);
        } else {
            // Otherwise, use the cooldown time of the main command
            return config.getInt("commands." + mainCommand + ".cooldown", 0);
        }
    }
}
