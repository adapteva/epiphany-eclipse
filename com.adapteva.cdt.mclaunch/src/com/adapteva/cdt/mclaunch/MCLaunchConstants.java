package com.adapteva.cdt.mclaunch;

import java.util.List;

import org.eclipse.cdt.debug.gdbjtag.core.Activator;

public interface MCLaunchConstants {
	public static final String COREID_PROPERTY = "COREID";

	public static final String MC_LAUNCH_ATTR_DO_GDBTERMINAL_ONLY = Activator.PLUGIN_ID + ".doGdbTerminalOnly"; //$NON-NLS-1$

	//public static final String DEBUGGER_ID = "org.eclipse.cdt.debug.mi.core.CDebuggerNew"; //$NON-NLS-1$
	public static final String MC_LOADER_LAUNCH_ATTR_DO_RESET = Activator.PLUGIN_ID + ".doReset"; //$NON-NLS-1$
	public static final String MC_LOADER_LAUNCH_ATTR_DO_RUN = Activator.PLUGIN_ID + ".doRun"; //$NON-NLS-1$


	public static final String MC_ESERVER_LAUNCH_ATTR_DO_MEMTEST = Activator.PLUGIN_ID + ".doMemtest"; //$NON-NLS-1$
	public static final String MC_ESERVER_LAUNCH_ATTR_DO_SHOW_MEM = Activator.PLUGIN_ID + ".doShowmem"; //$NON-NLS-1$
	public static final String MC_ESERVER_LAUNCH_ATTR_XML_FILE = Activator.PLUGIN_ID + ".xml_file"; //$NON-NLS-1$
	public static final String MC_ESERVER_LAUNCH_ATTR_XML_FILE_DEFAULT = "/bsps/emek3/emek3.xml"; //$NON-NLS-1$


	public static final String MC_LAUNCH_ATTR_DO_STOP_AT_MAIN = Activator.PLUGIN_ID + ".doStopAtMain"; //$NON-NLS-1$
	public static final String MC_LAUNCH_ATTR_DO_RESUME= Activator.PLUGIN_ID + ".doResume"; //$NON-NLS-1$
	public static final String MC_LAUNCH_ATTR_MI_VERBOSE_MODE = Activator.PLUGIN_ID + ".doMiVerboseMode"; //$NON-NLS-1$

	// Debugger
	public static final String MC_LAUNCH_ATTR_USE_REMOTE_TARGET = Activator.PLUGIN_ID + ".useRemoteTarget"; //$NON-NLS-1$

	public static final String MC_LAUNCH_ATTR_IP_ADDRESS = Activator.PLUGIN_ID + ".ipAddress"; //$NON-NLS-1$
	public static final String MC_LAUNCH_ATTR_HOST_ADDRESS = Activator.PLUGIN_ID + ".hostname"; //$NON-NLS-1$

	public static final String MC_LAUNCH_ATTR_PORT_NUMBER_ESERVER = Activator.PLUGIN_ID + ".portNumber_e_server"; //$NON-NLS-1$
	public static final String MC_LAUNCH_ATTR_PORT_NUMBER_LOADER = Activator.PLUGIN_ID + ".portNumber_loader"; //$NON-NLS-1$
	public static final String MC_LAUNCH_ATTR_PORT_NUMBER_E_GDBSERVER_RUN = Activator.PLUGIN_ID + ".portNumber_eserver"; //$NON-NLS-1$



	public static final String MC_LAUNCH_ATTR_SELECTED_PROJETS_LIST = Activator.PLUGIN_ID + ".selected_projects_list"; //$NON-NLS-1$



	public static final String MC_LAUNCH_ATTR_JTAG_DEVICE = Activator.PLUGIN_ID + ".jtagDevice"; //$NON-NLS-1$


	public static final boolean MC_LAUNCH_DEFAULT_DO_RESET = true;
	public static final boolean MC_LAUNCH_DEFAULT_DO_RUN = true;


	public static final boolean MC_LAUNCH_DEFAULT_DO_STOP_AT_MAIN = true;
	public static final boolean MC_LAUNCH_DEFAULT_DO_RESUME = true;
	public static final boolean MC_LAUNCH_DEFAULT_DO_MI_VERBOSE_MODE = false;

	public static final String MC_LAUNCH_DEFAULT_IP_ADDRESS = "127.0.0.1";
	public static final String MC_LAUNCH_DEFAULT_HOST_NAME = "localhost";


	public static final String MC_LAUNCH_DEFAULT_LOADER_IP_PORT_NUMBER = "50999";
	public static final String MC_LAUNCH_DEFAULT_GDBSERVER_PORT_NUMBER = "51000";

	public static final int MC_LAUNCH_DEFAULT_GDBSERVER_PORT_NUMBER_INT = 51000;

	public static final boolean MC_LAUNCH__DEFAULT_DO_GDBTERMINAL_ONLY = false;


};