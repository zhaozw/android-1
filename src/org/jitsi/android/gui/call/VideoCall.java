/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.call;

import java.util.*;

//import net.java.sip.communicator.service.neomedia.*;
import org.jitsi.service.osgi.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.call.*;
import net.java.sip.communicator.util.call.CallPeerAdapter;
//import net.java.sip.communicator.util.event.*;

import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.jitsi.*;

public class VideoCall
    extends OSGiActivity
    implements  CallPeerRenderer,
                CallRenderer
{
    private ViewGroup remoteVideoContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.video_call);

        TextView calleeName = (TextView) findViewById(R.id.calleeName);

        final Call call = CallManager.getActiveCalls().next();

        if (call != null)
        {
            Iterator<? extends CallPeer> callPeerIter
                = call.getCallPeers();

            if (callPeerIter.hasNext())
            {
                CallPeer callPeer = callPeerIter.next();

                CallPeerAdapter callPeerAdapter
                    = new CallPeerAdapter(callPeer, this);
                callPeer.addCallPeerListener(callPeerAdapter);
                callPeer.addCallPeerSecurityListener(callPeerAdapter);
                callPeer.addPropertyChangeListener(callPeerAdapter);

                addVideoListener(callPeer);
                calleeName.setText(calleeName.getText()
                    + " " + callPeer.getDisplayName());
            }
        }

        ImageView hangupView = (ImageView) findViewById(R.id.hangupButton);

        hangupView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                CallManager.hangupCall(call);

                Intent callContactIntent
                    = new Intent(VideoCall.this, CallContact.class);

                VideoCall.this.startActivity(callContactIntent);
            }
        });

        SurfaceView previewDisplay
            = (SurfaceView) findViewById(R.id.previewDisplay);

        if (previewDisplay != null)
        {
//            previewDisplay.getHolder().addCallback(
//                    new SurfaceHolder.Callback()
//                    {
//                        public void surfaceChanged(
//                                SurfaceHolder holder,
//                                int format,
//                                int width, int height)
//                        {
//                            // TODO Auto-generated method stub
//                        }
//
//                        public void surfaceCreated(SurfaceHolder holder)
//                        {
//                            /*
//                             * TODO Setting a static previewDisplay on the
//                             * MediaRecorder DataSource is a workaround which
//                             * allows not changing the
//                             * OperationSetVideoTelephony and related APIs.
//                             */
//                            net.java.sip.communicator.impl.neomedia.jmfext.media
//                                    .protocol.mediarecorder.DataSource
//                                            .setDefaultPreviewDisplay(
//                                                    holder.getSurface());
//                        }
//
//                        public void surfaceDestroyed(SurfaceHolder holder)
//                        {
//                            /*
//                             * TODO Setting a static previewDisplay on the
//                             * MediaRecorder DataSource is a workaround which
//                             * allows not changing the
//                             * OperationSetVideoTelephony and related APIs.
//                             */
//                            net.java.sip.communicator.impl.neomedia.jmfext.media
//                                    .protocol.mediarecorder.DataSource
//                                            .setDefaultPreviewDisplay(
//                                                    null);
//                        }
//                    });
        }

        remoteVideoContainer
            = (ViewGroup) findViewById(R.id.remoteVideoContainer);
    }

