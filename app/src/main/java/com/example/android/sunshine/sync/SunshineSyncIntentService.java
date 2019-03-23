package com.example.android.sunshine.sync;

import android.app.IntentService;
import android.content.Intent;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class SunshineSyncIntentService extends IntentService {

    // Constructor that calls super and pass name of this class
    public SunshineSyncIntentService(){
        super("SunshineSyncIntentService");
    }

    @Override
    protected void onHandleIntent( Intent intent) {
        SunshineSyncTask.syncWeather(this);
    }
}
