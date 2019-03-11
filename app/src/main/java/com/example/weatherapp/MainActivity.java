package com.example.weatherapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.weatherapp.data.SunshinePreferences;
import com.example.weatherapp.utilities.NetworkUtils;
import com.example.weatherapp.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity {

    // Create field to store weather display TextView
    private TextView mWeatherTextView;

    // Add a TextView variable for the error message display
    private TextView mErrorMessageDisplay;

    // Add a ProgressBar variable to show and hide the progress bar
    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        // Use findViewById to get a reference to the weather display TextView
        /* This TextView is used to display errors and will be hidden if there are no errors */
        mWeatherTextView = (TextView) findViewById(R.id.tv_weather_data);

        // Find the TextView for the error message using findViewById
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message);

        // Find the ProgressBar using findViewById
        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        loadWeatherData();
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
        mWeatherTextView.setVisibility(View.INVISIBLE);

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
        mWeatherTextView.setVisibility(View.VISIBLE);
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
            mWeatherTextView.setText("");
            loadWeatherData();
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
                mWeatherTextView.setVisibility(View.VISIBLE);

                for(String weatherString : weatherData){
                    mWeatherTextView.append((weatherString) + "\n\n\n");
                }
            } else{
                // If the weather data was null, show the error message
                showErrorMessage();
            }
        }
    }
}
