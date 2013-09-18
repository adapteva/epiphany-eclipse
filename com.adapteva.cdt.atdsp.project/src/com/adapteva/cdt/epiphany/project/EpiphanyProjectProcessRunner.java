/**
 * 
 */
package com.adapteva.cdt.epiphany.project;

/**
 * @author oraikhman
 *
 */


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.BufferedInputStream;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.internal.core.scannerconfig.CfgDiscoveredPathManager;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateEngineHelper;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessHelper;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.core.templateengine.process.processes.CreateFolder;
import org.eclipse.cdt.core.templateengine.process.processes.Messages;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorCleaner;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.templateengine.ProjectCreatedActions;

import org.eclipse.cdt.core.templateengine.process.processes.CreateSourceFolder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;

import org.eclipse.cdt.managedbuilder.core.BuildException;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.ui.PlatformUI;


/**
 * Creates a new Project in the workspace.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class EpiphanyProjectProcessRunner extends ProcessRunner  {
	protected boolean savedAutoBuildingValue;
	protected ProjectCreatedActions pca;
	protected IManagedBuildInfo info;
	
	private static final String COREID_PROPERTY = "COREID";

	public EpiphanyProjectProcessRunner() {
		pca = new ProjectCreatedActions();
	}

	public void ChangeBuilTypeForLibProject(String projectNameBase) {

		///modify the project lib (ArtefactType)
		String projectLibName = projectNameBase + "_commonlib";
		IProject projectLib = ResourcesPlugin.getWorkspace().getRoot().getProject(projectLibName);
		
		
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(projectLib);
		IConfiguration[] configs = buildInfo.getManagedProject().getConfigurations();
		
		for (IConfiguration config : configs) {
			try {
				config.setBuildArtefactType("org.eclipse.cdt.build.core.buildArtefactType.staticLib");
			} catch (BuildException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			ChangeDiscoveryCompilerRunCommand(projectLib);
		} catch (ProcessFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ManagedBuildManager.saveBuildInfo(projectLib, true);		
	}

	
	public void  AddRefLibProject(IProject project, String projectLibName) {
		
		//prepare reference map
		String pName = new String(projectLibName);	
		HashMap<String, String> hMap = new HashMap<String, String>() ;
		hMap.put(pName, "");	
		

		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescription desBase = coreModel.getProjectDescription(project);
		
		ICConfigurationDescription dess[] = desBase.getConfigurations();
		for (ICConfigurationDescription cfgMain : dess) {
			
			cfgMain.setReferenceInfo(hMap);
			try {
				coreModel.setProjectDescription(project, desBase);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void AddCommonLibPath(IProject project, String libProjectName) {
		
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] configs = buildInfo.getManagedProject().getConfigurations();
		
		for (IConfiguration config : configs) {
			
			ITool linkerTool = config.getTargetTool();
			
			IOption optLinkPath = linkerTool.getOptionBySuperClassId("gnu.c.link.option.paths");
			ArrayList<String> libPathList =new ArrayList<String>() ;
			
			String confName = config.getName();
			
			libPathList.add( new String ("\"${workspace_loc:/" + libProjectName + "/" +  confName + "}\""));
			
			
			//libPathList.add( new String ("../../" + libProjectName + "/" + confName));
			
			String[] libPaths = (String[])libPathList.toArray(new String[0]);
			
			ManagedBuildManager.setOption(config, linkerTool, optLinkPath, libPaths);
			
		
			IOption optLinkLibs = linkerTool.getOptionBySuperClassId("gnu.c.link.option.libs");
			ArrayList<String> libPathLibs =new ArrayList<String>() ;
			libPathLibs.add( new String (libProjectName ));
			
			String[] libs = (String[])libPathLibs.toArray(new String[0]);
			
			ManagedBuildManager.setOption(config, linkerTool, optLinkLibs, libs);
		}	
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {


		String projectNameBase = args[0].getSimpleValue();
		String locationBase = args[1].getSimpleValue();
		String location = new String(locationBase);
		String artifactExtension = args[2].getSimpleValue();
		String isCProjectValue = args[3].getSimpleValue();
		String sourceFolderName  = args[4].getSimpleValue();
		String coreIdNameExtention = args[5].getSimpleValue();

		ProcessArgument[] rolColArgs = args[6].getComplexValue();	


		
		ChangeBuilTypeForLibProject(projectNameBase);

		int nRows = Integer.parseInt(rolColArgs[0].getSimpleValue().trim());
		int nCols = Integer.parseInt(rolColArgs[1].getSimpleValue().trim());
		int nStartRowNumber,nStartColNumber;
		try {
			nStartRowNumber = Integer.parseInt(rolColArgs[2].getSimpleValue().trim(),10);
			nStartColNumber = Integer.parseInt(rolColArgs[3].getSimpleValue().trim(),10);
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block

			throw new ProcessFailureException(Messages.getString("Wrong coordinate number formant") + e1.getMessage(), e1); //$NON-NLS-1$
		}

		//String numberCoresStr = args[6].getSimpleValue();
		ProcessArgument firstFile = args[7];//file to create
		ProcessArgument mainFileArg = args[8];//file to create
		ProcessArgument makefile = args[9];//file to create
		ProcessArgument makefileCoresMk = args[10];//file to create
		ProcessArgument coordinatesFile = args[11];//file to create
		ProcessArgument optionsetTry = args[12];//not used



		//create common folder
		String common_file = "// Multicore project 'common file'\n";
		common_file+=  "unsigned mc_core_common_go() {\n return 0;\n}\n";
		


		IProject projectBase = ResourcesPlugin.getWorkspace().getRoot().getProject(projectNameBase);	
		IConfiguration activeConfig = ManagedBuildManager.getBuildInfo(projectBase).getDefaultConfiguration();
		try {
			activeConfig.setManagedBuildOn(false);
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		//add file to folder .. makefile
		AddFile(template,   projectNameBase,  makefile, processId, monitor);	
		//modify makefile
		for(int j=0; j<nRows; j++){
			for(int i=0; i<nCols; i++){
				String currentCore =  Integer.toString(nStartRowNumber + j, 10) + "_" + Integer.toString(nStartColNumber + i, 10);
				String coreId = Integer.toString((((nStartRowNumber + j) << 6)  |  (nStartColNumber + i)),16);
				String toAppend = new String(
						"ALL_ELF += ../$(TARGET).core."+currentCore+"/Debug/$(TARGET).core."+currentCore+".elf \n" +
						"ALL_SREC +=   ./Cores/$(TARGET).core."+currentCore+".srec \n" +
						"./Cores/$(TARGET).core."+currentCore+".srec : ../$(TARGET).core."+currentCore+"/Debug/$(TARGET).core."
						+currentCore+".elf\n"+
						"\t@mkdir -p ./Cores/\n" +
						"\t@e-objcopy --coreid " + coreId +" --srec-forceS3 --output-target srec \"$<\" \"$@\"\n"+
						"\t@echo Creating srec file for CoreID\\<0x" + coreId + "\\>\n"+
						"\n\n"
				);

				AppendFileWithContent(template,   projectNameBase, toAppend , makefileCoresMk.getComplexValue()[1].getSimpleValue(), processId, monitor);

			}
		}


		HashMap<String, String> hMap = new HashMap<String, String>() ;
		
		for(int j=0; j<nRows; j++){
			for(int i=0; i<nCols; i++){


				String currentCore =  Integer.toString(nStartRowNumber + j, 10) + "_" + Integer.toString(nStartColNumber + i, 10);

				String projectName = new String( projectNameBase.concat(coreIdNameExtention) + "."+ currentCore);

				boolean isCProject = Boolean.valueOf(isCProjectValue).booleanValue();

				final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);			

				if (!project.exists()) {
					try {
						IWorkspace workspace = ResourcesPlugin.getWorkspace();
						turnOffAutoBuild(workspace);

						IPath locationPath = null;
						if (location != null && !location.trim().equals("")) { //$NON-NLS-1$
							locationPath = Path.fromPortableString(location);
						}

						@SuppressWarnings("unchecked")
						List<IConfiguration> configs = (List<IConfiguration>) template.getTemplateInfo().getConfigurations();
						if (configs == null || configs.size() == 0) {
							throw new ProcessFailureException(Messages.getString("NewManagedProject.4") + projectName); //$NON-NLS-1$
						}

						pca.setProject(project);
						pca.setProjectLocation(locationPath);
						pca.setConfigs(configs.toArray(new IConfiguration[configs.size()]));
						pca.setArtifactExtension(artifactExtension);
						info = pca.createProject(monitor, CCorePlugin.DEFAULT_INDEXER, isCProject);

						info.setValid(true);
						
						
						
						ChangeDiscoveryCompilerRunCommand(project);	
						
						AddRefLibProject(project, projectNameBase + "_commonlib");
						
						

						//restoreAutoBuild(workspace);

					} catch (CoreException e) {
						throw new ProcessFailureException(Messages.getString("NewManagedProject.3") + e.getMessage(), e); //$NON-NLS-1$
					} catch (BuildException e) {
						throw new ProcessFailureException(Messages.getString("NewManagedProject.3") + e.getMessage(), e); //$NON-NLS-1$
					}

					
					//create SRC folder
					createSourceFolder(projectName, sourceFolderName, monitor);
					

					//add common linb to the project
					AddCommonLibPath(project,projectNameBase + "_commonlib");


					//add file to folder .. basename --------- Moved to lib
					//AddFile(template,   projectName,  firstFile, processId, monitor);
					//String contentToAppend = new String ("");
					//AppendFileWithContent(template,   projectName,  contentToAppend, firstFile.getComplexValue()[1].getSimpleValue(), processId, monitor);


					//main.c

					ProcessArgument[] fileMembers = mainFileArg.getComplexValue();

					AppendCreate(template,   projectName,   fileMembers[0].getSimpleValue(),   
							fileMembers[1].getSimpleValue(),    fileMembers[2].getSimpleValue().equals("true")          , processId, monitor);

					//batch gdb file 
					String batch_gdbFile =  "batch_gdb.x" ;
					String batch_gdbFileCommon =  "batch_gdb_common.x" ;
					AppendCreate(template,   projectName,batch_gdbFile ,  batch_gdbFile ,
							fileMembers[2].getSimpleValue().equals("true")          , processId, monitor);

					int portNum = 51000 + j * nCols + i;
					String remotetargetStr = "target remote :" + portNum + "\n";

					AppendFileWithContent(template,   projectName, remotetargetStr , batch_gdbFile, processId, monitor);
					AppendCreate(template,   projectName,batch_gdbFileCommon ,  batch_gdbFile ,
							fileMembers[2].getSimpleValue().equals("true")          , processId, monitor);
					AppendFileWithContent(template,   projectName, "# e-gdb -x " +  batch_gdbFile + " "+ projectName + ".elf", batch_gdbFile, processId, monitor);

					//coordinates

					fileMembers = coordinatesFile.getComplexValue();

					AppendCreate(template,   projectName,   fileMembers[0].getSimpleValue(),   
							fileMembers[1].getSimpleValue(),    fileMembers[2].getSimpleValue().equals("true")          , processId, monitor);

					String coordinates = "\nasm(\".set __CORE_ROW_," + Integer.toString(nStartRowNumber + j,10)  + "\");\n";
					coordinates = coordinates + "asm(\".set __CORE_COL_," +Integer.toString(nStartColNumber + i,10) + "\");\n";

					AppendFileWithContent(template,   projectName, coordinates , coordinatesFile.getComplexValue()[1].getSimpleValue(), processId, monitor);



				} else {
					//			throw new ProcessFailureException(Messages.getString("NewManagedProject.5") + projectName); //$NON-NLS-1$
				}


				//prepare reference map
				String pName = new String(project.getName());	
				hMap.put(pName, "");	
				
				//create core id property
				String coreId = Integer.toString((((nStartRowNumber + j) << 6)  |  (nStartColNumber + i)),16);
				try {
					project.setPersistentProperty(new QualifiedName("", COREID_PROPERTY), coreId);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				ManagedBuildManager.saveBuildInfo(project, true);
			}	
		}//end of for ( row,col)


		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescription desBase = coreModel.getProjectDescription(projectBase);
		ICConfigurationDescription dess[] = desBase.getConfigurations();
		for (ICConfigurationDescription cfgMain : dess) {
			// Main Project references core.n project	
			cfgMain.setReferenceInfo(hMap);
			try {
				coreModel.setProjectDescription(projectBase, desBase);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		ChangeDiscoveryCompilerRunCommand(projectBase);
		
		ManagedBuildManager.saveBuildInfo(projectBase, true);

	}

	@SuppressWarnings("restriction")
	private void ChangeDiscoveryCompilerRunCommand(IProject project) throws ProcessFailureException {
		//discovery update 
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] configs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : configs) {
		
			
			@SuppressWarnings("restriction")
			ICfgScannerConfigBuilderInfo2Set cbi = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(config);
			
			Map<CfgInfoContext, IScannerConfigBuilderInfo2> infoMap = cbi.getInfoMap();
			CfgInfoContext cic = infoMap.entrySet().iterator().next().getKey();
			
			
			DiscoveredPathInfo pathInfo = new DiscoveredPathInfo(project);
			InfoContext infoContext = cic.toInfoContext();
			
			// 1. Remove scanner info from .metadata/.plugins/org.eclipse.cdt.make.core/Project.sc
			DiscoveredScannerInfoStore dsiStore = DiscoveredScannerInfoStore.getInstance();
			try {
				dsiStore.saveDiscoveredScannerInfoToState(project, infoContext, pathInfo);
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			@SuppressWarnings("restriction")
			// 2. Remove scanner info from CfgDiscoveredPathManager cache and from the Tool
			CfgDiscoveredPathManager cdpManager = CfgDiscoveredPathManager.getInstance();
			cdpManager.removeDiscoveredInfo(project, cic);
			
			
			// 3. Remove scanner info from SI collector
			ICfgScannerConfigBuilderInfo2Set info2 = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(config);
			Map<CfgInfoContext, IScannerConfigBuilderInfo2> infoMap2 = info2.getInfoMap();
			IScannerConfigBuilderInfo2 buildInfo2 = infoMap2.get(cic);
			if (buildInfo2!=null) {
				ScannerConfigProfileManager scpManager = ScannerConfigProfileManager.getInstance();
				String selectedProfileId = buildInfo2.getSelectedProfileId();
				SCProfileInstance profileInstance = scpManager.getSCProfileInstance(project, infoContext, selectedProfileId);
				
				IScannerInfoCollector collector = profileInstance.getScannerInfoCollector();
				if (collector instanceof IScannerInfoCollectorCleaner) {
					((IScannerInfoCollectorCleaner) collector).deleteAll(project);
				}
				buildInfo2 = null;
			}
			
			
			
			Map<CfgInfoContext, IScannerConfigBuilderInfo2> map = cbi.getInfoMap();
			IScannerConfigBuilderInfo2 bi = map.values().iterator().next();
			String providerId = "specsFile";
			String runCommand = bi.getProviderRunCommand(providerId);
			bi.setProviderRunCommand(providerId, "e-gcc");
			
			bi.setAutoDiscoveryEnabled(true);
			
			
			try {
				cbi.applyInfo(cic, bi);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				bi.save();
			} catch (CoreException e) {
				throw new ProcessFailureException(e);
			}
			


		}
		
		ManagedBuildManager.saveBuildInfo(project, true);
		
		
		
		// Save the project description
		ICProjectDescription prjDesc = CCorePlugin.getDefault().getProjectDescription(project);
		try {
			CoreModel.getDefault().setProjectDescription(project, prjDesc);
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	protected final void turnOffAutoBuild(IWorkspace workspace) throws CoreException {
		IWorkspaceDescription workspaceDesc = workspace.getDescription();
		savedAutoBuildingValue = workspaceDesc.isAutoBuilding();
		workspaceDesc.setAutoBuilding(false);
		workspace.setDescription(workspaceDesc);
	}

	protected final void restoreAutoBuild(IWorkspace workspace) throws CoreException {
		IWorkspaceDescription workspaceDesc = workspace.getDescription();
		workspaceDesc.setAutoBuilding(savedAutoBuildingValue);
		workspace.setDescription(workspaceDesc);
	}




	protected void createSourceFolder(String projectName, String targetPath, IProgressMonitor monitor) throws ProcessFailureException {
		//If the targetPath is an empty string, there will be no source folder to create.
		// Also this is not an error. So just return gracefully.
		if (targetPath == null || targetPath.length()==0) {
			return;
		}

		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		if (!projectHandle.exists()) {
			throw new ProcessFailureException(Messages.getString("CreateSourceFolder.0") + projectName); //$NON-NLS-1$
		}

		CreateFolder.createFolder(projectName, targetPath, monitor);

		IPath projPath = projectHandle.getFullPath();
		IFolder folder = projectHandle.getFolder(targetPath);

		try {
			ICProject cProject = CoreModel.getDefault().create(projectHandle);
			if (cProject != null) {
				if(CCorePlugin.getDefault().isNewStyleProject(cProject.getProject())){
					//create source folder for new style project
					createNewStyleProjectFolder(monitor, projectHandle, folder);
				} else {
					//create source folder for all other projects 
					createFolder(targetPath, monitor, projPath, cProject);
				}
			}
		} catch (WriteAccessException e) {
			throw new ProcessFailureException(Messages.getString("CreateSourceFolder.2") + e.getMessage(), e); //$NON-NLS-1$
		} catch (CoreException e) {
			throw new ProcessFailureException(Messages.getString("CreateSourceFolder.2") + e.getMessage(), e); //$NON-NLS-1$
		}
	}

	/**
	 * @param monitor
	 * @param projectHandle
	 * @param folder
	 * @throws CoreException
	 * @throws WriteAccessException
	 */
	private void createNewStyleProjectFolder(IProgressMonitor monitor, IProject projectHandle, IFolder folder) throws CoreException, WriteAccessException {
		ICSourceEntry newEntry = new CSourceEntry(folder, null, 0); 
		ICProjectDescription description = CCorePlugin.getDefault().getProjectDescription(projectHandle);

		ICConfigurationDescription configs[] = description.getConfigurations();
		for(int i=0; i < configs.length; i++){
			ICConfigurationDescription config = configs[i];
			ICSourceEntry[] entries = config.getSourceEntries();
			Set<ICSourceEntry> set = new HashSet<ICSourceEntry>();
			for (int j=0; j < entries.length; j++) {
				if(new Path(entries[j].getValue()).segmentCount() == 1)
					continue;
				set.add(entries[j]);
			}
			set.add(newEntry);
			config.setSourceEntries(set.toArray(new ICSourceEntry[set.size()]));
		}

		CCorePlugin.getDefault().setProjectDescription(projectHandle, description, false, monitor);
	}

	/**
	 * @param targetPath
	 * @param monitor
	 * @param 
	 * @param cProject
	 * @throws CModelException
	 */
	private void createFolder(String targetPath, IProgressMonitor monitor, IPath projPath, ICProject cProject) throws CModelException {
		IPathEntry[] entries = cProject.getRawPathEntries();
		List<IPathEntry> newEntries = new ArrayList<IPathEntry>(entries.length + 1);

		int projectEntryIndex= -1;
		IPath path = projPath.append(targetPath);

		for (int i = 0; i < entries.length; i++) {
			IPathEntry curr = entries[i];
			if (path.equals(curr.getPath())) {
				// just return if this folder exists already
				return;
			}
			if (projPath.equals(curr.getPath())) {
				projectEntryIndex = i;
			}	
			newEntries.add(curr);
		}

		IPathEntry newEntry = CoreModel.newSourceEntry(path);

		if (projectEntryIndex != -1) {
			newEntries.set(projectEntryIndex, newEntry);
		} else {
			newEntries.add(CoreModel.newSourceEntry(path));
		}

		cProject.setRawPathEntries(newEntries.toArray(new IPathEntry[newEntries.size()]), monitor);
	}


	/**
	 * This method Adds the File to the corresponding Project.
	 */
	@SuppressWarnings("deprecation")
	public void AddFile(TemplateCore template,  String projectName, ProcessArgument file,   String processId, IProgressMonitor monitor) throws ProcessFailureException {

		ProcessArgument[] fileMembers = file.getComplexValue();

		String fileSourcePath = fileMembers[0].getSimpleValue();
		String fileTargetPath = fileMembers[1].getSimpleValue();
		boolean replaceable = fileMembers[2].getSimpleValue().equals("true"); //$NON-NLS-1$

		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		URL path;
		try {
			path = TemplateEngineHelper.getTemplateResourceURLRelativeToTemplate(template, fileSourcePath);
			if (path == null) {
				throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, Messages.getString("AddFile.0") + fileSourcePath)); //$NON-NLS-1$
			}
		} catch (IOException e1) {
			throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, Messages.getString("AddFile.1") + fileSourcePath)); //$NON-NLS-1$
		}

		InputStream contents = null;
		if (replaceable) {
			String fileContents;
			try {
				fileContents = ProcessHelper.readFromFile(path);
			} catch (IOException e) {
				throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, Messages.getString("AddFile.2") + fileSourcePath)); //$NON-NLS-1$
			}
			fileContents = ProcessHelper.getValueAfterExpandingMacros(fileContents, ProcessHelper.getReplaceKeys(fileContents), template.getValueStore());
			contents = new ByteArrayInputStream(fileContents.getBytes());
		} else {
			try {
				contents = path.openStream();
			} catch (IOException e) {
				throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, Messages.getString("AddFile.3") + fileSourcePath)); //$NON-NLS-1$
			}
		}

		try {
			IFile iFile = projectHandle.getFile(fileTargetPath);
			if (!iFile.getParent().exists()) {
				ProcessHelper.mkdirs(projectHandle, projectHandle.getFolder(iFile.getParent().getProjectRelativePath()));
			}
			if(iFile.exists()) {
				iFile.delete(true, null);
			}
			
			if(!iFile.exists()) {
				iFile.create(contents, true, null);	
			} else {
				iFile.setContents(contents, true, true, null);
			}
			iFile.refreshLocal(IResource.DEPTH_ONE, null);
			projectHandle.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, Messages.getString("AddFile.4") + e.getMessage()), e); //$NON-NLS-1$
		}
	}


	public void SetMBSStringOptionValue(TemplateCore template, String projectName , ProcessArgument args, String processId, IProgressMonitor monitor) throws ProcessFailureException {

		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription workspaceDesc = workspace.getDescription();
		boolean autoBuilding = workspaceDesc.isAutoBuilding();
		workspaceDesc.setAutoBuilding(false);
		try {
			workspace.setDescription(workspaceDesc);
		} catch (CoreException e) {//ignore
		}

		ProcessArgument[][] resourcePathObjects = args.getComplexArrayValue();
		boolean modified = false;
		for (ProcessArgument[] resourcePathObject : resourcePathObjects) {
			String id = resourcePathObject[0].getSimpleValue();
			String value = resourcePathObject[1].getSimpleValue();
			String path = resourcePathObject[2].getSimpleValue();
			try {
				modified |= setOptionValue(projectHandle, id, value, path);
			} catch (BuildException e) {
				throw new ProcessFailureException(Messages.getString("SetMBSStringOptionValue.0") + e.getMessage(), e); //$NON-NLS-1$
			}
		}
		if (modified) {
			ManagedBuildManager.saveBuildInfo(projectHandle, true);
		}

		workspaceDesc.setAutoBuilding(autoBuilding);
		try {
			workspace.setDescription(workspaceDesc);
		} catch (CoreException e) {//ignore
		}
	}

	private boolean setOptionValue(IProject projectHandle, String id, String value, String path) throws BuildException, ProcessFailureException {
		IConfiguration[] projectConfigs = ManagedBuildManager.getBuildInfo(projectHandle).getManagedProject().getConfigurations();

		boolean resource = !(path == null || path.equals("") || path.equals("/")); //$NON-NLS-1$ //$NON-NLS-2$
		boolean modified = false;

		for (IConfiguration config : projectConfigs) {
			IResourceConfiguration resourceConfig = null;
			if (resource) {
				resourceConfig = config.getResourceConfiguration(path);
				if (resourceConfig == null) {
					IFile file = projectHandle.getFile(path);
					if (file == null) {
						throw new ProcessFailureException(Messages.getString("SetMBSStringOptionValue.3") + path); //$NON-NLS-1$
					}
					resourceConfig = config.createResourceConfiguration(file);
				}
				ITool[] tools = resourceConfig.getTools();
				for (ITool tool : tools) {
					modified |= setOptionForResourceConfig(id, value, resourceConfig, tool.getOptions(), tool);
				}
			} else {
				IToolChain toolChain = config.getToolChain();
				modified |= setOptionForConfig(id, value, config, toolChain.getOptions(), toolChain);

				ITool[] tools = config.getTools();
				for (ITool tool : tools) {
					modified |= setOptionForConfig(id, value, config, tool.getOptions(), tool);
				}
			}
		}

		return modified;
	}

	private boolean setOptionForResourceConfig(String id, String value, IResourceConfiguration resourceConfig, IOption[] options, IHoldsOptions optionHolder) throws BuildException {
		boolean modified = false;
		String lowerId = id.toLowerCase();
		for (IOption option : options) {
			if (option.getBaseId().toLowerCase().matches(lowerId)) {
				int optionType = option.getValueType();
				if ((optionType == IOption.STRING) || (optionType == IOption.ENUMERATED)) {
					ManagedBuildManager.setOption(resourceConfig, optionHolder, option, value);
					modified = true;
				}
			}
		}
		return modified;
	}

	private boolean setOptionForConfig(String id, String value, IConfiguration config, IOption[] options, IHoldsOptions optionHolder) throws BuildException {
		boolean modified = false;
		String lowerId = id.toLowerCase();
		for (IOption option : options) {
			if (option.getBaseId().toLowerCase().matches(lowerId)) {
				int optionType = option.getValueType();
				if ((optionType == IOption.STRING) || (optionType == IOption.ENUMERATED)) {
					ManagedBuildManager.setOption(config, optionHolder, option, value);
					modified = true;
				}
			}
		}
		return modified;
	}

	/*
	 * Appends a file to an existing file if present. If not, create the file
	 */

	void AppendCreate(TemplateCore template, String projectName , String sourcePath, String targetPath ,boolean replaceable , String processId, IProgressMonitor monitor) throws ProcessFailureException {

		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		URL sourceURL;
		try {
			sourceURL = TemplateEngineHelper.getTemplateResourceURLRelativeToTemplate(template, sourcePath);
			if (sourceURL == null) {
				throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, Messages.getString("AppendCreate.1") + sourcePath)); //$NON-NLS-1$
			}
		} catch (IOException e1) {
			throw new ProcessFailureException(Messages.getString("AppendCreate.2") + sourcePath); //$NON-NLS-1$
		}
		String fileContents;
		try {
			fileContents = ProcessHelper.readFromFile(sourceURL);
		} catch (IOException e1) {
			throw new ProcessFailureException(Messages.getString("AppendCreate.3") + sourcePath); //$NON-NLS-1$
		}
		if (replaceable) {
			fileContents = ProcessHelper.getValueAfterExpandingMacros(fileContents, ProcessHelper.getReplaceKeys(fileContents), template.getValueStore());
		}
		try {
			// Check whether the file exists
			IFile iFile = projectHandle.getFile(targetPath);
			if (!iFile.getParent().exists()) {
				ProcessHelper.mkdirs(projectHandle, projectHandle.getFolder(iFile.getParent().getProjectRelativePath()));
			} 
			InputStream contents = new ByteArrayInputStream(fileContents.getBytes());
			if (!iFile.exists()) {
				// Create the file
				iFile.create(contents, true, null);
				iFile.refreshLocal(IResource.DEPTH_ONE, null);

			} else {
				// Append the file keeping the history
				iFile.appendContents(contents, true, true, null);
			}
			// Update the project
			projectHandle.refreshLocal(IResource.DEPTH_INFINITE, null);

		} catch (CoreException e) {
			throw new ProcessFailureException(Messages.getString("AppendCreate.4"), e); //$NON-NLS-1$
		}

	}
	void AppendFileWithContent(TemplateCore template, String projectName , String fileContents, /*ProcessArgument file*/ String targetPath, String processId, IProgressMonitor monitor) throws ProcessFailureException {

		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		//		ProcessArgument[] fileMembers = file.getComplexValue();

		//		String sourcePath = fileMembers[0].getSimpleValue();
		//		String targetPath = fileMembers[1].getSimpleValue();
		//		boolean replaceable = fileMembers[2].getSimpleValue().equals("true"); //$NON-NLS-1$

		try {
			// Check whether the file exists
			IFile iFile = projectHandle.getFile(targetPath);
			if (!iFile.getParent().exists()) {
				ProcessHelper.mkdirs(projectHandle, projectHandle.getFolder(iFile.getParent().getProjectRelativePath()));
			} 
			InputStream contents = new ByteArrayInputStream(fileContents.getBytes());
			if (!iFile.exists()) {
				// Create the file
				iFile.create(contents, true, null);
				iFile.refreshLocal(IResource.DEPTH_ONE, null);

			} else {
				// Append the file keeping the history
				iFile.appendContents(contents, true, true, null);
			}
			// Update the project
			projectHandle.refreshLocal(IResource.DEPTH_INFINITE, null);

		} catch (CoreException e) {
			throw new ProcessFailureException(Messages.getString("AppendCreate.4"), e); //$NON-NLS-1$
		}

	}


	private static String readFileAsString(String filePath) throws java.io.IOException{
		byte[] buffer = new byte[(int) new File(filePath).length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(filePath));
			f.read(buffer);
		} finally {
			if (f != null) try { f.close(); } catch (IOException ignored) { }
		}
		return new String(buffer);
	}

	
	
	public void CreateIncludeFolder( String projectName,String targetPath ,String processId, IProgressMonitor monitor) throws ProcessFailureException {

		createSourceFolder(projectName, targetPath, monitor);
		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(projectHandle);
		try {
			IConfiguration[] configs = info.getManagedProject().getConfigurations();
			for (IConfiguration config : configs) {
				String path = projectHandle.getFolder(targetPath).getLocation().toOSString();
				IToolChain toolChain = config.getToolChain();
				setIncludePathOptionForConfig(path, config, toolChain.getOptions(), toolChain);

				ITool[] tools = config.getTools();
				for (ITool tool : tools) {
					setIncludePathOptionForConfig(path, config, tool.getOptions(), tool);
				}
			}
		} catch (BuildException e) {
			throw new ProcessFailureException(Messages.getString("CreateIncludeFolder.3") + e.getMessage(), e); //$NON-NLS-1$
		}
		ManagedBuildManager.saveBuildInfo(projectHandle, true);
	}

	private void setIncludePathOptionForConfig(String path, IConfiguration config, IOption[] options, IHoldsOptions optionHolder) throws BuildException {
		for (IOption option : options) {
			if (option.getValueType() == IOption.INCLUDE_PATH) {
				String[] includePaths = option.getIncludePaths();
				String[] newPaths = new String[includePaths.length + 1];
				System.arraycopy(includePaths, 0, newPaths, 0, includePaths.length);
				newPaths[includePaths.length] = path;
				ManagedBuildManager.setOption(config, optionHolder, option, newPaths);
			}
		}
	}
}




