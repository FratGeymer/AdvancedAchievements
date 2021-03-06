package com.hm.achievement.listener;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with Trades, AnvilsUsed, Smelting and Brewing achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveTradeAnvilBrewSmeltListener extends AbstractRateLimitedListener {

	public AchieveTradeAnvilBrewSmeltListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void cleanPlayerData(UUID uuid) {
		String uuidString = uuid.toString();
		// The cooldown for this class only handles Brewing achievements, but each slot in the brewing stand must be
		// handled independently, hence the prefix in the cooldown map.
		cooldownMap.remove("0" + uuidString);
		cooldownMap.remove("1" + uuidString);
		cooldownMap.remove("2" + uuidString);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getRawSlot() < 0 || event.getRawSlot() > 2 || event.getCurrentItem() == null
				|| event.getCurrentItem().getType() == Material.AIR || event.getAction() == InventoryAction.NOTHING
				|| event.getClick() == ClickType.NUMBER_KEY
						&& event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		NormalAchievements category;
		InventoryType inventoryType = event.getInventory().getType();
		if (event.getRawSlot() == 2 && inventoryType == InventoryType.MERCHANT) {
			category = NormalAchievements.TRADES;
		} else if (event.getRawSlot() == 2 && inventoryType == InventoryType.ANVIL) {
			category = NormalAchievements.ANVILS;
		} else if (inventoryType == InventoryType.BREWING
				&& (event.getCurrentItem().getType() == Material.POTION
						|| plugin.getServerVersion() >= 9 && event.getCurrentItem().getType() == Material.SPLASH_POTION)
				&& !isWaterPotion(event.getCurrentItem())) {
			category = NormalAchievements.BREWING;
		} else if (event.getRawSlot() == 2 && inventoryType == InventoryType.FURNACE) {
			category = NormalAchievements.SMELTING;
		} else {
			return;
		}

		if (plugin.getDisabledCategorySet().contains(category.toString())
				|| !shouldIncreaseBeTakenIntoAccount(player, category) || category == NormalAchievements.BREWING
						&& isInCooldownPeriod(player, Integer.toString(event.getRawSlot()), false, category)) {
			return;
		}

		int eventAmount = event.getCurrentItem().getAmount();
		if (event.isShiftClick()) {
			eventAmount = Math.min(eventAmount, getInventoryAvailableSpace(player, event.getCurrentItem()));
			if (eventAmount == 0) {
				return;
			}
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, eventAmount);
	}
}
