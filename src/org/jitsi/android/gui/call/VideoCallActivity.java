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

import org.jitsi.*;
import org.jitsi.android.gui.menu.*;
import org.jitsi.service.neomedia.*;
import org.jitsi.util.event.*;

import net.java.sip.communicator.service.gui.call.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.call.*;
import net.java.sip.communicator.util.call.CallPeerAdapter;

import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

/**
 * The <tt>VideoCallActivity</tt> corresponds the call screen.
 *
 * @author Yana Stamcheva
 */
public class VideoCallActivity
    extends MainMenuActivity
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
     * Mute call status
     */
    private boolean isMuted = false;

    /**
     * On hold call status
     */
    private boolean isOnHold = false;

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

        if (call != null)
        {
            callConference = call.getConference();

            Iterator<? extends CallPeer> callPeerIter
                = call.getCallPeers();

            if (callPeerIter.hasNext())
            {
                addCallPeerUI(callPeerIter.next());
            }
        }

        initVolumeView();
        initMicrophoneView();
        initLocalVideoView();
        initHangupView();

        calleeAvatar = (ImageView) findViewById(R.id.calleeAvatar);

        previewDisplay
            = (SurfaceView) findViewById(R.id.previewDisplay);

        if (previewDisplay != null)
        {
            previewDisplay.getHolder().addCallback(
                    new SurfaceHolder.Callback()
                    {
                        public void surfaceChanged(
                                SurfaceHolder holder,
                                int format,
                                int width, int height)
                        {
                            // TODO Auto-generated method stub
                        }

                        public void surfaceCreated(SurfaceHolder holder)
                        {
                            /*
                             * TODO Setting a static previewDisplay on the
                             * MediaRecorder DataSource is a workaround which
                             * allows not changing the
                             * OperationSetVideoTelephony and related APIs.
                             */
                            org.jitsi.impl.neomedia.jmfext.media
                                    .protocol.mediarecorder.DataSource
                                            .setDefaultPreviewDisplay(
                                                    holder.getSurface());
                        }

                        public void surfaceDestroyed(SurfaceHolder holder)
                        {
                            /*
                             * TODO Setting a static previewDisplay on the
                             * MediaRecorder DataSource is a workaround which
                             * allows not changing the
                             * OperationSetVideoTelephony and related APIs.
                             */
                            org.jitsi.impl.neomedia.jmfext.media
                                    .protocol.mediarecorder.DataSource
                                            .setDefaultPreviewDisplay(
                                                    null);
                        }
                    });
        }

        remoteVideoContainer
            = (ViewGroup) findViewById(R.id.remoteVideoContainer);
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
                CallManager.hangupCall(call);

                Intent callContactIntent
                    = new Intent(   VideoCallActivity.this,
                                    CallContactActivity.class);

                VideoCallActivity.this.startActivity(callContactIntent);
            }
        });
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
                //boolean isMute = !CallManager.isMute(call);
                CallManager.setMute(call, !isMuted);
            }
        });
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
        boolean isMute = isMuted;

        if (isMute)
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
            final SurfaceView previewDisplay
                = (SurfaceView) findViewById(R.id.previewDisplay);

            previewDisplay.getHandler().post(new Runnable()
            {
                public void run()
                {
                    // Show/hide the local video.
                    if (event.getType() == VideoEvent.VIDEO_ADDED)
                    {
                        // Show the local video in the center or in the left
                        // corner depending on if we have a remote video shown.
                        realignPreviewDisplay();

                        previewDisplay.setVisibility(View.VISIBLE);
                    }
                    else
                        previewDisplay.setVisibility(View.GONE);
                }
            });
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

                remoteVideoContainer.getHandler().post(
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
        this.isMuted = isMute;
        updateMuteStatus();
    }

    /**
     * Method mapped to hold button view on click event
     *
     * @param holdButtonView the button view that has been clicked
     */
    public void onHoldButtonClicked(View holdButtonView)
    {
        CallManager.putOnHold(call, !isOnHold);
    }

    public void setOnHold(boolean isOnHold)
    {
        this.isOnHold = isOnHold;
        updateHoldStatus();
    }

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

        if (isOnHold)
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
        previewDisplay.getHandler().post(new Runnable()
        {
            public void run()
            {
                if (isVisible)
                    previewDisplay.setVisibility(View.VISIBLE);
                else
                    previewDisplay.setVisibility(View.GONE);
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
        inflater.inflate(R.menu.main_menu, menu);
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
            default:
                return super.onOptionsItemSelected(item);
        }
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

    public void updateHoldButtonState() {}

    public void dispose() {}

    public void securityNegotiationStarted(
        CallPeerSecurityNegotiationStartedEvent securityStartedEvent) {}

    public void securityPending() {}

    public void securityTimeout(CallPeerSecurityTimeoutEvent evt) {}

    public void setSecurityPanelVisible(boolean visible) {}

    public void securityOff(CallPeerSecurityOffEvent evt) {}

    public void securityOn(CallPeerSecurityOnEvent evt) {}
}