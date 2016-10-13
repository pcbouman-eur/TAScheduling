package nl.eur.ese.bouman.tas.solver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import nl.eur.ese.bouman.tas.data.Assistant;
import nl.eur.ese.bouman.tas.data.CostInformation;
import nl.eur.ese.bouman.tas.data.Instance;
import nl.eur.ese.bouman.tas.data.Session;
import nl.eur.ese.bouman.tas.data.Slot;
import nl.eur.ese.bouman.tas.solution.AssistantSchedule;
import nl.eur.ese.bouman.tas.solution.Solution;

public class CplexReschedule extends CplexSolver
{
	private Solution original;
	private Distance<AssistantSchedule> dist;

	public CplexReschedule(Instance i, Solution org, List<AssistantSchedule> as,
			Distance<AssistantSchedule> dist, boolean integer) throws IloException
	{
		super(i, as, integer);
		this.original = org;
		this.dist = dist;
	}
	
	@Override
	protected void initObjective() throws IloException
	{
		CostInformation ci = instance.getCosts();
		IloNumExpr expr = model.constant(0);
		for (Entry<Session,IloNumVar> e : dummyVars.entrySet())
		{
			double cost = ci.getPenalty(e.getKey());
			expr = model.sum(expr, model.prod(cost, e.getValue()));
		}
		for (Entry<AssistantSchedule, IloNumVar> e : scheduleVars.entrySet())
		{
			AssistantSchedule as = e.getKey();
			double cost = as.evaluateCosts(ci);
			AssistantSchedule os = original.getSchedule(as.getAssistant());
			if (os != null && os.isFeasible())
			{
				// We now should consider the change between these two schedules
				cost -= dist.apply(os, as);
			}
			expr = model.sum(expr, model.prod(cost, e.getValue()));
		}
		model.addMaximize(expr);
	}

	@Override
	public Solution getSolution() throws IloException
	{
		Solution newSol = super.getSolution();
		
		for (Assistant a : instance.getAssistants())
		{
			AssistantSchedule oldAS = original.getSchedule(a);
			AssistantSchedule newAS = newSol.getSchedule(a);
			if (!oldAS.isFeasible() || dist.apply(oldAS, newAS) > 0)
			{
				newSol.setAffected(a);
			}
		}

		return newSol;
	}
	
	@FunctionalInterface
	public static interface Distance<E> extends BiFunction<E,E,Double> {}
	
	public static class TimeSlotDistance implements Distance<AssistantSchedule>
	{
		private double general;
		private double perSlot;
		
		public TimeSlotDistance(double general, double perSlot)
		{
			this.general = general;
			this.perSlot = perSlot;
		}
		
		@Override
		public Double apply(AssistantSchedule t, AssistantSchedule u)
		{
			SortedSet<Slot> tSet = t.getSlots();
			SortedSet<Slot> uSet = u.getSlots();
			
			SortedSet<Slot> union = new TreeSet<>(tSet);
			union.addAll(uSet);
			
			SortedSet<Slot> intersection = new TreeSet<>(tSet);
			intersection.retainAll(uSet);
			
			int diff = union.size() - intersection.size();
			if (diff == 0)
			{
				return 0d;
			}
			return general + perSlot*diff;
		}
	}
}
