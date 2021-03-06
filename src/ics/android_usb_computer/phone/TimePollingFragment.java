/**
 * @author hengxin
 * @date Jun 21, 2014
 * @description 
 */
package ics.android_usb_computer.phone;

import ics.android_usb_computer.R;
import ics.android_usb_computer.message.AuthMsg;
import ics.android_usb_computer.message.Message;
import ics.android_usb_computer.message.RequestTimeMsg;
import ics.android_usb_computer.message.ResponseTimeMsg;
import ics.android_usb_computer.message.handler.MessageHandler;
import ics.android_usb_computer.message.handler.SyncTimeMsgHandler;
import ics.android_usb_computer.pc.ADBExecutor;
import ics.android_usb_computer.utils.socket.SocketUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class TimePollingFragment extends Fragment implements OnClickListener 
{
	private static final String TAG = TimePollingFragment.class.getName();
	
	// timeout in second
	public static final int TIMEOUT = 60;

	private static final Executor exec = Executors.newCachedThreadPool();
	
	/**
	 * ServerSocket on the side of Android device
	 */
	private ServerSocket server_socket = null;
	
	/**
	 * Socket connecting Android device and PC host
	 */
	private Socket host_socket = null;
	
	// UI elements 
	private Button btn_start_time_sync = null;
	private Button btn_start_time_poll = null;
	private Button btn_time_poll = null;

	/**
	 * default constructor of {@link TimePollingFragment}
	 */
	public TimePollingFragment()
	{
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_communication_main, container, false);
		
		this.btn_start_time_sync = (Button) rootView.findViewById(R.id.btn_connect);
		this.btn_start_time_sync.setOnClickListener(this);
		
		this.btn_start_time_poll = (Button) rootView.findViewById(R.id.btn_start_polling);
		this.btn_start_time_poll.setOnClickListener(this);
		
		this.btn_time_poll = (Button) rootView.findViewById(R.id.btn_time_polling);
		this.btn_time_poll.setOnClickListener(this);
		this.btn_time_poll.setEnabled(false);
		
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
			
			case R.id.btn_connect:	// initialize server socket in background
				exec.execute(this.SyncTimeDaemon);
				Toast.makeText(getActivity(), "Port is open. Begin to Sync. Time.", Toast.LENGTH_SHORT).show();
				this.btn_start_time_sync.setEnabled(false);
				break;
			
			case R.id.btn_start_polling:
				exec.execute(this.TimePollingDaemon);
				Toast.makeText(getActivity(), "Starting polling time", Toast.LENGTH_SHORT).show();
				this.btn_start_time_poll.setEnabled(false);
				break;
				
			case R.id.btn_time_polling:
				this.pollHostTime();
				break;
				
			default:
				break;
		}
	}

	/**
	 * Starting as a ServerSocket. 
	 * Listen to client Socket, accept, and store it for further communication.
	 */
	public void establishDeviceHostConnection()
	{
		if (this.server_socket != null)
			return;
		
		try
		{
			server_socket = new ServerSocket();
			server_socket.bind(new InetSocketAddress("localhost", ADBExecutor.ANDROID_PORT));
			
			Log.d(TAG, "Localhost serversocket for time polling: " + server_socket.toString());
			
			host_socket = server_socket.accept();
			
			// receive (and consume) {@link AuthMsg} from PC and enable the time-polling functionality.
			this.receiveAuthMsg();
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	// Daemon thread for establishing and maintaining the time polling connection
	private Runnable TimePollingDaemon = new Runnable()
	{
		public void run()
		{
			establishDeviceHostConnection();
		}
	};
	
	/**
	 * Receive {@link AuthMsg} from PC; Enable the time-polling functionality.
	 */
	private void receiveAuthMsg()
	{
		SocketUtil.INSTANCE.receiveMsg(host_socket);
		
		getActivity().runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				btn_time_poll.setEnabled(true);
			}
		});
	}
	
	/**
	 * Wait for and receive {@link ResponseTimeMsg} from PC.
	 * @param host_socket the message is sent via this specified socket
	 * @return {@link ResponseTimeMsg} from PC
	 */
	private ResponseTimeMsg receiveResponseTimeMsgInNewThread(final Socket host_socket)
	{
		Message msg = SocketUtil.INSTANCE.receiveMsgInNewThread(host_socket);
		assert msg.getType() == Message.RESPONSE_TIME_MSG;
		return (ResponseTimeMsg) msg;
	}
	
	/**
	 * Poll system time of PC
	 * @return system time of PC
	 */
	public long pollHostTime()
	{
		/**
		 * Send {@link RequestTimeMsg} to PC in a new thread.
		 * You cannot use network connection on the Main UI thread.
		 * Otherwise you will get {@link NetworkOnMainThreadException}
		 */
		SocketUtil.INSTANCE.sendMsgInNewThread(new RequestTimeMsg(), host_socket);
		
		ResponseTimeMsg responseTimeMsg = this.receiveResponseTimeMsgInNewThread(host_socket);
		final long time = responseTimeMsg.getHostPCTime();
		
		getActivity().runOnUiThread(new Runnable()
		{
			public void run()
			{
				Toast.makeText(getActivity(), String.valueOf(time), Toast.LENGTH_SHORT).show();
			}
		});
		
		return time;
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
								TimePollingFragment.this.onReceive(msg);
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
	 * Called when the "Back button" on mobile phone is clicked;
	 * Release the server socket (if it exists) before exiting
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
	private Runnable SyncTimeDaemon = new Runnable()
	{
		public void run()
		{
			getReadyForSync();
		}
	};
	
}
