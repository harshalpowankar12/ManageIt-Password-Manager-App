package com.hpdevelopers.manageit.passwords;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hpdevelopers.manageit.R;
import com.hpdevelopers.manageit.Utility;
import com.hpdevelopers.manageit.adapter.PasswordAdapter;
import com.hpdevelopers.manageit.auth.LoginActivity;
import com.hpdevelopers.manageit.model.Password;

import java.util.Objects;
import java.util.concurrent.Executors;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class PasswordActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FloatingActionButton createPasswordFab;
    TextView EmptyRvTVp;

    //Back To Exit
    boolean doubleBackToExitPressedOnce = false;

    //Init RecyclerView & Adapter
    RecyclerView passwordRecyclerView;
    PasswordAdapter passwordAdapter;

    //Navigation Drawer
    private DrawerLayout drawer;
    NavigationView navigationView;
    private TextView userNameTV, userEmailTV;


    //Firebase Auth
    FirebaseAuth mAuth;

    //ProgressDialog
    ProgressDialog mProgressDialog;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        //ProgressDialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        //Firebase auth
        mAuth = FirebaseAuth.getInstance();

        //Fab
        createPasswordFab = findViewById(R.id.passwordFloatingBtn);

        //RecyclerView
        passwordRecyclerView = findViewById(R.id.recyclerView);

        //Empty RV TV
        EmptyRvTVp = findViewById(R.id.EmptyRvTVP);


        //Navigation Drawer
        navigationView = findViewById(R.id.NavigationDrawer);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.NavigationDrawer);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView titleTextView = toolbar.findViewById(R.id.title_text_view);
        titleTextView.setTypeface(null, Typeface.ITALIC);
        toolbar.setTitleTextColor(getResources().getColor(R.color.bluish_bg));


        //NavigationView
        navigationView.setNavigationItemSelectedListener(PasswordActivity.this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.addDrawerListener(toggle);
        toggle.syncState();
        //------------To change Navigation drawer icon ---------------//
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);


        //HeaderView
        View headerView = navigationView.getHeaderView(0);
        userNameTV = (TextView) headerView.findViewById((R.id.nav_header_name));
        userEmailTV = (TextView) headerView.findViewById((R.id.nav_header_email));


        //Empty Recycler View
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore.getInstance().collection("passwords").document(currentUser.getUid()).collection("my_passwords").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {

                    if (!queryDocumentSnapshots.isEmpty()) {

                        int count = queryDocumentSnapshots.size();

                        if (count > 0) {
                            EmptyRvTVp.setVisibility(View.GONE);
                        }
                    } else {

                        EmptyRvTVp.setVisibility(View.VISIBLE);
                    }


                }
            }

        });

        //Fab
        createPasswordFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PasswordActivity.this, CreatePasswordActivity.class);
                startActivity(intent);
            }
        });

        //Fab Scroll
        passwordRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    createPasswordFab.hide();
                } else {
                    createPasswordFab.show();
                }
            }
        });


        setupPRV();
        showBiometricPrompt();
        loadMyInfo();


        //Delete Item Form Rv
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                passwordAdapter.deleteItem(viewHolder.getAdapterPosition());

            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive).addBackgroundColor(ContextCompat.getColor(PasswordActivity.this, R.color.sushi_red_600)).addActionIcon(R.drawable.ic_delete).create().decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }


        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(passwordRecyclerView);


    }

    //Dialog About
    void alertdialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PasswordActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.about_layout, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        ImageView dialog_btn_github = (ImageView) dialog.findViewById(R.id.github);
        ImageView dialog_btn_linkedin = (ImageView) dialog.findViewById(R.id.linkedin);
        ImageView dialog_btn_gmail = (ImageView) dialog.findViewById(R.id.gmail);


        assert dialog_btn_github != null;
        dialog_btn_github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/harshalpowankar12"));
                startActivity(intent);
            }
        });

        assert dialog_btn_linkedin != null;
        dialog_btn_linkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.linkedin.com/in/harshal-powankar"));
                startActivity(intent);
            }
        });

        assert dialog_btn_gmail != null;
        dialog_btn_gmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("mailto:harshalpowankar@gmail.com?subject=Need%20help!&amp;body=Dear%20developer%3C%3E"));
                startActivity(intent);
            }
        });


    }

    private void setupPRV() {
        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (isFinishing()) {
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if (isDestroyed()) {
                        return;
                    }
                }
                finish();
            }
        });
        Query query = Utility.getCollectionRefPassword().orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Password> options = new FirestoreRecyclerOptions.Builder<Password>().setQuery(query, Password.class).build();

        passwordRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        passwordAdapter = new PasswordAdapter(options, this);
        passwordRecyclerView.setAdapter(passwordAdapter);

        passwordAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mProgressDialog.dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        passwordAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        passwordAdapter.startListening();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        passwordAdapter.notifyDataSetChanged();

    }


    //Biometric Prompt
    private void showBiometricPrompt() {
        BiometricPrompt biometricPrompt = new BiometricPrompt(PasswordActivity.this, Executors.newSingleThreadExecutor(), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                finish();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Handler handler = new Handler(Looper.getMainLooper());

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PasswordActivity.this, "Authentication Successful", Toast.LENGTH_SHORT).show();
                    }
                }, 500);

            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                finish();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("Biometric Authentication").setSubtitle("Log in using your biometric credentials").setNegativeButtonText("Cancel").build();

        biometricPrompt.authenticate(promptInfo);
    }

    //Header View Load Info
    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();

                    userNameTV.setText(name);
                    userEmailTV.setText(email);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //Navigation Menu
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                navigationView.setCheckedItem(R.id.nav_home);
                break;

            case R.id.nav_profile:
                Toast.makeText(this, "Dummy Profile", Toast.LENGTH_SHORT).show();
                break;


            case R.id.nav_setting:
                Toast.makeText(this, "Dummy Setting", Toast.LENGTH_SHORT).show();
                break;


            case R.id.nav_aboutus:
                alertdialog();
                break;


            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(PasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(PasswordActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
                finish();
                break;


        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        drawer.isDrawerOpen(GravityCompat.START);
        drawer.closeDrawer(GravityCompat.START);

        //Double click Back to Exit
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        //Handler Delayed Action
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


}