/**
 * @author hengxin
 * @date Jun 21, 2014
 * @description 
 */
package ics.android_usb_computer.phone;

import ics.android_usb_computer.R;
import ics.android_usb_computer.message.Message;
import ics.android_usb_computer.message.handler.MessageHandler;
import ics.android_usb_computer.message.handler.SyncTimeMsgHandler;
import ics.android_usb_computer.pc.ADBExecutor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class SyncTimeFragment extends Fragment implements OnClickListener 
{
	public static final String TAG = "Connection";
	
	// timeout in second
	public static final int TIMEOUT = 5;

	private final Executor exec = Executors.newCachedThreadPool();
	
	private Button btn_connect = null;
	private ServerSocket server_socket = null;

	/**
	 * default constructor of {@link SyncTimeFragment}
	 */
	public SyncTimeFragment()
	{
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(
				R.layout.fragment_communication_main, container, false);
		
		this.btn_connect = (Button) rootView.findViewById(R.id.btn_connect);
		this.btn_connect.setOnClickListener(this);
		
		return rootView;
	}

	/**
	 * handle with the click events on buttons.
	 */
	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			// initialize server socket in a new separate thread
			case R.id.btn_connect:
				new Thread(connection).start();
				Toast.makeText(getActivity(), "Attempting to connect.", Toast.LENGTH_LONG).show();
				break;
			default:
				break;
		}
	}

	/**
	 * receive messages and dispatch then to appropriate handlers
	 * @param msg received {@link Message}
	 */
	public void onReceive(Message msg)
	{
		MessageHandler msg_handler = null;
		
		switch (msg.getType())
		{
			case Message.SYNC_TIME_MSG:
				msg_handler = new SyncTimeMsgHandler(msg, getActivity());
				break;

			default:
				break;
		}
		
		msg_handler.handle();
	}
	
	/**
	 * create server socket, listen on port, and wait for coming connections;
	 * upon receiving connection, start a new thread to process it.
	 */
	private Runnable connection = new Thread()
	{
		public void run()
		{
			// initialize server socket
			try
			{
				server_socket = new ServerSocket(ADBExecutor.ANDROID_PORT);
				server_socket.setSoTimeout(TIMEOUT * 1000);
			} catch (SocketException se)
			{
				se.printStackTrace();
			} catch (IOException ioe)
			{
				ioe.printStackTrace();
			}

			// wait to accept connections
			while(true)
			{
				try
				{
					final Socket client_socket = server_socket.accept();
					
					// handle with the received connections (and messages) asynchronously
					Runnable receive_task = new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								ObjectInputStream ois = new ObjectInputStream(client_socket.getInputStream());
								Message msg = (Message) ois.readObject();
								SyncTimeFragment.this.onReceive(msg);
							} catch (StreamCorruptedException sce)
							{
								sce.printStackTrace();
							} catch (IOException ioe)
							{
								ioe.printStackTrace();
							} catch (ClassNotFoundException cnfe)
							{
								cnfe.printStackTrace();
							}
						}
					}; 
					exec.execute(receive_task);
					
				} catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
		}
	};	
	
}
