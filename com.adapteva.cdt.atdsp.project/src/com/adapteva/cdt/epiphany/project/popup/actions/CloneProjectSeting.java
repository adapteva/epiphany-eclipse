package com.adapteva.cdt.epiphany.project.popup.actions;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.cdt.core.model.CoreModel;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;



public class CloneProjectSeting extends ActionDelegate implements IObjectActionDelegate {
	private IProject selectedProject = null;

	private Shell shell;

	final String PatternMC = "[.]core[.][0-9]+_[0-9]+$";

	//	public String extractBaseName( String selectedProjName) {
	//		
	//		
	//		String delims = "[_]";
	//		String[] tokens = selectedProjName.split(delims);
	//		
	//		String res = "";
	//		for(int i = 0 ; i < (tokens.length -2) ; i++ ) {
	//			res = res + tokens[i];
	//		}
	//		return res;
	//		
	//	}

	//return the "base" name of multicores project or null if pattern is not matched
	public String getMatchBaseName(String projName){	
		String baseName = null;

		Pattern pattern = Pattern.compile(PatternMC);
		Matcher matcher = pattern.matcher(projName);
		// Check all occurrence
		if(matcher.find()) {
			baseName =  projName.substring(0, matcher.start()-1);
		}


		return baseName;

	}

	/**
	 * Constructor for Action1.
	 */
	public CloneProjectSeting() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();



	}
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	//	@SuppressWarnings("restriction")
	public void run(IAction action) {
		ArrayList<IProject> iProjects = new ArrayList<IProject>() ;
		ArrayList<String> AppliedProjList =new ArrayList<String>() ;
		
		ICProjectDescriptionManager mgr = CoreModel.getDefault().getProjectDescriptionManager();
		IProject fProjectSrc = getSelectedProject();
		String baseName = getMatchBaseName(fProjectSrc.getName());

		ListSelectionDialog dlg = new ListSelectionDialog(
				shell,
				ResourcesPlugin.getWorkspace().getRoot(), 
				new BaseWorkbenchContentProvider(),
				new WorkbenchLabelProvider(),
		"Select the Project(s) to apply the C/C++ setting from the selected project " + fProjectSrc.getName());

		
		IProject fDestProjects[] =  ResourcesPlugin.getWorkspace().getRoot().getProjects();

		//filter out non relevant project by default
		for(int pp =0 ; pp < fDestProjects.length; pp ++ ) {
			IProject fProjectDst = fDestProjects[pp];

			if( fProjectDst.isAccessible() != true) {
				continue;
			}

			//src == dst
			if(fProjectDst ==  fProjectSrc ) {
				continue;
			}
			
			String coreProjPattern =  baseName + PatternMC;
			Pattern pattern = Pattern.compile( coreProjPattern);
			Matcher matcher = pattern.matcher(fProjectDst.getName());

			//check if matches
			if(!matcher.find()) {
				continue;
			}

			iProjects.add(fProjectDst);
		}
		
		dlg.setInitialElementSelections(iProjects);
		dlg.setTitle("Project Selection");
		dlg.open();
		

		//if(fProjectSrc != null && baseName != null) 


		//IProject fProjectDst = ResourcesPlugin.getWorkspace().getRoot().getProject("b");

		Object selectedProjects [] = dlg.getResult();
		if(selectedProjects == null) {
			return;
		}
	
		for (Object objProject : selectedProjects) {
			IProject fProjectDst = (IProject) objProject;

			ICProjectDescription des = mgr.getProjectDescription(fProjectDst, true);

			if (des == null) {
				continue;
			}
			//src == dst
			if(fProjectDst ==  fProjectSrc ) {
				continue;
			}
			
			AppliedProjList.add(fProjectDst.getName());


			String fSavedConfName = ManagedBuildManager.getBuildInfo(fProjectSrc).getDefaultConfiguration().getName();



			ManagedBuildManager.getBuildInfo(fProjectDst).setDefaultConfiguration(fSavedConfName);


			String configurationsName [] = ManagedBuildManager.getBuildInfo(fProjectDst).getConfigurationNames();


			for( int confNamesInd = 0;  confNamesInd < configurationsName.length; confNamesInd++) {

				String confName = configurationsName[confNamesInd];

				ManagedBuildManager.getBuildInfo(fProjectDst).setDefaultConfiguration(confName);
				//FIXME check if exist
				IConfiguration fDefConfDest = ManagedBuildManager.getBuildInfo(fProjectDst).getDefaultConfiguration();

				ICProjectDescription projectDescriptionSrc = CoreModel.getDefault().getProjectDescription(fProjectSrc);
				//ICConfigurationDescription activeConfSrc = projectDescriptionSrc.getActiveConfiguration(); // or another config
				ICConfigurationDescription activeConfSrc = projectDescriptionSrc.getConfigurationByName(confName);

				IConfiguration cfgSrc = ManagedBuildManager.getConfigurationForDescription(activeConfSrc);


				ICFolderDescription fDescSrc = activeConfSrc.getRootFolderDescription();


				ICLanguageSetting[] languageSettings = fDescSrc.getLanguageSettings();
				

				IFolderInfo fInfo = cfgSrc.getRootFolderInfo();

				//		ICProjectDescription projectDescriptionDst = CoreModel.getDefault().getProjectDescription(fProjectDst);
				//		ICConfigurationDescription activeConfDst = projectDescriptionDst.getActiveConfiguration(); // or another config
				//		//IConfiguration activeConfig_b = ManagedBuildManager.getConfigurationForDescription(activeConfDst);

				ITool [] tools = fInfo.getTools();
				for(int t = 0; t < tools.length ; t++ ) {

					//ITool linkerTool = fInfo.getTools()[3];//getTool("cdt.managedbuild.tool.gnu.c.linker.atdsp.exe.debug")[3];

					ITool tool_a = fInfo.getTools()[t];

					ITool tool_b = fDefConfDest.getTools()[t];



					//ITool linkerTool = activeConfig.getTargetTool();
					//ITool linkerTool = fInfo.
					//IOption option = tool.getOptionBySuperClassId("lv.tests.libFiles.option");
					//IConfiguration activeConfig = ManagedBuildManager.getBuildInfo(fProject).getDefaultConfiguration();
					//ITool[] tools = activeConfig.getTools();



					//ITool linkerTool_b = activeConfig_b.getTargetTool();

					IOption opts[] = tool_a.getOptions();


					for(int i = 0; i < opts.length; i++){
						IOption option = opts[i];
						String sId = option.getBaseId();

						IOption optSource = tool_a.getOptionBySuperClassId(sId);

						IOption optDest = tool_b.getOptionBySuperClassId(sId);

						//need patch for -L option : I have no idea why the the option is not copied in regular way
						if(sId.matches("gnu.c.link.option.paths") ) {


							ArrayList<String> libPathList =new ArrayList<String>() ;

							//FIXME
							if(languageSettings.length  <3 ) {
								continue;
							}
							
							int nl = 0;
							boolean fFoundLinkerSetting = false;
							for(nl = 0 ; nl < languageSettings.length; nl++) {
								if(languageSettings[nl].getId().startsWith("cdt.managedbuild.tool.gnu.c.linker")) {
									fFoundLinkerSetting=true;
									break;
								}
							}
							
							//
							ICLanguageSetting lang = languageSettings[nl]; // pick one from languageSettings, by id
							String aaa = lang.getId();
							
							
							ICLanguageSettingEntry libPathSettings[] = lang.getSettingEntries(ICSettingEntry.LIBRARY_PATH);

							for(int j = 0; j < libPathSettings.length; j++){

								ICLanguageSettingEntry libPathSetting = libPathSettings[j];

								String vvv ;

								int Flag = libPathSetting.getFlags() ;

								if(Flag ==  ICSettingEntry.VALUE_WORKSPACE_PATH)  {
									vvv  = "\"${workspace_loc:" + libPathSetting.getValue() + "}\"";
								} else {
									vvv  = libPathSetting.getValue();
								}

								libPathList.add(vvv);

							}



							String[] updated = (String[])libPathList.toArray(new String[0]);

							//String[] updated = (String[])list.toArray(new String[0]);
							ManagedBuildManager.setOption(fDefConfDest, tool_b, optDest, updated);
						}

						try {

							String tmp_value[] = optSource.getDefinedSymbols();

							ManagedBuildManager.setOption(fDefConfDest, tool_b, optDest, tmp_value);

						} catch (BuildException e) {

						}


						try {

							String tmp_value[] = optSource.getIncludePaths();

							ManagedBuildManager.setOption(fDefConfDest, tool_b, optDest, tmp_value);

						} catch (BuildException e) {

						}

						try {
							String tmp_value[] = optSource.getUserObjects();

							ManagedBuildManager.setOption(fDefConfDest, tool_b, optDest, tmp_value);

						} catch (BuildException e) {

						}

						try {
							String tmp_value[] = optSource.getLibraries();

							ManagedBuildManager.setOption(fDefConfDest, tool_b, optDest, tmp_value);

						} catch (BuildException e) {
						}

						try {
							String tmp_value[] = optSource.getLibraryFiles();

							ManagedBuildManager.setOption(fDefConfDest, tool_b, optDest, tmp_value);

						} catch (BuildException e) {
						}		

						int opType;

						try {
							opType = optSource.getValueType();
							//opType = optSource.getBasicValueType();

							switch(opType){ 
							case IOption.BOOLEAN:
							{
								boolean value = optSource.getBooleanValue();
								ManagedBuildManager.setOption(fDefConfDest, tool_b, optDest, value);

								break;
							}
							case IOption.ENUMERATED:
							case IOption.STRING:
							{
								String value = optSource.getStringValue();
								ManagedBuildManager.setOption(fDefConfDest, tool_b, optDest, value);
								break;
							}
							case IOption.STRING_LIST:
							case IOption.INCLUDE_PATH:
							case IOption.PREPROCESSOR_SYMBOLS:
							case IOption.LIBRARIES:
							case IOption.OBJECTS:
							case IOption.INCLUDE_FILES:
							case IOption.LIBRARY_PATHS:
							case IOption.LIBRARY_FILES:
							case IOption.MACRO_FILES:
							{
								String value[] = optSource.getStringListValue();
								ManagedBuildManager.setOption(fDefConfDest, tool_b, optDest, value);
								break;
							}

							default:
								//				org.eclipse.core.runtime.Assert.  ("wrong option type passed");
							}


						} catch (BuildException e1) {

							IOption option_ = opts[i];
							String sId_ = option.getBaseId();


						}

					}

					//}


				}


				ManagedBuildManager.getBuildInfo(fProjectDst).setDefaultConfiguration(fSavedConfName);
				ManagedBuildManager.saveBuildInfo(fProjectDst, true);				

			}



		}




		MessageDialog.openInformation(
				shell,
				"Apply The C/C++ setting for other core projects",
				"The C/C++ project <" + this.getSelectedProject().getName() + "> setting has been applied for the following projects:\n" +    AppliedProjList.toString() );



	}



	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			Object obj = sel.getFirstElement();
			if (obj instanceof IProject) {
				IProject project = (IProject)obj;				 
				// Save the selected project.
				setSelectedProject(project);
				return;
			}
		}
		setSelectedProject(null);
	}
	/**
	 * @return Returns the selectedProject.
	 */
	private IProject getSelectedProject() {
		return selectedProject;
	}

	/**
	 * @param selectedProject The selectedProject to set.
	 */
	private void setSelectedProject(IProject selectedProject) {
		this.selectedProject = selectedProject;
	}
}







