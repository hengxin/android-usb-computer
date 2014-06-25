/**
 * @author hengxin
 * @date Jun 25, 2014
 * @description Android + JUnit3; this does not work! 
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

//	public void testSingleSync()
//	{
//		host.broadcastMessage(new SyncTimeMsg(System.currentTimeMillis()));
//	}

	/**
	 * sync. every ten seconds for an hour
	 */
	public void testPeriodicalSync()
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
		final ScheduledFuture<?> sync_handler = sync_scheduler.scheduleAtFixedRate(sync, 0, 5, TimeUnit.SECONDS);
		
		// last for an hour
		sync_scheduler.schedule(new Runnable() { public void run() { sync_handler.cancel(true); } }, 
				60 * 60, TimeUnit.SECONDS);
	}
}
