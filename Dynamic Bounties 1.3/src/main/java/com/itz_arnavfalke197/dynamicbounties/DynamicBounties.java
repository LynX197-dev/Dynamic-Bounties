package com.itz_arnavfalke197.dynamicbounties;

import com.itz_arnavfalke197.dynamicbounties.commands.BountyCommand;
import com.itz_arnavfalke197.dynamicbounties.commands.DBCommand;
import com.itz_arnavfalke197.dynamicbounties.listeners.PlayerDeathListener;
import com.itz_arnavfalke197.dynamicbounties.managers.BountyManager;
import com.itz_arnavfalke197.dynamicbounties.managers.StatsManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class DynamicBounties extends JavaPlugin {
   private static DynamicBounties instance;
   private Economy economy;
   private BountyManager bountyManager;
   private StatsManager statsManager;

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      if (!this.setupEconomy()) {
         this.getLogger().severe("Vault economy not found! Disabling plugin.");
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         this.bountyManager = new BountyManager();
         this.statsManager = new StatsManager();

         // REGISTER COMMAND AND COMPLETER
         BountyCommand bountyCommand = new BountyCommand();
         this.getCommand("bounty").setExecutor(bountyCommand);
         this.getCommand("bounty").setTabCompleter(bountyCommand); // This activates the tab-complete feature

         DBCommand dbCommand = new DBCommand();
         this.getCommand("db").setExecutor(dbCommand);
         this.getCommand("db").setTabCompleter(dbCommand);

         // REGISTER EVENT LISTENERS

         this.getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
         this.getLogger().info("DynamicBounties has been enabled!");
      //ASCII BANNER
         Bukkit.getConsoleSender().sendMessage(AsciiBanner.DESIGN_CREDIT);
      }
   }

   public void onDisable() {
      this.getLogger().info("DynamicBounties has been disabled!");
   }

   private boolean setupEconomy() {
      if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
         return false;
      } else {
         RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
            return false;
         } else {
            this.economy = (Economy) rsp.getProvider();
            return this.economy != null;
         }
      }
   }

   public static DynamicBounties getInstance() {
      return instance;
   }

   public Economy getEconomy() {
      return this.economy;
   }

   public BountyManager getBountyManager() {
      return this.bountyManager;
   }

   public StatsManager getStatsManager() {
      return this.statsManager;
   }
}
