package com.example.kristijan.opgwebshopadmin.UserAdministration;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kristijan.opgwebshopadmin.MainMenu;
import com.example.kristijan.opgwebshopadmin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import info.hoang8f.widget.FButton;

public class LogIn extends AppCompatActivity {

    TextView Password_reset;
    EditText email_login, password_login;
    FButton Login_Btn;

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference userAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.loginActivity);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LogIn.this, MainMenu.class));
            finish();
        }
        setContentView(R.layout.activity_log_in);

        Password_reset = (TextView) findViewById(R.id.link_password_reset);
        email_login = (EditText) findViewById(R.id.txt_email_reset);
        password_login = (EditText) findViewById(R.id.txt_password_login);
        Login_Btn = (FButton) findViewById(R.id.Login_Button_on);

        auth = FirebaseAuth.getInstance();

        Password_reset.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent reset = new Intent(LogIn.this,ResetPassword.class);
                startActivity(reset);
                return false;
            }
        });

        Login_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_login.getText().toString();
                final String password = password_login.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), R.string.enter_email, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), R.string.enter_password, Toast.LENGTH_SHORT).show();
                    return;
                }
                final ProgressDialog mdialog = new ProgressDialog(LogIn.this);
                mdialog.setMessage("Please wait...");
                mdialog.show();
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LogIn.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.

                                if (!task.isSuccessful()) {
                                    // there was an error
                                    if (password.length() < 6) {
                                        Toast.makeText(getApplicationContext(), R.string.password_short, Toast.LENGTH_SHORT).show();
                                        mdialog.dismiss();
                                    } else {
                                        Toast.makeText(LogIn.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                        mdialog.dismiss();
                                    }
                                } else {

                                    userAdmin = database.getInstance().getReference().child("adminUser");

                                    userAdmin.orderByChild("email").equalTo(FirebaseAuth.getInstance().getCurrentUser().getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                mdialog.dismiss();

                                                Intent menu = new Intent(LogIn.this, MainMenu.class);
                                                startActivity(menu);
                                                finish();
                                            } else {
                                                mdialog.dismiss();
                                                FirebaseAuth.getInstance().signOut();
                                                Toast.makeText(LogIn.this, R.string.user_not_exist, Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        });
                }
        });
    }
}
