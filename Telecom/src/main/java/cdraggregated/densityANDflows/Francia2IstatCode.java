package cdraggregated.densityANDflows;

public class Francia2IstatCode implements ZoneConverter {

	@Override
	public String convert(String zone) {
		String c = zone.replaceAll("-", "");
		while(c.startsWith("0")) c = c.substring(1);
		return c;
	}

}
