package com.zjh.classifywithtflite;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.zjh.classifywithtflite.tflite.Classifier;
import com.zjh.classifywithtflite.tflite.Classifier.Recognition;

import java.io.IOException;
import java.util.List;

public class ClassifierActivity extends AppCompatActivity {

    private static final String TAG = "ClassifierActivity";

    private static Uri imageUri;
    private ImageView resultImage;
    private TextView classifierResult;
    private TextView urlToBuy;
    private Classifier classifier;
    private static List<Recognition> resultList;
    private String firstResult;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classfier);

        imageUri = getIntent().getParcelableExtra("imageUri");
        resultImage = findViewById(R.id.result_image);
        classifierResult = findViewById(R.id.classifier_result);
        urlToBuy = findViewById(R.id.url_to_buy);

        try {
            classifier = Classifier.create(this);
            handleInputPhoto(imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        firstResult = resultList.get(0).getTitle();
        classifierResult.setText(firstResult);
        url = "https://s.taobao.com/search?q="+firstResult;
        urlToBuy.setText(url);
//        urlToBuy.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setData(Uri.parse(url));
//                startActivity(intent);
//            }
//        });
    }

    /**
     * 处理图片
     *
     * @param imageUri 图像Uri地址
     */
    private void handleInputPhoto(Uri imageUri) throws IOException {
        //加载图片
        GlideApp.with(ClassifierActivity.this).asBitmap().listener(new RequestListener<Bitmap>() {

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                Log.d(TAG, "handleInputPhoto onLoadFailed");
                Toast.makeText(ClassifierActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                Log.d(TAG, "handleInputPhoto onResourceReady");
//                resultList = classifier.recognizeImage(resource, 90);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                });
                return false;
            }
        }).load(imageUri).into(resultImage);
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        resultList = classifier.recognizeImage(bitmap, 90);
    }
}
