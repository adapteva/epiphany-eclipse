package atdsp.com.atdsp.debug;

import java.util.Collection;

import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagDeviceImpl;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice;

public class AtdspGdbSimulator  extends DefaultGDBJtagDeviceImpl implements
IGDBJtagDevice {


	public void doRemote(String ip, int port, Collection<String> commands) {
		String cmd = "target sim"; //$NON-NLS-1$ //$NON-NLS-2$
		
		addCmd(commands, cmd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doDelay(int, java.util.Collection)
	 */
	public void doDelay(int delay, Collection<String> commands) {
	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doReset(java.util.Collection)
	 */
	public void doReset(Collection<String> commands) {
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doHalt(java.util.Collection)
	 */
	public void doHalt(Collection<String> commands) {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doStopAt(java.lang.String, java.util.Collection)
	 */
	public void doStopAt(String stopAt, Collection<String> commands) {
		String cmd = "break " + stopAt; //$NON-NLS-1$
		addCmd(commands, cmd);
	}
}
