package org.openoffice.ide.eclipse.core.wizards.utils;

import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchPage;
import org.openoffice.ide.eclipse.core.internal.model.UnoFactory;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;

public class WizardPageSet {
	
	private Vector<IWizardPage> mPages = new Vector<IWizardPage>();
	private Vector<Boolean> mHidden = new Vector<Boolean>();
	protected boolean mChangingPages = false;
	
	/**
	 * Listener to use to listen for any page change which impacts the wizard
	 * set contained pages.
	 */
	protected IPageListener mPageListener = new IPageListener() {
		
		/*
		 * (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.wizards.utils.IPageListener#pageChanged(org.openoffice.ide.eclipse.core.model.UnoFactoryData)
		 */
		public void pageChanged(UnoFactoryData data) {
			if (!mChangingPages) {
				dataChanged(data);
			}
		}
	};
	
	/**
	 * Reference to the wizard containing the pages. This reference has
	 * to be set before adding pages
	 */
	protected IWizard mWizard;
	
	public WizardPageSet(IWizard wizard) {
		if (wizard != null) {
			mWizard = wizard;
		}
	}
	
	/**
	 * Returns the page with the given name.
	 * 
	 * @param name the name of the page to look for
	 * @return the page found or <code>null</code> if none has been found.
	 */
	public IWizardPage getPage(String name) {
		IWizardPage page = null;
		int i = 0;
		while (page == null && i<mPages.size()) {
			if (mPages.get(i).getName().equals(name)) page = mPages.get(i);
			i++;
		}
		return page;
	}
	
	/**
	 * Add the pages into a list in order to store them. 
	 * 
	 * <p>The order in which they had been added will be the order in which they
	 * will be show in the wizard. By default all the added pages are visible.</p>
	 * 
	 * <p><strong>Note that the pages are not added into the wizard. This one has
	 * to add all the set pages using the {@link #getNextPage(IWizardPage)} 
	 * method.</strong></p>
	 * 
	 * @param page the page to add.
	 */
	public void addPage(IWizardPage page) {
		if (!mPages.contains(page)) {
			mPages.add(page);
			mHidden.add(Boolean.FALSE);
		}
	}
	
	/**
	 * Get an array of the contained pages.
	 * 
	 * <p>This method doesn't care whether the pages are visible or not.</p>
	 * 
	 * @return the contained pages
	 */
	public IWizardPage[] getPages() {
		return mPages.toArray(new IWizardPage[mPages.size()]);
	}
	
	/**
	 * Set a page as hidden in the wizard.
	 * 
	 * <p>This method defines whether a page of the wizard set should be shown
	 * or not. Be aware that the page are created even if they aren't shown.</p>
	 * 
	 * <p>This method has no effect if the page is not contained in the wizard
	 * set. Otherwise the page will simply not be taken into consideration in
	 * the wizard.</p>
	 * 
	 * @param page the page to hide
	 * @param hidden <code>true</code> to hide the page, 
	 * 			<code>false</code> otherwise
	 */
	public void setHidden(IWizardPage page, boolean hidden) {
		int id = mPages.indexOf(page);
		if (id != -1) {
			mHidden.set(id, Boolean.valueOf(hidden));
		}
	}
	
	/**
	 * Get the visible page to show after a page.
	 * 
	 * <p>The next page is determined by the order in which it has been added
	 * in the wizard set. The first non-hidden page added after the current page
	 * will be returned.</p>
	 * 
	 * <p><strong>The wizard should use this method to find the next page instead of
	 * the normal one.</strong></p>
	 * 
	 * <p>Here is a sample replacement of the normal method in the wizard:</p>
	 * <pre>
	 * public IWizardPage getNextPage(IWizardPage page) {
	 * 	IWizardPage next = null;
	 *	try {
	 *		next = wizardSet.getNextPage(page);
	 *	} catch (NoSuchPageException e) {
	 *		// Return the default next page if the page is not in the wizard set. 
	 *		next = super.getNextPage(page);
	 *	}
	 *	
	 *	return next;
	 * }</pre>
	 * 
	 * @param currentPage the page after which is the next page
	 * @return the next page or <code>null</code> if the current page is the last one.
	 * @throws NoSuchPageException is thrown if the page isn't contained in the 
	 * 		wizard set.
	 * 
	 * @see #setHidden(IWizardPage, boolean) for more informations on hidden 
	 * 			pages in the wizard set. 
	 */
	public IWizardPage getNextPage(IWizardPage currentPage) throws NoSuchPageException {
		IWizardPage nextPage = null;
		
		if (mPages.contains(currentPage)) {
			int currentId = mPages.indexOf(currentPage);
			int nextId = currentId + 1;
			boolean found = false;
			while (nextId < mPages.size() && !found) {
				if (mHidden.get(nextId).booleanValue()) {
					nextId ++;
				} else {
					found = true;
				}
			}
			
			if (found) nextPage = mPages.get(nextId);
		} else {
			throw new NoSuchPageException();
		}
		return nextPage;
	}
	
