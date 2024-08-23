package com.itheima.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileUpDownService {

    //浏览文件
    void scanDirectory(String agreement, InputStream netIn, OutputStream netOut) throws IOException;

    //下载文件
    void downloadFile(String agreement, InputStream netIn, OutputStream netOut) throws IOException;

    //上传文件
    void uploadFile(String agreement, InputStream netIn, OutputStream netOut) throws IOException;
}
