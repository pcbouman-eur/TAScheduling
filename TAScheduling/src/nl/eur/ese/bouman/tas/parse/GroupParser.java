package nl.eur.ese.bouman.tas.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;

import nl.eur.ese.bouman.tas.data.Group;

public class GroupParser
{
	private Map<String,Group> groups;
	private Map<String,String> types;
	private String prefer, notPrefer;
	
	public GroupParser()
	{
		this(Collections.emptyMap());
	}
	
	public GroupParser(Map<String,Integer> headers)
	{
		groups = new HashMap<>();
		types = new HashMap<>();
		
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
			for (String group : getTypes(grps))
			{
				res.put(group, Preference.FAVORED);
			}
		}
		if (notPrefer != null)
		{
			String grps = rec.get(notPrefer);
			for (String group : getTypes(grps))
			{
				res.put(group, Preference.DISFAVORED);
			}
		}
		return res;
		
	}
	
	public Group getGroup(String s)
	{
		String key = s.toLowerCase().trim();
		if (groups.containsKey(key))
		{
			return groups.get(key);
		}
		String value = s.trim();
		Group g = new Group(value);
		groups.put(key, g);
		return g;
	}
	
	public List<Group> getGroups(String in)
	{
		List<Group> res = new ArrayList<>();
		String [] grps = in.split(",");
		for (String str : grps)
		{
			res.add(getGroup(str));
		}
		return res;
	}
	
	public List<Group> getGroups()
	{
		return groups.values()
				     .stream()
				     .collect(Collectors.toList());
	}
	
	public String getType(String s)
	{
		String key = s.toLowerCase().trim();
		if (types.containsKey(key))
		{
			return types.get(key);
		}
		String value = s.trim();
		types.put(key, value);
		return value;
	}
	
	public List<String> getTypes(String in)
	{
		List<String> types = new ArrayList<>();
		String [] split = in.split(",");
		for (String str : split)
		{
			if (str.trim().length() > 0)
			{
				String type = getType(str.trim());
				types.add(type);
			}
		}
		return types;
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
