package com.example.misakabox;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Button btnWindow;
    private ToastUtil toastUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnWindow=findViewById(R.id.btn_window);
        toastUtil=new ToastUtil(this);
        btnWindow.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (FloatingWindowService.isStarted) {
                    return;
                }
                FloatingWindowService.isStarted=true;
                if (!Settings.canDrawOverlays(MainActivity.this)) {
                    //Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT);
                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
                } else {
                    startService(new Intent(MainActivity.this, FloatingWindowService.class));
                }
            }
        });
        //initView();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(!Settings.canDrawOverlays(this))
        {
            toastUtil.showText("授权失败");
        }
        else startService(new Intent(MainActivity.this, FloatingWindowService.class));
    }


}