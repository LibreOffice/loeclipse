package org.openoffice.ide.eclipse.gui.rows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class BooleanRow extends LabeledRow {
	
	private boolean value;
	
	public BooleanRow(Composite parent, String property, String label) {
		super(property);
		
		Button checkbox = new Button(parent, SWT.CHECK);
		checkbox.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				toggleValue();
			};
		});
		checkbox.setText(label);
		
		Label text = new Label(parent, SWT.NONE);
		
		createContent(parent, checkbox,text, null);
	}

	public void setLabel(String newLabel){
		((Label)field).setText(newLabel);
	}
	
	public void setValue(boolean aValue){
		if (value != aValue){
			((Button)label).setSelection(aValue);
			toggleValue();
		}
	}
	
	public void toggleValue(){
		value = !value;
		fireFieldChangedEvent(new FieldEvent(property, getValue()));
	}
	
	public boolean getBooleanValue(){
		return value;
	}
	
	public String getValue() {
		return Boolean.toString(value);
	}

}
