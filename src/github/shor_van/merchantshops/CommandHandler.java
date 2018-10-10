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
									
								} catch (IllegalArgumentException e) {
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
								
							} catch(Exception e) {
								sender.sendMessage(ChatColor.RED + args[1] + " is not a valid id number!");
								return true;
							}
							
							sender.sendMessage(ChatColor.GOLD + "Merchant: " + ChatColor.AQUA + merchant.getMerchantEntity().getCustomName() + ChatColor.GOLD + "(" + ChatColor.AQUA + id + ChatColor.GOLD + ") has been removed.");
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
								
							} catch(Exception e) {
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
											
										} catch (IllegalArgumentException e) {
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
										
									} catch (IllegalArgumentException e) {
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
										sender.sendMessage(ChatColor.RED + args[1] + " is not a valid id number!");
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
					else if(args[3].equalsIgnoreCase("edititem"))
					{
						
					}
				}
			}
		}
		Collections.sort(options);
		return options;
	}
}
