package com.example.kristijan.opgwebshopadmin;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kristijan.opgwebshopadmin.Model.About;
import com.example.kristijan.opgwebshopadmin.Network.CheckConnectivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import info.hoang8f.widget.FButton;

public class UpdateAbout extends Base {

    EditText UpdateActivities,UpdateGoals,UpdateIban,UpdateAddress,UpdateEmail,UpdateFacebook,UpdatePhone;
    FButton save;

    FirebaseDatabase database;
    DatabaseReference about;
    About UpdateAbout,SetAbout;
    ProgressDialog mdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_about);
        getSupportActionBar().setTitle(R.string.update_aboutActivity);

        database = FirebaseDatabase.getInstance();
        about = database.getReference("aboutUs");

        UpdateActivities=(EditText)findViewById(R.id.txt_activities);
        UpdateGoals=(EditText)findViewById(R.id.txt_goals);
        UpdateIban=(EditText)findViewById(R.id.txt_iban);
        UpdateAddress=(EditText)findViewById(R.id.txt_address);
        UpdateEmail=(EditText)findViewById(R.id.txt_Email);
        UpdateFacebook=(EditText)findViewById(R.id.txt_facebook);
        UpdatePhone=(EditText)findViewById(R.id.txt_phone);

        save=(FButton)findViewById(R.id.btn_save);

        mdialog = new ProgressDialog(UpdateAbout.this);
        mdialog.setMessage("Please wait...");
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

        about.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //FirebaseUser Auth_user = FirebaseAuth.getInstance().getCurrentUser();
                //Provjera dali korisnik postoji u bazi
                if (dataSnapshot.child("01").exists()) {

                    mdialog.dismiss();
                    UpdateAbout = dataSnapshot.child("01").getValue(About.class);
                    UpdateActivities.setText(UpdateAbout.getActivities());
                    UpdateGoals.setText(UpdateAbout.getGoals());
                    UpdateIban.setText(UpdateAbout.getIban());
                    UpdateAddress.setText(UpdateAbout.getAddress());
                    UpdateEmail.setText(UpdateAbout.getEmail());
                    UpdateFacebook.setText(UpdateAbout.getFacebook());
                    UpdatePhone.setText(UpdateAbout.getPhone());
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

                SetAbout=new About(String.valueOf(UpdateActivities.getText()),
                        String.valueOf(UpdateGoals.getText()),
                        String.valueOf(UpdateIban.getText()),
                        String.valueOf(UpdateAddress.getText()),
                        String.valueOf(UpdateEmail.getText()),
                        String.valueOf(UpdateFacebook.getText()),
                        String.valueOf(UpdatePhone.getText()));

                about.child("01").setValue(SetAbout);
                Toast.makeText(UpdateAbout.this, R.string.about_update, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
