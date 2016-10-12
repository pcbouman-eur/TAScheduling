package nl.eur.ese.bouman.tas.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import ilog.concert.IloException;
import nl.eur.ese.bouman.tas.data.Assistant;
import nl.eur.ese.bouman.tas.data.CostInformation;
import nl.eur.ese.bouman.tas.data.Group;
import nl.eur.ese.bouman.tas.data.Instance;
import nl.eur.ese.bouman.tas.data.Session;
import nl.eur.ese.bouman.tas.data.Slot;
import nl.eur.ese.bouman.tas.solution.AssistantSchedule;
import nl.eur.ese.bouman.tas.solution.Solution;
import nl.eur.ese.bouman.tas.solver.BranchInformation;
import nl.eur.ese.bouman.tas.solver.CplexSolver;
import nl.eur.ese.bouman.tas.solver.LPModel;
import nl.eur.ese.bouman.tas.solver.TASolver;

public class GoogleFormParser
{
	public static void main(String [] args) throws IOException
	{
		File csvFile = new File("reactions-ict.csv");
		Charset cs = Charset.forName("utf8");
		
		File sessFile = new File("sessions.txt");
		
		Instance i = readInstance(csvFile, cs, sessFile);
		System.out.println(i);
		
		CostInformation ci = CostInformation.getDefault();
		BranchInformation bi = BranchInformation.getDefault();
		
		List<AssistantSchedule> schedules = new ArrayList<AssistantSchedule>();
		
		
		for (Assistant a : i.getAssistants())
		{
			TASolver tas = new TASolver(i,a,bi);
			tas.run();
			List<AssistantSchedule> taSchedules = tas.getSchedules();
			System.out.println(taSchedules.size()+" schedules for assistant "+a);
			schedules.addAll(taSchedules);
		}

		System.out.println("Total schedules : "+schedules.size());
		
		try
		{
			CplexSolver solver = new CplexSolver(i, schedules, true);
			solver.solve();
			solver.printSolution();
			Solution sol = solver.getSolution();
			sol.writeCSV(new File("output.csv"),";");
			sol.writeHTML(new File("output.html"));
		}
		catch(IloException ie)
		{
			ie.printStackTrace();
		}
		
		//LPModel lmp = new LPModel(i, schedules, bi);
		
		//lmp.bnb(10e-9);
		
		/*
		System.out.println(lmp.solve());
		System.out.println(lmp.getObjectiveValue());
		for (Entry<AssistantSchedule,Double> e : lmp.getValues().entrySet())
		{
			if (e.getValue() > 0)
			{
				System.out.println(e.getValue()+" : "+e.getKey());
			}
		}
		System.out.println();
		System.out.println("Uncovered");
		for (Entry<Session,Double> e : lmp.getUncovered().entrySet())
		{
			if (e.getValue() > 0)
			{
				System.out.println(e.getValue()+" : "+e.getKey());
			}
		}
		*/
	}
	
	public static Instance readInstance(File csvFile, Charset csvCs, File sessFile)
						throws IOException
	{
		return readInstance(csvFile, csvCs, sessFile, PreferenceMap.getDefault());
	}
	
	public static Instance readInstance(File csvFile, Charset csvCs, File sessFile,
			PreferenceMap pm) throws IOException
	{
		CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader()
                .withSkipHeaderRecord();

		CSVParser csv = CSVParser.parse(csvFile, csvCs, format);
		Map<String, Integer> headers = csv.getHeaderMap();
		
		SlotParser sp = new SlotParser();
		InARowParser iar = new InARowParser(headers);
		GroupParser gp = new GroupParser(headers);
		
		List<FormAssistant> assistants = readAssistants(csv, sp, iar, gp, pm);
		List<Session> sessions = readSessions(sessFile, sp, gp);
		Instance i = new Instance();
		for (FormAssistant fa : assistants)
		{
			i.addAssistant(fa);
		}
		for (Session s : sessions)
		{
			i.addSession(s);
		}
		for (Group g : gp.getGroups())
		{
			i.addGroup(g);
		}
		return i;
	}
	
	
	
	public static List<FormAssistant> readFormData(File f, Charset cs) throws IOException
	{
		return readFormData(f, cs, PreferenceMap.getDefault(1d,10d,10d));
	}
	
	public static List<FormAssistant> readFormData(File f, Charset cs, PreferenceMap pm) throws IOException
	{
		CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader()
                .withSkipHeaderRecord();

		CSVParser csv = CSVParser.parse(f, cs, format);
		Map<String, Integer> headers = csv.getHeaderMap();
		
		SlotParser sp = new SlotParser();
		InARowParser iar = new InARowParser(headers);
		GroupParser gp = new GroupParser(headers);
		
		return readAssistants(csv, sp, iar, gp, pm);
	}
	