/*		
ICProjectDescriptionManager mgr = CoreModel.getDefault().getProjectDescriptionManager();
ICProjectDescription des = mgr.getProjectDescription(fProject_b, true);

try {
	des = mgr.createProjectDescription(fProject, true);
} catch (CoreException e1) {
	// TODO Auto-generated catch block
	e1.printStackTrace();
}

ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(fProject_b);


IProjectType projType =
         ManagedBuildManager.getExtensionProjectType("my.project.type"); // or get projectType from UI
IToolChain toolChain =
         ManagedBuildManager.getExtensionToolChain("my.toolchain"); // or get toolChain from UI

ManagedProject mProj = new ManagedProject(fProject, projType);
info.setManagedProject(mProj);

IConfiguration[] configs = ManagedBuildManager.getExtensionConfigurations(toolChain, projType);

for (IConfiguration icf : configs) {
    if (!(icf instanceof Configuration)) {
        continue;
    }
    Configuration cf = (Configuration) icf;

    String id = ManagedBuildManager.calculateChildId(cf.getId(), null);
    Configuration config = new Configuration(mProj, cf, id, false, true);

    ICConfigurationDescription cfgDes;
	try {
		cfgDes = des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID,
		                                     config.getConfigurationData());


	    config.setConfigurationDescription(cfgDes);
	    config.exportArtifactInfo();

	    IBuilder bld = config.getEditableBuilder();
	    if (bld != null) { bld.setManagedBuildOn(true); }

	    config.setName(toolChain.getName());
	    config.setArtifactName(fProject.getName());


	} catch (WriteAccessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (CoreException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}


}

try {
	mgr.setProjectDescription(fProject, des);
} catch (CoreException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
 */	




