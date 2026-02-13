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
import org.bukkit.entity.Player;

public class BountyManager {
   private final Map<UUID, BountyManager.Bounty> bounties = new HashMap();
   private final File bountyFile;
   private final FileConfiguration bountyConfig;

   public BountyManager() {
      DynamicBounties plugin = DynamicBounties.getInstance();
      this.bountyFile = new File(plugin.getDataFolder(), "bounties.yml");
      this.bountyConfig = YamlConfiguration.loadConfiguration(this.bountyFile);
      this.loadBounties();
   }

   public void setBounty(Player victim, Player host, double amount) {
      UUID victimUUID = victim.getUniqueId();
      this.bounties.put(victimUUID, new BountyManager.Bounty(host.getUniqueId(), amount));
      this.saveBounties();
   }

   public BountyManager.Bounty getBounty(UUID victimUUID) {
      return (BountyManager.Bounty)this.bounties.get(victimUUID);
   }

   public void removeBounty(UUID victimUUID) {
      this.bounties.remove(victimUUID);
      this.saveBounties();
   }

   public Map<UUID, BountyManager.Bounty> getAllBounties() {
      return new HashMap(this.bounties);
   }

   private void loadBounties() {
      if (this.bountyConfig.contains("bounties")) {
         Iterator var1 = this.bountyConfig.getConfigurationSection("bounties").getKeys(false).iterator();

         while(var1.hasNext()) {
            String key = (String)var1.next();
            UUID victimUUID = UUID.fromString(key);
            UUID hostUUID = UUID.fromString(this.bountyConfig.getString("bounties." + key + ".host"));
            double amount = this.bountyConfig.getDouble("bounties." + key + ".amount");
            this.bounties.put(victimUUID, new BountyManager.Bounty(hostUUID, amount));
         }
      }

   }

   private void saveBounties() {
      this.bountyConfig.set("bounties", (Object)null);
      Iterator var1 = this.bounties.entrySet().iterator();

      while(var1.hasNext()) {
         Entry<UUID, BountyManager.Bounty> entry = (Entry)var1.next();
         String key = ((UUID)entry.getKey()).toString();
         this.bountyConfig.set("bounties." + key + ".host", ((BountyManager.Bounty)entry.getValue()).getHost().toString());
         this.bountyConfig.set("bounties." + key + ".amount", ((BountyManager.Bounty)entry.getValue()).getAmount());
      }

      try {
         this.bountyConfig.save(this.bountyFile);
      } catch (IOException var4) {
         var4.printStackTrace();
      }

   }

   public static class Bounty {
      private final UUID host;
      private final double amount;

      public Bounty(UUID host, double amount) {
         this.host = host;
         this.amount = amount;
      }

      public UUID getHost() {
         return this.host;
      }

      public double getAmount() {
         return this.amount;
      }
   }
}
