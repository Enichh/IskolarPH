package com.example.iskolarphh.util;

import android.util.Log;
import android.view.Choreographer;
import android.os.Handler;
import android.os.Looper;

/**
 * Performance monitoring utility for tracking frame times and performance metrics
 */
public class PerformanceMonitor {
    
    private static final String TAG = "PerformanceMonitor";
    private static final long FRAME_TIME_THRESHOLD_MS = 16; // 60fps threshold
    private static boolean isMonitoring = false;
    private static Choreographer.FrameCallback frameCallback;
    private static long lastFrameTime = 0;
    
    public static void startMonitoring() {
        if (isMonitoring) return;
        
        isMonitoring = true;
        frameCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (lastFrameTime != 0) {
                    long frameTimeMs = (frameTimeNanos - lastFrameTime) / 1_000_000;
                    
                    if (frameTimeMs > FRAME_TIME_THRESHOLD_MS) {
                        Log.w(TAG, "Slow frame detected: " + frameTimeMs + "ms (threshold: " + FRAME_TIME_THRESHOLD_MS + "ms)");
                    }
                }
                
                lastFrameTime = frameTimeNanos;
                
                if (isMonitoring) {
                    Choreographer.getInstance().postFrameCallback(this);
                }
            }
        };
        
        Choreographer.getInstance().postFrameCallback(frameCallback);
        Log.d(TAG, "Performance monitoring started");
    }
    
    public static void stopMonitoring() {
        if (!isMonitoring) return;
        
        isMonitoring = false;
        lastFrameTime = 0;
        Log.d(TAG, "Performance monitoring stopped");
    }
    
    public static void logMethodExecutionTime(String methodName, long startTime) {
        long duration = System.nanoTime() - startTime;
        long durationMs = duration / 1_000_000;
        
        if (durationMs > 10) {
            Log.w(TAG, "Slow method: " + methodName + " took " + durationMs + "ms");
        } else {
            Log.d(TAG, "Method: " + methodName + " took " + durationMs + "ms");
        }
    }
    
    public static void logMemoryUsage(String tag) {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        
        Log.d(TAG, tag + " - Memory usage: " + 
              (usedMemory / 1024 / 1024) + "MB / " + 
              (maxMemory / 1024 / 1024) + "MB (" + 
              ((usedMemory * 100) / maxMemory) + "%)");
    }
}
