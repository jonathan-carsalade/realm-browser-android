package com.jcarsalade.realmbrowser.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jcarsalade.realmbrowser.RealmBrowser;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RealmBrowser realmBrowser = new RealmBrowser.Builder()
                .port(8080)
                .build();
    }
}
