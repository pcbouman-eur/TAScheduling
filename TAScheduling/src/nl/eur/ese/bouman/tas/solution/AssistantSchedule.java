package nl.eur.ese.bouman.tas.solution;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import nl.eur.ese.bouman.tas.data.Assistant;
import nl.eur.ese.bouman.tas.data.CostInformation;
import nl.eur.ese.bouman.tas.data.Group;
import nl.eur.ese.bouman.tas.data.Session;

import java.util.SortedSet;
import java.util.TreeSet;

public class AssistantSchedule implements Comparable<AssistantSchedule>
{
	private final Assistant assistant;
	private final TreeSet<Session> sessions;
	private final Map<String,Integer> counts;
	
	public AssistantSchedule(Assistant a)
	{
		this.assistant = a;
		this.sessions = new TreeSet<>();
		this.counts = new LinkedHashMap<>();
	}
	
	public boolean addSession(Session s)
	{
		boolean change = sessions.add(s);
		if (change)
		{
			counts.merge(s.getCategory(), 1, Integer::sum);
		}
		return change;
	}
	
	public boolean removeSession(Session s)
	{
		boolean change = sessions.remove(s);
		if (change)
		{
			counts.merge(s.getCategory(), -1, Integer::sum);
		}
		return change;
	}
	
	public SortedSet<Session> getSessions()
	{
		return Collections.unmodifiableSortedSet(sessions);
	}
	
	public int getRemainingCapacity(String cat)
	{
		return assistant.maximumSessions(cat) - counts.getOrDefault(cat, 0);
	}
	
	public Assistant getAssistant()
	{
		return assistant;
	}
	
	public AssistantSchedule copy()
	{
		AssistantSchedule result = new AssistantSchedule(assistant);
		result.sessions.addAll(sessions);
		result.counts.putAll(counts);
		return result;
	}
	
	public boolean isFeasible()
	{
		Map<String,Integer> counts = new LinkedHashMap<>();
		Session prev = null;
		for (Session s : sessions)
		{
			if (prev != null && prev.overlaps(s))
			{
				return false;
			}
			if (!assistant.canCover(s))
			{
				return false;
			}
			prev = s;
			counts.merge(s.getCategory(), 1, Integer::sum);
		}
		for (Entry<String,Integer> e : counts.entrySet())
		{
			if (e.getValue() > assistant.maximumSessions(e.getKey()))
			{
				return false;
			}
		}
		return true;
	}
	
	public double evaluateCosts(CostInformation ci)
	{
		double result = 0;
		Map<Group,Integer> groupCounts = new LinkedHashMap<>();
		Map<String,Integer> catCounts = new LinkedHashMap<>();
		
		// First compute consecutive session costs and covering costs
		Session prev = null;
		int consecutive = 0;
		for (Session s : sessions)
		{
			if (prev != null && prev.overlaps(s))
			{
				throw new IllegalStateException("An assitant cannot have overlapping sessions");
			}
			
			if (!assistant.canCover(s))
			{
				throw new IllegalStateException("The assistant cannot cover one of the sessions.");
			}
			
			if (prev != null && !prev.adjacent(s))
			{
				if (prev != null)
				{
					// Consecutive costs
					result += assistant.getConsecutiveCost(consecutive);
				}
				consecutive = 0;
			}
			else if (prev != null && prev.adjacent(s))
			{
				consecutive++;
			}
			// Slot costs
			result += assistant.getSlotCost(s.getSlot());
			result += ci.getShadowCosts(s);
			
			// Count group occurences
			groupCounts.merge(s.getGroup(), 1, Integer::sum);
			
			// Count category occurences
			catCounts.merge(s.getCategory(), 1, Integer::sum);
			
			// Store the current as the previous
			prev = s;
		}

		result += assistant.getConsecutiveCost(consecutive);

		for (Entry<String,Integer> e : catCounts.entrySet())
		{
			if (assistant.maximumSessions(e.getKey()) < e.getValue())
			{
				throw new IllegalStateException("Assitant violates maximum "
						+ "number of sessions for category "+e.getKey());
			}
			
			result += assistant.getCoverCost(e.getKey(), e.getValue());
		}

		
		for (Entry<Group,Integer> e : groupCounts.entrySet())
		{
			result += ci.getSameGroupCost(e.getKey(), e.getValue());
		}
		
		return result;
	}
	
	@Override
	public String toString()
	{
		return assistant + " -> " + sessions;
	}

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assistant == null) ? 0 : assistant.getName().hashCode());
		result = prime * result + ((sessions == null) ? 0 : sessions.hashCode());
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
		AssistantSchedule other = (AssistantSchedule) obj;
		if (assistant == null) {
			if (other.assistant != null)
				return false;
		} else if (!assistant.getName().equals(other.assistant.getName()))
			return false;
		if (sessions == null) {
			if (other.sessions != null)
				return false;
		} else if (!sessions.equals(other.sessions))
			return false;
		return true;
	}

	@Override
	public int compareTo(AssistantSchedule o)
	{
		if (!assistant.getName().equals(o.getAssistant().getName()))
		{
			return assistant.getName().compareTo(o.getAssistant().getName());
		}
		if (sessions.size() != o.sessions.size())
		{
			return o.sessions.size() - sessions.size();
		}
		
		Iterator<Session> it1 = sessions.iterator();
		Iterator<Session> it2 = o.sessions.iterator();
		
		while (it1.hasNext())
		{
			Session s1 = it1.next();
			Session s2 = it2.next();
			if (!s1.equals(s2))
			{
				return s1.compareTo(s2);
			}
		}
		
		return 0;
	}
	
}
