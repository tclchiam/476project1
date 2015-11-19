package edu.msu.chuppthe.steampunked;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterDlg extends DialogFragment {

    private AlertDialog dlg;

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

        // Add a cancel button
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Cancel just closes the dialog box
            }
        });

        // Add a login button
        builder.setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String username = getUsername();
                String password = getPassword();
                String confirm = getConfirm();

                if (password.equals(confirm)) {
                    register(username, password);
                } else {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(view.getContext(), R.string.register_failed_confirm,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        dlg = builder.create();

        return dlg;
    }

    private void register(String username, String password) {
        // TODO: Register user
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
