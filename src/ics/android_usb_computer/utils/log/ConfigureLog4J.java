/**
 * @author hengxin
 * @date Jun 23, 2014
 * @description to configure "android-logging-log4j"
 *  <url>https://code.google.com/p/android-logging-log4j/</url>
 */
package ics.android_usb_computer.utils.log;

import java.io.File;

import org.apache.log4j.Level;

import android.os.Environment;
import de.mindpipe.android.logging.log4j.LogConfigurator;

public enum ConfigureLog4J
{
	INSTANCE;
	
	private LogConfigurator log_config = null;
	
    public void configure() 
    {
    	if (this.log_config == null)
    	{
    		this.log_config = new LogConfigurator();
    		
	        String file_name = Environment.getExternalStorageDirectory() + File.separator + "/single_execution/sync_time.txt";
	        log_config.setFileName(file_name);
	        log_config.setRootLevel(Level.DEBUG);
	        log_config.setFilePattern("%m%n");
	        log_config.setImmediateFlush(true);
	        log_config.setUseLogCatAppender(false);
	        
	        // Set log level of a specific logger
	        log_config.setLevel("ics.android-usb-computer", Level.DEBUG);
	        
	        log_config.configure();
    	}
    }
}
