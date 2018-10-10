package github.shor_van.merchantshops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**Handles the /shop command for the merchant shops plugin*/
public class CommandHandler implements CommandExecutor, TabCompleter
{
    private final JavaPlugin plugin; //The plugin
    
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
                    if(args[0].equalsIgnoreCase("addmerchant"))
                    {
                        if(sender instanceof Player)
                        {
                            if(args.length >= 4)
                            {
                                Player playerSender = (Player)sender;
                                Location location = null;
                                String displayName = ChatColor.BOLD + "NOT NAMED!";
                                
                                //Validate/parse
                                try
                                {
                                    //base position data
                                    double posX = Double.parseDouble(args[1]);
                                    double posY = Double.parseDouble(args[2]);
                                    double posZ = Double.parseDouble(args[3]);
                                    
                                    //pitch
                                    float pitch = 0f;
                                    if(args.length >= 5)
                                        pitch = Float.parseFloat(args[4]);
                                    
                                    //yaw
                                    float yaw = 0f;
                                    if(args.length >= 6)
                                        yaw = Float.parseFloat(args[5]);
                                    
                                    location = new Location(playerSender.getWorld(), posX, posY, posZ, yaw, pitch);

                                } catch (NumberFormatException e) {
                                    playerSender.sendMessage(ChatColor.RED + "Invalid position data given!");
                                    return true;
                                }
    							
                                //Name
                                if(args.length >= 7)
                                    displayName = args[6].replace("_", " ");
                                
                                //create
                                ((MerchantShops) plugin).createNewMerchant(location, displayName);;
                                
                                playerSender.sendMessage(ChatColor.GOLD + "New merchant created at X: " + ChatColor.AQUA + location.getX() + ChatColor.GOLD  + " Y: " + ChatColor.AQUA + location.getY() + ChatColor.GOLD + " Z: " + ChatColor.AQUA + location.getZ());
                                return true;
                            }
                            else
                            {
                                sender.sendMessage(ChatColor.AQUA + "Usage: /shop addmerchant <x> <y> <z> [pitch] [yaw] [name]");
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
                    else if(args[0].equalsIgnoreCase("removemerchant"))
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
                    else if(args[0].equalsIgnoreCase("merchant")) //Add new item to the list
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
                            if(args[2].equalsIgnoreCase("name"))
                            {
                                if(args.length >= 4)
                                {
                                    merchant.getMerchantEntity().setCustomName(ChatColor.translateAlternateColorCodes('&', args[3].replace("_", " ")));
                                    sender.sendMessage(ChatColor.GOLD + "Merchant: " + ChatColor.AQUA + merchantId + ChatColor.GOLD + " name changed to " + ChatColor.AQUA + merchant.getMerchantEntity().getCustomName().replace("_", " ") + ChatColor.GOLD + ".");
                                    ((MerchantShops) plugin).saveMerchants();
                                    return true;
                                }
                                else
                                {
                                    sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> name <name>");
                                    return true;
                                }
                            }
                            else if(args[2].equalsIgnoreCase("position"))
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
                            else if(args[2].equalsIgnoreCase("additem"))
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
                            else if(args[2].equalsIgnoreCase("removeitem"))
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
                            else if(args[2].equalsIgnoreCase("edititem"))
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
                                    if(args[4].equalsIgnoreCase("itemKey"))
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
                                    else if(args[4].equalsIgnoreCase("damage"))
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
                                    else if(args[4].equalsIgnoreCase("amount"))
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
                                    else if(args[4].equalsIgnoreCase("levelcost"))
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
                                    else if(args[4].equalsIgnoreCase("name"))
                                    {
                                        if(args.length >= 6)
                                        {
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
                                    else if(args[4].equalsIgnoreCase("skullowner"))
                                    {
                                        if(args.length >= 6)
                                        {
                                            String skullOwner = args[5];
                                            buyableItem.setSkullOwner(skullOwner);
                                            ((MerchantShops) plugin).saveMerchants();
                                            
                                            sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") skull owner set to " + ChatColor.AQUA + skullOwner);
                                            return true;
                                        }
                                        else
                                        {
                                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> skullowner <UUID>");
                                            return true;
                                        }
                                    }
                                    else if(args[4].equalsIgnoreCase("skulltexture"))
                                    {
                                        if(args.length >= 6)
                                        {
                                            String skullTexture = args[5];
                                            buyableItem.setSkullTexture(skullTexture);
                                            ((MerchantShops) plugin).saveMerchants();
                                            
                                            sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.DARK_AQUA + itemId + ChatColor.GOLD + " sold by merchant" + ChatColor.GOLD + "(" + ChatColor.AQUA + merchantId + ChatColor.GOLD + ") skull texture set to " + ChatColor.AQUA + skullTexture);
                                            return true;
                                        }
                                        else
                                        {
                                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <id> edititem <id> skulltexture <skulltexture>");
                                            return true;
                                        }
                                    }
                                    else if(args[4].equalsIgnoreCase("lore"))
                                    {
                                        if(args.length >= 6)
                                        {
                                            buyableItem.getLore().clear();
                                            String[] loreLines = args[5].split(":");
                                            for(String loreLine : loreLines)
                                                buyableItem.getLore().add(loreLine.replace("_", " "));
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
                                    else if(args[4].equalsIgnoreCase("enchants"))
                                    {
                                        if(args.length >= 6)
                                        {
                                            if(args[5].equalsIgnoreCase("add"))
                                            {
                                                if(args.length >= 7)
                                                {
                                                    int level = 1;
                                                    String enchantKey = args[6];
                                                    if(buyableItem.hasEnchant(enchantKey) == false)
                                                    {
                                                        //Specified level
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
                                            else if(args[5].equalsIgnoreCase("remove"))
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
                                            else if(args[5].equalsIgnoreCase("setlevel"))
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
                            else if(args[2].equalsIgnoreCase("listitems"))
                            {
                                sender.sendMessage( ChatColor.AQUA + "Listing all items sold by merchant " + ChatColor.GOLD + merchant.getMerchantEntity().getCustomName() + ChatColor.AQUA + "(" + ChatColor.GOLD + merchantId + ChatColor.AQUA + ")....");
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
                                            + ChatColor.AQUA + " Item key: " + ChatColor.GOLD + buyableItem.getItemKey());
                                }
                                return true;
                            }
                            else
                            {
                                sender.sendMessage(ChatColor.RED + "Invalid option, possible options: name, position, additem, removeitem, edititem, listitems");
                                return true;
                            }
                        }
                        else
    					{
                            sender.sendMessage(ChatColor.AQUA + "Usage: /shop merchant <name|postion|additem|removeitem|listitems>");
                            return true;
    					}
                    }
                    else if(args[0].equalsIgnoreCase("listmerchants"))
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
                                    ChatColor.AQUA + " Name: " + ChatColor.GOLD + merchant.getMerchantEntity().getCustomName() + 
                                    ChatColor.AQUA + " posX: " + ChatColor.GOLD + merchant.getMerchantEntity().getLocation().getX() +
                                    ChatColor.AQUA + " posY: " + ChatColor.GOLD + merchant.getMerchantEntity().getLocation().getY() +
                                    ChatColor.AQUA + " posZ: " + ChatColor.GOLD + merchant.getMerchantEntity().getLocation().getZ());
                        }
                        return true;
                    }
                    else if(args[0].equalsIgnoreCase("reload"))
                    {
                        //plugin.getServer().getPluginManager().disablePlugin(plugin);  
                        //plugin.getServer().getPluginManager().enablePlugin(plugin);
                        sender.sendMessage(ChatColor.RED + "Not enabled.");
                        return true;
                    }
                    else if(args[0].equalsIgnoreCase("help")) //Shop help command
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
    				
                    //pitch
                    if(args.length == 5)
                    {
                        if(args[4].isEmpty() == false)
                        {
                            if((playerSender.getLocation().getPitch() + "").startsWith(args[4]))
                                options.add(playerSender.getLocation().getPitch() + "");
                        }
                        else
                            options.add(playerSender.getLocation().getPitch() + "");
                    }
    				
                    //yaw
                    if(args.length == 6)
                    {
                        if(args[5].isEmpty() == false)
                        {
                            if((playerSender.getLocation().getYaw() + "").startsWith(args[5]))
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
                    if(args[2].equalsIgnoreCase("position"))
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
