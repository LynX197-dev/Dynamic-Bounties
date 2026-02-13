package com.itz_arnavfalke197.dynamicbounties.commands;

import com.itz_arnavfalke197.dynamicbounties.DynamicBounties;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DBCommand implements CommandExecutor, TabCompleter {
   private final DynamicBounties plugin = DynamicBounties.getInstance();

   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("dynamicbounties.admin")) {
         sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
         return true;
      }

      if (args.length == 0) {
         sender.sendMessage(ChatColor.RED + "Usage: /db <reload>");
         return true;
      }

      String subCommand = args[0].toLowerCase();

      switch (subCommand) {
         case "reload":
            this.handleReload(sender);
            break;
         default:
            sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /db <reload>");
            break;
      }

      return true;
   }

   @Override
   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (args.length == 1) {
         return Arrays.asList("reload").stream()
               .filter(s -> s.startsWith(args[0].toLowerCase()))
               .collect(Collectors.toList());
      }

      return new ArrayList<>();
   }

   private void handleReload(CommandSender sender) {
      try {
         this.plugin.reloadConfig();
         sender.sendMessage(ChatColor.GREEN + "DynamicBounties configuration reloaded successfully!");
      } catch (Exception e) {
         sender.sendMessage(ChatColor.RED + "Failed to reload configuration: " + e.getMessage());
         this.plugin.getLogger().severe("Error reloading config: " + e.getMessage());
      }
   }
}