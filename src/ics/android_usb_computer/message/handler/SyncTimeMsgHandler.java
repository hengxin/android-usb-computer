/**
 * @author hengxin
 * @date Jun 21, 2014
 * @description message handler of {@link SyncTimeMsg}
 */
package ics.android_usb_computer.message.handler;

import ics.android_usb_computer.message.Message;
import ics.android_usb_computer.message.SyncTimeMsg;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public class SyncTimeMsgHandler extends MessageHandler
{
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
		long cur_time = System.currentTimeMillis();
		long time = ((SyncTimeMsg) super.msg).getSyncTime();
		
		final long diff = cur_time - time;
		
		((FragmentActivity) this.ctxt).runOnUiThread(new Runnable() 
		{
            @Override
            public void run() 
            {
            	Toast.makeText(ctxt, String.valueOf(diff), Toast.LENGTH_LONG).show();
            }
		});
	}

}
