package com.adapteva.cdt.mclaunch;




import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.gdbjtag.core.GDBJtagDebugger;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContribution;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContributionFactory;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.command.factories.CommandFactoryDescriptor;
import org.eclipse.cdt.debug.mi.core.command.factories.CommandFactoryManager;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;


public class EGDBSingleCoreLaunchConfigurationDelegate extends AbstractCLaunchDelegate {


	protected String getPluginID() {
		return Activator.PLUGIN_ID;
	};



	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {


		String selectedProjName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");


		IProject projTolaunch =  ResourcesPlugin.getWorkspace().getRoot().getProject(selectedProjName);

		if( projTolaunch.isAccessible() != true) {
			return;
		}
		
		ILaunchConfigurationWorkingCopy configuration_working= configuration.getWorkingCopy();

		setNotDefaults(configuration_working,projTolaunch);
		configuration_working.doSave();


		SubMonitor submonitor = SubMonitor.convert(monitor, 2);
		// set the default source locator if required
		setDefaultSourceLocator(launch, configuration);


		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			GDBJtagDebugger debugger = new GDBJtagDebugger();
			ICProject project = CDebugUtils.verifyCProject(configuration);
			IPath exePath = CDebugUtils.verifyProgramPath(configuration);
			ICDISession session = debugger.createSession(launch, null, submonitor.newChild(1));



			IBinaryObject exeBinary = null;
			if ( exePath != null ) {
				exeBinary = verifyBinary(project, exePath);
			}

			try {
				// create the Launch targets/processes for eclipse.
				ICDITarget[] targets = session.getTargets();
				for( int i = 0; i < targets.length; i++ ) {
					Process process = targets[i].getProcess();
					IProcess iprocess = null;
					if ( process != null ) {
						iprocess = DebugPlugin.newProcess(launch, process, renderProcessLabel(exePath != null ? exePath.toOSString() : "???"),
								getDefaultProcessMap() );
					}
					CDIDebugModel.newDebugTarget(launch, project.getProject(), targets[i],
							renderProcessLabel("E-GDB." + projTolaunch.getName()), iprocess, exeBinary, true, false, false);
				}

				debugger.doRunSession(launch, session, submonitor.newChild(1));
			} catch (CoreException e) {
				try {
					session.terminate();
				} catch (CDIException e1) {
					// ignore
				}
				throw e;
			}
		} else {
			cancel("TargetConfiguration not supported",
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}

	}

	public void setNotDefaults(ILaunchConfigurationWorkingCopy configuration, IProject projTolaunch) {

		///------------------ Debugger
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "e-gdb");

		CommandFactoryManager cfManager = MIPlugin.getDefault().getCommandFactoryManager();///Linux
		CommandFactoryDescriptor defDesc = cfManager.getDefaultDescriptor(IGDBJtagConstants.DEBUGGER_ID);
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY, defDesc.getName());

		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, defDesc.getMIVersions()[0]);//MI


		//break point full path
		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_FULLPATH_BREAKPOINTS, true);

		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET, IGDBJtagConstants.DEFAULT_USE_REMOTE_TARGET);//true -- use remote


		GDBJtagDeviceContribution[] availableDevices = GDBJtagDeviceContributionFactory.getInstance().getGDBJtagDeviceContribution();


		//set simulator
		String savedJtagDevice = availableDevices[0].getDeviceName();
		for(int i = 0 ; i < availableDevices.length ; i++ ) {
			String ss = availableDevices[i].getDeviceId() ;

			if(ss.startsWith("atdsp.com.atdsp.debug.atdspGdbSimulator")) {
				//				savedJtagDevice= availableDevices[i].getDeviceName();
				//				configuration.setAttribute(IGDBJtagConstants.ATTR_RUN_COMMANDS, "run");
			}


			if(ss.startsWith("atdsp.com.atdsp.debug.atdspGdbServer")) {
				savedJtagDevice= availableDevices[i].getDeviceName();
				//configuration.setAttribute(IGDBJtagConstants.ATTR_RUN_COMMANDS, "continue");
			}
		}

		configuration.setAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE, savedJtagDevice);

		configuration.setAttribute(IGDBJtagConstants.ATTR_CONNECTION, "e-gdb");

		////////////////// Start up 
		// Initialization Commands
		//configuration.setAttribute(IGDBJtagConstants.ATTR_DO_RESET, IGDBJtagConstants.DEFAULT_DO_RESET);
		configuration.setAttribute(IGDBJtagConstants.ATTR_DO_RESET, false);
		configuration.setAttribute(IGDBJtagConstants.ATTR_DELAY, IGDBJtagConstants.DEFAULT_DELAY);
		//configuration.setAttribute(IGDBJtagConstants.ATTR_DO_HALT, IGDBJtagConstants.DEFAULT_DO_HALT);
		configuration.setAttribute(IGDBJtagConstants.ATTR_DO_HALT, false);


		// Load Image...
		configuration.setAttribute(IGDBJtagConstants.ATTR_LOAD_IMAGE, IGDBJtagConstants.DEFAULT_LOAD_IMAGE);
		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_IMAGE, IGDBJtagConstants.DEFAULT_USE_PROJ_BINARY_FOR_IMAGE);
		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_FILE_FOR_IMAGE, IGDBJtagConstants.DEFAULT_USE_FILE_FOR_IMAGE);
		configuration.setAttribute(IGDBJtagConstants.ATTR_IMAGE_FILE_NAME, IGDBJtagConstants.DEFAULT_IMAGE_FILE_NAME);
		configuration.setAttribute(IGDBJtagConstants.ATTR_IMAGE_OFFSET, IGDBJtagConstants.DEFAULT_IMAGE_OFFSET);

		//.. and Symbols
		configuration.setAttribute(IGDBJtagConstants.ATTR_LOAD_SYMBOLS, IGDBJtagConstants.DEFAULT_LOAD_SYMBOLS);
		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_SYMBOLS, IGDBJtagConstants.DEFAULT_USE_PROJ_BINARY_FOR_SYMBOLS);
		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_FILE_FOR_SYMBOLS, IGDBJtagConstants.DEFAULT_USE_FILE_FOR_SYMBOLS);
		configuration.setAttribute(IGDBJtagConstants.ATTR_SYMBOLS_FILE_NAME, IGDBJtagConstants.DEFAULT_SYMBOLS_FILE_NAME);
		configuration.setAttribute(IGDBJtagConstants.ATTR_SYMBOLS_OFFSET, IGDBJtagConstants.DEFAULT_SYMBOLS_OFFSET);

		// Runtime Options
		configuration.setAttribute(IGDBJtagConstants.ATTR_SET_PC_REGISTER, IGDBJtagConstants.DEFAULT_SET_PC_REGISTER);
		
//		//port number 
//		
//		String portNumberString;
//		try {
//			portNumberString = configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_PORT_NUMBER_E_GDBSERVER_RUN,
//					MCLaunchConstants.MC_LAUNCH_DEFAULT_GDBSERVER_PORT_NUMBER);
//			
//			configuration.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER , Integer.parseInt(portNumberString));	
//			
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		

	}

}
