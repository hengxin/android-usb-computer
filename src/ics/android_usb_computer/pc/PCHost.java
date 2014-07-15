/**
 * @author hengxin
 * @date Jun 21, 2014
 * @description pc host side:
 *   (1) port forwarding
 *   (2)
 */
package ics.android_usb_computer.pc;

import ics.android_usb_computer.message.AuthMsg;
import ics.android_usb_computer.message.Message;
import ics.android_usb_computer.message.RequestTimeMsg;
import ics.android_usb_computer.message.ResponseTimeMsg;
import ics.android_usb_computer.utils.socket.SocketUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
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

import android.util.Log;

public class PCHost
{
	private Executor exec = Executors.newFixedThreadPool(5);
	
	/**
	 * pairs of (device, hostport)
	 */
	private final Map<String, Integer> device_hostport_map;
	private final Map<String, Socket> device_hostsocket_map = new HashMap<>();
	
	/**
	 * constructor of {@link PCHost}:
	 * establish socket connections for each device
	 * 
	 * @param device_hostport_map {@link #device_hostport_map}: map of (device, port)
	 */
	public PCHost(Map<String, Integer> device_hostport_map)
	{
		this.device_hostport_map = device_hostport_map;
		this.createConnection();
	}
	
	/**
	 * establish connections for each device on a specified port on the host PC
	 * and store them in the {@link #device_hostsocket_map} for further use
	 */
	private void createConnection()
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
			this.device_hostsocket_map.put(device, device_hostsocket);
		}
	}
	
	private Message waitForRequestTimeMsg(final Socket host_socket)
	{
		RequestTimeMsg msg = null;
		
		try
		{
			final ObjectInputStream ois = new ObjectInputStream(host_socket.getInputStream());
			msg = (RequestTimeMsg) ois.readObject();
		} catch (StreamCorruptedException sce)
		{
			sce.printStackTrace();
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
		}
		
		return msg;
	}
	
	private void sendAuthMsg(final Socket host_socket)
	{
		SocketUtil.INSTANCE.sendMsg(new AuthMsg(), host_socket);
	}
	
	private void sendResponseTimeMsg(final Socket host_socket)
	{
		SocketUtil.INSTANCE.sendMsg(new ResponseTimeMsg(System.currentTimeMillis()), host_socket);
	}
	
	public void startTimePollingService()
	{
		Socket host_socket = null;
		for(Map.Entry<String, Socket> device_hostsocket : this.device_hostsocket_map.entrySet())
		{
			host_socket = device_hostsocket.getValue();
			exec.execute(new Alternate(host_socket));
		}
	}
	
	final class Alternate implements Runnable
	{
		final Socket host_socket;
		
		public Alternate(Socket host_socket)
		{
			this.host_socket = host_socket;
		}
		
		@Override
		public void run()
		{
			sendAuthMsg(host_socket);
			
			Message msg = null;
			while (true)
			{
				msg = waitForRequestTimeMsg(host_socket);
				System.out.println("Receiving RequestTimeMsg: " + msg.toString());
				
				if (msg.getType() == Message.REQUEST_TIME_MSG)
					sendResponseTimeMsg(host_socket);
			}
		}
	}
	
	/**
	 * Send {@link AuthMsg} to an Android device in a new thread
	 * @param host_socket send message to an Android device on the other side of this specified socket
	 */
	private void sendAuthMsgInNewThread(final Socket host_socket)
	{
//		exec.execute(new SendMsgTask(new AuthMsg(), host_socket));
		SocketUtil.INSTANCE.sendMsgInNewThread(new AuthMsg(), host_socket);
	}
	
	/**
	 * Send the current system time (denoted by {@link ResponseTimeMsg})
	 * to some device attached to the specified socket in a new thread
	 * @param host_socket the message is sent via this socket
	 */
	private void sendResponseTimeMsgInNewThread(final Socket host_socket)
	{
//		exec.execute(new SendMsgTask(new ResponseTimeMsg(System.currentTimeMillis()), host_socket));
		SocketUtil.INSTANCE.sendMsgInNewThread(new ResponseTimeMsg(System.currentTimeMillis()), host_socket);
	}
	
	public void waitForRequestTimeMsgInNewThread(final Socket host_socket)
	{
		class ReceiveTask implements Runnable
		{
			@Override
			public void run()
			{
				try
				{
					final ObjectInputStream ois = new ObjectInputStream(host_socket.getInputStream());
					ois.readObject();
				} catch (StreamCorruptedException sce)
				{
					sce.printStackTrace();
				} catch (IOException ioe)
				{
					ioe.printStackTrace();
				} catch (ClassNotFoundException cnfe)
				{
					cnfe.printStackTrace();
				}
			}
		}
		
		exec.execute(new ReceiveTask());
	}
	
	/**
	 * close all the host sockets
	 */
	public void shutDown()
	{
		for (Map.Entry<String, Socket> device_hostsocket : this.device_hostsocket_map.entrySet())
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
	 * Authorize time polling from all the attached Android devices
	 */
	private void publishAuth()
	{
		Socket host_socket = null;
		for(Map.Entry<String, Socket> device_hostsocket : this.device_hostsocket_map.entrySet())
		{
			host_socket = device_hostsocket.getValue();
			this.sendAuthMsgInNewThread(host_socket);
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
		for(Map.Entry<String, Socket> device_hostsocket : this.device_hostsocket_map.entrySet())
		{
			host_socket = device_hostsocket.getValue();
			this.sendResponseTimeMsgInNewThread(host_socket);
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

		host.startTimePollingService();
//		host.singleSync();
//		host.shutDown();
		
		// sync. every 5 seconds for one hour and a half
//		host.periodicalSync(5, 60 * 60 * 4);
//		host.shutDown();
	}

}
