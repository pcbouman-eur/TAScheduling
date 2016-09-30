package nl.eur.ese.bouman.tas.data;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface CostInformation
{
	public double getSameGroupCost(Group group, int repetitions);
	public double getShadowCosts(Session s);
	
	public boolean isRelevant(Assistant a, double cost);
	
	
	public static CostInformation getDefault()
	{
		return build( (g,i) -> i >= 2 ? -1d : 0d, s -> 0d, (a,d) -> true);
	}
	
	public static CostInformation build( BiFunction<Group,Integer,Double> sameGroup,
			                             Function<Session,Double> shadow,
			                             BiFunction<Assistant,Double,Boolean> relevant)
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
		};
	}
	
}
