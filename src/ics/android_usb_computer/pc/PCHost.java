/**
 * @author hengxin
 * @date Jun 21, 2014
 * @description pc host side:
 *   (1) port forwarding
 *   (2)
 */
package ics.android_usb_computer.pc;

import ics.android_usb_computer.message.Message;
import ics.android_usb_computer.message.SyncTimeMsg;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class PCHost
{
	private Executor exec = Executors.newFixedThreadPool(5);
	
	/**
	 * pairs of (device, hostport)
	 */
	private Map<String, Integer> device_hostport_map = new HashMap<>();
	private Map<String, Socket> device_hostsockect_map = new HashMap<>();
	
	/**
	 * constructor of {@link PCHost}:
	 * establish socket connections for each device
	 * 
	 * @param device_hostport_map2 {@link #device_hostport_map}: map of (device, port)
	 */
	public PCHost(Map<String, Integer> device_hostport_map2)
	{
		this.device_hostport_map = device_hostport_map2;
		this.establishConnection();
	}
	
	/**
	 * establish connections for each device on a specified port on the host PC
	 * and store them in the {@link #device_hostsockect_map} for further use
	 */
	private void establishConnection()
	{
		String device = null;
		int host_port = -1;
		
		for(Map.Entry<String, Integer> device_hostport : this.device_hostport_map.entrySet())
		{
			device = device_hostport.getKey();
			host_port = device_hostport.getValue();
			
			// create socket for each "device" on the "host_port" of host PC
			Socket device_hostsocket = null;
			try
			{
				device_hostsocket = new Socket("localhost", host_port);
			} catch (UnknownHostException uhe)
			{
				uhe.printStackTrace();
			} catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
			
			// store the sockets created for each "device"
			this.device_hostsockect_map.put(device, device_hostsocket);
		}
	}
	
	/**
	 * send the current system time (denoted by {@link SyncTimeMsg})
	 * to some device attached to the specified socket
	 * @param host_socket the message is sent via this socket
	 */
	private void sendCurrentTimeViaUSB(final Socket host_socket)
	{
		class SendTask implements Runnable
		{
			// start a new thread to send this message via a specified socket
			@Override
			public void run()
			{
				try
				{
					ObjectOutputStream oos = new ObjectOutputStream(host_socket.getOutputStream());
					SyncTimeMsg sync_time_msg = new SyncTimeMsg(System.currentTimeMillis());
					oos.writeObject(sync_time_msg);
					oos.flush();
				} catch (SocketTimeoutException stoe)
				{
					stoe.printStackTrace();
				} catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
			
		}
		
		exec.execute(new SendTask());
	}
	
	/**
	 * close all the host sockets
	 */
	public void shutDown()
	{
		for (Map.Entry<String, Socket> device_hostsocket : this.device_hostsockect_map.entrySet())
		{
			try
			{
				device_hostsocket.getValue().close();
			} catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}
	
	/**
	 * sync. time once
	 * 
	 * For each attached device via USB,
	 * send it the current system time of the PC host.
	 */
	public void singleSync()
	{
		Socket host_socket = null;
		for(Map.Entry<String, Socket> device_hostsocket : this.device_hostsockect_map.entrySet())
		{
			host_socket = device_hostsocket.getValue();
			this.sendCurrentTimeViaUSB(host_socket);
		}
	}
	
	/**
	 * sync. time periodically
	 * @param period   the period between successive executions (in seconds)
	 * @param last_time duration in seconds
	 */
	public void periodicalSync(long period, long last_time)
	{
		ScheduledExecutorService sync_scheduler = Executors.newScheduledThreadPool(1);

		// define the sync. task
		final Runnable sync = new Runnable() 
		{
			public void run() 
			{ 
				singleSync();
			}
		};

		// sync. every @param delay seconds
		final ScheduledFuture<?> sync_handler = sync_scheduler.scheduleAtFixedRate(sync, 0, period, TimeUnit.SECONDS);
		
		// last for @param last_time seconds
		sync_scheduler.schedule(new Runnable() { public void run() { sync_handler.cancel(true); } }, 
				last_time, TimeUnit.SECONDS);
	}
	
	public static void main(String[] args) throws InterruptedException
	{
		ADBExecutor adb_executor = new ADBExecutor("D:\\AndroidSDK\\platform-tools\\adb.exe ");
		Map<String, Integer> device_hostport_map = adb_executor.execAdbOnlineDevicesPortForward();
		final PCHost host = new PCHost(device_hostport_map);

		host.singleSync();
//		host.shutDown();
		
		// sync. every 5 seconds for one hour and a half
//		host.periodicalSync(5, 5400);
//		host.shutDown();
	}

}
