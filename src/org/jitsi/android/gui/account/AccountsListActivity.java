/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.account;

import android.content.*;
import android.graphics.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.account.*;
import org.jitsi.R;
import org.jitsi.android.gui.*;
import org.jitsi.android.gui.menu.*;
import org.jitsi.android.gui.util.*;
import org.osgi.framework.*;

import android.view.*;
import android.widget.*;

import java.beans.*;
import java.util.*;

/**
 * The activity display list of currently stored accounts
 * showing it's protocol and current status.
 *
 * @author Pawel Domas
 */
public class AccountsListActivity
    extends MainMenuActivity
    implements AdapterView.OnItemClickListener,
        ServiceListener
{
    /**
     * The logger
     */
    private static final Logger logger =
            Logger.getLogger(AccountsListActivity.class);

    private BundleContext bundleContext;

    /**
     * The list adapter for accounts
     */
    private AccountListAdapter listAdapter;

    @Override
    protected synchronized void start(BundleContext bundleContext)
        throws Exception
    {
        /*
         * If there are unit tests to be run, do not run anything else and just
         * perform the unit tests.
         */
        if (System.getProperty(
                    "net.java.sip.communicator.slick.runner.TEST_LIST")
                != null)
            return;

        this.bundleContext = bundleContext;
        //Registers service listener
        bundleContext.addServiceListener(this);
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.account_list);

        accountsInit();
    }

    @Override
    protected void onDestroy()
    {
        // Unregisters presence status listeners
        if(listAdapter != null)
        {
            listAdapter.deinitStatusListeners();
        }
        super.onDestroy();
    }

    /**
     * Initializes the accounts table.
     */
    private void accountsInit()
    {
        /*AccountManager accountManager = AccountManager.get(this);

        Account[] accountsList
            = accountManager.getAccountsByType(
                getString(R.string.ACCOUNT_TYPE));

        for (Account account : accountsList)
        {
            addAccountView(account);
        }*/

        // Create accounts array
        Collection<AccountID> accountIDCollection =
                AccountUtils.getStoredAccounts();
        AccountID[] accounts = new AccountID[accountIDCollection.size()];
        accountIDCollection.toArray(accounts);
        // Create account list adapter
        listAdapter = new AccountListAdapter(
                            getBaseContext(),
                            accounts,
                            getLayoutInflater());
        // Puts the adapter into accounts ListView
        ListView lv = (ListView)findViewById(R.id.accountListView);
        lv.setAdapter(listAdapter);
        lv.setOnItemClickListener(this);
    }

    /**
     * Account clicked callback for displayed list.
     *
     * @param adapterView
     * @param view
     * @param position
     * @param l
     */
    public void onItemClick(AdapterView<?> adapterView,
                            View view,
                            int position,
                            long l)
    {
        AccountID account = listAdapter.getItem(position);

        Intent statusIntent = new Intent( getBaseContext(),
                                          PresenceStatusActivity.class);
        statusIntent.putExtra( PresenceStatusActivity.INTENT_ACCOUNT_ID,
                               account.getAccountUniqueID());

        startActivity(statusIntent);
    }

    /**
     * Adds or removes account when a <tt>ProtocolProviderService</tt> has
     * been registered or removed.
     * 
     * @param event the <tt>ServiceEvent</tt> that notified us about the change
     */
    public void serviceChanged(ServiceEvent event)
    {
        // if the event is caused by a bundle being stopped, we don't want to
        // know
        if (event.getServiceReference().getBundle().getState()
                == Bundle.STOPPING)
        {
            return;
        }
        Object sourceService =
                bundleContext.getService(event.getServiceReference());

        // we don't care if the source service is not a protocol provider
        if (!(sourceService instanceof ProtocolProviderService))
        {
            return;
        }

        ProtocolProviderService protocolProvider
                = (ProtocolProviderService) sourceService;

        // Add or remove the protocol provider from our accounts list.
        if (event.getType() == ServiceEvent.REGISTERED &&
                listAdapter != null)
        {
            listAdapter.addAccount(protocolProvider);
        }
        else if (event.getType() == ServiceEvent.UNREGISTERING &&
                listAdapter != null)
        {
            listAdapter.removeAccount(protocolProvider);
        }
    }

    /**
     * Class responsible for creating list row Views
     */
    class AccountListAdapter
            extends ArrayAdapter<AccountID>
            implements ProviderPresenceStatusListener,
                RegistrationStateChangeListener
    {

        /**
         * The inflater used to create views
         */
        private final LayoutInflater inflater;
        /**
         * Map storing operation sets if they were returned for given account
         */
        private Map<AccountID, OperationSetPresence> presenceOpSetMap;
        /**
         * Map storing protocl providers for each account
         */
        private Map<AccountID, ProtocolProviderService> providersMap;
        /**
         * Array of accounts
         */
        //private AccountID[] accounts;

        /**
         * Creates new instance of {@link AccountListAdapter}
         * @param context the current {@link Context}
         * @param accounts array of currently stored accounts
         * @param inflater the {@link LayoutInflater} which
         *  will be used to create new {@link View}s
         */
        public AccountListAdapter(
                Context context,
                AccountID[] accounts,
                LayoutInflater inflater)
        {
           super(context, R.layout.account_list_row, accounts);
           this.inflater = inflater;
           //this.accounts = accounts;
           initStatusListeners();
        }

        /**
         * Registers status update listeners for all accounts
         */
        private void initStatusListeners()
        {
            presenceOpSetMap = new HashMap<AccountID, OperationSetPresence>();
            providersMap = new HashMap<AccountID, ProtocolProviderService>();

            for(int accIdx=0; accIdx<getCount(); accIdx++)
            {
                AccountID account = getItem(accIdx);

                registerStatusListeners(account);
            }
        }

        /**
         * Registers {@link ProviderPresenceStatusListener} and
         *  {@link RegistrationStateChangeListener} for given <tt>account</tt>
         *
         * @param account
         */
        private void registerStatusListeners(AccountID account)
        {
            ProtocolProviderService protocolProvider =
                    AccountUtils.getRegisteredProviderForAccount(account);
            if(protocolProvider == null)
            {
                logger.debug(
                        "No protocol provider returned for " +
                                account.getDisplayName());
                return;
            }
            OperationSetPresence presenceOpSet =
                    AccountStatusUtils.getProtocolPresenceOpSet(
                            protocolProvider);
            logger.debug("Registering for regstate from: "
                + account.getDisplayName());
            protocolProvider.addRegistrationStateChangeListener(this);
            providersMap.put(account, protocolProvider);
            if(presenceOpSet == null)
            {
                logger.debug(
                        "No presence opset returned for " +
                                account.getDisplayName());
                return;
            }
            presenceOpSetMap.put(account, presenceOpSet);
            logger.debug("Registering for presence updates from: "
                + account.getDisplayName());
            presenceOpSet.addProviderPresenceStatusListener(this);
        }

        /**
         * Unregisters status update listeners for accounts
         */
        private void deinitStatusListeners()
        {
            for(int accIdx=0; accIdx < getCount(); accIdx++)
            {
                AccountID account = getItem(accIdx);
                unregisterstatusListeners(account);
            }
        }

        /**
         * Unregisters {@link PresenceStatusListener} and
         *  {@link RegistrationStateChangeListener} fro given <tt>account</tt>
         *
         * @param account
         */
        private void unregisterstatusListeners(AccountID account)
        {
            OperationSetPresence pOpSet = presenceOpSetMap.get(account);
            if(pOpSet != null)
            {
                logger.debug("Unregistering: "+pOpSet);
            }
            ProtocolProviderService provider = providersMap.get(account);
            if(provider != null)
            {
                logger.debug("Unregistering: "+provider);
            }
        }

        public View getView(int i, View view, ViewGroup viewGroup)
        {
            Logger logger = Logger.getLogger(Jitsi.class);

            AccountID account = getItem(i);

            View statusItem = inflater.inflate(
                    R.layout.account_list_row, viewGroup, false);

            TextView accountName =
                    (TextView) statusItem.findViewById(R.id.accountName);
            ImageView accountProtocol =
                    (ImageView) statusItem.findViewById(R.id.accountProtoIcon);
            ImageView statusIconView =
                   (ImageView) statusItem.findViewById(R.id.accountStatusIcon);
            TextView accountStatus =
                    (TextView) statusItem.findViewById(R.id.accountStatus);

            // Sets the account name
            accountName.setText(account.getDisplayName());

            ProtocolProviderService protocolProvider =
                    AccountUtils.getRegisteredProviderForAccount(account);
            if(protocolProvider != null)
            {
                PresenceStatus presenceStatus = presenceOpSetMap.get(account).
                        getPresenceStatus();

                // Sets the account status
                String statusName = presenceStatus.getStatusName();
                accountStatus.setText(statusName);
                logger.debug(account.getDisplayName()+
                        " have status: "+statusName);
                logger.debug(account.getDisplayName()+
                        " have registration status: "+
                        protocolProvider.getRegistrationState().getStateName());

                // Sets the protocol icon
                byte[] protocolBlob =
                        protocolProvider.getProtocolIcon().getIcon(
                                ProtocolIcon.ICON_SIZE_48x48);
                Bitmap protocolIcon =
                        AndroidImageUtil.bitmapFromBytes(protocolBlob);
                if(protocolIcon != null)
                    accountProtocol.setImageBitmap(protocolIcon);

                // Sets the status icon
                byte[] statusBlob = presenceStatus.getStatusIcon();
                Bitmap statusIcon = AndroidImageUtil.bitmapFromBytes(statusBlob);
                if(statusIcon != null)
                {
                    statusIconView.setImageBitmap(statusIcon);
                }
            }

            return statusItem;
        }

        /**
         * Check if given <tt>account</tt> exists on the list
         * @param account
         * @return <tt>true</tt> if account is on the list
         */
        private boolean containsAccountID(AccountID account)
        {
            for(int i=0; i<getCount(); i++)
            {
                if(getItem(i).equals(account))
                    return true;
            }
            return false;
        }

        /**
         * Adds new account to the list
         * @param protocolProvider
         */
        public void addAccount(ProtocolProviderService protocolProvider)
        {
            AccountID account = protocolProvider.getAccountID();
            logger.debug("Account added: " + account.getDisplayName());

            if(!containsAccountID(account))
                add(account);
            registerStatusListeners(account);

            doRefreshList();
        }

        /**
         * Removes the account from the list
         * @param protocolProvider
         */
        public void removeAccount(ProtocolProviderService protocolProvider)
        {
            AccountID account = protocolProvider.getAccountID();
            logger.debug("Account removed: "+
                    account.getDisplayName());

            unregisterstatusListeners(account);
            remove(account);

            doRefreshList();
        }

        /**
         * Runs list change notification on the UI thread
         */
        private void doRefreshList()
        {
            runOnUiThread(new Runnable() {
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        public void providerStatusChanged(ProviderPresenceStatusChangeEvent evt)
        {
            logger.debug("Received presence update "+
                    evt.getNewStatus().getStatusName()+
                    " from "+evt.getProvider().getAccountID().getDisplayName());
            doRefreshList();
        }

        public void providerStatusMessageChanged(PropertyChangeEvent evt)
        {
            // Ignore status messages
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt)
        {
            logger.debug("Received registration update "+
                    evt.getNewState().getStateName()+
                    " from "+evt.getProvider().getAccountID().getDisplayName());
            doRefreshList();
        }
    }
}
