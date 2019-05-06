package github.shor_van.merchantshops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**Handles the /shop command for the merchant shops plugin*/
public class CommandHandler implements CommandExecutor, TabCompleter
{
    private final JavaPlugin plugin; //Reference to the base plugin, should not be null
    
    /**Creates a new instance of the command handler, there should only be one
     * @param plugin the plugin, this should be of type MerchantShops*/
    public CommandHandler(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }
    
    /**Handles the /shop command, triggered when the /shop command is sent to the server
     * @param sender the CommandSender object that sent the command
     * @param cmd the command that was sent, in this case it should only be /shop
     * @param label the CommandSender object that sent the command
     * @param args the arguments that the user sent with the command*/
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
    {
        if (cmd.getName().equalsIgnoreCase("shop"))//<-checks if command sent was /shop
        {
            //OP/perms check
            if(sender.hasPermission("merchantshop.command.shop") && sender.isOp() == true)
            {
                if(args.length >= 1)
                {
                    if(args[0].equalsIgnoreCase("addmerchant")) //Add a new merchant at the specified location
                    {
                        if(sender instanceof Player)
                        {
                            if(args.length >= 4)
                            {
                                Player playerSender = (Player)sender;
                                Location location = null;
                                EntityType entityType = EntityType.VILLAGER;
                                String displayName = ChatColor.RED + "NOT NAMED!";
                                
                                //Validate/parse
                                try
                                {
                                    //base position data
                                    double posX = Double.parseDouble(args[1]);
                                    double posY = Double.parseDouble(args[2]);
                                    double posZ = Double.parseDouble(args[3]);
                                    
                                    //entity type
                                    if(args.length >= 5)
                                        if(MerchantShops.isValidEntityType(args[4]) == true)
                                            entityType = EntityType.valueOf(args[4].toUpperCase());
                                        else
                                            { sender.sendMessage(ChatColor.RED + args[4] + " is a invalid entity type!"); return true; }
                                        
                                    //pitch
                                    float pitch = 0f;
                                    if(args.length >= 6)
                                        pitch = Float.parseFloat(args[5]);
                                    
                                    //yaw
                                    float yaw = 0f;
                                    if(args.length >= 7)
                                        yaw = Float.parseFloat(args[6]);
                                    
                                    location = new Location(playerSender.getWorld(), posX, posY, posZ, yaw, pitch);

                                } catch (NumberFormatException e) {
                                    sender.sendMessage(ChatColor.RED + "Invalid position data given!");
                                    return true;
                                }
    							
                                //Name
                                if(args.length >= 8)
                                    displayName = args[7].replace("_", " ");
                                
                                //create
                                UUID entityUUID = ((MerchantShops) plugin).spawnMerchantEntity(entityType, location, displayName);
                                ((MerchantShops) plugin).getMerchants().add(new Merchant(entityUUID, entityType, ChatColor.translateAlternateColorCodes('&', displayName), location, new ArrayList<BuyableItem>()));
                                ((MerchantShops) plugin).saveMerchants();
                                
                                sender.sendMessage(ChatColor.GOLD + "New merchant created at X: " + ChatColor.AQUA + location.getX() + ChatColor.GOLD  + " Y: " + ChatColor.AQUA + location.getY() + ChatColor.GOLD + " Z: " + ChatColor.AQUA + location.getZ());
                                return true;
                            }
                            else
                            {
                                sender.sendMessage(ChatColor.AQUA + "Usage: /shop addmerchant <x> <y> <z> [EntityType] [pitch] [yaw] [name]");
                                return true;
                            }
                        }
                        else if(sender instanceof ConsoleCommandSender)
                        {
                            if(args.length >= 5)
                            {
                                    
                            }
                            else
                            {
                                sender.sendMessage(ChatColor.AQUA + "Usage: /shop addmerchant <world> <x> <y> <z> [pitch] [yaw] [name]");
                                return true;
                            }
                        }
                    }
                    else if(args[0].equalsIgnoreCase("removemerchant")) //Remove the specified merchant
                    {
                        if(args.length >= 2)
                        {
                            //Get merchant
                            int id;
                            Merchant merchant = null;
                            try 
                            {
                                id = Integer.parseInt(args[1]);
                                if(id >= 0 && id < ((MerchantShops) plugin).getMerchants().size())
                                    merchant = ((MerchantShops) plugin).getMerchants().get(id);
                                else
                                {
                                    sender.sendMessage(ChatColor.RED + "ID: " + args[1] + " is out of range!");
                                    return true;
                                }
                                
                            } catch(NumberFormatException e) {
                                sender.sendMessage(ChatColor.RED + args[1] + " is not a valid id number!");
                                return true;
                            }
                            
                            sender.sendMessage(ChatColor.GOLD + "Merchant: " + "(" + ChatColor.AQUA + id + ChatColor.GOLD + ") has been removed.");
                            ((MerchantShops) plugin).getMerchants().remove(merchant);
                            merchant.remove();
                            
                            ((MerchantShops) plugin).saveMerchants();
                            return true;
                        }
                        else
                        {
                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop removemerchant <id>");
                            return true;
                        }
                    }
                    else if(args[0].equalsIgnoreCase("merchant")) //Modify the specified merchant
                    {
                        if(args.length >= 3)
                        {
                            //Get merchant
                            int merchantId;
                            Merchant merchant = null;
                            try 
                            {
                                merchantId = Integer.parseInt(args[1]);
                                if(merchantId >= 0 && merchantId < ((MerchantShops) plugin).getMerchants().size())
                                    merchant = ((MerchantShops) plugin).getMerchants().get(merchantId);
                                else
                                {
                                    sender.sendMessage(ChatColor.RED + "ID: " + args[1] + " is out of range!");
                                    return true;
                                }
                                
                            } catch(NumberFormatException e) {
                                sender.sendMessage(ChatColor.RED + args[1] + " is not a valid id number!");
                                return true;
                            }
                            
                            //options
                            if(args[2].equalsIgnoreCase("name")) //Change the display name of the merchant
                            {
                                if(args.length >= 4)
                                {
                                    if(merchant.setMerchantName(ChatColor.translateAlternateColorCodes('&', args[3].replace("_", " "))) == true)
                                    {
                                        sender.sendMessage(ChatColor.GOLD + "Merchant: " + ChatColor.AQUA + merchantId + ChatColor.GOLD + " name changed to " + ChatColor.AQUA + merchant.getMerchantName() + ChatColor.GOLD + ".");
                                        ((MerchantShops) plugin).saveMerchants();
                                        return true;
                                    }
                                    else
                                    {
                                        sender.sendMessage(ChatColor.RED + "Mechant: " + merchantId + " could not be moved, is entity dead?");
                                        return true;
                                    }
                                }
                                else
                                {
                                    sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> name <name>");
                                    return true;
                                }
                            }
                            else if(args[2].equalsIgnoreCase("entitytype")) //Change the merchant's entity type
                            {
                                sender.sendMessage("NOT IMPLEMENTED");
                                return true;
                            }
                            else if(args[2].equalsIgnoreCase("position")) //Change the position of the merchants
                            {
                                if(sender instanceof Player)
                                {
                                    if(args.length >= 6)
                                    {
                                        Player playerSender = (Player)sender;
                                        Location location = null;
                                        
                                        //Validate/parse
                                        try
                                        {
                                            //base position data
                                            double posX = Double.parseDouble(args[3]);
                                            double posY = Double.parseDouble(args[4]);
                                            double posZ = Double.parseDouble(args[5]);
                                            
                                            //pitch
                                            float pitch = 0.0f;
                                            if(args.length >= 7)
                                                pitch = Float.parseFloat(args[6]);
                                            
                                            //yaw
                                            float yaw = 0.0f;
                                            if(args.length >= 8)
                                                yaw = Float.parseFloat(args[7]);
                                            
                                            location = new Location(playerSender.getWorld(), posX, posY, posZ, yaw, pitch);
                                            
                                        } catch (NumberFormatException e) {
                                            playerSender.sendMessage(ChatColor.RED + "Invalid position data given!");
                                            return true;
                                        }
                                        
                                        //set location
                                        if(merchant.setLocation(location) == true)
                                        {
                                            ((MerchantShops) plugin).saveMerchants();
                                            
                                            sender.sendMessage(ChatColor.GOLD + "Merchant: " + ChatColor.AQUA + merchantId + ChatColor.GOLD + " position set to  X:" + ChatColor.AQUA + location.getX() + ChatColor.GOLD 
                                                    + " Y: " + ChatColor.AQUA + location.getZ() + ChatColor.GOLD 
                                                    + " Z: " + ChatColor.AQUA + location.getZ() + ChatColor.GOLD
                                                    + " Pitch: " + ChatColor.AQUA + location.getPitch() + ChatColor.GOLD
                                                    + " Yaw: " + ChatColor.AQUA + location.getYaw());
                                            return true;
                                        }
                                        else
                                        {
                                            sender.sendMessage(ChatColor.RED + "Mechant: " + merchantId + " could not be moved, is entity dead?");
                                            return true;
                                        }
                                    }
                                    else
                                    {
                                        sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> position <x> <y> <z> [pitch] [yaw]");
                                        return true;
                                    }
                                }
                            }
                            else if(args[2].equalsIgnoreCase("additem")) //add a new item that the merchant sells
                            {
                                if(args.length >= 7)
                                {
                                    String itemKey = args[3].toLowerCase();
                                    int damage = 0;
                                    int amount = 0;
                                    int levelCost = 0;
                                    try
                                    {
                                        damage = Integer.parseInt(args[4]);
                                        amount = Integer.parseInt(args[5]);
                                        levelCost = Integer.parseInt(args[6]);
                                		
                                    } catch (NumberFormatException e) {
                                        sender.sendMessage(ChatColor.RED + "Invalid parameters given!");
                                        return true;
                                    }
                                    
                                    BuyableItem buyableItem = new BuyableItem(itemKey, damage, amount, levelCost);
                                    merchant.getItemsForSale().add(buyableItem);
                                    ((MerchantShops) plugin).saveMerchants();
                                    
                                    sender.sendMessage(ChatColor.GOLD + "Item added to merchant "  + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ")");
                                    return true;
                                }
                                else
                                {
                                    sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> additem <itemKey> <damage> <amount> <levelCost>");
                                    return true;
                                }
                            }
                            else if(args[2].equalsIgnoreCase("removeitem")) //Remove a item that the merchant sells
                            {
                                if(args.length >= 4)
                                {
                                    int itemId;
                                    try 
                                    {
                                        itemId = Integer.parseInt(args[3]);
                                        if(itemId < 0 || itemId > merchant.getItemsForSale().size())
                                        {
                                            sender.sendMessage(ChatColor.RED + "ID: " + args[3] + " is out of range!");
                                            return true;
                                        }
                                        
                                    } catch(Exception e) {
                                        sender.sendMessage(ChatColor.RED + args[3] + " is not a valid id number!");
                                        return true;
                                    }
                                    
                                    sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " has been removed from merchant " + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ")");
                                    merchant.getItemsForSale().remove(itemId);
                                    ((MerchantShops) plugin).saveMerchants();
                                    return true;
                                }
                                else
                                {
                                    sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> renoveitem <id>");
                                    return true;
                                }
                            }
                            else if(args[2].equalsIgnoreCase("edititem")) //edit a item that the merchant sells
                            {
                                if(args.length >= 5)
                                {
                                    //Get buyable item
                                    int itemId;
                                    BuyableItem buyableItem = null;
                                    try 
                                    {
                                        itemId = Integer.parseInt(args[3]);
                                        if(itemId >= 0 && itemId < merchant.getItemsForSale().size())
                                            buyableItem = merchant.getItemsForSale().get(itemId);
                                        else
                                        {
                                            sender.sendMessage(ChatColor.RED + "ID: " + args[3] + " is out of range!");
                                            return true;
                                        }
                                        
                                    } catch(NumberFormatException e) {
                                        sender.sendMessage(ChatColor.RED + args[3] + " is not a valid id number!");
                                        return true;
                                    }
                                    
                                    //options
                                    if(args[4].equalsIgnoreCase("itemKey")) //Change the specified item's itemkey(the type of item)
                                    {
                                        if(args.length >= 6)
                                        {
                                            String itemKey = args[5].toLowerCase();
                                            buyableItem.setItemKey(itemKey);
                                            ((MerchantShops) plugin).saveMerchants();
                                            
                                            sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") has been set to " + ChatColor.AQUA + itemKey);
                                            return true;
                                        }
                                        else
                                        {
                                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> itemkey <itemkey>");
                                            return true;
                                        }
                                    }
                                    else if(args[4].equalsIgnoreCase("damage")) //Changes the specified item's damage
                                    {
                                        if(args.length >= 6)
                                        {
                                            int damage = 0;
                                            try 
                                            { damage = Integer.parseInt(args[5]); } 
                                            catch(NumberFormatException e) 
                                            {
                                                sender.sendMessage(ChatColor.RED + args[5] + " is not a valid number!");
                                                return true;
                                            }
                                            
                                            buyableItem.setDamage(damage);
                                            ((MerchantShops) plugin).saveMerchants();
                                            
                                            sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") damage set to " + ChatColor.AQUA + damage);
                                            return true;
                                        }
                                        else
                                        {
                                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> damage <damage>");
                                            return true;
                                        }
                                    }
                                    else if(args[4].equalsIgnoreCase("amount")) //Change the amount the player gets when buying the specified item
                                    {
                                        if(args.length >= 6)
                                        {
                                            int amount = 0;
                                            try 
                                            { amount = Integer.parseInt(args[5]); } 
                                            catch(NumberFormatException e) 
                                            {
                                                sender.sendMessage(ChatColor.RED + args[5] + " is not a valid number!");
                                                return true;
                                            }
                                            
                                            buyableItem.setAmount(amount);
                                            ((MerchantShops) plugin).saveMerchants();
                                            
                                            sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") amount set to " + ChatColor.AQUA + amount);
                                            return true;
                                        }
                                        else
                                        {
                                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> amount <amount>");
                                            return true;
                                        }
                                    }
                                    else if(args[4].equalsIgnoreCase("levelcost")) //Change the amount of levels the specified item costs
                                    {
                                        if(args.length >= 6)
                                        {
                                            int levelCost = 0;
                                            try 
                                            { levelCost = Integer.parseInt(args[5]); } 
                                            catch(NumberFormatException e) 
                                            {
                                                sender.sendMessage(ChatColor.RED + args[5] + " is not a valid number!");
                                                return true;
                                            }
                                            
                                            buyableItem.setLevelCost(levelCost);
                                            ((MerchantShops) plugin).saveMerchants();
                                            
                                            sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") level cost set to " + ChatColor.AQUA + levelCost);
                                            return true;
                                        }
                                        else
                                        {
                                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> levelcost <levelcost>");
                                            return true;
                                        }
                                    }
                                    else if(args[4].equalsIgnoreCase("bulkcost")) //Change the amount of levels the specified item costs when buying in bulk
                                    {
                                        if(args.length >= 6)
                                        {
                                            int bulkCost = 0;
                                            try 
                                            { bulkCost = Integer.parseInt(args[5]); } 
                                            catch(NumberFormatException e) 
                                            {
                                                sender.sendMessage(ChatColor.RED + args[5] + " is not a valid number!");
                                                return true;
                                            }
                                            
                                            buyableItem.setBulkLevelCost(bulkCost);;
                                            ((MerchantShops) plugin).saveMerchants();
                                            
                                            sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") bulk cost set to " + ChatColor.AQUA + bulkCost);
                                            return true;
                                        }
                                        else
                                        {
                                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> bulkcost <bulkcost>");
                                            return true;
                                        }
                                    }
                                    else if(args[4].equalsIgnoreCase("bulkmultiplier")) //Change the amount that the item is multiplied by when buying in bulk
                                    {
                                        if(args.length >= 6)
                                        {
                                            int bulkmultiplier = 0;
                                            try 
                                            { bulkmultiplier = Integer.parseInt(args[5]); } 
                                            catch(NumberFormatException e) 
                                            {
                                                sender.sendMessage(ChatColor.RED + args[5] + " is not a valid number!");
                                                return true;
                                            }
                                            
                                            buyableItem.setBulkBuyMutiplier(bulkmultiplier);
                                            ((MerchantShops) plugin).saveMerchants();
                                            
                                            sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") bulk multiplier set to " + ChatColor.AQUA + bulkmultiplier);
                                            return true;
                                        }
                                        else
                                        {
                                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> bulkmultiplier <multiplier>");
                                            return true;
                                        }
                                    }
                                    else if(args[4].equalsIgnoreCase("name")) //Change the display name of the specified item
                                    {
                                        if(args.length >= 6)
                                        {
                                            //if sender wants to clear the display name
                                            if(args[5].equalsIgnoreCase("clear"))
                                            {
                                                //does not have a display name
                                                if(buyableItem.getDisplayName() == null)
                                                {
                                                    sender.sendMessage(ChatColor.RED + "Item: " + itemId + " sold by merchant (" + merchantId + ") does not have a display name set!");
                                                    return true;
                                                }
                                                
                                                buyableItem.setDisplayName(null);
                                                ((MerchantShops) plugin).saveMerchants();
                                                
                                                sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") display name cleared");
                                                return true;
                                            }
                                            
                                            String displayName = args[5].replace("_", " ");
                                            buyableItem.setDisplayName(displayName);
                                            ((MerchantShops) plugin).saveMerchants();
                                            
                                            sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") display name set to " + ChatColor.AQUA + displayName);
                                            return true;
                                        }
                                        else
                                        {
                                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> name <name>");
                                            return true;
                                        }
                                    }
                                    else if(args[4].equalsIgnoreCase("skullowner")) //Change the UUID of the player that owns the skull
                                    {
                                        if(args.length >= 6)
                                        {
                                            //if sender wants to clear the skull owner
                                            if(args[5].equalsIgnoreCase("clear"))
                                            {
                                                //does not have skull owner
                                                if(buyableItem.getSkullOwner() == null)
                                                {
                                                    sender.sendMessage(ChatColor.RED + "Item: " + itemId + " sold by merchant (" + merchantId + ") does not have a skull owner set!");
                                                    return true;
                                                }
                                                
                                                buyableItem.setSkullOwner(null);
                                                ((MerchantShops) plugin).saveMerchants();
                                                
                                                sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") skull owner cleared");
                                                return true;
                                            }
                                            
                                            String skullOwner = args[5];
                                            buyableItem.setSkullOwner(skullOwner);
                                            ((MerchantShops) plugin).saveMerchants();
                                            
                                            sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") skull owner set to " + ChatColor.AQUA + skullOwner);
                                            
                                          //Warn if not skull that value is useless
                                            if(buyableItem.getItemKey().equals("player_head") == false)
                                                sender.sendMessage(ChatColor.GRAY + "[WARNING] Item: " + ChatColor.DARK_GRAY + itemId + ChatColor.GRAY + " is not set to type skull, this value is ignored if it is not set to item type player_head");
                                            return true;
                                        }
                                        else
                                        {
                                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> skullowner <UUID>");
                                            return true;
                                        }
                                    }//32 24 34
                                    else if(args[4].equalsIgnoreCase("skulltexture")) //Change the texture of the skull item
                                    {
                                        if(args.length >= 6)
                                        {
                                            //if sender wants to clear the skull texture
                                            if(args[5].equalsIgnoreCase("clear"))
                                            {
                                                //does not have skull texture
                                                if(buyableItem.getSkullTexture() == null)
                                                {
                                                    sender.sendMessage(ChatColor.RED + "Item: " + itemId + " sold by merchant (" + merchantId + ") does not have a skull texture set!");
                                                    return true;
                                                }
                                                
                                                buyableItem.setSkullTexture(null);
                                                ((MerchantShops) plugin).saveMerchants();
                                                
                                                sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") skull texture cleared");
                                                return true;
                                            }
                                            
                                            String skullTexture = args[5];
                                            buyableItem.setSkullTexture(skullTexture);
                                            ((MerchantShops) plugin).saveMerchants();
                                            
                                            sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") skull texture set to " + ChatColor.AQUA + skullTexture);
                                            
                                            //Warn if not skull that value is useless
                                            if(buyableItem.getItemKey().equals("player_head") == false)
                                                sender.sendMessage(ChatColor.GRAY + "[WARNING] Item: " + ChatColor.DARK_GRAY + itemId + ChatColor.GRAY + " is not set to type skull, this value is ignored if it is not set to item type player_head");
                                            return true;
                                        }
                                        else
                                        {
                                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> skulltexture <skulltexture>");
                                            return true;
                                        }
                                    }
                                    else if(args[4].equalsIgnoreCase("lore")) //Change the lore of the specified item
                                    {
                                        if(args.length >= 6)
                                        {
                                            //If sender wants to clear lore
                                            if(args[5].equalsIgnoreCase("clear"))
                                            {
                                                //does not have lore
                                                if(buyableItem.getLore() == null)
                                                {
                                                    sender.sendMessage(ChatColor.RED + "Item: " + itemId + " sold by merchant (" + merchantId + ") does not have any lore!");
                                                    return true;
                                                }
                                                
                                                buyableItem.removeLore();
                                                ((MerchantShops) plugin).saveMerchants();
                                                
                                                sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") lore has been cleared");
                                                return true;
                                            }
                                            
                                            String[] loreLines = args[5].split(":");
                                           
                                            buyableItem.removeLore();
                                            for(String loreLine : loreLines)
                                                buyableItem.addLore(loreLine.replace("_", " "));
                                            ((MerchantShops) plugin).saveMerchants();
                                            
                                            sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") lore has changed");
                                            return true;
                                        }
                                        else
                                        {
                                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> lore <lore> (lore lines are seperated by ':')");
                                            return true;
                                        }
                                    }
                                    else if(args[4].equalsIgnoreCase("enchants")) //Change the enchantments of the specified item
                                    {
                                        if(args.length >= 6)
                                        {
                                            if(args[5].equalsIgnoreCase("add")) //Add the specified enchant to the specified item
                                            {
                                                if(args.length >= 7)
                                                {
                                                    int level = 1;
                                                    String enchantKey = args[6];
                                                    if(buyableItem.hasEnchant(enchantKey) == false)
                                                    {
                                                        //Specified a level
                                                        if(args.length >= 8)
                                                        {
                                                            try 
                                                            { level = Integer.parseInt(args[7]); } 
                                                            catch(NumberFormatException e) 
                                                            {
                                                                sender.sendMessage(ChatColor.RED + args[7] + " is not a valid number!");
                                                                return true;
                                                            }
                                                        }
                                                        
                                                        buyableItem.addEnchant(enchantKey, level);
                                                        ((MerchantShops) plugin).saveMerchants();
                                                            
                                                        sender.sendMessage(ChatColor.GOLD + "Enchant: " + ChatColor.AQUA + enchantKey + ChatColor.GOLD + " has been to item: " + ChatColor.AQUA + itemId + ChatColor.GOLD + " sold by merchant: " + ChatColor.AQUA + merchantId);
                                                        return true;
                                                    }
                                                    else
                                                    {
                                                        sender.sendMessage(ChatColor.RED + "Item: " + itemId + " already has the " + enchantKey + " enchantment!");
                                                        return true;
                                                    }
                                                }
                                                else
                                                {
                                                    sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> enchants add <enchantKey> [level]");
                                                    return true;
                                                }
                                            }
                                            else if(args[5].equalsIgnoreCase("remove")) //remove the specified enchant from the specified item
                                            {
                                                if(args.length >= 7)
                                                {
                                                    String enchantKey = args[6];
                                                    int idx = buyableItem.getEnchantIndex(enchantKey);
                                                    if(idx != -1)
                                                    {
                                                        buyableItem.removeEnchant(idx);
                                                        ((MerchantShops) plugin).saveMerchants();
                                                        
                                                        sender.sendMessage(ChatColor.GOLD + "Enchant: " + ChatColor.AQUA + enchantKey + ChatColor.GOLD + " of item: " + ChatColor.AQUA + itemId + ChatColor.GOLD + " sold by merchant: " + ChatColor.AQUA + merchantId + ChatColor.GOLD + " has been removed");
                                                        return true;
                                                    }
                                                    else
                                                    {
                                                        sender.sendMessage(ChatColor.RED + "Item: " + itemId + " does not have the " + enchantKey + " enchantment!");
                                                        return true;
                                                    }
                                                }
                                                else
                                                {
                                                    sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> enchants remove <enchantKey>");
                                                    return true;
                                                }
                                            }
                                            else if(args[5].equalsIgnoreCase("setlevel")) //Change the specified enchant's level of the specified item
                                            {
                                                if(args.length >= 8)
                                                {
                                                    int level = 0;
                                                    String enchantKey = args[6];
                                                    int idx = buyableItem.getEnchantIndex(enchantKey);
                                                    if(idx != -1)
                                                    {
                                                        try 
                                                        { level = Integer.parseInt(args[7]); } 
                                                        catch(NumberFormatException e) 
                                                        {
                                                            sender.sendMessage(ChatColor.RED + args[7] + " is not a valid number!");
                                                            return true;
                                                        }
                                                        
                                                        buyableItem.setEnchantLevel(idx, level);;
                                                        ((MerchantShops) plugin).saveMerchants();
                                                       
                                                        sender.sendMessage(ChatColor.GOLD + "Enchant: " + ChatColor.AQUA + enchantKey + ChatColor.GOLD + " of item: " + ChatColor.AQUA + itemId + ChatColor.GOLD + " sold by merchant: " + ChatColor.AQUA + merchantId + ChatColor.GOLD + " level set to " + ChatColor.AQUA + level);
                                                        return true;
                                                    }
                                                    else
                                                    {
                                                        sender.sendMessage(ChatColor.RED + "Item: " + itemId + " does not have the " + enchantKey + " enchantment!");
                                                        return true;
                                                    }
                                                }
                                                else
                                                {
                                                    sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> enchants setlevel <enchantKey> <level>");
                                                    return true;
                                                }
                                            }
                                            else
                                            {
                                                sender.sendMessage(ChatColor.RED + "Invalid option, possible options: add, remove, setlevel");
                                                return true;
                                            }
                                        }
                                        else
                                        {
                                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> enchants <add/remove|setlevel>");
                                            return true;
                                        }
                                    }
                                    else
                                    {
                                        sender.sendMessage(ChatColor.RED + "Invalid option, possible options: itemkey, damage, amount, levelcost, name, skullowner, skulltexture, lore, enchants");
                                        return true;
                                    }
                                }
                                else
                                {
                                    sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> renoveitem <id>");
                                    return true;
                                }
                            }
                            else if(args[2].equalsIgnoreCase("listitems")) //List all the items that the merchant sells
                            {
                                sender.sendMessage( ChatColor.AQUA + "Listing all items sold by merchant " + ChatColor.GOLD + merchant.getMerchantName() + ChatColor.AQUA + "(" + ChatColor.GOLD + merchantId + ChatColor.AQUA + ")....");
                                sender.sendMessage(ChatColor.YELLOW + "---------------------------------------------");
    							
                                //if no items for sale
                                if(merchant.getItemsForSale().size() == 0)
                                {
                                    sender.sendMessage(ChatColor.AQUA + "merchant does not seem to be selling any items.");
                                    return true;
                                }
                                
                                //list items
                                for(int i = 0; i < merchant.getItemsForSale().size();  i++)
                                {
                                    BuyableItem buyableItem = merchant.getItemsForSale().get(i);
                                    sender.sendMessage(ChatColor.AQUA + "ID: " + ChatColor.GOLD + i 
                                            + ChatColor.AQUA + " Item key: " + ChatColor.GOLD + buyableItem.getItemKey()
                                            + ChatColor.AQUA + " Amount: " + ChatColor.GOLD + buyableItem.getAmount()
                                            + ChatColor.AQUA + " Cost: " + ChatColor.GOLD + buyableItem.getLevelCost());
                                }
                                return true;
                            }
                            else
                            {
                                sender.sendMessage(ChatColor.RED + "Invalid option, possible options: name, entitytype, position, additem, removeitem, edititem, listitems");
                                return true;
                            }
                        }
                        else
    					{
                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <name|postion|additem|removeitem|listitems>");
                            return true;
    					}
                    }
                    else if(args[0].equalsIgnoreCase("listmerchants")) //Lists all the active merchants
                    {
                        sender.sendMessage( ChatColor.AQUA + "Listing all registered merchants....");
                        sender.sendMessage(ChatColor.YELLOW + "---------------------------------------------");
                        
                        //if no merchants
                        if(((MerchantShops) plugin).getMerchants().size() == 0)
                        {
                            sender.sendMessage(ChatColor.AQUA + "No merchants registered.");
                            return true;
                        }
    					
                        //list merchants
                        for(int i = 0; i < ((MerchantShops) plugin).getMerchants().size(); i++)
                        {
                            Merchant merchant = ((MerchantShops) plugin).getMerchants().get(i);
                            sender.sendMessage(ChatColor.AQUA + "ID: " + ChatColor.GOLD + i + 
                                    ChatColor.AQUA + " Name: " + ChatColor.GOLD + merchant.getMerchantName() + 
                                    ChatColor.AQUA + " posX: " + ChatColor.GOLD + merchant.getLocation().getX() +
                                    ChatColor.AQUA + " posY: " + ChatColor.GOLD + merchant.getLocation().getY() +
                                    ChatColor.AQUA + " posZ: " + ChatColor.GOLD + merchant.getLocation().getZ());
                        }
                        return true;
                    }
                    else if(args[0].equalsIgnoreCase("reload")) //Reloads the plugin
                    {
                        //plugin.getServer().getPluginManager().disablePlugin(plugin);  
                        //plugin.getServer().getPluginManager().enablePlugin(plugin);
                        sender.sendMessage(ChatColor.RED + "Command not implemented.");
                        return true;
                    }
                    else if(args[0].equalsIgnoreCase("help")) //Shows all the shop commands
                    {
                        sender.sendMessage("Shop coammnds" + ChatColor.GOLD + " (" + ChatColor.AQUA + "<requierd>" + ChatColor.DARK_AQUA + " [optional]" + ChatColor.GOLD + ")");
                        sender.sendMessage(ChatColor.YELLOW + "---------------------------------------------");
                        sender.sendMessage(ChatColor.AQUA + "/shop addmerchant " + ChatColor.GOLD + "-" + ChatColor.WHITE + " adds a new merchant at the specified location.");
                        sender.sendMessage(ChatColor.AQUA + "/shop removemerchant " + ChatColor.GOLD + "-" + ChatColor.WHITE + " removes the specified merchant.");
                        sender.sendMessage(ChatColor.AQUA + "/shop merchant " + ChatColor.GOLD + "-" + ChatColor.WHITE + " edit the specified merchant's properties.");
                        sender.sendMessage(ChatColor.AQUA + "/shop listmerchants " + ChatColor.GOLD + "-" + ChatColor.WHITE + " lists all the merchants.");
                        sender.sendMessage(ChatColor.AQUA + "/shop reload " + ChatColor.GOLD + "-" + ChatColor.WHITE + " reloads tbe plugin.");
                        sender.sendMessage(ChatColor.AQUA + "/shop help " + ChatColor.GOLD + "-" + ChatColor.WHITE + " shows the list of commands.");
                        sender.sendMessage(ChatColor.YELLOW + "---------------------------------------------");
                        return true;
                    }
                }
                else
                {
                    sender.sendMessage(ChatColor.AQUA + "Usage: /shop <addmerchant|removemerchant|merchant|listmerchants|help>");
                    return true;
                }
            }
            else
            {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return true;
            }
        }
        return false;
    }
    
    /**Handles tab completion of the /shop command
     * @param sender the CommandSender object that sent the command
     * @param cmd the command that was sent, in thiis case it should only be /shop
     * @param alias the CommandSender object that sent the command
     * @param args the arguments that the user sent with the command*/
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args)
    {
        //base options
        List<String> options = new ArrayList<String>();
        if(args.length == 1)
        {
            if(args[0].isEmpty() == false)
            {
                if("addmerchant".startsWith(args[0].toLowerCase()))
                	options.add("addmerchant");
                if("removemerchant".startsWith(args[0].toLowerCase()))
                	options.add("removemerchant");
                if("merchant".startsWith(args[0].toLowerCase()))
                	options.add("merchant");
                if("listmerchants".startsWith(args[0].toLowerCase()))
                	options.add("listmerchants");
                if("reload".startsWith(args[0].toLowerCase()))
                	options.add("reload");
                if("help".startsWith(args[0].toLowerCase()))
                	options.add("help");
            }
            else
            {
                options.add("addmerchant");
                options.add("removemerchant");
                options.add("merchant");
                options.add("listmerchants");
                options.add("reload");
                options.add("help");
            }
        }
    	
        //sub commands
        if(args.length >= 2)
        {
            if(args[0].equalsIgnoreCase("addmerchant"))
            {
                if(sender instanceof Player)
                {
                    Player playerSender = (Player)sender;
                    
                    //x 
                    if(args.length == 2)
                    {
                        if(args[1].isEmpty() == false)
                        {
                            if((playerSender.getLocation().getX() + "").startsWith(args[1]))
                                options.add(playerSender.getLocation().getX() + "");
                        }
                        else
                            options.add(playerSender.getLocation().getX() + "");
                    }
    				
                    //y
                    if(args.length == 3)
                    {
                        if(args[2].isEmpty() == false)
                        {
                            if((playerSender.getLocation().getY() + "").startsWith(args[2]))
                                options.add(playerSender.getLocation().getY() + "");
                        }
                        else
                            options.add(playerSender.getLocation().getY() + "");
                    }
    				
                    //z
                    if(args.length == 4)
                    {
                        if(args[3].isEmpty() == false)
                        {
                            if((playerSender.getLocation().getZ() + "").startsWith(args[3]))
                                options.add(playerSender.getLocation().getZ() + "");
                        }
                        else
                            options.add(playerSender.getLocation().getZ() + "");
                    }
    				
                    //entity type
                    if(args.length == 5)
                    {
                        if(args[4].isEmpty() == false)
                        {
                            for(EntityType entityType : MerchantShops.validEntityTypes)
                                if(entityType.name().toLowerCase().startsWith(args[4].toLowerCase()))
                                    options.add(entityType.name().toLowerCase());
                        }
                        else
                            for(EntityType entityType : MerchantShops.validEntityTypes)
                                options.add(entityType.name().toLowerCase());
                    }
                    
                    //pitch
                    if(args.length == 6)
                    {
                        if(args[5].isEmpty() == false)
                        {
                            if((playerSender.getLocation().getPitch() + "").startsWith(args[5]))
                                options.add(playerSender.getLocation().getPitch() + "");
                        }
                        else
                            options.add(playerSender.getLocation().getPitch() + "");
                    }
    				
                    //yaw
                    if(args.length == 7)
                    {
                        if(args[6].isEmpty() == false)
                        {
                            if((playerSender.getLocation().getYaw() + "").startsWith(args[6]))
                                options.add(playerSender.getLocation().getYaw() + "");
                        }
                        else
                            options.add(playerSender.getLocation().getYaw() + "");
                    }
                }
            }
            else if(args[0].equalsIgnoreCase("merchant"))
            {
                //sub options
                if(args.length == 3)
                {
                    if(args[2].isEmpty() == false)
                    {
                        if("name".startsWith(args[2].toLowerCase()))
                            options.add("name");
                        if("entitytype".startsWith(args[2].toLowerCase()))
                            options.add("entitytype");
                        if("position".startsWith(args[2].toLowerCase()))
                            options.add("position");
                        if("additem".startsWith(args[2].toLowerCase()))
                            options.add("additem");
                        if("removeitem".startsWith(args[2].toLowerCase()))
                            options.add("removeitem");
                        if("edititem".startsWith(args[2].toLowerCase()))
                            options.add("edititem");
                        if("listitems".startsWith(args[2].toLowerCase()))
                            options.add("listitems");
                    }
                    else
                    {
                        options.add("name");
                        options.add("entitytype");
                        options.add("position");
                        options.add("additem");
                        options.add("removeitem");
                        options.add("edititem");
                        options.add("listitems");
                    }
                }
    			
                //subs
                if(args.length >= 4)
                {
                    if(args[2].equalsIgnoreCase("entitytype"))
                    {
                        if(args[3].isEmpty() == false)
                        {
                            for(EntityType entityType : MerchantShops.validEntityTypes)
                                if(entityType.name().toLowerCase().startsWith(args[3].toLowerCase()))
                                    options.add(entityType.name().toLowerCase());
                        }
                        else
                            for(EntityType entityType : MerchantShops.validEntityTypes)
                                options.add(entityType.name().toLowerCase());
                    }
                    else if(args[2].equalsIgnoreCase("position"))
                    {
                        if(sender instanceof Player)
                        {
                            Player playerSender = (Player)sender;
    						
                            //x
                            if(args.length == 4)
                            {
                                if(args[3].isEmpty() == false)
                                {
                                    if((playerSender.getLocation().getX() + "").startsWith(args[3]))
                                        options.add(playerSender.getLocation().getX() + "");
                                }
                                else
                                    options.add(playerSender.getLocation().getX() + "");
                            }
    						
                            //y
                            if(args.length == 5)
                            {
                                if(args[4].isEmpty() == false)
                                {
                                    if((playerSender.getLocation().getY() + "").startsWith(args[4]))
                                        options.add(playerSender.getLocation().getY() + "");
                                }
                                else
                                    options.add(playerSender.getLocation().getY() + "");
                            }
    						
                            //z
                            if(args.length == 6)
                            {
                                if(args[5].isEmpty() == false)
                                {
                                    if((playerSender.getLocation().getZ() + "").startsWith(args[5]))
                                        options.add(playerSender.getLocation().getZ() + "");
                                }
                                else
                                    options.add(playerSender.getLocation().getZ() + "");
                            }
    						
                            //pitch
                            if(args.length == 7)
                            {
                                if(args[6].isEmpty() == false)
                                {
                                    if((playerSender.getLocation().getPitch() + "").startsWith(args[6]))
                                        options.add(playerSender.getLocation().getPitch() + "");
                                }
                                else
                                    options.add(playerSender.getLocation().getPitch() + "");
                            }
    						
                            //yaw
                            if(args.length == 8)
                            {
                                if(args[7].isEmpty() == false)
                                {
                                    if((playerSender.getLocation().getYaw() + "").startsWith(args[7]))
                                        options.add(playerSender.getLocation().getYaw() + "");
                                }
                                else
                                    options.add(playerSender.getLocation().getYaw() + "");
                            }
                        }
                    }
                    else if(args[2].equalsIgnoreCase("additem"))
                    {
                        if(args.length == 4)
                        {
                            if(args[3].isEmpty() == false)
                            {
                                for(Material material : Material.values())
                                    if(material.name().startsWith("LEGACY_") == false)
                                        if(material.name().toLowerCase().startsWith(args[3].toLowerCase()))
                                            options.add(material.name().toLowerCase());
                            }
                            else
                            {
                                for(Material material : Material.values())
                                    if(material.name().startsWith("LEGACY_") == false)										
                                        options.add(material.name().toLowerCase());
                            }
                        }
                    }
                    else if(args[2].equalsIgnoreCase("edititem"))
                    {
                        if(args.length == 5)
                        {
                            if(args[4].isEmpty() == false)
                            {
                                if("itemkey".startsWith(args[4].toLowerCase()))
                                    options.add("itemkey");
                                if("damage".startsWith(args[4].toLowerCase()))
                                    options.add("damage");
                                if("amount".startsWith(args[4].toLowerCase()))
                                    options.add("amount");
                                if("levelcost".startsWith(args[4].toLowerCase()))
                                    options.add("levelcost");
                                if("bulkcost".startsWith(args[4].toLowerCase()))
                                    options.add("bulkcost");
                                if("bulkmultiplier".startsWith(args[4].toLowerCase()))
                                    options.add("bulkmultiplier");
                                if("name".startsWith(args[4].toLowerCase()))
                                    options.add("name");
                                if("skullowner".startsWith(args[4].toLowerCase()))
                                    options.add("skullowner");
                                if("skulltexture".startsWith(args[4].toLowerCase()))
                                    options.add("skulltexture");
                                if("lore".startsWith(args[4].toLowerCase()))
                                    options.add("lore");
                                if("enchants".startsWith(args[4].toLowerCase()))
                                    options.add("enchants");
                            }
                            else
                            {
                                    options.add("itemkey");
                                    options.add("damage");
                                    options.add("amount");
                                    options.add("levelcost");
                                    options.add("bulkcost");
                                    options.add("bulkmultiplier");
                                    options.add("name");
                                    options.add("skullowner");
                                    options.add("skulltexture");
                                    options.add("lore");
                                    options.add("enchants");
                            }
                        }
                        
                        //Sub options
                        if(args.length >= 6)
                        {
                            if(args[4].equalsIgnoreCase("itemkey"))
                            {
                                if(args.length == 6)
                                {
                                    if(args[5].isEmpty() == false)
                                    {
                                        for(Material material : Material.values())
                                            if(material.name().startsWith("LEGACY_") == false)
                                                if(material.name().toLowerCase().startsWith(args[5].toLowerCase()))
                                                    options.add(material.name().toLowerCase());
                                    }
                                    else
                                    {
                                        for(Material material : Material.values())
                                            if(material.name().startsWith("LEGACY_") == false)                                      
                                                options.add(material.name().toLowerCase());
                                    }
                                }
                            }
                            else if(args[4].equalsIgnoreCase("enchants"))
                            {
                                if(args.length == 6)
                                {
                                    if(args[5].isEmpty() == false)
                                    {
                                        if("add".startsWith(args[5].toLowerCase()))
                                            options.add("add");
                                        if("remove".startsWith(args[5].toLowerCase()))
                                            options.add("remove");
                                        if("setlevel".startsWith(args[5].toLowerCase()))
                                            options.add("setlevel");
                                    }
                                    else
                                    {
                                        options.add("add");
                                        options.add("remove");                                        
                                        options.add("setlevel");
                                    }
                                }
                                
                                if(args.length == 7)
                                {
                                    if(args[6].isEmpty() == false)
                                    {
                                        for(Enchantment enchant : Enchantment.values())
                                            if(enchant.getKey().getKey().toLowerCase().startsWith(args[6].toLowerCase()))
                                                options.add(enchant.getKey().getKey().toLowerCase());
                                    }
                                    else
                                    {
                                        for(Enchantment enchant : Enchantment.values())
                                            options.add(enchant.getKey().getKey().toLowerCase());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Collections.sort(options);
        return options;
    }
}
