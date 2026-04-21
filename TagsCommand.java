package com.example.playertags.commands;

import com.example.playertags.PlayerTags;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles /tags command and subcommands
 */
public class TagsCommand implements CommandExecutor, TabCompleter {
    
    private final PlayerTags plugin;
    
    public TagsCommand(PlayerTags plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("playertags.use")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use tags!");
                return true;
            }
            
            plugin.getGuiManager().openTagMenu(player);
            return true;
        }
        
        // Handle subcommands
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("playertags.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to reload the config!");
                return true;
            }
            
            plugin.reload();
            sender.sendMessage(ChatColor.GREEN + "PlayerTags configuration reloaded!");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender);
            return true;
        }
        
        sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /tags help for help.");
        return true;
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== PlayerTags Help ===");
        sender.sendMessage(ChatColor.YELLOW + "/tags " + ChatColor.WHITE + "- Open tag selection GUI");
        
        if (sender.hasPermission("playertags.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/tags reload " + ChatColor.WHITE + "- Reload configuration");
        }
        
        sender.sendMessage(ChatColor.YELLOW + "/tags help " + ChatColor.WHITE + "- Show this help message");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            if (sender.hasPermission("playertags.admin")) {
                completions.add("reload");
            }
            completions.add("help");
        }
        
        return completions;
    }
}