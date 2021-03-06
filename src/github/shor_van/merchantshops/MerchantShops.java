package github.shor_van.merchantshops;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

/**The main class for the plugin*/
public class MerchantShops extends JavaPlugin
{
  //Array of entity types that can be used
    public static final EntityType[] validEntityTypes = { EntityType.BAT, EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CHICKEN, EntityType.COD, EntityType.COW, 
            EntityType.CREEPER, EntityType.DOLPHIN, EntityType.DONKEY, EntityType.DROWNED, EntityType.ELDER_GUARDIAN, EntityType.ENDER_DRAGON, EntityType.ENDERMAN, 
            EntityType.ENDERMITE, EntityType.EVOKER, EntityType.GHAST, EntityType.GIANT, EntityType.GUARDIAN, EntityType.HORSE, EntityType.HUSK, EntityType.ILLUSIONER, 
            EntityType.IRON_GOLEM, EntityType.LLAMA, EntityType.MAGMA_CUBE, EntityType.MULE, EntityType.MUSHROOM_COW, EntityType.OCELOT, EntityType.PARROT, EntityType.PHANTOM, 
            EntityType.PIG, EntityType.PIG_ZOMBIE, EntityType.POLAR_BEAR, EntityType.PUFFERFISH, EntityType.RABBIT, EntityType.SALMON, EntityType.SHEEP, EntityType.SHULKER, 
            EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SKELETON_HORSE, EntityType.SLIME, EntityType.SNOWMAN, EntityType.SPIDER, EntityType.SQUID, EntityType.STRAY, 
            EntityType.TROPICAL_FISH, EntityType.TURTLE, EntityType.VEX, EntityType.VILLAGER, EntityType.VINDICATOR, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON, 
            EntityType.WOLF, EntityType.ZOMBIE, EntityType.ZOMBIE_HORSE, EntityType.ZOMBIE_VILLAGER, EntityType.FOX, EntityType.WANDERING_TRADER, EntityType.PANDA, EntityType.CAT};
    
    private CommandHandler cmdHandler; //command handler for the plugin
    private EventListener eventListener; //the event listener of the plugin
    
    private List<Merchant> merchants; //the list of merchants
    
    /**Initiates the plugin*/
    @Override
    public void onEnable()
    {
        //config load
        this.getConfig();
        this.saveDefaultConfig();
    	
        //initiate handlers
        cmdHandler = new CommandHandler(this);
        eventListener = new EventListener(this);
        merchants = new ArrayList<>();
    	
        getCommand("shop").setExecutor(cmdHandler);
        getCommand("shop").setTabCompleter(cmdHandler);
    	
        getServer().getPluginManager().registerEvents(eventListener, this);
    	
        //load merchant data
        loadMerchants();
    }
    
    /**Disables the plugin.*/
    @Override
    public void onDisable()
    {
        //remove handlers
        cmdHandler = null;
        eventListener = null;
    	
        //remove merchants
        for(Merchant merchant : merchants)
            merchant.remove();
        merchants.clear();
        merchants = null;
    }
    
