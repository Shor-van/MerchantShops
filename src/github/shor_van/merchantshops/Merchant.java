package github.shor_van.merchantshops;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**Represents a merchant*/
public class Merchant
{
    public static final int displaySize = 27; //The max amount of items items to show if we would go over the max size of the inventory
    public static final Material buttonMaterial = Material.SPECTRAL_ARROW; //The material of the item use by the navigation buttons
    public static final Material errorItemMaterial = Material.BARRIER; //The material used to show a item with invalid data
    public static final String errItemToken = ChatColor.BLACK + "" + ChatColor.MAGIC + "errItem8246"; //the lore line used by the error item
    public static final String prevPageLoreToken = "Go to the previous page"; //The lore line used by the prev page navigation button
    public static final String nextPageLoreToken = "Go to the next page"; //The lore line used by the next page navigation button
    
    private String name; //The name of the merchant
    private EntityType entityType; //The entity type of the merchant
    private UUID entityUUID; //The UUID of the merchant entity
    private Location location; //The location of the merchant entity
    private List<BuyableItem> sellItems; //The items that the merchant sells
    
    /**Creates a new merchant instance
     * @param entityUUID the UUID of the entity that physical represents the merchant in the world
     * @param entityType the type of entity the merchant is represented by
     * @param displayName the display name of the merchant
     * @param location the location of the entity
     * @param sellItems the list of items that the merchant sells*/
    public Merchant(UUID entityUUID, EntityType entityType, String displayName, Location location, List<BuyableItem> sellItems)
    {
        this.entityUUID = entityUUID;
        this.entityType = entityType;
        this.name = displayName;
        this.sellItems = sellItems;
        this.location = location;
    }
    
