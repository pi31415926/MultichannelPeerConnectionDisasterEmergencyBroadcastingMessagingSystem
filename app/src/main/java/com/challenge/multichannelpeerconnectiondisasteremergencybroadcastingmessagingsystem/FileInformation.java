package com.challenge.multichannelpeerconnectiondisasteremergencybroadcastingmessagingsystem;


import java.io.Serializable;

public class FileInformation implements Serializable {

    private String fileName;

    private long fileSize;

    private String md5Code;

    public FileInformation(String name, long fileSize) {
        this.fileName = name;
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMd5Code() {
        return md5Code;
    }

    public void setMd5Code(String md5) {
        this.md5Code = md5;
    }

    @Override
    public String toString() {
        return fileName + ' ' + fileSize +
                " '" + md5Code;
    }

}