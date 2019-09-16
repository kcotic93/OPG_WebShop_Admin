package com.example.kristijan.opgwebshopadmin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.kristijan.opgwebshopadmin.Network.CheckConnectivity;

public class Base extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if( new CheckConnectivity(this).isNetworkConnectionAvailable())
        {
            new CheckConnectivity.TestInternet(this).execute();
        }
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
