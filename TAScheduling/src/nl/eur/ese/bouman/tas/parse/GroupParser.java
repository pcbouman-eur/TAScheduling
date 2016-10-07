package nl.eur.ese.bouman.tas.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.csv.CSVRecord;

public class GroupParser
{
	private Map<String,String> groups;
	private String prefer, notPrefer;
	
	public GroupParser(Map<String,Integer> headers)
	{
		groups = new HashMap<>();
		
		Optional<String> like = flexPat(headers, "like to teach");
		if (like.isPresent())
		{
			prefer = like.get();
		}
		
		Optional<String> disLike = flexPat(headers, "not to teach");
		if (disLike.isPresent())
		{
			notPrefer = disLike.get();
		}
	}
	
	public Map<String,Preference> parseGroups(CSVRecord rec)
	{
		Map<String,Preference> res = new HashMap<>();
		if (prefer != null)
		{
			String grps = rec.get(prefer);
			for (String group : getGroups(grps))
			{
				res.put(group, Preference.FAVORED);
			}
		}
		if (notPrefer != null)
		{
			String grps = rec.get(notPrefer);
			for (String group : getGroups(grps))
			{
				res.put(group, Preference.DISFAVORED);
			}
		}
		return res;
		
	}
	
	public String getGroup(String s)
	{
		String key = s.toLowerCase().trim();
		if (groups.containsKey(key))
		{
			return groups.get(key);
		}
		String value = s.trim();
		groups.put(key, value);
		return value;
	}
	
	public List<String> getGroups(String in)
	{
		List<String> res = new ArrayList<>();
		String [] grps = in.split(",");
		for (String str : grps)
		{
			String grp = getGroup(str);
			if (grp.length() > 0)
			{
				res.add(grp);
			}
		}
		return res;
	}
	
	public static Optional<String> flexPat(Map<String,Integer> headers, String pat)
	{
		List<String> candidates = new ArrayList<>();
		for (String s : headers.keySet())
		{
			String lc = s.toLowerCase();
			if (lc.contains(pat))
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
