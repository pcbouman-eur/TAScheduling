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

public class GoogleFormParser
{
	public static void main(String [] args) throws IOException
	{
		File f = new File("reactions-ict.csv");
		Charset cs = Charset.forName("utf8");
		CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader()
				                            .withSkipHeaderRecord();
		
		CSVParser res = CSVParser.parse(f, cs, format);
		Map<String, Integer> headers = res.getHeaderMap();
		for (CSVRecord record : res)
		{
			for (String col : headers.keySet())
			{
				String d = record.get(col);
				System.out.println(""+col+" -> "+d+" ; "+Availability.flexParse(d));
			}
		}
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