    /**Shows the specified player up to 36 of the merchant's items, starting from specified item index
     * @param player the player to whom to show the inventory
     * @param startIdx the item index at witch to start the item list from*/
    @SuppressWarnings("deprecation")
    public void showBuyMenu(Player player, int startIdx)
    {
        Inventory buyMenu = null;
    	
        //set inventory size
        if(startIdx > 0)
            buyMenu = Bukkit.createInventory(null, 36, name);
        else if(sellItems.size() <= 9)
            buyMenu = Bukkit.createInventory(null, 9, name);
        else if(sellItems.size() <= 18)
    	    buyMenu = Bukkit.createInventory(null, 18, name);
    	else if(sellItems.size() <= 27)
    	    buyMenu = Bukkit.createInventory(null, 27, name);
    	else
    	    buyMenu = Bukkit.createInventory(null, 36, name);
    	
        //if size greater then inventory create next page button
        if(sellItems.size() - startIdx > 36)
        {
            ItemStack nextItem = new ItemStack(buttonMaterial);
            ItemMeta nextItemMeta = nextItem.getItemMeta();
    		
            nextItemMeta.setDisplayName("Page " + ((startIdx / displaySize) + 2));
    		
            List<String> nextLore = new ArrayList<>();
            nextLore.add(nextPageLoreToken);
            nextItemMeta.setLore(nextLore);
    		
            nextItem.setItemMeta(nextItemMeta);
    		
            buyMenu.setItem(buyMenu.getSize() - 1, nextItem);
        }
    	
        //if not first page create back button
        if(startIdx > 0)
        {
            ItemStack prevItem = new ItemStack(buttonMaterial);
            ItemMeta prevItemMeta = prevItem.getItemMeta();
    		
            prevItemMeta.setDisplayName("Page " + (startIdx / displaySize));
    		
            List<String> nextLore = new ArrayList<>();
            nextLore.add(prevPageLoreToken);
            prevItemMeta.setLore(nextLore);
    		
            prevItem.setItemMeta(prevItemMeta);
    		
            buyMenu.setItem(buyMenu.getSize() - 9, prevItem);
        }
    	
        //setup items
        for(int i = startIdx; i < sellItems.size(); i++)
        {
            //if would go over break
            if(i - startIdx >= displaySize && (startIdx != 0 || sellItems.size() - startIdx > 36))
                break;
    		
            //Get buyable item data
            BuyableItem buyableItem = sellItems.get(i);
    		
            //Check if item has invalid data
            if(Material.getMaterial(buyableItem.getItemKey().toUpperCase()) == null || buyableItem.getAmount() <= 0 || buyableItem.getLevelCost() <= 0 || buyableItem.hasInvalidEffects() == true || buyableItem.hasInvalidEnchants() == true)
            {
                Bukkit.getLogger().warning("[MerchantShops] Item: " + buyableItem.getItemKey() + " IDX:" + i + " solded by merchant: " + name + " has invalid data!");
                
                //Create error item
                ItemStack errItem = new ItemStack(errorItemMaterial);
                ItemMeta errMeta = errItem.getItemMeta();
                
                errMeta.setDisplayName(ChatColor.RED + "ERROR ITEM!");
                
                List<String> errLore = new ArrayList<>();
                errLore.add("This item has invalid data, inform a server admin.");
                
                //Display all invalid data
                if(Material.getMaterial(buyableItem.getItemKey().toUpperCase()) == null)
                    errLore.add("itemKey: " + ChatColor.RED + buyableItem.getItemKey() + ChatColor.DARK_PURPLE + " is not a valid item.");
                if(buyableItem.getAmount() <= 0)
                    errLore.add("amount: " + ChatColor.RED + buyableItem.getAmount() + ChatColor.DARK_PURPLE + " should be greater then zero.");
                if(buyableItem.getLevelCost() <= 0)
                    errLore.add("level cost: " + ChatColor.RED + buyableItem.getLevelCost() + ChatColor.DARK_PURPLE + " should be greater then zero.");
                
                //potion effects if has any
                if(buyableItem.getEffects() != null)
                {
                    for(String effect : buyableItem.getEffects())
                    {
                        String[] effectData = effect.split(" ");
                        if(PotionEffectType.getByName(effectData[0].toUpperCase()) == null)
                            errLore.add("effect: " + ChatColor.RED + effectData[0] + ChatColor.DARK_PURPLE + " is not a valid potion effect");
                        if(MerchantShops.isInteger(effectData[1]) == false)
                            errLore.add("level: " + ChatColor.RED + effectData[1] + ChatColor.DARK_PURPLE + " for effect: " + ChatColor.RED + effectData[0] + ChatColor.DARK_PURPLE + " is NaN.");
                        if(MerchantShops.isInteger(effectData[2]) == false)
                            errLore.add("duration: " + ChatColor.RED + effectData[2] + ChatColor.DARK_PURPLE + " for effect: " + ChatColor.RED + effectData[0] + ChatColor.DARK_PURPLE + " is NaN.");
                    }
                }
                
                //Enchant data if has any
                if(buyableItem.getEnchants() != null)
                {
                    for(String enchant : buyableItem.getEnchants())
                    {
                        String[] enchantData = enchant.split(" ");
                        if(Enchantment.getByKey(NamespacedKey.minecraft(enchantData[0].toLowerCase())) == null)
                            errLore.add("enchant: " + ChatColor.RED + enchantData[0] + ChatColor.DARK_PURPLE + " is not a valid enchantment.");
                        if(MerchantShops.isInteger(enchantData[1]) == false)
                            errLore.add("level: " + ChatColor.RED + enchantData[1] + ChatColor.DARK_PURPLE + " for enchant: " + ChatColor.RED + enchantData[0] + ChatColor.DARK_PURPLE + " is NaN.");
                    }
                }
                
                //Add error lore token
                errLore.add(errItemToken);
                errMeta.setLore(errLore);
                
                errMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                errItem.setItemMeta(errMeta);
                
                errItem.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
                
                buyMenu.setItem(buyMenu.firstEmpty(), errItem);
                continue;
            }
    		
            //base data
            ItemStack item = new ItemStack(Material.getMaterial(buyableItem.getItemKey().toUpperCase()));
            ItemMeta meta = item.getItemMeta();
            item.setDurability((short) buyableItem.getDamage());
            item.setAmount(buyableItem.getAmount());
    		
            //if item is skull
            if(item.getType() == Material.PLAYER_HEAD)
                if(buyableItem.getSkullOwner() != null)
                    ((SkullMeta)meta).setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(buyableItem.getSkullOwner())));	
    		
