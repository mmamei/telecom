package analysis;

import java.util.HashSet;
import java.util.Set;

public class SynchConstraints {
	
	public String title;
	private Set<String> selected;
	private boolean invert;
	
	SynchConstraints(String title, Set<String> selected, boolean invert) {
		this.title = title;
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
		this.title = (invert? "not-" : "") + title;
		this.selected = new HashSet<String>();
		selected.add(title);
		this.invert = invert;
	}
	SynchConstraints(String title){
		this(title,false);
	}
	
	public boolean ok(String meta) {
		if(!invert && selected.contains(meta)) return true;
		if(invert && !selected.contains(meta)) return true;
		return false;
	}
	
}
