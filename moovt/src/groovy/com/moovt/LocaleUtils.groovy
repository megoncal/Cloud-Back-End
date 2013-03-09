package com.moovt

class LocaleUtils {

	public LocaleUtils() {
		// TODO Auto-generated constructor stub
	}
	
	public static String localeToString(Locale l)
	{
		return l.getLanguage() + "_" + l.getCountry();
	}
	
	public static Locale stringToLocale(String s)
	{
		//System.out.println("String To Locale " + s);
		StringTokenizer tempStringTokenizer = new StringTokenizer(s,"_");
		String l = "en";
		String c = "US";
		if(tempStringTokenizer.hasMoreTokens())
			l = tempStringTokenizer.nextElement();
		if(tempStringTokenizer.hasMoreTokens())
			c = tempStringTokenizer.nextElement();
		return new Locale(l,c);
	}

}
