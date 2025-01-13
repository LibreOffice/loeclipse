/*************************************************************************
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * Sun Microsystems Inc., October, 2000
 *
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2000 by Sun Microsystems, Inc.
 * 901 San Antonio Road, Palo Alto, CA 94303, USA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 *
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 *
 * Copyright: 2002 by Sun Microsystems, Inc.
 *
 * All Rights Reserved.
 *
 * Contributor(s): Cedric Bosdonnat
 *
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.wizards.pages;

import java.text.MessageFormat;
import java.util.Vector;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.gui.rows.BooleanRow;
import org.libreoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.libreoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.libreoffice.ide.eclipse.core.gui.rows.LabeledRow;
import org.libreoffice.ide.eclipse.core.gui.rows.TextRow;
import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.UnoFactoryData;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.unotypebrowser.UnoTypeProvider;
import org.libreoffice.ide.eclipse.core.wizards.Messages;
import org.libreoffice.ide.eclipse.core.wizards.utils.IListenablePage;
import org.libreoffice.ide.eclipse.core.wizards.utils.IPageListener;

/**
 * Abstract class for a wizard page to create a scoped element such as a service or an interface.
 */
public abstract class NewScopedElementWizardPage extends WizardPage implements IFieldChangedListener, IListenablePage {

    private static final String P_PACKAGE = "__package"; //$NON-NLS-1$
    private static final String P_NAME = "__name"; //$NON-NLS-1$
    private static final String P_PUBLISHED = "__published"; //$NON-NLS-1$

    private IUnoidlProject mUnoProject;
    private String mRootName;
    private String mSubpackageName;
    private String mElementName;

    private Vector<IPageListener> mListeners = new Vector<IPageListener>();

    private TextRow mPackageRow;
    private TextRow mNameRow;
    private BooleanRow mPublishedRow;

