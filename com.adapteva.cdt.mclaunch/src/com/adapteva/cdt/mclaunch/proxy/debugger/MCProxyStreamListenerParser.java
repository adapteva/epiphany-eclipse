package com.adapteva.cdt.mclaunch.proxy.debugger;


import java.io.IOException;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;

public class MCProxyStreamListenerParser implements IStreamListener {


	boolean fIsCout;

	MCProxyDebugTarget fMCproxyDebugger;


	MCProxyStreamListenerParser(MCProxyDebugTarget MCproxyDebugger, boolean _fIsCout) {
		fMCproxyDebugger = MCproxyDebugger;
		fIsCout = _fIsCout;
	}

	//TODO
	void targetCmdFailHandler() {

	}

	
	@Override
	public void streamAppended(String text, IStreamMonitor monitor) {

		if(fIsCout) {

			String textNoNewLine = text.substring(0, text.length()-1);//remove \n
			
			if(textNoNewLine.charAt(0) == '#')  {
				//Ignore comments
				return;
			}
			
			try {
				fMCproxyDebugger.sendToGdb(textNoNewLine);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	//used to test 
	public void streamAppendedTest(String text, IStreamMonitor monitor) {

		if(fIsCout) {

			String textNoNewLine = text.substring(0, text.length()-1);//remove \n
			
			if(textNoNewLine.charAt(0) == '#')  {
				//Ignore comments
				return;
			}

			if(textNoNewLine.equalsIgnoreCase("continue") || textNoNewLine.equalsIgnoreCase("c")) {
				try {
					fMCproxyDebugger.continueAll();
				} catch (DebugException e) {
					targetCmdFailHandler();
				}

			}  
			else if(textNoNewLine.equalsIgnoreCase("halt")  ) {
				try {
					fMCproxyDebugger.suspendAll();
				} catch (DebugException e) {
					targetCmdFailHandler();
				}

			}	

			else if(textNoNewLine.equalsIgnoreCase("si")|| textNoNewLine.equalsIgnoreCase("stepinto")) {
				try {
					fMCproxyDebugger.stepIntoAll();
				} catch (DebugException e) {
					targetCmdFailHandler();
				}

			}	
			else if(textNoNewLine.equalsIgnoreCase("so") || textNoNewLine.equalsIgnoreCase("stepover")) {
				try {
					fMCproxyDebugger.stepOverAll();
				} catch (DebugException e) {
					targetCmdFailHandler();
				}

			}	
			else if(textNoNewLine.equalsIgnoreCase("sr") || textNoNewLine.equalsIgnoreCase("stepreturn")) {
				try {
					fMCproxyDebugger.stepReturnAll();
				} catch (DebugException e) {
					targetCmdFailHandler();
				}

			}	
			else if(textNoNewLine.equalsIgnoreCase("kill") ) {
				try {
					fMCproxyDebugger.terminate();
				} catch (DebugException e) {
					targetCmdFailHandler();
				}

			}else {

				//check if it gdb command
				String[]  gdbCmd = textNoNewLine.split(MCProxyDebugConstants.MC_PROXY_GDB_DELIMETER);
				if(gdbCmd.length > 1 ) {
					if(gdbCmd[0].equalsIgnoreCase("gdb")) {
						try {
							fMCproxyDebugger.sendToGdb(gdbCmd[1]);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} else {
						try {
							fMCproxyDebugger.logCmds(">Unknown command: " + text );
						} catch (IOException e) {
							targetCmdFailHandler();
						}

					}
				}


			}

			try {
				if(textNoNewLine.equalsIgnoreCase("kill") == false) {
					fMCproxyDebugger.logCmds(MCProxyDebugConstants.MC_PROXY_OK_RESPONSE + "\n");//OK
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
