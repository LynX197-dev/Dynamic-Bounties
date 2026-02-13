package com.itz_arnavfalke197.dynamicbounties.commands;

import com.itz_arnavfalke197.dynamicbounties.DynamicBounties;
import com.itz_arnavfalke197.dynamicbounties.managers.BountyManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class BountyCommand implements CommandExecutor, TabCompleter {
   private final DynamicBounties plugin = DynamicBounties.getInstance();
   private final Economy economy;
   private final BountyManager bountyManager;

   public BountyCommand() {
      this.economy = this.plugin.getEconomy();
      this.bountyManager = this.plugin.getBountyManager();
   }

   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage("Only players can use this command.");
         return true;
      }

      Player player = (Player) sender;
      String prefix = this.plugin.getConfig().getString("bounty.prefix", "&8[&cDynamicBounties&8] ");

      if (args.length == 0) {
         player.sendMessage(
               ChatColor.translateAlternateColorCodes('&', prefix + "&cUsage: /bounty <set|list|info|cancel|top>"));
         return true;
      }

      String subCommand = args[0].toLowerCase();

      switch (subCommand) {
         case "set":
            this.handleSet(player, args, prefix);
            break;
         case "list":
            this.handleList(player, prefix);
            break;
         case "info":
            this.handleInfo(player, args, prefix);
            break;
         case "cancel":
            this.handleCancel(player, args, prefix);
            break;
         case "top":
            this.handleTop(player, args, prefix);
            break;
         default:
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cUnknown subcommand."));
            break;
      }

      return true;
   }

   @Override
   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (args.length == 1) {
         return Arrays.asList("set", "list", "info", "cancel", "top").stream()
               .filter(s -> s.startsWith(args[0].toLowerCase()))
               .collect(Collectors.toList());
      }

      if (args.length == 2) {
         String sub = args[0].toLowerCase();
         if (sub.equals("set") || sub.equals("info") || sub.equals("cancel")) {
            return null; // Shows online players
         }
         if (sub.equals("top")) {
            return Arrays.asList("money", "kills").stream()
                  .filter(s -> s.startsWith(args[1].toLowerCase()))
                  .collect(Collectors.toList());
         }
      }

      return new ArrayList<>();
   }

   private void handleSet(Player player, String[] args, String prefix) {
      if (args.length < 3) {
         player.sendMessage(
               ChatColor.translateAlternateColorCodes('&', prefix + "&cUsage: /bounty set <player> <amount>"));
      } else {
         Player victim = Bukkit.getPlayer(args[1]);
         if (victim == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cPlayer not found."));
         } else if (victim.equals(player)) {
            player.sendMessage(
                  ChatColor.translateAlternateColorCodes('&', prefix + "&cYou cannot set a bounty on yourself."));
         } else {
            double amount;
            try {
               amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
               player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cInvalid amount."));
               return;
            }

            double min = this.plugin.getConfig().getDouble("bounty.min_bounty", 100.0D);
            double max = this.plugin.getConfig().getDouble("bounty.max_bounty", 100000.0D);
            if (!(amount < min) && !(amount > max)) {
               if (!this.plugin.getConfig().getBoolean("bounty.allow_negative_balance", true)
                     && this.economy.getBalance(player) < amount) {
                  player.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', prefix + "&cYou don't have enough money."));
               } else {
                  this.economy.withdrawPlayer(player, amount);
                  this.bountyManager.setBounty(victim, player, amount);
                  player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        prefix + "&aBounty set on " + victim.getName() + " for " + amount + " coins."));
               }
            } else {
               player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                     prefix + "&cAmount must be between " + min + " and " + max + "."));
            }
         }
      }
   }

   private void handleList(Player player, String prefix) {
      Map<UUID, BountyManager.Bounty> bounties = this.bountyManager.getAllBounties();
      if (bounties.isEmpty()) {
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cNo active bounties."));
      } else {
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cActive Bounties:"));
         for (Entry<UUID, BountyManager.Bounty> entry : bounties.entrySet()) {
            Player victim = Bukkit.getPlayer(entry.getKey());
            String name = (victim != null) ? victim.getName() : Bukkit.getOfflinePlayer(entry.getKey()).getName();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                  "&c- " + name + " → " + entry.getValue().getAmount() + " coins"));
         }
      }
   }

   private void handleInfo(Player player, String[] args, String prefix) {
      if (args.length < 2) {
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cUsage: /bounty info <player>"));
      } else {
         Player target = Bukkit.getPlayer(args[1]);
         if (target == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cPlayer not found."));
         } else {
            BountyManager.Bounty bounty = this.bountyManager.getBounty(target.getUniqueId());
            if (bounty == null) {
               player.sendMessage(
                     ChatColor.translateAlternateColorCodes('&', prefix + "&cNo bounty on " + target.getName() + "."));
            } else {
               player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                     prefix + "&cBounty on " + target.getName() + ": " + bounty.getAmount() + " coins"));
            }
         }
      }
   }

   private void handleCancel(Player player, String[] args, String prefix) {
      if (args.length < 2) {
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cUsage: /bounty cancel <player>"));
      } else {
         Player target = Bukkit.getPlayer(args[1]);
         if (target == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cPlayer not found."));
         } else {
            BountyManager.Bounty bounty = this.bountyManager.getBounty(target.getUniqueId());
            if (bounty != null && bounty.getHost().equals(player.getUniqueId())) {
               double amount = bounty.getAmount();
               double feePercent = this.plugin.getConfig().getDouble("bounty.cancel_fee_percent", 5.0D);
               double fee = amount * (feePercent / 100.0D);
               double refund = amount - fee;
               this.economy.depositPlayer(player, refund);
               this.bountyManager.removeBounty(target.getUniqueId());
               player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                     prefix + "&aBounty cancelled. Refunded " + refund + " coins (fee: " + fee + ")."));
            } else {
               player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                     prefix + "&cYou don't have a bounty on " + target.getName() + "."));
            }
         }
      }
   }

   private void handleTop(Player player, String[] args, String prefix) {
      if (args.length < 2) {
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cUsage: /bounty top <money|kills>"));
         return;
      }

      String type = args[1].toLowerCase();

      if (type.equals("money")) {
         if (!this.plugin.getConfig().getBoolean("leaderboards.enable_money", true)) {
            player.sendMessage(
                  ChatColor.translateAlternateColorCodes('&', prefix + "&cMoney leaderboard is disabled."));
            return;
         }

         Map<UUID, Double> moneyEarned = this.plugin.getStatsManager().getMoneyEarned();
         List<Entry<UUID, Double>> sortedMoney = moneyEarned.entrySet().stream()
               .sorted(Entry.<UUID, Double>comparingByValue().reversed())
               .limit(10L).collect(Collectors.toList());

         player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cTop Bounty Earners:"));
         int rank = 1;
         for (Entry<UUID, Double> entry : sortedMoney) {
            String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                  "&c" + rank + ". " + (name != null ? name : "Unknown") + " → " + entry.getValue() + " coins"));
            rank++;
         }
      } else if (type.equals("kills")) {
         if (!this.plugin.getConfig().getBoolean("leaderboards.enable_kills", true)) {
            player.sendMessage(
                  ChatColor.translateAlternateColorCodes('&', prefix + "&cKills leaderboard is disabled."));
            return;
         }

         Map<UUID, Integer> kills = this.plugin.getStatsManager().getKills();
         List<Entry<UUID, Integer>> sortedKills = kills.entrySet().stream()
               .sorted(Entry.<UUID, Integer>comparingByValue().reversed())
               .limit(10L).collect(Collectors.toList());

         player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cTop Hunters:"));
         int rank = 1;
         for (Entry<UUID, Integer> entry : sortedKills) {
            String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                  "&c" + rank + ". " + (name != null ? name : "Unknown") + " → " + entry.getValue() + " kills"));
            rank++;
         }
      } else {
         player.sendMessage(
               ChatColor.translateAlternateColorCodes('&', prefix + "&cUse /bounty top money or /bounty top kills"));
      }
   }
}