package com.challenge.multichannelpeerconnectiondisasteremergencybroadcastingmessagingsystem;

import android.app.IntentService;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceivingService extends IntentService {

    public interface ProgressMonitor {

        void whenProgressIsChanging(FileInformation fileInformation, int progress);

        void whenTransferIsFinished(File file);

    }

    private ServerSocket serverSocket;

    private InputStream inputStream;

    private ObjectInputStream objectInputStream;

    private FileOutputStream fileOutputStream;

    private ProgressMonitor progressMonitor;

    private static final int PORT = 5010;

    public class ReceivingServiceBinder extends Binder {
        public ReceivingService getService() {
            return ReceivingService.this;
        }
    }

    public ReceivingService() {
        super("ReceivingService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ReceivingServiceBinder();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        clean();
        File file = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(PORT));
            Socket receiverSocket = serverSocket.accept();
            inputStream = receiverSocket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);
            FileInformation fileInformation = (FileInformation) objectInputStream.readObject();
            Log.e("info", "fileName " + fileInformation);

            ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
            File directory = contextWrapper.getDir(getFilesDir().getName(), Context.MODE_PRIVATE);
            file = new File(directory, fileInformation.getFileName());
            fileOutputStream = new FileOutputStream(file);
            byte buf[] = new byte[512];
            int lineLength;
            long totalLength = 0;
            int progress;
            while ((lineLength = inputStream.read(buf)) != -1) {
                fileOutputStream.write(buf, 0, lineLength);
                totalLength += lineLength;
                progress = (int) ((totalLength * 100) / fileInformation.getFileSize());
                if (progressMonitor != null) {
                    progressMonitor.whenProgressIsChanging(fileInformation, progress);
                }
            }
            serverSocket.close();
            inputStream.close();
            objectInputStream.close();
            fileOutputStream.close();
            serverSocket = null;
            inputStream = null;
            objectInputStream = null;
            fileOutputStream = null;
            Log.e("info", "fileComplete " + Md5Util.getMd5Code(file));
        } catch (Exception e) {
            Log.e("warn", "fileIncomplete " + e.getMessage());
        } finally {
            clean();
            if (progressMonitor != null) {
                progressMonitor.whenTransferIsFinished(file);
            }
            startService(new Intent(this, ReceivingService.class));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clean();
    }

    public void setProgressMonitor(ProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
    }

    private void clean() {
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
                fileOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (objectInputStream != null) {
            try {
                objectInputStream.close();
                objectInputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (inputStream != null) {
            try {
                inputStream.close();
                inputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
