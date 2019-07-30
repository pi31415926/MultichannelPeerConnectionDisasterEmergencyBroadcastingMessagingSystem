package com.challenge.multichannelpeerconnectiondisasteremergencybroadcastingmessagingsystem;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;


public class SendingService extends AsyncTask<String, Integer, Boolean> {


    private FileInformation fileInformation;

    private static final int PORT = 4786;


    public SendingService(Context context, FileInformation fileInformation) {
        this.fileInformation = fileInformation;
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected Boolean doInBackground(String... strings) {
        fileInformation.setMd5Code(Md5Util.getMd5Code(new File(fileInformation.getFileName())));
        Socket socket = null;
        OutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        InputStream inputStream = null;
        try {
            socket = new Socket();
            socket.bind(null);
            socket.connect((new InetSocketAddress(strings[0], PORT)), 10000);
            outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(fileInformation);
            inputStream = new FileInputStream(new File(fileInformation.getFileName()));
            long fileSize = fileInformation.getFileSize();
            long totalLength = 0;
            byte buf[] = new byte[512];
            int lineLength;
            while ((lineLength = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, lineLength);
                totalLength += lineLength;
                int progress = (int) ((totalLength * 100) / fileSize);
                publishProgress(progress);
            }
            outputStream.close();
            objectOutputStream.close();
            inputStream.close();
            socket.close();
            outputStream = null;
            objectOutputStream = null;
            inputStream = null;
            socket = null;
            Log.e("info", "fileComplete");
            return true;
        } catch (Exception e) {
            Log.e("warn", "fileIncomplete" + e.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Integer... values) { }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Log.e("info", "isPostExecute " + aBoolean);
    }

}
