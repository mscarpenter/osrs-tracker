package com.osrstracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("osrs-tracker")
public interface OsrsTrackerConfig extends Config
{
	@ConfigItem(
		keyName = "enableSync",
		name = "Enable XP Sync",
		description = "Send XP snapshots to the OSRS Tracker backend",
		warning = "This feature submits your IP address to a 3rd-party server not controlled or verified by RuneLite developers"
	)
	default boolean enableSync()
	{
		return false;
	}

	@ConfigItem(
		keyName = "syncIntervalMinutes",
		name = "Sync interval (minutes)",
		description = "How often to send XP snapshots (minimum: 1)"
	)
	default int syncIntervalMinutes()
	{
		return 5;
	}
}