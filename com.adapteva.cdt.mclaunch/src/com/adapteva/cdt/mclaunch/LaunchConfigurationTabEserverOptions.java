package com.adapteva.cdt.mclaunch;


import java.io.File;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.CDebugUtils;

import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.debug.gdbjtag.ui.Activator;
import org.eclipse.cdt.debug.gdbjtag.ui.Messages;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;



public class LaunchConfigurationTabEserverOptions extends AbstractLaunchConfigurationTab {
	private static final String TAB_NAME = "E-server options";

	Button doMemoryTest;
	Button doShowMem;
	Button fSearchButton;


	Text gdbserverHostPortNum;
	private Label gdbserverHostPortNumLabel;

	private Label  xmlLabel;

	@Override
	public void createControl(Composite parent) {

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		setControl(sc);

		Composite comp = new Composite(sc, SWT.NONE);
		sc.setContent(comp);
		GridLayout layout = new GridLayout();
		comp.setLayout(layout);

		createInitGroup(comp);


		createXmlGroup(comp);


		sc.setMinSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));


	}



	public void createXmlGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);

		Composite comp = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		comp.setLayout(layout);

		fSearchButton = createPushButton(comp, "Select the platform xml file", null); 


		fSearchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleSearchButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});

		xmlLabel = new Label(comp, SWT.NONE);
		xmlLabel.setText(System.getenv("EPIPHANY_HOME") +  MCLaunchConstants.MC_ESERVER_LAUNCH_ATTR_XML_FILE_DEFAULT);

	}
	void handleSearchButtonSelected() {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		fd.setText("Open");
		fd.setFilterPath(System.getenv("EPIPHANY_HOME") + "/bsps/emek3");
		String[] filterExt = { "*.xml"};
		fd.setFilterExtensions(filterExt);

		if(fd.open() != null) {
			xmlLabel.setText(fd.open());
		}
	}

	public void createInitGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		//group.setText(Messages.getString("));

		Composite comp = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		comp.setLayout(layout);

		doMemoryTest = new Button(comp, SWT.CHECK);
		doMemoryTest.setText("Do memory test");
		doMemoryTest.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doResetChanged();
				updateLaunchConfigurationDialog();
			}
		});


		doShowMem = new Button(comp, SWT.CHECK);
		doShowMem.setText("Show supported memory");
		gd = new GridData();
		gd.horizontalSpan = 1;
		doShowMem.setLayoutData(gd);
		doShowMem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});


		gdbserverHostPortNumLabel = new Label(comp, SWT.NONE);
		gdbserverHostPortNumLabel.setText("The base port number where the e-server will accept connections");
		gdbserverHostPortNum = new Text(comp, SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 800;
		gdbserverHostPortNum.setLayoutData(gd);
		gdbserverHostPortNum.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				//FIXME
				//e.doit = (Character.isDigit(e.character) );
			}
		});
		gdbserverHostPortNum.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				scheduleUpdateJob();
			}
		});


	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(MCLaunchConstants.MC_ESERVER_LAUNCH_ATTR_DO_MEMTEST, true);

		configuration.setAttribute(MCLaunchConstants.MC_ESERVER_LAUNCH_ATTR_DO_SHOW_MEM, true);




		configuration.setAttribute(MCLaunchConstants.MC_ESERVER_LAUNCH_ATTR_XML_FILE, System.getenv("EPIPHANY_HOME") +  MCLaunchConstants.MC_ESERVER_LAUNCH_ATTR_XML_FILE_DEFAULT);

		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_PORT_NUMBER_ESERVER, MCLaunchConstants.MC_LAUNCH_DEFAULT_GDBSERVER_PORT_NUMBER);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			// Initialization Commands
			doMemoryTest.setSelection(configuration.getAttribute(MCLaunchConstants.MC_ESERVER_LAUNCH_ATTR_DO_MEMTEST, true));
			doShowMem.setSelection(configuration.getAttribute(MCLaunchConstants.MC_ESERVER_LAUNCH_ATTR_DO_SHOW_MEM, true));

			gdbserverHostPortNum.setText(configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_PORT_NUMBER_ESERVER , MCLaunchConstants.MC_LAUNCH_DEFAULT_GDBSERVER_PORT_NUMBER));			

		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		configuration.setAttribute(MCLaunchConstants.MC_ESERVER_LAUNCH_ATTR_DO_MEMTEST, doMemoryTest.getSelection());
		configuration.setAttribute(MCLaunchConstants.MC_ESERVER_LAUNCH_ATTR_DO_SHOW_MEM, doShowMem.getSelection());

		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_PORT_NUMBER_ESERVER, gdbserverHostPortNum.getText());

		configuration.setAttribute(MCLaunchConstants.MC_ESERVER_LAUNCH_ATTR_XML_FILE, xmlLabel.getText());

	}

	@Override
	public String getName() {

		return TAB_NAME;
	}



	private void doResetChanged() {

	}


}


