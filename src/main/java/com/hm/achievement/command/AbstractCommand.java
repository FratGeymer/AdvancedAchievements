package com.hm.achievement.command;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.utils.Reloadable;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Abstract class in charge of factoring out common functionality for commands.
 *
 * @author Pyves
 */
public abstract class AbstractCommand implements Reloadable {

	protected final AdvancedAchievements plugin;

	protected String configIcon;
	protected ChatColor configColor;

	private String langNoPermissions;

	protected AbstractCommand(AdvancedAchievements plugin) {
		this.plugin = plugin;
	}

	@Override
	public void extractConfigurationParameters() {
		configIcon = StringEscapeUtils.unescapeJava(plugin.getPluginConfig().getString("Icon", "\u2618"));
		configColor = ChatColor.getByChar(plugin.getPluginConfig().getString("Color", "5").charAt(0));

		langNoPermissions = Lang.getWithChatHeader(CmdLang.NO_PERMISSIONS, plugin);
	}

	/**
	 * Executes the command issued by the sender if he has the relevant permissions. If permission null, skip check.
	 *
	 * @param sender
	 * @param args
	 * @param permission
	 */
	public void executeCommand(CommandSender sender, String[] args, String permission) {
		if (permission != null && !sender.hasPermission("achievement." + permission)) {
			sender.sendMessage(langNoPermissions);
			return;
		}

		executeCommand(sender, args);
	}

	/**
	 * Executes the command issued by the sender.
	 *
	 * @param sender
	 * @param args
	 */
	protected abstract void executeCommand(CommandSender sender, String[] args);

	/**
	 * Plays a firework sound.
	 *
	 * @param player
	 */
	protected void playFireworkSound(Player player) {
		// If old version, retrieving sound by name as it no longer exists in newer versions.
		Sound sound = plugin.getServerVersion() < 9 ? Sound.valueOf("FIREWORK_BLAST")
				: Sound.ENTITY_FIREWORK_LARGE_BLAST;
		player.getWorld().playSound(player.getLocation(), sound, 1, 0.7f);
	}

	protected String translateColorCodes(String translate) {
		return ChatColor.translateAlternateColorCodes('&', translate);
	}
}
