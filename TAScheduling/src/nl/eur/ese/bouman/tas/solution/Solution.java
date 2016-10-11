package nl.eur.ese.bouman.tas.solution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;

import nl.eur.ese.bouman.tas.data.Assistant;
import nl.eur.ese.bouman.tas.data.Instance;
import nl.eur.ese.bouman.tas.data.Session;
import nl.eur.ese.bouman.tas.data.Slot;
import nl.eur.ese.bouman.tas.parse.FormAssistant;
import nl.eur.ese.bouman.tas.parse.Preference;

public class Solution
{
	private Instance instance;
	private Map<Assistant,AssistantSchedule> schedules;
	
	public Solution(Instance i, List<AssistantSchedule> sol)
	{
		this.instance = i;
		this.schedules = new TreeMap<>();
		for (AssistantSchedule as : sol)
		{
			if (schedules.containsKey(as.getAssistant()))
			{
				throw new IllegalArgumentException("Cannot store two "+
						"schedules for the same assistant!");
			}
			schedules.put(as.getAssistant(), as);
		}
	}
	
	public void writeCSV(File f) throws FileNotFoundException
	{
		writeCSV(f,",");
	}
	
	public void writeCSV(File f, String sep) throws FileNotFoundException
	{

		try (PrintWriter pw = new PrintWriter(f))
		{
			pw.print("category"+sep+"slot"+sep+"group");
			for (Assistant a : instance.getAssistants())
			{
				pw.print(sep+a);
			}
			pw.println(sep+"Total");
			
			double total = 0;
			for (Session s : instance.getSessions())
			{
				pw.print(s.getCategory());
				pw.print(sep+s.getSlot());
				pw.print(sep+s.getGroup());
				
				int count = 0;
				for (Assistant a : instance.getAssistants())
				{
					AssistantSchedule as = schedules.get(a);
					if (as.getSessions().contains(s))
					{
						pw.print(sep+"X");
						count++;
					}
					else
					{
						pw.print(sep);
					}
				}
				if (count < 1)
				{
					total += instance.getCosts().getPenalty(s);
				}
				pw.println(sep+count);
			}
			
			pw.println();
			pw.print("Schedule quality"+sep+sep);
			for (Assistant a : instance.getAssistants())
			{
				AssistantSchedule as = schedules.get(a);
				double cost = as.evaluateCosts(instance.getCosts());
				pw.print(sep+cost);
				total += cost;
			}
			pw.println(sep+total);
		}
	}
	
	public void writeHTML(File f) throws FileNotFoundException
	{
		List<Assistant> assistants = new ArrayList<>(instance.getAssistants());
		Collections.sort(assistants);
		
		try (PrintWriter pw = new PrintWriter(f))
		{
			pw.println("<html\n<head>\n<title>Schedule</title>");
			pw.print("<style>");
			pw.println(" .slightlyFavored { background-color: #ccffcc; }");
			pw.println(" .favored { background-color: #75ff75; }");
			pw.println(" .neutral { background-color: #fffa70; }");
			pw.println(" .slightlyDisfavored { background-color: #ffba75; }");
			pw.println(" .disFavored { background-color: #ff9075; }");
			pw.println(" .unavailable { background-color: #000000; }");
			pw.println("</style>\n<body>");
			
			pw.println("<table>");
			pw.println("\t<tr>\n\t\t<th>Time slot</th>");
			for (Assistant a : assistants)
			{
				pw.println("\t\t<th>"+a.getName()+"</th>");
			}
			pw.println("\t</tr>");
			
			Map<Slot, SortedSet<Session>> map = instance.getSlotMap();
			
			for (Slot s : map.keySet())
			{
				pw.println("\t<tr>");
				pw.println("\t\t<td class=\"slot\">"+s.toString()+"</td>");
				for (Assistant a : assistants)
				{
					pw.print("\t\t<td class=\"taslot");
					if (a instanceof FormAssistant)
					{
						FormAssistant fa = (FormAssistant) a;
						Preference pref = fa.getPreference(s);
						if (pref == Preference.DISFAVORED)
						{
							pw.print(" disfavored");
						}
						else if (pref == Preference.SLIGHTLY_DISFAVORED)
						{
							pw.print(" slightlyDisfavored");
						}
						else if (pref == Preference.NEUTRAL)
						{
							pw.print(" neutral");
						}
						else if (pref == Preference.SLIGHTLY_FAVORED)
						{
							pw.print(" slightlyFavored");
						}
						else if (pref == Preference.FAVORED)
						{
							pw.print(" favored");
						}
						else if (pref == Preference.UNAVAILABLE)
						{
							pw.print(" unavailable");
						}
						
						pw.print("\">");
						Session ses = getSession(s, a);
						if (ses != null)
						{
							pw.print(ses.getGroup());
						}
						pw.println("</td>");
					}
				}
				pw.println("\t</tr>");
			}
			pw.println("</table>");
			pw.println("</body>");
			pw.println("</html>");
		}
	}
	
	public Session getSession(Slot timeslot, Assistant a)
	{
		AssistantSchedule as = schedules.get(a);
		for (Session sess : as.getSessions())
		{
			if (sess.getSlot().equals(timeslot))
			{
				return sess;
			}
		}
		return null;
	}
}
