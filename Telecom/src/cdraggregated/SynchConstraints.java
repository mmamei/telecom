package cdraggregated;

import java.util.HashSet;
import java.util.Set;

public class SynchConstraints {
	
	public static final boolean CLEAN = true;
	
	public String title;
	private Set<String> selected;
	private boolean invert;
	
	SynchConstraints(String title, Set<String> selected, boolean invert) {
		this.title = title + (CLEAN ? "-CLEAN" : "");
		this.selected = selected;
		this.invert = invert;
	} 
	
	public boolean isInverted() {
		return invert;
	}
	
	SynchConstraints(String title,Set<String> selected) {
		this(title,selected,false);
	}
	
	SynchConstraints(String title,boolean invert) {
		this.title = (invert? "not-" : "") + title + (CLEAN ? "-CLEAN" : "");
		this.selected = new HashSet<String>();
		selected.add(title);
		this.invert = invert;
	}
	SynchConstraints(String title){
		this(title,false);
	}
	
	
	public boolean ok(String meta) {
		
		String gmeta = meta.length() <2 ? meta: meta.substring(0, meta.length()-2)+"00"; // useful for caps (generalized meta)
		//System.out.println(meta+" --> "+gmeta);
		
		if(CLEAN && meta.equals("55032")) return false;
		if(CLEAN && meta.equals("0")) return false;
		
		if(!invert && (selected.contains(meta) || selected.contains(gmeta))) return true;
		if(invert &&  (!selected.contains(meta) && !selected.contains(gmeta))) return true;
		return false;
	}
	
}
