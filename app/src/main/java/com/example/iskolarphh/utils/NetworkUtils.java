package com.example.iskolarphh.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkUtils {
    
    private static final String TAG = "NetworkUtils";
    private static final String IP_SERVICE_URL = "https://api.ipify.org?format=text";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    /**
     * Get the user's public IP address
     * @param callback Callback to receive the IP address
     */
    public static void getPublicIpAddress(IpCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(IP_SERVICE_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String ipAddress = reader.readLine();
                    reader.close();
                    
                    if (ipAddress != null && !ipAddress.isEmpty() && isValidIp(ipAddress)) {
                        callback.onIpReceived(ipAddress);
                    } else {
                        Log.w(TAG, "Invalid IP received: " + ipAddress);
                        callback.onIpReceived("unknown");
                    }
                } else {
                    Log.w(TAG, "Failed to get IP. Response code: " + responseCode);
                    callback.onIpReceived("unknown");
                }
                connection.disconnect();
            } catch (IOException e) {
                Log.e(TAG, "Error getting public IP", e);
                callback.onIpReceived("unknown");
            }
        });
    }
    
    /**
     * Validate if the string is a valid IP address
     */
    private static boolean isValidIp(String ip) {
        return ip.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    }
    
    /**
     * Check if network is available
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
    
    /**
     * Get network type (WiFi, Mobile, etc.)
     */
    public static String getNetworkType(Context context) {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                return activeNetworkInfo.getTypeName();
            }
        }
        return "unknown";
    }
    
    public interface IpCallback {
        void onIpReceived(String ipAddress);
    }
}
