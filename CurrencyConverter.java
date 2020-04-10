import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CurrencyConverter {
    private static CurrencyConverter instance = null;
    // essential URL structure is built using constants
    private boolean isWaitingForApi = false;
    private static  final int updateFrequencyInDays = 1;
    private static final String LAST_UPDATED = "last_updated";
    private static final String TAG = "CURRENCY_CONVERTER";
    private static final String ACCESS_KEY = "YOUR_ACCESS_KEY";
    private static final String BASE_URL = "http://api.currencylayer.com/";
    private static final String ENDPOINT = "live";
    private static final String FILE_NAME = "exchange_rates.json";
    private JSONObject currencies;

    private CurrencyConverter(Application context) {
        ///Execute on creation.
        loadJSONFile(context);
    }

    public static CurrencyConverter getInstance(Application context) {
        if(instance == null) instance = new CurrencyConverter(context);
        return instance;
    }

    private String getRequestUrl(){
        return String.format("%s%s?access_key=%s", BASE_URL, ENDPOINT, ACCESS_KEY);
    }

    private void loadJSONFile(Context context){
        FileHandler fileHandler = new FileHandler(context);
        Log.i(TAG,"Attempting to open cached currencies, file name: " + FILE_NAME);
        String fileStr = fileHandler.readFile(FILE_NAME);
        if(fileStr == null){
            //File does not exist or could not be accessed, let's create a new one!
            requestApi(context);
            return;
        }

        try {
            Log.i(TAG,"JSON file -> " + fileStr);

            currencies = new JSONObject(fileStr);

            if(currencies.isNull("quotes") || currencies.isNull(LAST_UPDATED)){
                //The file might be corrupted..
                Log.e(TAG, "File is invalid, calling API to create a new file.");
                requestApi(context);
                return;
            }

            long lastUpdated = currencies.getLong(LAST_UPDATED);
            Date today = Calendar.getInstance().getTime();
            long diff = Math.abs(today.getTime() - lastUpdated);
            long daysDiff = diff / 86400000;

            if(daysDiff > updateFrequencyInDays){
                //The information is outdated!
                Log.i(TAG, "File is outdated, calling API to update the file.");
                requestApi(context);
            }
            Log.i(TAG, "File has been successfully loaded onto local variable.");
        } catch (JSONException e) {
            Log.e(TAG, "The file was corrupted or its content cannot be interpreted. We'll create a new one!");
            e.printStackTrace();
            requestApi(context);

        }
    }
    private void requestApi(Context context){
        FileHandler fileHandler = new FileHandler(context);
        isWaitingForApi = true;
        if(IsInternetConnection(context)){
            RequestQueue queue = Volley.newRequestQueue(context);
            String URL_BASE = getRequestUrl();
            Log.i(TAG, "Calling API -> " + URL_BASE);
            queue.add(
                    new JsonObjectRequest(
                            Request.Method.GET,
                            URL_BASE,
                            (JSONObject) null, response -> {
                        Log.i(TAG, "Response from the API -> " + response.toString());

                        // Save it to local variable.
                        currencies = response;

                        try {
                            //Save last updated...
                            currencies.put(LAST_UPDATED, Calendar.getInstance().getTime().getTime());
                        } catch (JSONException e) {
                            Log.e(TAG, "Cannot save last update...");
                        }
                        isWaitingForApi = false;
                        Log.i(TAG, "Requesting file handler to save the file -> " + currencies.toString());
                        fileHandler.writeStringToFile(currencies.toString(), FILE_NAME);
                    }, error -> Log.e(TAG, "API response error ->  " + error.toString()))
            );
        }
        else
            Log.i(TAG, "No internet connection available");

    }
    private boolean IsInternetConnection(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        return  Objects.requireNonNull(cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED
                || Objects.requireNonNull(cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;

    }

    public double convert(String from, String to, double amount) {
        if(isWaitingForApi) return -1;

        JSONObject quotes;
        try {
            quotes = currencies.getJSONObject("quotes");
            double currency_1 = quotes.getDouble("USD" + from);
            double currency_2 = quotes.getDouble("USD" + to);

            if(from.equals("USD") || to.equals("USD")){
                if(from.equals("USD"))
                    return amount * currency_2;
                else
                    return amount / currency_1;
            }
            double dollar = currency_2 * amount;
            return dollar / currency_1;

        } catch (JSONException e) {
            Log.e(TAG, "The file has not been loaded onto the variable or the file content is corrupted...");
            e.printStackTrace();
            return -1;
        }
    }
}