//    public void handleVideoEvent(CallPeer callPeer,
//                                 final VideoEvent event)
//    {
//        if (!event.isConsumed() && (event.getOrigin() == VideoEvent.REMOTE))
//        {
//            ProtocolProviderService pps = callPeer.getProtocolProvider();
//            final Component visualComponent;
//
//            if (pps != null)
//            {
//                OperationSetVideoTelephony osvt
//                    = pps.getOperationSet(OperationSetVideoTelephony.class);
//
//                if (osvt != null)
//                    visualComponent = osvt.getVisualComponent(callPeer);
//                else
//                    visualComponent = null;
//            }
//            else
//                visualComponent = null;
//
//            if (remoteVideoContainer != null)
//            {
//                event.consume();
//
//                remoteVideoContainer.post(
//                    new Runnable()
//                    {
//                        public void run()
//                        {
//                            View view = null;
//
//                            if (visualComponent instanceof ViewAccessor)
//                            {
//                                view
//                                    = ((ViewAccessor) visualComponent)
//                                            .getView(VideoCall.this);
//                            }
//
//                            int width = -1;
//                            int height = -1;
//
//                            DisplayMetrics displaymetrics = new DisplayMetrics();
//                            getWindowManager().getDefaultDisplay()
//                                .getMetrics(displaymetrics);
//                            int viewHeight = displaymetrics.heightPixels;
//                            int viewWidth = displaymetrics.widthPixels;
//
//                            if (view != null)
//                            {
//                                /*
//                                 * If the visualComponent displaying the
//                                 * video of the remote callPeer has a
//                                 * preferredSize, attempt to respect it.
//                                 */
//                                Dimension preferredSize
//                                    = visualComponent.getPreferredSize();
//
//                                if ((preferredSize != null)
//                                        && (preferredSize.width > 0)
//                                        && (preferredSize.height > 0))
//                                {
//                                    width = preferredSize.width;
//                                    height = preferredSize.height;
//                                }
//                                else if (event instanceof SizeChangeVideoEvent)
//                                {
//                                    /*
//                                     * The SizeChangeVideoEvent may have
//                                     * been delivered with a delay and thus
//                                     * may not represent the up-to-date size
//                                     * of the remote video. But since the
//                                     * visualComponent does not have a
//                                     * preferredSize, anything like the size
//                                     * reported by the SizeChangeVideoEvent
//                                     * may be used as a hint.
//                                     */
//                                    SizeChangeVideoEvent scve
//                                        = (SizeChangeVideoEvent) event;
//
//                                    if ((scve.getHeight() > 0)
//                                            && (scve.getWidth() > 0))
//                                    {
//                                        height = scve.getHeight();
//                                        width = scve.getWidth();
//                                    }
//                                }
//                            }
//
//                            remoteVideoContainer.removeAllViews();
//
//                            if (view != null)
//                            {
//                                float ratio = width / (float) height;
//
//                                if (height < viewHeight)
//                                {
//                                    height = viewHeight;
//                                    width = (int) (height*ratio);
//                                }
//
//                                remoteVideoContainer.addView(
//                                        view,
//                                        new ViewGroup.LayoutParams(
//                                                width,
//                                                height));
//                            }
//                        }
//                    });
//            }
//        }
//    }

    private void addVideoListener(final CallPeer callPeer)
    {
        ProtocolProviderService pps = callPeer.getProtocolProvider();

        if (pps == null)
            return;

        OperationSetVideoTelephony osvt
            = pps.getOperationSet(OperationSetVideoTelephony.class);

        if (osvt == null)
            return;

//        osvt.addVideoListener(
//                callPeer,
//                new VideoListener()
//                {
//                    public void videoAdded(VideoEvent event)
//                    {
//                        handleVideoEvent(callPeer, event);
//                    }
//
//                    public void videoRemoved(VideoEvent event)
//                    {
//                        handleVideoEvent(callPeer, event);
//                    }
//
//                    public void videoUpdate(VideoEvent event)
//                    {
//                        handleVideoEvent(callPeer, event);
//                    }
//                });
    }

    public void setPeerName(final String name)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                TextView calleeName = (TextView) findViewById(R.id.calleeName);

                calleeName.setText(name);
            }
        });
    }

    public void setPeerImage(byte[] image)
    {
        
    }

    public void setPeerState(final String state)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                TextView statusName = (TextView) findViewById(R.id.callStatus);

                statusName.setText(state);
            }
        });
    }


    public void setTime(final Date time)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                TextView callTime = (TextView) findViewById(R.id.callTime);

                String timeText = GuiUtils.formatTime(time);

                callTime.setText(timeText);
            }
        });
    }

    public void setErrorReason(String reason)
    {
        
    }

    public void setMute(boolean isMute)
    {
        // TODO Auto-generated method stub
        
    }

    public void setOnHold(boolean isOnHold)
    {
        // TODO Auto-generated method stub
        
    }

    public void securityOn(CallPeerSecurityOnEvent evt)
    {
        // TODO Auto-generated method stub
        
    }

    public void securityOff(CallPeerSecurityOffEvent evt)
    {
        // TODO Auto-generated method stub
        
    }

    public void setCallPeerAdapter(CallPeerAdapter adapter)
    {
        // TODO Auto-generated method stub
        
    }

    public CallPeerAdapter getCallPeerAdapter()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void printDTMFTone(char dtmfChar)
    {
        // TODO Auto-generated method stub
        
    }

    public CallRenderer getCallRenderer()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setLocalVideoVisible(boolean isVisible)
    {
        // TODO Auto-generated method stub
        
    }

    public boolean isLocalVideoVisible()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public Call getCall()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void enterFullScreen()
    {
        // TODO Auto-generated method stub
        
    }

    public void exitFullScreen()
    {
        // TODO Auto-generated method stub
        
    }

//TODO    public void ensureSize(Component component, int width, int height)
//    {
//
//    }

    public CallPeerRenderer getCallPeerRenderer(CallPeer callPeer)
    {
        return this;
    }

    public void conferenceMemberAdded(CallPeer callPeer,
        ConferenceMember conferenceMember)
    {
        
    }

    public void conferenceMemberRemoved(CallPeer callPeer,
        ConferenceMember conferenceMember)
    {
        
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
}
