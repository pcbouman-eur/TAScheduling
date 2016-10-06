package nl.eur.ese.bouman.tas.parse;

public enum Availability
{
	UNAVAILABLE,
	DISFAVORED,
	SLIGHTLY_DISFAVORED,
	NEUTRAL,
	SLIGHTLY_FAVORED,
	FAVORED,
	UNKNOWN;
	
	public static Availability flexParse(String in)
	{
		String lower = in.toLowerCase();
		if (lower.contains("unavailable"))
		{
			return UNAVAILABLE;
		}
		if (lower.contains("disfavor"))
		{
			if (lower.contains("slight"))
			{
				return SLIGHTLY_DISFAVORED;
			}
			return DISFAVORED;
		}
		if (lower.contains("neutral") || lower.contains("indifferent"))
		{
			return NEUTRAL;
		}
		if (lower.contains("prefer") || lower.contains("favor"))
		{
			if (lower.contains("slight"))
			{
				return SLIGHTLY_FAVORED;
			}
			return FAVORED;
		}
		return UNKNOWN;
	}
}
