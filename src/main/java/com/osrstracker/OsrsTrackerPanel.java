package com.osrstracker;

import com.osrstracker.model.F2pSkill;
import net.runelite.client.ui.PluginPanel;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.Map;

public class OsrsTrackerPanel extends PluginPanel
{
	private final JLabel usernameLabel = new JLabel("Not logged in", SwingConstants.CENTER);
	private final Map<F2pSkill, JLabel> xpLabels = new EnumMap<>(F2pSkill.class);

	OsrsTrackerPanel()
	{
		setLayout(new BorderLayout(0, 8));
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		usernameLabel.setForeground(Color.WHITE);
		add(usernameLabel, BorderLayout.NORTH);

		JPanel skillsPanel = new JPanel(new GridLayout(0, 1, 0, 2));
		skillsPanel.setBackground(getBackground());

		for (F2pSkill skill : F2pSkill.values())
		{
			JLabel label = new JLabel(skill.key + ": —");
			label.setForeground(Color.LIGHT_GRAY);
			xpLabels.put(skill, label);
			skillsPanel.add(label);
		}

		add(skillsPanel, BorderLayout.CENTER);
	}

	void setUsername(String username)
	{
		usernameLabel.setText(username != null ? username : "Not logged in");
	}

	void updateSkill(F2pSkill skill, int xp, int level)
	{
		JLabel label = xpLabels.get(skill);
		if (label != null)
		{
			label.setText(String.format("%s: Lv %d (%,d xp)", skill.key, level, xp));
		}
	}

	void reset()
	{
		usernameLabel.setText("Not logged in");
		xpLabels.values().forEach(l -> l.setText(l.getText().split(":")[0] + ": —"));
	}
}