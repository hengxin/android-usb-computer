/**
 * @author hengxin
 * @date Jun 25, 2014
 * @description Android + JUnit3; this does not work! 
 */
package ics.android_usb_computer.sync;

import ics.android_usb_computer.pc.ADBExecutor;
import ics.android_usb_computer.pc.PCHost;

import java.util.Map;

import junit.framework.TestCase;

public class SyncTimeTest extends TestCase
{
	ADBExecutor adb_executor = null;
	PCHost host = null;

	protected void setUp() throws Exception
	{
		this.adb_executor = new ADBExecutor("D:\\AndroidSDK\\platform-tools\\adb.exe ");
		Map<String, Integer> device_hostport_map = adb_executor.execAdbOnlineDevicesPortForward();

		this.host = new PCHost(device_hostport_map);
	}

	/**
	 * sync. every ten seconds for an hour
	 */
	public void testPeriodicalSync()
	{
		host.periodicalSync(10, 3600);
	}
}
