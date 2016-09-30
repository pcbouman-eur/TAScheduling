package nl.eur.ese.bouman.tas.data;

public class Slot implements Comparable<Slot>
{
	private final String day;
	private final int beginHour;
	private final int endHour;
	
	public Slot(String day, int beginHour, int endHour)
	{
		this.day = day.trim().toUpperCase();
		this.beginHour = beginHour;
		this.endHour = endHour;
	}
	
	public boolean overlaps(Slot other)
	{
		if (!day.equals(other.day))
		{
			return false;
		}
		return !(endHour <= other.beginHour || beginHour >= other.endHour);
	}
	
	public boolean adjacent(Slot other)
	{
		if (!day.equals(other.day))
		{
			return false;
		}
		return endHour == other.beginHour || beginHour == other.endHour;
	}
	
	@Override
	public String toString()
	{
		return day+" "+beginHour+"-"+endHour;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + beginHour;
		result = prime * result + ((day == null) ? 0 : day.hashCode());
		result = prime * result + endHour;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Slot other = (Slot) obj;
		if (beginHour != other.beginHour)
			return false;
		if (day == null) {
			if (other.day != null)
				return false;
		} else if (!day.equals(other.day))
			return false;
		if (endHour != other.endHour)
			return false;
		return true;
	}

	@Override
	public int compareTo(Slot other)
	{
		if (!day.equals(other.day))
		{
			return day.compareTo(other.day);
		}
		if (beginHour != other.beginHour)
		{
			return beginHour - other.beginHour;
		}
		return endHour - other.endHour;
	}
}
