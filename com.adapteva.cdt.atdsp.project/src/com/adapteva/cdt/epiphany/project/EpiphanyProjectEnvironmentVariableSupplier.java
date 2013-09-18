package com.adapteva.cdt.epiphany.project;

import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.envvar.IProjectEnvironmentVariableSupplier;



public class EpiphanyProjectEnvironmentVariableSupplier  implements
		IProjectEnvironmentVariableSupplier {

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName,
			IManagedProject project, IEnvironmentVariableProvider provider) {
		
		
		if((variableName.startsWith("CPATH") )  || (variableName.startsWith("C_INCLUDE_PATH")) ) {//
			
			return new HelpBuildEnvVar(variableName, "/home" , IBuildEnvironmentVariable.ENVVAR_APPEND, "/");
		} else { 
			return null;
		}
		
		
		
	
		//return null;
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IManagedProject project,
			IEnvironmentVariableProvider provider) {
		// TODO Auto-generated method stub
		return null;
	}

}
