package nl.eur.ese.bouman.tas.parse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVRecord;

import nl.eur.ese.bouman.tas.data.Slot;

public class SlotParser
{
	private Map<String,Slot> slots = new HashMap<>();
	
	private static final String time = "(\\d{1,2}:\\d\\d)";
	private static final Pattern pat = Pattern.compile("(\\w+)\\s+"+time+"\\s*-{1,2}\\s*"+time);
	
	
	
	public static void main(String [] args)
	{
		String msg = "Monday 11:00 - 13:00";
		Matcher mat = pat.matcher(msg);
		mat.matches();
		System.out.println(mat.group(3));
	}
	
	public Map<Slot,Preference> parseSlots(CSVRecord record)
	{
		Map<Slot,Preference> res = new HashMap<>();
		Map<String, String> map = record.toMap();
		for (String k : map.keySet())
		{
			Optional<Slot> op = getSlot(k);
			if (op.isPresent())
			{
				Slot s = op.get();
				String datum = record.get(k);
				Preference pref = Preference.flexParse(datum);
				if (pref != Preference.UNKNOWN)
				{
					res.put(s, pref);
				}
			}
		}
		return res;
	}
	
	public Optional<Slot> getSlot(String in)
	{
		if (slots.containsKey(in))
		{
			return Optional.of(slots.get(in));
		}
		
		Matcher match = pat.matcher(in);
		if (!match.matches())
		{
			return Optional.empty();
		}
		
		String day = match.group(1);
		match.matches();
		int fromHour = getHour(match.group(2));
		int endHour = getHour(match.group(3));
		Slot slot = new Slot(day,fromHour,endHour);
		slots.put(in, slot);
		return Optional.of(slot);
	}
	
	private static int getHour(String time)
	{
		String [] split = time.split(":");
		return Integer.parseInt(split[0]);
	}
}
