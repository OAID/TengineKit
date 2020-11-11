package com.faceDemo.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class FileHandler {
    public static void copyAllAssets(Context context, String destination) {
        copyAssetsToDst(context, "", destination);
    }

    /**
     * @param context :application context
     * @param srcPath :the path of source file
     * @param dstPath :the path of destination
     */
    private static void copyAssetsToDst(Context context, String srcPath, String dstPath) {
        try {
            String fileNames[] = context.getAssets().list(srcPath);
            if (fileNames.length > 0) {
                File file = new File(dstPath);
                file.mkdirs();
                for (String fileName : fileNames) {
                    if (srcPath != "") {
                        copyAssetsToDst(context, srcPath + "/" + fileName, dstPath + "/" + fileName);
                    } else {
                        copyAssetsToDst(context, fileName, dstPath + "/" + fileName);
                    }
                }
            } else {
                InputStream is = context.getAssets().open(srcPath);
                FileOutputStream fos = new FileOutputStream(new File(dstPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void writeData(String filePath, String fileName, String content) {
        writeTxtToFile(content, filePath, fileName);
    }

    //写文件
    private static void writeTxtToFile(String content, String filePath, String fileName) {
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        String strContent = content + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
        }
    }

    //创建文件
    private static File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    //创建文件夹
    private static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
        }
    }
}
