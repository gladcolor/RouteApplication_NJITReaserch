package com.example.bhati.routeapplication.Model;

import android.app.Application;

//import com.example.audiolib.AndroidAudioConverter;
//import com.example.audiolib.callback.ILoadCallback;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        AndroidAudioConverter.load(this, new ILoadCallback() {
//            @Override
//            public void onSuccess() {
//                // Great!
//            }
//            @Override
//            public void onFailure(Exception error) {
//                // FFmpeg is not supported by device
//                error.printStackTrace();
//            }
//        });
    }
}