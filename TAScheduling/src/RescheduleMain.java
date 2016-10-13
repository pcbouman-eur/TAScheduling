import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import ilog.concert.IloException;
import nl.eur.ese.bouman.tas.data.Instance;
import nl.eur.ese.bouman.tas.parse.GoogleFormParser;
import nl.eur.ese.bouman.tas.solution.AssistantSchedule;
import nl.eur.ese.bouman.tas.solution.Solution;
import nl.eur.ese.bouman.tas.solver.CplexReschedule;
import nl.eur.ese.bouman.tas.solver.CplexReschedule.Distance;
import nl.eur.ese.bouman.tas.solver.TASolver;

public class RescheduleMain
{
	public static void main(String [] args)
	{
		System.out.println("Rescheduler...");
		File csvFile = new File("reactions-ict.csv");
		Charset cs = Charset.forName("utf8");
		
		File sessFile = new File("sessions.txt");
		
		try
		{
			Instance i = GoogleFormParser.readInstance(csvFile, cs, sessFile);
			Solution s = Solution.readSolution(new File("output.csv"), i, ';', true);
			
			List<AssistantSchedule> schedules = TASolver.findAllSchedules(i, true);
			
			Distance<AssistantSchedule> dist = new CplexReschedule.TimeSlotDistance(1,9,3);
			
			CplexReschedule solver = new CplexReschedule(i, s, schedules, dist, true);
			solver.solve();
			Solution newSol = solver.getSolution();
			newSol.writeHTML(new File("new-output.html"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (IloException e)
		{
			e.printStackTrace();
		}

	}
}
