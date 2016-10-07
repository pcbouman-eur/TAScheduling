package nl.eur.ese.bouman.tas.data;

import java.util.Map;
import java.util.TreeMap;

public class SimpleAssistant extends Assistant
{
	private Map<Slot,Double> available;
	private int maxSess;

	public SimpleAssistant(String name, Map<Slot,Double> available, int maxSess)
	{
		super(name);
		this.available = new TreeMap<>(available);
		this.maxSess = maxSess;
	}

	@Override
	public boolean canCover(Session s)
	{
		return available.containsKey(s.getSlot())
		    && Double.isFinite(available.get(s.getSlot()));
	}

	@Override
	public int maximumSessions(String category)
	{
		return maxSess;
	}

	@Override
	public double getSlotCost(Session s)
	{
		return 0;
	}

	@Override
	public double getConsecutiveCost(int k)
	{
		if (k > 1)
		{
			return -1;
		}
		return 0;
	}

	@Override
	public double getCoverCost(String category, int number)
	{
		return maxSess - number;
	}

}