    /**Loads the merchant data from the config file*/
    @SuppressWarnings("unchecked")
    private void loadMerchants()
    {
        int loaded = 0;
        this.getLogger().info("Loading merchants from config file...");
    	
        //get merchants section
        ConfigurationSection merchants = this.getConfig().getConfigurationSection("shops-data");
        for(String merchantEntry : merchants.getKeys(false))
        {
            if(merchantEntry.startsWith("merchant-"))
            {
                ConfigurationSection merchantData = this.getConfig().getConfigurationSection(merchants.getCurrentPath() + "." + merchantEntry);
                if(merchantData.contains("position", true) && merchantData.contains("sells", true))//Validation
                {
                    //entity type
                    EntityType entityType = EntityType.VILLAGER;
                    if(merchantData.contains("entity-type", true))
                    {
                        String eType = merchantData.getString("entity-type").toUpperCase();
                        if(isValidEntityType(eType) == true)
                            entityType = EntityType.valueOf(eType);
                        else
                            this.getLogger().warning(merchantEntry + " has invalid entity type:" + eType + " using default.");
                    }
                    
                    //name
                    String displayName = merchantData.getString("name", ChatColor.RED + "NOT NAMED!"); 
    				
                    //get position data
                    String worldName;
                    double posX, posY, posZ, pitch, yaw;
                    ConfigurationSection positionData = this.getConfig().getConfigurationSection(merchantData.getCurrentPath() + ".position");
                    if(positionData.contains("world", true) && positionData.contains("x", true) && positionData.contains("y", true) && positionData.contains("z", true))//Validation
                    {
                        worldName = positionData.getString("world");
                        posX = positionData.getDouble("x");
                        posY = positionData.getDouble("y");
                        posZ = positionData.getDouble("z");
                        pitch = positionData.getDouble("pitch", 0.0);
                        yaw = positionData.getDouble("yaw", 0.0);
                    }
                    else
                    {
                        this.getLogger().warning(merchantEntry + " does not seem to have valid position data, skipping!");
                        continue;
                    }
    				
                    //get sell items data
                    List<BuyableItem> merchantItems = new ArrayList<>();
                    ConfigurationSection sellItemsData = this.getConfig().getConfigurationSection(merchantData.getCurrentPath() + ".sells");
                    for(String itemEntry : sellItemsData.getKeys(false))
                    {
                        ConfigurationSection itemData = this.getConfig().getConfigurationSection(sellItemsData.getCurrentPath() + "." + itemEntry);
                        if(itemData.contains("item-key", true) && itemData.contains("damage", true) && itemData.contains("amount", true) && itemData.contains("level-cost", true))
                        {
                            String itemKey = itemData.getString("item-key");
                            int damage = itemData.getInt("damage");
                            int amount = itemData.getInt("amount");
                            int levelCost = itemData.getInt("level-cost");
                            
                            BuyableItem buyableItem = new BuyableItem(itemKey, damage, amount, levelCost);
                            
                            if(itemData.contains("bulk-level-cost", true))
                                buyableItem.setBulkLevelCost(itemData.getInt("bulk-level-cost"));
                            
                            if(itemData.contains("bulk-multiplier", true))
                                buyableItem.setBulkBuyMutiplier(itemData.getInt("bulk-multiplier"));
                            
                            if(itemData.contains("display-name", true))
                                buyableItem.setDisplayName(itemData.getString("display-name"));
    						
                            if(itemData.contains("skull-owner", true))
                                buyableItem.setSkullOwner(itemData.getString("skull-owner"));
    						
                            if(itemData.contains("skull-texture", true))
                                buyableItem.setSkullTexture(itemData.getString("skull-texture"));
    						
                            if(itemData.contains("lore", true))
                                buyableItem.setLore((List<String>)itemData.getList("lore"));
    						
                            if(itemData.contains("enchants", true))
                                buyableItem.setEnchants((List<String>)itemData.getList("enchants"));
    						
                            if(itemData.contains("effects", true))
                                buyableItem.setEffects((List<String>)itemData.getList("effects"));
                            
                            merchantItems.add(buyableItem);
                        }
                        else
                        {
                            this.getLogger().warning(itemEntry + " sold by " + merchantEntry + " does not seem to have valid data, skipping item!");
                            continue;
                        }
                    }
    				
                    //create merchant
                    World world = Bukkit.getWorld(worldName);
                    
                    //check if world exists
                    if(world == null)
                    { this.getLogger().warning("World: " + worldName + " does not seem to exists for merchant: " + merchantEntry); continue;}
                        
                    Location location = new Location(world, posX, posY, posZ, (float)yaw, (float)pitch);
    				
                    //Check if we have a duplicate entities
                    for(Entity entity : location.getWorld().getNearbyEntities(location, 1, 1, 1))
                        if(entity.getType() == entityType && entity.getCustomName().equals(ChatColor.translateAlternateColorCodes('&', displayName)))
                            { entity.remove(); this.getLogger().info("Found duplicate entity for " + merchantEntry + " removing it."); }
                    
                    //Try spawn merchant
                    UUID entityUUID = spawnMerchantEntity(entityType, location, displayName);
                    this.merchants.add(new Merchant(entityUUID, entityType, ChatColor.translateAlternateColorCodes('&', displayName), location, merchantItems));
                    loaded++;
                    
                    //if failed to spawn entity send error to OPs
                    if(entityUUID == null)
                        for(Player player : Bukkit.getOnlinePlayers())
                            if(player.isOp() == true)
                                player.sendMessage(ChatColor.GRAY + "[Merchant Shops] " + ChatColor.RED + ChatColor.ITALIC + "Failed to spawn entity: " + entityType.toString() + " for merchant: " + displayName 
                                        + " at loction X: " + location.getX() + " Y: " + location.getY() + " Z: " + location.getZ());
                }
                else
                {
                    this.getLogger().warning(merchantEntry + " does not have valid data, skipping!");
                    continue;
                }
            }
        }
    	
        this.getLogger().info("Completed, " + loaded + " merchants loaded.");
    }
    
