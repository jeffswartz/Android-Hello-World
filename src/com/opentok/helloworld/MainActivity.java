package com.opentok.helloworld;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.widget.RelativeLayout;

import com.opentok.OpentokException;
import com.opentok.Publisher;
import com.opentok.Session;
import com.opentok.Stream;
import com.opentok.Subscriber;

/**
 * This application demonstrates the basic workflow for getting started with the OpenTok Android SDK.
 */
public class MainActivity extends Activity implements Publisher.Listener, Subscriber.Listener, Session.Listener {
	
	private static final String LOGTAG = "hello-world";
	
	// Fill the following variables using your own Project info from https://dashboard.tokbox.com
	private static String SESSION_ID =""; // Replace with your generated Session ID
	private static String TOKEN = ""; // Replace with your generated Token (use Project Tools or from a server-side library)

	private ExecutorService executor;
	private RelativeLayout publisherView;
	private RelativeLayout subscriberView;
	private Publisher publisher;
	private Subscriber subscriber;
	private Session session;
	private WakeLock wakeLock;
	private boolean subscribeToSelf=true; // Change to false if you want to subscribe to streams other than your own.

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		publisherView = (RelativeLayout)findViewById(R.id.publisherview);
		subscriberView = (RelativeLayout)findViewById(R.id.subscriberview);
		
		// A simple executor will allow us to perform tasks asynchronously.
		executor = Executors.newCachedThreadPool();

		// Disable screen dimming
		PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
						"Full Wake Lock");
		
		sessionConnect();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onStop() {
		super.onStop();
		
		//release the session
		if(session!=null){
			session.disconnect();
		}
		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if (!wakeLock.isHeld()) {
			wakeLock.acquire();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
	}
	
	private void sessionConnect(){
		executor.submit(new Runnable() {
			public void run() {
				session = Session.newInstance(MainActivity.this, SESSION_ID, MainActivity.this);
				session.connect(TOKEN);
			}});
	}

	private void showAlert(String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		 
	    builder.setTitle("Message from video session ");
	    builder.setMessage(message);
	    builder.setPositiveButton("OK", new OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	            dialog.cancel();
	        }
	    });
	    builder.create();
	    builder.show();
	}
	
	@Override
	public void onSessionConnected() {
		Log.i(LOGTAG,"session connected");
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				publisher=Publisher.newInstance(MainActivity.this);
				publisher.setName("hello");
				publisher.setListener(MainActivity.this);
				publisherView.addView(publisher.getView());
				session.publish(publisher);
			}});
	}

	@Override
	public void onSessionReceivedStream(final Stream stream) {
		Log.i(LOGTAG,"session received stream");
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (subscribeToSelf == session.getConnection().equals(stream.getConnection())) {
					subscriber = Subscriber.newInstance(MainActivity.this, stream);
					subscriberView.addView(subscriber.getView());
					subscriber.setListener(MainActivity.this);
					session.subscribe(subscriber);
				}
			}});
	}

	@Override
	public void onPublisherStreamingStarted() {
		Log.i(LOGTAG, "publisher is streaming!");
	}

	@Override
	public void onSessionDroppedStream(Stream stream) {
		Log.i(LOGTAG, String.format("stream dropped", stream.toString()));
	}

	@Override
	public void onSessionDisconnected() {
		Log.i(LOGTAG, "session disconnected");	
		showAlert("Session disconnected: " + session.getSessionId());
	}

	@Override
	public void onPublisherStreamingStopped() {
		Log.i(LOGTAG, "publisher disconnected");	
	}

	@Override
	public void onPublisherChangedCamera(int cameraId) {
		Log.i(LOGTAG, "publisher changed camera to cameraId: " + cameraId);
	}

	@Override
	public void onSubscriberConnected(Subscriber subscriber) {
		Log.i(LOGTAG, "subscriber connected");	
	}

	@Override
	public void onSessionException(OpentokException exception) {
		Log.e(LOGTAG, "session failed! " + exception.toString());
		showAlert("There was an error connecting to session " + session.getSessionId());
	}
	
	@Override
	public void onSubscriberException(Subscriber subscriber, OpentokException exception) {
		Log.i(LOGTAG, "subscriber " + subscriber + " failed! " + exception.toString());
		showAlert("There was an error subscribing to stream " + subscriber.getStream().getStreamId());
	}

	@Override
	public void onPublisherException(OpentokException exception) {
		Log.i(LOGTAG, "publisher failed! " + exception.toString());
		showAlert("There was an error publishing");
	}
}
