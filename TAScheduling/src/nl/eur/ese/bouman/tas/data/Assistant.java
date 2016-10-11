package nl.eur.ese.bouman.tas.data;

public abstract class Assistant implements Comparable<Assistant>
{
	private final String name;
	
	public Assistant(String name)
	{
		this.name = name;
	}
	
	//public abstract boolean available(Slot s);
	public abstract boolean canCover(Session s);
	public abstract int maximumSessions(String category);
	public abstract int maximumSessions();
	
	public abstract double getSlotCost(Session s);
	public abstract double getConsecutiveCost(int k);
	public abstract double getCoverCost(String category, int number);
	
	public String getName()
	{
		return name;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
		
	@Override
	public int compareTo(Assistant other)
	{
		return name.compareTo(other.name);
	}
	
}
