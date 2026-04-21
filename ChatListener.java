package com.example.playertags.listeners;

import com.example.playertags.PlayerTags;
import com.example.playertags.models.Tag;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Handles chat formatting with tags
 */
public class ChatListener implements Listener {
    
    private final PlayerTags plugin;
    
    public ChatListener(PlayerTags plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Tag tag = plugin.getTagManager().getPlayerTag(player);
        
        String format = event.getFormat();
        
        if (tag != null) {
            // Add tag prefix and suffix to chat
            String prefix = tag.getPrefix();
            String suffix = tag.getSuffix();
            
            // Replace {DISPLAYNAME} placeholder
            String displayName = prefix + player.getName() + suffix;
            
            // Handle PlaceholderAPI if enabled
            if (plugin.getConfigManager().usePlaceholderAPI() && 
                Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                displayName = PlaceholderAPI.setPlaceholders(player, displayName);
            }
            
            // Update chat format
            format = format.replace("%1$s", displayName);
            event.setFormat(format);
        }
        
        // Apply PlaceholderAPI to the message if enabled
        if (plugin.getConfigManager().usePlaceholderAPI() && 
            Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            event.setMessage(PlaceholderAPI.setPlaceholders(player, event.getMessage()));
        }
    }
}