    /**Saves the merchant data to the config file*/
    public void saveMerchants()
    {
        //create merchants section
        ConfigurationSection merchants = this.getConfig().createSection("shops-data");
        for(int i = 0; i < this.merchants.size(); i++)
        {
            Merchant merchant = this.merchants.get(i);
            ConfigurationSection merchantSection = merchants.createSection("merchant-" + i);
    		
            //entity type
            if(merchant.getEntityType().equals(EntityType.VILLAGER) == false)
                merchantSection.set("entity-type", merchant.getEntityType().name().toLowerCase());
            
            //Name
            merchantSection.set("name", merchant.getMerchantName());
    		
            //position
            Location loc = merchant.getLocation();
            merchantSection.set("position.world", loc.getWorld().getName());
            merchantSection.set("position.x", loc.getX());
            merchantSection.set("position.y", loc.getY());
            merchantSection.set("position.z", loc.getZ());
            merchantSection.set("position.pitch", loc.getPitch());
            merchantSection.set("position.yaw", loc.getYaw());
    		
            //sell items
            ConfigurationSection items = merchantSection.createSection("sells");
            for(int j = 0; j < merchant.getItemsForSale().size(); j++)
            {
                BuyableItem buyableItem = merchant.getItemsForSale().get(j);
                ConfigurationSection itemSection = items.createSection("item-" + j);
    			
                //base item data
                itemSection.set("item-key", buyableItem.getItemKey());
                itemSection.set("damage", buyableItem.getDamage());
                itemSection.set("amount", buyableItem.getAmount());
                itemSection.set("level-cost", buyableItem.getLevelCost());
                
                //bulk cost if has
                if(buyableItem.getBulkLevelCost() > 0)
                    itemSection.set("bulk-level-cost", buyableItem.getBulkLevelCost());
                
                //bulk multiplier if has
                if(buyableItem.getBulkLevelCost() > 0)
                    itemSection.set("bulk-multiplier", buyableItem.getBulkBuyMutiplier());
                
                //display name if has
                if(buyableItem.getDisplayName() != null)
                    itemSection.set("display-name", buyableItem.getDisplayName());
    			
                //skull owner if has
                if(buyableItem.getSkullOwner() != null)
                    itemSection.set("skull-owner", buyableItem.getSkullOwner());
    			
                //skull texture if has
                if(buyableItem.getSkullTexture() != null)
                    itemSection.set("skull-texture", buyableItem.getSkullTexture());
    			
                //lore if has
                if(buyableItem.getLore() != null)
                    itemSection.set("lore", buyableItem.getLore());
    			
                //enchants if has
                if(buyableItem.getEnchants() != null)
                    itemSection.set("enchants", buyableItem.getEnchants());
                
                //potion effects if has
                if(buyableItem.getEffects() != null)
                    itemSection.set("effects", buyableItem.getEffects());
            }
        }
        this.saveConfig();
    }
    
    /**Spawns the merchant's entity at the specified location
     * @param entityType the type of entity
     * @param location the location where to spawn the entity
     * @param displayName the display name of the merchant
     * @return the UUID of the entity*/
    public static UUID spawnMerchantEntity(EntityType entityType, Location location, String displayName)
    {
        try
        {
            Entity merchantEntity = location.getWorld().spawnEntity(location, entityType);
            
            merchantEntity.setCustomName(ChatColor.translateAlternateColorCodes('&', displayName));
            merchantEntity.setCustomNameVisible(true);
            merchantEntity.setInvulnerable(true);
            ((LivingEntity) merchantEntity).setAI(false);
            
            //Check if the entity was spawned and return the UUID
            return Bukkit.getEntity(merchantEntity.getUniqueId()).getUniqueId();
        }
        catch(Exception e) {
            Bukkit.getLogger().severe("[Merchant Shops] Failed to spawn entity: " + entityType.toString() + " for merchant: " + displayName 
                    + " at loction X: " + location.getX() + " Y: " + location.getY() + " Z: " + location.getZ() + "?"); return null;
        }
    }
    
