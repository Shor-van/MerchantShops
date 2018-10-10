package github.shor_van.merchantshops;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

/**The main class for the plugin*/
public class MerchantShops extends JavaPlugin
{
	public static final EntityType merchantEntityType = EntityType.VILLAGER; //The entity type used by the merchants
	
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
					//name
					String displayName = merchantData.getString("name", "NAME NOT SET"); 
					
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
									
							if(itemData.contains("display-name", true))
								buyableItem.setDisplayName(itemData.getString("display-name"));
							
							if(itemData.contains("skull-owner", true))
								buyableItem.setSkullOwner(itemData.getString("skull-owner"));
							
							if(itemData.contains("skull-texture", true))
								buyableItem.setSkullTexture(itemData.getString("skull-texture"));
							
							if(itemData.contains("lore", true))
								buyableItem.getLore().addAll((List<String>)itemData.getList("lore"));
							
							if(itemData.contains("enchants", true))
								buyableItem.getEnchants().addAll((List<String>)itemData.getList("enchants"));
							
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
					Location location = new Location(world, posX, posY, posZ, (float)yaw, (float)pitch);
					
					Entity merchantEntity = world.spawnEntity(location,  merchantEntityType);
					
					merchantEntity.setCustomName(ChatColor.translateAlternateColorCodes('&', displayName));
					merchantEntity.setCustomNameVisible(true);
					merchantEntity.setInvulnerable(true);
					((LivingEntity) merchantEntity).setAI(false);
					
					//Check if we have a duplicate entities
					for(Entity entity : merchantEntity.getNearbyEntities(1, 1, 1))
						if(entity.getType() == merchantEntityType && entity.getCustomName().equals(merchantEntity.getCustomName()))
								{ entity.remove(); this.getLogger().info("Found duplicate entity for " + merchantEntry + " removing it."); }
					
					this.merchants.add(new Merchant(merchantEntity.getUniqueId(), location, merchantItems));
					loaded++;
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
			
			//Name
			merchantSection.set("name", merchant.getMerchantEntity().getCustomName());
			
			//position
			Location loc = merchant.getMerchantEntity().getLocation();
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
				
				//display name if has
				if(buyableItem.getDisplayName().isEmpty() == false)
					itemSection.set("display-name", buyableItem.getDisplayName());
				
				//skull owner if has
				if(buyableItem.getSkullOwner().isEmpty() == false)
					itemSection.set("skull-owner", buyableItem.getSkullOwner());
				
				//skull texture if has
				if(buyableItem.getSkullTexture().isEmpty() == false)
					itemSection.set("skull-texture", buyableItem.getSkullTexture());
				
				//lore if has
				if(buyableItem.getLore().size() > 0)
					itemSection.set("lore", buyableItem.getLore());
				
				//enchants if has
				if(buyableItem.getEnchants().size() > 0)
					itemSection.set("enchants", buyableItem.getEnchants());
			}
		}
		this.saveConfig();
	}
	
    /**Gets the list of active merchants
     * @return the list of merchants*/
	public void createNewMerchant(Location location, String displayName)
	{
		Entity merchantEntity = location.getWorld().spawnEntity(location, merchantEntityType);
		
		merchantEntity.setCustomName(ChatColor.translateAlternateColorCodes('&', displayName));
		merchantEntity.setCustomNameVisible(true);
		merchantEntity.setInvulnerable(true);
		((LivingEntity) merchantEntity).setAI(false);
		
		List<BuyableItem> buyableItems = new ArrayList<>();
		merchants.add(new Merchant(merchantEntity.getUniqueId(), location, buyableItems));
		
		saveMerchants();
	}
	
    /**Gets the list of active merchants
     * @return the list of merchants*/
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
}