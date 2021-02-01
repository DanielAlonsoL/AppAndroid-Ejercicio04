package com.danielalonso.ejercicio04;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class Score extends AppCompatActivity {

    int score;
    private TextView tvScore;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        int defaultValue = 0;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // Se obtiene el score actual
        score = sharedPref.getInt(getString(R.string.saved_high_score_key), defaultValue);
        tvScore = (TextView) findViewById(R.id.tvScore);
        tvScore.setText(" "+score+" ");

        //Agregando toolbar a la pantalla principal
        toolbar = findViewById(R.id.toolbar_contact);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.aboutMe:
                // User chose the "Settings" item, show the app settings UI...
                Intent about = new Intent(this, About.class);
                startActivity(about);
                break;

            case R.id.contact:
                Intent contact = new Intent(this, Contact.class);
                startActivity(contact);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}