package com.aware.standalone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aware.Aware;
import com.aware.plugin.google.activity_recognition.Algorithm;
import com.aware.plugin.google.activity_recognition.ContextCard;
import com.aware.plugin.google.activity_recognition.Plugin;
import com.aware.plugin.google.activity_recognition.Settings;

public class Standolone extends AppCompatActivity {

    private static TextView current_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standolone);

        //Start AWARE framework
        Intent aware = new Intent(this, Aware.class);
        startService(aware);

        //Now we can turn on sensors or plugins...
        //For this example, we will simply use Google Activity Recognition Plugin and show the current activity on the interface
        //Let's import Google Activity Plugin module to our application now
        //Now make the plugin a library you will want to use on your application
        //We now need to bring the plugin ContentProvider to inside your application's manifest. This is because the ContentProvider applicationId needs to match your application's applicationId
        //Even though we made the plugin a library, we haven't told Gradle that we are using this library on Standalone, let's do it now
        //Since the plugin itself was doing the icon replacement already, we need to remove this call from the plugin's manifest.
        //Simply remove the icon declaration on the manifest of the library plugin module

        //OK, now we have the plugin bundled with the application and AWARE too
        //Let's show the context card of the plugin on our application

        //Start the activity recognition plugin
        Aware.setSetting(this, Settings.STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, true, "com.aware.plugin.google.activity_recognition");
        Aware.setSetting(this, Settings.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, 60, "com.aware.plugin.google.activity_recognition"); //every minute, this is in seconds
        Aware.startPlugin(this, "com.aware.plugin.google.activity_recognition");

        //Once it's started, load the card into our interface
        LinearLayout activity_card = (LinearLayout) findViewById(R.id.activity_card);
        activity_card.addView(new ContextCard().getContextCard(getApplicationContext()));

        //That's it. If we run this application now, it will:
        /**
         * Start AWARE
         * Start the activity recognition plugin
         * Load the context card from the plugin
         * Show the information of the plugin inside your application UI
         */

        //Oops, forgot to add AWARE's content providers to my app too :) Let's do that now. Just make sure to check on Github what is the current list of providers:
        //https://github.com/denzilferreira/aware-client/blob/master/aware-core/src/main/AndroidManifest.xml
        //Just copy the <provider> nodes to your standalone manifest

        //Lets now show the current detected activity on the UI, by listening to the plugin's broadcasts
        current_activity = (TextView) findViewById(R.id.current_activity);

        IntentFilter filter = new IntentFilter(); //create a filter for our broadcast receiver
        filter.addAction(Plugin.ACTION_AWARE_GOOGLE_ACTIVITY_RECOGNITION); //what is the context broadcast I'm interested

        registerReceiver(activityListener, filter); //register the listener with Android OS
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(activityListener); //remember to unregister when you quit your application. The plugin continues collecting the data even when you leave your application because it is a service

        //If you want to stop it
        Aware.stopPlugin(getApplicationContext(), "com.aware.plugin.google.activity_recognition");
    }

    public class ActivityListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            current_activity.setText(Algorithm.getActivityName(intent.getIntExtra(Plugin.EXTRA_ACTIVITY, -1)));

            //Another alternative is to query the database directly when the context changes
            //You can do a ContentObserver or a direct Cursor call. Check the website for the tutorial on how to use either of these
        }
    }
    private ActivityListener activityListener = new ActivityListener();
}
