package github.shor_van.merchantshops;

import java.util.ArrayList;
import java.util.List;

/**Represents a item that is sold by a merchant*/
public class BuyableItem
{
    private String itemKey; //The item's namespace key
    private int damage; //The amount of damage the item has
    private int amount; //The amount of the item the player gets
    private int levelCost; //The amount of xp levels tha item costs
    private String displayName; //The item's custom display name
    private String skullOwner; //The UUID of the player who owns the skull
    private String skullTexture; //Base64 string of the texture used by the skull
    private List<String> lore; //A list of the item's lore
    private List<String> enchants; //A list of the item's enchants
    
    /**Creates a new instance of BuyableItem
     * @param itemKey the namespace key of the item
     * @param damage the amount of damage the item has
     * @param amount the amount that the player gets
     * @param levelCost the cost of the item in xp levels*/
    public BuyableItem(String itemKey, int damage, int amount, int levelCost)
    {
        this.itemKey = itemKey;
        this.damage = damage;
        this.amount = amount;
        this.levelCost = levelCost;
        
        this.displayName = "";
        this.skullOwner = "";
        this.skullTexture = "";
        this.lore = new ArrayList<>();
        this.enchants = new ArrayList<>();
    }
    
    /**Gets the index of the enchantment in the item's enchants list 
     * @param enchantKey the namespace key of the enchantment
     * @return the index of the enchant in the list, -1 if the item does not have the enchant*/
    public int getEnchantIndex(String enchantKey)
    {
        for(int i = 0; i > enchants.size(); i++)
            if(enchants.get(i).split(" ")[0].toLowerCase().equals(enchantKey.toLowerCase()))
                return i;
        return -1;
    }
    
    /**Checks if the enchantment is in the item's enchants list 
     * @param enchantKey the namespace key of the enchantment
     * @return true if the item has the enchantment else false*/
    public boolean hasEnchant(String enchantKey)
    {
        for(String enchant : enchants)
            if(enchant.split(" ")[0].toLowerCase().equals(enchantKey.toLowerCase()))
                return true;
        return false;
    }
    
    /**Sets the level of the enchantment at the specified index 
     * @param index the index of enchantment of the level to set
     * @param level the level to set*/
    public void setEnchantLevel(int index, int level)
    {
        if(index < 0 || index >= enchants.size())
            throw new IllegalArgumentException("The index if out of range to the enchants list");
        
        String enchantKey = enchants.get(index).split(" ")[0];
        enchants.set(index, enchantKey + " " + level);
    }
    
    /**removes the specified enchant from the enchants list 
     * @param index the index of enchantment to remove*/
    public void removeEnchant(int index)
    {
        if(index < 0 || index >= enchants.size())
            throw new IllegalArgumentException("The index if out of range to the enchants list");
        
        enchants.remove(index);
    }
    
    /**Adds the specified enchant to the item's enchants list
     * @param enchantKey the namespace key of the enchantment
     * @param level the level of the enchantment*/
    public void addEnchant(String enchantKey, int level) { enchants.add(enchantKey + " " + level); }
    
    /**Sets the item namespace key for this buyable item
     * @param itemKey the namespace key of the item*/
    public void setItemKey(String itemKey) { this.itemKey = itemKey; } 
    
    /**Sets how much damage the item has
     * @param damage how much damage the item has*/
    public void setDamage(int damage) { this.damage = damage; }
    
    /**Sets the amount that the player gets when they buy the item 
     * @param amount the amount to set*/
    public void setAmount(int amount) { this.amount = amount; }
    
    /**Sets how many levels the item costs to buy
     * @param levelCost the amount of levels the item costs*/
    public void setLevelCost(int levelCost) { this.levelCost = levelCost; }
    
    /**Sets the item's custom display name
     * @param displayName the custom display name of the item*/
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    /**Sets the UUID string of the player who owns the skull, only valid if the itemKey is of type minecraft:player_head
     * @param skullOwner the UUID of the player who owns the skull*/
    public void setSkullOwner(String skullOwner) { this.skullOwner = skullOwner; }
    
    /**Sets the base64 encoded string of the texture used by the skull, only valid if the itemKey is of type minecraft:player_head
     * @param skullTexture base64 encoded string of the texture used by the skull*/
    public void setSkullTexture(String skullTexture) { this.skullTexture = skullTexture; }
    
    /**Gets the namespace key of the item
     * @return the namespace key of the item*/
    public String getItemKey() { return itemKey; }
    
    /**Gets the amount of damage the item should have
     * @return the amount of damage the item has*/
    public int getDamage() { return damage; }
    
    /**Gets the amount of the item the player gets
     * @return the amount of the item*/
    public int getAmount() { return amount; }
    
    /**Gets the amount of xp levels the item costs to buy
     * @return the amount of levels the item costs*/
    public int getLevelCost() { return levelCost; }
    
    /**Gets the item's custom display name if it has one
     * @return the item's custom display name, if it does not have one returns ""*/
    public String getDisplayName() { return displayName; }
    
    /**Gets the UUID string of the player who owns the skull, only valid if the itemKey is of type minecraft:player_head
     * @return UUID string of the player who owns the skull*/
    public String getSkullOwner() { return skullOwner; }
    
    /**Gets the base64 encoded string of the skull texture, only valid if the itemKey is of type minecraft:player_head
     * @return base64 encoded string of the texture used by the skull*/
    public String getSkullTexture() { return skullTexture; }
    
    /**Gets a list of the item's lore
     * @return a list containing the item's lore*/
    public List<String> getLore() { return lore; }
    
    /**Gets a list of the items enchants, enchantment type and level are seperated by space EG: "sharpness 5"
     * @return a list containing the item's enchants*/
    public List<String> getEnchants() { return enchants; }
}
