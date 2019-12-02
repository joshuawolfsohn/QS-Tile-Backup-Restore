package com.jpwolfso.qstilebackuprestore;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.SupportMenuInflater;

import static android.content.Intent.ACTION_GET_CONTENT;

public class MainActivity extends AppCompatActivity {

    String tiles;
    final int MY_STORAGE_PERMISSION = 1;
    final int MY_FILE_RESULTCODE = 2;
    Context context;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        context = this;

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_STORAGE_PERMISSION);
        }

        if (checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(context).setMessage("To restore your QS tiles, this app requires the WRITE_SECURE_SETTINGS permission.")
                    .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishAndRemoveTask();
                        }
                    }).setPositiveButton("Next", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new AlertDialog.Builder(context).setMessage("In an ADB shell, please run the command 'pm grant com.jpwolfso.qstilebackuprestore android.permission.WRITE_SECURE_SETTINGS' and then restart this app.")
                            .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finishAndRemoveTask();
                                }
                            }).setPositiveButton("OK", null).setCancelable(false).show();
                }
            }).setCancelable(false).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            findViewById(R.id.restore).setEnabled(false);
        } else {
            findViewById(R.id.restore).setEnabled(true);
        }

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            findViewById(R.id.backup).setEnabled(false);
            findViewById(R.id.restore).setEnabled(false);
        } else {
            findViewById(R.id.backup).setEnabled(true);
            findViewById(R.id.restore).setEnabled(true);
        }

        findViewById(R.id.backup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tiles = Settings.Secure.getString(getContentResolver(), "sysui_qs_tiles");
                try {
                    file = new File(Environment.getExternalStorageDirectory() + "/qstiles", "backup-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".txt");
                    file.getParentFile().mkdirs();
                    file.createNewFile();

                    FileWriter writer = new FileWriter(file);
                    writer.append(tiles);
                    writer.flush();
                    writer.close();

                    Toast.makeText(MainActivity.this, "QS tiles backed up to " + file.toString(), Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    Toast.makeText(context, String.valueOf(e), Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.restore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(ACTION_GET_CONTENT);
                    intent.setType("text/plain");
                    startActivityForResult(Intent.createChooser(intent, "Select a QS tile backup"), MY_FILE_RESULTCODE);

                    tiles = Settings.Secure.getString(getContentResolver(),"sysui_qs_tiles");
                    invalidateOptionsMenu();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, String.valueOf(e), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_FILE_RESULTCODE) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri uri = data.getData();
                    file = new File(uri.getPath());
                    InputStream test = getContentResolver().openInputStream(uri);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(test));
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(bufferedReader.readLine());

                    Settings.Secure.putString(getContentResolver(), "sysui_qs_tiles", stringBuilder.toString());
                    Toast.makeText(context, "QS tiles restored", Toast.LENGTH_SHORT).show();
                    invalidateOptionsMenu();
                } catch (IOException e) {
                    Toast.makeText(context, String.valueOf(e), Toast.LENGTH_SHORT);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        SupportMenuInflater supportMenuInflater = new SupportMenuInflater(this);
        supportMenuInflater.inflate(R.menu.overflow,menu);

        tiles = Settings.Secure.getString(getContentResolver(), "sysui_qs_tiles");

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

