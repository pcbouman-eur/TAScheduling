package nl.eur.ese.bouman.tas.data;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

public class Group
{
	private final String name;
	private TreeSet<Session> sessions;
	
	public Group(String name)
	{
		this.name = name;
		this.sessions = new TreeSet<>();
	}
	
	public void addSession(Session s)
	{
		sessions.add(s);
	}
	
	public String getName()
	{
		return name;
	}
	
	public SortedSet<Session> getSessions()
	{
		return Collections.unmodifiableSortedSet(sessions);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Group other = (Group) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
