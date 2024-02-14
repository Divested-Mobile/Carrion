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

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MainActivity extends Activity {

    private TextView logView;
    private File database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault_DayNight);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        logView = findViewById(R.id.txtLogOutput);
        logView.setMovementMethod(new ScrollingMovementMethod());
        logView.setTextIsSelectable(false);

        if (!getSystemService(RoleManager.class).isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);
            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
            startActivityForResult(intent, new Random().nextInt());
            logView.append("I am not the screening service, requesting permission.\n");
        } else {
            logView.append("I am the screening service.\n");
        }

        database = new File(getFilesDir() + "/complaint_numbers.txt.gz");
        if (database.exists()) {
            logView.append("Database available.\n");
        } else {
            logView.append("Database not available.\n");
        }

        logView.append("Stats:\n");
        logView.append("\tVerification passed: " + getDefaultSharedPreferences(this).getInt("STAT_VERIFICATION_PASSED", 0) + "\n");
        logView.append("\tVerification failed: " + getDefaultSharedPreferences(this).getInt("STAT_VERIFICATION_FAILED", 0) + "\n");
        logView.append("\tVerification unknown: " + getDefaultSharedPreferences(this).getInt("STAT_VERIFICATION_UNKNOWN", 0) + "\n");
        logView.append("\tMatched database: " + getDefaultSharedPreferences(this).getInt("STAT_MATCHED_DATABASE", 0) + "\n");
        logView.append("\tExcluded: " + getDefaultSharedPreferences(this).getInt("STAT_EXCLUDED", 0) + "\n");
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.toggleSilenceUnknown).setChecked(getDefaultSharedPreferences(this).getBoolean("PREF_SILENCE_UNKNOWN", false));
        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mnuUpdateDatabaseFull) {
            if(isNetworkAvailable(this)) {
                logView.append("Downloading full database...\n");
                downloadDatabase("https://divested.dev/complaint_numbers.txt.gz", database);
            } else {
                logView.append("No Internet, can't download database.\n");
            }
        } else if (item.getItemId() == R.id.mnuUpdateDatabaseHighconf) {
            if(isNetworkAvailable(this)) {
                logView.append("Downloading high confidence only database...\n");
                downloadDatabase("https://divested.dev/complaint_numbers-highconf.txt.gz", database);
            } else {
                logView.append("No Internet, can't download database.\n");
            }
        } else if (item.getItemId() == R.id.mnuDeleteDatabase) {
            if (database != null && database.exists()) {
                logView.append("Deleting database...\n");
                if (database.delete()) {
                    logView.append("Deleted database.\n");
                } else {
                    logView.append("Failed to delete database.\n");
                }
            } else {
                logView.append("Database not available.\n");
            }
        } else if (item.getItemId() == R.id.toggleBlockUnknown) {
            if (!item.isChecked()) {
                getDefaultSharedPreferences(this).edit().putBoolean("PREF_BLOCK_UNKNOWN", true).apply();
            } else {
                getDefaultSharedPreferences(this).edit().putBoolean("PREF_BLOCK_UNKNOWN", false).apply();
            }
            item.setChecked(!item.isChecked());
        } else if (item.getItemId() == R.id.toggleSilenceUnknown) {
            if (!item.isChecked()) {
                getDefaultSharedPreferences(this).edit().putBoolean("PREF_SILENCE_UNKNOWN", true).apply();
            } else {
                getDefaultSharedPreferences(this).edit().putBoolean("PREF_SILENCE_UNKNOWN", false).apply();
            }
            item.setChecked(!item.isChecked());
        }
        return super.onOptionsItemSelected(item);
    }

    private void downloadDatabase(String url, File out) {
        new Thread(() -> {
            try {
                File outNew = new File(out + ".new");
                if (outNew.exists()) {
                    outNew.delete();
                }
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(90000);
                connection.setReadTimeout(30000);
                connection.addRequestProperty("User-Agent", "Carrion");
                if (out.exists()) {
                    connection.setIfModifiedSince(out.lastModified());
                }
                connection.connect();
                int res = connection.getResponseCode();
                if (res != 304) {
                    if (res == 200) {
                        FileOutputStream fileOutputStream = new FileOutputStream(outNew);
                        final byte[] data = new byte[1024];
                        int count;
                        while ((count = connection.getInputStream().read(data, 0, 1024)) != -1) {
                            fileOutputStream.write(data, 0, count);
                        }
                        fileOutputStream.close();
                        outNew.renameTo(out); //Move the new file into place
                        logView.append("Database download successful.\n");
                    } else {
                        logView.append("Database download failed.\n");
                    }
                } else {
                    logView.append("Database already latest.\n");
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    //Credit: https://stackoverflow.com/a/4239019
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
