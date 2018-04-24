package com.seetaface2as;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.seeta.sdk.FaceDetector2;
import com.seeta.sdk.PointDetector2;
import com.seeta.sdk.SeetaImageData;
import com.seeta.sdk.SeetaPointF;
import com.seeta.sdk.SeetaRect;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

/**
 * Created by User on 2018/4/24.
 */

public class DetectionActivity extends Activity {
    private static final String TAG = "DetectionActivity";

    //seetaface2
    private FaceDetector2 faceDetector;
    private PointDetector2 pointDetector;

    private static final int SELECT_IMAGE = 1;

    private TextView infoResult;
    private ImageView imageView;
    private Bitmap yourSelectedImage = null;

    private int testTimeCount = 10;

    AppCompatEditText etTestTimeCount;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        infoResult = (TextView) findViewById(R.id.infoResult);
        imageView = (ImageView) findViewById(R.id.imageView);

        etTestTimeCount = (AppCompatEditText) findViewById(R.id.etTestTimeCount);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);
        verifyStoragePermissions(this);

        initView();


        //模型初始化
        File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        String faceDetectorModel = sdDir.toString() + "/bindata/SeetaFaceDetector2.0.ats";
        faceDetector = new FaceDetector2(faceDetectorModel);

        String pointDetectorModel = sdDir.toString() + "/bindata/SeetaPointDetector2.0.pts5.ats";
        pointDetector = new PointDetector2(pointDetectorModel);

        Button buttonImage = (Button) findViewById(R.id.buttonImage);
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_IMAGE);
            }
        });

        Button buttonDetect = (Button) findViewById(R.id.buttonDetect);
        buttonDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (yourSelectedImage == null)
                    return;

                testTimeCount = Integer.valueOf(TextUtils.isEmpty(etTestTimeCount.getText().toString()) ? "1" : etTestTimeCount.getText().toString());

                int width = yourSelectedImage.getWidth();
                int height = yourSelectedImage.getHeight();
                SeetaImageData imageData = new SeetaImageData(yourSelectedImage.getWidth(), yourSelectedImage.getHeight(), 3);
                imageData.data = getPixelsBGR(yourSelectedImage);

                long timeDetectFace = System.currentTimeMillis();
                SeetaRect[] faceRects=null;

                for (int i = 0; i < testTimeCount; i++) {
                    faceRects = faceDetector.Detect(imageData);//make sure SeetaImageData.data in BGR format
                }

                timeDetectFace = System.currentTimeMillis() - timeDetectFace;
                Log.i(TAG, "人脸平均检测时间：" + timeDetectFace/testTimeCount);

                if (faceRects.length > 0) {
                    Bitmap drawBitmap = yourSelectedImage.copy(Bitmap.Config.ARGB_8888, true);
                    infoResult.setText("图宽："+width+"高："+height+"人脸平均检测时间："+timeDetectFace/testTimeCount+" 数目：" + faceRects.length);
                    Log.i(TAG, "图宽："+width+"高："+height+" 人脸数目：" + faceRects.length );

                    for (int i = 0; i < faceRects.length; i++) {
                        long timePointDetect = System.currentTimeMillis();
                        SeetaPointF[] landmarks = pointDetector.Detect(imageData, faceRects[i]);//if seetaRects not empty，seetaRects[0] is maximum face
                        timePointDetect = System.currentTimeMillis() - timePointDetect;
                        Log.i(TAG, "人脸特征点检测时间：" + timePointDetect);

                        Canvas canvas = new Canvas(drawBitmap);
                        Paint paint = new Paint();
                        int left, top, right, bottom;
                        left = faceRects[i].x;
                        top = faceRects[i].y;
                        right = faceRects[i].x + faceRects[i].width;
                        bottom = faceRects[i].y + faceRects[i].height;
                        paint.setColor(Color.RED);
                        paint.setStyle(Paint.Style.STROKE);//不填充
                        paint.setStrokeWidth(5);  //线的宽度
                        canvas.drawRect(left, top, right, bottom, paint);
                        //画特征点
                        canvas.drawPoints(new float[]{(float)landmarks[0].x, (float)landmarks[0].y,
                                (float)landmarks[1].x, (float)landmarks[1].y,
                                (float)landmarks[2].x, (float)landmarks[2].y,
                                (float)landmarks[3].x, (float)landmarks[3].y,
                                (float)landmarks[4].x, (float)landmarks[4].y}, paint);//画多个点

                    }
                    imageView.setImageBitmap(drawBitmap);
                } else {
                    infoResult.setText("未检测到人脸");
                }
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            try {
                if (requestCode == SELECT_IMAGE) {
                    Bitmap bitmap = decodeUri(selectedImage);

                    Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                    // resize to 227x227
                    //yourSelectedImage = Bitmap.createScaledBitmap(rgba, 227, 227, false);
                    yourSelectedImage = rgba;

                    imageView.setImageBitmap(yourSelectedImage);
                }
            } catch (FileNotFoundException e) {
                Log.e("MainActivity", "FileNotFoundException");
                return;
            }
        }
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 400;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }

    //提取像素点
    private byte[] getPixelsRGBA(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the

        return temp;
    }


    public SeetaImageData ConvertToSeetaImageData(String imgPath) {
        Bitmap bmp = BitmapFactory.decodeFile(imgPath);

        Bitmap bmp_src = bmp.copy(Bitmap.Config.ARGB_8888, true); // true is RGBA
        SeetaImageData imageData = new SeetaImageData(bmp_src.getWidth(), bmp_src.getHeight(), 3);
        imageData.data = getPixelsBGR(bmp_src);

        return imageData;
    }

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

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Detection Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
