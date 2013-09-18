package com.adapteva.cdt.mclaunch;


import java.io.File;

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


/*
 * sage: e-loader [ -version ] [ -gdbserver_host_addr <ip-addr>] [ -gdbserver_port <port base number > ] [-no_reset_target] srecProgramFile
Program options:
	 srecProgramFile       The file to be loaded in the SREC format
	 -gdbserver_host_addr   IP address of the gdb server machine, The default is 127.0.0.1 (same machine)
	 -gdbserver_port        The base port number where the gdbserver will accept connections, the default is 51000
	 -run_target            Send ILAT set for current coreid  to the hardware after the core program has been loaded to the target
	 -no_reset_target        Don't send the RESET requect to the hardware platform

 */
public class LaunchConfigurationTabLoaderOptions extends AbstractLaunchConfigurationTab {
	private static final String TAB_NAME = "E-loader options";
	
	Button doReset;
	Button doRun;
	
	Text gdbserverHostAddr;
	private Label gdbserverHostAddrlabel;
	
	Text gdbserverHostPortNum;
	private Label gdbserverHostPortNumLabel;

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
		
		
		sc.setMinSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
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
		
		doReset = new Button(comp, SWT.CHECK);
		doReset.setText("Reset target");
		doReset.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doResetChanged();
				updateLaunchConfigurationDialog();
			}
		});
		
		
		doRun = new Button(comp, SWT.CHECK);
		doRun.setText("Run target");
		gd = new GridData();
		gd.horizontalSpan = 1;
		doRun.setLayoutData(gd);
		doRun.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
				

		
		gdbserverHostAddrlabel = new Label(comp, SWT.NONE);
		gdbserverHostAddrlabel.setText("IP address of the e-gdbserver running machine");
		gdbserverHostAddr = new Text(comp, SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 800;
		gdbserverHostAddr.setLayoutData(gd);
		
		gdbserverHostAddr.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				//FIXME
				//e.doit = (Character.isDigit(e.character) || e.character == '.'); 
				
			}
		});
		
		gdbserverHostAddr.addModifyListener(new ModifyListener() {
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
		

		
	}

	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_RESET, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_RESET);
		
		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_RUN, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_RUN);
		
		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_IP_ADDRESS, MCLaunchConstants.MC_LAUNCH_DEFAULT_IP_ADDRESS);
		
		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_PORT_NUMBER_LOADER, MCLaunchConstants.MC_LAUNCH_DEFAULT_LOADER_IP_PORT_NUMBER);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			// Initialization Commands
			doReset.setSelection(configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_RESET, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_RESET));
			doRun.setSelection(configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_RUN, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_RUN));
			
			gdbserverHostAddr.setText(configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_IP_ADDRESS , MCLaunchConstants.MC_LAUNCH_DEFAULT_IP_ADDRESS));
			gdbserverHostPortNum.setText(configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_PORT_NUMBER_LOADER , MCLaunchConstants.MC_LAUNCH_DEFAULT_LOADER_IP_PORT_NUMBER));

		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_RESET, doReset.getSelection());
		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_RUN, doRun.getSelection());
		
		
		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_IP_ADDRESS, gdbserverHostAddr.getText());
		configuration.setAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_PORT_NUMBER_LOADER, gdbserverHostPortNum.getText());
	}

	@Override
	public String getName() {
		
		return TAB_NAME;
	}

	
	
	private void doResetChanged() {
		
	}


}


