package com.example.android.sunshine.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

import com.example.android.sunshine.data.WeatherContract;

public class SunshineSyncUtils {

    /*
     * Interval at which to sync with the weather. Use TimeUnit for convenience, rather than
     * writing out a bunch of multiplication ourselves and risk making a silly mistake.
     */

    private static final int SYNC_INTERVAL_HOURS = 3;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;


    private static boolean sInitialized;

    // Used for identifying sync jobs
    private static final String SUNSHINE_SYNC_TAG = "sunshine-sync";

    /**
     * Schedules a repeating sync of Sunshine's weather data using FirebaseJobDispatcher.
     * @param context Context used to create the GooglePlayDriver that powers the
     *                FirebaseJobDispatcher
     */

    static void scheduleFirebaseJobDispatcherSync(@NonNull final Context context){

        Driver driver = new GooglePlayDriver(context);

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        // Create job to periodically sync weather data
        Job syncSunshineJob = dispatcher.newJobBuilder()
        // Service that will be used for syncing data
        .setService(SunshineFirebaseJobService.class)
                // Set the UNIQUE tag used to identify this Job
                .setTag(SUNSHINE_SYNC_TAG)
                /*
                 * Network constraints on which this Job should run. We choose to run on any
                 * network, but you can also choose to run only on un-metered networks or when the
                 * device is charging. It might be a good idea to include a preference for this,
                 * as some users may not want to download any data on their mobile plan. ($$$)
                 */
                .setConstraints(Constraint.ON_ANY_NETWORK)
                // Sets how long job should persist. Options: keep "forever" or die on next boot
                .setLifetime(Lifetime.FOREVER)
                // Set to recur to ensure data stays up to date
                .setRecurring(true)
                /*
                * We want the data to sync every 3-4 hours. First argument for Trigger's
                * static executionWindow method is the start of the time frame when
                * the sync should be done. The second argument is the latest point when
                * the data should be synced. Note: this end time isn't guaranteed, but is more
                * of a guideline for the the dispatcher to go off of
                * */
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                // If Job with given tag already exists, this new job will replace it
                .setReplaceCurrent(true)
                .build();

        // Schedule Job with the dispatcher
        dispatcher.schedule(syncSunshineJob);
    }

    /**
     * Creates periodic sync tasks and checks if an immediate sync is needed. If so, this method
     * will ensure that the sync occurs.
     *
     * @param context Context that will be passed to other methods and used to access the
     * ContentResolver
     */

    synchronized  public static void initialize(@NonNull final Context context){
        /*
         * Only initiate once per app lifetime. If initiation already done,
         * there is nothing to be done in this method
         */
        if(sInitialized) return;

        sInitialized = true;

        /*
         * This method call triggers Sunshine to create its task to synchronize weather data
         * periodically.
         */
        scheduleFirebaseJobDispatcherSync(context);

        /*
        * Need to check if ContentProvider has data to display to forecast list.
        * However, performing a query on the main thread should be avoided.
        * So, a thread will be used to run the query to check ContentProvider's content
        * */

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {

                // URI for all rows of weather data in weather table
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;

                /*
                * Since following query will be used to only check if there is data
                * (rather than to display), we only need to PROJECT the ID of all rows.
                * In the queries where we display the data, it is needed to PROJECT more
                * columns to tell what weather details are needed to display.
                * */

                String[] projectionColumns = {WeatherContract.WeatherEntry._ID};
                String selectionStatement = WeatherContract.WeatherEntry
                        .getSqlSelectForTodayOnwards();

                // Perform query to check if there is any weather data
                Cursor cursor = context.getContentResolver().query(
                    forecastQueryUri,
                    projectionColumns,
                    selectionStatement,
                    null,
                    null);

                /*
                 * A Cursor object can be null for various different reasons. A few are
                 * listed below.
                 *
                 *   1) Invalid URI
                 *   2) A certain ContentProvider's query method returns null
                 *   3) A RemoteException was thrown.
                 *
                 * Bottom line, it is generally a good idea to check if a Cursor returned
                 * from a ContentResolver is null.
                 *
                 * If the Cursor was null OR if it was empty, we need to sync immediately to
                 * be able to display data to the user.
                 */

                // If cursor is null or empty, immediately sync
                if(cursor == null || cursor.getCount() == 0){
                    startImmediateSync(context);
                }

                // Close cursor afterwards
                cursor.close();
                return null;
            }
        }.execute();

    }


    /**
     * Helper method to perform a sync immediately using an IntentService for asynchronous
     * execution.
     *
     * @param context The Context used to start the IntentService for the sync.
     */

    public static void startImmediateSync(@NonNull final Context context){
        Intent intentToSyncImmediately = new Intent(context, SunshineSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }
}
