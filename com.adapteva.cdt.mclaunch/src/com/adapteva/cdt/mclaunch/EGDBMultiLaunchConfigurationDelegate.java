package com.adapteva.cdt.mclaunch;


import java.awt.List;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;



import com.adapteva.cdt.mclaunch.proxy.debugger.MCProxyDebugTarget;


public class EGDBMultiLaunchConfigurationDelegate extends AbstractCLaunchDelegate {

	protected String getPluginID() {
		return Activator.PLUGIN_ID;
	};

	@SuppressWarnings({ "restriction", "static-access" })
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		String hostName = configuration.getAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, MCLaunchConstants.MC_LAUNCH_DEFAULT_HOST_NAME);
		int pNum = configuration.getAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, MCLaunchConstants.MC_LAUNCH_DEFAULT_GDBSERVER_PORT_NUMBER_INT);

		//the echo process will reset the target before entering to wait loop
		ArrayList<String>  commandList = new ArrayList<String>();
		commandList.add("echo-process");
		commandList.add(hostName);
		commandList.add(Integer.toString(pNum-2));// server port will be listening on base -2 

		String[] commandLine = (String[]) commandList.toArray(new String[commandList.size()]);
		Process process = DebugPlugin.exec(commandLine, null);


		IProcess p = DebugPlugin.newProcess(launch, process, "Multicore e-gdb control console");

		//For now just wait for 3 seconds
		try {
			Thread.currentThread().sleep(3000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
				//FIXME add Listener and do laucnch once the echo process responded with OK
				//		IStreamMonitor watchForResetProxy = p.getStreamsProxy().getErrorStreamMonitor();



				//		boolean isServerResponded = false;
				//		for( int i = 0 ; i < 3 ; i ++ ) { 
				//			String respondFromEchoProcess = watchForResetProxy.getContents();
				//			if(respondFromEchoProcess.contains("OK")) {
				//				isServerResponded = true;
				//				break;
				//			}
				//
				//			try {
				//				Thread.currentThread().sleep(2000);
				//			} catch (InterruptedException e1) {
				//				try {
				//					p.getStreamsProxy().write("ERROR: Can't Connect to e-server, check the server is up");
				//				} catch (IOException e) {
				//					// TODO Auto-generated catch block
				//					e.printStackTrace();
				//				}
				//				
				//				
				//				return;
				//			}//sleep for 1000 ms
				//			
				//		}
				//		if(!isServerResponded) {
				//			try {
				//				p.getStreamsProxy().write("ERROR (internal): Can't reset the platfrom");
				//			} catch (IOException e) {
				//				// TODO Auto-generated catch block
				//				e.printStackTrace();
				//			}
				//			return;
				//		}


				if(configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_GDBTERMINAL_ONLY, 
						MCLaunchConstants.MC_LAUNCH__DEFAULT_DO_GDBTERMINAL_ONLY)) {
					try {
						launchTerminalMode(configuration,mode,launch,p,monitor);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					launchFullMode(configuration,mode,launch,p,monitor);
				}

			}


			/**
			 * Create the gdb terminal for cores, no source level debugging or breakpoint are supported
			 * @param configuration
			 * @param mode
			 * @param launch
			 * @param monitor
			 * @throws CoreException
			 * @throws IOException 
			 */
			void launchTerminalMode(ILaunchConfiguration configuration, String mode,
					ILaunch launch, IProcess iechoProcess, IProgressMonitor monitor) throws CoreException, IOException {

				class MCGdbStreamListenerEcho implements IStreamListener {
					ArrayList<IProcess> fGdbProcess;

					@Override
					public void streamAppended(String text, IStreamMonitor monitor) {
						Iterator<IProcess> gdbProcessIt = fGdbProcess.iterator();
						while (gdbProcessIt.hasNext()) {
							try {
								gdbProcessIt.next().getStreamsProxy().write(text);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}

				}

				MCGdbStreamListenerEcho gdbEcho = new MCGdbStreamListenerEcho();
				gdbEcho.fGdbProcess = new ArrayList<IProcess>();

				@SuppressWarnings("unchecked")
				ArrayList<String> AppliedProjList = (ArrayList<String>) configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_SELECTED_PROJETS_LIST, (ArrayList<String>) new ArrayList<String>());

				Iterator<String> itr = AppliedProjList.iterator();

				int portOffset =0;

				while (itr.hasNext()) {


					String prName = itr.next();

					IProject projTolaunch =  ResourcesPlugin.getWorkspace().getRoot().getProject(prName);

					String execPath = getBinaryPathFromProject(projTolaunch);

					ArrayList<String>  commandList = new ArrayList<String>();
					commandList.add("e-gdb");

					String[] commandLine = (String[]) commandList.toArray(new String[commandList.size()]);
					Process process = DebugPlugin.exec(commandLine, null);
					IProcess p = DebugPlugin.newProcess(launch, process, "e-gdb." + prName);

					String hostName = configuration.getAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, MCLaunchConstants.MC_LAUNCH_DEFAULT_HOST_NAME);

					int pNum = portOffset + configuration.getAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, MCLaunchConstants.MC_LAUNCH_DEFAULT_GDBSERVER_PORT_NUMBER_INT);

					portOffset++;

					try {
						String coreIDVal = projTolaunch.getPersistentProperty(new QualifiedName("", MCLaunchConstants.COREID_PROPERTY));

						if(coreIDVal != null) { 
							coreIDVal = "0x" + coreIDVal;
							p.getStreamsProxy().write("set coreid " + coreIDVal + "\n");
						}

					} catch (CoreException e) {

					}

					p.getStreamsProxy().write("file " + execPath + "\n");
					p.getStreamsProxy().write("target remote " + hostName + ":" + pNum+ "\n");
					p.getStreamsProxy().write("load\n");

					if(configuration.getAttribute(IGDBJtagConstants.ATTR_SET_STOP_AT, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_STOP_AT_MAIN)) {
						p.getStreamsProxy().write("b main\n");
					}

					if(configuration.getAttribute(IGDBJtagConstants.ATTR_SET_RESUME, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_RESUME)) {
						p.getStreamsProxy().write("continue \n");
					}
					//TODO init and run commands

					//broadcast echo to all e-gdbs
					gdbEcho.fGdbProcess.add(p);
				}

				iechoProcess.getStreamsProxy().write("#Just type the valid gdb commands\n");		
				iechoProcess.getStreamsProxy().getOutputStreamMonitor().addListener(gdbEcho);
			}
			/**
			 * Create and single core launch 
			 * @param configuration
			 * @param mode
			 * @param launch
			 * @param monitor
			 * @throws CoreException
			 */

			void launchFullMode(ILaunchConfiguration configuration, String mode,
					ILaunch launch, IProcess iechoProcess, IProgressMonitor monitor) throws CoreException {



				@SuppressWarnings("unchecked")
				ArrayList<String> AppliedProjList = (ArrayList<String>) configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_SELECTED_PROJETS_LIST, (ArrayList<String>) new ArrayList<String>());

				ArrayList<ILaunch> launchList = new ArrayList<ILaunch> ();


				ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

				ILaunchConfiguration [] allConfs =  manager.getLaunchConfigurations();
				ILaunch [] lls = manager.getLaunches();

				Iterator<String> itr = AppliedProjList.iterator();

				while (itr.hasNext()) {

					String prName = itr.next();

					IProject projTolaunch =  ResourcesPlugin.getWorkspace().getRoot().getProject(prName);

					if( projTolaunch.isAccessible() != true) {
						continue;
					}


					for (int j=0; j < lls.length; j++) {

						ILaunch c_launch = lls[j];

						ILaunchConfiguration lConf = c_launch.getLaunchConfiguration();


						String mcProjectLaunchName = new String(lConf.getName());

						if(mcProjectLaunchName.contains("core")) {
							//need to terminate and remove

							if(!c_launch.isTerminated()) {
								if(c_launch.canTerminate()) {
									c_launch.terminate();
								}
							}

							manager.removeLaunch(c_launch);


						}

					}
				}


				itr = AppliedProjList.iterator();

				int portOffset =0;

				while (itr.hasNext()) {



					String prName = itr.next();

					IProject projTolaunch =  ResourcesPlugin.getWorkspace().getRoot().getProject(prName);

					if( projTolaunch.isAccessible() != true) {
						continue;
					}

					String coreIDVal = projTolaunch.getPersistentProperty(new QualifiedName("", MCLaunchConstants.COREID_PROPERTY));
					String confName =  new String(prName+ "." + coreIDVal + ".debug");


					ILaunchConfigurationType configType = 
						manager.getLaunchConfigurationType("com.adapteva.cdt.mclaunch.launchConfigurationType1");



					ILaunchConfigurationWorkingCopy workingCopy = configType.newInstance(null,confName);



					//workingCopy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, 
					//		configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, ""));

					//workingCopy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, 
					//		configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""));


					//update project name and binary location
					ICProject cProject = CoreModel.getDefault().create(projTolaunch);
					IBinary[] binaries;
					try {
						binaries = cProject.getBinaryContainer().getBinaries();

						IBinary execBinary = binaries[0];		
						String path;
						path = execBinary.getResource().getRawLocation().toOSString();
						workingCopy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, path);
						String pname =  projTolaunch.getName();
						workingCopy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, pname);
					} catch (CModelException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					workingCopy.setAttribute(IGDBJtagConstants.ATTR_SET_STOP_AT, 
							configuration.getAttribute(IGDBJtagConstants.ATTR_SET_STOP_AT, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_STOP_AT_MAIN));

					workingCopy.setAttribute(IGDBJtagConstants.ATTR_STOP_AT, "main");

					workingCopy.setAttribute(IGDBJtagConstants.ATTR_SET_RESUME, 
							configuration.getAttribute(IGDBJtagConstants.ATTR_SET_RESUME, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_RESUME));

					workingCopy.setAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS  , 
							configuration.getAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, MCLaunchConstants.MC_LAUNCH_DEFAULT_HOST_NAME));

					int portNmum = configuration.getAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, 
							MCLaunchConstants.MC_LAUNCH_DEFAULT_GDBSERVER_PORT_NUMBER_INT);

					workingCopy.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER , 		portNmum+	portOffset		);

					portOffset++;

					workingCopy.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE, 
							configuration.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE, 
									IMILaunchConfigurationConstants.DEBUGGER_VERBOSE_MODE_DEFAULT));

					workingCopy.setAttribute(IGDBJtagConstants.ATTR_INIT_COMMANDS, 
							configuration.getAttribute(IGDBJtagConstants.ATTR_INIT_COMMANDS, 
									IGDBJtagConstants.DEFAULT_INIT_COMMANDS));

					workingCopy.setAttribute(IGDBJtagConstants.ATTR_RUN_COMMANDS, 
							configuration.getAttribute(IGDBJtagConstants.ATTR_RUN_COMMANDS, 
									IGDBJtagConstants.DEFAULT_RUN_COMMANDS));


					ILaunchConfiguration new_configuration = workingCopy.doSave();


					//DebugUITools.launch(new_configuration, ILaunchManager.DEBUG_MODE);

					ILaunch cLaunch = new_configuration.launch(ILaunchManager.DEBUG_MODE, monitor);
					launchList.add(cLaunch);

					//DebugUIPlugin.launchInBackground(new_configuration, "debug");



				}//end of it 


				IDebugTarget mcproxy_target = new MCProxyDebugTarget(launch, iechoProcess,(ILaunch[]) launchList.toArray(new ILaunch[0]));
				launch.addDebugTarget(mcproxy_target);	
			}

			/**
			 * @param projTolaunch
			 * @return the elf file location for project
			 * @throws CModelException
			 */

			String getBinaryPathFromProject(IProject projTolaunch) throws CModelException {
				//update project name and binary location
				ICProject cProject = CoreModel.getDefault().create(projTolaunch);
				IBinary[] binaries;

				binaries = cProject.getBinaryContainer().getBinaries();

				IBinary execBinary = binaries[0];		
				return  execBinary.getResource().getRawLocation().toOSString();

			}


			public void launch__(ILaunchConfiguration configuration, String mode,
					ILaunch launch, IProgressMonitor monitor) throws CoreException {


				String selectedProjName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");


				@SuppressWarnings("unchecked")
				ArrayList<String> AppliedProjList = (ArrayList<String>) configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_SELECTED_PROJETS_LIST, (ArrayList<String>) new ArrayList<String>());

				if(AppliedProjList.isEmpty()) {
					AppliedProjList.add(selectedProjName);

				}


				Iterator<String> itr = AppliedProjList.iterator();
				int pp=0;
				while (itr.hasNext()) {
					String prName = itr.next();


					IProject projTolaunch =  ResourcesPlugin.getWorkspace().getRoot().getProject(prName);


					//	    }

					//		
					//		String baseProjName = getMatchBaseName(selectedProjName);
					//		if(baseProjName == null) {
					//			baseProjName = selectedProjName;
					//		} else {
					//			baseProjName = baseProjName + "";
					//		}
					//		
					//		
					//		IProject fProjectsToLaunch[] =  ResourcesPlugin.getWorkspace().getRoot().getProjects();




					//		for(int pp =0 ; pp < fProjectsToLaunch.length; pp ++ ) {
					//			IProject projTolaunch = fProjectsToLaunch[pp];

					if( projTolaunch.isAccessible() != true) {
						continue;
					}



					//			String coreProjPattern =  baseProjName + PatternMC;
					//			Pattern pattern = Pattern.compile( coreProjPattern);
					//			Matcher matcher = pattern.matcher(projTolaunch.getName());
					//
					//			//check if matches
					//			if(!matcher.find()) {
					//
					//				continue;
					//			}


					ILaunchConfigurationWorkingCopy configuration_working =  configuration.getWorkingCopy();

					//			trySetDefaults(configuration_working,projTolaunch,pp-1);

					///-------------------
					//trySetDefaults(configuration_working,projTolaunch,pp);


					pp++;


					ILaunchConfiguration configuration_saved =  configuration_working.doSave();

					SubMonitor submonitor = SubMonitor.convert(monitor, 2);
					// set the default source locator if required
					setDefaultSourceLocator(launch, configuration_saved);


					if (mode.equals(ILaunchManager.DEBUG_MODE)) {
						GDBJtagDebugger debugger = new GDBJtagDebugger();
						ICProject project = CDebugUtils.verifyCProject(configuration_saved);
						IPath exePath = CDebugUtils.verifyProgramPath(configuration_saved);
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

				//roll back the master project setting
				//ILaunchConfigurationWorkingCopy configuration_working_back =  configuration.getWorkingCopy();

				//configuration_working_back.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, "N/A --- ");
				//configuration_working_back.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, baseProjName);
				//configuration_working_back.doSave();	

			}
			//
			//	public void trySetDefaults(ILaunchConfigurationWorkingCopy configuration, IProject projTolaunch, int coreNum) {
			//
			//		//update project name and binary location
			//		ICProject cProject = CoreModel.getDefault().create(projTolaunch);
			//		IBinary[] binaries;
			//		try {
			//			binaries = cProject.getBinaryContainer().getBinaries();
			//
			//			IBinary execBinary = binaries[0];		
			//			String path;
			//			path = execBinary.getResource().getRawLocation().toOSString();
			//			configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, path);
			//			String pname =  projTolaunch.getName();
			//			configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, pname);
			//		} catch (CModelException e1) {
			//			// TODO Auto-generated catch block
			//			e1.printStackTrace();
			//		}
			//
			//		//main 
			//
			//
			////		try {
			////			String programName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, "");
			////			String projName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			////			String ss = projName;
			////
			////		} catch (CoreException e) {
			////			// TODO Auto-generated catch block
			////			e.printStackTrace();
			////		}
			//
			//		///------------------ Debugger
			//		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "e-gdb");
			//
			//		CommandFactoryManager cfManager = MIPlugin.getDefault().getCommandFactoryManager();///Linux
			//		CommandFactoryDescriptor defDesc = cfManager.getDefaultDescriptor(IGDBJtagConstants.DEBUGGER_ID);
			//		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY, defDesc.getName());
			//
			//		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, defDesc.getMIVersions()[0]);//MI
			//
			//		//configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE, IMILaunchConfigurationConstants.DEBUGGER_VERBOSE_MODE_DEFAULT);
			//
			//		//configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE,true);
			//
			//		//break point full path
			//		//configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_FULLPATH_BREAKPOINTS, IMILaunchConfigurationConstants.DEBUGGER_FULLPATH_BREAKPOINTS_DEFAULT);
			//		configuration.setAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_FULLPATH_BREAKPOINTS, true);
			//
			//
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET, IGDBJtagConstants.DEFAULT_USE_REMOTE_TARGET);//true -- use remote
			//
			//
			//		GDBJtagDeviceContribution[] availableDevices = GDBJtagDeviceContributionFactory.getInstance().getGDBJtagDeviceContribution();
			//
			//
			//		//set simulator
			//		String savedJtagDevice = availableDevices[0].getDeviceName();
			//		for(int i = 0 ; i < availableDevices.length ; i++ ) {
			//			String ss = availableDevices[i].getDeviceId() ;
			//			
			//			if(ss.startsWith("atdsp.com.atdsp.debug.atdspGdbSimulator")) {
			////				savedJtagDevice= availableDevices[i].getDeviceName();
			////				configuration.setAttribute(IGDBJtagConstants.ATTR_RUN_COMMANDS, "run");
			//			}
			//			
			//			
			//			if(ss.startsWith("atdsp.com.atdsp.debug.atdspGdbServer")) {
			//				savedJtagDevice= availableDevices[i].getDeviceName();
			//				//configuration.setAttribute(IGDBJtagConstants.ATTR_RUN_COMMANDS, "continue");
			//			}
			//		}
			//
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE, savedJtagDevice);
			//
			//
			//
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_CONNECTION, "e-gdb");
			//
			//
			//		String hostName = null;
			//		try {
			//			hostName = configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_HOST_ADDRESS, MCLaunchConstants.MC_LAUNCH_DEFAULT_HOST_NAME);
			//		} catch (CoreException e) {
			//			// TODO Auto-generated catch block
			//			e.printStackTrace();
			//		}
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, hostName);
			//		
			//		
			//		
			//		int port=0;
			//		try {
			//			port = Integer.parseInt(configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_PORT_NUMBER_E_GDBSERVER_RUN, MCLaunchConstants.MC_LAUNCH_DEFAULT_GDBSERVER_PORT_NUMBER));
			//		} catch (NumberFormatException e) {
			//			// TODO Auto-generated catch block
			//			e.printStackTrace();
			//		} catch (CoreException e) {
			//			// TODO Auto-generated catch block
			//			e.printStackTrace();
			//		}
			//		port = port +  coreNum;
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, port);
			//
			//
			//
			//		////////////////// Start up 
			//		// Initialization Commands
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_DO_RESET, IGDBJtagConstants.DEFAULT_DO_RESET);
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_DELAY, IGDBJtagConstants.DEFAULT_DELAY);
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_DO_HALT, IGDBJtagConstants.DEFAULT_DO_HALT);
			//		//done from dedicated e-gdb tab
			//		//configuration.setAttribute(IGDBJtagConstants.ATTR_INIT_COMMANDS, IGDBJtagConstants.DEFAULT_INIT_COMMANDS);
			//
			//		// Load Image...
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_LOAD_IMAGE, IGDBJtagConstants.DEFAULT_LOAD_IMAGE);
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_IMAGE, IGDBJtagConstants.DEFAULT_USE_PROJ_BINARY_FOR_IMAGE);
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_FILE_FOR_IMAGE, IGDBJtagConstants.DEFAULT_USE_FILE_FOR_IMAGE);
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_IMAGE_FILE_NAME, IGDBJtagConstants.DEFAULT_IMAGE_FILE_NAME);
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_IMAGE_OFFSET, IGDBJtagConstants.DEFAULT_IMAGE_OFFSET);
			//
			//		//.. and Symbols
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_LOAD_SYMBOLS, IGDBJtagConstants.DEFAULT_LOAD_SYMBOLS);
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_SYMBOLS, IGDBJtagConstants.DEFAULT_USE_PROJ_BINARY_FOR_SYMBOLS);
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_FILE_FOR_SYMBOLS, IGDBJtagConstants.DEFAULT_USE_FILE_FOR_SYMBOLS);
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_SYMBOLS_FILE_NAME, IGDBJtagConstants.DEFAULT_SYMBOLS_FILE_NAME);
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_SYMBOLS_OFFSET, IGDBJtagConstants.DEFAULT_SYMBOLS_OFFSET);
			//
			//		// Runtime Options
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_SET_PC_REGISTER, IGDBJtagConstants.DEFAULT_SET_PC_REGISTER);
			//		configuration.setAttribute(IGDBJtagConstants.ATTR_PC_REGISTER, IGDBJtagConstants.DEFAULT_PC_REGISTER);
			//
			//		//configuration.setAttribute(IGDBJtagConstants.ATTR_SET_STOP_AT, IGDBJtagConstants.DEFAULT_SET_STOP_AT);
			//		//configuration.setAttribute(IGDBJtagConstants.ATTR_STOP_AT, IGDBJtagConstants.DEFAULT_STOP_AT);
			//
			////		boolean setStopAtMain = true;
			////		try {
			////			setStopAtMain = configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_STOP_AT_MAIN, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_STOP_AT_MAIN);
			////		} catch (CoreException e) {
			////			// TODO Auto-generated catch block
			////			e.printStackTrace();
			////		}
			////		configuration.setAttribute(IGDBJtagConstants.ATTR_SET_STOP_AT, setStopAtMain);
			////		configuration.setAttribute(IGDBJtagConstants.ATTR_STOP_AT, "main");		
			////
			////		//configuration.setAttribute(IGDBJtagConstants.ATTR_SET_RESUME, IGDBJtagConstants.DEFAULT_SET_RESUME);
			////		boolean doResume = true; 
			////		try {
			////			doResume = configuration.getAttribute(MCLaunchConstants.MC_LAUNCH_ATTR_DO_RESUME, MCLaunchConstants.MC_LAUNCH_DEFAULT_DO_RESUME);
			////		} catch (CoreException e) {
			////			// TODO Auto-generated catch block
			////			e.printStackTrace();
			////		}
			////		configuration.setAttribute(IGDBJtagConstants.ATTR_SET_RESUME, doResume);
			//
			//		//done from dedicated e-gdb tab
			//		//configuration.setAttribute(IGDBJtagConstants.ATTR_RUN_COMMANDS, IGDBJtagConstants.DEFAULT_RUN_COMMANDS);
			//		
			//	}

		}
