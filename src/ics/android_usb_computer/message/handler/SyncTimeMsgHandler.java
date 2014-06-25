/**
 * @author hengxin
 * @date Jun 21, 2014
 * @description message handler of {@link SyncTimeMsg}
 */
package ics.android_usb_computer.message.handler;

import ics.android_usb_computer.message.Message;
import ics.android_usb_computer.message.SyncTimeMsg;
import ics.android_usb_computer.utils.ConfigureLog4J;

import org.apache.log4j.Logger;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public class SyncTimeMsgHandler extends MessageHandler
{
	private final Logger log4android = Logger.getLogger(SyncTimeMsgHandler.class);
	
	/**
	 * constructor of {@link SyncTimeMsgHandler}
	 * @param sync_time_msg message of type {@link SyncTimeMsg} to handle with
	 */
	public SyncTimeMsgHandler(Message sync_time_msg)
	{
		super(sync_time_msg);
	}
	
	/**
	 * constructor of {@link SyncTimeMsgHandler}
	 * @param sync_time_msg message of type {@link SyncTimeMsg} to handle with
	 * @param ctxt possible {@link Context} to use
	 */
	public SyncTimeMsgHandler(Message sync_time_msg, Context ctxt)
	{
		super(sync_time_msg, ctxt);
	}
	
	/**
	 * handle with the messages of type {@link SyncTimeMsg}
	 */
	@Override
	public void handle()
	{
		long android_time = System.currentTimeMillis();
		long pc_time = ((SyncTimeMsg) super.msg).getSyncTime();
		
		final long diff = android_time - pc_time;
		
		// show the diff in Toast
		((FragmentActivity) this.ctxt).runOnUiThread(new Runnable() 
		{
            @Override
            public void run() 
            {
            	Toast.makeText(ctxt, String.valueOf(diff), Toast.LENGTH_LONG).show();
            }
		});
		
		this.log2ExternalStorage(diff, pc_time, android_time);
	}

	/**
	 * log the sync time info. into external storage for further retrieve
	 * @param diff @param android_time - @param pc_time
	 * @param pc_time received time of PC
	 * @param android_time current system time of Android
	 */
	private void log2ExternalStorage(long diff, long pc_time, long android_time)
	{
		ConfigureLog4J.INSTANCE.configure();
		
		StringBuilder sb = new StringBuilder();
		sb.append("diff ").append(diff).append('\n')
			.append("pc_time ").append(pc_time).append('\n')
			.append("android_time ").append(android_time);
		
		log4android.debug(sb.toString());
	}
}
