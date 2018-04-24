package com.seetaface2as;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener  {
    private static final String TAG = "MainActivity";

    private LinearLayout detectLayout,compareLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        Log.i("MainActivity","onCreate");
    }

    //初始化界面部分
    private void initView() {
        detectLayout = (LinearLayout) findViewById(R.id.detect_layout);
        detectLayout.setOnClickListener(this);

        compareLayout = (LinearLayout) findViewById(R.id.compare_layout);
        compareLayout.setOnClickListener(this);
    }

    //选择按钮功能
    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.detect_layout:
                intent = new Intent(MainActivity.this, DetectionActivity.class);
                break;
            case R.id.compare_layout:
                intent = new Intent(MainActivity.this, CompareActivity.class);
                break;
            default:
                break;
        }
        startActivity(intent);
        Log.i("MainActivity","onClicked");
    }
}
