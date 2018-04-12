package com.shaverz.cream;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.shaverz.cream.data.DB;
import com.shaverz.cream.models.Account;
import com.shaverz.cream.utils.DBUtils;

public class AddEditAccountFragment extends Fragment {

    private Account account; // Account object
    private boolean addingNewAccount = true; // adding (true) or editing
    private boolean deleteWindowOpen;

    // EditTexts for account information

    private FloatingActionButton saveContactFAB;
    private View mView;

    private TextInputLayout openingBalanceTextLayout;
    private TextInputLayout nameTextLayout;

    // set AddEditFragmentListener when Fragment attached
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    // remove AddEditFragmentListener when Fragment detached
    @Override
    public void onDetach() {
        super.onDetach();
    }

    // called when Fragment's view needs to be created
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true); // fragment has menu items to display

        // inflate GUI and get references to values
        mView = inflater.inflate(R.layout.fragment_add_edit_account, container, false);
        nameTextLayout = (TextInputLayout) mView.findViewById(R.id.nameTextInputLayout);
        openingBalanceTextLayout = (TextInputLayout) mView.findViewById(R.id.openingBalanceTextInputLayout);

        // set FloatingActionButton's event listener
        saveContactFAB = (FloatingActionButton) mView.findViewById(R.id.addTransactionButton);
        saveContactFAB.setOnClickListener(saveContactButtonClicked);

        Bundle arguments = getArguments(); // null if creating new contact

        // get Account from args
        if (arguments != null) {
            addingNewAccount = false;
            openingBalanceTextLayout.setVisibility(View.GONE); // don't allow to edit opening bal

            account = arguments.getParcelable(MainActivity.ACCOUNT_OBJECT);
            nameTextLayout.getEditText().setText(account.getName());
        }

        nameTextLayout.requestFocus(); // focus on name by default
        deleteWindowOpen = false; // user has to click delete button to open

        return mView;
    }

    @Override
    public void onResume(){
        super.onResume();

        // Set title bar
        ((MainActivity) getActivity())
                .setActionBarTitle((addingNewAccount ? "Add" : "Edit") + " Account");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!addingNewAccount)
            inflater.inflate(R.menu.menu_add_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteAccount();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // responds to event generated when user saves a contact
    private final View.OnClickListener saveContactButtonClicked =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // hide the virtual keyboard
                    ((InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                            getView().getWindowToken(), 0);
                    saveAccount(); // save contact to the database
                }
            };

    private void deleteAccount() {
        if (!deleteWindowOpen) {
            deleteWindowOpen = true;

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            if (deleteWindowOpen) {
                                deleteWindowOpen = false; // close window after 1 second - user must double click
                                Snackbar.make(getView(), getString(R.string.delete_confirmation),
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    },1000);

        } else {
            // only delete if existing transaction
            if (!addingNewAccount) {
                int numDeletions = getContext().getContentResolver().delete(
                        DB.Account.buildAccountUri(Long.parseLong(account.getId())),
                        null,
                        null);

                if (numDeletions > 0) {
                    Snackbar.make(this.getView(), getString(R.string.delete_account_ok),
                            Snackbar.LENGTH_LONG).show();
                    deleteWindowOpen = false;
                    // go back to wherever we were
                    getFragmentManager().popBackStack();
                } else {
                    Snackbar.make(this.getView(), getString(R.string.delete_account_fail),
                            Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }


    // saves contact information to the database
    private void saveAccount() {
        // reset errors
        nameTextLayout.setError(null);
        openingBalanceTextLayout.setError(null);

        String stringOpeningBalance = openingBalanceTextLayout.getEditText().getText().toString();
        String name = nameTextLayout.getEditText().getText().toString();

        // Check for a valid account name
        if (TextUtils.isEmpty(name)) {
            nameTextLayout.setError(getString(R.string.error_field_required));
            nameTextLayout.requestFocus();
            return; // don't save
        }

        if (addingNewAccount) {

            // Check for a valid opening balance for new accounts
            if (TextUtils.isEmpty(stringOpeningBalance)) {
                openingBalanceTextLayout.setError(getString(R.string.error_field_required));
                openingBalanceTextLayout.requestFocus();
                return; // don't save
            }

            double openingBalance = Double.parseDouble(stringOpeningBalance); // no need to validate, text is signed decimal

            // use Activity's ContentResolver to invoke
            // insert on the AppContentProvider
            Uri newAccountUri = DBUtils.createNewAccount(getContext(), name, openingBalance);
            if (newAccountUri != null) {
                Snackbar.make(this.getView(), getString(R.string.add_account_ok),
                        Snackbar.LENGTH_LONG).show();
                // go back to wherever we were
                getFragmentManager().popBackStack();
            } else {
                Snackbar.make(this.getView(), getString(R.string.add_account_fail),
                        Snackbar.LENGTH_LONG).show();
            }

        }
        else {
            // use Activity's ContentResolver to invoke
            // insert on the AppContentProvider
            Uri accountUri = DB.Account.buildAccountUri(Long.parseLong(this.account.getId()));

            ContentValues contentValues = new ContentValues();
            contentValues.put(DB.Account.COLUMN_NAME, name);

            int updatedRows = getActivity().getContentResolver().update(
                    accountUri, contentValues, null, null);

            if (updatedRows > 0) {
                Snackbar.make(this.getView(), getString(R.string.edit_account_ok),
                        Snackbar.LENGTH_LONG).show();
                // go back to wherever we were
                getFragmentManager().popBackStack();
            } else {
                Snackbar.make(this.getView(), getString(R.string.edit_account_fail),
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

}
