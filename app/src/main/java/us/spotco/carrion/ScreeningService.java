/*
Copyright (c) 2024 Divested Computing Group

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package us.spotco.carrion;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Random;
import java.util.zip.GZIPInputStream;

public class ScreeningService extends CallScreeningService {
    private NotificationManager notificationManager = null;
    private File database = null;
    private static HashSet<String> databaseNumbers = null;
    private static long databaseTimestamp = 0;

    @Override
    public void onScreenCall(Call.Details details) {
        boolean isIncoming = details.getCallDirection() == Call.Details.DIRECTION_INCOMING;
        Log.d("Carrion", "Received call");
        if (isIncoming && !isEmergencyCall(details)) {
            Log.d("Carrion", "Verification " + details.getCallerNumberVerificationStatus());
            if (details.getCallerNumberVerificationStatus() == Connection.VERIFICATION_STATUS_FAILED) {
                sendNotification(getString(R.string.lblDisallowedCall), getString(R.string.lblStatusVerifyFailed));
                callDisallow(details);
                incrementIntPref("STAT_VERIFICATION_FAILED");
            } else if (details.getHandle() != null && isNumberInDatabase(details.getHandle().toString())) {
                sendNotification(getString(R.string.lblSilencedCall), getString(R.string.lblStatusMatchedDatabase));
                callSilence(details);
                incrementIntPref("STAT_MATCHED_DATABASE");
            } else if (details.getCallerNumberVerificationStatus() == Connection.VERIFICATION_STATUS_PASSED) {
                //sendNotification(getString(R.string.lblAllowedCall), getString(R.string.lblStatusVerifySuccess));
                callAllow(details);
                incrementIntPref("STAT_VERIFICATION_PASSED");
            } else {
                if (getDefaultSharedPreferences(this).getBoolean("PREF_BLOCK_UNKNOWN", false)) {
                    sendNotification(getString(R.string.lblDisallowedCall), getString(R.string.lblStatusVerifyUnknown));
                    callDisallow(details);
                } else if (getDefaultSharedPreferences(this).getBoolean("PREF_SILENCE_UNKNOWN", false)) {
                    sendNotification(getString(R.string.lblSilencedCall), getString(R.string.lblStatusVerifyUnknown));
                    callSilence(details);
                } else {
                    sendNotification(getString(R.string.lblAllowedCall), getString(R.string.lblStatusVerifyUnknown));
                    callAllow(details);
                }
                incrementIntPref("STAT_VERIFICATION_UNKNOWN");
            }
        } else {
            //sendNotification(getString(R.string.lblAllowedCall), getString(R.string.lblStatusExcluded));
            callAllow(details);
            incrementIntPref("STAT_EXCLUDED");
        }
    }

    private boolean isEmergencyCall(Call.Details details) {
        return details.hasProperty(Call.Details.PROPERTY_EMERGENCY_CALLBACK_MODE)
                || details.hasProperty(Call.Details.PROPERTY_NETWORK_IDENTIFIED_EMERGENCY_CALL);
    }

    private void callDisallow(Call.Details details) {
        Log.d("Carrion", "Disallowing call");
        CallResponse.Builder response = new CallResponse.Builder();
        response.setDisallowCall(true);
        respondToCall(details, response.build());
    }

    private void callSilence(Call.Details details) {
        Log.d("Carrion", "Silencing call");
        CallResponse.Builder response = new CallResponse.Builder();
        response.setSilenceCall(true);
        respondToCall(details, response.build());
    }

    private void callAllow(Call.Details details) {
        Log.d("Carrion", "Allowing call");
        respondToCall(details, new CallResponse.Builder().build());
    }

    private void sendNotification(String title, String content) {
        //Log.d("Carrion", "Trying to notify: " + title + ", " + content);
        if (isDeviceLocked()) {
            if (notificationManager == null) {
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

    private boolean isNumberInDatabase(String number) {
        //Log.d("Carrion", "NUMBER LOOKUP: " + number);
        if (database == null) {
            database = new File(getFilesDir() + "/complaint_numbers.txt.gz");
        }
        if (database.exists() && database.length() > 0) {
            if (databaseNumbers == null || databaseTimestamp != database.lastModified()) {
                databaseNumbers = new HashSet<>();
                databaseTimestamp = database.lastModified();
                try {
                    long startTime = System.currentTimeMillis();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(database.toPath()))));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.length() == 10 && line.chars().allMatch(Character::isDigit)) {
                            databaseNumbers.add(line);
                        }
                    }
                    reader.close();
                    Log.d("Carrion", "Loaded database with " + databaseNumbers.size() + " entries in " + (System.currentTimeMillis() - startTime) + "ms");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (databaseNumbers != null && number.length() == 18 && number.startsWith("tel:%2B1")) {
                //Log.d("Carrion", "Normalized number: " + number.substring(8));
                if (databaseNumbers.contains(number.substring(8))) {
                    Log.d("Carrion", "Number found in database");
                    return true;
                } else {
                    Log.d("Carrion", "Number not in database");
                }
            } else {
                Log.d("Carrion", "Number doesn't meet requirements to lookup");
            }
        } else {
            return false;
        }
        return false;
    }

    private void incrementIntPref(String pref) {
        int curValue = getDefaultSharedPreferences(this).getInt(pref, 0);
        getDefaultSharedPreferences(this).edit().putInt(pref, curValue + 1).apply();
    }
}
