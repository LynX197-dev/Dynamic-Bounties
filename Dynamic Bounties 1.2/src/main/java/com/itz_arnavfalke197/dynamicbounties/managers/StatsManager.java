package com.itz_arnavfalke197.dynamicbounties.managers;

import com.itz_arnavfalke197.dynamicbounties.DynamicBounties;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class StatsManager {
   private final Map<UUID, Double> moneyEarned = new HashMap();
   private final Map<UUID, Integer> kills = new HashMap();
   private final File statsFile;
   private final FileConfiguration statsConfig;

   public StatsManager() {
      DynamicBounties plugin = DynamicBounties.getInstance();
      this.statsFile = new File(plugin.getDataFolder(), "stats.yml");
      this.statsConfig = YamlConfiguration.loadConfiguration(this.statsFile);
      this.loadStats();
   }

   public void addMoneyEarned(UUID player, double amount) {
      this.moneyEarned.put(player, (Double)this.moneyEarned.getOrDefault(player, 0.0D) + amount);
      this.saveStats();
   }

   public void addKill(UUID player) {
      this.kills.put(player, (Integer)this.kills.getOrDefault(player, 0) + 1);
      this.saveStats();
   }

   public Map<UUID, Double> getMoneyEarned() {
      return new HashMap(this.moneyEarned);
   }

   public Map<UUID, Integer> getKills() {
      return new HashMap(this.kills);
   }

   private void loadStats() {
      Iterator var1;
      String key;
      UUID uuid;
      if (this.statsConfig.contains("moneyEarned")) {
         var1 = this.statsConfig.getConfigurationSection("moneyEarned").getKeys(false).iterator();

         while(var1.hasNext()) {
            key = (String)var1.next();
            uuid = UUID.fromString(key);
            double amount = this.statsConfig.getDouble("moneyEarned." + key);
            this.moneyEarned.put(uuid, amount);
         }
      }

      if (this.statsConfig.contains("kills")) {
         var1 = this.statsConfig.getConfigurationSection("kills").getKeys(false).iterator();

         while(var1.hasNext()) {
            key = (String)var1.next();
            uuid = UUID.fromString(key);
            int count = this.statsConfig.getInt("kills." + key);
            this.kills.put(uuid, count);
         }
      }

   }

   private void saveStats() {
      this.statsConfig.set("moneyEarned", (Object)null);
      Iterator var1 = this.moneyEarned.entrySet().iterator();

      Entry entry;
      while(var1.hasNext()) {
         entry = (Entry)var1.next();
         this.statsConfig.set("moneyEarned." + ((UUID)entry.getKey()).toString(), entry.getValue());
      }

      this.statsConfig.set("kills", (Object)null);
      var1 = this.kills.entrySet().iterator();

      while(var1.hasNext()) {
         entry = (Entry)var1.next();
         this.statsConfig.set("kills." + ((UUID)entry.getKey()).toString(), entry.getValue());
      }

      try {
         this.statsConfig.save(this.statsFile);
      } catch (IOException var3) {
         var3.printStackTrace();
      }

   }
}
