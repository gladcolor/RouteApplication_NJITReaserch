package com.example.bhati.routeapplication.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import com.example.bhati.routeapplication.R;
import static com.example.bhati.routeapplication.R.id.ten;

public class menu extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private PopupMenu pm;
    private Menu menu;
    int savetime=4;
    int video=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);
        Button btn = (Button) findViewById(R.id.time_btn);
        Button vbtn = (Button) findViewById(R.id.voice);
        Button apply=(Button) findViewById(R.id.back);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(menu.this, v);
                popup.setOnMenuItemClickListener(menu.this);
                popup.inflate(R.menu.popupmenu_activity);
                popup.show();
            }
        });
        vbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(menu.this, v);
                popup.setOnMenuItemClickListener(menu.this);
                popup.inflate(R.menu.audio_popup);
                popup.show();
            }
        });
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                System.out.println("savetime"+savetime);
                System.out.println("ttttttt"+video);
                intent.putExtra("len",savetime);
                intent.putExtra("audio",video);
                setResult(2,intent);
                finish();
            }
        });
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.five:
                savetime=5;
                // do your code
                return true;
            case R.id.ten:
                savetime=10;
                System.out.println(10);
                return true;
            case R.id.yes:
                video=1;
                return true;
            case R.id.no:
                video=0;
                return true;
            default:
                return false;
        }
    }

}