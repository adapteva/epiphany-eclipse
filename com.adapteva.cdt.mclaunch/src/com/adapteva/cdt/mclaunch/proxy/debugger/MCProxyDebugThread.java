package com.adapteva.cdt.mclaunch.proxy.debugger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

public class MCProxyDebugThread  implements IThread {

	
	MCProxyDebugTarget fMCProxyDebugTarget;
	
	
	public MCProxyDebugThread(MCProxyDebugTarget _fMCProxyDebugTarget) throws CoreException{
		fMCProxyDebugTarget = _fMCProxyDebugTarget;
	}

	@Override
	public boolean canStepInto() {
		try {
			return fMCProxyDebugTarget.anyCanStepInto();
		} catch (DebugException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean canStepOver() {
		try {
			return fMCProxyDebugTarget.anyCanStepOver();
		} catch (DebugException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean canStepReturn() {
		try {
			return fMCProxyDebugTarget.anyCanStepReturn();
		} catch (DebugException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isStepping() {
		try {
			return fMCProxyDebugTarget.anyIsSteping();
		} catch (DebugException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stepInto() throws DebugException {
		fMCProxyDebugTarget.stepIntoAll();
		
	}

	@Override
	public void stepOver() throws DebugException {
		fMCProxyDebugTarget.stepOverAll();
		
	}

	@Override
	public void stepReturn() throws DebugException {
		fMCProxyDebugTarget.stepReturnAll();	
	}

	@Override
	public IStackFrame[] getStackFrames() throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasStackFrames() throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getPriority() throws DebugException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IStackFrame getTopStackFrame() throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IBreakpoint[] getBreakpoints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getModelIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return fMCProxyDebugTarget;
	}

	@Override
	public ILaunch getLaunch() {
		return fMCProxyDebugTarget.getLaunch();
	}

	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canResume() {
		return false;// allow only in target
		//return fMCProxyDebugTarget.canResume();
	}

	@Override
	public boolean canSuspend() {
		return false;// allow only in target
		//return fMCProxyDebugTarget.canSuspend();
	}

	@Override
	public boolean isSuspended() {
		return fMCProxyDebugTarget.isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		fMCProxyDebugTarget.resume();
		
	}

	@Override
	public void suspend() throws DebugException {
		fMCProxyDebugTarget.suspend();
		
	}

	@Override
	public boolean canTerminate() {
		
		return fMCProxyDebugTarget.canTerminate();
	}

	@Override
	public boolean isTerminated() {
		
		return fMCProxyDebugTarget.isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
		fMCProxyDebugTarget.terminate();
		
	}

	@Override
	public String getName() throws DebugException {
		
		return "{StepInto} {StepOver} {StepReturn} All";
	}


}
