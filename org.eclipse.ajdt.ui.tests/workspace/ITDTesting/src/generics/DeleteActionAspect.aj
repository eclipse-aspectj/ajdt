package generics;

import java.util.List;


public aspect DeleteActionAspect {

    public void DeleteAction<T extends Object>.delete() {
            Object selected = getSelected();
            selected.toString();
            delete3.add("");
    }
	 
	public int DeleteAction<T extends Object>.delete2;
	
	public List<String> DeleteAction.delete3;
	
} 