package nl.eur.ese.bouman.tas.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class Instance
{
	private List<Assistant> assistants;
	private List<Group> groups;
	private SortedSet<Session> sessions;
	private CostInformation ci;
	
	public Instance()
	{
		assistants = new ArrayList<>();
		groups = new ArrayList<>();
		sessions = new TreeSet<>();
	}
	
	public void addAssistant(Assistant a)
	{
		assistants.add(a);
	}
	
	public void addGroup(Group g)
	{
		groups.add(g);
	}
	
	public void addSession(Session s)
	{
		sessions.add(s);
	}
	
	public SortedSet<Session> getSessions()
	{
		return Collections.unmodifiableSortedSet(sessions);
	}
	
	public List<Assistant> getAssistants()
	{
		return Collections.unmodifiableList(assistants);
	}
	
	public List<Group> getGroups()
	{
		return Collections.unmodifiableList(groups);
	}
	
	public CostInformation getCosts()
	{
		return ci;
	}
}
