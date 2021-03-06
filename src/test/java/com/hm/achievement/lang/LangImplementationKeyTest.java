package com.hm.achievement.lang;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.lang.command.HelpLang;
import com.hm.achievement.lang.command.InfoLang;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.MockUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 * Tests that all keys used in Lang implementations can be found in lang.yml.
 * 
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.class)
public class LangImplementationKeyTest {

	@ClassRule
	public static final TemporaryFolder temporaryFolder = new TemporaryFolder();

	private AdvancedAchievements plugin;

	@Before
	public void setUp() throws Exception {
		MockUtility mockUtility = MockUtility.setUp()
				.withLogger()
				.withPluginDescription()
				.withDataFolder(temporaryFolder.getRoot())
				.withPluginLang();
		plugin = mockUtility.getPluginMock();
	}

	@Test
	public void testLangImplForWrongKeys() {
		List<Lang> langImpl = Arrays.stream(
				new Lang[][] {
						CmdLang.values(),
						HelpLang.values(),
						HelpLang.Hover.values(),
						InfoLang.values(),
						GuiLang.values(),
						ListenerLang.values(),
						RewardLang.values(),
						NormalAchievements.values(),
						MultipleAchievements.values()
				}).flatMap(Arrays::stream).collect(Collectors.toList());

		List<String> missing = new ArrayList<>();
		for (Lang lang : langImpl) {
			String key = lang.toLangKey();
			if (!plugin.getPluginLang().contains(key)) {
				missing.add(key + " (" + lang + ")");
			}
		}
		assertTrue("lang.yml is missing keys for: " + missing.toString(), missing.isEmpty());
	}

}
