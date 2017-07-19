package com.hear2.aidansliney.hear2;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cleveroad.audiovisualization.AudioVisualization;
import com.cleveroad.audiovisualization.DbmHandler;


public class ScrollingActivity extends AppCompatActivity {

    private AudioManager audioManager;
    private AudioVisualization audioVisualization;
    private Record record;

    int listening = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Aidan: linking the visualiser
        //https://github.com/Cleveroad/WaveInApp
        audioVisualization = (AudioVisualization) findViewById(R.id.visualizer_view);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        final SeekBar slider = (SeekBar) findViewById(R.id.slider);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                if (listening  == 0)
                {
                    //Get audio permission if we don't already have it
                    if (ContextCompat.checkSelfPermission(ScrollingActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        Log.d("log","Request Permission");
                        ActivityCompat.requestPermissions(ScrollingActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                    }
                    else
                    {
                        Log.d("log","We already have the permission");
                    }
                    //start listening
                    fade(findViewById(R.id.noisyScene));
                    startQuietAnimation();
                    fab.setImageResource(android.R.drawable.ic_media_pause);
                    listening  = 1; //on
                    Snackbar.make(view, "Listening started...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    int audioSessionId = audioManager.generateAudioSessionId();
                    record = new Record(audioSessionId);
                    record.start();
                    audioVisualization.linkTo(DbmHandler.Factory.newVisualizerHandler(ScrollingActivity.this, audioSessionId));
                    audioVisualization.onResume();
                } else {
                    //stop listening
                    fadeIn(findViewById(R.id.noisyScene));
                    fab.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
                    listening  = 0; //off
                    if (record != null) {
                        record.stopRecording();
                    }
                    audioVisualization.onPause();
                    Snackbar.make(view, "Listening stopped...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        //Slider
        TextView seekbar_0 = (TextView) findViewById(R.id.seekbar_0);
        seekbar_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slider.setProgress(0);
            }
        });

        TextView seekbar_100 = (TextView) findViewById(R.id.seekbar_100);
        seekbar_100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slider.setProgress(100);
            }
        });
    }

    @Override
    public void onDestroy() {
        audioVisualization.release();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        audioVisualization.onResume();
    }

    @Override
    public void onPause() {
        audioVisualization.onPause();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.info_settings) {
            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(ScrollingActivity.this);
            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage(R.string.app_name).setTitle(R.string.welcome);
            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();
            return true;
        }
        if (id == R.id.rate_settings) {
            rateApp();
            return true;
        }

        if (id == R.id.feedback_settings) {
            sendEmail();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //Send users to rate the app
    public void rateApp() {
        try {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        } catch (ActivityNotFoundException e) {
            Intent rateIntent = rateIntentForUrl("http://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }

    //Send users to rate the app
    private Intent rateIntentForUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21) {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        } else {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }

    public void sendEmail(){
        Log.i("Send email", "");
        String[] TO = {"aidansliney@gmail.com"};
        String[] CC = {"aidansliney@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hear Feedback");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Please write your feedback here. The more info the better");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {

        }
    }

    // animations
    public void move(View view, int durat){
        ImageView image = (ImageView) view;
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_right);
        animation1.setDuration(durat);
        image.startAnimation(animation1);
    }
    //refactor: Merge the below
    public void fade(View view){
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        view.startAnimation(animation1);
        animation1.setFillAfter(true);
    }

    public void fadeIn(View view){
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        view.startAnimation(animation1);
        animation1.setFillAfter(true);
    }

    public void startQuietAnimation() {
        move(findViewById(R.id.balloon), 85000);
        move(findViewById(R.id.cloud),425000);
        move(findViewById(R.id.boat), 100000);

    }

}
