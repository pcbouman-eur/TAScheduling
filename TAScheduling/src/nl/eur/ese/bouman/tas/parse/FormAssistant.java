package nl.eur.ese.bouman.tas.parse;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nl.eur.ese.bouman.tas.data.Assistant;
import nl.eur.ese.bouman.tas.data.Group;
import nl.eur.ese.bouman.tas.data.Session;
import nl.eur.ese.bouman.tas.data.Slot;

public class FormAssistant extends Assistant
{
	private int maxPerCat;
	private int maxTotal;
	
	private Map<Integer,Preference> inARow;
	private Map<Slot,Preference> slotPrefs;
	private Map<String,Preference> groups;
	private PreferenceMap pm;

	public FormAssistant(String name, int max, Map<Slot,Preference> slots,
			Map<Integer,Preference> inARow, Map<String,Preference> groups,
			PreferenceMap pm)
	{
		this(name, max, max, slots, inARow, groups, pm);
	}
	
	public FormAssistant(String name, int max, int maxPerCat, 
			Map<Slot,Preference> slots, Map<Integer,Preference> inARow,
			Map<String,Preference> groups, PreferenceMap pm)
	{
		super(name);
		this.maxTotal = max;
		this.maxPerCat = maxPerCat;
		
		this.inARow = new HashMap<>(inARow);
		this.slotPrefs = new HashMap<>(slots);
		this.groups = new HashMap<>(groups);
		this.pm = pm;
	}
	
	public FormAssistant(String name, int max, int maxPerCat,
			Map<Slot,Preference> slots, Map<Integer,Preference> inARow,
			Map<String,Preference> groups)
	{
		this(name, max, slots, inARow, groups, PreferenceMap.getDefault());
	}

	public FormAssistant(String name, int max, Map<Slot,Preference> slots,
			Map<Integer,Preference> inARow, Map<String,Preference> groups)
	{
		this(name, max, max, slots, inARow, groups);
	}

	@Override
	public boolean canCover(Session s)
	{
		return  slotPrefs.getOrDefault(s.getSlot(),Preference.UNAVAILABLE)
				!= Preference.UNAVAILABLE
		    &&  getGroupPref(s.getGroup()) != Preference.UNAVAILABLE;
	}

	@Override
	public int maximumSessions(String category)
	{
		return maxPerCat;
	}

	@Override
	public double getSlotCost(Session s)
	{
		Slot slot = s.getSlot();
		double slotCost = pm.getSlotCost(slotPrefs.getOrDefault(slot, Preference.NEUTRAL));
		double groupCost = pm.getGroupCost(getGroupPref(s.getGroup()));
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
	
	private Preference getGroupPref(Group g)
	{
		String name = g.getName().toLowerCase();
		for (Entry<String,Preference> e : groups.entrySet())
		{
			if (name.contains(e.getKey().toLowerCase()))
			{
				return e.getValue();
			}
		}
		return Preference.NEUTRAL;
	}

	@Override
	public int maximumSessions()
	{
		return maxTotal;
	}

	public Preference getPreference(Slot s)
	{
		return slotPrefs.getOrDefault(s, Preference.UNKNOWN);
	}
	
}
