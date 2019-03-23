package com.example.android.sunshine.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.android.sunshine.data.WeatherContract;

public class SunshineSyncUtils {

    private static boolean sInitialized;

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
