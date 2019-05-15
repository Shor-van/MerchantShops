package github.shor_van.merchantshops;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

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
    private List<String> potEffects; //A list of potion effects
    
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
        this.potEffects = null;
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
    
    /**Adds the specified enchant to the item's enchants list
     * @param enchantKey the namespace key of the enchantment
     * @param level the level of the enchantment*/
    public void addEnchant(String enchantKey, int level) 
    {
        if(enchants == null)
            enchants = new ArrayList<>();
        
        enchants.add(enchantKey + " " + level); 
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
    
    /**Removes all the item's enchants*/
    public void removeEnchants()
    {
        if(enchants != null)
        {
            enchants.clear();
            enchants = null;
        }
    }
    
    /**Checks if the effect is in the item's effect list 
     * @param effectKey the namespace key of the effect
     * @return true if the item has the effect else false*/
    public boolean hasEffect(String effectKey)
    {
        if(potEffects == null)
            return false;
        
        for(String effect : potEffects)
            if(effect.split(" ")[0].toLowerCase().equals(effectKey.toLowerCase()))
                return true;
        return false;
    }
    
    /**Gets the index of the effect in the item's enchants list 
     * @param effectKey the namespace key of the effect
     * @return the index of the effect in the list, -1 if the item does not have the effect*/
    public int getEffectIndex(String effectKey)
    {
        if(potEffects == null)
            return -1;
        
        for(int i = 0; i < potEffects.size(); i++)
            if(potEffects.get(i).split(" ")[0].toLowerCase().equals(effectKey.toLowerCase()))
                return i;
        return -1;
    }
    
    /**Checks if the potion effect data that the item has is invalid
     * @return true if the item has invalid effect data, false if all data is valid*/
    public boolean hasInvalidEffects()
    {
        if(potEffects == null)
            return false;
        
        for(String effect : potEffects)
        {
            String[] effectData = effect.split(" ");
            if(PotionEffectType.getByName(effectData[0].toUpperCase()) == null)
                return true;
            else if(MerchantShops.isInteger(effectData[1]) == false)
                return true;
            else if(MerchantShops.isInteger(effectData[2]) == false)
                return true;
        }
        return false;
    }
    
    /**Sets the level of the potion effect at the specified index 
     * @param index the index of effect
     * @param level the level to set*/
    public void setEffectLevel(int index, int level)
    {
        if(potEffects == null)
            throw new IllegalStateException("This item does not have any effects!");
        
        if(index < 0 || index >= potEffects.size())
            throw new IllegalArgumentException("The index is out of range in the effects list!");
        
        String[] effectData = potEffects.get(index).split(" ");
        potEffects.set(index, effectData[0] + level + effectData[2]);
    }
    
    /**Sets the duration of the potion effect at the specified index 
     * @param index the index of effect
     * @param duration the duration to set*/
    public void setEffectDuration(int index, int duration)
    {
        if(potEffects == null)
            throw new IllegalStateException("This item does not have any effects!");
        
        if(index < 0 || index >= potEffects.size())
            throw new IllegalArgumentException("The index is out of range in the effects list!");
     
        String[] effectData = potEffects.get(index).split(" ");
        potEffects.set(index, effectData[0] + effectData[1] + duration);
    }
    
    /**Adds the specified potion effect to the item's effects list
     * @param potEffectKey the namespace key of the effect
     * @param level the level of the effect
     * @param duration the duration of the effect*/
    public void addEffect(String potEffectKey, int level, int duration)
    {
        if(potEffects == null)
            potEffects = new ArrayList<>();
        
        potEffects.add(potEffectKey + " " + level + " " + duration);
    }
    
    /**removes the specified effect from the effects list 
     * @param index the index of potion effect to remove*/
    public void removeEffect(int index)
    {
        if(potEffects == null)
            throw new IllegalStateException("This item does not have any effects!");
        
        if(index < 0 || index >= potEffects.size())
            throw new IllegalArgumentException("The index is out of range in the effects list!");
        
        potEffects.remove(index);
        
        if(potEffects.size() == 0)
            potEffects = null;
    }
    
    /**Removes all the item's potion effects*/
    public void removeEffects()
    {
        if(potEffects != null)
        {
            potEffects.clear();
            potEffects = null;
        }
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
    
    /**Sets the item's potion effects
     * @param effects a list of strings containing the potion effect data*/
    public void setEffects(List<String> effects) { this.potEffects = effects; }
    
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
    
    /**Gets a list of the item's potion effects, effect type, level and duration are seperated by space EG: "regeneration 2 165"
     * @return a list containing the item's enchants*/
    public List<String> getEffects() { return potEffects; }
}
