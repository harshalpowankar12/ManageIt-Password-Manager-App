package com.hpdevelopers.manageit.passwords;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.hpdevelopers.manageit.R;
import com.hpdevelopers.manageit.Utility;
import com.hpdevelopers.manageit.model.Password;

public class CreatePasswordActivity extends AppCompatActivity {

    EditText accountNameEdittext, userIdEdittext, passwordEdittext, otherEdittext, cnfPasswordEditex;

    String accountName, userId, password, other, docIdp;
    TextView pageTitleTV;

    ImageButton savepasswordBtn, backArrowCP;

    boolean isEditmode = false;

    Button deletepassword;

    ProgressDialog mProgressDialog;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_password);


        //Edittext init
        accountNameEdittext = findViewById(R.id.passwordCCategoryEt);
        userIdEdittext = findViewById(R.id.passwordCUserIdET);
        passwordEdittext = findViewById(R.id.passwordCPasswordET);
        cnfPasswordEditex = findViewById(R.id.passwordCCnfPasswordET);
        otherEdittext = findViewById(R.id.passwordCOtherEt);
        //ImageButton init
        savepasswordBtn = findViewById(R.id.saveCPasswordBtn);
        backArrowCP = findViewById(R.id.backArrowCPassword);
        //PageTitle
        pageTitleTV = findViewById(R.id.addNewCPassword);
        //delete
        deletepassword = findViewById(R.id.deleteCPasswordBtn);

        //GetIntent
        accountName = getIntent().getStringExtra("account");
        userId = getIntent().getStringExtra("userid");
        password = getIntent().getStringExtra("password");
        other = getIntent().getStringExtra("other");
        docIdp = getIntent().getStringExtra("docIdP");

        if (docIdp != null) {
            isEditmode = true;
        }

        accountNameEdittext.setText(accountName);
        userIdEdittext.setText(userId);
        passwordEdittext.setText(password);
        cnfPasswordEditex.setText(password);
        otherEdittext.setText(other);

        if (isEditmode) {
            pageTitleTV.setText("Edit Your Accounts");
            deletepassword.setVisibility(View.VISIBLE);

        } else {
            deletepassword.setVisibility(View.GONE);
        }

        //Dialog No Internet
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CreatePasswordActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.no_internet_dialog, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog dialogNw = dialogBuilder.create();


        savepasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Method Call
                if (!isOnline()) {
                    dialogNw.show();
                    TextView dialog_btn_retry = (TextView) dialogNw.findViewById(R.id.retry);
                    assert dialog_btn_retry != null;
                    dialog_btn_retry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogNw.dismiss();
                        }
                    });
                } else {
                    savePassword();
                }

            }
        });
        deletepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOnline()) {
                    dialogNw.show();
                    TextView dialog_btn_retry = (TextView) dialogNw.findViewById(R.id.retry);
                    assert dialog_btn_retry != null;
                    dialog_btn_retry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogNw.dismiss();
                        }
                    });
                } else {
                    deletePassword();
                }

            }


        });
        backArrowCP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }


    //Validations
    private void savePassword() {


        String accountName = accountNameEdittext.getText().toString().trim();
        String userid = userIdEdittext.getText().toString().trim();
        String password = passwordEdittext.getText().toString().trim();
        String cnfPassword = cnfPasswordEditex.getText().toString().trim();
        String other = otherEdittext.getText().toString().trim();


        if (accountName.isEmpty()) {
            accountNameEdittext.setError("Account Name is Required ");
            return;
        }
        if (userid.isEmpty()) {
            userIdEdittext.setError("Please Enter UserId ");
            return;
        }
        if (password.isEmpty()) {
            passwordEdittext.setError("Please Enter Password ");
            return;
        }
        if ((cnfPassword.isEmpty())) {
            cnfPasswordEditex.setError("Enter Password Again");
        }

        if (!(cnfPassword.equals(password))) {
            cnfPasswordEditex.setError("Password Not Matched !");
        } else {
            Password passwd = new Password();
            passwd.setAccount(accountName);
            passwd.setUserid(userid);
            passwd.setPassword(cnfPassword);
            passwd.setOther(other);
            passwd.setTimestamp(Timestamp.now());

            savePasswordFirebase(passwd);
        }


    }

    //Saving to Firebase
    private void savePasswordFirebase(Password passwd) {

        //Progress Dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        DocumentReference documentReferenceP;

        if (isEditmode) {
            //Update
            documentReferenceP = Utility.getCollectionRefPassword().document(docIdp);
        } else {
            //Create
            documentReferenceP = Utility.getCollectionRefPassword().document();
        }

        documentReferenceP.set(passwd).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CreatePasswordActivity.this, "Details Added..", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                    finish();
                } else {
                    Toast.makeText(CreatePasswordActivity.this, "Failed Adding the Details..", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                }
            }
        });


    }

    //Delete Account
    private void deletePassword() {

        //Progress Dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        DocumentReference documentReferenceP;
        documentReferenceP = Utility.getCollectionRefPassword().document(docIdp);

        documentReferenceP.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CreatePasswordActivity.this, "Account Deleted Successful..", Toast.LENGTH_SHORT).show();
                    finish();
                    mProgressDialog.dismiss();
                } else {
                    Toast.makeText(CreatePasswordActivity.this, "Error while deleting Account details..", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                }

            }
        });

    }


    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            return false;
        }
        return true;
    }
}