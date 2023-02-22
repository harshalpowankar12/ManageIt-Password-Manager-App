package com.hpdevelopers.manageit.adapter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.hpdevelopers.manageit.R;
import com.hpdevelopers.manageit.Utility;
import com.hpdevelopers.manageit.model.Password;
import com.hpdevelopers.manageit.passwords.CreatePasswordActivity;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class PasswordAdapter extends FirestoreRecyclerAdapter<Password, PasswordAdapter.PasswordViewHolder> {

    /**
     * Create a new RecyclerView adapter that listens to a FireStore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    Context context;

    public PasswordAdapter(@NonNull FirestoreRecyclerOptions<Password> options, Context context) {
        super(options);
        this.context = context;
    }


    @Override
    protected void onBindViewHolder(@NonNull PasswordViewHolder holder, int position, @NonNull Password passwd) {

        holder.accountN.setText(passwd.getAccount());
        holder.timestampN.setText(Utility.timeStampToString(passwd.getTimestamp()));
        String account = passwd.getAccount();
        if (account != null && !account.isEmpty()) {
            holder.icontvN.setText(account.substring(0, 1).toUpperCase(Locale.ROOT));
        } else {
            // handle the case where account is null or empty
        }


        //DECRYPTING THE DATA

        //UserId
        String userId = passwd.getUserid();
        //Password
        String password = passwd.getPassword();
        //Other
        String other = passwd.getOther();


        holder.cpypasswordN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager myClipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData myClip = ClipData.newPlainText("Password", password);
                myClipboard.setPrimaryClip(myClip);
                Toast.makeText(view.getContext(), "Password Copied", Toast.LENGTH_SHORT).show();

            }
        });

        //VertIcon
        holder.vertpasswordN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popup = new PopupMenu(view.getContext(), view);
                popup.getMenuInflater().inflate(R.menu.password_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.copyUsername:
                                //handle menu1 click
                                ClipboardManager myUserIdClipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData myClipUserId = ClipData.newPlainText("UserName", userId);
                                myUserIdClipboard.setPrimaryClip(myClipUserId);
                                Toast.makeText(view.getContext(), "UserName Copied", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.copyPassword:
                                //handle menu2 click
                                ClipboardManager myPasswordClipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData myClipPassword = ClipData.newPlainText("Password", other);
                                myPasswordClipboard.setPrimaryClip(myClipPassword);
                                Toast.makeText(view.getContext(), "Password Copied", Toast.LENGTH_SHORT).show();
                                break;
                        }

                        return false;
                    }
                });
                popup.show();


            }
        });

        //On Item Click Listener
        holder.itemView.setOnClickListener((v) ->
        {
            String docIdP = this.getSnapshots().getSnapshot(position).getId();
            Intent intent = new Intent(context, CreatePasswordActivity.class);
            intent.putExtra("account", passwd.getAccount());
            intent.putExtra("userid", passwd.getUserid());
            intent.putExtra("password", passwd.getPassword());
            intent.putExtra("other", passwd.getOther());

            intent.putExtra("docIdP", docIdP);
            context.startActivity(intent);

        });

    }


    //Item Count
    @Override
    public int getItemCount() {
        return super.getItemCount();
    }


    //On Create View Holder
    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.password_layout, parent, false);
        return new PasswordViewHolder(view);
    }


    //Delete Item
    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    //View Holder Class
    static class PasswordViewHolder extends RecyclerView.ViewHolder {

        TextView accountN, timestampN, icontvN;
        ImageButton vertpasswordN, cpypasswordN;

        public PasswordViewHolder(@NonNull View itemView) {
            super(itemView);

            accountN = itemView.findViewById(R.id.passwordCardAccountTV);
            timestampN = itemView.findViewById(R.id.passwordCardDateTV);
            icontvN = itemView.findViewById(R.id.passwordCardIconTv);
            vertpasswordN = itemView.findViewById(R.id.passwordCardVertPassword);
            cpypasswordN = itemView.findViewById(R.id.passwordCardCpyPassword);


        }
    }



}



