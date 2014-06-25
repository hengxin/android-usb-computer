/**
 * @author hengxin
 * @date Jun 24, 2014
 * @description Android + JUnit4; this does not work! 
 */
package ics.android_usb_computer.sync;

import ics.android_usb_computer.message.SyncTimeMsg;
import ics.android_usb_computer.pc.ADBExecutor;
import ics.android_usb_computer.pc.PCHost;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
		host.broadcastMessage(new SyncTimeMsg(System.currentTimeMillis()));
	}

	/**
	 * sync. every ten seconds for an hour
	 */
	@Test
	public void periodicalSync()
	{
		ScheduledExecutorService sync_scheduler = Executors.newScheduledThreadPool(1);

		// define the sync. task
		final Runnable sync = new Runnable() 
		{
			public void run() 
			{ 
				long time = System.currentTimeMillis();
				host.broadcastMessage(new SyncTimeMsg(time));
				System.out.println(time);
			}
		};

		// sync. every ten seconds
		final ScheduledFuture<?> sync_handler = sync_scheduler.scheduleAtFixedRate(sync, 0, 10, TimeUnit.SECONDS);
		
		// last for an hour
		sync_scheduler.schedule(new Runnable() { public void run() { sync_handler.cancel(true); } }, 
				60 * 60, TimeUnit.SECONDS);
	}
}