            //if has skull texture
            if(buyableItem.getSkullTexture() != null)
                MerchantShops.applyTexture((SkullMeta)meta, UUID.fromString(buyableItem.getSkullOwner()), buyableItem.getSkullTexture());
    		
            //if has display name
            if(buyableItem.getDisplayName() != null)
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', buyableItem.getDisplayName()));
    		
            //if has lore
            List<String> lore = new ArrayList<>();
            if(buyableItem.getLore() != null)			
                for(String line : buyableItem.getLore())
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
    		
            //cost info in lore
            lore.add(ChatColor.GOLD + "Buy " + ChatColor.GREEN + buyableItem.getAmount() + ChatColor.GOLD + " for " + ChatColor.GREEN + buyableItem.getLevelCost() + ChatColor.GOLD + " levels");
            
            //Has a bulk cost
            if(buyableItem.getBulkLevelCost() > 0)
                lore.add(ChatColor.GOLD + "Hold Shift to buy " + ChatColor.GREEN + (buyableItem.getAmount() * buyableItem.getBulkBuyMutiplier()) + ChatColor.GOLD + " for " + ChatColor.GREEN + buyableItem.getBulkLevelCost() + ChatColor.GOLD + " levels");
            
            meta.setLore(lore);
    		
            //if potion effects
            if(buyableItem.getEffects() != null)
            {
                if(item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION)
                {
                    PotionMeta potMeta = (PotionMeta)meta;
                    for(String effect : buyableItem.getEffects())
                    {
                        String[] effectData = effect.split(" ");
                        int level = Integer.parseInt(effectData[1]);
                        int duration = Integer.parseInt(effectData[2]);
                        
                        potMeta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(effectData[0]), duration, level), true);
                    }
                }
            }
            
            //set meta
            item.setItemMeta(meta);
    		
            //if has enchants
            if(buyableItem.getEnchants() != null)
            {
                for(String enchant : buyableItem.getEnchants())
                {
                    try
                    {
                        String[] enchantData = enchant.split(" ");
                        int level = Integer.parseInt(enchantData[1]);
    				
                        item.addUnsafeEnchantment(Enchantment.getByKey(NamespacedKey.minecraft(enchantData[0].toLowerCase())), level);
                    }
                    catch(Exception  e)
                    { 
                        Bukkit.getLogger().warning("[MerchantShop] A item in the shop data has invalid enchantment data, skipping");
                        continue;
                    }
                }
            }
            
            buyMenu.setItem(buyMenu.firstEmpty(), item);
        }
    	
        //Show inventory
        player.openInventory(buyMenu);
    }
    
    /**Removes the merchant from the world.*/
    public void remove()
    {
        //if not loaded load chunk
        Chunk chunk = null;
        boolean wasUnloaded = false;
        if(getMerchantEntity() == null)
        {
            chunk = this.location.getChunk();
            if(chunk.isLoaded() == false)
            {
                wasUnloaded = true;
                chunk.load(false);
            }
        }
    		
        if(getMerchantEntity() != null)
            getMerchantEntity().remove();
        else
            Bukkit.getLogger().warning("[Merchant Shops] Merchant entity could not be found while trying to remove it, assuming its dead.");
    	
        //Check for players viewing merchant inventory if so close it
        for(Player player : Bukkit.getOnlinePlayers())
            if(player.getOpenInventory() != null && player.getOpenInventory().getTitle().endsWith(name))
                player.getOpenInventory().close();
                
        sellItems.clear();
        sellItems = null;
        location = null;
    	name = null;
        
        if(wasUnloaded == true)
            chunk.unload(true);
    }
    
    /**Sets the merchant's location to the specified location. Do not use Entity.teleport(location) as we need to update the merchants location in this object
     * @param location the new location of the merchant
     * @return true if the location of the merchant entity was modified else if it failed returns false*/
    public boolean setLocation(Location location)
    {
        //if not loaded load chunk
        Chunk chunk = null;
        boolean wasUnloaded = false;
        if(getMerchantEntity() == null)
        {
            chunk = this.location.getChunk();
            if(chunk.isLoaded() == false)
            {
                wasUnloaded = true;
                chunk.load(false);
            }
        }
    	
        //if still null
        if(getMerchantEntity() == null) { 
            Bukkit.getLogger().warning("[Merchant Shops] Merchant entity could not be found while tring to move it, is it dead?"); return false;
        }
    	
        getMerchantEntity().teleport(location);
        this.location = location;
    	
        if(wasUnloaded == true)
            chunk.unload(true);
    	
        return true;
    }
    
    /**Sets the merchant's display name. Do not use Entity.setCustomName(name) as we need to update the merchants name in this object
     * @param name the new name of the merchant
     * @return true if the name of the merchant entity was modified else if it failed returns false*/
    public boolean setMerchantName(String name)
    {
        //if not loaded try to load chunk
        Chunk chunk = null;
        boolean wasUnloaded = false;
        if(getMerchantEntity() == null)
        {
            chunk = this.location.getChunk();
            if(chunk.isLoaded() == false)
            {
                wasUnloaded = true;
                chunk.load(false);
            }
        }
        
        //if still null
        if(getMerchantEntity() == null) { 
            Bukkit.getLogger().warning("[Merchant Shops] Merchant entity could not be found while tring to change its name, is it dead?"); return false;
        }
        
        getMerchantEntity().setCustomName(ChatColor.translateAlternateColorCodes('&', name));
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        
        //Check for players viewing merchant inventory if so close the inventory, I would have liked to update the name but it looks like its not doable
        for(Player player : Bukkit.getOnlinePlayers())
            if(player.getOpenInventory() != null && player.getOpenInventory().getTitle().equals(name))
                player.getOpenInventory().close();
        
        if(wasUnloaded == true)
            chunk.unload(true);
        
        return true;
    }
    
    /**Sets the merchant entity type of the merchant
     * @param entityType the new entity type to use for the merchant
     * @return true if the name of the merchant entity was modified else if it failed returns false*/
    public boolean setEntityType(EntityType entityType)
    {
        //Check if entity type is valid
        if(MerchantShops.isValidEntityType(entityType.toString()) != true) {
            Bukkit.getLogger().warning("[Merchant Shops] Tried to set merchant: " + name + " entity type to invalid entity type!"); return false;
        }
            
        //Get current entity in the world
        Chunk chunk = null;
        boolean wasUnloaded = false;
        Entity oldEntity = getMerchantEntity();
        if(oldEntity == null)
        {
            chunk = this.location.getChunk();
            if(chunk.isLoaded() == false)
            {
                wasUnloaded = true;
                chunk.load(false);
                
                oldEntity = getMerchantEntity();
            }
        }
        
        //if could not find entity
        if(oldEntity == null) { 
            Bukkit.getLogger().warning("[Merchant Shops] Merchant entity could not be found while tring to change its entity type, is it dead?"); return false;
        }
        
        //Spawn new entity
        UUID newEntityUUID = MerchantShops.spawnMerchantEntity(entityType, this.location, this.name);
        
        //Check if entity was spawn successfully if not cancel
        if(newEntityUUID == null) {
            Bukkit.getLogger().warning("[Merchant Shops] Failed to spawn new merchant entity for merchant: " + name + " entity type: " + entityType.toString() + "?"); return false;
        }
        
        //Set entity type and data
        this.entityUUID = newEntityUUID;
        this.entityType = entityType;
        
        //Remove entity
        oldEntity.remove();
        
        if(wasUnloaded == true)
            chunk.unload(true);
       
        return true;
    }
    
    /**Gets the merchant entity's UUID
     * @return the physical entity's UUID*/
    public UUID getMerchantEntityUUID() { return entityUUID; }
    
    /**Gets the name of the merchant entity
     * @return the name of the merchant entity*/
    public String getMerchantName() { return name; }
    
    /**Gets the type of entity that is used by merchant
     * @return the entity type of the merchant*/
    public EntityType getEntityType() { return entityType; }
    
    /**Gets location of the merchant entity
     * @return the location of the merchant entity in the world*/
    public Location getLocation() { return location; }
    
    /**Gets the merchant entity
     * @return the merchant entity*/
    public Entity getMerchantEntity() { return Bukkit.getEntity(entityUUID); }
    
    /**Gets the list of all items that the merchant is selling
     * @return a List containing all the items that the merchant is selling*/
    public List<BuyableItem> getItemsForSale() { return sellItems; }
}
