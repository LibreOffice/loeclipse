package org.openoffice.ide.eclipse.model;

/**
 * <p>This class is provided only to avoid duplicate code for all the elements
 * that are typed. This class is not usable as is. It have to be subclassed and
 * the following methods should be overridden:</p>
 * <ul>
 *   <li>computeBeforeString() to print the whole string</li>
 * </ul>
 * 
 * @author cbosdonnat
 *
 */
public class TypedDeclaration extends SingleFileDeclaration {
	
	public final static String[] BASIC_TYPES = {
		"void",
		"boolean",
		"byte",
		"short",
		"unsigned short",
		"long",
		"unsigned long",
		"hyper",
		"unsigned hyper",
		"float",
		"double",
		"char",
		"string",
		"type",
		"any"
	};

	public TypedDeclaration(TreeNode node, String aName, UnoidlFile file, 
			int aType, String aDeclarationType, boolean canBeVoid, 
			boolean canBeRest) {
		
		super(node, aName, file, aType);
		setDeclarationType(aDeclarationType);
		
		this.canBeVoid = canBeVoid;
		this.canBeRest = canBeRest;
	}
	
	//-------------------------------------------------- Declaration overriding
	
	public int[] getValidTypes() {
		return new int[]{};
	}
	
	public String computeAfterString(TreeNode callingNode) {
		return "";
	}
	
	//-------------------------------------------------------- Member managment

	private Object declarationType;
	
	private boolean canBeVoid = true;
	
	private boolean canBeRest = true;
	
	public Object getDeclarationType(){
		return declarationType;
	}
	
	public boolean isBasicDeclarationType(){
		return declarationType instanceof String;
	}
	
	public void setDeclarationType(String aDeclarationType){
		if (isBasicType(aDeclarationType)){
			declarationType = aDeclarationType;
		} else {
			declarationType = new ScopedName(aDeclarationType);
		}
	}
	
	private boolean isBasicType(String aType) {
		boolean result = false;
		
		if (aType.matches("any[\\t ]*\\.\\.\\.") && canBeRest) {
			result = true;
		} else {
			int i=0;
			while (i < BASIC_TYPES.length && !result){
				if (BASIC_TYPES[i].equals(aType) && canBeVoid){
					result = true;
				} else {
					i++;
				}
			}
		}
		
		return result;
	}
}
