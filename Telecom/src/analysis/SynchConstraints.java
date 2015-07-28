package analysis;

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
	
	public boolean ok(String meta) {
		if(!invert && selected.contains(meta)) return true;
		if(invert && !selected.contains(meta)) return true;
		return false;
	}
	
}
