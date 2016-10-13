package nl.eur.ese.bouman.tas.solver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import nl.eur.ese.bouman.tas.data.Assistant;
import nl.eur.ese.bouman.tas.data.CostInformation;
import nl.eur.ese.bouman.tas.data.Instance;
import nl.eur.ese.bouman.tas.data.Session;
import nl.eur.ese.bouman.tas.solution.AssistantSchedule;
import nl.eur.ese.bouman.tas.solution.Solution;

public class CplexSolver
{
	protected Instance instance;
	protected List<AssistantSchedule> schedules;
	private boolean binary;

	protected Map<Session,IloNumVar> dummyVars;
	protected Map<AssistantSchedule,IloNumVar> scheduleVars;
	protected IloCplex model;
	
	private Map<IloRange,Session> sessionConstr;
	private Map<IloRange,Assistant> assistantConstr;
	
	private boolean initialized;
	
	public CplexSolver(Instance i, List<AssistantSchedule> schedules, boolean binary) throws IloException
	{
		this.instance = i;
		this.schedules = new ArrayList<>(schedules);
		this.binary = binary;
		this.initialized = false;
	}
	
	protected void init() throws IloException
	{
		if (!initialized)
		{
			model = new IloCplex();
			
			initVars();
			initSessionConstraints();
			initAssistantConstraints();
			initObjective();
			initialized = true;
		}
	}
	
	protected void initVars() throws IloException
	{
		dummyVars = new LinkedHashMap<>();
		scheduleVars = new LinkedHashMap<>();
		
		for (Session s : instance.getSessions())
		{
			IloNumVar var;
			if (binary)
			{
				var = model.boolVar();
			}
			else
			{
				var = model.numVar(0, 1);
			}
			dummyVars.put(s, var);
		}
		
		for (AssistantSchedule as : schedules)
		{
			IloNumVar var;
			if (binary)
			{
				var = model.boolVar();
			}
			else
			{
				var = model.numVar(0, 1);
			}
			scheduleVars.put(as, var);
		}
	}
	
	protected void initSessionConstraints() throws IloException
	{
		sessionConstr = new LinkedHashMap<>();
		for (Session s : instance.getSessions())
		{
			IloNumExpr expr = dummyVars.get(s);
			for (Entry<AssistantSchedule,IloNumVar> e : scheduleVars.entrySet())
			{
				if (e.getKey().getSessions().contains(s))
				{
					expr = model.sum(expr, e.getValue());
				}
			}
			IloRange constr = model.addGe(expr, 1);
			sessionConstr.put(constr, s);
		}
	}
	
	protected void initAssistantConstraints() throws IloException
	{
		assistantConstr = new LinkedHashMap<>();
		for (Assistant a : instance.getAssistants())
		{
			IloNumExpr expr = model.constant(0);
			for (Entry<AssistantSchedule,IloNumVar> e : scheduleVars.entrySet())
			{
				if (e.getKey().getAssistant().equals(a))
				{
					expr = model.sum(expr, e.getValue());
				}
			}
			IloRange constr = model.addLe(expr, 1);
			assistantConstr.put(constr, a);
		}
	}
	
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
			double cost = e.getKey().evaluateCosts(ci);
			expr = model.sum(expr, model.prod(cost, e.getValue()));
		}
		model.addMaximize(expr);
	}
	
	public void solve() throws IloException
	{
		init();
		model.solve();
	}
	
	public void printSolution() throws IloException
	{
		System.out.println("Objective value : "+model.getObjValue());
		System.out.println("Selected Schedules: ");
		for (Entry<AssistantSchedule,IloNumVar> e : scheduleVars.entrySet())
		{
			double value = model.getValue(e.getValue());
			if (value > 10e-10)
			{
				System.out.println(value+" -> "+e.getKey());
			}
		}
		System.out.println("\nUncovered Sessions:");
		for (Entry<Session,IloNumVar> e : dummyVars.entrySet())
		{
			double value = model.getValue(e.getValue());
			if (value > 10e-10)
			{
				System.out.println(value+" -> "+e.getKey());
			}
		}
	}
	
	public Solution getSolution() throws IloException
	{
		List<AssistantSchedule> schedules = new ArrayList<>();
		for (Entry<AssistantSchedule,IloNumVar> e : scheduleVars.entrySet())
		{
			double value = model.getValue(e.getValue());
			if (value >= 1 - 10e-10)
			{
				schedules.add(e.getKey());
			}
		}
		return new Solution(instance, schedules);
	}
	
}
