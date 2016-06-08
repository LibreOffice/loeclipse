package org.libreoffice.ide.eclipse.core.editors.utils;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.libreoffice.ide.eclipse.core.model.utils.IModel;
import org.libreoffice.ide.eclipse.core.model.utils.IModelChangedListener;

/**
 * Abstract section class providing mechanisms to suspend the dirty state change notifications.
 *
 * @param <ModelType>
 *            the type of the model object for the section
 *
 * @author Cédric Bosdonnat
 *
 */
public abstract class AbstractSection<ModelType extends IModel> extends SectionPart implements IModelChangedListener {

    private ModelType mModel;

    /**
     * The SectionPart constructor.
     *
     * @param pParent
     *            the parent composite
     * @param pPage
     *            the form page to use
     * @param pStyle
     *            the SectionPart style
     */
    public AbstractSection(Composite pParent, FormPage pPage, int pStyle) {
        super(pParent, pPage.getManagedForm().getToolkit(), pStyle);
        initialize(pPage.getManagedForm());
        getManagedForm().addPart(this);
        getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    /**
     * @return the data model for the section
     */
    public ModelType getModel() {
        return mModel;
    }

    /**
     * @param pModel
     *            the new data model for the section
     */
    public void setModel(ModelType pModel) {
        mModel = pModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirty() {
        return mModel.isDirty();
    }

    /**
     * Marks the editor as saved.
     */
    @Override
    public void modelSaved() {
        getManagedForm().dirtyStateChanged();
    }

    /**
     * Marks the editor as non dirty.
     */
    @Override
    public void modelChanged() {
        getManagedForm().dirtyStateChanged();
    }

    /**
     * Load the non-localized data from the model into the fields.
     */
    public abstract void loadData();
}