/*
 * 

 */



//contentToAppend = new String ( "__CORE_ROW_ = 0x"  + Integer.toHexString(nStartRowNumber + j)  + ";\n" +
//							  "__CORE_COL_ = 0x"  + Integer.toHexString(nStartColNumber + i)  + ";\n" );


//AppendFileWithContent(template,   projectName,  contentToAppend, ldfFile.getComplexValue()[1].getSimpleValue(), processId, monitor);

//AppendFileWithContent(template,   projectName,  defLDfFileString, ldfFile.getComplexValue()[1].getSimpleValue(), processId, monitor);


//SetMBSStringOptionValue( template,  projectName , optionsetTry,  processId,  monitor);

//AppendFileWithContent(template,   projectName,  file, "AAAAAAAAAAAAAAAA" ,processId, monitor);

//Append(template,   projectName,  file, processId, monitor);
//!!!!!!!!!
//			try {
//				setOptionValue(project, "coreid", "12", "");
//			} catch (BuildException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}//String id, String value, String path

//	Append(template,   projectName,  file, processId, monitor);


//create file with core id ni=umber
/*
FileWriter outputStream = null;
try {

try {

outputStream = new FileWriter("characteroutput.txt");
outputStream.write("AAAAAAAAa");
} catch (IOException e) {
throw new ProcessFailureException(Messages.getString("Append.4.1"), e); //$NON-NLS-1$
}


} finally {

if (outputStream != null) {
try {
	outputStream.close();
} catch (IOException e) {
	throw new ProcessFailureException(Messages.getString("Append.4.2"), e); //$NON-NLS-1$
}
}
}
 */
