package nl.eur.ese.bouman.tas.data;

public class Session implements Comparable<Session>
{
	private final Group group;
	private final Slot slot;
	private final String category;
	
	public Session(Group group, Slot slot)
	{
		this(group, slot, "");
	}
	
	public Session(Group group, Slot slot, String category)
	{
		this.group = group;
		this.slot = slot;
		this.category = category;
		
		group.addSession(this);
	}
	
	public boolean adjacent(Session other)
	{
		return slot.adjacent(other.slot);
	}
	
	public boolean overlaps(Session other)
	{
		return slot.overlaps(other.slot);
	}
	
	public Slot getSlot()
	{
		return slot;
	}
	
	public Group getGroup()
	{
		return group;
	}
	
	public String getCategory()
	{
		return category;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((slot == null) ? 0 : slot.hashCode());
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
		Session other = (Session) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (slot == null) {
			if (other.slot != null)
				return false;
		} else if (!slot.equals(other.slot))
			return false;
		return true;
	}



	@Override
	public int compareTo(Session o)
	{
		if (!slot.equals(o.slot))
		{
			return slot.compareTo(o.slot);
		}
		if (!category.equals(o.category))
		{
			return category.compareTo(o.category);
		}
		return group.getName().compareTo(o.group.getName());
	}
	
	@Override
	public String toString()
	{
		return group+" / " + category + " @ " + slot;
	}
	
}
