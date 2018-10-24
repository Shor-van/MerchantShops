package github.shor_van.merchantshops;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**Listens for game events to trigger*/
public class EventListener implements Listener
{	
    private final JavaPlugin plugin; //Reference to the base plugin, should not be null
    
    /**Creates a new instance of the event listener, there should only be one
     * @param plugin the plugin, this should be of type MerchantShops*/
    public EventListener(JavaPlugin plugin)
    {
        this.plugin = plugin;    
    }
    
    /**Triggered when a player right clicks on a entity
     * @param event the PlayerInteractEntityEvent that was triggered*/
    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();
        Entity clicked = event.getRightClicked();
        for(Merchant merchant : ((MerchantShops) plugin).getMerchants())
        {
            if(merchant.getMerchantEntityUUID().equals(clicked.getUniqueId()))
            {
                event.setCancelled(true);
                merchant.showBuyMenu(player, 0);
            }
        }
    }
    
    /**Triggered when a player clicks in a inventory
     * @param event the InventoryClickEvent that was triggered*/
    @SuppressWarnings("deprecation")
    @EventHandler(priority=EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event)
    {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        for(Merchant merchant : ((MerchantShops) plugin).getMerchants())
        {
            //is inventory merchant inventory 
            if(inventory.getName().equals(merchant.getMerchantName()))
            {
                //cancel event
                event.setCancelled(true);
                
                //clicked in merchant inventory
                int slotClicked = event.getRawSlot();
                if(slotClicked >= 0 && slotClicked < inventory.getSize())
                {
                    ItemStack clickedItem = inventory.getItem(slotClicked);
                    if(clickedItem != null)
                    {
                        //get the startIdx
                        int startIdx = 0;
                        ItemStack prevPage = inventory.getItem(inventory.getSize() - 9);
                        if(prevPage != null && prevPage.getType() == Merchant.buttonMaterial && prevPage.getItemMeta().getLore().get(0).equals(Merchant.prevPageLoreToken))
                        {
                            int page = Integer.parseInt(prevPage.getItemMeta().getDisplayName().split(" ")[1]);
                            startIdx = page * Merchant.displaySize;
                        }
                        
                        //if next page item clicked
                        if((slotClicked == inventory.getSize() - 1) && clickedItem.getType() == Merchant.buttonMaterial && clickedItem.getItemMeta().getLore().get(0).equals(Merchant.nextPageLoreToken))
                        {
                            int page = Integer.parseInt(clickedItem.getItemMeta().getDisplayName().split(" ")[1]);
                            merchant.showBuyMenu(player, (page - 1) * Merchant.displaySize);
                            return;
                        }
                        
                        //prev page item clicked
                        if(slotClicked == inventory.getSize() - 9 && clickedItem.getType() == Merchant.buttonMaterial && clickedItem.getItemMeta().getLore().get(0).equals(Merchant.prevPageLoreToken))
                        {
                            int page = Integer.parseInt(clickedItem.getItemMeta().getDisplayName().split(" ")[1]);
                            merchant.showBuyMenu(player, (page - 1) * Merchant.displaySize);
                            return;
                        }
                        
                        //Broken item
                        if(clickedItem.getType() == Merchant.errorItemMaterial  && clickedItem.getItemMeta().getLore().get(2).equals(Merchant.errItemToken))
                        {
                            player.sendMessage(ChatColor.RED + "This item is broken! inform a server admin.");
                            return;
                        }
                        
                        //Get buyable item
                        BuyableItem buyableItem = merchant.getItemsForSale().get(slotClicked + startIdx);
                        
                        //Check player has enough levels
                        if(player.getLevel() >= buyableItem.getLevelCost())
                        {
                            //check if player has space in inventory
                            if(player.getInventory().firstEmpty() != -1)
                            {
                                ItemStack item = new ItemStack(Material.getMaterial(buyableItem.getItemKey().toUpperCase()));
                                ItemMeta meta = item.getItemMeta();
                                item.setDurability((short) buyableItem.getDamage());
                                item.setAmount(buyableItem.getAmount());

                                //if skull is skull
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
                                meta.setLore(lore);
                                
                                //set meta
                                item.setItemMeta(meta);
                                
                                //if has enchants
                                if(buyableItem.getEnchants() != null)
                                {
                                    for(String enchant : buyableItem.getEnchants())
                                    {
                                        String[] enchantData = enchant.split(" ");
                                        int level = Integer.parseInt(enchantData[1]);
                                        
                                        item.addUnsafeEnchantment(Enchantment.getByKey(NamespacedKey.minecraft(enchantData[0].toLowerCase())), level);
                                    }
                                }
                                
                                //Bulk buying
                                if(buyableItem.getBulkLevelCost() > 0 && buyableItem.getBulkBuyMutiplier() > 0 && event.isShiftClick())
                                {
                                    //check if has enough levels
                                    if(player.getLevel() >= buyableItem.getBulkLevelCost())
                                    {
                                        if(MerchantShops.canItemFitInInventory(player.getInventory(), item, buyableItem.getAmount() * buyableItem.getBulkBuyMutiplier()))
                                        {
                                          //give items
                                            for(int i = 0; i < buyableItem.getBulkBuyMutiplier(); i++)
                                                player.getInventory().addItem(new ItemStack(item));//give items
                                            
                                            //subtract cost from player level
                                            player.setLevel(player.getLevel() - buyableItem.getBulkLevelCost());
                                            
                                            String name = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name().replace("_", " ").toLowerCase();
                                            player.sendMessage(ChatColor.GOLD + "" + (buyableItem.getAmount() * buyableItem.getBulkBuyMutiplier()) + " of " + name + ChatColor.GOLD + " purchsed for " + buyableItem.getBulkLevelCost() + " levels.");
                                        }
                                        else
                                        {
                                            player.sendMessage(ChatColor.RED + "You do not have enough space in your inventory!");
                                            return;
                                        }
                                    }
                                    else
                                    {
                                        player.sendMessage(ChatColor.RED + "You do not have enough XP levels to buy! you need " + buyableItem.getBulkLevelCost() + " levels.");
                                        return;
                                    }
                                }
                                else
                                {
                                    //give item
                                    player.getInventory().addItem(item);
                                
                                    //subtract cost from player level
                                    player.setLevel(player.getLevel() - buyableItem.getLevelCost());
                                
                                    String name = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name().replace("_", " ").toLowerCase();
                                    player.sendMessage(ChatColor.GOLD + "" + buyableItem.getAmount() + " " + name + ChatColor.GOLD + " purchsed for " + buyableItem.getLevelCost() + " levels.");
                                }
                            }
                            else
                            {
                                player.sendMessage(ChatColor.RED + "Your inventory is full!");
                                return;
                            }
                        }
                        else
                        {
                            player.sendMessage(ChatColor.RED + "You do not have enough XP levels to buy! you need " + buyableItem.getLevelCost() + " levels.");
                            return;
                        }
                    }
                }
            }
        }
    }
}
