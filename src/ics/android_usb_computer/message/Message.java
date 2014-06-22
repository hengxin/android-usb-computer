/**
 * @author hengxin
 * @date Jun 21, 2014
 * @description Basic messages in communication; 
 * Each message has its type indicated by an Integer and payload of arbitrary class. 
 */
package ics.android_usb_computer.message;

import java.io.Serializable;

public abstract class Message implements Serializable
{
	private static final long serialVersionUID = -3130699534237155171L;

	public static final int SYNC_TIME_MSG = 0;
	
	// type of message
	protected int type = -1;
	// actual payload of message
	protected Object payload = null;
	
	/**
	 * constructor of {@link Message}
	 * @param type type of this message ( {@link #type} )
	 */
	public Message(int type)
	{
		this.type = type;
	}
	
	/**
	 * constructor of {@link Message}
	 * @param type type of this message ( {@linke #type} )
	 * @param payload actual payload of this message ( {@link #payload} )
	 */
	public Message(int type, Object payload)
	{
		this.type = type;
		this.payload = payload;
	}
	
	/**
	 * @return {@link #type}: type of the message
	 */
	public int getType()
	{
		return this.type;
	}
	
	/**
	 * @return {@link #payload}: actual payload of the message
	 */
	public Object getPayload()
	{
		return this.payload;
	}
}
