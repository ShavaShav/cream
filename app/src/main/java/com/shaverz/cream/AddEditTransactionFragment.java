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
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.shaverz.cream.data.DB;
import com.shaverz.cream.models.Account;
import com.shaverz.cream.models.Transaction;

public class AddEditTransactionFragment extends Fragment {

    private Transaction transaction; // Transaction object
    private boolean addingNewTransaction = true; // adding (true) or editing
    private boolean deleteWindowOpen;

    // EditTexts for transaction information

    private FloatingActionButton saveContactFAB;
    private View mView;

    private TextInputLayout amountTextLayout;
    private TextInputLayout payerPayeeTextLayout;
    private Spinner accountSpinner;
    private ArrayAdapter<Account> accountArrayAdapter; // holds account objects so can get id easily
    private Spinner categorySpinner;
    private ArrayAdapter<String> categoryArrayAdapter;
    private ToggleButton incomeToggle;

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
        mView = inflater.inflate(R.layout.fragment_add_edit_transaction, container, false);

        accountSpinner = (Spinner) mView.findViewById(R.id.account_spinner);
        categorySpinner = (Spinner) mView.findViewById(R.id.currency_spinner);
        amountTextLayout = mView.findViewById(R.id.amountTextInputLayout);
        payerPayeeTextLayout = mView.findViewById(R.id.payerPayeeTextInputLayout);
        incomeToggle = mView.findViewById(R.id.incomeToggle);

        incomeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isIncome) {
                if (isIncome)
                    payerPayeeTextLayout.setHint(getText(R.string.hint_payer));
                else
                    payerPayeeTextLayout.setHint(getText(R.string.hint_payee));
            }
        });

        amountTextLayout.requestFocus(); // focus on amount by default

        accountArrayAdapter = new ArrayAdapter<Account>(getContext(),
                android.R.layout.simple_spinner_item, MainActivity.CURRENT_USER.getAccountList());

        accountArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountSpinner.setAdapter(accountArrayAdapter);

        categoryArrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.category_array));

        categoryArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryArrayAdapter);

        // set FloatingActionButton's event listener
        saveContactFAB = (FloatingActionButton) mView.findViewById(R.id.addTransactionButton);
        saveContactFAB.setOnClickListener(saveContactButtonClicked);

        Bundle arguments = getArguments(); // null if creating new contact

        // get Transaction or income/expense from args
        if (arguments != null) {

            if (arguments.containsKey(MainActivity.IS_INCOME_BOOLEAN)) {
                boolean isIncome = arguments.getBoolean(MainActivity.IS_INCOME_BOOLEAN);
                if (isIncome) {
                    incomeToggle.setChecked(true); // income
                    payerPayeeTextLayout.setHint(getText(R.string.hint_payer));
                } else {
                    incomeToggle.setChecked(false); // expense
                    payerPayeeTextLayout.setHint(getText(R.string.hint_payee));
                }
            }

            // transaction object will override any is income toggle
            if (arguments.containsKey(MainActivity.TRANSACTION_OBJECT)) {
                addingNewTransaction = false;
                transaction = arguments.getParcelable(MainActivity.TRANSACTION_OBJECT);

                if (transaction.getAmount() < 0.00) {
                    incomeToggle.setChecked(false); // expense
                    payerPayeeTextLayout.setHint(getText(R.string.hint_payee));
                } else {
                    incomeToggle.setChecked(true); // income
                    payerPayeeTextLayout.setHint(getText(R.string.hint_payer));
                }

                // find account by name from transaction, in order to set spinner
                Account toSet = MainActivity.CURRENT_USER.findAccountByName(transaction.getAccount());
                int accountPosition = accountArrayAdapter.getPosition(toSet);
                accountSpinner.setSelection(accountPosition);

                // set spinner by transaction category
                int categoryPosition = categoryArrayAdapter.getPosition(transaction.getCategory());
                categorySpinner.setSelection(categoryPosition);

                amountTextLayout.getEditText().setText(String.valueOf(Math.abs(transaction.getAmount())));
                payerPayeeTextLayout.getEditText().setText(transaction.getPayee());
            }

        }

        deleteWindowOpen = false; // user has to click delete button to open

        return mView;
    }

    @Override
    public void onResume(){
        super.onResume();

        // Set title bar
        ((MainActivity) getActivity())
                .setActionBarTitle((addingNewTransaction ? "Add" : "Edit") + " Transaction");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!addingNewTransaction)
            inflater.inflate(R.menu.menu_add_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteTransaction();
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
                    saveTransaction(); // save contact to the database
                }
            };

    private void deleteTransaction() {
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
            if (!addingNewTransaction && deleteWindowOpen) {
                int numDeletions = getContext().getContentResolver().delete(
                        DB.Transaction.buildTransactionUri(Long.parseLong(transaction.getId())),
                        null,
                        null);

                if (numDeletions > 0) {
                    Snackbar.make(this.getView(), getString(R.string.delete_transaction_ok),
                            Snackbar.LENGTH_LONG).show();
                    deleteWindowOpen = false;
                    // go back to wherever we were
                    getFragmentManager().popBackStack();
                } else {
                    Snackbar.make(this.getView(), getString(R.string.delete_transaction_fail),
                            Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    // saves contact information to the database
    private void saveTransaction() {
        // reset errors
        payerPayeeTextLayout.setError(null);
        amountTextLayout.setError(null);

        Account account = accountArrayAdapter.getItem(accountSpinner.getSelectedItemPosition());
        String stringAmount = amountTextLayout.getEditText().getText().toString();
        String payee = payerPayeeTextLayout.getEditText().getText().toString();
        String category = categoryArrayAdapter.getItem(categorySpinner.getSelectedItemPosition());

        // Check for a valid payee/payer
        if (TextUtils.isEmpty(payee)) {
            payerPayeeTextLayout.setError(getString(R.string.error_field_required));
            payerPayeeTextLayout.requestFocus();
            return; // don't save
        }

        // Check for a non-empty amount
        if (TextUtils.isEmpty(stringAmount)) {
            amountTextLayout.setError(getString(R.string.error_field_required));
            amountTextLayout.requestFocus();
            return; // don't save
        }

        double amount = 0.00;
        try {
            amount = Math.abs(Double.parseDouble(stringAmount));
        } catch (Exception e) {
            // Amount is not a valid double
            amountTextLayout.setError(getString(R.string.error_invalid_number));
            amountTextLayout.requestFocus();
            return; // don't save
        }

        if (! incomeToggle.isChecked()) {
            // expense chosen, set amount to negative
            amount = -amount;
        }

        // create ContentValues object containing transactions's key-value pairs
        ContentValues contentValues = new ContentValues();
        contentValues.put(DB.Transaction.COLUMN_ACCOUNT_ID, account.getId());
        contentValues.put(DB.Transaction.COLUMN_AMOUNT, amount);
        contentValues.put(DB.Transaction.COLUMN_CATEGORY, category);
        contentValues.put(DB.Transaction.COLUMN_PAYEE, payee);

        if (addingNewTransaction) {
            // new transaction, use current time as date
            contentValues.put(DB.Transaction.COLUMN_DATE, System.currentTimeMillis());

            // use Activity's ContentResolver to invoke
            // insert on the AppContentProvider
            Uri newContactUri = getActivity().getContentResolver().insert(
                    DB.Transaction.CONTENT_URI, contentValues);
            if (newContactUri != null) {
                Snackbar.make(this.getView(), getString(R.string.add_transaction_ok),
                        Snackbar.LENGTH_LONG).show();
                // go back to wherever we were
                getFragmentManager().popBackStack();
            } else {
                Snackbar.make(this.getView(), getString(R.string.add_transaction_fail),
                        Snackbar.LENGTH_LONG).show();
            }

        }
        else {
            // use Activity's ContentResolver to invoke
            // insert on the AppContentProvider
            Uri transactionUri = DB.Transaction.buildTransactionUri(Long.parseLong(transaction.getId()));

            int updatedRows = getActivity().getContentResolver().update(
                    transactionUri, contentValues, null, null);

            if (updatedRows > 0) {
                Snackbar.make(this.getView(), getString(R.string.edit_transaction_ok),
                        Snackbar.LENGTH_LONG).show();
                // go back to wherever we were
                getFragmentManager().popBackStack();
            } else {
                Snackbar.make(this.getView(), getString(R.string.edit_transaction_fail),
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

}
