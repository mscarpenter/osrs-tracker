package com.osrstracker;

import com.google.inject.Provides;
import com.osrstracker.api.ApiClient;
import com.osrstracker.api.SkillSnapshot;
import com.osrstracker.model.F2pSkill;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import okhttp3.Call;
import okhttp3.Response;

import javax.inject.Inject;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@PluginDescriptor(
	name = "OSRS Tracker",
	description = "Tracks F2P skill XP and recommends training methods",
	tags = {"xp", "skills", "tracker", "f2p"}
)
public class OsrsTrackerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OsrsTrackerConfig config;

	@Inject
	private ApiClient apiClient;

	private OsrsTrackerPanel panel;
	private NavigationButton navButton;

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> syncTask;

	// Escritos no client thread via eventos, lidos no executor thread em sendSnapshot.
	// volatile garante visibilidade dos campos simples; synchronized guarda os mapas.
	private volatile String cachedUsername;
	private volatile String cachedAccountType;
	private final Map<F2pSkill, Long> xpCache = new EnumMap<>(F2pSkill.class);
	private final Map<F2pSkill, Integer> levelCache = new EnumMap<>(F2pSkill.class);

	@Override
	protected void startUp()
	{
		panel = new OsrsTrackerPanel();

		BufferedImage icon = ImageUtil.loadImageResource(OsrsTrackerPlugin.class, "/icon.png");
		if (icon == null)
		{
			icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		}

		navButton = NavigationButton.builder()
			.tooltip("OSRS Tracker")
			.icon(icon)
			.panel(panel)
			.priority(5)
			.build();

		clientToolbar.addNavigation(navButton);
		log.info("OSRS Tracker started");
	}

	@Override
	protected void shutDown()
	{
		if (syncTask != null)
		{
			syncTask.cancel(false);
			syncTask = null;
		}
		executor.shutdownNow();
		clientToolbar.removeNavigation(navButton);
		panel = null;

		synchronized (xpCache)
		{
			xpCache.clear();
		}
		synchronized (levelCache)
		{
			levelCache.clear();
		}

		cachedUsername = null;
		cachedAccountType = null;
		log.info("OSRS Tracker stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			if (client.getLocalPlayer() != null)
			{
				cachedUsername = client.getLocalPlayer().getName();
			}
			cachedAccountType = client.getAccountType().name();

			SwingUtilities.invokeLater(() -> panel.setUsername(cachedUsername));
			scheduleSyncTask();
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			cancelSyncTask();
			cachedUsername = null;
			cachedAccountType = null;

			synchronized (xpCache)
			{
				xpCache.clear();
			}
			synchronized (levelCache)
			{
				levelCache.clear();
			}

			SwingUtilities.invokeLater(() -> panel.reset());
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		F2pSkill skill = F2pSkill.fromSkill(event.getSkill());
		if (skill == null)
		{
			return;
		}

		synchronized (xpCache)
		{
			xpCache.put(skill, (long) event.getXp());
		}
		synchronized (levelCache)
		{
			levelCache.put(skill, event.getLevel());
		}

		final F2pSkill finalSkill = skill;
		final int xp = event.getXp();
		final int level = event.getLevel();
		SwingUtilities.invokeLater(() -> panel.updateSkill(finalSkill, xp, level));
	}

	private void scheduleSyncTask()
	{
		cancelSyncTask();
		int intervalMinutes = Math.max(1, config.syncIntervalMinutes());
		syncTask = executor.scheduleAtFixedRate(
			this::sendSnapshot,
			intervalMinutes,
			intervalMinutes,
			TimeUnit.MINUTES
		);
	}

	private void cancelSyncTask()
	{
		if (syncTask != null)
		{
			syncTask.cancel(false);
			syncTask = null;
		}
	}

	private void sendSnapshot()
	{
		if (!config.enableSync())
		{
			return;
		}

		String username = cachedUsername;
		String accountType = cachedAccountType;
		if (username == null || accountType == null)
		{
			return;
		}

		final Map<F2pSkill, Long> xpSnapshot;
		final Map<F2pSkill, Integer> levelSnapshot;

		synchronized (xpCache)
		{
			xpSnapshot = new EnumMap<>(xpCache);
		}
		synchronized (levelCache)
		{
			levelSnapshot = new EnumMap<>(levelCache);
		}

		Map<String, Long> skills = new HashMap<>();
		long totalXp = 0;
		int totalLevel = 0;

		for (F2pSkill skill : F2pSkill.values())
		{
			long xp = xpSnapshot.getOrDefault(skill, 0L);
			skills.put(skill.key, xp);
			totalXp += xp;
			totalLevel += levelSnapshot.getOrDefault(skill, 1);
		}

		skills.put("total_level", (long) totalLevel);
		skills.put("total_xp", totalXp);

		SkillSnapshot snapshot = new SkillSnapshot(username, accountType, skills);

		apiClient.sendSnapshot(snapshot, new okhttp3.Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.debug("Failed to send snapshot", e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				try (response)
				{
					if (!response.isSuccessful())
					{
						log.debug("Snapshot response error: {}", response.code());
					}
				}
			}
		});
	}

	@Provides
	OsrsTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(OsrsTrackerConfig.class);
	}
}