    /**Gets the specified merchant
     * @param index the index of the merchant to get*/
    public Merchant getMerchant(int index)
    {
        if(index >= 0 && index < merchants.size())
    	    return merchants.get(index);
        return null;
    }
    
    /**Gets the list of active merchants
     * @return the list of merchants*/
    public List<Merchant> getMerchants() { return merchants; }
    
    /**Applies a texture-url to a playerhead's meta.
     * @param headMeta The SkullMeta associated with the playerhead to modify
     * @param uuid A UUID to associate with the head and texture
     * @param texture The Base64-encoded Texture-URL tags.
     * @return true: the information was properly set on the playerhead; false: there was an error setting the profile field.
     * @author x7aSv*/
    public static boolean applyTexture(SkullMeta headMeta, UUID uuid, String texture)
    {
        GameProfile profile = new GameProfile(uuid, null);
        profile.getProperties().put("textures", new Property("textures", texture));
        try
        {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } 
        catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) 
        {
            error.printStackTrace();
            return false;
        }
        return true;
    }
    
    /**Counts the number of empty slots in the specified inventory.
     * @param inventory the inventory to count
     * @return the number of empty/free slots in the inventory.*/
    public static int getNumberOfEmptySlotsInInventory(Inventory inventory)
    {
        int count = 0;
        if(inventory.firstEmpty() == -1)
            return count;
            
        for(ItemStack itemStack : inventory.getStorageContents())
            if(itemStack == null || itemStack.getType() == Material.AIR)
                count++;
        return count;
    }
    
    /**Checks if the specified item stack can fit in the specified inventory.
     * @param inventory the inventory to check if the item would fit in
     * @param item the item stack to check if it would fit
     * @param amount the amount of the item, this is here as sometimes you might want to specify a amount higher then the max stack size
     * @return true if the item stack can fit in the specified inventory, false if it can not fit.*/
    public static boolean canItemFitInInventory(Inventory inventory, ItemStack item, int amount)
    {
        int amountLeft = amount;

        // First check if it can merge with existing stacks
        ItemStack[] itemsInInv = inventory.getStorageContents();
        for (int i = 0; i < itemsInInv.length; i++)
        {
            if (itemsInInv[i] == null)
                continue;

            if (itemsInInv[i].isSimilar(item) == true)
            {
                if (itemsInInv[i].getAmount() < itemsInInv[i].getMaxStackSize())
                {
                    if (itemsInInv[i].getAmount() + amountLeft <= itemsInInv[i].getMaxStackSize()) // Check if stack would fit in itemstack
                        return true;
                    else // If overflows take what is need
                        amountLeft = amountLeft - (itemsInInv[i].getMaxStackSize() - itemsInInv[i].getAmount());
                }
            }
        }

        // If amount left check if inventor has free slots
        int freeSlots = getNumberOfEmptySlotsInInventory(inventory);
        if (amountLeft > 0 && freeSlots != 0)
        {
            for(int i = 1; i <= freeSlots; i++)
            {
                if(amountLeft <= item.getMaxStackSize()) //Left can fit in one stack
                    return true;
                else //get overflow
                    amountLeft = amountLeft - item.getMaxStackSize();
            }
        }
        
        return false;
    }
    
    /**Checks if the specified string is a valid integer.
     * @param string the string to check if it is a number
     * @return true if the string is a valid number, false if it is not a valid number.*/
    public static boolean isInteger(String string) 
    {
        return string.matches("[+-]?\\d+");
    }
    
    /**Checks if the specified string is a valid entity type.
     * @param value the entity type string
     * @return true if the string is a valid entity, false if it is not a valid entity.*/
    public static boolean isValidEntityType(String value)
    {
        for(EntityType entityType : validEntityTypes)
            if(entityType.name().equals(value.toUpperCase()))
                return true;
        return false;
    }
}
