package com.challenge.multichannelpeerconnectiondisasteremergencybroadcastingmessagingsystem;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.challenge.multichannelpeerconnectiondisasteremergencybroadcastingmessagingsystem.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    public void sendUpdateMessage(View view) {
        startActivity(new Intent(view.getContext(), SendingUpdateMessageActivity.class));
    }

    public void sendSOSMessage(View view) {
        startActivity(new Intent(view.getContext(), SendingSOSMessageActivity.class));
    }
}