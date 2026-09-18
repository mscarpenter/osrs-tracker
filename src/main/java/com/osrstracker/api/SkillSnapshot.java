package com.osrstracker.api;

import java.util.Map;

public class SkillSnapshot
{
	public final String account_type;
	public final Map<String, Long> skills;

	public SkillSnapshot(String account_type, Map<String, Long> skills)
	{
		this.account_type = account_type;
		this.skills = skills;
	}
}