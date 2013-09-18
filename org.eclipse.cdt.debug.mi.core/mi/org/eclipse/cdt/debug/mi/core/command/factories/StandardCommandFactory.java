/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories; 

import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
 
/**
 * The "standard" command factory.
 */
public class StandardCommandFactory extends CommandFactory {

	/** 
	 * Constructor for StandardCommandFactory. 
	 */
	public StandardCommandFactory() {
		super();
	}

	/** 
	 * Constructor for StandardCommandFactory. 
	 */
	public StandardCommandFactory( String miVersion ) {
		super( miVersion );
	}
}
