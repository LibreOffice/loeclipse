package org.openoffice.ide.eclipse.core.wizards.utils;


public interface IListenablePage {

	public void addPageListener(IPageListener aListener);
	
	public void removePageListener(IPageListener aListener);
	
}