    /**
     * Default constructor to use when neither the project nor the OOo instance is known.
     *
     * @param name
     *            wizard page name
     */
    public NewScopedElementWizardPage(String name) {
        this(name, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Constructor to use when the UNO project is already created.
     *
     * @param pageName
     *            name of the page
     * @param unoProject
     *            UNO project in which to create a scoped type
     */
    public NewScopedElementWizardPage(String pageName, IUnoidlProject unoProject) {
        this(pageName, unoProject, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Constructor to use when the UNO project is already created, the scoped type name and it's path already known.
     *
     * @param pageName
     *            name of the wizard page
     * @param project
     *            UNO project in which to create a scoped type
     * @param rootName
     *            scoped name of the module containing the type
     * @param elementName
     *            name of the type, without any '.' or '::'
     */
    public NewScopedElementWizardPage(String pageName, IUnoidlProject project, String rootName,
        String elementName) {

        this(pageName, rootName, elementName);
        setUnoidlProject(project);
    }

    /**
     * Creates a default scoped name type wizard page with blank container path and type name.
     *
     * @param pageName
     *            name of the wizard page
     * @param instance
     *            the OOo instance to use to retrieve the types
     */
    public NewScopedElementWizardPage(String pageName, IOOo instance) {
        this(pageName, "", "", instance); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Constructor to use when the UNO project is already created, the scoped type name and it's path already known.
     *
     * @param pageName
     *            name of the wizard page
     * @param rootName
     *            scoped name of the module containing the type
     * @param elementName
     *            name of the type, without any '.' or '::'
     * @param instance
     *            the reference to the OOo to use for type selection
     */
    public NewScopedElementWizardPage(String pageName, String rootName, String elementName, IOOo instance) {

        this(pageName, rootName, elementName);
        setOOoInstance(instance);
    }

    /**
     * Creates a default page for a scoped element like an interface or a service. This constructor let provide default
     * values for the container path and the type name.
     *
     * @param pageName
     *            name of the wizard page
     * @param rootName
     *            scoped name of the module containing the type
     * @param elementName
     *            name of the type, without any '.' or '::'
     */
    private NewScopedElementWizardPage(String pageName, String rootName, String elementName) {

        super(pageName);

        setTitle(getTitle());
        setDescription(getDescription());
        setImageDescriptor(getImageDescriptor());

        mRootName = ""; //$NON-NLS-1$
        if (null != rootName) {
            mRootName = rootName;
        }
        mElementName = ""; //$NON-NLS-1$
        if (null != elementName) {
            mElementName = elementName;
        }
        mSubpackageName = ""; //$NON-NLS-1$
    }

    /**
     * @return the project which has been set to the page
     */
    public IUnoidlProject getProject() {
        return mUnoProject;
    }

    /**
     * @return the string corresponding to the type name, e.g. "interface".
     */
    protected abstract String getTypeLabel();

    /**
     * @return the image descriptor to put on the top-right of the page
     */
    protected abstract ImageDescriptor getImageDescriptor();

    /**
     * Implement this method to add specific controls for the subclassing wizard page.
     *
     * @param pParent
     *            the composite parent where to put the controls
     */
    protected abstract void createSpecificControl(Composite pParent);

    /**
     * @return the types to get in the UNO types provider. The returned integer is a
     *
     *         <pre>
     * bit or
     *         </pre>
     *
     *         of the types defined in the {@link UnoTypeProvider} class.
     */
    public abstract int getProvidedTypes();

    /**
     * Set the OOo instance to query the types from.
     *
     * @param instance
     *            OOo instance to use.
     */
    public void setOOoInstance(IOOo instance) {
        if (instance != null) {
            UnoTypeProvider provider = UnoTypeProvider.getInstance();
            provider.setOOoInstance(instance);
        }
    }

    /**
     * Sets the UNO project in which to create the scoped name type.
     *
     * @param pUnoProject
     *            the projet for which to create the UNO type.
     */
    public void setUnoidlProject(IUnoidlProject pUnoProject) {
        mUnoProject = pUnoProject;
        UnoTypeProvider provider = UnoTypeProvider.getInstance();
        provider.setProject(mUnoProject);
    }

    /**
     * @return the root module where to create the UNO type.
     */
    public String getPackageRoot() {
        String packageName = ""; //$NON-NLS-1$
        if (mUnoProject != null) {
            packageName = mUnoProject.getRootModule();
        }

        if (mRootName != null && !mRootName.equals("")) { //$NON-NLS-1$
            if (!packageName.equals("")) { //$NON-NLS-1$
                packageName += "::"; //$NON-NLS-1$
            }
            packageName = mRootName.replaceAll("\\.", "::"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return packageName;
    }

    /**
     * @return the module containing the UNO type, separated by "::".
     */
    public String getPackage() {
        String packageName = getPackageRoot();

        if (mPackageRow != null && !mPackageRow.getValue().equals("")) { //$NON-NLS-1$
            if (!packageName.equals("")) { //$NON-NLS-1$
                packageName += "::"; //$NON-NLS-1$
            }
            packageName += mPackageRow.getValue();
        }
        return packageName;
    }

    /**
     * @return the name of the element to create.
     */
    public String getElementName() {
        return mElementName;
    }

    /**
     * The container name of the type to create is composed of two parts: the package root and the package; this method
     * sets the first part.
     *
     * @param pValue
     *            the new package root to set
     */
    public void setPackageRoot(String pValue) {
        String packageLabel = Messages.getString("NewScopedElementWizardPage.Package") + pValue; //$NON-NLS-1$
        mRootName = pValue;

        if (mPackageRow != null) {
            mPackageRow.setLabel(packageLabel);
        }
    }

    /**
     * the container name of the type to create is composed of two parts: the package root and the package. This method
     * sets the second part.
     *
     * @param value
     *            the new package value
     * @param forced
     *            <code>true</code> will replace the current value, <code>false</code> will set the value only if the
     *            current package is empty or <code>null</code>.
     */
    public void setPackage(String value, boolean forced) {
        String moduleSep = "::"; //$NON-NLS-1$

        if (value.startsWith(moduleSep)) {
            value = value.substring(moduleSep.length());
        }

        if (mPackageRow != null) {
            mPackageRow.setValue(value);
            mPackageRow.setEnabled(!forced);
        } else {
            mSubpackageName = value;
        }
    }

    /**
     * Sets the name of the element to create.
     *
     * @param value
     *            the new package value
     * @param forced
     *            <code>true</code> will replace the current value, <code>false</code> will set the value only if the
     *            current package is empty or <code>null</code>.
     */
    public void setName(String value, boolean forced) {

        mElementName = value;
        if (mNameRow != null) {

            value = value.replace(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$

            mNameRow.setValue(value);
            mNameRow.setEnabled(!forced);
        }
        setPageComplete(isPageComplete());
    }

    /**
     * @return whether the service is published or not.
     */
    public boolean isPublished() {
        boolean isPublished = false;
        if (mPublishedRow != null) {
            isPublished = mPublishedRow.getBooleanValue();
        }
        return isPublished;
    }

    /**
     * Sets whether the type is published or not.
     *
     * @param value
     *            <code>true</code> if the type is published, <code>false</code> otherwise
     * @param forced
     *            <code>true</code> to overwrite the existing value.
     */
    public void setPublished(boolean value, boolean forced) {

        mPublishedRow.setValue(value);
        mPublishedRow.setEnabled(!forced);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        try {
            mPackageRow.removeFieldChangedlistener();
            mNameRow.removeFieldChangedlistener();
            mPublishedRow.removeFieldChangedlistener();
        } catch (NullPointerException e) {
            PluginLogger.debug(e.getMessage());
        }

        super.dispose();
    }

    // ---------------------------------------------------------- IListenablePage

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPageListener(IPageListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePageListener(IPageListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    /**
     * Notifies all the page listeners that the pages data have changed.
     *
     * @param data
     *            the new data of the page.
     */
    protected void firePageChanged(UnoFactoryData data) {
        for (int i = 0, length = mListeners.size(); i < length; i++) {
            mListeners.get(i).pageChanged(data);
        }
    }

    // --------------------------------------------------- Page content managment

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControl(Composite pParent) {

        Composite body = new Composite(pParent, SWT.NONE);
        body.setLayout(new GridLayout(LabeledRow.LAYOUT_COLUMNS, false));
        body.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Creates the package row
        String packageLabel = Messages.getString("NewScopedElementWizardPage.Package"); //$NON-NLS-1$
        if (null != mUnoProject) {
            packageLabel = packageLabel + mUnoProject.getRootModule();
        } else if (mRootName != null) {
            packageLabel += mRootName;
        }

        mPackageRow = new TextRow(body, P_PACKAGE, packageLabel);
        mPackageRow.setFieldChangedListener(this);
        mPackageRow.setValue(mSubpackageName);
        mPackageRow.setTooltip(Messages.getString("NewScopedElementWizardPage.PackageTooltip")); //$NON-NLS-1$

        mNameRow = new TextRow(body, P_NAME, getTypeLabel());
        mNameRow.setFieldChangedListener(this);
        mNameRow.setValue(mElementName);
        mNameRow.setTooltip(Messages.getString("NewScopedElementWizardPage.TypeNameTooltip")); //$NON-NLS-1$

        createSpecificControl(body);

        Composite publishedParent = new Composite(body, SWT.NONE);
        publishedParent.setLayout(new GridLayout(2, false));
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = LabeledRow.LAYOUT_COLUMNS;
        publishedParent.setLayoutData(gd);

        mPublishedRow = new BooleanRow(publishedParent, P_PUBLISHED,
            Messages.getString("NewScopedElementWizardPage.Published")); //$NON-NLS-1$
        mPublishedRow.setFieldChangedListener(this);

        setPageComplete(isPageComplete());

        // force layout because of some bug in SWT for Windows
        body.layout();

        setControl(body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisible(boolean pVisible) {
        super.setVisible(pVisible);
    }

    /**
     * @param data
     *            the UNO data to complete
     *
     * @return the given data with the completed properties, <code>null</code> if the provided data is <code>null</code>
     */
    public UnoFactoryData fillData(UnoFactoryData data) {
        if (data != null) {
            data.setProperty(IUnoFactoryConstants.PACKAGE_NAME, getPackage());
            data.setProperty(IUnoFactoryConstants.TYPE_NAME, getElementName());
            data.setProperty(IUnoFactoryConstants.TYPE_PUBLISHED, Boolean.valueOf(isPublished()));
        }
        return data;
    }

    /**
     * Creates an empty factory data for the page UNO type.
     *
     * @return the empty UNO factory data
     */
    public abstract UnoFactoryData getEmptyTypeData();

    /**
     * @param data
     *            the data of the project for which to get the default type data.
     *
     * @return the default type data for the project
     */
    public static UnoFactoryData getTypeData(UnoFactoryData data) {
        UnoFactoryData typeData = new UnoFactoryData();

        if (data != null) {
            try {
                String name = (String) data.getProperty(IUnoFactoryConstants.PROJECT_NAME);
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                name = name.replace(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$

                String packageName = (String) data.getProperty(IUnoFactoryConstants.PROJECT_PREFIX);
                packageName = packageName.replace(".", "::"); //$NON-NLS-1$ //$NON-NLS-2$

                // put the properties in the data
                typeData.setProperty(IUnoFactoryConstants.TYPE_NAME, name);
                typeData.setProperty(IUnoFactoryConstants.PACKAGE_NAME, packageName);
                typeData.setProperty(IUnoFactoryConstants.TYPE_PUBLISHED, Boolean.FALSE);
            } catch (Exception e) {
                typeData = null;
            }
        }
        return typeData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fieldChanged(FieldEvent event) {

        UnoFactoryData typeDelta = null;

        try {
            if (event.getProperty().equals(P_PACKAGE)) {
                typeDelta = getEmptyTypeData();
                typeDelta.setProperty(IUnoFactoryConstants.PACKAGE_NAME, getPackage());

            } else if (event.getProperty().equals(P_NAME)) {
                mElementName = event.getValue();
                // Test if there is the scoped name already exists
                String[] containers = new String[] { getProject().getTypesPath().toOSString(),
                    getProject().getOOo().getName() };
                boolean exists = UnoTypeProvider.getInstance().contains(event.getValue(), containers);
                if (exists) {
                    setErrorMessage(Messages.getString("NewScopedElementWizardPage.NameExistsError")); //$NON-NLS-1$
                } else {
                    setErrorMessage(null);
                    typeDelta = getEmptyTypeData();
                    typeDelta.setProperty(IUnoFactoryConstants.TYPE_NAME, event.getValue());
                }
            }
        } catch (NullPointerException e) {
            // Nothing to do... this is sometimes normal
        }

        if (typeDelta != null) {
            UnoFactoryData delta = new UnoFactoryData();

            try {
                String projectName = getProject().getName();
                String prefix = getProject().getCompanyPrefix();
                IOOo ooo = getProject().getOOo();

                delta.setProperty(IUnoFactoryConstants.PROJECT_NAME, projectName);
                delta.setProperty(IUnoFactoryConstants.PROJECT_OOO, ooo);
                delta.setProperty(IUnoFactoryConstants.PROJECT_PREFIX, prefix);
            } catch (NullPointerException e) {
                // The project isn't defined: it might be normal
            }

            delta.addInnerData(typeDelta);

            firePageChanged(delta);

            setPageComplete(isPageComplete());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPageComplete() {
        String type = getPackage() + "::" + getElementName(); //$NON-NLS-1$
        boolean result = !existsIdlFile(type, getProject());

        try {
            // An IDL identifier corresponds to the following regexp:
            // [A-Za-z_][A-Za-z_0-9]*
            if (!mNameRow.getValue().matches("[A-Za-z_][A-Za-z_0-9]*")) { //$NON-NLS-1$
                result = false;
            }
        } catch (NullPointerException e) {
            result = false;
        }

        return result;
    }

    /**
     * Checks if an IDL file exists in the project for a given IDL type.
     *
     * <p>
     * Please note that this method behaves correctly only if the user is respecting the following design rules:
     * <ul>
     * <li>One IDL type per file</li>
     * <li>The IDL types have to be organized in directories representing the UNO modules</li>
     * </ul>
     * </p>
     *
     * @param idlFullName
     *            the full name of the IDL file check
     * @param pPrj
     *            the project where to look for the IDL file
     *
     * @return <code>true</code> if the an IDL file corresponds to the searched type, <code>false</code> otherwise.
     */
    public static boolean existsIdlFile(String idlFullName, IUnoidlProject pPrj) {

        boolean exists = false;

        if (pPrj != null) {
            try {
                IPath idlPath = pPrj.getIdlPath();
                String slashedName = idlFullName.replace("::", "/"); //$NON-NLS-1$ //$NON-NLS-2$
                idlPath = idlPath.append(slashedName + ".idl"); //$NON-NLS-1$

                idlPath = pPrj.getProjectPath().append(idlPath);

                exists = idlPath.toFile().exists();
            } catch (Exception e) {
                String pattern = Messages.getString("ServiceWizardSet.IsIdlTypeExistingWarning"); //$NON-NLS-1$
                String msg = MessageFormat.format(pattern, idlFullName);
                PluginLogger.warning(msg, e);
            }
        }

        return exists;
    }
}
