package com.adapteva.cdt.epiphany.project;

import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;




public class HelpBuildEnvVar extends EnvironmentVariable implements IBuildEnvironmentVariable {
	public HelpBuildEnvVar(String name, String value, int op, String delimiter){
		super(name, value, op, delimiter);
	}
	
	protected HelpBuildEnvVar(){
		
	}
	
	public HelpBuildEnvVar(String name){
		super(name);
	}
	
	public HelpBuildEnvVar(String name, String value){
		super(name, value);	
	}

	public HelpBuildEnvVar(String name, String value, String delimiter){
		super(name, value, delimiter);	
	}
	
	public HelpBuildEnvVar(IEnvironmentVariable var){
		super(var);	
	}
}
