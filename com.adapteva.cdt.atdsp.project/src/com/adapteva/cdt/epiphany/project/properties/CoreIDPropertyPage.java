package com.adapteva.cdt.epiphany.project.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class CoreIDPropertyPage extends PropertyPage {

//	private static final String PATH_TITLE = "Path:";
	private static final String COREID_TITLE = "&CoreID:";
	
	private static final String COREID_PROPERTY = "COREID";
	private static final String DEFAULT_CORE_ID = "0";

	private static final int TEXT_FIELD_WIDTH = 50;

	private Text rowText;

	/**
	 * Constructor for CoreIDPropertyPage.
	 */
	public CoreIDPropertyPage() {
		super();
	}

	private void addFirstSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// Path text field
		Text pathValueText = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		pathValueText.setText(((IResource) getElement()).getFullPath().toString());
	}

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	private void addSecondSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// Label for coreid field
		Label rowLabel = new Label(composite, SWT.NONE);
		rowLabel.setText(COREID_TITLE);

		// Coreid text field
		rowText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
		rowText.setLayoutData(gd);
		rowText.setEnabled(false);

		// Populate owner text field
		try {
			String coreId =
				((IResource) getElement()).getPersistentProperty(
					new QualifiedName("", COREID_PROPERTY));
			rowText.setText((coreId != null) ? coreId : DEFAULT_CORE_ID);
		} catch (CoreException e) {
			rowText.setText(DEFAULT_CORE_ID);
		}
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addFirstSection(composite);
		addSeparator(composite);
		addSecondSection(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	protected void performDefaults() {
		super.performDefaults();
		// Populate the owner text field with the default value
		IProject project =  (IProject)getElement();
		try {
			String coreIDVal = project.getPersistentProperty(new QualifiedName("", COREID_PROPERTY));
			rowText.setText(coreIDVal);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public boolean performOk() {

		
		// store the value in the owner text field
		try {
			((IResource) getElement()).setPersistentProperty(
				new QualifiedName("", COREID_PROPERTY),
				rowText.getText());
		} catch (CoreException e) {
			return false;
		}
		return true;
	}

}