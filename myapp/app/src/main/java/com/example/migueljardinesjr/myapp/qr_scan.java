package com.example.migueljardinesjr.myapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.view.ViewGroup;
import android.view.View;
import android.view.View.OnClickListener;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;
import java.net.UnknownHostException;
import java.io.IOException;
import android.support.annotation.NonNull;
import android.graphics.PorterDuff;


public class qr_scan extends Activity {
    private ImageButton startButton;
    private boolean recording = false;

    public byte[] buffer;
    public static DatagramSocket socket;
    private int port=50005;

    AudioRecord recorder;

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private int sampleRate = 16000; // 44100 for music
    //private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //private int audioFormat = AudioFormat.ENCODING_PCM_8BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private boolean status = true;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_qr_scan);

        requestPermissions(permissions, REQUEST_RECORD_AUDIO_PERMISSION);


        startButton = (ImageButton) findViewById (R.id.start_button);

        startButton.setOnClickListener (handleListener);


    }

    private final OnClickListener handleListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if(!recording){
                startListener(v);
            }
            else {
                stopListener(v);
            }
        }

    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    public void stopListener(View v){
        recording = false;
        ImageView recordButton = (ImageView)v;
        recordButton.setColorFilter(getResources().getColor(R.color.shell_blue), PorterDuff.Mode.SRC_IN);
        status = false;
        recorder.release();

        Log.d(LOG_TAG,"Recorder released");
    };

    public void startListener(View v){
        recording = true;
        ImageView recordButton = (ImageView)v;
        recordButton.setColorFilter(getResources().getColor(R.color.recordRED), PorterDuff.Mode.SRC_IN);
        startStreaming();
    };

    public void startStreaming() {


        Thread streamThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    DatagramSocket socket = new DatagramSocket();
                    Log.d("VS", "Socket Created");

                    byte[] buffer = new byte[minBufSize];

                    Log.d("VS","Buffer created of size " + minBufSize);
                    DatagramPacket packet;
                    //"192.168.56.1"
                    final InetAddress destination = InetAddress.getByName("10.108.120.62");
                    Log.d("VS", "Address retrieved");
                    Log.d("VS", destination.toString());

                    //recorder = findAudioRecord();//new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);
                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*100);

                    if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                    {
                        Log.d("VS", "Recorder Initialized");

                    }else{
                        Log.d("VS", "Recorder NOT Initialized");

                    }


                    recorder.startRecording();


                    while(status == true) {


                        //reading data from MIC into buffer
                        minBufSize = recorder.read(buffer, 0, buffer.length);

                        //putting buffer in the packet
                        packet = new DatagramPacket (buffer,buffer.length,destination,port);

                        // byteArray = new byte[5];
                        //byte [] byteArray = {'a', 'T','E','S','T'};
                        //DatagramPacket packet1 = new DatagramPacket(byteArray,5,destination,port);
                        //packet1 = byteArray;

                        socket.send(packet);
                        System.out.println("MinBufferSize: " +minBufSize);


                    }

                    //recorder.release();

                } catch(UnknownHostException e) {
                    Log.e("VS", "UnknownHostException");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("VS", "IOException");
                }
            }

        });
        streamThread.start();
    }

    private int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
    int bufferSize;
    static final int RECORDINGDURATION = 10000;

    AudioRecord audioInput = findAudioRecord();

    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] {
                    AudioFormat.ENCODING_PCM_8BIT,
                    AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] {
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        Log.d("Mic2", "Attempting rate " + rate
                                + "Hz, bits: " + audioFormat
                                + ", channel: " + channelConfig);
                        bufferSize = AudioRecord.getMinBufferSize(rate,
                                channelConfig, audioFormat);

                        if (RECORDINGDURATION * sampleRate != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a
                            // success
                            AudioRecord recorder = new AudioRecord(
                                    MediaRecorder.AudioSource.DEFAULT, rate,
                                    channelConfig, audioFormat, bufferSize);
                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                sampleRate = rate;
                            return recorder;
                        }
                    } catch (Exception e) {
                        Log.e("VS", rate + "Exception, keep trying.", e);
                    }
                }
            }
        }
        return null;
    }
}