/*
 * catch (IOException e) {
throw new ProcessFailureException(Messages.getString("Append.4"), e); //$NON-NLS-1$
}
 */

//				toAppend = new String(
//						"ALL_RUNS +=   ./Cores/$(TARGET).core."+currentCore+".gdb \n" +
//						"./Cores/$(TARGET).core."+currentCore+".gdb : ../$(TARGET).core."+currentCore+"/Debug/$(TARGET).core."
//						+currentCore+".elf\n"
//						+"../$(TARGET).core."+currentCore+"/Debug/$(TARGET).core."+currentCore+".elf\n"
//						+"target remote :5100\n" + "load\n" + "c\n" + "q\n" 
//						+"\n\n"
//				);
//
//				AppendFileWithContent(template,   projectNameBase, toAppend , makefileCoresMk.getComplexValue()[1].getSimpleValue(), processId, monitor);
//			


//CreateIncludeFolder( projectNameBase,"Includes" , processId,  monitor);


//		PlatformUI.getWorkbench().getDisplay().getActiveShell();
//		String defLdfFile= System.getenv("EPIPHANY_TOOLS_HOME");
//		
//		String osname = System.getProperty("os.name") ;
//		
//		defLdfFile = defLdfFile + "linux/epiphany-unknown-elf/lib/ldscripts/elf32epiphany.x";
//
//		String defLDfFileString = new String("/*${EPIPHANY_TOOLS_HOME}/linux/epiphany-unknown-elf/lib/ldscripts/elf32epiphany.x*/\n");
//		try {
//			defLDfFileString = defLDfFileString + new String(readFileAsString(defLdfFile));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//AppendCreate(template,   projectNameBase,   defLdfFile,   "AAAA.LDF",  true  , processId, monitor);


//add project references

//		File file = new File(defLdfFile);
//		IPath path = new Path(file.getAbsolutePath());
//		IFile f = ResourcesPlugin.getWorkspace().getRoot().getFile(path); 

//		IWorkspace workspace= ResourcesPlugin.getWorkspace();
//		IPath ldfLoc= Path.fromOSString(file.getAbsolutePath());
//		IFile f= workspace.getRoot().getFileForLocation(ldfLoc); 
//		
//		try {
//			InputStream contents = 	f.getContents();
//		} catch (CoreException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

//		IPath ldfFLoc = new Path(defLdfFile);
//		IFile file = projectBase.getFile(ldfFLoc);
//		try {
//			InputStream contents = 	file.getContents();
//		} catch (CoreException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		
//		try {
//			file.createLink(ldfFLoc, IResource.NONE, null);
//		} catch (CoreException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
