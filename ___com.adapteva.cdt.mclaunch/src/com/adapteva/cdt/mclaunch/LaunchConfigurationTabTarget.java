package com.adapteva.cdt.mclaunch;

import java.io.File;

import org.eclipse.cdt.debug.core.CDebugUtils;

import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.debug.gdbjtag.ui.Activator;
import org.eclipse.cdt.debug.gdbjtag.ui.Messages;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
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
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

public class LaunchConfigurationTabTarget extends
		AbstractLaunchConfigurationTab {
	
	private static final String TAB_NAME = "Targets/e-gdb selection";
	
	Text gdbserverHostPortNum;
	private Label gdbserverHostPortNumLabel;
	
	Text gdbserverHostName;
	private Label gdbserverHostNamelabel;
	
	Button doStopAtMain;
	
	Button doMiVerbose;
	
	Button doResume;
	
	Text initCommands;
	Text runCommands;
	
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
		
		
		
		createGroup(comp);
		
		
		sc.setMinSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
	}


	public void createGroup(Composite parent) {
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
		

			
		gdbserverHostNamelabel = new Label(comp, SWT.NONE);
		gdbserverHostNamelabel.setText("Host name of the e-gdbserver running machine");
		gdbserverHostName = new Text(comp, SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 800;
		gdbserverHostName.setLayoutData(gd);
		
		gdbserverHostName.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				//FIXME
				//e.doit = (Character.isDigit(e.character) || e.character == '.'); 
				
			}
		});
		
		gdbserverHostName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				scheduleUpdateJob();
			}
		});
		

		
		gdbserverHostPortNumLabel = new Label(comp, SWT.NONE);
		gdbserverHostPortNumLabel.setText("The base port number where the e-gdbserver will accept connections");
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
		
		
		createInitGroup(comp);
		
		
		doStopAtMain = new Button(comp, SWT.CHECK);
		doStopAtMain.setText("Stop at main");
		doStopAtMain.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				updateLaunchConfigurationDialog();
			}
		});
		
		doResume = new Button(comp, SWT.CHECK);
		doResume.setText("Resume (Run untill breakpoit)");
		doResume.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				updateLaunchConfigurationDialog();
			}
		});
		
		
		

		
		doMiVerbose = new Button(comp, SWT.CHECK);
		doMiVerbose.setText("E-gdb verbose mode");
		doMiVerbose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				updateLaunchConfigurationDialog();
			}
		});
		
		
		createRunGroup(comp);
		
		
	}

	
	public void createInitGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setText(Messages.getString("GDBJtagStartupTab.initGroup_Text"));
				
		initCommands = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 60;
		initCommands.setLayoutData(gd);
		initCommands.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				scheduleUpdateJob();
			}
		});
		
	}
	
	public void createRunGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setText(Messages.getString("GDBJtagStartupTab.runGroup_Text"));

		runCommands = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 60;
		runCommands.setLayoutData(gd);
		runCommands.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				scheduleUpdateJob();
			}
		});
	}
	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_HOST_ADDRESS, MCLaunchConstants.MC_LAUNCH_DEFAULT_HOST_NAME);
		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_PORT_NUMBER_E_GDBSERVER , MCLaunchConstants.MC_LAUNCH_DEFAULT_GDBSERVER_PORT_NUMBER);
	
		//configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_STOP_AT_MAIN, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_STOP_AT_MAIN);
		configuration.setAttribute(IGDBJtagConstants.ATTR_SET_STOP_AT, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_STOP_AT_MAIN);	
		configuration.setAttribute(IGDBJtagConstants.ATTR_STOP_AT, "main");
		
		
		//configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_RESUME, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_RESUME);
		configuration.setAttribute(IGDBJtagConstants.ATTR_SET_RESUME, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_RESUME);
		
		//configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_MI_VERBOSE_MODE, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_MI_VERBOSE_MODE);
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE,IMILaunchConfigurationConstants.DEBUGGER_VERBOSE_MODE_DEFAULT);


		
		configuration.setAttribute(IGDBJtagConstants.ATTR_INIT_COMMANDS, IGDBJtagConstants.DEFAULT_INIT_COMMANDS);
		configuration.setAttribute(IGDBJtagConstants.ATTR_RUN_COMMANDS, IGDBJtagConstants.DEFAULT_RUN_COMMANDS); 
		
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {

			//doStopAtMain.setSelection(configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_STOP_AT_MAIN, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_STOP_AT_MAIN));
			doStopAtMain.setSelection(configuration.getAttribute(IGDBJtagConstants.ATTR_SET_STOP_AT, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_STOP_AT_MAIN));	
			
			
			//doResume.setSelection(configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_RESUME, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_RESUME));
			doResume.setSelection(configuration.getAttribute(IGDBJtagConstants.ATTR_SET_RESUME, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_RESUME));

			
			
			gdbserverHostName.setText(configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_HOST_ADDRESS, MCLaunchConstants.MC_LAUNCH_DEFAULT_HOST_NAME));
			gdbserverHostPortNum.setText(configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_PORT_NUMBER_E_GDBSERVER, MCLaunchConstants.MC_LAUNCH_DEFAULT_GDBSERVER_PORT_NUMBER));

			
			//doMiVerbose.setSelection(configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_MI_VERBOSE_MODE, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_MI_VERBOSE_MODE));
			doMiVerbose.setSelection(configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE,IMILaunchConfigurationConstants.DEBUGGER_VERBOSE_MODE_DEFAULT));
			
			initCommands.setText(configuration.getAttribute(IGDBJtagConstants.ATTR_INIT_COMMANDS, IGDBJtagConstants.DEFAULT_INIT_COMMANDS));
			runCommands.setText(configuration.getAttribute(IGDBJtagConstants.ATTR_RUN_COMMANDS, IGDBJtagConstants.DEFAULT_RUN_COMMANDS));
			
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		//configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_STOP_AT_MAIN, doStopAtMain.getSelection());
		configuration.setAttribute(IGDBJtagConstants.ATTR_SET_STOP_AT, doStopAtMain.getSelection());
		
		
		//configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_RESUME, doResume.getSelection());
		configuration.setAttribute(IGDBJtagConstants.ATTR_SET_RESUME, doResume.getSelection());
		
		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_HOST_ADDRESS, gdbserverHostName.getText());
		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_PORT_NUMBER_E_GDBSERVER, gdbserverHostPortNum.getText());	
		
		
		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_MI_VERBOSE_MODE, doMiVerbose.getSelection());
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE,doMiVerbose.getSelection());

		
		configuration.setAttribute(IGDBJtagConstants.ATTR_INIT_COMMANDS, initCommands.getText());
		configuration.setAttribute(IGDBJtagConstants.ATTR_RUN_COMMANDS, runCommands.getText());
		
	}

	@Override
	public String getName() {
		
		return TAB_NAME;
	}

	




}
