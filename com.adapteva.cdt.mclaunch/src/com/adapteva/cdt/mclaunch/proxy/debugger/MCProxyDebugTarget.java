/**
 * 
 */
package com.adapteva.cdt.mclaunch.proxy.debugger;

import java.io.IOException;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.IThread;


/**
 * @author oraikhman
 *
 */
public class MCProxyDebugTarget implements IDebugTarget {

	// associated system process (VM)
	private IProcess fProcess;

	// containing launch object
	private ILaunch fLaunch;

	// program name
	private String fName;


	// threads
	private MCProxyDebugThread fThread;
	private IThread[] fThreads;


	//single Core Launches
	ILaunch[] fLaunches;


	public MCProxyDebugTarget(ILaunch launch, IProcess process,ILaunch[] launches) throws CoreException  {

		//		super(null);

		fLaunch = launch;
		//		fTarget = this;
		fProcess = process;


		fLaunches = launches;

		fThread = new MCProxyDebugThread(this);
		fThreads = new IThread[] {fThread};

		IStreamsProxy strProxy =fProcess.getStreamsProxy();

		IStreamMonitor errMonitor = strProxy.getErrorStreamMonitor();

		IStreamMonitor coutMonitor = strProxy.getOutputStreamMonitor();

		try {
			strProxy.write(MCProxyDebugConstants.MC_PROXY_COMMANDS +"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		errMonitor.addListener(new MCProxyStreamListenerParser(this,false));
		coutMonitor.addListener(new MCProxyStreamListenerParser(this,true));
	}



	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	@Override
	public boolean canTerminate() {

		//		for( int i = 0; i < fLaunches.length; i++ ) {
		//
		//			ILaunch cLaunch = fLaunches[i];
		//			if(cLaunch.canTerminate()) {
		//				return true;
		//			}
		//		}
		//		
		//		return false;		
		return getProcess().canTerminate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	@Override
	public boolean isTerminated() {

		//		for( int i = 0; i < fLaunches.length; i++ ) {
		//
		//			ILaunch cLaunch = fLaunches[i];
		//			if(!cLaunch.isTerminated()) {
		//				return false;
		//			}
		//		}
		//		
		//		return true;

		return getProcess().isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	@Override
	public void terminate() throws DebugException {

		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();


		for( int i = 0; i < fLaunches.length; i++ ) {

			ILaunch cLaunch = fLaunches[i];
			if(!cLaunch.isTerminated()) {
				if(cLaunch.canTerminate()) {
					cLaunch.terminate();
				}
			}

			manager.removeLaunch(cLaunch);
		}


		getProcess().terminate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	@Override
	public boolean canResume() {

		if(isTerminated()) {
			return false;
		}

		for( int i = 0; i < fLaunches.length; i++ ) {

			ILaunch cLaunch = fLaunches[i];

			IDebugTarget primaryDebugTarget = cLaunch.getDebugTarget();

			if(primaryDebugTarget!= null) {
				if(primaryDebugTarget.canResume()) {
					return true;
				}
			}

		}

		//not found any core, that can be resumed
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	@Override
	public boolean canSuspend() {
		if(isTerminated()) {
			return false;
		}

		for( int i = 0; i < fLaunches.length; i++ ) {

			ILaunch cLaunch = fLaunches[i];

			IDebugTarget primaryDebugTarget = cLaunch.getDebugTarget();

			if(primaryDebugTarget!= null) {
				if(primaryDebugTarget.canSuspend()) {
					return true;
				}
			}

		}

		//not found any core, that can be suspended
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	@Override
	public boolean isSuspended() {
		return canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	@Override
	public void resume() throws DebugException {

		this.continueAll();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	@Override
	public void suspend() throws DebugException {

		this.suspendAll();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse.debug.core.model.IBreakpoint)
	 */
	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#canDisconnect()
	 */
	@Override
	public boolean canDisconnect() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#disconnect()
	 */
	@Override
	public void disconnect() throws DebugException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#isDisconnected()
	 */
	@Override
	public boolean isDisconnected() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
	 */
	@Override
	public boolean supportsStorageRetrieval() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long, long)
	 */
	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length)
	throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getProcess()
	 */
	@Override
	public IProcess getProcess() {
		return fProcess;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getThreads()
	 */
	@Override
	public IThread[] getThreads() throws DebugException {
		return fThreads;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#hasThreads()
	 */
	@Override
	public boolean hasThreads() throws DebugException {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getName()
	 */
	@Override
	public String getName() throws DebugException {
		return "{Resume} {Suspend} {Terminate} All";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#supportsBreakpoint(org.eclipse.debug.core.model.IBreakpoint)
	 */
	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public String getModelIdentifier() {
		return "com.adapteva.cdt.mclaunch.debugModelPresentation";
	}


	@Override
	public IDebugTarget getDebugTarget() {

		return this;
	}


	@Override
	public ILaunch getLaunch() {
		return fLaunch;
	}


	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	boolean anyCanStepInto() throws DebugException  {
		for( int i = 0; i < fLaunches.length; i++ ) {

			ILaunch cLaunch = fLaunches[i];
			IDebugTarget primaryDebugTarget = cLaunch.getDebugTarget();

			if(primaryDebugTarget!= null) {
				IThread [] threads = primaryDebugTarget.getThreads();
				for( int j = 0; j < threads.length; j++ ) {
					IThread thread = threads[j];

					if(thread.canStepInto()) {
						return true;
					}

				}
			}
		}
		return false;
	}
	
	void stepIntoAll() throws DebugException  {
		for( int i = 0; i < fLaunches.length; i++ ) {

			ILaunch cLaunch = fLaunches[i];
			IDebugTarget primaryDebugTarget = cLaunch.getDebugTarget();

			if(primaryDebugTarget!= null) {
				IThread [] threads = primaryDebugTarget.getThreads();
				for( int j = 0; j < threads.length; j++ ) {
					IThread thread = threads[j];

					if(thread.canStepInto()) {
						thread.stepInto();
					}

				}
			}
		}
	}

	boolean anyCanStepOver() throws DebugException  {
		for( int i = 0; i < fLaunches.length; i++ ) {

			ILaunch cLaunch = fLaunches[i];
			IDebugTarget primaryDebugTarget = cLaunch.getDebugTarget();

			if(primaryDebugTarget!= null) {
				IThread [] threads = primaryDebugTarget.getThreads();
				for( int j = 0; j < threads.length; j++ ) {
					IThread thread = threads[j];

					if(thread.canStepOver()) {
						return true;
					}

				}
			}
		}
		return false;
	}
	
	
	void stepOverAll() throws DebugException  {
		for( int i = 0; i < fLaunches.length; i++ ) {

			ILaunch cLaunch = fLaunches[i];
			IDebugTarget primaryDebugTarget = cLaunch.getDebugTarget();

			if(primaryDebugTarget!= null) {
				IThread [] threads = primaryDebugTarget.getThreads();
				for( int j = 0; j < threads.length; j++ ) {
					IThread thread = threads[j];

					if(thread.canStepOver()) {
						thread.stepOver();
					}

				}
			}
		}

	}
	
	boolean anyCanStepReturn() throws DebugException  {
		for( int i = 0; i < fLaunches.length; i++ ) {

			ILaunch cLaunch = fLaunches[i];
			IDebugTarget primaryDebugTarget = cLaunch.getDebugTarget();

			if(primaryDebugTarget!= null) {
				IThread [] threads = primaryDebugTarget.getThreads();
				for( int j = 0; j < threads.length; j++ ) {
					IThread thread = threads[j];

					if(thread.canStepReturn()) {
						return true;
					}

				}
			}
		}
		return false;
		
		
	}
	
	void stepReturnAll() throws DebugException  {
		for( int i = 0; i < fLaunches.length; i++ ) {

			ILaunch cLaunch = fLaunches[i];
			IDebugTarget primaryDebugTarget = cLaunch.getDebugTarget();

			if(primaryDebugTarget!= null) {
				IThread [] threads = primaryDebugTarget.getThreads();
				for( int j = 0; j < threads.length; j++ ) {
					IThread thread = threads[j];

					if(thread.canStepReturn()) {
						thread.stepReturn();
					}

				}
			}
		}

	}
	
	
	boolean anyIsSteping() throws DebugException {
		
		for( int i = 0; i < fLaunches.length; i++ ) {

			ILaunch cLaunch = fLaunches[i];
			IDebugTarget primaryDebugTarget = cLaunch.getDebugTarget();

			if(primaryDebugTarget!= null) {
				IThread [] threads = primaryDebugTarget.getThreads();
				for( int j = 0; j < threads.length; j++ ) {
					IThread thread = threads[j];

					if(thread.isStepping()) {
						return true;
					}

				}
			}
		}
		return false;	

	}

	void suspendAll() throws DebugException  {

		for( int i = 0; i < fLaunches.length; i++ ) {

			ILaunch cLaunch = fLaunches[i];

			IDebugTarget primaryDebugTarget = cLaunch.getDebugTarget();

			if(primaryDebugTarget!= null) {
				if(primaryDebugTarget.canSuspend()) {
					primaryDebugTarget.suspend();
				}
			}

		}

	}

	void continueAll() throws DebugException {

		for( int i = 0; i < fLaunches.length; i++ ) {

			ILaunch cLaunch = fLaunches[i];

			IDebugTarget primaryDebugTarget = cLaunch.getDebugTarget();

			if(primaryDebugTarget!= null) {
				if(primaryDebugTarget.canResume()) {
					primaryDebugTarget.resume();
				}
			}

		}

	}
	
	
	void logCmds(String errorStr) throws IOException {
		fProcess.getStreamsProxy().write(errorStr);
	}



	public void sendToGdb(String gdbCmdString) throws IOException {
		// TODO Auto-generated method stub
		
		for( int i = 0; i < fLaunches.length; i++ ) {

			ILaunch cLaunch = fLaunches[i];
			
			
			
			IDebugTarget primaryDebugTarget = cLaunch.getDebugTarget();

			if(primaryDebugTarget!= null) {
				if(primaryDebugTarget.isTerminated()) {
					continue;
				}
				if(primaryDebugTarget.isSuspended() != true) {//otherwise the gdb MI interface will be broken
					continue;
				}
				
			}
			
			IProcess[] processes = cLaunch.getProcesses();
			for( int j = 0; j < processes.length; j++ ) {
				IProcess gdbProcess = processes[j];
				if(gdbProcess.isTerminated()) {
					continue;
				} 
				gdbProcess.getStreamsProxy().write(gdbCmdString + "\n");
			}

		}
		
	}

}
