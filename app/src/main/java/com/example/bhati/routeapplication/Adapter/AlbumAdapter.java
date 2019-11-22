package com.example.bhati.routeapplication.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.bhati.routeapplication.Activities.FileUtils;
import com.example.bhati.routeapplication.Activities.SavingActivity;
import com.example.bhati.routeapplication.R;
import com.example.bhati.routeapplication.Rest.Album;

import java.util.ArrayList;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyViewHolder> {
    private static Context mContext;
    private List<Album> albumList;

    public AlbumAdapter(Context context , List<Album> albums)
    {
        this.mContext = context;
        this.albumList = albums;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        Album album = albumList.get(i);

        //holder.videoView.setImageBitmap(retriveVideoFrameFromVideo(album.getVideo_url()));
        Log.d("VIDEO_URL","IS : "+album.getVideo_url());
        holder.txtDuration.setText(""+getVideoTime(album.getVideo_url()));
        holder.txtTime.setText(album.getTime());
        holder.txtDate.setText(album.getDate());
        holder.txtSize.setText(album.getSize());
        holder.txtName.setText(album.getName());


        String filter=album.getVideo_name().toString().substring(0,album.getVideo_name().toString().length()-4);
        System.out.println("Fileis:"+filter);
        ArrayList<String> chumkfiles = FileUtils.getFileNames(Environment.getExternalStorageDirectory() + "/RouteApp","chunk_"+filter,1);

        if(chumkfiles!=null)
        {
            for(int j=0;j<chumkfiles.size();j++)
            {
                System.out.println("chumkis:"+j+":"+chumkfiles.get(j));
            }
            holder.txtCity.setText(chumkfiles.size()+"");

        }else
        {
            holder.txtCity.setText("1");
        }

        //  holder.txtUserName.setText(mAuth.getCurrentUser().getEmail());
        holder.txtVideoName.setText(album.getVideo_name());
        Log.d("AUDIO_FILE","IS :"+album.getAudio_file());
        holder.ll.setOnClickListener(v -> {
           // holder.videoView.start();
            //Toast.makeText(mContext, ""+album.getId(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(mContext, SavingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("uri", album.getVideo_url());
            bundle.putString("listLatLng", album.getLatLngs());
            bundle.putString("listOthers", album.getRecorder_file());
            bundle.putString("AUDIOFILE", album.getAudio_file());
            //Log.d("onBindViewHolder", "onBindViewHolder: "+album.getLatLngs());
            intent.putExtra("bundle_values", bundle);
            mContext.startActivity(intent);
            //finish();
        });
        try
        {
            Glide.with(mContext)
                    .load(Uri.parse(album.getVideo_url()))
                    .into(holder.videoView);


        }
        catch (Exception ex)
        {
            Log.d("GLIDE_EXCEPTION","EXception"+ex.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public ImageView videoView;
        public LinearLayout ll;
        public TextView txtDuration  , txtVideoName;
        public TextView txtTime;
        public TextView txtDate;
        public TextView txtSize;
        public TextView txtCity;
        public TextView txtName;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoView);
            txtDuration = itemView.findViewById(R.id.txtDuration);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtSize = itemView.findViewById(R.id.txtSize);

            txtCity = itemView.findViewById(R.id.txtCityName);

            txtVideoName = itemView.findViewById(R.id.txtVideoName);
            txtName = itemView.findViewById(R.id.txtUsername);
            ll = itemView.findViewById(R.id.ll);
        }
    }
    @SuppressLint("DefaultLocale")
    private String getVideoTime(String videoUri)
    {
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(mContext, Uri.parse(videoUri));
//        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//        long timeInMillisec = Long.parseLong(time );
//        Log.d("Adapter", "getVideoTime: "+timeInMillisec);
//        retriever.release();
//       // SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
//       // mTimeText.setText("Time: " + dateFormat.format(timeInMillisec));
//        //timeInMillisec = 5000;
//        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeInMillisec),
//                TimeUnit.MILLISECONDS.toMinutes(timeInMillisec) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeInMillisec)),
//                TimeUnit.MILLISECONDS.toSeconds(timeInMillisec) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillisec)));

        return "some value";
    }

    static Bitmap bitmap;
    public static Bitmap retriveVideoFrameFromVideo(String videoPath)
    {

        Bitmap bMap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MICRO_KIND);
        //        String[] filePathColumn = {MediaStore.Images.Media.DATA};
//        Cursor cursor = mContext.getContentResolver().query(Uri.parse(videoPath), filePathColumn, null, null, null);
//        cursor.moveToFirst();
//        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
////        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//        String picturePath = cursor.getString(column_index);
//        cursor.close();
//
//        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(picturePath, MediaStore.Video.Thumbnails.MICRO_KIND);
        return bMap;
    }

}
