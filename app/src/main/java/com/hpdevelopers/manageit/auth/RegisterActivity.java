package com.hpdevelopers.manageit.auth;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hpdevelopers.manageit.R;
import com.hpdevelopers.manageit.ViewUtils;
import com.hpdevelopers.manageit.passwords.PasswordActivity;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    TextInputLayout nameEt, emailEt, passwordEt, cnfPasswordEt;
    Button registerUserBtn;
    TextView signInTV;


    private String name, email, password, cnfPassword;

    //Firebase Declaration
    FirebaseAuth mAuth;
    ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        //init views
        nameEt = findViewById(R.id.etRegUserName);
        emailEt = findViewById(R.id.etRegEmail);
        passwordEt = findViewById(R.id.etRegPassword);
        cnfPasswordEt = findViewById(R.id.etRegCnfPassword);
        signInTV = findViewById(R.id.tvRegSignInBtn);
        registerUserBtn = findViewById(R.id.btnRegRegister);

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Progress Dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        //Dialog No Internet
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(RegisterActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.no_internet_dialog, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog dialogNw = dialogBuilder.create();


        signInTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        registerUserBtn.setOnClickListener(new View.OnClickListener() {
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
                    inputData();
                }


            }
        });

    }


    //Taking The Data
    private void inputData() {
        name = nameEt.getEditText().getText().toString().trim();
        email = emailEt.getEditText().getText().toString().trim();
        password = passwordEt.getEditText().getText().toString().trim();
        cnfPassword = cnfPasswordEt.getEditText().getText().toString().trim();

        ViewUtils.resetTextInputErrorsOnTextChanged(nameEt, emailEt, passwordEt, cnfPasswordEt);

        if (!validateUsername() | !validateEmail() | !validatePassword() | !validateCnfPassword()) {
            return;
        }

        createAccount(email, password);

    }

    //Creating Account
    private void createAccount(String email, String password) {

        mProgressDialog.setMessage("Creating Account...");
        mProgressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        saveFirebaseData();
                        Toast.makeText(RegisterActivity.this, "Account Created..", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Saving Data To Firebase
    private void saveFirebaseData() {
        mProgressDialog.setMessage("Saving Account Details..");
        final String timestamp = "" + System.currentTimeMillis();
        if (email != null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" + mAuth.getUid());
            hashMap.put("email", "" + email);
            hashMap.put("name", "" + name);
            hashMap.put("timestamp", "" + timestamp);


            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(mAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mProgressDialog.dismiss();
                            Intent intent = new Intent(RegisterActivity.this, PasswordActivity.class);
                            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Error Saving Details.." + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }


    }


    //Validations
    private boolean validateUsername() {
        String un = nameEt.getEditText().getText().toString().trim();

        if (un.isEmpty()) {
            nameEt.setError("Field can not be empty");
            return false;
        } else if (un.length() >= 20) {
            nameEt.setError("Username too Long..");
            return false;
        } else {
            nameEt.setError(null);
            nameEt.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateEmail() {
        String val = emailEt.getEditText().getText().toString().trim();
        String checkEmail = "[a-zA-Z0-9._-]+@[a-z]+.+[a-z]+";
        String checkEmail1 = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

        if (val.isEmpty()) {
            emailEt.setError("Field can not be empty");
            return false;
        } else if (!val.matches(checkEmail1)) {
            emailEt.setError("Invalid Email!");
            return false;
        } else {
            emailEt.setError(null);
            emailEt.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword() {
        String val = passwordEt.getEditText().getText().toString().trim();
        String checkPassword = "^" +
                //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                //"(?=.*[@#$%^&+=])" +    //at least 1 special character
                //"(?=S+$)" +           //no white spaces
                //".{4,}" +               //at least 4 characters
                "$";

        if (val.isEmpty()) {
            passwordEt.setError("Field can not be empty");
            return false;
        } else if (val.matches(checkPassword)) {
            passwordEt.setError("Password should contain 6 characters!");
            return false;
        } else {
            passwordEt.setError(null);
            passwordEt.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateCnfPassword() {
        String cp = cnfPasswordEt.getEditText().getText().toString().trim();
        String val = passwordEt.getEditText().getText().toString().trim();


        if (cp.isEmpty()) {
            cnfPasswordEt.setError("Field can not be empty");
            return false;
        } else if (!val.equals(cp)) {
            cnfPasswordEt.setError("Password Not Matched !");
            return false;
        } else {
            cnfPasswordEt.setError(null);
            cnfPasswordEt.setErrorEnabled(false);
            return true;
        }
    }

    //Check Data Connectivity
    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            return false;
        }
        return true;
    }
}