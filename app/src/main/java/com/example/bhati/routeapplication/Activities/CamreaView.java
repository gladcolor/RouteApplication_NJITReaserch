package com.example.bhati.routeapplication.Activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.AsyncTask;
import android.os.HandlerThread;

import android.graphics.Bitmap;

import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.CameraDevice;
import android.media.Image;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.io.FileOutputStream;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.example.bhati.routeapplication.Database.DBHelper;
import com.example.bhati.routeapplication.Model.GPSTracker;
import com.example.bhati.routeapplication.R;
import com.example.bhati.routeapplication.Servicess.background_location_updates;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.mapbox.mapboxsdk.geometry.LatLng;

import com.iceteck.silicompressorr.SiliCompressor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import static com.example.bhati.routeapplication.Servicess.background_location_updates.Latitude;
import static com.example.bhati.routeapplication.Servicess.background_location_updates.Longitude;

public class CamreaView extends AppCompatActivity  implements SurfaceHolder.Callback, SensorEventListener {
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    public MediaRecorder mrec = new MediaRecorder();
    private Button startRecording = null;
    private android.hardware.Camera mCamera;
    String vido_file_path,vfile="";
    ToggleButton btnrescordaudio;
    Button recoderbtn,recoder_stop, take_photo;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private boolean isVideoCapturing;
    ArrayList<LatLng> arrayList;
    ArrayList<String[]> arrayori;
    public long lastcheck;
    private Sensor mAccelerometers;
    private SensorManager mSensorMenager;
    ArrayList<String> arrayList_recorder;
    Bundle bundle = new Bundle();
    private DBHelper myDb;
    FFmpeg fFmpeg;
    File video_file;
    MyReceiver myReceiver;
    Timer timer;
    Intent ServiceIntent;
    private boolean receiversRegistered;
    Thread thread;
    boolean is_userRecordingAudio=false;
    ArrayList<String> arrayList_video;
    TextView recorder_timer;
    Timer t;
    int minute =0, seconds = 0, hour = 0;
    JSONObject obj ;
    JSONArray jsonArray;
    JSONObject finalobject;
    Double loclat,loclog;
    GPSTracker gpsloc;
    public static int timelen=10000;
    public static int audiositu=1;
    public Vector<Long> pictime;
    public long eachround;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_capture_dialogue);

        Intent getintent=getIntent();
        timelen=getintent.getIntExtra("len",8)*2000;
        audiositu=getintent.getIntExtra("audio",1);
        System.out.println("audio"+audiositu);
        System.out.println("camera:"+timelen);
        arrayList = new ArrayList<>();
        arrayori = new ArrayList<>();
        pictime =new Vector<Long>();
        arrayList_recorder=new ArrayList<>();
        arrayList_video = new ArrayList<>();
        obj = new JSONObject();
        jsonArray = new JSONArray();
        myDb = new DBHelper(this);
        //gpsloc = new GPSTracker(CamreaView.this);
        lastcheck=System.currentTimeMillis();
        mSensorMenager=(SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometers=mSensorMenager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //claim sensor
        myReceiver = new MyReceiver();
        Start_Service();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                Start_Service();
                LatLng l1 = new LatLng();
                l1.setLatitude(properties.loclat);
                l1.setLongitude(properties.loclog);
                Double a1=properties.locang;
                Double a2=properties.locdir;
                Double a3=properties.locrol;
                // why is this happening
                arrayList.add(l1);
                arrayori.add(new String[]{String.valueOf(String.valueOf(a1)),String.valueOf(String.valueOf(a2)),String.valueOf(String.valueOf(a3)),});
                //properties.loclat= gpsloc.getLatitude();
                //properties.loclog= gpsloc.getLongitude();
                Log.d( "lat:log",l1.getLatitude()+":"+l1.getLongitude());
                Log.d("Angle",Double.toString(a1)+":"+Double.toString(a2)+":"+Double.toString(a3));

                //Log.d( "lat:log",l1.getLatitude()+","+gpsloc.getLocation().getLatitude()+":"+l1.getLongitude()+","+gpsloc.getLocation().getLongitude());
            }
        }, 0, 1000);
        setUiWidgets();
        initializefFmpeg();
    }
    protected void startRecording() throws IOException
    {
        final long currentTimeMillis = System.currentTimeMillis();
        File folder = new File(Environment.getExternalStorageDirectory() + "/RouteApp");
        if (!folder.exists()) { folder.mkdir();
        }
        CamcorderProfile prof=CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        vido_file_path = currentTimeMillis + ".mp4";
        System.out.println(vido_file_path);
        vfile=currentTimeMillis+"";
        video_file = new File(folder, vido_file_path);
        mrec = new MediaRecorder();  // Works well
        mCamera.unlock();
        mrec.setCamera(mCamera);
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        if (audiositu==1) {
            mrec.setAudioSource(MediaRecorder.AudioSource.MIC);
            mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        }
        else {
            mrec.setOutputFormat(prof.fileFormat);
            mrec.setVideoFrameRate(prof.videoFrameRate);
            mrec.setVideoSize(prof.videoFrameWidth, prof.videoFrameHeight);
            mrec.setVideoEncodingBitRate(prof.videoBitRate);
            mrec.setVideoEncoder(prof.videoCodec);
        }
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setOutputFile(video_file.getPath());
        mrec.prepare();
        mrec.start();
        eachround=System.currentTimeMillis();
    }

    private void GenerateAudioChunks(File audio,String start,String last,File folder,int ct) {
        fFmpeg = FFmpeg.getInstance(CamreaView.this);
        ProgressDialog progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        start = start.replace(" ","");
        last = last.replace(" ","");
        String st = start.split(":")[2];
        String lt = last.split(":")[2];
        Log.d("audiofileloc:",audio.getName());
        String ffile = folder+"/"+audio.getName();
        String opfile=folder+"/chunk_"+audio.getName().substring(0,audio.getName().length()-4)+"_"+st+"_"+lt+"_"+ct+".wav";
        Log.d("commamd:",opfile);
        //String opfile=start+"_"+last+"_chumk.mp3";
        //String command = "-i "+audio+" -acodec copy -ss "+st+" -to "+lt+" "+folder+"/"+st+"_"+lt+"_chumk.wav";
        String command = "-i "+ffile+" -ss "+start+" -to "+last+" -c copy "+opfile;
        Log.d("commamd:",command);
        String[] cmd = command.split(" ");

        try {
            fFmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                    //  Log.d("FFMpeg", "onStart: ");
                    progress.setMessage("Generating Audio Chunks");
                    progress.show();
                }

                @Override
                public void onProgress(String message) {
//                    progress.setMessage(message);
                    //Log.d("FFMpeg", message);
                }

                @Override
                public void onFailure(String message) {
                    // Log.d("FFMpeg",message);
                    Toast.makeText(CamreaView.this, "Failed Chunk"+message, Toast.LENGTH_SHORT).show();
                    Log.d("FAILCHUMK:",message);
                    progress.dismiss();
                }

                @Override
                public void onSuccess(String message) {
                    progress.dismiss();
                    Toast.makeText(CamreaView.this, "chunks generated!", Toast.LENGTH_SHORT).show();
                    //speechtotext(opfile);
                    /*filePath = opfile;
                    try {
                        //new UploadFileToServer().execute();
                        chumkUpload(filePath);
                    }catch (Exception ex)
                    {
                        Log.d("CHUMKEXP:","Error:",ex);
                    }*/
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (Exception e) {
            Log.d("FFMpeg", "AudioChumk: " , e);
            e.printStackTrace();
        }

    }



    public void createAudioFile()
    {
        final long currentTimeMillis = System.currentTimeMillis();
        final String audio_file_path = vfile + ".wav";
        File folder = new File(Environment.getExternalStorageDirectory() + "/RouteApp");
        if (!folder.exists()) { folder.mkdir();
        }
        File outfile = new File(folder, audio_file_path);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera != null){
            Camera.Parameters parameters = mCamera.getParameters();
            List<String>    focusModes = parameters.getSupportedFocusModes();
            if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else
            if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Camera not available!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorMenager.registerListener(this, mAccelerometers, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        if (isVideoCapturing){
            long currtime=System.currentTimeMillis();
            if(currtime-lastcheck>200){
                if (event.sensor.equals(mAccelerometers)){
                    lastcheck=currtime;
                    properties.locang= Double.valueOf(event.values[0]);
                    properties.locdir= Double.valueOf(event.values[1]);
                    properties.locrol=Math.toDegrees(event.values[2]);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){}

    public void IntialValues()
    {
        preferences = getSharedPreferences("isVideoCapturing", MODE_PRIVATE);
    }
    public void setUiWidgets()
    {
        mCamera = Camera.open();
        recorder_timer=findViewById(R.id.recorder_timer);
        surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        btnrescordaudio=findViewById(R.id.btnrescordaudio);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(CamreaView.this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        createAudioFile();
        IntialValues();
        btnrescordaudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {

                    is_userRecordingAudio=true;
                }
                else
                {
                    is_userRecordingAudio=false;
                }
            }
        });
        recoderbtn=findViewById(R.id.recoder);
        recoderbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    StartRecodingTime(); //Don not change
                    recordingThreadStart(); //Do not change
                    recoderbtn.setEnabled(false);
                    recoderbtn.setAlpha(0.5f);
                    recoder_stop.setEnabled(true);
                    recoder_stop.setAlpha(1f);
                    isVideoCapturing = true;
                    editor = preferences.edit();
                    editor.putBoolean("is_video_capturing", true);
                    editor.apply();
                    startRecording();
                    System.out.println("yes I am here");
                    /*new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {*/
                            /*if (isVideoCapturing==true){
                                recoder_stop.performClick();
                            }*/
                            /*System.out.println("ndndndnd");
                            if (isVideoCapturing==true){
                                System.out.println("didididididid");
                                mrec.stop();
                                mCamera.stopPreview();
                                Uri file_uri=Uri.fromFile(video_file);
                                float file_size=video_file.length();
                                final long currentTimeMillis=System.currentTimeMillis();
                                final String audio_file_path=vfile+"wav";
                                CreatSubExcelSheet(video_file.getName());
                                Toast.makeText(CamreaView.this, "Video Saved !", Toast.LENGTH_SHORT).show();
                                updateCsvFile(arrayList, video_file.getName(), arrayori);//要清理arrayori
                                //storeDataInDb(file_uri, video_file, file_size, name,audio_file_path);
                                try {
                                    startRecording();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    },timelen);*/
                    Handler handler=new Handler();
                    Runnable run=new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("ndndndnd");
                            if (isVideoCapturing==true) {
                                System.out.println("didididididid");
                                mrec.stop();
                                mCamera.stopPreview();
                                Uri file_uri = Uri.fromFile(video_file);
                                System.out.println("filenameis: "+video_file.getAbsolutePath());
                                float file_size = video_file.length();
                                final long currentTimeMillis = System.currentTimeMillis();
                                final String audio_file_path = vfile + "wav";
                                File folder = new File(Environment.getExternalStorageDirectory() + "/RouteApp"+"/pic");
                                if (!folder.exists()) { folder.mkdir();
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getVideoFrame(video_file.getAbsolutePath(),folder.getAbsolutePath(),pictime);
                                    }
                                }).start();
                                //getVideoFrame(video_file.getAbsolutePath(),folder.getAbsolutePath(),pictime);
                                //File compressfolder= new File(Environment.getExternalStorageDirectory() + "/RouteApp"+"/video");
                                //if (!compressfolder.exists()) { compressfolder.mkdir();
                                //}
                                //new VideoCompressAsyncTask(CamreaView.this).execute(video_file.getAbsolutePath(),compressfolder.getPath());
                                CreatSubExcelSheet(video_file.getName());
                                Toast.makeText(CamreaView.this, "Video Saved !", Toast.LENGTH_SHORT).show();
                                updateCsvFile(arrayList, video_file.getName(), arrayori);//要清理arrayori
                                arrayList.clear();
                                arrayori.clear();
                                pictime =new Vector<Long>();
                                //storeDataInDb(file_uri, video_file, file_size, name,audio_file_path);
                                try {
                                    startRecording();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                handler.postDelayed(this,timelen);
                            }
                        }
                    };
                    handler.postDelayed(run,timelen);
                    /*Toast.makeText(CamreaView.this, properties.loclat+""+properties.loclog, Toast.LENGTH_SHORT).show();
                    if(properties.loclog==0.0 || properties.loclat==0.0)
                    {
                        Toast.makeText(CamreaView.this, "Location not yet fetched!", Toast.LENGTH_SHORT).show();
                    }else
                    {
                        StartRecodingTime();
                        recordingThreadStart();
                        recoderbtn.setEnabled(false);
                        recoderbtn.setAlpha(0.5f);
                        recoder_stop.setEnabled(true);
                        recoder_stop.setAlpha(1f);
                        isVideoCapturing = true;
                        editor = preferences.edit();
                        editor.putBoolean("is_video_capturing", true);
                        editor.apply();
                        startRecording();
                    }*/

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        take_photo=findViewById(R.id.photo);
        take_photo.setAlpha(0.5f);
        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pictime.add(System.currentTimeMillis()-eachround);
            }
        });
        recoder_stop=findViewById(R.id.recoder_stop);
        recoder_stop.setEnabled(false);
        recoder_stop.setAlpha(0.5f);
        recoder_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                     finalobject = new JSONObject();
                    finalobject.put("Data", jsonArray);
                    //properties.finalobject.put("Data",jsonArray);
                    //properties.jsonArray = jsonArray;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                is_userRecordingAudio=false;
                t.cancel();
                try {
                    mrec.stop();
                }catch (RuntimeException e) {
                    e.printStackTrace();
                }
                mCamera.stopPreview();
                isVideoCapturing = false;
                editor = preferences.edit();
                editor.putBoolean("is_video_capturing", false);
                editor.apply();
                Uri file_uri=Uri.fromFile(video_file);
                float file_size=video_file.length();
                Log.d("FILE_URI","IS:"+file_uri);
                Log.d("FILE_SIZE","IS:"+file_size);
                Log.d("FILE_path","IS:"+video_file);
                Log.d("LATLNG","SIZE:"+arrayList.size());
                Log.d("OTHERS","ARE :"+finalobject.toString());
                Log.d("OTHERS","ARE :"+finalobject.length());
                File folder = new File(Environment.getExternalStorageDirectory() + "/RouteApp"+"/pic");
                if (!folder.exists()) { folder.mkdir();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getVideoFrame(video_file.getAbsolutePath(),folder.getAbsolutePath(),pictime);
                    }
                }).start();
                //File compressfolder= new File(Environment.getExternalStorageDirectory() + "/RouteApp"+"/video");
                //if (!compressfolder.exists()) { compressfolder.mkdir();}
                //new VideoCompressAsyncTask(CamreaView.this).execute(video_file.getAbsolutePath(),compressfolder.getPath());
                // dialogueSave(file_uri,video_file,file_size);
                CreatSubExcelSheet(video_file.getName());
                Toast.makeText(CamreaView.this, "Video Saved !", Toast.LENGTH_SHORT).show();
                updateCsvFile(arrayList, video_file.getName(), arrayori);//要清理arrayori
                arrayList.clear();
                arrayori.clear();
            }
        });
    }



    public void dialogueSave(Uri uri,File file,float size)
    {
        Dialog dialog = new Dialog(CamreaView.this);
        dialog.setContentView(R.layout.dialog);
        Button btnPlay = dialog.findViewById(R.id.btnPlay);
        ImageView btnCancel = dialog.findViewById(R.id.btnCancelImage);
        Button btnRetry = dialog.findViewById(R.id.btnRetry);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        VideoView videoView = dialog.findViewById(R.id.videoView);
        Uri finalUri = uri;
        btnPlay.setOnClickListener(v -> {
            videoView.setVideoURI(finalUri);
            videoView.start();

        });
        btnSave.setOnClickListener(v -> {
            dialog.dismiss();
            recoder_stop.setEnabled(false);
            recoder_stop.setAlpha(0.5f);
            recoderbtn.setEnabled(true);
            recoderbtn.setAlpha(1);
            Dialog dialogUsername = new Dialog(CamreaView.this, R.style.MyDialogTheme);
            dialogUsername.setContentView(R.layout.username);
            dialogUsername.setCancelable(false);
            EditText username = dialogUsername.findViewById(R.id.edUsername);
            Button btnSave2 = dialogUsername.findViewById(R.id.btnSave);
            Button btnCancel2 = dialogUsername.findViewById(R.id.btnCancel);
            btnSave2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = username.getText().toString();
                    if (TextUtils.isEmpty(name)) {
                        username.setError("Required field");
                        return;
                    }
                    dialogUsername.dismiss();
                    final long currentTimeMillis = System.currentTimeMillis();
                    final String audio_file_path = vfile + ".wav";
                    CreatSubExcelSheet(video_file.getName());
                    // after creating subexcel sheet now update the sub excel sheet
                    Toast.makeText(CamreaView.this, "Video Saved !", Toast.LENGTH_SHORT).show();
                    updateCsvFile(arrayList, video_file.getName(), arrayori);
                    storeDataInDb(finalUri, file, size, name,audio_file_path);
                }
            });
            btnCancel2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogUsername.dismiss();
                }
            });

            dialogUsername.show();

            //  convertToAudio(new File(String.valueOf(finalUri)));  //TODO convert Speech to text
            /**/
        });

        btnRetry.setOnClickListener(v -> {
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void getVideoFrame(String path, String svpath, Vector<Long> timearray) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        System.out.println(path);
        System.out.println(timearray);
        for (Long time:timearray) {
            long timeUs = time * 1000;
            System.out.println("savepic");
            System.out.println(timeUs);
            Bitmap pic=mmr.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            String npath=svpath+"/"+System.currentTimeMillis()+".png";
            File picdic=new File(npath);
            saveBitmap(pic,picdic);
        }
        File fdelete=new File(path);
        if (fdelete.exists()) {
            fdelete.delete();
        }
    }
    public static boolean saveBitmap(Bitmap bitmap, File file) {
        if (bitmap == null) {
            System.out.println("no bit");
            return false;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            System.out.println("did trans");
            fos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private void storeDataInDb(Uri finalUri, File file, Float size, String username,String audio_file_path) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String date = dateFormat.format(calendar.getTime());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String time = timeFormat.format(calendar.getTime());
        size = size / 1000;
        String size_in_mbs = String.format("%.2f", size);
        if (arrayList.size() > 0) {
            String city_name = GetCurrentLocationName(arrayList.get(0).getLatitude(), arrayList.get(0).getLongitude());
            boolean i = myDb.insertData(finalUri, file.getName(),
                    "speech", arrayList.toString(), date,
                    size_in_mbs + " MB", time, date,
                    city_name, username,audio_file_path,finalobject.toString());
            SaveDataLocallly(username, city_name, date, time, getVideoTime(finalUri.toString()), size_in_mbs + "MB",
                    finalUri.toString(), file.getName(),arrayList.toString(),audio_file_path);
            if (i) {
                arrayList.clear();
                arrayori.clear();
                File folder = new File(Environment.getExternalStorageDirectory() + "/RouteApp");
                if (!folder.exists()) {
                    folder.mkdir();
                }
                Log.d("AUDIO_LIST","Size:"+arrayList_video.size());
                for (int j=0;j<arrayList.size();j++)
                {
                    //Log.d("ITEM_Name","Is"+arrayList_video.get(j));
                    //String[] separated = arrayList_video.get(j).split(",");
                    //String point_lat =separated[0]; // this wil   l contain "Fruit"
                    //String point_long_time=separated[1];
                    String point_lat = String.valueOf(arrayList.get(j).getLatitude());
                    String point_long_time = String.valueOf(arrayList.get(j).getLongitude());
                    //Log.d("REMIANING","STRING"+point_long_time);
                    //String point_time=separated[2];
                    //String point_systemtime=separated[3];
                    // adding data to excel sheet
//                    saveSubListSheet(getCurrentDate(),"0:00",point_lat,point_long_time,file.getName(),convertNumberIntoTimeFormat(j+1),audio_file_path,"",
//                            video_file.getName(),j+1);
                }
                //convertToAudio(file,audio_file_path,folder);
            }
        } else {
            // commenting this line to check if the file not deleted everytime after creation
            //deleteUnUsedFile(finalUri.getPath(),file);
            Toast.makeText(CamreaView.this, "No Points to save.", Toast.LENGTH_SHORT).show();
        }

    }
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            //Log.d("BroadcastReceiver", "onReceive:");
            // Bundle bundle = getIntent().getExtras().getBundle("LatLngBundle");
            //bundle = arg1.getBundleExtra("LatLngBundle");
            //if (bundle != null) {
              //  arrayList = bundle.getParcelableArrayList("LatLng");
                // Log.d("BroadcastReceiver", "onReceive: array = " + arrayList);
            //}
        }

    }
    private String GetCurrentLocationName(double lat, double lng) {
        Geocoder geocoder = new Geocoder(CamreaView.this);
        if (geocoder.isPresent()) {
            try {
                geocoder = new Geocoder(CamreaView.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    String add = address.getLocality();
                    return add;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        return null;
    }
    private String getVideoTime(String videoUri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, Uri.parse(videoUri));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time );
        Log.d("Adapter", "getVideoTime: "+timeInMillisec);
        retriever.release();
        // SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        // mTimeText.setText("Time: " + dateFormat.format(timeInMillisec));
        //timeInMillisec = 5000;
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeInMillisec),
                TimeUnit.MILLISECONDS.toMinutes(timeInMillisec) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeInMillisec)),
                TimeUnit.MILLISECONDS.toSeconds(timeInMillisec) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillisec)));

        return hms;
    }
    public void deleteUnUsedFile(String path,File file) {
        File fdelete = new File(path);
        try {
            if (fdelete.exists()) {
                file.getCanonicalFile().delete();
                getApplicationContext().deleteFile(file.getName());
            } else {
                Log.d("file Deleted :", "IS :" + path);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void SaveDataLocallly(String user_name, String city_name, String date, String time, String duaration,
                                 String size, String video_path, String video_name,String cordniate,String audio_file) {
        int row = myDb.getAllData().size();
        String Fnamexls = "localDb" + ".xls";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/RouteApp");
        directory.mkdirs();
        File file = new File(directory, Fnamexls);
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        Workbook workbook;
        try {
            int a = 1;
            workbook = Workbook.getWorkbook(file, wbSettings);
            WritableWorkbook wb = Workbook.createWorkbook(file, workbook);
            //workbook.createSheet("Report", 0);
            WritableSheet sheet = wb.getSheet("First Sheet");
            Label cell_user_name = new Label(0, row, user_name);
            Label cell_city_name = new Label(1, row, city_name);
            Label cell_date = new Label(2, row, date);
            Label cell_time = new Label(3, row, time);
            Label cell_duration = new Label(4, row, duaration);
            Label cell_size = new Label(5, row, size);
            Label cell_path = new Label(6, row, video_path);
            Label cell_name = new Label(7, row, video_name);
            Label cell_cordniate = new Label(7, row, cordniate);
            Label cell_file_audio = new Label(8, row, audio_file);
            try {
                sheet.addCell(cell_user_name);
                sheet.addCell(cell_city_name);
                sheet.addCell(cell_date);
                sheet.addCell(cell_time);
                sheet.addCell(cell_duration);
                sheet.addCell(cell_size);
                sheet.addCell(cell_path);
                sheet.addCell(cell_name);
                sheet.addCell(cell_cordniate);
                sheet.addCell(cell_file_audio);

            } catch(Exception e){
                //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
//            catch (RowsExceededException e) {
//                // TODO Auto-generated catch block
//                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                e.printStackTrace();
//            } catch (WriteException e) {
//                // TODO Auto-generated catch block
//                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                e.printStackTrace();
//            }
            wb.write();
            wb.close();
            try {
                workbook.close();
            } catch (Exception ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }
            //createExcel(excelSheet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

//        } catch (BiffException e) {
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        } catch (WriteException e) {
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }
    }
    private void convertToAudio(File video,String file_path,File folder) {
        fFmpeg = FFmpeg.getInstance(CamreaView.this);
        ProgressDialog progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        //String command  = "ffmpeg -i "+video+" -vn -ar 44100 -ac 2 -ab 192k -f mp3 Sample.mp3";
        //String command  = "ffmpeg -i +"+video+" -vn -acodec copy output-audio.aac";
        //fileName = fileName + filePath.substring(i);
        //int i = path.indexOf(".");
        //fileName = fileName + path.substring(i);
        // String command = "-y -i " + video + " -an " + folder + "/" + "Hussain_abc.mp4";
        //String command = "-y -i " + video + " -b:a 192K -vn " + folder + "/" + "" + file_path;
        String command ="-i "+video+" -map 0:1 -acodec pcm_s16le -ac 2 "+folder + "/" + "" + file_path;
        Log.d("commamd:",command);
        String[] cmd = command.split(" ");
        try {
            fFmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                    //  Log.d("FFMpeg", "onStart: ");
                    progress.setMessage("Please wait...");
                    progress.show();
                }
                @Override
                public void onProgress(String message) {
//                    progress.setMessage(message);
                    //Log.d("FFMpeg", message);
                }
                @Override
                public void onFailure(String message) {
                    // Log.d("FFMpeg",message);
                    progress.dismiss();
                }
                @Override
                public void onSuccess(String message) {

                    Toast.makeText(CamreaView.this, "Audio Extracted Successfully!"+file_path, Toast.LENGTH_SHORT).show();
                    File afile= new File(folder+"/"+file_path);
                    populateChunks(jsonArray,afile);
                    progress.dismiss();
                }
                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Log.e("FFMpeg", "convertToAudio: " , e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }
    public void populateChunks(JSONArray jsonArray,File audiofile) {
        Log.d("CHUMKSTART","start");
        try {
            File folder = new File(Environment.getExternalStorageDirectory() + "/RouteApp");
            boolean startset=false;
            properties.audiodata = new HashMap<String, String>();
            String start= "",last="";
            int chunk = 1;
            for (int i = 0; i < jsonArray.length(); i++) {
                Log.d("CHUMKSTART","start");
                JSONObject c = jsonArray.getJSONObject(i);
                if(!startset)
                {
                    if(c.getString("AUDIO").equals("1"))
                    {
                        start = c.getString("TIME");
                        startset=true;
                    }

                }else
                {
                    if(c.getString("AUDIO").equals("0"))
                    {
                        last = c.getString("TIME");
                        properties.audiodata.put(""+chunk,start+"_"+last);
                        startset=false;
                        Log.d("ACHUMK:",chunk+"");
                        initializefFmpeg();
                        GenerateAudioChunks(audiofile,start,last,folder,chunk);
                        chunk=chunk+1;
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("CHUMKEXP:",e.getMessage());
        }
    }
    public void initializefFmpeg() {
        fFmpeg = FFmpeg.getInstance(this);
        try {
            fFmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.d("FFMPEG", "onFailure: ");
                }

                @Override
                public void onSuccess() {
                    Log.d("FFMPEG", "onSuccess: ");
                }

                @Override
                public void onStart() {
                    Log.d("FFMPEG", "onStart: ");
                }

                @Override
                public void onFinish() {
                    Log.d("FFMPEG", "onFinish: ");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }
    private void Start_Service() {
        ServiceIntent = new Intent(CamreaView.this, background_location_updates.class);
        ServiceIntent.putExtra("is_video_capturing", isVideoCapturing);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try
            {
                startForegroundService(ServiceIntent);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        } else {
            startService(ServiceIntent);
        }
        register_Reciever();
    }
    private void register_Reciever() {
        if (!receiversRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(background_location_updates.MY_ACTION);
            registerReceiver(myReceiver, intentFilter);
            receiversRegistered = true;
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        try {
            if (receiversRegistered) {
                unregisterReceiver(myReceiver);
                receiversRegistered = false;
            }
        } catch (Exception e) {

        }
    }
    public void CreatSubExcelSheet(String sheet_name) {
        String Fnamexls = sheet_name + ".xls";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/RouteApp");
            Log.d("Make", "Directory");
            directory.mkdirs();
            File file = new File(directory, Fnamexls);
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));
            WritableWorkbook workbook;
            try {
                int a = 1;
                workbook = Workbook.createWorkbook(file, wbSettings);
                //workbook.createSheet("Report", 0);
                WritableSheet sheet = workbook.createSheet(sheet_name, 0);
                Label cell_date = new Label(0, 0, "date");
                Label cell_time = new Label(1, 0, "time");
                Label cell_lattitude = new Label(2, 0, "latitude");
                Label cell_longitude = new Label(3, 0, "longitude");
                Label cell_media = new Label(4, 0, "media time");
                Label cell_pitch = new Label (5,0,"pitch");
                Label cell_direction = new Label (6,0,"direction");
                Label cell_roll = new Label (7,0,"roll");
//                Label cell_audio_time = new Label(5, 0, "Audio Time");
//                Label cell_audio_path = new Label(6, 0, "Audio Path");
//                Label cell_text = new Label(7, 0, "text");
                try {
                    sheet.addCell(cell_date);
                    sheet.addCell(cell_time);
                    sheet.addCell(cell_lattitude);
                    sheet.addCell(cell_longitude);
                    sheet.addCell(cell_media);
                    sheet.addCell(cell_pitch);
                    sheet.addCell(cell_direction);
                    sheet.addCell((cell_roll));
                    // these values are required in the file
//                    sheet.addCell(cell_audio_time);
//                    sheet.addCell(cell_audio_path);
//                    sheet.addCell(cell_text);
                } catch (RowsExceededException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (WriteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                workbook.write();
                try {
                    workbook.close();
                } catch (WriteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //createExcel(excelSheet);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }

    }

    /**
     * this fxn creates a csv file for a single video
     * @param Date date of the video recording
     * @param time time of the video recording
     * @param latitude latitude of the video recording
     * @param longitude longitude of video recording
     * @param mdeia  path
     * @param audio_time time at which this latitude was recovered
     * @param audio_path path of the audio
     * @param text text extracted
     * @param file_name name of the file
     * @param row row no
     */
    public void saveSubListSheet(String Date, String time, String latitude, String longitude,String pitch, String direction,String roll, String mdeia,
                                 String audio_time, String audio_path, String text,
                                 String file_name,int row ) {
        Toast.makeText(this, "adding data to excel file ", Toast.LENGTH_SHORT).show();
        String Fnamexls = file_name + ".xls";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/RouteApp");
        directory.mkdirs();
        File file = new File(directory, Fnamexls);
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        Workbook workbook;
        try {
            int a = 1;
            workbook = Workbook.getWorkbook(file, wbSettings);
            WritableWorkbook wb = Workbook.createWorkbook(file, workbook);
            //workbook.createSheet("Report", 0);
            WritableSheet sheet = wb.getSheet(file_name);
            Label cell_date = new Label(0, row, Date);
            Label cell_time = new Label(1, row, time);
            Label cell_latitude = new Label(2, row, latitude);
            Label cell_longitude = new Label(3, row, longitude);
            //getting formatted time
            String formattedMediaTime = convertNumberIntoTimeFormat((long) row);
            Label cell_mdeia = new Label(4, row, formattedMediaTime);
            Label cell_pitch = new Label (5,row,pitch);
            Label cell_direction = new Label (6,row,direction);
            Label cell_roll = new Label (7,row,roll);
//            Label cell_mdeia = new Label(4, row, mdeia);
//            Label cell_audio_time = new Label(5, row, audio_time);
//            Label cell_audio_path = new Label(6, row, audio_path);
//            Label cell_audio_text = new Label(7, row, text);
            try {
                sheet.addCell(cell_date);
                sheet.addCell(cell_time);
                sheet.addCell(cell_latitude);
                sheet.addCell(cell_longitude);
                sheet.addCell(cell_mdeia);
                sheet.addCell(cell_pitch);
                sheet.addCell(cell_direction);
                sheet.addCell(cell_roll);
//                sheet.addCell(cell_audio_time);
//                sheet.addCell(cell_audio_path);
//                sheet.addCell(cell_audio_text);s
            } catch (RowsExceededException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (WriteException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            wb.write();
            wb.close();
            Toast.makeText(this, "closing the workbook ", Toast.LENGTH_SHORT);
            try {
                workbook.close();
            } catch (Exception ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }
            //createExcel(excelSheet);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (BiffException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (WriteException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    public void recordingThreadStart()
    {

         thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        sleep(1000);
                        if(is_userRecordingAudio)
                        {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                            String currentTime = sdf.format(new Date());
                            Log.d("THREAD", "WORKING" + Latitude);
                            Log.d("THREAD", "WORKING" + Longitude);
                            Log.d("VIDEO","TIME :"+""
                                    + (hour > 9 ? hour : ("0" + hour)) + ":"
                                    + (minute > 9 ? minute : ("0" + minute))
                                    + ":"
                                    + (seconds > 9 ? seconds : "0" + seconds));
                            arrayList_video.add(Latitude+","+Longitude+","
                                    + (hour > 9 ? hour : ("0" + hour)) + ":"
                                    + (minute > 9 ? minute : ("0" + minute))
                                    + ":"
                                    + (seconds > 9 ? seconds : "0" + seconds)+","+currentTime);
                        }

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }
    public void StartRecodingTime()
    {
         t = new Timer("hello", true);
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                recorder_timer.post(new Runnable() {
                    public void run() {
                        seconds++;
                        if (seconds == 60) {
                            seconds = 0;
                            minute++;
                        }
                        if (minute == 60) {
                            minute = 0;
                            hour++;
                        }
                        recorder_timer.setText(""
                                + (hour > 9 ? hour : ("0" + hour)) + " : "
                                + (minute > 9 ? minute : ("0" + minute))
                                + " : "
                                + (seconds > 9 ? seconds : "0" + seconds));
                        obj = new JSONObject();
                        String time=(hour > 9 ? hour : ("0" + hour)) + " : "
                                + (minute > 9 ? minute : ("0" + minute))
                                + " : "
                                + (seconds > 9 ? seconds : "0" + seconds);
                        try {
                            obj.put("LATITUDE", properties.loclat);
                            obj.put("LONGITUDE", properties.loclog);
                            obj.put("TIME", time);
                            if(is_userRecordingAudio)
                            {
                                obj.put("AUDIO", "1");
                            }else
                            {
                                obj.put("AUDIO", "0");
                            }

                            Log.d("ISTLOC:",properties.loclat+":"+properties.loclog);
                            //Log.d("ISTSIZE:",arrayList.size()+"");


                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        jsonArray.put(obj);
                    }
                });

            }
        }, 0, 1000);
    }

    /**
     * this fxn returns the current date from the system
     * @return
     */
    public String  getCurrentDate()
    {
        String today_date=null;
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
        String formattedDate = df.format(c);
        today_date=formattedDate;
        System.out.println("Date"+today_date);
        return today_date;
    }


    /**
     * this fxn takes a string number and retun it in time format
     * if the number is 62 it will return it like 00:01:02
     * @param num string number
     */
    public String convertNumberIntoTimeFormat(long num){
        // calculate no of hours from seconds
        // remove the days if there are any
        long hoursFromToday = num%86400;
        long hours = hoursFromToday/3600;
        long remaningHours = hoursFromToday%3600;
        long minutes = remaningHours/60;
        long seconds = remaningHours%60;
        String formattedHours = String.format("%02d", hours);
        String formattedSeconds = String.format("%02d", seconds);
        String formattedMinutes = String.format("%02d", minutes);
        return formattedHours+":"+formattedMinutes+":"+formattedSeconds;
    }


    /**
     * this fxn updates the csv file
     * @param coordinates coordintes to add in csv file
     * @param fileName filename
     */
    public void updateCsvFile(ArrayList<LatLng> coordinates, String fileName, ArrayList<String[]> angle){
        if(coordinates!=null && coordinates.size()>0){
            ArrayList<String> timeList = getTimeList(coordinates.size());
            System.out.println("sizearray: "+coordinates.size());
            System.out.println("sizeori: "+angle.size());
            System.out.println("time: "+timeList.size());

            for(int i=0; i<timeList.size(); i++){
                LatLng coordinate = coordinates.get(i);
                String[] oneangle = angle.get(i);
                String latitude = String.valueOf(coordinate.getLatitude());
                String longitude = String.valueOf(coordinate.getLongitude());
                String roll=oneangle[2];
                String pitch=oneangle[1];
                String direction=oneangle[0];
                //String time = getTimeFromVideoList(i);
                // getting the time from another array list_video
                // calling a prebuilt function to update csv file
                saveSubListSheet(getCurrentDate(),timeList.get(i),latitude, longitude,pitch,direction,roll,"",convertNumberIntoTimeFormat((long)i), "","",fileName,i+1);
            }
        }
    }

    /**
     * this fxn creates a list of time from the current time in millis
     * @param size size of the list needed
     * @return list of time as string
     */
    public ArrayList<String> getTimeList(int size){
        ArrayList<String> resultList = new ArrayList<>();
        long current_time = System.currentTimeMillis();
        //System.out.println("initial time"+current_time);
        // calculating offset time based on the local timezone
        int offset = TimeZone.getDefault().getOffset(current_time);
        //System.out.println("offsettime:"+offset);
        long time_in_secs = (int) ((current_time+offset)/1000);
        //System.out.println("timesec:"+time_in_secs);
        for(int i=0; i<size; i++){
            String formattedTime = convertNumberIntoTimeFormat(time_in_secs);
            //System.out.println("formatt:"+formattedTime);
            resultList.add(formattedTime);
            time_in_secs++;
        }
        return resultList;
    }

    class VideoCompressAsyncTask extends AsyncTask<String, String, String>{

        Context mContext;

        public VideoCompressAsyncTask(Context context) {mContext = context;}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... paths) {
            String filePath = null;
            try {
                System.out.println("p0"+paths[0]);
                System.out.println("p0"+paths[1]);
                filePath = SiliCompressor.with(mContext).compressVideo(paths[0], paths[1]);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return filePath;

        }


        @Override
        protected void onPostExecute(String compressedFilePath) {
            super.onPostExecute(compressedFilePath);
            File imageFile = new File(compressedFilePath);
            float length = imageFile.length() / 1024f; // Size in KB
            Log.i("Silicompressor", "Path: " + compressedFilePath);
        }
    }

//    /**
//     * this fxn is for testing it will have an array list
//     */
//    public void testCode(){
//        ArrayList<LatLng> points = new ArrayList<>();
//        points.add(new LatLng(80.12, 45.11, 34.11));
//        points.add(new LatLng(80.12, 45.11, 34.11));
//        points.add(new LatLng(80.12, 45.11, 34.11));
//        points.add(new LatLng(80.12, 45.11, 34.11));
//        points.add(new LatLng(80.12, 45.11, 34.11));
//        points.add(new LatLng(80.12, 45.11, 34.11));
//        updateCsvFile(points,"Sheet_One");
//    }


}
