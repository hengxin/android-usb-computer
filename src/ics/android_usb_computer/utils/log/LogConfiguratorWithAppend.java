/**
 * @author hengxin
 * @date Jun 24, 2014
 * @description 
 */
package ics.android_usb_computer.utils.log;

import java.io.IOException;

import org.apache.log4j.Layout;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.LogLog;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class LogConfiguratorWithAppend extends LogConfigurator
{
	private boolean append_flag = true;
	
	/**
	 * constructor of {@link LogConfiguratorWithAppend}
	 * @param append flag to indicate whether to overwrite the file or to append to it
	 */
	public LogConfiguratorWithAppend(boolean append)
	{
		this.append_flag = append;
	}
	
	/**
	 * configure the log with the flag indicating whether the file
	 * specified in a file appender (if it exists) is overwritten or is appended
	 */
	public void configure() 
	{
		final Logger root = Logger.getRootLogger();
		
		if(isResetConfiguration())
			LogManager.getLoggerRepository().resetConfiguration();

		LogLog.setInternalDebugging(isInternalDebugging());
		
		if(isUseFileAppender()) 
			configureFileAppenderWithAppend();
		
		// ignore the LogCatAppender
		
		root.setLevel(getRootLevel());
	}
	
	/**
	 * configure the file appender with the {@link #append_flag}
	 * indicating whether the file is overwritten or appended.
	 * 
	 * Notice: the configureFileAppender() is private in {@link LogConfigurator}.
	 * I have to re-implement it instead of overriding it.
	 */
	private void configureFileAppenderWithAppend() 
	{
		final Logger root = Logger.getRootLogger();
		final RollingFileAppender rollingFileAppender;
		final Layout fileLayout = new PatternLayout(getFilePattern());

		try 
		{
			rollingFileAppender = new RollingFileAppender(fileLayout, getFileName(), this.append_flag);
		} catch (final IOException ioe) 
		{
			throw new RuntimeException("Exception configuring log system", ioe);
		}

		rollingFileAppender.setMaxBackupIndex(getMaxBackupSize());
		rollingFileAppender.setMaximumFileSize(getMaxFileSize());
		rollingFileAppender.setImmediateFlush(isImmediateFlush());

		root.addAppender(rollingFileAppender);
	}
}
