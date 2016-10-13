import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import nl.eur.ese.bouman.tas.data.Instance;
import nl.eur.ese.bouman.tas.parse.GoogleFormParser;
import nl.eur.ese.bouman.tas.solution.Solution;

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
			Solution s = Solution.readSolution(new File("output.csv"), i, ';');
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}
}
