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


public class PCHost
{
	private Executor exec = Executors.newCachedThreadPool();
	
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
	 * broadcast a message to every device
	 * @param msg {@link Message} to send
	 */
	public void broadcastMessage(final Message msg)
	{
		class SendTask implements Runnable
		{
			// send the message to this device
			private String device;

			/**
			 * constructor of {@link SendTask} with specified device
			 * @param device #device
			 */
			public SendTask(String device)
			{
				this.device = device;
			}
			
			/**
			 * retrieve the socket for the specified device
			 * and send the message to it
			 */
			@Override
			public void run()
			{
				// get the socket for this device ( {@link #device} )
				Socket host_socket = PCHost.this.device_hostsockect_map.get(this.device);
				ObjectOutputStream oos;
				try
				{
					oos = new ObjectOutputStream(host_socket.getOutputStream());
					oos.writeObject(msg);
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

		// send the message to every device
		for (String device : this.device_hostsockect_map.keySet())
			this.exec.execute(new SendTask(device));
	}
	
	public void close()
	{

	}
	
	public static void main(String[] args)
	{
		ADBExecutor adb_executor = new ADBExecutor("D:\\AndroidSDK\\platform-tools\\adb.exe ");
		Map<String, Integer> device_hostport_map = adb_executor.execAdbOnlineDevicesPortForward();
		PCHost host = new PCHost(device_hostport_map);
		host.broadcastMessage(new SyncTimeMsg(System.currentTimeMillis()));
	}

}
