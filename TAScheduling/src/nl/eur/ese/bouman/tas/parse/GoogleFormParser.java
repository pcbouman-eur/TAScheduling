package nl.eur.ese.bouman.tas.parse;

import java.io.File;
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

import nl.eur.ese.bouman.tas.data.Slot;

public class GoogleFormParser
{
	public static void main(String [] args) throws IOException
	{
		File f = new File("reactions-ict.csv");
		Charset cs = Charset.forName("utf8");
		
		System.out.println(readFormData(f, cs));
	}
	
	public static List<FormAssistant> readFormData(File f, Charset cs) throws IOException
	{
		return readFormData(f, cs, PreferenceMap.getDefault());
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
		
		Optional<String> nameOpt = flexName(headers);
		Optional<String> flexSess = flexSessions(headers);
		
		if (!nameOpt.isPresent() || !flexSess.isPresent())
		{
			throw new IllegalArgumentException("Header of file does not contain a name"+
						" column and/or a number of sessions column.");
		}
		
		String nameCol = nameOpt.get();
		String sessCol = flexSess.get();
		
		List<FormAssistant> result = new ArrayList<>();
		
		for (CSVRecord record : csv)
		{
			String name = record.get(nameCol);
			int sess = Integer.parseInt(record.get(sessCol));
			Map<Slot, Preference> slots = sp.parseSlots(record);
			Map<Integer, Preference> rows = iar.parseInARow(record);
			Map<String, Preference> groups = gp.parseGroups(record);
			
			FormAssistant fa = new FormAssistant(name,sess,slots,rows,groups);
			result.add(fa);
		}
		
		return result;
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
	
	
}
