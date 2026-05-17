package com.osrstracker.model;

import net.runelite.api.Skill;

import java.util.EnumMap;
import java.util.Map;

public enum F2pSkill
{
	HITPOINTS(Skill.HITPOINTS, "hitpoints"),
	ATTACK(Skill.ATTACK, "attack"),
	STRENGTH(Skill.STRENGTH, "strength"),
	DEFENCE(Skill.DEFENCE, "defence"),
	RANGED(Skill.RANGED, "ranged"),
	PRAYER(Skill.PRAYER, "prayer"),
	MAGIC(Skill.MAGIC, "magic"),
	MINING(Skill.MINING, "mining"),
	SMITHING(Skill.SMITHING, "smithing"),
	FISHING(Skill.FISHING, "fishing"),
	COOKING(Skill.COOKING, "cooking"),
	FIREMAKING(Skill.FIREMAKING, "firemaking"),
	WOODCUTTING(Skill.WOODCUTTING, "woodcutting"),
	CRAFTING(Skill.CRAFTING, "crafting"),
	RUNECRAFT(Skill.RUNECRAFT, "runecraft");

	public final Skill runeliteSkill;
	public final String key;

	F2pSkill(Skill runeliteSkill, String key)
	{
		this.runeliteSkill = runeliteSkill;
		this.key = key;
	}

	private static final Map<Skill, F2pSkill> BY_SKILL = new EnumMap<>(Skill.class);

	static
	{
		for (F2pSkill f : values())
		{
			BY_SKILL.put(f.runeliteSkill, f);
		}
	}

	public static F2pSkill fromSkill(Skill skill)
	{
		return BY_SKILL.get(skill);
	}
}