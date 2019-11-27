package com.jpwolfso.qstilebackuprestore;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();

        final String tiles = Settings.Secure.getString(getContentResolver(),"sysui_qs_tiles");
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
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, String.valueOf(e), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    }

