package devsnewapps.usbautocharger;

import android.telephony.SmsManager;

/**
 * Created by frog on 05/01/15.
 */
public class SMSSender {
    public static void sendSms(String number, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, message, null, null);
    }
}