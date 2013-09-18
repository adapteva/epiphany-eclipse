package com.adapteva.cdt.mclaunch;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;

import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.debug.gdbjtag.ui.Activator;
import org.eclipse.cdt.debug.gdbjtag.ui.Messages;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.cdt.launch.ui.ICDTLaunchHelpContextIds;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/*
 * 		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText());
		if (fProgText != null) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, fProgText.getText());
 */
public class LaunchConfigurationTabProjectsSelection extends CMainTab {

	Button buttonAdd;
	Label label;

	ArrayList<String> AppliedProjList;



	private static final String TAB_NAME = "Projects";

	public LaunchConfigurationTabProjectsSelection() {
		super(CMainTab.DONT_CHECK_PROGRAM);
	}

	@SuppressWarnings("restriction")
	public void createControl(Composite parent) {
		AppliedProjList =new ArrayList<String>() ;



		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		LaunchUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(), ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		createVerticalSpacer(comp, 1);
		createExeFileGroup(comp, 1);
		createProjectGroup(comp, 1);
		createBuildOptionGroup(comp, 1);
		createVerticalSpacer(comp, 1);
		
		label = new Label(comp, SWT.CENTER);
//		label.setText("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		label.setBounds(comp.getClientArea());
		//addButtons(comp);

		if (wantsTerminalOption() /* && ProcessFactory.supportesTerminal() */) {
			createTerminalOption(comp, 1);
		}
		LaunchUIPlugin.setDialogShell(parent.getShell());

	}
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
		
		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_SELECTED_PROJETS_LIST, AppliedProjList);
	}
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);

		try {
			updateGroupProjectList(configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(AppliedProjList.isEmpty()) {
			label.setText(" ");
		} else {
			label.setText(AppliedProjList.toString());
		}
		
		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_SELECTED_PROJETS_LIST, AppliedProjList);
	}
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		
		try {
			updateGroupProjectList(configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(AppliedProjList.isEmpty()) {
			label.setText(" ");
		} else {
			label.setText(AppliedProjList.toString());
		}

	}

	public String getName() {

		return TAB_NAME;
	}

	private void updateGroupProjectList(String fSelectedProjectName) {


		AppliedProjList.clear();

		//go over all project fill the list with projects which match core and name patterns
		IProject fProjectsToLaunch[] =  ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for(int pp =0 ; pp < fProjectsToLaunch.length; pp ++ ) {
			IProject projTolaunch = fProjectsToLaunch[pp];

			if( projTolaunch.isAccessible() != true) {
				continue;
			}
			String projName = projTolaunch.getName();
			if(   projName.contains("core") && projName.contains(fSelectedProjectName)) {
				AppliedProjList.add(projName);
			}				
		}

	}

	private void addButtons(Composite composite) {

		IProject selectedPrpj =  ResourcesPlugin.getWorkspace().getRoot().getProject();

		// listViewer.setSelection( new StructuredSelection(aas));
		//		FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
		//		fillLayout.spacing = 2;
		//
		//		composite.setLayout(fillLayout);

		buttonAdd = new Button(composite, SWT.PUSH);
		buttonAdd.setText("Add");


		buttonAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

//				label.setText("AAAAAAAAAAAAAAAA");



			}
		});
	}
}

/*
public class LaunchConfigurationTabProjectsSelection extends
AbstractLaunchConfigurationTab {









	@Override
	public void createControl(Composite parent) {



	}


	public void initializeFrom(ILaunchConfiguration configuration) {







	}



}
 */
