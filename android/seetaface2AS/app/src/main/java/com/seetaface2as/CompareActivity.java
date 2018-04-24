package com.seetaface2as;

/**
 * Created by User on 2018/4/23.
 */


import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.seeta.sdk.FaceDetector2;
import com.seeta.sdk.FaceRecognizer2;
import com.seeta.sdk.PointDetector2;
import com.seeta.sdk.SeetaImageData;
import com.seeta.sdk.SeetaPointF;
import com.seeta.sdk.SeetaRect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * 方法名: 人脸静态比对（1：1）</br>
 * 详述: 包括本地图像和摄像头拍照两个选取方式，选择左右两边图像，点击比对即可得到相似度</br>
 * 开发人员：moli232777144</br>
 * 创建时间：2017年03月29日</br>
 *
 * @param
 */

public class CompareActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CompareActivity";

    private Button leftLocalBtn, leftTakeBtn, rightLocalBtn, rightTakeBtn, compareBtn;
    private ImageView leftImageView, rightImageView;
    private TextView resultView;
    private ProgressBar progressBar;
    private ActionBar actionBar;

    private int photoDirection;
    private static final int PHOTO_LEFT = 1;// 左侧
    private static final int PHOTO_RIGHT = 2;// 右侧

    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果

    private Handler mHandler;
    private static final int COMPARE = 1;//判断比对数目

    private File tempFile = new File(Environment.getExternalStorageDirectory(),
            getPhotoFileName());

    private Bitmap rgba;
    private Bitmap leftPhoto;
    private Bitmap rightPhoto;
    private Uri photoURI;


    //seetaface2
    private FaceDetector2 faceDetector;
    private PointDetector2 pointDetector;
    private FaceRecognizer2 faceRecognizer;

    public byte[] getPixelsBGR(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();

        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer

        byte[] temp = buffer.array(); // Get the underlying array containing the data.

        byte[] pixels = new byte[(temp.length / 4) * 3]; // Allocate for BGR

        // Copy pixels into place
        for (int i = 0; i < temp.length / 4; i++) {

            pixels[i * 3] = temp[i * 4 + 2];        //B
            pixels[i * 3 + 1] = temp[i * 4 + 1];    //G
            pixels[i * 3 + 2] = temp[i * 4];       //R

        }

        return pixels;
    }

    //界面创建
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_compare);
        initView();

        //模型初始化
        File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        String faceDetectorModel = sdDir.toString() + "/bindata/SeetaFaceDetector2.0.ats";
        faceDetector = new FaceDetector2(faceDetectorModel);

        String pointDetectorModel = sdDir.toString() + "/bindata/SeetaPointDetector2.0.pts5.ats";
        pointDetector = new PointDetector2(pointDetectorModel);

        String faceRecognizerModel = sdDir.toString() + "/bindata/SeetaFaceRecognizer2.0.ats";
        faceRecognizer = new FaceRecognizer2(faceRecognizerModel);


        // 比对，更新UI
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case COMPARE:
                        Log.i("CompareActivity", "face score= start");
                        //TODO 人脸比对
                        if (leftPhoto!=null || rightPhoto !=null) {

                            SeetaImageData leftImageData = new SeetaImageData(leftPhoto.getWidth(), leftPhoto.getHeight(), 3);
                            leftImageData.data = getPixelsBGR(leftPhoto);

                            SeetaImageData rightImageData = new SeetaImageData(leftPhoto.getWidth(), leftPhoto.getHeight(), 3);
                            rightImageData.data = getPixelsBGR(leftPhoto);

                            SeetaRect[] leftFaceRects = faceDetector.Detect(leftImageData);
                            SeetaRect[] rightFaceRects = faceDetector.Detect(rightImageData);
                            if(leftFaceRects==null||rightFaceRects==null){
                                resultView.setText("未检测到人脸");
                                return;
                            }

                            SeetaPointF[] leftLandmarks = pointDetector.Detect(leftImageData, leftFaceRects[0]);
                            SeetaPointF[] rightLandmarks = pointDetector.Detect(rightImageData, rightFaceRects[0]);

                            long timeFaceRecognizer = System.currentTimeMillis();
                            float similarity = faceRecognizer.Compare(leftImageData, leftLandmarks, rightImageData, rightLandmarks);
                            timeFaceRecognizer = System.currentTimeMillis() - timeFaceRecognizer;
                            Log.i(TAG, "人脸1:1比对识别时间：" + timeFaceRecognizer);

                            resultView.setText("相似度:"+similarity);
                        }
                        else {
                            resultView.setText("检测图像为空，请输入人脸图像");
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    //CompareActivity界面初始化
    private void initView() {

        leftImageView = (ImageView) findViewById(R.id.left_img);
        rightImageView = (ImageView) findViewById(R.id.right_img);

        leftLocalBtn = (Button) findViewById(R.id.left_local);
        leftTakeBtn = (Button) findViewById(R.id.left_take);
        rightLocalBtn = (Button) findViewById(R.id.right_local);
        rightTakeBtn = (Button) findViewById(R.id.right_take);

        leftLocalBtn.setOnClickListener(this);
        leftTakeBtn.setOnClickListener(this);
        rightLocalBtn.setOnClickListener(this);
        rightTakeBtn.setOnClickListener(this);

        compareBtn = (Button) findViewById(R.id.compare);
        compareBtn.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progress);
        resultView = (TextView) findViewById(R.id.result);

        // 给左上角图标的左边加上一个返回的图标
        //actionBar = getActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        Log.i(TAG,"initView");

    }

    //按键监控操作
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.left_local:
                photoDirection = PHOTO_LEFT;
                // 本地图片
                Intent leftLocalIntent = new Intent(Intent.ACTION_PICK, null);
                leftLocalIntent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(leftLocalIntent, PHOTO_REQUEST_GALLERY);
                break;
            case R.id.left_take:
                photoDirection = PHOTO_LEFT;
                // 调用系统的拍照功能
                Intent leftTakeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 指定调用相机拍照后照片的储存路径
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    photoURI = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", tempFile);
                    // 指定调用相机拍照后照片的储存路径

                }
                else {
                    photoURI = Uri.fromFile(tempFile);
                }
                leftTakeIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                startActivityForResult(leftTakeIntent, PHOTO_REQUEST_TAKEPHOTO);
                break;
            case R.id.right_local:
                photoDirection = PHOTO_RIGHT;
                // 本地图片
                Intent rightLocalIntent = new Intent(Intent.ACTION_PICK, null);
                rightLocalIntent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(rightLocalIntent, PHOTO_REQUEST_GALLERY);
                break;
            case R.id.right_take:
                photoDirection = PHOTO_RIGHT;
                // 调用系统的拍照功能
                Intent rightTakeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 指定调用相机拍照后照片的储存路径
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    photoURI = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", tempFile);
                    // 指定调用相机拍照后照片的储存路径

                }
                else {
                    photoURI = Uri.fromFile(tempFile);
                }
                rightTakeIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                startActivityForResult(rightTakeIntent, PHOTO_REQUEST_TAKEPHOTO);
                break;
            case R.id.compare:
                resultView.setText("");
                progressBar.setVisibility(View.VISIBLE);

                // 创建线程发送COMPARE
                Message msg = new Message();
                msg.what = COMPARE;
                mHandler.sendMessage(msg);
                break;
            default:
                break;
        }
    }

    //返回按键操作结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PHOTO_REQUEST_TAKEPHOTO:
                startPhotoZoom(photoURI, 250);//裁剪大小150
                Log.i("CompareActivity","拍照采集功能实现");
                break;
            case PHOTO_REQUEST_GALLERY:
                if (data != null)
                    startPhotoZoom(data.getData(), 250);
                Log.i("CompareActivity","本地图像采集功能实现");
                break;
            case PHOTO_REQUEST_CUT:
                if (data != null)
                    setPicToView(data);
                Log.i("CompareActivity","采集结果功能实现");
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     *
     * 方法名: 裁剪返回的图像大小</br>
     * 详述: </br>
     * 开发人员：</br>
     * 创建时间：2017年03月29日</br>
     *
     * @param uri:图像数据
     * @param size:裁剪大小
     */
    private void startPhotoZoom(Uri uri, int size) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "false");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    /**
     *
     * 方法名: 将进行剪裁后的图片显示到UI界面上</br>
     * 详述: </br>
     * 开发人员：</br>
     * 创建时间：2017年03月29日</br>
     *
     * @param picdata:
     */
    @SuppressWarnings("deprecation")
    private void setPicToView(Intent picdata) {
        Bundle bundle = picdata.getExtras();
        if (bundle != null) {
            Bitmap photo = bundle.getParcelable("data");
            // 更新UI
            Drawable drawable = new BitmapDrawable(photo);
            switch (photoDirection) {
                case PHOTO_LEFT:
                    leftImageView.setBackgroundDrawable(drawable);
                    leftPhoto = photo;
                    break;
                case PHOTO_RIGHT:
                    rightImageView.setBackgroundDrawable(drawable);
                    rightPhoto = photo;
                    break;
            }
            // 保存Bitmap到本地
            saveBitmap(photo);
            Log.i("CompareActivity","saved picture to sdcard");
        }
    }

    /**
     *
     * 方法名: 将要比对的拍照或本地图像保存到SD特定目录</br>
     * 详述: </br>
     * 开发人员：</br>
     * 创建时间：2017年03月29日</br>
     *
     * @param bmp:bitmap图像
     */
    private void saveBitmap(Bitmap bmp) {
        //File file = new File("/mnt/sdcard/nexhome/images");
        File file = new File(Environment.getExternalStorageDirectory(),
                "nexhome/images");
        if (!file.exists()) {
            file.mkdirs();
            Log.i("CompareActivity","sdcard file new build");
        }
        switch (photoDirection) {
            case PHOTO_LEFT:
                file = new File(Environment.getExternalStorageDirectory(),
                        "nexhome/images/1.jpg");
                break;
            case PHOTO_RIGHT:
                //   file = new File("/mnt/sdcard/nexhome/images/2.jpg");
                file = new File(Environment.getExternalStorageDirectory(),
                        "nexhome/images/2.jpg");
                break;
        }

        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //拍照图像保存
    public String getPhotoFileName() {
        return "temp.jpg";
    }

    //左上角返回
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

}

