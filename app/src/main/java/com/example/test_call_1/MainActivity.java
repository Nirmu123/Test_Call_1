package com.example.test_call_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.media.AudioManager;
import android.Manifest;
import android.content.pm.PackageManager;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.azure.android.communication.calling.Call;
import com.azure.android.communication.calling.CallAgentOptions;
import com.azure.android.communication.calling.IncomingCall;
import com.azure.android.communication.calling.IncomingCallListener;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.communication.calling.CallAgent;
import com.azure.android.communication.calling.CallClient;
import com.azure.android.communication.calling.StartCallOptions;
import com.azure.android.communication.calling.internal.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.*;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    IncomingCall incomingCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getAllPermissions();
        CallAgent callAgent = createAgent();

        Button callButton = findViewById(R.id.call_button);
        callButton.setOnClickListener(l -> startCall(callAgent));

        Button acceptButton = findViewById(R.id.accept_button);
        acceptButton.setOnClickListener(l -> acceptCall(callAgent));

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        final String[] fcm_device_token = new String[1];

        //Section for getting FCM Registration Token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("PushNotification", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        fcm_device_token[0] = task.getResult();

                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("PushNotification", "FCM Device registration success " + fcm_device_token[0]);
                        //Toast.makeText(MainActivity.this, "Device Token : " + fcm_device_token[0], Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getAllPermissions() {
        String[] requiredPermissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};
        ArrayList<String> permissionsToAskFor = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToAskFor.add(permission);
            }
        }
        if (!permissionsToAskFor.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToAskFor.toArray(new String[0]), 1);
        }
    }

    private CallAgent createAgent() {
        String userToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEwNiIsIng1dCI6Im9QMWFxQnlfR3hZU3pSaXhuQ25zdE5PU2p2cyIsInR5cCI6IkpXVCJ9.eyJza3lwZWlkIjoiYWNzOmQ2MWM2MDcwLTY3YjItNDAzMC1iZmMxLTUyYjY4ZDg0N2Q0ZF8wMDAwMDAxMi1lOTM3LWRlNGUtMzU3My01NzQ4MjIwMDdkODAiLCJzY3AiOjE3OTIsImNzaSI6IjE2NTkwODc3NjQiLCJleHAiOjE2NTkxNzQxNjQsImFjc1Njb3BlIjoidm9pcCIsInJlc291cmNlSWQiOiJkNjFjNjA3MC02N2IyLTQwMzAtYmZjMS01MmI2OGQ4NDdkNGQiLCJpYXQiOjE2NTkwODc3NjR9.UDzLwgvGbRZWqxVfCzRLTPflqjZWww8oO8I12WgzoLLiIxgounz11lr9bafZ0C0RW1vTuvDn7RThTLC5wNGQ3P5u4iiadbHrlyWNb5YBkrhGZVDOg-VvNLi2T3BCE7I5N3yyz_Jd1B_S_Gf7pluNkMrinMtLR_aB6Ky5jMykxmr9Gsb_Ll740QLsz2HS60pxrS95Dt8IIihWyCOisiPO4JtxBzKoF0wF0UxPB_ejfzsuS1dK7CYKZNVmisIEqiXHSU-fYUFptG5xSlTVJS09K_NhwJbYzf4Z69GbXIy7hvsg2u48J_fjddrv1PISd3FUtwAKfcxpfoXxzLYZSx9sbQ";
        try {
            CommunicationTokenCredential credential = new CommunicationTokenCredential(userToken);
            //CallAgentOptions callAgentOptions = new CallAgentOptions();
            //callAgentOptions.setDisplayName("Nirmal");
            CallAgent callAgent = new CallClient().createCallAgent(getApplicationContext(), credential).get();
            return callAgent;
        }
        catch (Exception ex){
            Toast.makeText(getApplicationContext(), "Failed to create call agent.", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private void startCall(CallAgent callAgent) {
        EditText calleeIdView = findViewById(R.id.callee_id);
        String calleeId = calleeIdView.getText().toString();
        //String calleeId = "";

        StartCallOptions options = new StartCallOptions();

        callAgent.startCall(
                getApplicationContext(),
                new CommunicationUserIdentifier[] {new CommunicationUserIdentifier(calleeId)},
                options);
    }

    private void acceptCall(CallAgent callAgent) {
        try {
            Context appContext = this.getApplicationContext();
            IncomingCall incomingCall = retrieveIncomingCall(callAgent);
            Call call = (Call) incomingCall.accept(appContext).get();
        }
        catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Failed to accept call.", Toast.LENGTH_SHORT).show();
        }
    }

    public IncomingCall retrieveIncomingCall(CallAgent callAgent) {
        //IncomingCall incomingCall;
        callAgent.addOnIncomingCallListener(new IncomingCallListener() {
            public void onIncomingCall(IncomingCall inboundCall) {
                incomingCall = inboundCall;
            }
        });
        return incomingCall;
    }

}