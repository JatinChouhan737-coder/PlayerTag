package com.example.playertags.listeners;

import com.example.playertags.PlayerTags;
import com.example.playertags.models.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player join/quit events for tag persistence
 */
public class PlayerListener implements Listener {
    
    private final PlayerTags plugin;
    
    public PlayerListener(PlayerTags plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Apply saved tag when player joins
        Tag tag = plugin.getTagManager().getPlayerTag(event.getPlayer());
        if (tag != null) {
            plugin.getTagManager().applyTag(event.getPlayer(), tag);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Save player data on quit
        plugin.getTagManager().saveAllPlayerTags();
    }
}