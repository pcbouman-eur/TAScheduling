package nl.eur.ese.bouman.tas.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.quantego.clp.CLP;
import com.quantego.clp.CLP.STATUS;
import com.quantego.clp.CLPConstraint;
import com.quantego.clp.CLPExpression;
import com.quantego.clp.CLPObjective;
import com.quantego.clp.CLPVariable;

import nl.eur.ese.bouman.tas.data.Instance;
import nl.eur.ese.bouman.tas.data.Assistant;
import nl.eur.ese.bouman.tas.data.CostInformation;
import nl.eur.ese.bouman.tas.data.Session;
import nl.eur.ese.bouman.tas.solution.AssistantSchedule;
;

public class LPModel
{
	private Instance instance;
	private CLP model;
	private List<AssistantSchedule> schedules;
	private BranchInformation bi;
	
	private Map<Session,CLPVariable> dummyMap;
	private Map<AssistantSchedule,CLPVariable> varMap;
	private Map<Session,CLPConstraint> sessionMap;
	private Map<Assistant,CLPConstraint> assistantMap;
	
	public LPModel(Instance i, List<AssistantSchedule> schedules, BranchInformation bi)
	{
		this.instance = i;
		this.schedules = new ArrayList<>(schedules);
		this.bi = bi;
		this.model = new CLP().maximization();
		
		initVars();
		initAssistantConstraints();
		initSessionConstraints();
		addObjective();
	}
	
	public STATUS solve()
	{
		return model.solve();
	}

	public double getObjectiveValue()
	{
		return model.getObjectiveValue();
	}
	
	public Map<AssistantSchedule,Double> getValues()
	{
		Map<AssistantSchedule,Double> result = new TreeMap<>();
		
		for (Entry<AssistantSchedule,CLPVariable> e : varMap.entrySet())
		{
			result.put(e.getKey(), model.getSolution(e.getValue()));
		}
		
		return result;
	}
	
	public Map<Session,Double> getUncovered()
	{
		Map<Session,Double> result = new TreeMap<>();
		
		for (Entry<Session,CLPVariable> e : dummyMap.entrySet())
		{
			result.put(e.getKey(), model.getSolution(e.getValue()));
		}
		
		return result;
	}
	
	public boolean isInteger(double eps)
	{
		for (Double d : getValues().values())
		{
			if (Math.abs(Math.round(d) - d) > eps)
			{
				return false;
			}
		}
		return true;
	}
	
	public Map<Session,Double> getSessionDuals()
	{
		Map<Session,Double> result = new TreeMap<>();
		for (Entry<Session,CLPConstraint> e : sessionMap.entrySet())
		{
			result.put(e.getKey(), model.getDualSolution(e.getValue()));
		}
		return result;
	}
	
	public Map<Assistant,Double> getAssistantDuals()
	{
		Map<Assistant,Double> result = new TreeMap<>();
		for (Entry<Assistant,CLPConstraint> e : assistantMap.entrySet())
		{
			result.put(e.getKey(), model.getDualSolution(e.getValue()));
		}
		return result;
	}
	
	private void initVars()
	{
		varMap = new TreeMap<>();
		dummyMap = new TreeMap<>();
		for (AssistantSchedule as : schedules)
		{
			if (bi.isPossible(as))
			{
				CLPVariable var = model.addVariable().lb(0);
				varMap.put(as, var);
			}
		}
		for (Session s : instance.getSessions())
		{
			CLPVariable var = model.addVariable().lb(0);
			dummyMap.put(s, var);
		}
	}
	
	private void initAssistantConstraints()
	{
		assistantMap = new TreeMap<>();
		Map<Assistant,CLPExpression> exprs = new TreeMap<>();
		for (Assistant a : instance.getAssistants())
		{
			exprs.put(a, model.createExpression());
		}
		
		for (AssistantSchedule as : schedules)
		{
			Assistant a = as.getAssistant();
			CLPExpression expr = exprs.get(a).add(varMap.get(as),1);
			exprs.put(a,expr);
		}
		
		for (Entry<Assistant, CLPExpression> e : exprs.entrySet())
		{
			CLPConstraint constr = e.getValue().leq(1);
			assistantMap.put(e.getKey(), constr);
		}
	}
	
	private void initSessionConstraints()
	{
		sessionMap = new TreeMap<>();
		Map<Session,CLPExpression> exprs = new TreeMap<>();
		for (Session s : instance.getSessions())
		{
			CLPExpression expr = model.createExpression();
			expr = expr.add(dummyMap.get(s));
			exprs.put(s, expr);
		}
		
		for (AssistantSchedule as : schedules)
		{
			for (Session s : as.getSessions())
			{
				CLPExpression expr = exprs.get(s).add(varMap.get(as),1);
				exprs.put(s,expr);
			}
		}
		
		for (Entry<Session, CLPExpression> e : exprs.entrySet())
		{
			CLPConstraint constr = e.getValue().geq(1);
			sessionMap.put(e.getKey(), constr);
		}
	}
	
	private void addObjective()
	{
		CostInformation ci = instance.getCosts();
		CLPExpression expr = model.createExpression();
		for (Entry<AssistantSchedule, CLPVariable> e : varMap.entrySet())
		{
			expr = expr.add(e.getValue(), e.getKey().evaluateCosts(ci));
		}
		for (Entry<Session,CLPVariable> e : dummyMap.entrySet())
		{
			expr = expr.add(e.getValue(), ci.getPenalty(e.getKey()));
		}
		expr.asObjective();
	} 
	
}
