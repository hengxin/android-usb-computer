/**
 * @author hengxin
 * @date Jun 21, 2014
 * @description message for sync time
 *   type: Message.SYNC_TIME_MSG
 *   payload: time in millisecond (long Class type)
 */
package ics.android_usb_computer.message;

public class SyncTimeMsg extends Message
{
	private static final long serialVersionUID = 1863422101310415237L;

	/**
	 * default constructor of {@link SyncTimeMsg}
	 */
	public SyncTimeMsg()
	{
		super(Message.SYNC_TIME_MSG);
	}
	
	/**
	 * constructor of {@link SyncTimeMsg}
	 * @param time actual payload of {@link SyncTimeMsg}: time in millisecond
	 */
	public SyncTimeMsg(long time)
	{
		super(Message.SYNC_TIME_MSG);
		super.payload = time;
	}
	
	/**
	 * @return actual payload of {@link SyncTimeMsg}: time in millisecond
	 */
	public long getSyncTime()
	{
		return (long) super.payload;
	}
	
	/**
	 * @Override
	 */
	public String toString()
	{
		return "SYNC_TIME_MSG: " + payload;
	}
}
