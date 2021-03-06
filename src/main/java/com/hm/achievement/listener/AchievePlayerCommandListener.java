package com.hm.achievement.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;

public class AchievePlayerCommandListener extends AbstractListener {

	public AchievePlayerCommandListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();

		if (!shouldIncreaseBeTakenIntoAccountNoPermissions(player)) {
			return;
		}

		MultipleAchievements category = MultipleAchievements.PLAYERCOMMANDS;

		List<String> equivalentCommands = getEquivalentCommands(event.getMessage());
		for (String prefix : plugin.getPluginConfig().getShallowKeys(category.toString())) {
			for (String equivalentCommand : equivalentCommands) {
				if (equivalentCommand.startsWith(prefix)) {
					if (player.hasPermission(category.toPermName() + '.' + StringUtils.deleteWhitespace(prefix))) {
						updateStatisticAndAwardAchievementsIfAvailable(player, category, prefix, 1);
					}
					return;
				}
			}
		}
	}

	/**
	 * Computes a list containing equivalent commands of an input command. For instance, if input is "/aach stats", the
	 * returned list is: ["aach stats", "advancedachievements stats", "aachievements stats", "aa stats"]
	 * 
	 * @param command
	 * @return the list of commands have the same effect as the input one
	 */
	private List<String> getEquivalentCommands(String command) {
		int firstSpaceIndex = command.indexOf(' ');
		String commandName;
		String commandParameters;
		if (firstSpaceIndex >= 0) {
			commandName = command.substring(1, firstSpaceIndex);
			// Command parameters start with an initial space.
			commandParameters = command.substring(firstSpaceIndex).toLowerCase();
		} else {
			commandName = command.substring(1);
			commandParameters = "";
		}

		PluginCommand pluginCommand = plugin.getServer().getPluginCommand(commandName);
		if (pluginCommand == null || pluginCommand.getAliases() == null) {
			return Arrays.asList(commandName.toLowerCase() + commandParameters);
		}

		List<String> equivalentCommands = new ArrayList<>(pluginCommand.getAliases().size() + 1);
		// Aliases don't contain the main plugin command, add it to the returned list.
		equivalentCommands.add(pluginCommand.getName().toLowerCase() + commandParameters);
		for (String alias : pluginCommand.getAliases()) {
			equivalentCommands.add(alias.toLowerCase() + commandParameters);
		}
		return equivalentCommands;
	}
}
