package com.adapteva.cdt.epiphany.project;

import org.eclipse.cdt.managedbuilder.core.IBuildPathResolver;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;

public class EpiphanyPathResolver implements IBuildPathResolver {

	@Override
	public String[] resolveBuildPaths(int pathType, String variableName,
			String variableValue, IConfiguration configuration) {
		// TODO Auto-generated method stub
		
		//String[] oneDimArray = { "abc","def","xyz" };
		//return oneDimArray;
		
		return null;
	}

}
