package com.adapteva.cdt.mclaunch;

import java.io.IOException;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;


import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;


public class LaunchConfigurationDelegateEServer extends AbstractCLaunchDelegate {

	protected String getPluginID() {
		return Activator.PLUGIN_ID;
	};

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		
		boolean doShowMem = configuration.getAttribute(MCLaunchConstants.MC_ESERVER_LAUNCH_ATTR_DO_SHOW_MEM, true);
		boolean doMemTest = configuration.getAttribute(MCLaunchConstants.MC_ESERVER_LAUNCH_ATTR_DO_MEMTEST, true);
		String portNum = configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_PORT_NUMBER_E_GDBSERVER_RUN, MCLaunchConstants.MC_LAUNCH_DEFAULT_GDBSERVER_PORT_NUMBER);
		
		String xmlFile = 	configuration.getAttribute(MCLaunchConstants.MC_ESERVER_LAUNCH_ATTR_XML_FILE, System.getenv("EPIPHANY_HOME") +  MCLaunchConstants.MC_ESERVER_LAUNCH_ATTR_XML_FILE_DEFAULT);	
		
		Process eserverProcess = null;
		
		String cmdLine = "e-server " ;
		if(doShowMem) {
			cmdLine += "-show-memory-map ";
		}
		if(doMemTest) {
			cmdLine += "-test-memory ";
		}
		
		cmdLine += " -p " + portNum;
		
		cmdLine += " -xml " + xmlFile;
		
		try {
			eserverProcess = ProcessFactory.getFactory().exec(cmdLine);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if ( eserverProcess != null ) {
			IProcess iprocess = DebugPlugin.newProcess(launch, eserverProcess, "E-server Process");
		
		}

	}


}





