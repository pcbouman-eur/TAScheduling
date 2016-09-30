package nl.eur.ese.bouman.tas.solver;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.function.Function;

import nl.eur.ese.bouman.tas.data.Assistant;
import nl.eur.ese.bouman.tas.data.Session;
import nl.eur.ese.bouman.tas.solution.AssistantSchedule;

public interface BranchInformation
{
	public default boolean isFixed(Assistant a, Session s)
	{
		return getFixed(a).contains(s);
	}
	
	public default boolean isForbidden(Assistant a, Session s)
	{
		return getForbidden(a).contains(s);
	}
	
	public default boolean isPossible(AssistantSchedule as)
	{
		Collection<Session> fixed = getFixed(as.getAssistant());
		Collection<Session> forbidden = getForbidden(as.getAssistant());
		SortedSet<Session> sessions = as.getSessions();
		
		
		return sessions.containsAll(fixed) &&
			   !sessions.stream().anyMatch(forbidden::contains);
	}
	
	public Collection<Session> getFixed(Assistant a);
	public Collection<Session> getForbidden(Assistant a);
	
	public static BranchInformation getDefault()
	{
		return build( a -> Collections.emptySet(), a -> Collections.emptySet() );
	}
	
	public static BranchInformation build(Function<Assistant,Collection<Session>> fixed,
			                              Function<Assistant,Collection<Session>> forbidden)
	{
		return new BranchInformation()
		{

			@Override
			public Collection<Session> getFixed(Assistant a)
			{
				return fixed.apply(a);
			}

			@Override
			public Collection<Session> getForbidden(Assistant a)
			{
				return forbidden.apply(a);
			}
			
		};
	}
}
