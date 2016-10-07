package nl.eur.ese.bouman.tas.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.csv.CSVRecord;

public class InARowParser {

	private final static String [] NUMBERS = { "zero", "one", "two", "three",
			"four", "five", "six", "seven", "eight", "nine", "ten" };
	
	private Map<String,Integer> map;
	
	public InARowParser(Map<String,Integer> headers)
	{
		map = new HashMap<>();
		int index = 0;
		for (String s : NUMBERS)
		{
			Optional<String> col = flexRow(headers, s);
			if (col.isPresent())
			{
				map.put(col.get(), index);
			}
			index++;
		}
	}
	
	public Map<Integer,Preference> parseInARow(CSVRecord rec)
	{
		Map<Integer,Preference> prefs = new HashMap<>();
		for (Entry<String,Integer> e : map.entrySet())
		{
			String datum = rec.get(e.getKey());
			Preference pref = Preference.flexParse(datum);
			prefs.put(e.getValue(), pref);
		}
		return prefs;
	}
	
	public static Optional<String> flexRow(Map<String,Integer> headers, String num)
	{
		List<String> candidates = new ArrayList<>();
		for (String s : headers.keySet())
		{
			String lc = s.toLowerCase();
			if (lc.contains("in a row") && lc.contains(num))
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

}
