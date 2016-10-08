package nl.eur.ese.bouman.tas.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import nl.eur.ese.bouman.tas.data.Assistant;
import nl.eur.ese.bouman.tas.data.CostInformation;
import nl.eur.ese.bouman.tas.data.Instance;
import nl.eur.ese.bouman.tas.data.Session;
import nl.eur.ese.bouman.tas.solution.AssistantSchedule;

public class TASolver
{
	private Assistant assistant;
	private CostInformation ci;
	private BranchInformation bi;
	private List<Session> sessions;
	
	private int maxSols;
	private TreeSet<ScheduleNode> bestSols;

	public TASolver(Instance i, Assistant as, BranchInformation bi)
	{
		this(i, as, bi, Integer.MAX_VALUE);
	}


	public TASolver(Instance i, Assistant as, BranchInformation bi, int maxSols)
	{
		this.assistant = as;
		this.bi = bi;
		this.ci = i.getCosts();
		
		this.sessions = i.getSessions()
				         .stream()
				         .filter(s -> as.canCover(s) && !bi.isForbidden(as, s))
				         .collect(Collectors.toList());
		
		this.maxSols = maxSols;
	}
	
	public void run()
	{
		bestSols = new TreeSet<>();
		List<Session> fixed = sessions.stream()
				                      .filter( s -> bi.isFixed(assistant, s))
				                      .collect(Collectors.toList());
		List<Session> choose = new ArrayList<>(sessions);
		choose.removeAll(fixed);
		AssistantSchedule as = new AssistantSchedule(assistant);
		fixed.forEach(s -> as.addSession(s));
		search(choose, as, null);
	}
	
	public List<AssistantSchedule> getSchedules()
	{
		return bestSols.stream()
				       .map(sn -> sn.schedule)
				       .collect(Collectors.toList());
	}
	
	private void search(List<Session> sessions, AssistantSchedule as, Session last)
	{
		boolean success = false;
		for (int t=0; t < sessions.size(); t++)
		{
			Session toAdd = sessions.get(t);
			if (   (last != null && last.overlaps(toAdd))
				|| as.getRemainingCapacity(toAdd.getCategory()) <= 0 )
			{
				// If the last session we added overlaps with the new one, skip it
				// immediately, as it will surely result in an infeasible solution.
				// If the TA has no more capacity to cover this session, also skip
				// it immediately.
				continue;
			}
			as.addSession(toAdd);
			if (as.isFeasible())
			{
				List<Session> subList = sessions.subList(t+1, sessions.size());
				// Filter out all sessions that overlap or have not enough capacity left.
				subList = subList.stream()
						         .filter(s -> !toAdd.overlaps(s) &&
						        		 as.getRemainingCapacity(s.getCategory()) > 0)
						         .collect(Collectors.toList());
				search(subList, as, toAdd);
				success = true;
			}
			as.removeSession(toAdd);
		}
		// Only commit the current solution if we cannot expand it
		if (!success)
		{
			commit(as);
		}
	}
	
	private void commit(AssistantSchedule as)
	{
		double cost = as.evaluateCosts(ci);
		
		if (!ci.isRelevant(assistant, cost))
		{
			return;
		}
		
		if (bestSols.size() < maxSols || cost > bestSols.last().cost)
		{
			bestSols.add(new ScheduleNode(as.copy(), cost));
			while (bestSols.size() > maxSols)
			{
				bestSols.remove(bestSols.last());
			}
		}
	}
	
	
	private static class ScheduleNode implements Comparable<ScheduleNode>
	{
		public final AssistantSchedule schedule;
		public final double cost;
		
		public ScheduleNode(AssistantSchedule as, double c)
		{
			this.schedule = as;
			this.cost = c;
		}
		
		
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(cost);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + ((schedule == null) ? 0 : schedule.hashCode());
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
			ScheduleNode other = (ScheduleNode) obj;
			if (Double.doubleToLongBits(cost) != Double.doubleToLongBits(other.cost))
				return false;
			if (schedule == null) {
				if (other.schedule != null)
					return false;
			} else if (!schedule.equals(other.schedule))
				return false;
			return true;
		}



		@Override
		public int compareTo(ScheduleNode o)
		{
			if (cost != o.cost)
			{
				return (int)Math.signum(cost - o.cost);
			}
			return schedule.compareTo(o.schedule);
		}
		
		@Override
		public String toString()
		{
			return "[c="+cost+"] "+schedule;
		}
		
	}
	
}
