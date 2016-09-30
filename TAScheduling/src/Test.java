import com.quantego.clp.CLP;
import com.quantego.clp.CLP.STATUS;
import com.quantego.clp.CLPConstraint;
import com.quantego.clp.CLPExpression;
import com.quantego.clp.CLPVariable;

public class Test
{
	public static void main(String [] args)
	{
		CLP clp = new CLP().minimization();
		CLPVariable x = clp.addVariable().lb(0).ub(3);
		CLPVariable y = clp.addVariable().lb(0);
		
		CLPExpression expr = clp.createExpression();
		CLPConstraint constr = expr.add(2, x).add(3,y).geq(10);
		
		expr = clp.createExpression();
		expr.add(3, x).add(2, y).asObjective();
		
		STATUS sol = clp.presolve(true).solve();
		System.out.println(sol);
		
		System.out.println("x : "+clp.getSolution(x));
		System.out.println("y : "+clp.getSolution(y));

		System.out.println("cons : "+clp.getDualSolution(constr));
		
		y.free().lb(0).ub(2);
		sol = clp.solve();
		System.out.println(sol);
		
		System.out.println("x : "+clp.getSolution(x));
		System.out.println("y : "+clp.getSolution(y));

		System.out.println("dual : "+clp.getDualSolution(constr));
		
		expr = clp.createExpression();
		CLPConstraint c2 = expr.add(3, x).geq(2);
		System.out.println(c2);
		clp.solve();
		clp.setConstraintCoefficient(c2, y, 0d);
		System.out.println(c2);
		clp.solve();
	}
}
