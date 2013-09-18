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

/**
 * @author Doug Schaefer
 * 
 */
public class LaunchConfigurationDelegate1 extends AbstractCLaunchDelegate {

	protected String getPluginID() {
		return Activator.PLUGIN_ID;
	};

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		SubMonitor submonitor = SubMonitor.convert(monitor, 2);
		// set the default source locator if required
		setDefaultSourceLocator(launch, configuration);
		
		
		
		boolean doReset = configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_RESET, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_RESET);
		boolean doRun = configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_RUN, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_RUN);
		String hostIpAddr = configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_DEFAULT_IP_ADDRESS, MCLaunchConstants.MC_LAUNCH_DEFAULT_IP_ADDRESS);
		String portNum = 	configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_PORT_NUMBER_LOADER, MCLaunchConstants.MC_LAUNCH_DEFAULT_LOADER_IP_PORT_NUMBER);	

		ICProject cproject = CDebugUtils.verifyCProject(configuration);

		IPath location = cproject.getProject().getLocation();


		Process loaderProcess = null;


		
		

		
		String cmdLine = "e-loader " ;
		if(!doReset) {
			cmdLine += "-no_reset_target ";
		}
		if(doRun) {
			cmdLine += "-run_target ";
		}
		cmdLine += " -gdbserver_host_addr " + hostIpAddr;
		
		cmdLine += " -gdbserver_port " + portNum;
		
		//add the srec file
		String s = " "  + location.toString() + "/Cores/" + location.lastSegment()+".srec";
		cmdLine+= s;
		
		
		try {
			loaderProcess = ProcessFactory.getFactory().exec(cmdLine);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if ( loaderProcess != null ) {
			IProcess iprocess = DebugPlugin.newProcess(launch, loaderProcess, "loader Process");

			String s1 = iprocess.getStreamsProxy().getOutputStreamMonitor().getContents();
			String ss = iprocess.getStreamsProxy().getErrorStreamMonitor().getContents();
		}

		//DebugPlugin.exec("ls -rtl",location.toString());
		//MessageConsole myConsole = findConsole("e-loader console");
		//MessageConsoleStream out = myConsole.newMessageStream();
		//out.println("Hello from Generic console sample action");

	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		//no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[]{myConsole});
		return myConsole;
	}

}





