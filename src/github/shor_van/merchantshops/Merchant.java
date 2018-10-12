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
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**Represents a merchant*/
public class Merchant
{
    public static final int displaySize = 27; //The max amount of items items to show if we would go over the max size of the inventory
    public static final Material buttonMaterial = Material.SPECTRAL_ARROW; //The material of the item use by the navigation buttons
    public static final String prevPageLoreToken = "Go to the back page"; //The lore line used by the prev page navigation button
    public static final String nextPageLoreToken = "Go to the next page"; //The lore line used by the next page navigation button
    
    private String name; //The name of the merchant
    private UUID entityUUID; //The UUID of the merchant entity
    private Location location; //The location of the merchant entity
    private List<BuyableItem> sellItems; //The items that the merchant sells
    
    /**Creates a new merchant instance
     * @param entityUUID the UUID of the entity that physical represents the merchant in the world
     * @param location the location of the entity
     * @param sellItems the list of items that the merchant sells*/
    public Merchant(UUID entityUUID, String displayName, Location location, List<BuyableItem> sellItems)
    {
        this.entityUUID = entityUUID;
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
            buyMenu = Bukkit.createInventory(null, 36, getMerchantEntity().getCustomName());
        else if(sellItems.size() <= 9)
            buyMenu = Bukkit.createInventory(null, 9, getMerchantEntity().getCustomName());
        else if(sellItems.size() <= 18)
    	    buyMenu = Bukkit.createInventory(null, 18, getMerchantEntity().getCustomName());
    	else if(sellItems.size() <= 27)
    	    buyMenu = Bukkit.createInventory(null, 27, getMerchantEntity().getCustomName());
    	else
    	    buyMenu = Bukkit.createInventory(null, 36, getMerchantEntity().getCustomName());
    	
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
    		
            //Item exists?
            if(Material.getMaterial(buyableItem.getItemKey().toUpperCase()) == null)
            {
                Bukkit.getLogger().warning("[MerchantShops] Item: " + buyableItem.getItemKey() + " IDX:" + i + " solded by merchant: " + getMerchantEntity().getCustomName() + " is not a valid item!");
                continue;
            }
    		
            //base data
            ItemStack item = new ItemStack(Material.getMaterial(buyableItem.getItemKey().toUpperCase()));
            ItemMeta meta = item.getItemMeta();
            item.setDurability((short) buyableItem.getDamage());
            item.setAmount(buyableItem.getAmount());
    		
            //if item is skull
            if(item.getType() == Material.PLAYER_HEAD)
                if(buyableItem.getSkullOwner().isEmpty() == false)
                    ((SkullMeta)meta).setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(buyableItem.getSkullOwner())));	
    		
            //if has skull texture
            if(buyableItem.getSkullTexture().isEmpty() == false)
                MerchantShops.applyTexture((SkullMeta)meta, UUID.fromString(buyableItem.getSkullOwner()), buyableItem.getSkullTexture());
    		
            //if has display name
            if(buyableItem.getDisplayName().isEmpty() == false)
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', buyableItem.getDisplayName()));
    		
            //if has lore
            List<String> lore = new ArrayList<>();
            if(buyableItem.getLore().size() > 0)			
                for(String line : buyableItem.getLore())
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
    		
            //cost info in lore
            lore.add(ChatColor.GREEN + "Level Cost:" + buyableItem.getLevelCost());
            
            //Has a bulk cost
            if(buyableItem.getBulkLevelCost() > 0)
                lore.add(ChatColor.GREEN + "Hold sHift to buy 5 stacks for:" + buyableItem.getBulkLevelCost());
            
            meta.setLore(lore);
    		
            //set meta
            item.setItemMeta(meta);
    		
            //if has enchants
            if(buyableItem.getEnchants().size() > 0)
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
            buyMenu.addItem(item);
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
    	
        sellItems.clear();
        sellItems = null;
        location = null;
    	
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
    
    /**Gets the merchant entity
     * @return the merchant entity*/
    public Entity getMerchantEntity() { return Bukkit.getEntity(entityUUID); }
    
    /**Gets the list of all items that the merchant is selling
     * @return a List containing all the items that the merchant is selling*/
    public List<BuyableItem> getItemsForSale() { return sellItems; }
}
