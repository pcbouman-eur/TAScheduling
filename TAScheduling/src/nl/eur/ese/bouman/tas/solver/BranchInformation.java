package nl.eur.ese.bouman.tas.solver;

import java.util.function.BiFunction;

import nl.eur.ese.bouman.tas.data.Assistant;
import nl.eur.ese.bouman.tas.data.Session;

public interface BranchInformation
{
	public boolean isFixed(Assistant a, Session s);
	public boolean isForibidden(Assistant a, Session s);
	
	public static BranchInformation getDefault()
	{
		return build( (a,s) -> false, (a,s) -> false );
	}
	
	public static BranchInformation build(BiFunction<Assistant,Session,Boolean> fixed,
			                              BiFunction<Assistant,Session,Boolean> forbidden)
	{
		return new BranchInformation()
		{
			@Override
			public boolean isFixed(Assistant a, Session s) {
				return fixed.apply(a, s);
			}

			@Override
			public boolean isForibidden(Assistant a, Session s) {
				return forbidden.apply(a, s);
			}	
		};
	}
}
