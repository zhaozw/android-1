/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.call;

import java.awt.*;
import java.beans.*;
import java.util.*;

import net.java.sip.communicator.service.protocol.media.*;
import org.jitsi.*;
import org.jitsi.impl.neomedia.jmfext.media.protocol.mediarecorder.*;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.osgi.*;
import org.jitsi.util.event.*;

import net.java.sip.communicator.service.gui.call.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.call.*;
import net.java.sip.communicator.util.call.CallPeerAdapter;

import android.graphics.Color;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

/**
 * The <tt>VideoCallActivity</tt> corresponds the call screen.
 *
 * @author Yana Stamcheva
 * @author Pawel Domas
 */
public class VideoCallActivity
    extends OSGiActivity
    implements  CallPeerRenderer,
                CallRenderer,
                CallChangeListener,
                PropertyChangeListener
{
    /**
     * The logger
     */
    private static final Logger logger =
            Logger.getLogger(VideoCallActivity.class);

    /**
     * The remote video container.
     */
    private ViewGroup remoteVideoContainer;

    /**
     * The local video container.
     */
    private SurfaceView previewDisplay;

    /**
     * The callee avatar.
     */
    private ImageView calleeAvatar;

    /**
     * The call peer adapter that gives us access to all call peer events.
     */
    private CallPeerAdapter callPeerAdapter;

    /**
     * The corresponding call.
     */
    private Call call;

    /**
     * Indicates if the call timer has been started.
     */
    private boolean isCallTimerStarted = false;

    /**
     * The start date time of the call.
     */
    private Date callStartDate;

    /**
     * A timer to count call duration.
     */
    private Timer callDurationTimer;

    /**
     * The {@link CallConference} instance depicted by this <tt>CallPanel</tt>.
     */
    private CallConference callConference;

    /**
     * The preview surface state handler
     */
    private CameraPreviewSurfaceHandler previewSurfaceHandler;

    /**
     * Flag indicates if the shutdown Thread has been started
     */
    private volatile boolean finishing = false;

    /**
     * Called when the activity is starting. Initializes the corresponding
     * call interface.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle).
     * Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.video_call);

        callDurationTimer = new Timer();

        String callIdentifier = getIntent().getExtras()
            .getString(CallManager.CALL_IDENTIFIER);

        call = CallManager.getActiveCall(callIdentifier);

        callConference = call.getConference();

        Iterator<? extends CallPeer> callPeerIter = call.getCallPeers();

        if (callPeerIter.hasNext())
        {
            addCallPeerUI(callPeerIter.next());
        }

        initVolumeView();
        initMicrophoneView();
        initLocalVideoView();
        initHangupView();

        calleeAvatar = (ImageView) findViewById(R.id.calleeAvatar);

        previewDisplay
            = (SurfaceView) findViewById(R.id.previewDisplay);
        
        // Creates and registers surface handler for events
        this.previewSurfaceHandler = new CameraPreviewSurfaceHandler();            
        org.jitsi.impl.neomedia.jmfext.media
                .protocol.mediarecorder.DataSource
                .setPreviewSurfaceProvider(previewSurfaceHandler);            
        previewDisplay.getHolder().addCallback(previewSurfaceHandler);

        remoteVideoContainer
            = (ViewGroup) findViewById(R.id.remoteVideoContainer);
        
        // Registers as the call state listener
        call.addCallChangeListener(this);
    }

    /**
     * Initializes the hangup button view.
     */
    private void initHangupView()
    {
        ImageView hangupView = (ImageView) findViewById(R.id.callHangupButton);

        hangupView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Start the hang up Thread, Activity will be closed later 
                // on call ended event
                CallManager.hangupCall(call);
            }
        });
    }

    /**
     * Called on call ended event. Runs on separate thread to release the EDT
     * Thread and preview surface can be hidden effectively.
     */
    private void doFinishActivity()
    {
        if(finishing)
            return;
        
        finishing = true;
        
        new Thread(new Runnable() 
        {
            public void run() 
            {
                // Waits for camera to be stopped
                previewSurfaceHandler.ensureCameraClosed();

                switchActivity(CallContactActivity.class);
            }
        }).start();        
    }

    /**
     * Initializes the local video button view.
     */
    private void initLocalVideoView()
    {
        final ImageView callVideoButton
            = (ImageView) findViewById(R.id.callVideoButton);

        callVideoButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                boolean isEnable = !CallManager.isLocalVideoEnabled(call);

                CallManager.enableLocalVideo(
                    call,
                    isEnable);

                if (isEnable)
                    callVideoButton.setBackgroundColor(0x50000000);
                else
                    callVideoButton.setBackgroundColor(Color.TRANSPARENT);
            }
        });
    }

    /**
     * Initializes the microphone button view.
     */
    private void initMicrophoneView()
    {
        final ImageView microphoneButton
            = (ImageView) findViewById(R.id.callMicrophoneButton);

        microphoneButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                CallManager.setMute(call, !isMuted());
            }
        });
    }

    /**
     * Returns <tt>true</tt> if call is currently muted.
     * @return <tt>true</tt> if call is currently muted.
     */
    private boolean isMuted()
    {
        return ((MediaAwareCall<?,?,?>)this.call).isMute();
    }

    private void updateMuteStatus()
    {
        runOnUiThread(
        new Runnable()
        {
            public void run()
            {
                doUpdateMuteStatus();
            }
        });
    }

    private void doUpdateMuteStatus()
    {
        final ImageView microphoneButton
                = (ImageView) findViewById(R.id.callMicrophoneButton);

        if (isMuted())
        {
            microphoneButton.setBackgroundColor(0x50000000);
            microphoneButton.setImageResource(
                    R.drawable.callmicrophonemute);
        }
        else
        {
            microphoneButton.setBackgroundColor(Color.TRANSPARENT);
            microphoneButton.setImageResource(
                    R.drawable.callmicrophone);
        }
    }

    /**
     * Initializes the volume button view.
     */
    private void initVolumeView()
    {
        ImageView volumeButton
            = (ImageView) findViewById(R.id.callVolumeButton);

        volumeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
            }
        });
    }

    /**
     * Handles a video event.
     *
     * @param callPeer the corresponding call peer
     * @param event the <tt>VideoEvent</tt> that notified us
     */
    public void handleVideoEvent(CallPeer callPeer,
                                 final VideoEvent event)
    {
        if (event.isConsumed())
            return;

        if (event.getOrigin() == VideoEvent.LOCAL)
        {
            // TODO: local video events are not used because the preview surface
            // is required for camera to start and it must not be removed until
            // is stopped, so it's handled by direct cooperation with 
            // .jmfext.media.protocol.mediarecorder.DataSource
            
            // Show/hide the local video.
            if (event.getType() == VideoEvent.VIDEO_ADDED)
            {
                
            }
            else if(event.getType() == VideoEvent.VIDEO_REMOVED)
            {
                
            }
            else if(event.getType() == SizeChangeVideoEvent.VIDEO_SIZE_CHANGE)
            {
                
            }
        }
        else if (event.getOrigin() == VideoEvent.REMOTE)
        {
            ProtocolProviderService pps = callPeer.getProtocolProvider();
            final Component visualComponent;

            if (pps != null)
            {
                OperationSetVideoTelephony osvt
                    = pps.getOperationSet(OperationSetVideoTelephony.class);

                if (osvt != null)
                    visualComponent = osvt.getVisualComponent(callPeer);
                else
                    visualComponent = null;
            }
            else
                visualComponent = null;

            if (remoteVideoContainer != null)
            {
                event.consume();

                Handler remoteVideoHandler = remoteVideoContainer.getHandler();
                if(remoteVideoHandler == null)
                {
                    // Remote video object is no longer attached
                    // to View hierarchy
                    logger.warn("Remote video container is not currently"
                                + " attached to the view hierarchy.");
                    return;
                }
                remoteVideoHandler.post(
                    new Runnable()
                    {
                        public void run()
                        {
                            View view = null;

                            if (visualComponent instanceof ViewAccessor)
                            {
                                view
                                    = ((ViewAccessor) visualComponent)
                                            .getView(VideoCallActivity.this);
                            }

                            int width = -1;
                            int height = -1;

                            DisplayMetrics displaymetrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay()
                                .getMetrics(displaymetrics);
                            int viewHeight = displaymetrics.heightPixels;
                            int viewWidth = displaymetrics.widthPixels;

                            if (view != null)
                            {
                                /*
                                 * If the visualComponent displaying the
                                 * video of the remote callPeer has a
                                 * preferredSize, attempt to respect it.
                                 */
                                Dimension preferredSize
                                    = visualComponent.getPreferredSize();

                                if ((preferredSize != null)
                                        && (preferredSize.width > 0)
                                        && (preferredSize.height > 0))
                                {
                                    width = preferredSize.width;
                                    height = preferredSize.height;
                                }
                                else if (event instanceof SizeChangeVideoEvent)
                                {
                                    /*
                                     * The SizeChangeVideoEvent may have
                                     * been delivered with a delay and thus
                                     * may not represent the up-to-date size
                                     * of the remote video. But since the
                                     * visualComponent does not have a
                                     * preferredSize, anything like the size
                                     * reported by the SizeChangeVideoEvent
                                     * may be used as a hint.
                                     */
                                    SizeChangeVideoEvent scve
                                        = (SizeChangeVideoEvent) event;

                                    if ((scve.getHeight() > 0)
                                            && (scve.getWidth() > 0))
                                    {
                                        height = scve.getHeight();
                                        width = scve.getWidth();
                                    }
                                }
                            }

                            remoteVideoContainer.removeAllViews();

                            if (view != null)
                            {
                                float ratio = width / (float) height;

                                if (height < viewHeight)
                                {
                                    height = viewHeight;
                                    width = (int) (height*ratio);
                                }
                                remoteVideoContainer.addView(
                                        view,
                                        new ViewGroup.LayoutParams(
                                                width,
                                                height));

                                calleeAvatar.setVisibility(View.GONE);

                                // Show the local video in the center or in the
                                // left corner depending on if we have a remote
                                // video shown.
                                realignPreviewDisplay();
                            }
                            else
                                calleeAvatar.setVisibility(View.VISIBLE);
                        }
                    });
            }
        }
    }

    /**
     * Adds a video listener for the given call peer.
     *
     * @param callPeer the <tt>CallPeer</tt> to which we add a video listener
     */
    private void addVideoListener(final CallPeer callPeer)
    {
        ProtocolProviderService pps = callPeer.getProtocolProvider();

        if (pps == null)
            return;

        OperationSetVideoTelephony osvt
            = pps.getOperationSet(OperationSetVideoTelephony.class);

        if (osvt == null)
            return;

        osvt.addVideoListener(
                callPeer,
                new VideoListener()
                {
                    public void videoAdded(VideoEvent event)
                    {
                        handleVideoEvent(callPeer, event);
                    }

                    public void videoRemoved(VideoEvent event)
                    {
                        handleVideoEvent(callPeer, event);
                    }

                    public void videoUpdate(VideoEvent event)
                    {
                        handleVideoEvent(callPeer, event);
                    }
                });
    }

    /**
     * Re-aligns the preview display depending on the remote video visibility.
     */
    private void realignPreviewDisplay()
    {
        RelativeLayout.LayoutParams params
            = (RelativeLayout.LayoutParams) previewDisplay
            .getLayoutParams();

        if (remoteVideoContainer.getChildCount() > 0)
        {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
        }
        else
        {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }

        previewDisplay.setLayoutParams(params);
    }

    /**
     * Sets the peer name.
     *
     * @param name the name of the call peer
     */
    public void setPeerName(final String name)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView calleeName = (TextView) findViewById(R.id.calleeName);

                calleeName.setText(name);
            }
        });
    }

    /**
     * Sets the peer image.
     *
     * @param image the avatar of the call peer
     */
    public void setPeerImage(byte[] image)
    {

    }

    /**
     * Sets the peer state.
     *
     * @param oldState the old peer state
     * @param newState the new peer state
     * @param stateString the state of the call peer
     */
    public void setPeerState(CallPeerState oldState, CallPeerState newState,
        final String stateString)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                TextView statusName = (TextView) findViewById(R.id.callStatus);

                statusName.setText(stateString);
            }
        });
    }

    /**
     * Sets the peer time string.
     *
     * @param timeString the time string for the call peer
     */
    public void setTimeString(final String timeString)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                TextView callTime = (TextView) findViewById(R.id.callTime);

                callTime.setText(timeString);
            }
        });
    }

    public void setErrorReason(String reason) {}

    public void setMute(boolean isMute)
    {
        // Just invoke mute UI refresh
        updateMuteStatus();
    }

    /**
     * Method mapped to hold button view on click event
     *
     * @param holdButtonView the button view that has been clicked
     */
    public void onHoldButtonClicked(View holdButtonView)
    {
        CallManager.putOnHold(call, !isOnHold());
    }

    private boolean isOnHold()
    {
        boolean onHold = false;
        Iterator<? extends CallPeer> peers = call.getCallPeers();
        if(peers.hasNext())
        {
            CallPeerState peerState = call.getCallPeers().next().getState();
            onHold = CallPeerState.ON_HOLD_LOCALLY.equals(peerState)
                    || CallPeerState.ON_HOLD_MUTUALLY.equals(peerState);
        }
        else 
        {
            logger.warn("No peer belongs to call: "+call.toString());    
        }

        return onHold;
    }

    public void setOnHold(boolean isOnHold){}

    /**
     * Updates on hold button to represent it's actual state
     */
    private void updateHoldStatus()
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                doUpdateHoldStatus();
            }
        });
    }

    /**
     * Updates on hold button to represent it's actual state.
     * Called from {@link #updateHoldStatus()}.
     */
    private void doUpdateHoldStatus()
    {
        final ImageView holdButton
                = (ImageView) findViewById(R.id.callHoldButton);

        if (isOnHold())
        {
            holdButton.setBackgroundColor(0x50000000);
        }
        else
        {
            holdButton.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void setCallPeerAdapter(CallPeerAdapter adapter)
    {
        this.callPeerAdapter = adapter;
    }

    public CallPeerAdapter getCallPeerAdapter()
    {
        return callPeerAdapter;
    }

    public void printDTMFTone(char dtmfChar)
    {

    }

    public CallRenderer getCallRenderer()
    {
        return this;
    }

    public void setLocalVideoVisible(final boolean isVisible)
    {
        // It can not be hidden here, because the preview surface will be
        // destroyed and camera recording system will crash     
    }

    /**
     * Sets {@link #previewDisplay} visibility state. As a result onCreate and
     * onDestroy events are produced when the surface used for camera display is
     * created/destroyed. 
     * 
     * @param isVisible flag indicating if it should be shown or hidden
     */
    private void setLocalVideoPreviewVisible(final boolean isVisible)
    {
        previewDisplay.getHandler().post(new Runnable() 
        {
            public void run() 
            {
                if (isVisible)
                {
                    // Show the local video in the center or in the left
                    // corner depending on if we have a remote video shown.
                    realignPreviewDisplay();
                    previewDisplay.setVisibility(View.VISIBLE);
                }
                else
                {
                    previewDisplay.setVisibility(View.GONE);
                }
            }
        });
    }

    public boolean isLocalVideoVisible()
    {
        return (previewDisplay.getVisibility() != View.VISIBLE)
                    ? false
                    : true;
    }

    public Call getCall()
    {
        return call;
    }

    public CallPeerRenderer getCallPeerRenderer(CallPeer callPeer)
    {
        return this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.video_call_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.low_resolution:
                return true;
            case R.id.high_resolution:
                return true;
            case R.id.call_info_item:
                showCallInfoDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Displays technical call information dialog.
     */
    private void showCallInfoDialog()
    {
        CallInfoDialogFragment callInfo
                = CallInfoDialogFragment.newInstance(
                getIntent().getStringExtra(
                        CallManager.CALL_IDENTIFIER));

        callInfo.show(getFragmentManager(), "callinfo");
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        /*
         * If a Call is added to or removed from the CallConference depicted
         * by this CallPanel, an update of the view from its model will most
         * likely be required.
         */
        if (evt.getPropertyName().equals(CallConference.CALLS))
            onCallConferenceEventObject(evt);
    }

    public void callPeerAdded(CallPeerEvent evt)
    {
        CallPeer callPeer = evt.getSourceCallPeer();

        addCallPeerUI(callPeer);

        onCallConferenceEventObject(evt);
    }

    public void callPeerRemoved(CallPeerEvent evt)
    {
        CallPeer callPeer = evt.getSourceCallPeer();

        if (callPeerAdapter != null)
        {
            callPeer.addCallPeerListener(callPeerAdapter);
            callPeer.addCallPeerSecurityListener(callPeerAdapter);
            callPeer.addPropertyChangeListener(callPeerAdapter);
        }

        setPeerState(   callPeer.getState(),
                        callPeer.getState(),
                        callPeer.getState().getLocalizedStateString());

        onCallConferenceEventObject(evt);
    }

    public void callStateChanged(CallChangeEvent evt)
    {
        onCallConferenceEventObject(evt);
    }

    /**
     * Invoked by {@link #callConferenceListener} to notify this instance about
     * an <tt>EventObject</tt> related to the <tt>CallConference</tt> depicted
     * by this <tt>CallPanel</tt>, the <tt>Call</tt>s participating in it,
     * the <tt>CallPeer</tt>s associated with them, the
     * <tt>ConferenceMember</tt>s participating in any telephony conferences
     * organized by them, etc. In other words, notifies this instance about
     * any change which may cause an update to be required so that this view
     * i.e. <tt>CallPanel</tt> depicts the current state of its model i.e.
     * {@link #callConference}.
     *
     * @param ev the <tt>EventObject</tt> this instance is being notified
     * about.
     */
    private void onCallConferenceEventObject(EventObject ev)
    {
        /*
         * The main task is to invoke updateViewFromModel() in order to make
         * sure that this view depicts the current state of its model.
         */

        try
        {
            /*
             * However, we seem to be keeping track of the duration of the call
             * (i.e. the telephony conference) in the user interface. Stop the
             * Timer which ticks the duration of the call as soon as the
             * telephony conference depicted by this instance appears to have
             * ended. The situation will very likely occur when a Call is
             * removed from the telephony conference or a CallPeer is removed
             * from a Call.
             */
            boolean tryStopCallTimer = false;

            if (ev instanceof CallPeerEvent)
            {
                tryStopCallTimer
                    = (CallPeerEvent.CALL_PEER_REMOVED
                            == ((CallPeerEvent) ev).getEventID());
            }
            else if (ev instanceof PropertyChangeEvent)
            {
                PropertyChangeEvent pcev = (PropertyChangeEvent) ev;

                tryStopCallTimer
                    = (CallConference.CALLS.equals(pcev)
                            && (pcev.getOldValue() instanceof Call)
                            && (pcev.getNewValue() == null));
            }

            if (tryStopCallTimer
                    && (callConference.isEnded()
                            || callConference.getCallPeerCount() == 0))
            {
                stopCallTimer();
                doFinishActivity();
            }
        }
        finally
        {
            updateViewFromModel(ev);
        }
    }

    /**
     * Starts the timer that counts call duration.
     */
    public void startCallTimer()
    {
        this.callStartDate = new Date();
        this.callDurationTimer
            .schedule(new CallTimerTask(),
                new Date(System.currentTimeMillis()), 1000);
        this.isCallTimerStarted = true;
    }

    /**
     * Stops the timer that counts call duration.
     */
    public void stopCallTimer()
    {
        this.callDurationTimer.cancel();
    }

    /**
     * Returns <code>true</code> if the call timer has been started, otherwise
     * returns <code>false</code>.
     * @return <code>true</code> if the call timer has been started, otherwise
     * returns <code>false</code>
     */
    public boolean isCallTimerStarted()
    {
        return isCallTimerStarted;
    }

    /**
     * Each second refreshes the time label to show to the user the exact
     * duration of the call.
     */
    private class CallTimerTask
        extends TimerTask
    {
        @Override
        public void run()
        {
            String time = GuiUtils.formatTime(
                callStartDate.getTime(),
                System.currentTimeMillis());

            VideoCallActivity.this.setTimeString(time);
        }
    }

    /**
     * The class exposes methods for managing preview surface state which must 
     * be synchronized with currently used {@link android.hardware.Camera} 
     * state.<br/>
     * The surface must be present before the camera is started and for this 
     * purpose {@link #obtainPreviewSurface()} method shall be used.
     * <br/>
     * When the call is ended, before the <tt>Activity</tt> is finished we
     * should ensure that the camera has been stopped(which is done by video
     * telephony internals), so we should wait for it to be disposed by 
     * invoking method {@link #ensureCameraClosed()}. It will block current 
     * <tt>Thread</tt> until it happens or an <tt>Exception</tt> will be thrown
     * if timeout occurs.
     * <br/>
     * It's a workaround which allows not changing the
     * OperationSetVideoTelephony and related APIs.
     *  
     * @see DataSource.PreviewSurfaceProvider
     * 
     */
    private class CameraPreviewSurfaceHandler
    implements DataSource.PreviewSurfaceProvider,
            SurfaceHolder.Callback    
    {

        /**
         * Timeout for dispose surface operation
         */
        private static final long REMOVAL_TIMEOUT=10000L;

        /**
         * Timeout for create surface operation
         */
        private static final long CREATE_TIMEOUT=10000L;

        /**
         * Pointer to the <tt>Surface</tt> used for preview
         */
        private Surface previewSurface;

        /**
         * Blocks until the {@link android.hardware.Camera} is stopped and 
         * {@link #previewDisplay} is hidden, or throws an <tt>Exception</tt>
         * if timeout occurs.
         */
        synchronized void ensureCameraClosed()
        {
            // If local video is visible wait until camera will be closed
            if(previewSurface != null)
            {
                try
                {
                    synchronized (this)
                    {
                        this.wait(REMOVAL_TIMEOUT);
                        if(previewSurface != null)
                        {
                            throw new RuntimeException(
                                    "Timeout waiting for" 
                                    + " preview surface removal");
                        }
                    }
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        /**
         * Blocks until {@link #previewDisplay} is shown and the surface is
         * created or throws en <tt>Exception</tt> if timeout occurs.
         * 
         * @return created <tt>Surface</tt> that shall be used for local camera
         * preview
         */
        synchronized public Surface obtainPreviewSurface()
        {
            setLocalVideoPreviewVisible(true);
            if(this.previewSurface == null)
            {
                try 
                {
                    this.wait(CREATE_TIMEOUT);
                    if(previewSurface == null)
                    {
                        throw new RuntimeException(
                                "Timeout waiting for surface");
                    }                    
                }
                catch (InterruptedException e) 
                {
                    throw new RuntimeException(e);
                }
            }
            return previewSurface;    
        }


        /**
         * Hides the local video preview component causing the <tt>Surface</tt>
         * to be destroyed.
         */
        public void onPreviewSurfaceReleased()
        {
            setLocalVideoPreviewVisible(false);
        }
        
        synchronized public void surfaceCreated(SurfaceHolder holder)
        {
            this.previewSurface = holder.getSurface();
            this.notifyAll();
        }

        public void surfaceChanged( SurfaceHolder surfaceHolder, 
                                    int i, int i2, int i3 ) 
        {
            
        }

        synchronized public void surfaceDestroyed(SurfaceHolder holder)
        {
            this.previewSurface = null;
            this.notifyAll();
        }
    }

    private void addCallPeerUI(CallPeer callPeer)
    {
        TextView calleeName = (TextView) findViewById(R.id.calleeName);

        callPeerAdapter
            = new CallPeerAdapter(callPeer, this);
        callPeer.addCallPeerListener(callPeerAdapter);
        callPeer.addCallPeerSecurityListener(callPeerAdapter);
        callPeer.addPropertyChangeListener(callPeerAdapter);

        addVideoListener(callPeer);

        setPeerState(   null,
                        callPeer.getState(),
                        callPeer.getState().getLocalizedStateString());
        setPeerName(calleeName.getText()
            + " " + callPeer.getDisplayName());
    }

    private void updateViewFromModel(EventObject ev)
    {
    }

    public void updateHoldButtonState() 
    {
        updateHoldStatus();
    }

    public void dispose() {}

    public void securityNegotiationStarted(
        CallPeerSecurityNegotiationStartedEvent securityStartedEvent) {}

    public void securityPending() {}

    public void securityTimeout(CallPeerSecurityTimeoutEvent evt) {}

    public void setSecurityPanelVisible(boolean visible) {}

    public void securityOff(CallPeerSecurityOffEvent evt) {}

    public void securityOn(CallPeerSecurityOnEvent evt) {}
}