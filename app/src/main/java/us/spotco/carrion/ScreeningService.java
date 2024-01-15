/*
Copyright (c) 2024 Divested Computing Group

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package us.spotco.carrion;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.Connection;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

public class ScreeningService extends CallScreeningService {
    private NotificationManager notificationManager = null;
    
    @Override
    public void onScreenCall(Call.Details details) {
        boolean isIncoming = details.getCallDirection() == Call.Details.DIRECTION_INCOMING;
        Log.d("Carrion", "Received call");
        if (isIncoming && !isEmergencyCall(details)) {
            Log.d("Carrion", "Verification " + details.getCallerNumberVerificationStatus());
            switch (details.getCallerNumberVerificationStatus()) {
                case Connection.VERIFICATION_STATUS_FAILED:
                    sendNotification(getString(R.string.lblDisallowedCall), getString(R.string.lblStatusVerifyFailed));
                    callDisallow(details, false);
                    break;
                case Connection.VERIFICATION_STATUS_PASSED:
                    //sendNotification(getString(R.string.lblAllowedCall), getString(R.string.lblStatusVerifySuccess));
                    callAllow(details);
                    break;
                default:
                    sendNotification(getString(R.string.lblAllowedCall), getString(R.string.lblStatusVerifyUnknown));
                    callAllow(details);
                    break;
            }
        } else {
            //sendNotification(getString(R.string.lblAllowedCall), getString(R.string.lblStatusExcluded));
            callAllow(details);
        }
    }

    private boolean isEmergencyCall(Call.Details details) {
        return details.hasProperty(Call.Details.PROPERTY_EMERGENCY_CALLBACK_MODE)
                || details.hasProperty(Call.Details.PROPERTY_NETWORK_IDENTIFIED_EMERGENCY_CALL);
    }

    private void callDisallow(Call.Details details, boolean reject) {
        Log.d("Carrion", "Disallowing call");
        CallResponse.Builder response = new CallResponse.Builder();
        response.setDisallowCall(true);
        response.setRejectCall(reject);
        respondToCall(details, response.build());
    }

    private void callAllow(Call.Details details) {
        Log.d("Carrion", "Allowing call");
        respondToCall(details, new CallResponse.Builder().build());
    }

    private void sendNotification(String title, String content){
        //Log.d("Carrion", "Trying to notify: " + title + ", " + content);
        if(isDeviceLocked()) {
            if(notificationManager == null) {
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel alertChannel = new NotificationChannel("CallScreeningAlerts", getString(R.string.lblNotificationAlertTitle), NotificationManager.IMPORTANCE_HIGH);
                alertChannel.setDescription(getString(R.string.lblNotificationAlertDescription));
                notificationManager.createNotificationChannel(alertChannel);
            }

            int notificationId = new Random().nextInt();
            Notification.Builder mBuilder =
                    new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle(title)
                            .setContentText(content)
                            .setShowWhen(true)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .setChannelId("CallScreeningAlerts");
            notificationManager.notify(notificationId, mBuilder.build());
        } else {
            Toast.makeText(this, title + "\n" + content, Toast.LENGTH_LONG).show();
        }
    }

    private boolean isDeviceLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.isDeviceLocked();
    }
}
