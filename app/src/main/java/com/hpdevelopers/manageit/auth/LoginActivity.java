package com.hpdevelopers.manageit.auth;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.hpdevelopers.manageit.R;
import com.hpdevelopers.manageit.ViewUtils;
import com.hpdevelopers.manageit.passwords.PasswordActivity;

public class LoginActivity extends AppCompatActivity {

    private TextView forgotTV, signupTV;
    private Button loginBtn;
    private TextInputLayout emailEt, passwordEt;


    FirebaseAuth mAuth;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //Flag FullScreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);


        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Progress Dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);


        //init
        emailEt = findViewById(R.id.etLogEmail);
        passwordEt = findViewById(R.id.etLogPassword);
        loginBtn = findViewById(R.id.btnLogLogin);
        forgotTV = findViewById(R.id.tvLogForgot);
        signupTV = findViewById(R.id.tvLogSignUp);


        //Dialog No Internet
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LoginActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.no_internet_dialog, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog dialogNw = dialogBuilder.create();


        //On Click Handle
        signupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
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
                    loginUser();
                }

            }
        });

        //Forgot Password
        forgotTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    forgotPassword();
                }


            }

        });

    }

    //Login User
    private void loginUser() {
        String email = emailEt.getEditText().getText().toString().trim();
        String password = passwordEt.getEditText().getText().toString().trim();
        ViewUtils.resetTextInputErrorsOnTextChanged(emailEt, passwordEt);
        if (!validateEmail() | !validatePassword()) {
            return;
        }

        mProgressDialog.setTitle("Login..");
        mProgressDialog.setMessage("Please wait while checking your information");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        loginUser(email, password);

    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    //progressBar.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(LoginActivity.this, PasswordActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                } else {

                    //progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this, "Error:" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(LoginActivity.this, "Please input valid email", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                }

            }
        });
    }


    //Validations
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

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
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


    //Forgot Password
    private void forgotPassword() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LoginActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_reset_password, null);
        dialogBuilder.setView(dialogView);

        final EditText editEmail = (EditText) dialogView.findViewById(R.id.email);
        final Button btnReset = (Button) dialogView.findViewById(R.id.btn_reset_password);
        final ProgressBar progressBar1 = (ProgressBar) dialogView.findViewById(R.id.progressBar);
        //final Button btn_back = (Button) dialogView.findViewById(R.id.btn_back);

        final AlertDialog dialog = dialogBuilder.create();

        btnReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = editEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplication(), "Enter your registered email id", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar1.setVisibility(View.VISIBLE);
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    dialog.dismiss();
                                    Toast.makeText(LoginActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }

                                progressBar1.setVisibility(View.GONE);
                                dialog.dismiss();


                            }
                        });
            }

        });
        dialog.show();

    }


}