package com.example.kristijan.opgwebshopadmin.UserAdministration;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kristijan.opgwebshopadmin.Base;
import com.example.kristijan.opgwebshopadmin.Model.AdminUser;
import com.example.kristijan.opgwebshopadmin.Network.CheckConnectivity;
import com.example.kristijan.opgwebshopadmin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import info.hoang8f.widget.FButton;

public class Profile extends Base {

    EditText Name,Surname,Phone,Email;
    FButton save,change_password,delete_user;

    FirebaseDatabase database;
    DatabaseReference adminUser;
    FirebaseUser user;


    AdminUser curentUser,updatedUser;
    ProgressDialog mdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setTitle(R.string.profileActivity);

        database = FirebaseDatabase.getInstance();
        adminUser = database.getReference("adminUser");
        user = FirebaseAuth.getInstance().getCurrentUser();

        curentUser= new AdminUser();

        Name=(EditText)findViewById(R.id.txt_name);
        Surname=(EditText)findViewById(R.id.txt_surname);
        Phone=(EditText)findViewById(R.id.txt_phone);
        Email=(EditText)findViewById(R.id.txt_email);

        save=(FButton)findViewById(R.id.btn_save);
        change_password=(FButton)findViewById(R.id.btn_changePass);
        delete_user=(FButton)findViewById(R.id.btn_deleteAccount);

        mdialog = new ProgressDialog(Profile.this);
        mdialog.setMessage(getResources().getString(R.string.wait));
        mdialog.show();
        if( new CheckConnectivity(this).isNetworkConnectionAvailable())
        {
            mdialog.show();
            new CheckConnectivity.TestInternet(this).execute();
        }
        else
        {
            mdialog.dismiss();
        }

        adminUser.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //FirebaseUser Auth_user = FirebaseAuth.getInstance().getCurrentUser();
                //Provjera dali korisnik postoji u bazi
                if (dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()) {
                    mdialog.dismiss();
                    curentUser = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(AdminUser.class);
                    Name.setText(curentUser.getName());
                    Surname.setText(curentUser.getSurname());
                    Phone.setText(curentUser.getPhone());
                    Email.setText(curentUser.getEmail());

                }
                else{
                    mdialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = Name.getText().toString();
                String surname = Surname.getText().toString();
                String phone = Phone.getText().toString();

                if (name.equals("") || surname.equals("") || phone.equals("")) {
                    Toast.makeText(Profile.this, R.string.fill_informations, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    updatedUser=new AdminUser(String.valueOf(Name.getText()),
                            String.valueOf(Surname.getText()),
                            String.valueOf(Phone.getText()),
                            String.valueOf(Email.getText()),
                            curentUser.getIsAdmin());
                    adminUser.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(updatedUser);
                    Toast.makeText(Profile.this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
                }
            }
        });

        change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent change_pas = new Intent(Profile.this, ChangePassword.class);
                startActivity(change_pas);
            }
        });

        delete_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });
    }

    public void deleteUser()

    {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Profile.this);
        alertDialog.setTitle(R.string.delete_user);
        alertDialog.setMessage(R.string.delete_user_message);

        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                adminUser.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();

                user.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Profile.this, R.string.user_deleted, Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(Profile.this, R.string.erorr, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                FirebaseAuth.getInstance().signOut();
                Intent redirect_login = new Intent(Profile.this, LogIn.class);
                startActivity(redirect_login);
                finish();
            }
        });
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
