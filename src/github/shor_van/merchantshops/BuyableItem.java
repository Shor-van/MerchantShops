package github.shor_van.merchantshops;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

/**Represents a item that is sold by a merchant*/
public class BuyableItem
{
    private String itemKey; //The item's namespace key
    private int damage; //The amount of damage the item has
    private int amount; //The amount of the item the player gets
    private int levelCost; //The amount of xp levels that item costs
    private int bulkLevelCost; //The amount of levels that buying in bulk costs
    private int bulkBuyMultiplier; //The amount that the single buy amount is multiplied by when the player is buying in bulk
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
        
        this.bulkLevelCost = 0;
        this.bulkBuyMultiplier = 1;
        this.displayName = null;
        this.skullOwner = null;
        this.skullTexture = null;
        this.lore = null;
        this.enchants = null;
    }
    
    /**Gets the index of the enchantment in the item's enchants list 
     * @param enchantKey the namespace key of the enchantment
     * @return the index of the enchant in the list, -1 if the item does not have the enchant*/
    public int getEnchantIndex(String enchantKey)
    {
        if(enchants == null)
            return -1;
        
        for(int i = 0; i < enchants.size(); i++)
            if(enchants.get(i).split(" ")[0].toLowerCase().equals(enchantKey.toLowerCase()))
                return i;
        return -1;
    }
    
    /**Checks if the enchantment is in the item's enchants list 
     * @param enchantKey the namespace key of the enchantment
     * @return true if the item has the enchantment else false*/
    public boolean hasEnchant(String enchantKey)
    {
        if(enchants == null)
            return false;
            
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
        if(enchants == null)
            throw new IllegalStateException("This item does not have any enchants!");
            
        if(index < 0 || index >= enchants.size())
            throw new IllegalArgumentException("The index is out of range of the enchants list!");
        
        String enchantKey = enchants.get(index).split(" ")[0];
        enchants.set(index, enchantKey + " " + level);
    }
    
    /**removes the specified enchant from the enchants list 
     * @param index the index of enchantment to remove*/
    public void removeEnchant(int index)
    {
        if(enchants == null)
            throw new IllegalStateException("This item does not have any enchants!");
        
        if(index < 0 || index >= enchants.size())
            throw new IllegalArgumentException("The index is out of range in the enchants list!");
        
        enchants.remove(index);
        
        if(enchants.size() == 0)
            enchants = null;
    }
    
    /**Adds the specified enchant to the item's enchants list
     * @param enchantKey the namespace key of the enchantment
     * @param level the level of the enchantment*/
    public void addEnchant(String enchantKey, int level) 
    {
        if(enchants == null)
            enchants = new ArrayList<>();
        
        enchants.add(enchantKey + " " + level); 
    }
    
    /**Removes all the item's enchants*/
    public void removeEnchants()
    {
        if(enchants != null)
        {
            enchants.clear();
            enchants = null;
        }
    }
    
    /**Checks if the enchantment data that the item has is invalid
     * @return true if the item has invalid enchantment data, false if all data is valid*/
    public boolean hasInvalidEnchants()
    {
        if(enchants == null)
            return false;
        
        for(String enchant : enchants)
        {
            String[] enchantData = enchant.split(" ");
            if(Enchantment.getByKey(NamespacedKey.minecraft(enchantData[0].toLowerCase())) == null)
                return true;
            else if(MerchantShops.isInteger(enchantData[1]) == false)
                return true;
        }
        return false;
    }
    
    /**Adds a list of strings to the item's lore, each entry in the list is a different line of lore
     * @param lore the list of lore to add*/
    public void addAllLore(List<String> lore)
    {
        if(this.lore == null)
            this.lore = new ArrayList<>();
        
        this.lore.addAll(lore);
    }
    
    /**Adds the specified string to the item's lore
     * @param loreline the line of lore to add*/
    public void addLore(String loreLine)
    {
        if(this.lore == null)
            this.lore = new ArrayList<>();
        
        this.lore.add(loreLine);
    }
    
    /**Removes all the item's lore*/
    public void removeLore() 
    { 
        if(lore != null)
        {
            lore.clear();
            lore = null;
        }
    }
    
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
    
    /**Sets the amount of xp levels the item costs to buy in bulk
     * @param bulkLevelCost the amount of levels the item costs to buy in bulk*/
    public void setBulkLevelCost(int bulkLevelCost) { this.bulkLevelCost = bulkLevelCost; }
    
    /**Sets the amount that the single buy amount is multiplied by when the player is buying in bulk
     * @param bulkBuyMultiplier the amount that the single buy amount is multiplied by when buying in bulk*/
    public void setBulkBuyMutiplier(int bulkBuyMultiplier) { this.bulkBuyMultiplier = bulkBuyMultiplier; }
    
    /**Sets the item's custom display name
     * @param displayName the custom display name of the item*/
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    /**Sets the UUID string of the player who owns the skull, only valid if the itemKey is of type minecraft:player_head
     * @param skullOwner the UUID of the player who owns the skull*/
    public void setSkullOwner(String skullOwner) { this.skullOwner = skullOwner; }
    
    /**Sets the base64 encoded string of the texture used by the skull, only valid if the itemKey is of type minecraft:player_head
     * @param skullTexture base64 encoded string of the texture used by the skull*/
    public void setSkullTexture(String skullTexture) { this.skullTexture = skullTexture; }
    
    /**Sets the item's lore
     * @param lore a list of strings containing the lore lines*/
    public void setLore(List<String> lore) { this.lore = lore; } 
    
    /**Sets the item's enchantments
     * @param enchants a list of strings containing the enchants*/
    public void setEnchants(List<String> enchants) { this.enchants = enchants; } 
    
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
    
    /**Gets the amount of xp levels it costs to buy in bulk
     * @return the amount of levels it costs to buy in bulk*/
    public int getBulkLevelCost() { return bulkLevelCost; }
    
    /**Gets the amount that the single buy amount is multiplied by when the player is buying in bulk
     * @return the amount that the single buy amount is multiplied by when buying in bulk*/
    public int getBulkBuyMutiplier() { return bulkBuyMultiplier; }
    
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
