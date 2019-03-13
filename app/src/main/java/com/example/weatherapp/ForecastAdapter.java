package com.example.weatherapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    private String[] mWeatherData;

    // Create a final private ForecastAdapterOnClickHandler called mClickHandler
    private final ForecastAdapterOnClickHandler mClickHandler;

    // Add an interface called ForecastAdapterOnClickHandler
    // Within that interface, define a void method that access a String as a parameter
    /**
     * The interface that receives onClick messages.
     */
    public interface ForecastAdapterOnClickHandler{
        void onClick(String weatherForDay);
    }

    // Add a ForecastAdapterOnClickHandler as a parameter to the constructor and store it in mClickHandler
    public ForecastAdapter(ForecastAdapterOnClickHandler clickHandler){
        mClickHandler = clickHandler;
    }

    // Create a class within ForecastAdapter called ForecastAdapterViewHolder
    // Implement OnClickListener in the ForecastAdapterViewHolder class
    // Extend RecyclerView.ViewHolder
    /**
     * Cache of the children views for a forecast list item.
     */
    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Create a public final TextView variable called mWeatherTextView
        public final TextView mWeatherView;

        // Create a constructor for this class that accepts a View as a parameter
        // Call super(view)
        // Using view.findViewById, get a reference to this layout's TextView and save it to mWeatherTextView
        public ForecastAdapterViewHolder(View view){
            super(view);
            mWeatherView = (TextView) view.findViewById(R.id.tv_weather_data);
            // Call setOnClickListener on the view passed into the constructor (use 'this' as the OnClickListener)
            view.setOnClickListener(this);
        }

        // Override onClick, passing the clicked day's data to mClickHandler via its onClick method
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String weatherForDay = mWeatherData[adapterPosition];
            mClickHandler.onClick(weatherForDay);
        }
    }

    // Override onCreateViewHolder
    // Within onCreateViewHolder, inflate the list item xml into a view
    // Within onCreateViewHolder, return a new ForecastAdapterViewHolder with the above view passed in as a parameter
    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.forecast_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new ForecastAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param forecastAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
        String weatherForThisDay = mWeatherData[position];
        forecastAdapterViewHolder.mWeatherView.setText(weatherForThisDay);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        if (mWeatherData == null) return 0;
        return mWeatherData.length;
    }

    /**
     * This method is used to set the weather forecast on a ForecastAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new ForecastAdapter to display it.
     *
     * @param weatherData The new weather data to be displayed.
     */
    public void setWeatherData(String[] weatherData) {
        mWeatherData = weatherData;
        notifyDataSetChanged();
    }
}
