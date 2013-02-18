/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.account;

import android.graphics.*;
import android.view.*;
import android.widget.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.account.*;
import org.jitsi.*;
import org.jitsi.android.gui.util.*;
import org.jitsi.service.osgi.*;

import java.util.*;

/**
 * Activity allows user to set presence status and status message
 * 
 * @author Pawel Domas
 */
public class PresenceStatusActivity
    extends OSGiActivity
{
    /**
     * Intent's extra's key for account ID property of this activity
     */
    static public final String INTENT_ACCOUNT_ID = "account_id";
    /**
     * The logger used by this class
     */
    static final private Logger logger =
            Logger.getLogger(PresenceStatusActivity.class);
    /**
     * The account's {@link ProtocolProviderService}
     */
    private ProtocolProviderService accountProvider;
    /**
     * The account's {@link OperationSetPresence}
     * used to perform presence operations
     */
    private OperationSetPresence accountPresence;
    /**
     * The {@link StatusListAdapter} used as status list adapter
     */
    private StatusListAdapter statusAdapter;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // Set the main layout
        setContentView(R.layout.presence_status);
        // Get account ID from intent extras
        String accountID = getIntent().getStringExtra(INTENT_ACCOUNT_ID);
        // Find account for given account ID
        AccountID account = AccountUtils.getAccountForID(accountID);
        if(account == null)
        {
            logger.error("No account found for: "+accountID);
            finish();
            return;
        }
        // Get the protocol provider for this account
        this.accountProvider =
                AccountUtils.getRegisteredProviderForAccount(account);
        if(accountProvider == null)
        {
            logger.error("No provider registered for account: "+account);
            finish();
            return;
        }
        // Get the presence operation set
        this.accountPresence =
                accountProvider.getOperationSet(OperationSetPresence.class);
        if(accountPresence == null)
        {
            logger.error("Presence is not supported by "
                         + accountProvider.getProtocolDisplayName());
        }
        // Initialize view with current values
        initPresenceStatus();
    }

    /**
     * Create and initialize the view with actual values
     */
    private void initPresenceStatus()
    {
        // Create spinner with status list
        Spinner statusSpinner = (Spinner) findViewById(
                R.id.presenceStatusSpinner);
        // Create list adapter
        Iterator<PresenceStatus> statusIter =
                accountPresence.getSupportedStatusSet();
        statusAdapter = new StatusListAdapter( statusIter,
                getLayoutInflater());
        statusSpinner.setAdapter(statusAdapter);
        // Selects current status
        statusSpinner.setSelection(
                statusAdapter.getPositionForItem(
                        accountPresence.getPresenceStatus()));
        // Status edit
        EditText statusEdit = (EditText) findViewById(
                R.id.presenceStatusMessageEdit);
        // Sets current status message
        statusEdit.setText(accountPresence.getCurrentStatusMessage());
    }

    /**
     * Method mapped to the "set status" button's on click event
     *
     * @param btnView clicked button View
     */
    public void onSetStatusButtonClicked(View btnView)
    {
        Spinner statusSpinner = (Spinner) findViewById(
                R.id.presenceStatusSpinner);
        EditText statusMessageEdit = (EditText) findViewById(
                R.id.presenceStatusMessageEdit);

        PresenceStatus selectedStatus =
                (PresenceStatus) statusSpinner.getSelectedItem();
        String statusText = statusMessageEdit.getText().toString();

        // Publish status in new thread
        publishStatus(selectedStatus, statusText);

        // Close this dialog
        finish();
    }

    /**
     * Method starts a new Thread and publishes the status
     *
     * @param status {@link PresenceStatus} to be set
     * @param text the status message
     */
    private void publishStatus(final PresenceStatus status, final String text)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    // Try to publish selected status
                    accountPresence.publishPresenceStatus(status, text);
                }
                catch (OperationFailedException e)
                {
                    logger.error(e);

                    AndroidUtils.showAlertDialog(
                            getBaseContext(),
                            "Error",
                            "An error occured while setting the status: "
                                    + e.getLocalizedMessage());
                }
            }
        }).start();
    }

    /**
     * Class responsible for creating the Views
     * for a given set of {@link PresenceStatus}
     */
    class StatusListAdapter
        extends IteratorAdapter<PresenceStatus>
    {
        /**
         * Creates new instance of {@link StatusListAdapter}
         *
         * @param objects {@link Iterator} for a set of {@link PresenceStatus}
         *
         * @param layoutInflater {@link LayoutInflater} used to create new Views
         */
        public StatusListAdapter( Iterator<PresenceStatus> objects,
                                  LayoutInflater layoutInflater)
        {
            super(objects, layoutInflater);
        }

        @Override
        protected View getView( PresenceStatus item,
                                ViewGroup parent,
                                LayoutInflater inflater)
        {

            // Retrieve views
            View statusItemView = inflater.inflate(
                    R.layout.presence_status_row, parent, false);
            TextView statusNameView = (TextView) statusItemView.findViewById(
                    R.id.presenceStatusNameView);
            ImageView statusIconView = (ImageView) statusItemView.findViewById(
                    R.id.presenceStatusIconView);

            // Set status name
            String statusName = item.getStatusName();
            statusNameView.setText(statusName);

            // Set status icon
            Bitmap presenceIcon =
                    AndroidImageUtil.bitmapFromBytes(
                            item.getStatusIcon());
            statusIconView.setImageBitmap(presenceIcon);

            return statusItemView;
        }

        /**
         * Find the position of <tt>status</tt> in adapter's list
         *
         * @param status the {@link PresenceStatus} for which
         *  the position is returned
         *
         * @return index of <tt>status</tt> in adapter's list
         */
        int getPositionForItem(PresenceStatus status)
        {
            for(int i=0; i < getCount(); i++)
            {
                PresenceStatus other = (PresenceStatus) getItem(i);
                if(other.equals(status))
                    return i;
            }
            return -1;
        }
    }
}
