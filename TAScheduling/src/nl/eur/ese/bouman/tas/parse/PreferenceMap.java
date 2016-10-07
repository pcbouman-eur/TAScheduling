package nl.eur.ese.bouman.tas.parse;

import java.util.HashMap;
import java.util.Map;

public class PreferenceMap
{
	private Map<Preference,Double> slotCost;
	private Map<Preference,Double> rowCost;
	private Map<Preference,Double> groupCost;
	
	public PreferenceMap()
	{
		this.slotCost = new HashMap<Preference,Double>();
		this.rowCost = new HashMap<Preference,Double>();
		this.groupCost = new HashMap<Preference,Double>();
	}
	
	public void putSlotCost(Preference p, double d)
	{
		slotCost.put(p, d);
	}
	
	public void putGroupCost(Preference p, double d)
	{
		groupCost.put(p, d);
	}
	
	public void putRowCost(Preference p, double d)
	{
		rowCost.put(p, d);
	}
	
	public void putAll(Preference p, double d)
	{
		putSlotCost(p,d);
		putGroupCost(p,d);
		putRowCost(p,d);
	}
	
	public double getSlotCost(Preference p)
	{
		return slotCost.getOrDefault(p, 0d);
	}
	
	public double getRowCost(Preference p)
	{
		return rowCost.getOrDefault(p, 0d);
	}
	
	public double getGroupCost(Preference p)
	{
		return groupCost.getOrDefault(p, 0d);
	}
	
	public static PreferenceMap getDefault()
	{
		PreferenceMap result = new PreferenceMap();
		result.putAll(Preference.DISFAVORED, -5);
		result.putAll(Preference.SLIGHTLY_DISFAVORED, -1);
		result.putAll(Preference.NEUTRAL, 0);
		result.putAll(Preference.SLIGHTLY_FAVORED, 1);
		result.putAll(Preference.FAVORED, 5);
		result.putAll(Preference.UNAVAILABLE, Double.NEGATIVE_INFINITY);
		result.putAll(Preference.UNKNOWN, Double.NEGATIVE_INFINITY);
		return result;
	}
	
}
