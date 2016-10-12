package nl.eur.ese.bouman.tas.data;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface CostInformation
{
	public double getSameGroupCost(Group group, int repetitions);
	public double getShadowCosts(Session s);
	public double getPenalty(Session s);
	
	public boolean isRelevant(Assistant a, double cost);
	
	public double getDistributionCost(Map<String,Integer> counts);
	
	
	public default CostInformation derive(Map<Session,Double> sesDuals,
			                              Map<Assistant,Double> aDuals)
	{
		CostInformation ci = this;
		return new CostInformation()
		{

			@Override
			public double getSameGroupCost(Group group, int repetitions)
			{
				return ci.getSameGroupCost(group, repetitions);
			}

			@Override
			public double getShadowCosts(Session s)
			{
				return -sesDuals.getOrDefault(s, 0d) + ci.getShadowCosts(s);
			}

			@Override
			public boolean isRelevant(Assistant a, double cost)
			{
				return cost > aDuals.getOrDefault(a, 0d)
					&& ci.isRelevant(a, cost);
			}
			
			@Override
			public double getPenalty(Session s)
			{
				return ci.getPenalty(s);
			}
			
			@Override
			public double getDistributionCost(Map<String, Integer> counts)
			{
				return ci.getDistributionCost(counts);
			}
			
		};
	}
	
	public static CostInformation getDefault()
	{
		return build( (g,i) -> i >= 2 ? 1d : 0d,
				          s -> 0d,
				      (a,d) -> true,
				          p -> -1000d,
				          0d);
	}
	
	public static CostInformation build( BiFunction<Group,Integer,Double> sameGroup,
			                             Function<Session,Double> shadow,
			                             BiFunction<Assistant,Double,Boolean> relevant,
			                             Function<Session,Double> penalty,
			                             double distributionPenalty)
	{
		return new CostInformation()
		{

			@Override
			public double getSameGroupCost(Group g, int r) {
				return sameGroup.apply(g, r);
			}

			@Override
			public double getShadowCosts(Session s) {
				return shadow.apply(s);
			}

			@Override
			public boolean isRelevant(Assistant a, double cost)
			{
				return relevant.apply(a, cost);
			}
			
			@Override
			public double getPenalty(Session s)
			{
				return penalty.apply(s);
			}
			
			@Override
			public double getDistributionCost(Map<String, Integer> counts)
			{
				Integer min = counts.values().stream().mapToInt(i -> i).min().getAsInt();
				Integer max = counts.values().stream().mapToInt(i -> i).max().getAsInt();
				
				return distributionPenalty * (max - min);
			}
		};
	}
	
	
	
}
