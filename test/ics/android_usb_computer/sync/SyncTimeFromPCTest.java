/**
 * @author hengxin
 * @date Jun 24, 2014
 * @description Android + JUnit4; this does not work! 
 */
package ics.android_usb_computer.sync;

import ics.android_usb_computer.pc.ADBExecutor;
import ics.android_usb_computer.pc.PCHost;

import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SyncTimeFromPCTest
{
	ADBExecutor adb_executor = null;
	PCHost host = null;

	/**
	 * discover the devices attached to the PC host;
	 */
	@Before
	public void discoverDevices()
	{
		this.adb_executor = new ADBExecutor("D:\\AndroidSDK\\platform-tools\\adb.exe ");
		Map<String, Integer> device_hostport_map = adb_executor.execAdbOnlineDevicesPortForward();

		this.host = new PCHost(device_hostport_map);
	}

	@Ignore("")
	@Test
	public void singleSync()
	{
		host.singleSync();
	}

	/**
	 * sync. every ten seconds for an hour
	 */
	@Test
	public void periodicalSync()
	{
		host.periodicalSync(10, 3600);
	}
}
