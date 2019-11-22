package com.example.bhati.routeapplication.Testing;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.bhati.routeapplication.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Test extends AppCompatActivity  implements View.OnClickListener {
    RecordAudio recordTask;
    PlayAudio playTask;
    Button startRecordingButton, stopRecordingButton, startPlaybackButton,
            stopPlaybackButton;
    TextView statusText;
    File recordingFile;
    boolean isRecording = false,isPlaying = false;
    DataOutputStream dos;
    int bufferSize;
    AudioRecord audioRecord;
    int frequency = 11025,channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        statusText = (TextView) this.findViewById(R.id.StatusTextView);

        startRecordingButton = (Button) this
                .findViewById(R.id.StartRecordingButton);
        stopRecordingButton = (Button) this
                .findViewById(R.id.StopRecordingButton);
        startPlaybackButton = (Button) this
                .findViewById(R.id.StartPlaybackButton);
        stopPlaybackButton = (Button) this
                .findViewById(R.id.StopPlaybackButton);

        startRecordingButton.setOnClickListener(this);
        stopRecordingButton.setOnClickListener(this);
        startPlaybackButton.setOnClickListener(this);
        stopPlaybackButton.setOnClickListener(this);

        stopRecordingButton.setEnabled(false);
        startPlaybackButton.setEnabled(false);
        stopPlaybackButton.setEnabled(false);

        File path = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/com.apress.proandroidmedia.ch07.altaudiorecorder/files/");
        path.mkdirs();
        try {
            recordingFile = File.createTempFile("recording", ".pcm", path);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create file on SD card", e);
        }
        intializeAudio();
    }

    public void onClick(View v) {
        if (v == startRecordingButton) {
            record();
        } else if (v == stopRecordingButton) {
            stopRecording();
        } else if (v == startPlaybackButton) {
            play();
        } else if (v == stopPlaybackButton) {
            stopPlaying();
        }
    }

    public void play() {
        startPlaybackButton.setEnabled(true);

        playTask = new PlayAudio();
        playTask.execute();

        stopPlaybackButton.setEnabled(true);
    }

    public void stopPlaying() {
        isPlaying = false;
        stopPlaybackButton.setEnabled(false);
        startPlaybackButton.setEnabled(true);
    }

    public void record() {
        startRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(true);
        startPlaybackButton.setEnabled(true);
        recordTask = new RecordAudio();
        recordTask.execute();
    }
    public void stopRecording() {
        isRecording = false;
    }

    private class PlayAudio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            isPlaying = true;

            int bufferSize = AudioTrack.getMinBufferSize(frequency,channelConfiguration, audioEncoding);
            short[] audiodata = new short[bufferSize / 4];

            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(recordingFile)));
                AudioTrack audioTrack = new AudioTrack(
                        AudioManager.STREAM_MUSIC, frequency,
                        channelConfiguration, audioEncoding, bufferSize,
                        AudioTrack.MODE_STREAM);

                audioTrack.play();
                while (isPlaying && dis.available() > 0) {
                    int i = 0;
                    while (dis.available() > 0 && i < audiodata.length) {
                        audiodata[i] = dis.readShort();
                        i++;
                    }
                    audioTrack.write(audiodata, 0, audiodata.length);
                }
                dis.close();
                startPlaybackButton.setEnabled(false);
                stopPlaybackButton.setEnabled(true);
            } catch (Throwable t) {
                Log.e("AudioTrack", "Playback Failed");
            }
            return null;
        }
    }
    private class RecordAudio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            isRecording = true;
            try {


                short[] buffer = new short[bufferSize];
                audioRecord.startRecording();
                int r = 0;
                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0,
                            bufferSize);
                    for (int i = 0; i < bufferReadResult; i++) {
                        dos.writeShort(buffer[i]);
                    }
                    publishProgress(new Integer(r));
                    r++;
                }
//                audioRecord.stop();
//                dos.close();
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }
        protected void onProgressUpdate(Integer... progress) {
            statusText.setText(progress[0].toString());
        }
        protected void onPostExecute(Void result) {
            startRecordingButton.setEnabled(true);
            stopRecordingButton.setEnabled(false);
            startPlaybackButton.setEnabled(true);
        }
    }
    public void intializeAudio()
    {
        try {

             dos = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(
                            recordingFile)));
             bufferSize = AudioRecord.getMinBufferSize(frequency,
                    channelConfiguration, audioEncoding);
             audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, frequency,
                    channelConfiguration, audioEncoding, bufferSize);
        }
        catch (Exception ex)
        {

        }
    }

}
