package com.jpwolfso.qstilebackuprestore;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.SupportMenuInflater;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import javax.xml.datatype.Duration;

public class MainActivity extends AppCompatActivity {

    String tiles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();

        tiles = Settings.Secure.getString(getContentResolver(),"sysui_qs_tiles");
        final SharedPreferences sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final boolean nobackup = (sharedPreferences.getString("value1", null) == null);

        if (nobackup) {
            findViewById(R.id.restore).setEnabled(false);
        }

        findViewById(R.id.backup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("value1", tiles);
                editor.commit();
                Toast.makeText(MainActivity.this, "QS Tiles backed up", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.restore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Settings.Secure.putString(getContentResolver(), "sysui_qs_tiles", sharedPreferences.getString("value1", null));
                    Toast.makeText(MainActivity.this, "QS Tiles restored", Toast.LENGTH_SHORT).show();
                    tiles = Settings.Secure.getString(getContentResolver(),"sysui_qs_tiles");
                    invalidateOptionsMenu();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, String.valueOf(e), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        SupportMenuInflater supportMenuInflater = new SupportMenuInflater(this);
        supportMenuInflater.inflate(R.menu.overflow,menu);

        checkString(menu,"MobileData",R.id.action_mobiledata);
        checkString(menu,"Hotspot",R.id.action_hotspot);
        checkString(menu,"VoLte",R.id.action_volte);
        checkString(menu,"DataSaver",R.id.action_datasaver);
        checkString(menu,"PrivateDnsTileService",R.id.action_privatedns);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case (R.id.action_mobiledata): {
                        Settings.Secure.putString(getContentResolver(), "sysui_qs_tiles", tiles + ",MobileData");
                        Toast.makeText(getApplicationContext(), "Added Mobile Data QS tile", Toast.LENGTH_LONG).show();
                        break;
                }
                case (R.id.action_hotspot): {
                    Settings.Secure.putString(getContentResolver(), "sysui_qs_tiles", tiles + ",Hotspot");
                    Toast.makeText(getApplicationContext(), "Added Hotspot tile", Toast.LENGTH_LONG).show();
                    break;
                }
                case (R.id.action_volte): {
                    Settings.Secure.putString(getContentResolver(), "sysui_qs_tiles", tiles + ",VoLte");
                    Toast.makeText(getApplicationContext(), "Added VoLTE QS tile", Toast.LENGTH_LONG).show();
                    break;
                }
                case (R.id.action_datasaver): {
                    Settings.Secure.putString(getContentResolver(), "sysui_qs_tiles", tiles + ",DataSaver");
                    Toast.makeText(getApplicationContext(), "Added Data Saver QS tile", Toast.LENGTH_LONG).show();

                    break;
                }
                case (R.id.action_privatedns): {
                    try {
                        PackageManager pm = getPackageManager();
                        pm.getPackageInfo("com.jpwolfso.privdnsqt",0);
                        Settings.Secure.putString(getContentResolver(), "sysui_qs_tiles", tiles + ",custom(com.jpwolfso.privdnsqt/.PrivateDnsTileService)");
                        Toast.makeText(getApplicationContext(), "Added Private DNS QS tile", Toast.LENGTH_LONG).show();
                    } catch (PackageManager.NameNotFoundException e) {

                        new AlertDialog.Builder(this).setTitle("Private DNS QS Tile not found").setMessage("Click OK to open the F-Droid app page for the Private DNS Quick Tile app.")
                                .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse("https://f-droid.org/en/packages/com.jpwolfso.privdnsqt/"));
                                            startActivity(intent);
                                        }
                                }).setNegativeButton("Cancel",null).show();
                    }
                    break;
                }
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, String.valueOf(e), Toast.LENGTH_SHORT).show();
        }

        tiles = Settings.Secure.getString(getContentResolver(),"sysui_qs_tiles");
        invalidateOptionsMenu();

        return super.onOptionsItemSelected(item);
    }

    public void checkString(Menu menu, String tilestr, int id) {
        if (tiles.contains(tilestr)) {
            menu.findItem(id).setVisible(false);
        }
    }
}

