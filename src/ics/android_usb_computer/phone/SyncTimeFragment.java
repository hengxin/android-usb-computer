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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.os.AsyncTask;
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
	public static final int TIMEOUT = 60;

	private static final Executor exec = Executors.newCachedThreadPool();
	
	private ServerSocket server_socket = null;
	
	private Button btn_connect = null;

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
			// initialize server socket in background
			case R.id.btn_connect:
				new ServerTask().execute();
				Toast.makeText(getActivity(), "Port is open. Wait for time from PC.", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
		}
	}

	/**
	 * create server socket, listen to and accept messages
	 */
	public void getReadyForSync()
	{
		if (this.server_socket != null)
			return;
		
		// initialize server socket
		try
		{
			server_socket = new ServerSocket();
			server_socket.bind(new InetSocketAddress("localhost", ADBExecutor.ANDROID_PORT));
//			server_socket.setSoTimeout(TIMEOUT * 1000);

			// wait to accept connections
			while (true)
			{
				final Socket client_socket = server_socket.accept();
				
				// handle with the received connections (and messages) asynchronously
				Runnable receive_task = new Runnable()
				{
					@Override
					public void run()
					{
						ObjectInputStream ois = null;
						Message msg = null;
						
						try
						{
							while (true)
							{
								ois = new ObjectInputStream(client_socket.getInputStream());
								msg = (Message) ois.readObject();
								SyncTimeFragment.this.onReceive(msg);
							}
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
			} 
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		} finally
		{
			try
			{
				server_socket.close();
			} catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
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
	 * Release the server socket if it exists
	 */
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		Toast.makeText(getActivity(), "Close the network socket and exit.", Toast.LENGTH_SHORT).show();
		
		if (this.server_socket != null && ! this.server_socket.isClosed())
		{
			try
			{
				this.server_socket.close();
			} catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}
	
	/**
	 * establish socket to listen to requests in an asynchronous manner
	 * 
	 * create server socket, listen on port, and wait for coming connections;
	 * upon receiving connection, start a new thread to process it.
	 */
	public class ServerTask extends AsyncTask<String, Void, Void>
	{
		@Override
		protected Void doInBackground(String... params)
		{
			getReadyForSync();
			return null;
		}
	}
}
