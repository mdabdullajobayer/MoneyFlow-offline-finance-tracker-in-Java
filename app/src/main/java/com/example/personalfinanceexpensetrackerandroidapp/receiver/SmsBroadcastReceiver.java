package com.example.personalfinanceexpensetrackerandroidapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import com.example.personalfinanceexpensetrackerandroidapp.data.repository.FinanceRepository;

public class SmsBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            try {
                SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                if (messages != null && messages.length > 0) {
                    String sender = messages[0].getOriginatingAddress();
                    StringBuilder fullBody = new StringBuilder();
                    for (SmsMessage msg : messages) {
                        if (msg.getMessageBody() != null) {
                            fullBody.append(msg.getMessageBody());
                        }
                    }
                    String body = fullBody.toString();
                    Log.d(TAG, "Received SMS from: " + sender + ", body: " + body);

                    FinanceRepository repository = new FinanceRepository(context);
                    repository.handleIncomingSms(sender, body);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing incoming SMS", e);
            }
        }
    }
}
