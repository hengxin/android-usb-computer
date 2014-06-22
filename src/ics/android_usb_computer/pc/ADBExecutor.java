/**
 * @author hengxin
 * @date Jun 21, 2014
 * @description executor of ADB commands
 */
package ics.android_usb_computer.pc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ADBExecutor
{
	public static final int ANDROID_PORT = 30000;
	public static final int HOST_BASE_PORT = 30000;
	
	private final String adb_directory;
	
	public ADBExecutor(String adb_directory)
	{
		this.adb_directory = adb_directory;
	}
	
	/**
	 * execute the adb command to get device ids: adb devices 
	 */
	public List<String> execAdbDevices()
	{
		List<String> ret_device_id_list = new ArrayList<>();
		
		Process proc = null;
		try
		{
			proc = new ProcessBuilder(this.adb_directory, "devices").start();
			proc.waitFor();
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		} catch (InterruptedException ire)
		{
			ire.printStackTrace();
		}
		
		String devices_result = this.collectResultFromProcess(proc);
	    String[] device_id_list = devices_result.split("\\r?\\n");

	    if (device_id_list.length <= 1)
	    {
	    	System.out.println("No Devices Attached.");
	    	return ret_device_id_list;
	    }
	    
	    /**
	     * collect the online devices 
	     */
	    String str_device_id = null;
	    String[] str_device_id_parts = null;
	    // ignore the first line which is "List of devices attached"
	    for (int i = 1; i < device_id_list.length; i++)
		{
			str_device_id = device_id_list[i];
			str_device_id_parts = str_device_id.split("\\s+");
			// add the online device
			if (str_device_id_parts[1].equals("device"))
				ret_device_id_list.add(str_device_id_parts[0]);
		}
	    
	    return ret_device_id_list;
	}
	
	/**
	 * Runs the android debug bridge command of forwarding the ports
	 */
	public void execAdbPortForward()
	{
		Process proc = null;
		try
		{
			proc = new ProcessBuilder(this.adb_directory, "forward", "tcp:" + ADBExecutor.HOST_BASE_PORT, "tcp:" + ADBExecutor.ANDROID_PORT).start();
			proc.waitFor();
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		} catch (InterruptedException ire)
		{
			ire.printStackTrace();
		}
		
		System.err.println(this.collectResultFromProcess(proc));
	}
	
	/**
	 * execute the "adb -s <device> forward" commands for each device with different ports on the host PC
	 * @return map of (device, hostport)
	 */
	public Map<String, Integer> execAdbOnlineDevicesPortForward()
	{
		List<String> device_id_list = this.execAdbDevices();
		Map<String, Integer> device_hostport_map = new HashMap<String, Integer>();
		
		int index = 0;
		for (String device : device_id_list)
		{
			int host_port = ADBExecutor.HOST_BASE_PORT + index * 10;
			this.execAdbSingleDevicePortForward(device, host_port, ADBExecutor.ANDROID_PORT);
			device_hostport_map.put(device, host_port);
			index++;
		}
		return device_hostport_map;
	}
	
	/**
	 * forwarding the @param host_port of PC to the @param to_part of the device (e.g., an Android phone)
	 * @param device_id forwarding a port to which device
	 * @param host_port port of PC
	 * @param to_port port of device (e.g., an Android phone)
	 */
	public void execAdbSingleDevicePortForward(String device_id, int host_port, int to_port)
	{
		System.out.println("adb -s " + device_id + " forward tcp:" + host_port + " tcp:" + to_port);
		
		Process proc = null;
		try
		{
			proc = new ProcessBuilder(this.adb_directory, "-s", device_id, "forward", "tcp:" + host_port, "tcp:" + to_port).start();
			proc.waitFor();
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		} catch (InterruptedException ire)
		{
			ire.printStackTrace();
		}
	}
	
	/**
	 * collect results (including error info.) from the process executing ADB command
	 * @param proc the process executing ADB command
	 * @return results collected from the process executing ADB command
	 */
	private String collectResultFromProcess(Process proc)
	{
		StringBuilder sb_result = new StringBuilder();
		
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        String result_line = null;
        
        try
        {
	        while ((result_line = stdInput.readLine()) != null) 
	        {
	            sb_result.append(result_line);
	            sb_result.append("\n");
	        }
	        
	        while ((result_line = stdError.readLine()) != null) 
	        {
	            sb_result.append(result_line);
	            sb_result.append("\n");
	        }
        } catch(IOException ioe)
        {
        	ioe.printStackTrace();
        }
        
        return sb_result.toString();
	}
	
	public static void main(String[] args)
	{
		ADBExecutor adb_executor = new ADBExecutor("D:\\AndroidSDK\\platform-tools\\adb.exe ");
//		adb_executor.execAdbPortForward();
		List<String> device_id_list = adb_executor.execAdbDevices();
		for (String device_id : device_id_list)
			System.out.println(device_id);
		adb_executor.execAdbOnlineDevicesPortForward();
	}
}
