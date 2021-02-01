package com.danielalonso.ejercicio04;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.danielalonso.ejercicio04.adapter.LetterAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

public class Game extends AppCompatActivity implements Response.ErrorListener, Response.Listener<JSONObject>{
    public static final int MY_DEFAULT_TIMEOUT = 10000;
    public static final int MY_DEFAULT_MAX_RETRIES = 3;

    private static TextView[] charViews;
    private static String word;
    private static String category;

    private TextView tvCategory;
    private LinearLayout wordLayout;

    private LetterAdapter adapter;
    private GridView gridView;

    private static int numCorr = 0;
    private static int numErrors = 0;
    private static int numChars;
    private int score;

    private ImageView parts;
    private int sizeParts = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Bundle bundle = getIntent().getExtras();

        word = bundle.getString(getString(R.string.word));
        category = bundle.getString(getString(R.string.category));

        tvCategory = (TextView) findViewById(R.id.tvCategory);
        wordLayout = (LinearLayout) findViewById(R.id.words) ;
        gridView = (GridView) findViewById(R.id.letters);
        parts = (ImageView) findViewById(R.id.parts);

        // Asigna la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try{
            playGame();
        } catch (RuntimeException runtimeException){
            connect();
        }

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

    public void playGame(){
        tvCategory.setText(""+category);
        charViews = new TextView[word.length()];

        wordLayout.removeAllViews();
        parts.setImageResource(R.drawable.start);

        for(int i = 0; i<word.length(); i++){
            charViews[i] = new TextView(this);
            charViews[i].setText(""+word.charAt(i));
            charViews[i].setLayoutParams(new ViewGroup.LayoutParams(
                    100,
                    100));
            charViews[i].setGravity(Gravity.CENTER);
            charViews[i].setTextColor(Color.WHITE);
            charViews[i].setBackgroundResource(R.drawable.letter_bg);
            wordLayout.addView(charViews[i]);
        }

        adapter = new LetterAdapter(this);
        gridView.setAdapter(adapter);
        numCorr = 0;
        numErrors = 0;
        numChars = word.length();
    }

    public void letterPressed(View view){
        String letter = ((TextView) view).getText().toString();
        char letterChar = letter.charAt(0);

        view.setEnabled(false);
        boolean correct = false;

        for(int i=0; i<word.length(); i++){
            if(word.charAt(i) == letterChar){
                correct = true;
                numCorr++;
                charViews[i].setTextColor(Color.BLACK);
            }
        }

        MediaPlayer mp;
        if(correct){
            if(numCorr == numChars){
                // Si adivina la palabra, se incrementa el contador de score
                int defaultValue = 0;
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                // Se obtiene el score actual
                SharedPreferences.Editor editor = sharedPref.edit();
                score = sharedPref.getInt(getString(R.string.saved_high_score_key), defaultValue);
                score++;
                // Se guarda el nuevo score
                editor.putInt(getString(R.string.saved_high_score_key), score);
                editor.apply();
                score = sharedPref.getInt(getString(R.string.saved_high_score_key), defaultValue);

                mp = MediaPlayer.create(this, R.raw.applause);
                mp.start();

                disableButtons();

                new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.you_won))
                    .setMessage(getString(R.string.message_won)+word)
                    .setPositiveButton(getString(R.string.play_again), (dialog, which) -> connect())
                    .setNegativeButton(getString(R.string.exit), (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
            }
        }
        else{
            numErrors++;
            if(numErrors == 1) {
                parts.setImageResource(R.drawable.head);
            }
            else if(numErrors == 2) {
                parts.setImageResource(R.drawable.body);
            }

            else if(numErrors == 3){
                parts.setImageResource(R.drawable.arm_right);
            }
            else if(numErrors == 4){
                parts.setImageResource(R.drawable.arm_left);
            }
            else if(numErrors == 5){
                parts.setImageResource(R.drawable.leg_left);
            }
            else if(numErrors == sizeParts){
                mp = MediaPlayer.create(this, R.raw.boo);
                mp.start();
                disableButtons();

                parts.setImageResource(R.drawable.game_over);
                new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.you_lose))
                    .setMessage(getString(R.string.message_lose)+word)
                    .setPositiveButton(getString(R.string.play_again), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        connect();
                        }
                    })
                    .setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
            }
        }
    }

    public void disableButtons(){
        for(int i = 0; i<gridView.getChildCount(); i++){
            gridView.getChildAt(i).setEnabled(false);
        }
    }

    public void connect(){
        RequestQueue queue = Volley.newRequestQueue(Game.this);
        //Variables para realizar la conexión
        String url = getResources().getString(R.string.base_url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, this, this);
        request.setRetryPolicy(new DefaultRetryPolicy(
                MY_DEFAULT_TIMEOUT,
                MY_DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
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
        try {
            word = response.getString(getString(R.string.word)).toUpperCase();
            category = response.getString(getString(R.string.category));
            //Log.d("EXITO","CONEXION EXITOSA: word: "+word+" category: "+category);
            playGame();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}