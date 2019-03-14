package com.example.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherapp.data.SunshinePreferences;
import com.example.weatherapp.utilities.NetworkUtils;
import com.example.weatherapp.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements ForecastAdapter.ForecastAdapterOnClickHandler {

    // Add a private RecyclerView variable called mRecyclerView
    private RecyclerView mRecyclerView;

    // Add a private ForecastAdapter variable called mForecastAdapter
    private ForecastAdapter mForecastAdapter;

    // Add a TextView variable for the error message display
    private TextView mErrorMessageDisplay;

    // Add a ProgressBar variable to show and hide the progress bar
    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        // Use findViewById to get a reference to the RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);

        // Find the TextView for the error message using findViewById
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message);

        // Create layoutManager, a LinearLayoutManager with VERTICAL orientation and shouldReverseLayout == false
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        // Set the layoutManager on mRecyclerView
        mRecyclerView.setLayoutManager(layoutManager);

        // Use setHasFixedSize(true) on mRecyclerView to designate that all items in the list will have the same size
        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        // Set mForecastAdapter equal to a new ForecastAdapter
        // Pass in 'this' as the ForecastAdapterOnClickHandler
        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         */
        mForecastAdapter = new ForecastAdapter(this);

        // Use mRecyclerView.setAdapter and pass in mForecastAdapter
        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mForecastAdapter);

        // Find the ProgressBar using findViewById
        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        loadWeatherData();
    }

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param weatherForDay The weather for the day that was clicked
     */
    @Override
    public void onClick(String weatherForDay) {
        Context context = this;

        // Launch the DetailActivity using an explicit Intent
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);

        // Pass the weather to the DetailActivity
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay);

        startActivity(intentToStartDetailActivity);

    }

    // Create a method called showErrorMessage that will hide the weather data and show the error message
    /**
     * This method will make the error message visible and hide the weather
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage(){
        // At start, hide the currently visible data
        mRecyclerView.setVisibility(View.INVISIBLE);

        // After, display the error message
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    // Create a method called showWeatherDataView that will hide the error message and show the weather data
    /**
     * This method will make the View for the weather data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showWeatherDataView(){
        // At start, ensure error message is hidden
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);

        // After, ensure weather data is visible
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    // Create a method that will get the user's preferred location and execute your new AsyncTask and call it loadWeatherData
    /**
     * This method will get the user's preferred location for weather, and then tell some
     * background method to get the weather data in the background.
     */
    private void loadWeatherData(){
        // Call showWeatherDataView before executing the AsyncTask
        showWeatherDataView();

        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        new FetchWeatherTask().execute(location);
    }

    // Override onCreateOptionsMenu to inflate the menu for this Activity
    // Return true to display the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();

        /* Use the inflater's inflate method to inflate menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu);

        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    // Override onOptionsItemSelected to handle clicks on the refresh button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_refresh){
            // Set the adapter to null before refreshing
            mForecastAdapter.setWeatherData(null);
            loadWeatherData();
            return true;
        }

        if(id == R.id.action_map){
            openLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Create a class that extends AsyncTask to perform network requests
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{

        // Within your AsyncTask, override the method onPreExecute and show the loading indicator
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        // Override the doInBackground method to perform your network requests
        @Override
        protected String[] doInBackground(String... params) {
            // If no zip code exists
            if (params.length == 0) return null;

            String location = params[0];
            URL weatherRequestUrl = NetworkUtils.buildUrl(location);

            try{
                String jsonWeatherResponse = NetworkUtils
                        .getResponseFromHttpUrl(weatherRequestUrl);

                String[] simpleJsonWeatherData = OpenWeatherJsonUtils
                        .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

                return simpleJsonWeatherData;
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        // Override the onPostExecute method to display the results of the network request
        @Override
        protected void onPostExecute(String[] weatherData) {
            // As soon as the data is finished loading, hide the loading indicator
            mLoadingIndicator.setVisibility(View.INVISIBLE);

            if(weatherData != null){
                // If the weather data was not null, make sure the data view is visible
                showWeatherDataView();
                // Use mForecastAdapter.setWeatherData and pass in the weather data
                mForecastAdapter.setWeatherData(weatherData);
            } else{
                // If the weather data was null, show the error message
                showErrorMessage();
            }
        }
    }

    /**
     * This method uses the URI scheme for showing a location found on a
     * map. This super-handy intent is detailed in the "Common Intents"
     * page of Android's developer site:
     *
     * @see <a"http://developer.android.com/guide/components/intents-common.html#Maps">
     *
     * Hint: Hold Command on Mac or Control on Windows and click that link
     * to automagically open the Common Intents page
     */
    private void openLocationInMap(){
        String addressString = "1600 Ampitheatre Parkway, CA";
        Uri geolocation = Uri.parse("geo:0,0?q=" + addressString);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geolocation);

        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        } else{
            Log.d("ERROR", "Can't call " + geolocation.toString());
        }
    }
}
