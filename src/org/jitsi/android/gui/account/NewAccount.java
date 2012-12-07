/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.account;

import net.java.sip.communicator.service.gui.*;
import org.jitsi.*;
import org.jitsi.android.gui.*;
import org.jitsi.android.gui.call.*;
import org.jitsi.service.osgi.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class NewAccount
    extends OSGiActivity
{
    private BundleContext bundleContext;

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
        spinner.setAdapter(adapter);
    }

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
                    Intent callContactIntent
                        = new Intent(NewAccount.this, CallContact.class);

                    NewAccount.this.startActivity(callContactIntent);
                }
            }
        });
    }

    private ProtocolProviderService signIn( String userName,
                                            String password,
                                            String protocolName)
    {
        Logger logger = Logger.getLogger(Jitsi.class);

        ServiceReference[] accountWizardRefs = null;
        try
        {
            accountWizardRefs = bundleContext.getServiceReferences(
                AccountRegistrationForm.class.getName(),
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
                AccountRegistrationForm accReg
                    = (AccountRegistrationForm) bundleContext
                        .getService(accountWizardRefs[i]);

                if (accReg.getProtocolName().equals(protocolName))
                {
                    try
                    {
                        return accReg.signin(userName, password);
                    }
                    catch (OperationFailedException e)
                    {
                        if (logger.isDebugEnabled())
                            logger.debug("The sign in operation has failed.");

                        if (e.getErrorCode()
                            == OperationFailedException.ILLEGAL_ARGUMENT)
                        {
                            
                        }
                        else if (e.getErrorCode()
                            == OperationFailedException.IDENTIFICATION_CONFLICT)
                        {
                            
                        }
                        else if (e.getErrorCode()
                            == OperationFailedException.SERVER_NOT_SPECIFIED)
                        {
                            
                        }
                    }
                }
            }
        }
        return null;
    }
}
