package edu.msu.chuppthe.steampunked.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import edu.msu.chuppthe.steampunked.utility.Preferences;
import edu.msu.chuppthe.steampunked.R;
import edu.msu.chuppthe.steampunked.utility.Cloud;

public class RegisterDlg extends DialogFragment {

    private AlertDialog dlg;

    private Preferences preferences;

    /**
     * Create the dialog box
     *
     * @param savedInstanceState The saved instance bundle
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the title
        builder.setTitle(R.string.register);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Pass null as the parent view because its going in the dialog layout
        @SuppressLint("InflateParams")
        final View view = inflater.inflate(R.layout.register_dlg, null);
        builder.setView(view);

        preferences = new Preferences(view.getContext());

        // Add a cancel button
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Cancel just closes the dialog box
            }
        });

        // Add a login button
        builder.setPositiveButton(R.string.register, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String username = getUsername();
                String password = getPassword();
                String confirm = getConfirm();

                if (username.isEmpty() || password.isEmpty()) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(view.getContext(), R.string.register_failed_empty,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (!password.equals(confirm)) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(view.getContext(), R.string.register_failed_confirm,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    register(username, password);
                }
            }
        });

        dlg = builder.create();

        return dlg;
    }

    private void register(final String username, final String password) {
        if (!(getActivity() instanceof MainMenuActivity)) {
            return;
        }

        final RegisteringDlg registeringDlg = new RegisteringDlg();
        registeringDlg.show(getActivity().getFragmentManager(), "registering");

        final MainMenuActivity activity = (MainMenuActivity) getActivity();
        final ImageView view = (ImageView) activity.findViewById(R.id.imageMainMenu);

        Runnable registerRunnable = new Runnable() {
            @Override
            public void run() {
                // Create a cloud object
                Cloud cloud = new Cloud(view.getContext());
                final boolean ok = cloud.registerUserToCloud(username, password);
                if (!ok) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            // If we fail to register, display a toast
                            Toast.makeText(view.getContext(), R.string.register_fail, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    preferences.setLoginUsername(getUsername());
                    preferences.setLoginPassword(getPassword());
                    preferences.setRememberMe(false);

                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(view.getContext(), R.string.register_success, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                registeringDlg.dismiss();
            }
        };

        new Thread(registerRunnable).start();
    }

    private String getUsername() {
        EditText usernameEdit = (EditText) dlg.findViewById(R.id.registerUsernameText);
        return usernameEdit.getText().toString();
    }

    private String getPassword() {
        EditText usernameEdit = (EditText) dlg.findViewById(R.id.registerPasswordText);
        return usernameEdit.getText().toString();
    }

    private String getConfirm() {
        EditText usernameEdit = (EditText) dlg.findViewById(R.id.registerConfirmText);
        return usernameEdit.getText().toString();
    }
}
