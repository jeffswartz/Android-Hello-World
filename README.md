Android-Hello-World
===================

The OpenTokHello sample app is a basic sample app that shows the most basic features of the OpenTok iOS SDK.

Once the app connects to the OpenTok session, it publishes an audio-video stream, which is displayed onscreen.
Then, the same audio-video stream shows up as a subscribed stream (along with any other streams currently in the
session).

Before you test the sample app, be sure to read the README file for [the OpenTok Android SDK](https://github.com/opentok/opentok-android-sdk).

Notes
-----

* The OpenTok Android SDK is only supported on the Samsung Galaxy S III.
* See the [API reference documentation](http://opentok.github.com/opentok-android-sdk) at the [OpenTok Android SDK project](https://github.com/opentok/opentok-android-sdk) on github.
* You cannot test using OpenTok videos in the AVD emulator.
* The SDK generates quite a bit of logging. Every logging tag starts with "opentok-".

Testing the sample app
----------------------

1.  Import the project into ADT.

2.  Connect your Android device to a USB port on your computer. Set up [USB debugging](http://developer.android.com/tools/device.html)
    on your device.

3.  Configure the project to use your API Key, your own Session, and a Token to access it.  If you don't have an
    API Key [sign up for a Developer Account](https://dashboard.tokbox.com/signups/new). Then to generate the Session ID
    and Token, use the Project Tools on the [Project Details](https://dashboard.tokbox.com/projects) page.

    Open the MainActivity.java file and set the `API_KEY`, `SESSION_ID`, and `TOKEN` strings to your own API Key, Session ID, and Token,
    respectively.

    Edit `browser_demo.html` and modify the variables `apiKey`, `sessionId`, and `token` with your own API Key, Session ID,
    and Token, respectively.

4.  Debug the app in ADT.

    The app should start on your connected device. Once the app connects to the OpenTok session, it publishes an audio-video
    stream, which is displayed onscreen. Then, the same audio-video stream shows up as a subscribed stream (along with any
    other streams currently in the session).

5.  Close the app. Now set up the app to subscribe to audio-video streams other than your own:
    -   Near the top of the `MainActivity.java` file, change the `subscribeToSelf` property to be set to `false`.
    -   Run the app on your iOS device again.
    -   In a browser on your development computer, load the `browser_demo.html` file to add more streams to
        the session.
    -   In the web page, click the Connect and Publish buttons.

        ***Note:*** If the web page asks you to set the Flash Player Settings, or if you do not see a display of your camera in
        the page, see the instructions in
        [Flash Player Settings for local testing](http://www.tokbox.com/opentok/docs/js/tutorials/helloworld.html#localTest).

Understanding the code
----------------------

The `MainActivity.java` file contains the main implementation code that includes use of the OpenTok Android API.

### Initializing an OTSession object and connecting to an OpenTok session

When the main activity is created, the app adds layout objects for the publisher and subscriber videos:

    publisherView = (RelativeLayout)findViewById(R.id.publisherview);
    subscriberView = (RelativeLayout)findViewById(R.id.subscriberview);

It then calls a method to instantiate an a Session object and connection to the OpenTok session:

    private void sessionConnect(){
      executor.submit(new Runnable() {
        public void run() {
          session = Session.newInstance(MainActivity.this, SESSION_ID, MainActivity.this);
          session.connect(TOKEN);
        }
      });
    }

The `Session.newInstance()` static method instantiates a new Session object.

- The first parameter of the method is the Android application context associated with this process.
- The second parameter is the session ID for the OpenTok session your app connects to. You can generate a session ID from the [Developer Dashboard](https://dashboard.tokbox.com/projects) or from a
[server-side library](http://www.tokbox.com/opentok/docs/concepts/server_side_libraries.html).
- The third parameter is the listener to respond to state changes on the session. This listener is defined by the
Session.Listener interface, and the MainActivity class of this app implements this interface.

The `connect()` method of the Session object connects your app to the OpenTok session. The `TOKEN` constant is the token string for the
client connecting to the session. See [Connection Token Creation](http://www.tokbox.com/docs/concepts/token_creation.html) for details.
You can generate a token from the [Developer Dashboard](https://dashboard.tokbox.com/projects) or from a
[server-side library](http://www.tokbox.com/opentok/docs/concepts/server_side_libraries.html). (In final applications,
use the OpenTok server-side library to generate unique tokens for each user.)

When the app connects to the OpenTok session, the `onConnect()` method of the Session.Listener is called.


### Publishing an audio-video stream to a session

In the `onConnect()` method, the app publishes a stream to the OpenTok session:

    @Override
    public void onSessionConnected() {
      Log.i(LOGTAG,"session connected");
      
      runOnUiThread(new Runnable() {
    
        @Override
        public void run() {
          //Create Publisher instance.
          publisher = Publisher.newInstance(MainActivity.this);
          publisher.setName("hello");
          publisher.setListener(MainActivity.this);
          publisherView.addView(publisher.getView());
          session.publish(publisher);
        }});
    }

The `Publisher.newInstance` method instantiates the Publisher object.

The `setName()` method of the Publisher object sets the name of the stream. The name of a stream is an optional string
that appears at the bottom of the stream's view when the user taps the stream (or clicks it in a browser).

Next, the `setListener()` method of the Publisher object sets a listener for publisher-related events. Note that the
MainActivity class implements the Publisher.Listener interface.

The `getView()` method of the Publisher object returns the view in which the Publisher will display video, and this view
is added to the publisherView we created earlier.

Next, we call the `publish()` method of the Session object, passing in the Publisher object as a parameter. This
publishes a stream to the OpenTok session. The `onPublisherStreamingStarted()` method, defined by the Publisher.Listener
interface, is called when the Publisher starts streaming.

### Subscribing to streams

When a stream is added to a session, the `onStreamReceived()` method of the Session.Listener is called. It then
initializes a Subscriber object for the stream, add's the subscriber's view (which contains the video) to the app,
sets the listener for subscriber-related events (defined by the Subscriber.Listener interface), and calls the
`subscribe()` method of the Session object to subscribe to the stream

    @Override
    public void onSessionReceivedStream(final Stream stream) {
      Log.i(LOGTAG,"session received stream");
      
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if(subscriberToSelf == session.getConnection().getConnectionId().equals(stream.getConnection().getConnectionId())))) {
              subscriber = Subscriber.newInstance(MainActivity.this, stream);
              subscriberView.addView(subscriber.getView());  
              subscriber.setListener(MainActivity.this);
              session.subscribe(subscriber);
          }
        }
      });
    }

This app subscribes to one stream, at most. It either subscribes to the stream you publish, or it subscribes to one
of the other streams in the session (if there is one), based on the `subscribeToSelf` property, which is set at the
top of the file. Normally, an app would not subscribe to a stream it publishes. (See the last step of "Testing the
sample app" above.)

The connection ID for the stream you publish will match the connection ID for your session. (***Note:*** in a real
application, a client would not normally subscribe to its own published stream. However, for this test app, it is
convenient for the client to subscribe to its own stream.)

The `onSubscriberConnected()` method of the Subscriber.Listener interface is called when the subscriber connects to the
stream.

### Removing dropped streams

As streams leave the session (when clients disconnect or stop publishing), the `onSessionDroppedStream()` method
of the Session.Listener interface is call. The OpenTok Android SDK automatically removed a Subscriber's views when its
stream is dropped.

### Knowing when you have disconnected from the session

Finally, when the app disconnects from the session, the `onSessionDisconnected()` method of the Session.Listener
interface is called.

    @Override
    public void onSessionDisconnected() {
      Log.i(LOGTAG, "session disconnected");
      showAlert("Session disconnected: " + session.getSessionId());
    }

If an app cannot connect to the session (perhaps because of no network connection), the OTSubscriberDelegate is sent
the `onSessionException()` method of the Session.Listener interface is called:

    @Override
    public void onSessionException(OpentokException exception) {
      Log.e(LOGTAG, "session failed! "+exception.toString());
      showAlert("There was an error connecting to session " + session.getSessionId());
    }


Next steps
----------

For details on the full OpenTok Android API, see the [reference documentation](http://opentok.github.io/opentok-android-sdk/).
