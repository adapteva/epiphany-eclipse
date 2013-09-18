package com.adapteva.cdt.epiphany.project;

import java.util.Map;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.internal.core.scannerconfig.CfgDiscoveredPathManager;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorCleaner;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.templateengine.ProjectCreatedActions;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


public class EpiphanyProjectProcessRunnerForSingleCore extends ProcessRunner  {
	protected boolean savedAutoBuildingValue;
	protected ProjectCreatedActions pca;
	protected IManagedBuildInfo info;
	
	

	public EpiphanyProjectProcessRunnerForSingleCore() {
		pca = new ProjectCreatedActions();
	}
	
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {

		String projectNameBase = args[0].getSimpleValue();
		IProject projectBase = ResourcesPlugin.getWorkspace().getRoot().getProject(projectNameBase);
		
		ChangeDiscoveryCompilerRunCommand(projectBase);
		
	}

	@SuppressWarnings("restriction")
	private void ChangeDiscoveryCompilerRunCommand(IProject project) throws ProcessFailureException {
		//discovery update 
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] configs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : configs) {
		
			
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
	
	
}