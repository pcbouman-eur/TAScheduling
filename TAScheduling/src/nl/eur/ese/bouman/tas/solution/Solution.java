package nl.eur.ese.bouman.tas.solution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import nl.eur.ese.bouman.tas.data.Assistant;
import nl.eur.ese.bouman.tas.data.Instance;
import nl.eur.ese.bouman.tas.data.Session;
import nl.eur.ese.bouman.tas.data.Slot;
import nl.eur.ese.bouman.tas.parse.FormAssistant;
import nl.eur.ese.bouman.tas.parse.Preference;

public class Solution
{
	private final static String SLOT_FIELD = "slot";
	private final static String CAT_FIELD = "category";
	private final static String GROUP_FIELD = "group";
	private final static String TOTAL_FIELD = "Total";
	private final static String QUALITY = "Schedule quality";
	private final static Locale LOCALE = Locale.US;
	private final static TextStyle STYLE = TextStyle.SHORT_STANDALONE;

	
	private Instance instance;
	private Map<Assistant,AssistantSchedule> schedules;
	private Set<Assistant> affected;
	
	public Solution(Instance i, List<AssistantSchedule> sol)
	{
		this.instance = i;
		this.schedules = new TreeMap<>();
		this.affected = new TreeSet<>();
		for (AssistantSchedule as : sol)
		{
			if (this.schedules.containsKey(as.getAssistant()))
			{
				throw new IllegalArgumentException("Cannot store two "+
						"schedules for the same assistant!");
			}
			this.schedules.put(as.getAssistant(), as);
		}
	}
	
	public void setAffected(Assistant a)
	{
		affected.add(a);
	}
	
	public void writeCSV(File f) throws FileNotFoundException
	{
		writeCSV(f,",");
	}
	
	public void writeCSV(File f, String sep) throws FileNotFoundException
	{

		try (PrintWriter pw = new PrintWriter(f))
		{
			pw.print(CAT_FIELD+sep+SLOT_FIELD+sep+GROUP_FIELD);
			for (Assistant a : instance.getAssistants())
			{
				pw.print(sep+a);
			}
			pw.println(sep+TOTAL_FIELD);
			
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
			pw.print(QUALITY+sep+sep);
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
			pw.println(" .slightlyDisfavored { background-color: #ffadad; }");
			pw.println(" .disFavored { background-color: #ff6b6b; }");
			pw.println(" .unavailable { background-color: #000000; }");
			pw.println(" .affected { font-style: italic; }");
			pw.println(" .lastDay td { padding-top: 5px; }");
			pw.println("</style>\n<body>");
			
			pw.println("<table>");
			pw.println("\t<tr>\n\t\t<th>Time slot</th>");
			for (Assistant a : assistants)
			{
				if (affected.contains(a))
				{
					pw.print("\t\t<th class=\"affected\">");
					pw.print(a.getName()+" *");
					pw.println("</th>");
				}
				else
				{
					pw.println("\t\t<th>"+a.getName()+"</th>");
				}
			}
			pw.println("\t</tr>");
			
			Map<Slot, SortedSet<Session>> map = instance.getSlotMap();
			
			Slot prevSlot = null;
			for (Slot s : map.keySet())
			{
				pw.print("\t<tr");
				if (prevSlot != null && !prevSlot.getDay().equals(s.getDay()))
				{
					pw.print(" class=\"lastDay\"");
				}
				pw.println(">");
				prevSlot = s;

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
	
	
	public static Solution readSolution(File f, Instance i, char sep, boolean strict) throws IOException
	{
		CSVFormat format = CSVFormat.EXCEL.withFirstRecordAsHeader().withDelimiter(sep);
		try (CSVParser csv = new CSVParser(new InputStreamReader(new FileInputStream(f)), format))
		{
			Map<Assistant,AssistantSchedule> schedules = new HashMap<>();
			
			Map<String,Assistant> assistantHeaders = new HashMap<>();
			
			for (String s : csv.getHeaderMap().keySet())
			{
				if (   !s.equals(SLOT_FIELD)  && !s.equals(CAT_FIELD)
					&& !s.equals(GROUP_FIELD) && !s.equals(TOTAL_FIELD))
				{
					Assistant a = i.getAssistant(s).orElse(null);
					if (a != null)
					{
						schedules.put(a, new AssistantSchedule(i, a));
						assistantHeaders.put(s, a);
					}
					else
					{
						if (strict)
						{
							throw new IllegalArgumentException("Could not find an assistant for column "+s);
						}
						else
						{
							System.out.println("Skipping assistant columm "+s);
						}
					}
				}
			}
			
			for (CSVRecord record : csv)
			{
				if (record.size() <= 3 || record.get(CAT_FIELD).equals(QUALITY))
				{
					// This is a record without relevant information 
					continue;
				}
				
				Slot slot = parseSlot(record.get(SLOT_FIELD), LOCALE, STYLE);
				String groupName = record.get(GROUP_FIELD);
				String cat = record.get(CAT_FIELD);
				Session ses = i.getSession(slot, groupName, cat).orElse(null);
				if (ses != null)
				{
					for (Entry<String,Assistant> e : assistantHeaders.entrySet())
					{
						String dat = record.get(e.getKey());
						if (dat != null && dat.trim().length() > 0 )
						{
							schedules.get(e.getValue()).addSession(ses);
						}
					}
				}
				else
				{
					if (strict)
					{
						throw new IllegalArgumentException("Could not find a session for "
									+groupName+" at "+slot+" for category "+cat);
					}
					else
					{
						System.out.println("Skipping session of "+groupName+" at "
								+ slot +" for category "+cat);
					}
				}
			}
			return new Solution(i, new ArrayList<>(schedules.values()));
		}
	}
	
	private static Slot parseSlot(String s, Locale l, TextStyle ts)
	{
		String [] split = s.split(" ");
		if (split.length < 2)
		{
			System.out.println(s);
		}
		DayOfWeek dow = parseDOW(split[0], l, ts);
		String [] split2 = split[1].split("-");
		int from = Integer.parseInt(split2[0]);
		int to = Integer.parseInt(split2[1]);
		return new Slot(dow.toString(), from, to);
	}
	
	private static DayOfWeek parseDOW(String dow, Locale l, TextStyle ts)
	{
		for (DayOfWeek d : DayOfWeek.values())
		{
			if (d.getDisplayName(ts, l).equalsIgnoreCase(dow))
			{
				return d;
			}
		}
		return null;
	}

	public AssistantSchedule getSchedule(Assistant assistant)
	{
		return schedules.get(assistant).copy();
	}
}
