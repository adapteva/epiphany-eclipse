package atdsp.com.atdsp.debug;

import java.util.Collection;

import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagDeviceImpl;

import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice;

public class AtdspGdbServer extends DefaultGDBJtagDeviceImpl implements
		IGDBJtagDevice {
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#getDefaultIpAddress()
	 */
	public String getDefaultIpAddress() {
		return "localhost"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doStopAt(java.lang.String, java.util.Collection)
	 */
	public void doStopAt(String stopAt, Collection<String> commands) {
		String cmd = "break " + stopAt; //$NON-NLS-1$
		addCmd(commands, cmd);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doRemote(java.lang.String, int, java.util.Collection)
	 */
	public void doRemote(String ip, int port, Collection<String> commands) {
		String cmd = "target remote " + ip + ":" + String.valueOf(port); //$NON-NLS-1$ //$NON-NLS-2$
		addCmd(commands, cmd);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doDelay(int, java.util.Collection)
	 */
	public void doDelay(int delay, Collection<String> commands) {
	
	}
	
	public void doHalt(Collection<String> commands) {
	//	String cmd = "monitor halt"; //$NON-NLS-1$
	//	addCmd(commands, cmd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#getDefaultPortNumber()
	 */
	public String getDefaultPortNumber() {
		
		
		
		return "51000"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doReset(java.util.Collection)
	 */
	public void doReset(Collection<String> commands) {
		//String cmd = "monitor reset"; //$NON-NLS-1$
		//addCmd(commands, cmd);
	}
}
