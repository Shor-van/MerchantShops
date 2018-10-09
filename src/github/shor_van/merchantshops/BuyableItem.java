package github.shor_van.merchantshops;

import java.util.ArrayList;
import java.util.List;

public class BuyableItem
{
	private String itemKey;
	private int damage;
	private int amount;
	private int levelCost;
	private String displayName;
	private String skullOwner;
	private String skullTexture;
	private List<String> lore;
	private List<String> enchants;
	
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
	
	public void setItemKey(String itemKey) { this.itemKey = itemKey; } 
	
	public void setDamage(int damage) { this.damage = damage; }
	
	public void setAmount(int amount) { this.amount = amount; }
	
	public void setLevelCost(int levelCost) { this.levelCost = levelCost; }
	
	public void setDisplayName(String displayName) { this.displayName = displayName; }
	
	public void setSkullOwner(String skullOwner) { this.skullOwner = skullOwner; }
	
	public void setSkullTexture(String skullTexture) { this.skullTexture = skullTexture; }
	
	public String getItemKey() { return itemKey; }
	
	public int getDamage() { return damage; }
	
	public int getAmount() { return amount; }
	
	public int getLevelCost() { return levelCost; }
	
	public String getDisplayName() { return displayName; }
	
	public String getSkullOwner() { return skullOwner; }
	
	public String getSkullTexture() { return skullTexture; }
	
	public List<String> getLore() { return lore; }
	
	public List<String> getEnchants() { return enchants; }
}
