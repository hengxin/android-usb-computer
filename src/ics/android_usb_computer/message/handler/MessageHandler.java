/**
 * @author hengxin
 * @date Jun 21, 2014
 * @description handler of {@link Message}s
 */
package ics.android_usb_computer.message.handler;

import ics.android_usb_computer.message.Message;
import android.content.Context;

public abstract class MessageHandler
{
	// {@link Message} to handle with
	protected Message msg = null;
	// possible Context to use
	protected Context ctxt = null;
	
	/**
	 * constructor of {@link MessageHanlder}
	 * @param msg {@link Message} to handle with
	 */
	public MessageHandler(Message msg)
	{
		this.msg = msg;
	}
	
	/**
	 * constructor of {@link MessageHandler}
	 * @param msg {@link Message} to handle with
	 * @param ctxt possible Context to use
	 */
	public MessageHandler(Message msg, Context ctxt)
	{
		this.msg = msg;
		this.ctxt = ctxt;
	}
	
	/**
	 * handle with different types of {@link Message}s
	 */
	public abstract void handle();
}