	public static List<FormAssistant> readAssistants(CSVParser csv, SlotParser sp,
			InARowParser iar, GroupParser gp, PreferenceMap pm) throws IOException
	{
		Map<String, Integer> headers = csv.getHeaderMap();
		
		Optional<String> nameOpt = flexName(headers);
		Optional<String> sessOpt = flexSessions(headers);
		Optional<String> verOpt = flexVersion(headers);
		
		if (!nameOpt.isPresent() || !sessOpt.isPresent() || !verOpt.isPresent())
		{
			throw new IllegalArgumentException("Header of file does not contain a name"+
						" column, a number of sessions column and/or a version column.");
		}
		
		String nameCol = nameOpt.get();
		String sessCol = sessOpt.get();
		String verCol = verOpt.get();
		
		List<FormAssistant> result = new ArrayList<>();
		
		for (CSVRecord record : csv)
		{
			String name = record.get(nameCol);
			int sess = Integer.parseInt(record.get(sessCol));
			boolean ver = isDutch(record.get(verCol));
			
			Map<Slot, Preference> slots = sp.parseSlots(record);
			Map<Integer, Preference> rows = iar.parseInARow(record);
			Map<String, Preference> groups = gp.parseGroups(record);
			
			
			if (ver)
			{
				groups.put("IB", Preference.DISFAVORED);
				//groups.put("IB", Preference.UNAVAILABLE);
			}
			else
			{
				//groups.put("EC", Preference.DISFAVORED);
				//groups.put("FI", Preference.DISFAVORED);
				//groups.put("MD", Preference.DISFAVORED);
				groups.put("EC", Preference.UNAVAILABLE);
				groups.put("FI", Preference.UNAVAILABLE);
				groups.put("MD", Preference.UNAVAILABLE);
			}
			
			
			//FormAssistant fa = new FormAssistant(name,sess,slots,rows,groups);
			FormAssistant fa = new FormAssistant(name,sess,(int)Math.ceil(sess/2.0),slots,rows,groups);
			result.add(fa);
		}
		
		return result;
	}
	
	public static boolean isDutch(String in)
	{
		return in.toLowerCase().contains("dutch");
	}
	
	public static Optional<String> flexVersion(Map<String,Integer> headers)
	{
		List<String> candidates = new ArrayList<>();
		for (String s : headers.keySet())
		{
			String lc = s.toLowerCase();
			if (lc.contains("which version"))
			{
				candidates.add(s);
			}
		}
		if (candidates.size() == 1)
		{
			return Optional.of(candidates.get(0));
		}
		return Optional.empty();
	}

	
	public static Optional<String> flexName(Map<String,Integer> headers)
	{
		List<String> candidates = new ArrayList<>();
		for (String s : headers.keySet())
		{
			String lc = s.toLowerCase();
			if (lc.contains("name"))
			{
				candidates.add(s);
			}
		}
		if (candidates.size() == 1)
		{
			return Optional.of(candidates.get(0));
		}
		return Optional.empty();
	}
	
	public static Optional<String> flexSessions(Map<String,Integer> headers)
	{
		List<String> candidates;
		candidates = headers.keySet()
				            .stream()
				            .filter( s -> s.toLowerCase()
				            		       .contains("sessions"))
				            .collect(Collectors.toList());

		if (candidates.size() > 1)
		{
			candidates = candidates.stream()
					               .filter(s -> s.toLowerCase()
					            		         .contains("many"))
					               .collect(Collectors.toList());
		}
		if (candidates.size() > 1)
		{
			candidates = candidates.stream()
					               .filter(s -> !s.toLowerCase()
					            		          .contains("something"))
					               .collect(Collectors.toList());
		}
		if (candidates.size() == 1)
		{
			return Optional.of(candidates.get(0));
		}
		return Optional.empty();
	}
	
	public static List<Session> readSessions(File f, SlotParser sp, GroupParser gp)
			throws FileNotFoundException, IOException
	{
		String currentCat = "";
		Slot slot = null;
		List<Session> sessions = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(f)))
		{
			String line;
			while ((line = br.readLine()) != null)
			{
				if (line.startsWith("*"))
				{
					currentCat = line.substring(1).trim();
				}
				else if (line.startsWith(":"))
				{
					slot = sp.getSlot(line.substring(1).trim()).orElse(null);
				}
				else
				{
					Group group = gp.getGroup(line.trim());
					Session s = new Session(group,slot,currentCat);
					sessions.add(s);
				}
			}
		}
		return sessions;
	}
}
