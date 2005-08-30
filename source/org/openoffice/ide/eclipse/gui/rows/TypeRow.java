package org.openoffice.ide.eclipse.gui.rows;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.unotypebrowser.UnoTypeBrowser;
import org.openoffice.ide.eclipse.unotypebrowser.UnoTypeProvider;

public class TypeRow extends TextRow {

	private UnoTypeProvider typesProvider;
	private int type = 0;
	
	public TypeRow(Composite parent, String property, String label, 
			   UnoTypeProvider aTypeProvider, int aType) {
		super(parent, property, label);
		
		if (aType >=0 && aType < 512) {
			type = aType;
		}
		typesProvider = aTypeProvider;
	}
	
	protected void createContent(Composite parent, Control label, 
			Control field, String browseText) {

		super.createContent(parent, label, field, 
				OOEclipsePlugin.getTranslationString(I18nConstants.BROWSE));
		
		final Shell shell = parent.getShell();
		
		((Button)browse).addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				
				int oldType = typesProvider.getTypes();
				typesProvider.setTypes(type);
				
				UnoTypeBrowser browser = new UnoTypeBrowser(
						shell, typesProvider);
				
				if (UnoTypeBrowser.OK == browser.open()) {
					String selectedType = browser.getSelectedType();
					if (null != selectedType){
						String[] splittedType = selectedType.split(" ");
						
						if (2 == splittedType.length) {
							setText(splittedType[0]);
						}
					}
				}
				
				typesProvider.setTypes(oldType);
			}
		});
	}
}
