/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial API and implementation
 *******************************************************************************/

package com.adapteva.cdt.epiphany.project.templateengine;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import org.eclipse.cdt.ui.templateengine.AbstractWizardDataPage;
import org.eclipse.cdt.ui.templateengine.IPagesAfterTemplateSelectionProvider;
import org.eclipse.cdt.ui.templateengine.IWizardDataPage;

/**
 * An example implementation of {@link IPagesAfterTemplateSelectionProvider} for
 * testing purposes.
 */
public class EpiphanyExtraPagesProvider implements IPagesAfterTemplateSelectionProvider {
	IWizardDataPage[] pages;
	
	final String PageCon = "The Wizard will create an Epiphany C project for each core, “common” code project and a “master” project.\nThe master and core projects can be built from the Project build menu.\nThe core projects are referenced by the master project. Note that each core project can be loaded and debugged independently.";
	
	public IWizardDataPage[] createAdditionalPages(IWorkbenchWizard wizard,
			IWorkbench workbench, IStructuredSelection selection) {
		pages= new IWizardDataPage[1];
		/*
		 * Change to: “”
		 */
		String pageDescription = new String (PageCon);
		pages[0]= new MyPage(pageDescription, "exampleAttr1", "Value1");
		return pages;
	}

	public IWizardDataPage[] getCreatedPages(IWorkbenchWizard wizard) {
		return pages;
	}

	/**
	 * An example implementation of {@link IWizardDataPage} for test purposes.
	 */
	static class MyPage extends AbstractWizardDataPage implements IWizardDataPage {
		String labelText , dataKey, dataValue;
		
		public MyPage(String labelText, String dataKey, String dataValue) {
			super("", "Epiphany multicore project additional information", null);
			setMessage("");
			this.labelText= labelText;
			this.dataKey= dataKey;
			this.dataValue= dataValue;
		}
		
		public Map getPageData() {
			return Collections.singletonMap(dataKey, dataValue);
		}

		public void createControl(Composite parent) {
			Label l= new Label(parent, SWT.NONE);
			l.setText(labelText);
			setControl(l);
		}
	}
}
