package nl.eur.ese.bouman.tas.parse;

import java.util.HashMap;
import java.util.Map;

import nl.eur.ese.bouman.tas.data.Assistant;
import nl.eur.ese.bouman.tas.data.Session;
import nl.eur.ese.bouman.tas.data.Slot;

public class FormAssistant extends Assistant
{
	private int maxTotal;
	
	private Map<Integer,Preference> inARow;
	private Map<Slot,Preference> slotPrefs;
	private Map<String,Preference> groups;
	private PreferenceMap pm;

	public FormAssistant(String name, int max, Map<Slot,Preference> slots,
			Map<Integer,Preference> inARow, Map<String,Preference> groups,
			PreferenceMap pm)
	{
		super(name);
		this.maxTotal = max;
		
		this.inARow = new HashMap<>(inARow);
		this.slotPrefs = new HashMap<>(slots);
		this.groups = new HashMap<>(groups);
		this.pm = pm;
	}
	
	public FormAssistant(String name, int max, Map<Slot,Preference> slots,
			Map<Integer,Preference> inARow, Map<String,Preference> groups)
	{
		this(name, max, slots, inARow, groups, PreferenceMap.getDefault());
	}

	@Override
	public boolean canCover(Session s)
	{
		return slotPrefs.getOrDefault(s.getSlot(),Preference.UNAVAILABLE)
				!= Preference.UNAVAILABLE;
	}

	@Override
	public int maximumSessions(String category)
	{
		return maxTotal / 2;
	}

	@Override
	public double getSlotCost(Session s)
	{
		Slot slot = s.getSlot();
		String group = s.getGroup().getName();
		double slotCost = pm.getSlotCost(slotPrefs.getOrDefault(slot, Preference.NEUTRAL));
		double groupCost = pm.getGroupCost(groups.getOrDefault(group, Preference.NEUTRAL));
		return slotCost + groupCost;
	}

	@Override
	public double getConsecutiveCost(int k)
	{
		return pm.getRowCost(inARow.getOrDefault(k, Preference.NEUTRAL));
	}

	@Override
	public double getCoverCost(String category, int number)
	{
		return 0;
	}

}
