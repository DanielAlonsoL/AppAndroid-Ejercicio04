package com.danielalonso.ejercicio04;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements Response.ErrorListener, Response.Listener<JSONObject>  {

    public static final int MY_DEFAULT_TIMEOUT = 10000;
    public static final int MY_DEFAULT_MAX_RETRIES = 3;

    String word;
    String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Creamos el intent
        Intent intentGame = new Intent(MainActivity.this, Game.class);
        Intent intentScore = new Intent(MainActivity.this, Score.class);

        MaterialButton btnPlay = (MaterialButton) findViewById(R.id.btnPlay);
        MaterialButton btnScore = (MaterialButton) findViewById(R.id.btnScore);

        // Asigna la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connect();

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentGame.putExtra(getString(R.string.word),word);
                intentGame.putExtra(getString(R.string.category),category);
                //Log.d("EXITO","Word: "+word+" Category: "+category);
                startActivity(intentGame);
            }
        });

        btnScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intentScore);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        connect();
    }

    public void connect(){
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        //Variables para realizar la conexión
        String url = getResources().getString(R.string.base_url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, this, this);
        request.setRetryPolicy(new DefaultRetryPolicy(
                MY_DEFAULT_TIMEOUT,
                MY_DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
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

    //**********************************************//
    // RESPUESTAS DEL SERVIDOR
    //**********************************************//
    @Override
    public void onErrorResponse(VolleyError error) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.title_error))
                .setMessage(getString(R.string.supporting_text))
                .setNeutralButton(getString(R.string.try_again),(dialog, which) ->
                        connect())
                .setPositiveButton(getString(R.string.accept),(dialog, which) ->
                        finish())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onResponse(JSONObject response) {
        Log.d("EXITO","CONEXION EXITOSA");
        try {
            word = response.getString("word").toUpperCase();
            category = response.getString("category");

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}