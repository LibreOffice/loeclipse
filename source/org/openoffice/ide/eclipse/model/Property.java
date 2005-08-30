package org.openoffice.ide.eclipse.model;

import java.util.Vector;

public class Property extends TypedDeclaration {

	public final static String F_BOUND = "bound";
	
	public final static String F_CONSTRAINED = "constrained";
	
	public final static String F_MAYBEAMBIGUOUS = "maybeambiguous";
	
	public final static String F_MAYBEDEFAULT = "maybedefault";
	
	public final static String F_MAYBEVOID = "maybevoid";
	
	public final static String F_OPTIONAL = "optional";
	
	public final static String F_READONLY = "readonly";
	
	public final static String F_REMOVABLE = "removable";
	
	public final static String F_TRANSIENT = "transient";
	
	public final static String[] FLAGS = {
		F_BOUND,
		F_CONSTRAINED,
		F_MAYBEAMBIGUOUS,
		F_MAYBEDEFAULT,
		F_MAYBEVOID,
		F_OPTIONAL,
		F_READONLY,
		F_REMOVABLE,
		F_TRANSIENT
	};
	
	public Property(TreeNode node, String aName, UnoidlFile file, 
													String aDeclarationType) {
		super(node, aName, file, T_PROPERTY, aDeclarationType, false, false);
	}
	
	//----------------------------------------------------- TreeNode overriding
	
	public String computeBeforeString(TreeNode callingNode) {
		String output = "[property";
		
		for (int i=0, length=flags.size(); i<length; i++){
			String flagi = (String)flags.get(i);
			output = output + ", " + flagi;
		}
		
		String typeString = getDeclarationType().toString();
		if (getDeclarationType() instanceof ScopedName){
			typeString = ((ScopedName)getDeclarationType()).toString();
		}
		
		output = output + "] " + typeString + " " + getName() + ";";
		
		return indentLine(output);
	}

	//-------------------------------------------------------- Member managment
	
	Vector flags = new Vector();
	
	public void addFlag(String aFlag){
		if (!flags.contains(aFlag) && isFlag(aFlag)) {
			flags.add(aFlag);
		}
	}
	
	public void removeFlag(String aFlag){
		flags.remove(aFlag);
	}
	
	public Vector getFlags(){
		return flags;
	}
	
	public static boolean isFlag(String aFlag){
		boolean result = false;
		
		int i = 0;
		
		while (i < FLAGS.length && !result){
			if (aFlag.equals(FLAGS[i])) {
				result = true;
			} else {
				i++;
			}
		}
		return result;
	}
}
