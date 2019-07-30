package com.challenge.multichannelpeerconnectiondisasteremergencybroadcastingmessagingsystem;

import android.os.Bundle;
import android.widget.TextView;


public class SendingSOSMessageActivity extends SignalingActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String fileName = generateSOSMessage();
        setContentView(R.layout.message_generated);
        TextView textView = findViewById(R.id.textView);
        textView.setText(fileName + " built");
        tryToSendData();
    }


    protected String generateSOSMessage(){
        return generateMessage("sos");
    }
}
