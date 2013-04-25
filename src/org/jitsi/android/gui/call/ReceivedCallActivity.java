/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.call;

import net.java.sip.communicator.util.*;
import org.jitsi.*;
import org.jitsi.android.*;
import org.jitsi.service.osgi.*;

import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.call.*;

/**
 * The <tt>ReceivedCallActivity</tt> is the activity that corresponds to the
 * screen shown on incoming call.
 *
 * @author Yana Stamcheva
 */
public class ReceivedCallActivity
    extends OSGiActivity
{
    /**
     * The logger
     */
    private final static Logger logger =
            Logger.getLogger(ReceivedCallActivity.class);

    /**
     * The identifier of the call.
     */
    private String callIdentifier;

    /**
     * The corresponding call.
     */
    private Call call;

    /**
     * Called when the activity is starting. Initializes the call identifier.
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

        setContentView(R.layout.received_call);

        TextView displayNameView
            = (TextView) findViewById(R.id.calleeDisplayName);
        TextView addressView
            = (TextView) findViewById(R.id.calleeAddress);

        ImageView avatarView
            = (ImageView) findViewById(R.id.calleeAvatar);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            String displayName
                = extras.getString(CallManager.CALLEE_DISPLAY_NAME);

            if (displayName != null)
                displayNameView.setText(displayName);

            String address = extras.getString(CallManager.CALLEE_ADDRESS);
            if (address != null)
                addressView.setText(address);

            byte[] avatar = extras.getByteArray(CallManager.CALLEE_AVATAR);
            if (avatar != null)
            {
                Bitmap bitmap
                    = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
                avatarView.setImageBitmap(bitmap);
            }

            callIdentifier = extras.getString(CallManager.CALL_IDENTIFIER);

            call = CallManager.getActiveCall(callIdentifier);
        }

        ImageView hangupView = (ImageView) findViewById(R.id.hangupButton);

        hangupView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (call != null)
                {
                    CallManager.hangupCall(call);

                    switchActivity(
                            JitsiApplication.getHomeScreenActivityClass());
                }
            }
        });

        final ImageView callButton = (ImageView) findViewById(R.id.callButton);

        callButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (call != null)
                {
                    answerCall(call, false);
                }
            }
        });
    }

    /**
     * Method mapped to answer button's onClick event
     *
     * @param v the answer with video button's <tt>View</tt>
     */
    public void onAnswerWithVideoClicked(View v)
    {
        if(call != null)
        {
            logger.trace("Answer call with video");
            answerCall(call, true);
        }
    }

    /**
     * Answers the given call and launches the call user interface.
     *
     * @param call the call to answer
     * @param useVideo indicates if video shall be used
     */
    private void answerCall(final Call call, boolean useVideo)
    {
        CallManager.answerCall(call, useVideo);

        runOnUiThread(new Runnable()
        {
            public void run()
            {
                Intent videoCall
                        = VideoCallActivity
                                .createVideoCallIntent(
                                        ReceivedCallActivity.this,
                                        callIdentifier);
                startActivity(videoCall);
                finish();
            }
        });
    }
}

