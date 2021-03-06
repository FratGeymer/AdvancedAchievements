package com.hm.achievement.command;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.mcshared.particle.ParticleEffect;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Abstract class in charge of factoring out common functionality for /aach top, week and month commands.
 *
 * @author Pyves
 */
public abstract class AbstractRankingCommand extends AbstractCommand {

	private static final int CACHE_EXPIRATION_DELAY = 60000;
	private static final int DECIMAL_CIRCLED_ONE = Integer.parseInt("2780", 16);
	private static final int DECIMAL_CIRCLED_ELEVEN = Integer.parseInt("246A", 16);
	private static final int DECIMAL_CIRCLED_TWENTY_ONE = Integer.parseInt("3251", 16);
	private static final int DECIMAL_CIRCLED_THIRTY_SIX = Integer.parseInt("32B1", 16);

	private final Lang languageHeader;

	private int configTopList;
	private boolean configAdditionalEffects;
	private boolean configSound;
	private String langPeriodAchievement;
	private String langPlayerRank;
	private String langNotRanked;
	// Used for caching.
	private Map<String, Integer> cachedSortedRankings;
	private List<Integer> cachedAchievementCounts;
	private long lastCacheUpdate = 0L;

	protected AbstractRankingCommand(AdvancedAchievements plugin, Lang languageHeader) {
		super(plugin);

		this.languageHeader = languageHeader;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configTopList = plugin.getPluginConfig().getInt("TopList", 5);
		configAdditionalEffects = plugin.getPluginConfig().getBoolean("AdditionalEffects", true);
		configSound = plugin.getPluginConfig().getBoolean("Sound", true);

		langPeriodAchievement = Lang.getWithChatHeader(languageHeader, plugin);
		langPlayerRank = Lang.getWithChatHeader(CmdLang.PLAYER_RANK, plugin) + " " + configColor;
		langNotRanked = Lang.getWithChatHeader(CmdLang.NOT_RANKED, plugin);
	}

	@Override
	public void executeCommand(CommandSender sender, String[] args) {
		if (System.currentTimeMillis() - lastCacheUpdate >= CACHE_EXPIRATION_DELAY) {
			// Update cached data structures.
			cachedSortedRankings = plugin.getDatabaseManager().getTopList(getRankingStartTime());
			cachedAchievementCounts = new ArrayList<>(cachedSortedRankings.values());
			lastCacheUpdate = System.currentTimeMillis();
		}

		sender.sendMessage(langPeriodAchievement);
		int currentRank = 1;
		for (Entry<String, Integer> ranking : cachedSortedRankings.entrySet()) {
			String playerName = Bukkit.getServer().getOfflinePlayer(UUID.fromString(ranking.getKey())).getName();
			if (playerName != null) {
				// Color the name of the player if he is in the top list.
				String color = ChatColor.GRAY + " ";
				if (sender instanceof Player && playerName.equals(((Player) sender).getName())) {
					color += configColor.toString();
				}
				sender.sendMessage(
						color + getRankingSymbol(currentRank) + " " + playerName + " - " + ranking.getValue());
			} else {
				plugin.getLogger().warning("Ranking command: could not find player's name using a database UUID.");
			}
			++currentRank;
			if (currentRank > configTopList) {
				break;
			}
		}

		if (sender instanceof Player) {
			Integer achievementsCount = cachedSortedRankings.get((((Player) sender).getUniqueId().toString()));
			// If not entry in the map, player has not yet received an achievement for this period, not ranked.
			if (achievementsCount == null) {
				sender.sendMessage(langNotRanked);
			} else {
				// Rank is the first index in the list that has received as many achievements as the player.
				int playerRank = cachedAchievementCounts.indexOf(achievementsCount) + 1;
				// Launch effect if player is in top list.
				if (playerRank <= configTopList) {
					launchEffects((Player) sender);
				}
				sender.sendMessage(
						langPlayerRank + playerRank + ChatColor.GRAY + "/" + configColor + cachedSortedRankings.size());
			}
		}
	}

	/**
	 * Returns an UTF-8 circled number based on the player's rank.
	 *
	 * @param rank
	 * @return an UTF-8 string corresponding to the rank
	 */
	private String getRankingSymbol(int rank) {
		int decimalRankSymbol;
		if (rank <= 10) {
			decimalRankSymbol = DECIMAL_CIRCLED_ONE + rank - 1;
		} else if (rank <= 20) {
			decimalRankSymbol = DECIMAL_CIRCLED_ELEVEN + rank - 11;
		} else if (rank <= 35) {
			decimalRankSymbol = DECIMAL_CIRCLED_TWENTY_ONE + rank - 21;
		} else {
			decimalRankSymbol = DECIMAL_CIRCLED_THIRTY_SIX + rank - 36;
		}
		return StringEscapeUtils.unescapeJava("\\u" + Integer.toHexString(decimalRankSymbol));
	}

	/**
	 * Returns start time for a specific ranking period.
	 *
	 * @return time (epoch) in millis
	 */
	protected abstract long getRankingStartTime();

	/**
	 * Launches sound and particle effects if player is in a top list.
	 *
	 * @param player
	 */
	private void launchEffects(Player player) {
		// Play special effect when in top list.
		if (configAdditionalEffects) {
			try {
				ParticleEffect.PORTAL.display(0, 1, 0, 0.5f, 1000, player.getLocation(), 1);
			} catch (Exception e) {
				plugin.getLogger().warning("Failed to display additional particle effects for rankings.");
			}
		}

		// Play special sound when in top list.
		if (configSound) {
			playFireworkSound(player);
		}
	}
}
