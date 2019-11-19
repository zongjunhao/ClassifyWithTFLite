package com.zjh.classifywithtflite;

import android.os.Environment;

import java.io.File;

public class FileUtil {
    public static File getPhotoCacheFolder() {
        File cacheFolder = new File(Environment.getExternalStorageDirectory(), "TensorFlowPhotos");
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
        return cacheFolder;
    }
}
