package org.libreoffice.ide.eclipse.core.editors.utils;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.libreoffice.ide.eclipse.core.model.utils.IModel;
import org.libreoffice.ide.eclipse.core.model.utils.IModelDataListener;

/**
 * Abstract section class providing mechanisms to suspend the dirty state change notifications.
 *
 * @param <ModelType>
 *            the type of the model object for the section
 */
public abstract class AbstractSection<ModelType extends IModel> extends SectionPart implements IModelDataListener {

    private ModelType mModel;

    /**
     * The SectionPart constructor.
     *
     * @param parent
     *            the parent composite
     * @param page
     *            the form page to use
     * @param style
     *            the SectionPart style
     */
    public AbstractSection(Composite parent, FormPage page, int style) {
        super(parent, page.getManagedForm().getToolkit(), style);
        initialize(page.getManagedForm());
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
     * @param model
     *            the new data model for the section
     */
    public void setModel(ModelType model) {
        mModel = model;
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
