package com.example.kristijan.opgwebshopadmin.UserAdministration;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kristijan.opgwebshopadmin.Model.AdminUser;
import com.example.kristijan.opgwebshopadmin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import info.hoang8f.widget.FButton;

public class CreateAccount extends AppCompatActivity {

    EditText email_reg,password_reg;
    FButton Create_account;

    FirebaseAuth auth;
    AdminUser user;

    FirebaseDatabase database;
    DatabaseReference adminUser;

    CheckBox admin;
    int is_admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        getSupportActionBar().setTitle(R.string.create_accountActivity);

        email_reg=(EditText) findViewById(R.id.input_email_reg);
        password_reg=(EditText) findViewById(R.id.input_password_reg);
        Create_account=(FButton) findViewById(R.id.Btn_Register);
        admin=(CheckBox)findViewById(R.id.checkBox);

        database = FirebaseDatabase.getInstance();
        adminUser = database.getReference("adminUser");


        auth = FirebaseAuth.getInstance();

        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (((CheckBox) v).isChecked())
                {
                    is_admin=1;
                }
                else
                {
                    is_admin=0;
                }
            }
        });

        Create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_reg.getText().toString().trim();
                String password = password_reg.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), R.string.enter_email, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), R.string.enter_password, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), R.string.password_short, Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(CreateAccount.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(CreateAccount.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                                } else {

                                    user= new AdminUser(FirebaseAuth.getInstance().getCurrentUser().getEmail(),is_admin);
                                    adminUser.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user);
                                    FirebaseAuth.getInstance().signOut();
                                    Toast.makeText(CreateAccount.this, R.string.seccesful, Toast.LENGTH_SHORT).show();
                                    Intent register_home = new Intent(CreateAccount.this, LogIn.class);
                                    register_home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(register_home);
                                }
                            }
                        });
            }
        });
    }
}
