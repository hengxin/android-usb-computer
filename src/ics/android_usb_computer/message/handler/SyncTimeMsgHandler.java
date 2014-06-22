/**
 * @author hengxin
 * @date Jun 21, 2014
 * @description message handler of {@link SyncTimeMsg}
 */
package ics.android_usb_computer.message.handler;

import ics.android_usb_computer.message.Message;
import ics.android_usb_computer.message.SyncTimeMsg;
import android.app.AlarmManager;
import android.content.Context;
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
		SyncTimeMsg sync_time_msg = (SyncTimeMsg) super.msg;
		long time = sync_time_msg.getSyncTime();
		
		
		AlarmManager am = (AlarmManager) this.ctxt.getSystemService(Context.ALARM_SERVICE);
		Toast.makeText(this.ctxt, String.valueOf(time), Toast.LENGTH_LONG).show();
		
		am.setTime(time);
	}

}