//ICProjectDescriptionManager mgr = CoreModel.getDefault().getProjectDescriptionManager();
//ICProjectDescription des = mgr.getProjectDescription(fProject_b, false);

//IConfiguration activeConfig = ManagedBuildManager.getBuildInfo(fProject).getDefaultConfiguration();

//	ICProjectDescriptionManager mgr = CoreModel.getDefault().getProjectDescriptionManager();
//	ICProjectDescription des = mgr.getProjectDescription(fProject, false);








//
//		try {
//			int tt = optL__.getBasicValueType();
//		} catch (BuildException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}


//		String ss = optL__.getCommand();
//		try {
//			String sss[] = optL__.getBasicStringListValue();
//			OptionStringValue[] dsadfdf = optL__.getBasicStringListValueElements();
//
//			ManagedBuildManager.setOption(activeConfig_b, linkerTool_b, optL_b, sss);
//
//
//		} catch (BuildException e3) {
//			// TODO Auto-generated catch block
//			e3.printStackTrace();
//		}
//
//		try {
//			OptionStringValue[] ves = optL.getBasicStringListValueElements();
//		} catch (BuildException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}


//		List list = new ArrayList();
//		list.add("usr_1");
//		list.add("usr_2");
//		try {
//			list.addAll(Arrays.asList(optL_b.getBasicStringListValue()));
//		} catch (BuildException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		//String[] updated = (String[])list.toArray(new String[0]);
//		
//		String[] updated = null;
//		try {
//			updated = optL.getBasicStringListValue();
//		} catch (BuildException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		ManagedBuildManager.setOption(activeConfig_b, linkerTool_b, optL_b, updated);



//		ManagedBuildInfo buildInfo = ManagedBuildManager.createBuildInfo(fProject_b);
//		for (IConfiguration config : buildInfo.getManagedProject().getConfigurations())
//		        CfgDiscoveredPathManager.getInstance().removeDiscoveredInfo(fProject_b, new CfgInfoContext(config));

//Object oo= optL.getValue();

//optL_b.setValue(oo);

//		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
//		if (buildInfo != null)
//		    for (IConfiguration config : buildInfo.getManagedProject().getConfigurations())
//		        CfgDiscoveredPathManager.getInstance().removeDiscoveredInfo(project, new CfgInfoContext(config));



//		try {
//			//mgr.setProjectDescription(fProject_b, des);
//			// change things in the buildInfo
//			
//			
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
