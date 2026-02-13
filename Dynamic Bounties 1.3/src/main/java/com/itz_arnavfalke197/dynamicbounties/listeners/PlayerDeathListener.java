package com.itz_arnavfalke197.dynamicbounties.listeners;

import com.itz_arnavfalke197.dynamicbounties.DynamicBounties;
import com.itz_arnavfalke197.dynamicbounties.managers.BountyManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
   private final DynamicBounties plugin = DynamicBounties.getInstance();
   private final Economy economy;
   private final BountyManager bountyManager;

   public PlayerDeathListener() {
      this.economy = this.plugin.getEconomy();
      this.bountyManager = this.plugin.getBountyManager();
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      Player victim = event.getEntity();
      Player killer = victim.getKiller();
      if (killer != null && !killer.equals(victim)) {
         BountyManager.Bounty bounty = this.bountyManager.getBounty(victim.getUniqueId());
         if (bounty != null) {
            double amount = bounty.getAmount();
            Player host = this.plugin.getServer().getPlayer(bounty.getHost());
            if (host == null || this.plugin.getConfig().getBoolean("bounty.allow_negative_balance", true) || !(this.economy.getBalance(host) < amount)) {
               if (host != null) {
                  this.economy.withdrawPlayer(host, amount);
               }

               this.economy.depositPlayer(killer, amount);
               this.bountyManager.removeBounty(victim.getUniqueId());
               this.plugin.getStatsManager().addMoneyEarned(killer.getUniqueId(), amount);
               this.plugin.getStatsManager().addKill(killer.getUniqueId());
               if (this.plugin.getConfig().getBoolean("bounty.broadcast_claim", true)) {
                  String message = this.plugin.getConfig().getString("bounty.broadcast_message", "&cA bounty of {amount} coins has been claimed on {victim}!");
                  message = message.replace("{amount}", String.valueOf(amount)).replace("{victim}", victim.getName());
                  this.plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
               }

            }
         }
      }
   }
}