	/**
	 * Get the visible page to show before a page.
	 * 
	 * <p>The previous page is determined by the order in which it has been added
	 * in the wizard set. The last non-hidden page added before the current page
	 * will be returned.</p>
	 * 
	 * <p><strong>The wizard should use this method to find the previous page 
	 * instead of the normal one.</strong></p>
	 * 
	 * <p>Here is a sample replacement of the normal method in the wizard:</p>
	 * <pre>
	 * public IWizardPage getPreviousPage(IWizardPage page) {
	 *	IWizardPage previous = null;
	 *	try {
	 *		previous = wizardSet.getPreviousPage(page);
	 *	} catch (NoSuchPageException e) {
	 *		// Return the default previous page if the page is not in the
	 *		// wizard set. 
	 *		previous = super.getPreviousPage(page);
	 *	}
	 *	
	 *	return previous;
	 * }
	 * </pre>
	 * 
	 * @param currentPage the page before which is the previous page
	 * @return the previous page or <code>null</code> if the current page is the
	 * 		first one.
	 * @throws NoSuchPageException is thrown if the page isn't contained in the 
	 * 		wizard set.
	 * 
	 * @see #setHidden(IWizardPage, boolean) for more informations on hidden 
	 * 			pages in the wizard set. 
	 */
	public IWizardPage getPreviousPage(IWizardPage currentPage) throws NoSuchPageException {
		IWizardPage prevPage = null;
		
		if (mPages.contains(currentPage)) {
			int currentId = mPages.indexOf(currentPage);
			int prevId = currentId - 1;
			boolean found = false;
			while (-1 < prevId && !found) {
				if (mHidden.get(prevId).booleanValue()) {
					prevId --;
				} else {
					found = true;
				}
			}
			
			if (found) prevPage = mPages.get(prevId);
		} else {
			throw new NoSuchPageException();
		}
		return prevPage;
	}
	
	/**
	 * Initializes the wizard pages with default data.
	 * 
	 * <p>This method should set all the correct data in the pages supposing
	 * nothing has already been entered.</p>
	 * 
	 * <p><strong>This method should be overridden by the subclasses. The 
	 * default method doesn't perform any action.</strong></p>
	 * 
	 * @param data the data describing the default/initial values of the pages 
	 */
	public void initialize(UnoFactoryData data) {
	}
	
	/**
	 * Performs the actions to run at the end of the wizard for the wizard set.
	 * 
	 * <p>Performs the changes needed by the wizard set. This method has to be 
	 * called in the {@link Wizard#performFinish()} method. For cleaner actions
	 * the actions performed by the wizard set finish should concern only the
	 * data defined by the wizard set pages.</p>
	 * 
	 * <p>The active page is often needed by the {@link UnoFactory} to open a 
	 * newly created file in the workbench.</p>
	 * 
	 * <p><strong>This method should be overridden by the subclasses. The 
	 * default method doesn't perform any action.</strong></p>
	 * 
	 * @param monitor the monitor used to follow the finish process.
	 * @param activePage the page that was active before opening the wizard.
	 * 
	 * @see Wizard#performFinish() for more informations on actions performed
	 * 			when finishing a wizard.
	 * 
	 */
	public void doFinish(IProgressMonitor monitor, IWorkbenchPage activePage) {	
	}
	
	/**
	 * This method has to be called to ask the pages contained in the set to
	 * be updated with new external data.
	 * 
	 * <p><strong>This method should be overridden by the subclasses. The default method
	 * doesn't perform any action.</strong></p>
	 * 
	 * @param delta the data delta of the changed page.
	 */
	protected void dataChanged(UnoFactoryData delta) {
	}
}
