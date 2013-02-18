/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.account;

import java.util.*;

import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

import org.jitsi.*;
import org.jitsi.android.gui.*;
import org.jitsi.android.gui.call.*;
import org.jitsi.android.gui.menu.*;
import org.jitsi.android.gui.util.*;
import org.jitsi.service.resources.*;
import org.osgi.framework.*;

import android.accounts.*;
import android.accounts.AccountManager;
import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

/**
 * The <tt>AccountLoginActivity</tt> is the activity responsible for creating
 * a new account.
 *
 * @author Yana Stamcheva
 */
public class AccountLoginActivity
    extends MainMenuActivity
{
    /**
     * The osgi bundle context.
     */
    private BundleContext bundleContext;

    /**
     * The resource management service.
     */
    private static ResourceManagementService resourcesService;

    /**
     * The username property name.
     */
    public static final String USERNAME = "Username";

    /**
     * The password property name.
     */
    public static final String PASSWORD = "Password";

    /**
     * Starts this osgi activity.
     *
     * @param bundleContext the osgi <tt>BundleContext</tt>
     * @throws Exception
     */
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

        runOnUiThread(new Runnable()
        {
            public void run()
            {
                initSignInButton();
            }
        });
    }

    /**
     * Returns the <tt>ResourceManagementService</tt>, through which we will
     * access all resources.
     *
     * @return the <tt>ResourceManagementService</tt>, through which we will
     * access all resources.
     */
    private ResourceManagementService getResourceService()
    {
        if (resourcesService == null)
        {
            resourcesService
                = ServiceUtils.getService(
                        bundleContext,
                        ResourceManagementService.class);
        }
        return resourcesService;
    }

    /**
     * Called when the activity is starting. Initializes the corresponding
     * call interface.
     *
     * @param savesInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle).
     * Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_account);

        final Spinner spinner = (Spinner) findViewById(R.id.networkSpinner);
        ArrayAdapter<CharSequence> adapter
            = ArrayAdapter.createFromResource(
                                        this,
                                        R.array.networks_array,
                                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            String username = extras.getString(USERNAME);

            if (username != null && username.length() > 0)
            {
                EditText usernameField
                    = (EditText) findViewById(R.id.usernameField);

                usernameField.setText(username);
            }

            String password = getIntent().getExtras().getString(PASSWORD);

            if (password != null && password.length() > 0)
            {
                EditText passwordField
                    = (EditText) findViewById(R.id.passwordField);

                passwordField.setText(username);
            }
        }

        spinner.setAdapter(adapter);
    }

    /**
     * Initializes the sign in button.
     */
    private void initSignInButton()
    {
        final Button signInButton = (Button) findViewById(R.id.signInButton);
        signInButton.setEnabled(true);

        signInButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                final Spinner spinner
                    = (Spinner) findViewById(R.id.networkSpinner);
                final EditText userNameField
                    = (EditText) findViewById(R.id.usernameField);
                final EditText passwordField
                    = (EditText) findViewById(R.id.passwordField);

                Object selectedNetwork = spinner.getSelectedItem();

                ProtocolProviderService protocolProvider
                    = signIn( userNameField.getText().toString(),
                        passwordField.getText().toString(),
                        selectedNetwork.toString());

                if (protocolProvider != null)
                {
                    //addAndroidAccount(protocolProvider);

                    Intent callContactIntent
                        = new Intent(   AccountLoginActivity.this,
                                        CallContactActivity.class);

                    AccountLoginActivity.this.startActivity(callContactIntent);
                }
            }
        });
    }

    /**
     * Sign in the account with the given <tt>userName</tt>, <tt>password</tt>
     * and <tt>protocolName</tt>.
     *
     * @param userName the username of the account
     * @param password the password of the account
     * @param protocolName the name of the protocol
     * @return the <tt>ProtocolProviderService</tt> corresponding to the newly
     * signed in account
     */
    private ProtocolProviderService signIn( String userName,
                                            String password,
                                            String protocolName)
    {
        ProtocolProviderService protocolProvider = null;

        Logger logger = Logger.getLogger(Jitsi.class);

        ServiceReference<?>[] accountWizardRefs = null;
        try
        {
            accountWizardRefs = bundleContext.getServiceReferences(
                AccountRegistrationWizard.class.getName(),
                null);
        }
        catch (InvalidSyntaxException ex)
        {
            // this shouldn't happen since we're providing no parameter string
            // but let's log just in case.
            logger.error(
                "Error while retrieving service refs", ex);
        }

        // in case we found any, add them in this container.
        if (accountWizardRefs != null)
        {
            if (logger.isDebugEnabled())
                logger.debug("Found "
                    + accountWizardRefs.length
                    + " already installed providers.");

            for (int i = 0; i < accountWizardRefs.length; i++)
            {
                AccountRegistrationWizard accReg
                    = (AccountRegistrationWizard) bundleContext
                        .getService(accountWizardRefs[i]);

                if (accReg.getProtocolName().equals(protocolName))
                {
                    try
                    {
                        protocolProvider = accReg.signin(userName, password);
                    }
                    catch (OperationFailedException e)
                    {
                        e.printStackTrace(System.err);

                        if (logger.isDebugEnabled())
                            logger.debug("The sign in operation has failed.");

                        if (e.getErrorCode()
                                == OperationFailedException.ILLEGAL_ARGUMENT)
                        {
                            AndroidUtils.showAlertDialog(
                                this,
                                R.string.service_gui_LOGIN_FAILED,
                                R.string.service_gui_USERNAME_NULL);
                        }
                        else if (e.getErrorCode()
                                == OperationFailedException
                                    .IDENTIFICATION_CONFLICT)
                        {
                            AndroidUtils.showAlertDialog(
                                this,
                                R.string.service_gui_LOGIN_FAILED,
                                R.string.service_gui_USER_EXISTS_ERROR);
                        }
                        else if (e.getErrorCode()
                                == OperationFailedException
                                    .SERVER_NOT_SPECIFIED)
                        {
                            AndroidUtils.showAlertDialog(
                                this,
                                R.string.service_gui_LOGIN_FAILED,
                                R.string.service_gui_SPECIFY_SERVER);
                        }
                        else
                        {
                            AndroidUtils.showAlertDialog(
                                this,
                                R.string.service_gui_LOGIN_FAILED,
                                R.string.service_gui_ACCOUNT_CREATION_FAILED);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace(System.err);
                        new AlertDialog.Builder(this).setIcon(R.drawable.icon)
                            .setTitle("Warning")
                            .setMessage(getResourceService().getI18NString(
                            "service.gui.ACCOUNT_CREATION_FAILED"))
                                .setNeutralButton("Close", null).show();
                    }
                }
            }
        }
        return protocolProvider;
    }

    /**
     * Stores the given <tt>protocolProvider</tt> data in the android system
     * accounts.
     *
     * @param protocolProvider the <tt>ProtocolProviderService</tt>,
     * corresponding to the account to store
     */
    private void storeAndroidAccount(ProtocolProviderService protocolProvider)
    {
        Map<String, String> accountProps
            = protocolProvider.getAccountID().getAccountProperties();

        String username = accountProps.get(ProtocolProviderFactory.USER_ID);

        Account account
            = new Account(  username,
                            getString(R.string.ACCOUNT_TYPE));

        final Bundle extraData = new Bundle();
        Iterator<String> propKeys = accountProps.keySet().iterator();
        while (propKeys.hasNext())
        {
            String key = propKeys.next();
            extraData.putString(key, accountProps.get(key));
        }

        AccountManager am = AccountManager.get(this);
        boolean accountCreated
            = am.addAccountExplicitly(
                account,
                accountProps.get(ProtocolProviderFactory.PASSWORD), extraData);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            if (accountCreated)
            {  //Pass the new account back to the account manager
                AccountAuthenticatorResponse response
                    = extras.getParcelable(
                        AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

                Bundle result = new Bundle();
                result.putString(   AccountManager.KEY_ACCOUNT_NAME,
                                    username);
                result.putString(   AccountManager.KEY_ACCOUNT_TYPE,
                                    getString(R.string.ACCOUNT_TYPE));
                result.putAll(extraData);

                response.onResult(result);
            }
            finish();
        }
    }
}
