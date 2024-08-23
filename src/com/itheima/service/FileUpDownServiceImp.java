package com.itheima.service;

import com.itheima.exception.BusinessException;
import com.itheima.util.AgreementUtil;
import com.itheima.util.IOUtil;

import java.io.*;
import java.net.Socket;
import java.util.ResourceBundle;

public class FileUpDownServiceImp implements Runnable, FileUpDownService{
    private Socket socket;
    private File rootDir;

    public FileUpDownServiceImp(Socket socket) {
        this.socket = socket;//更新端口信息
        //读取下载路径
        ResourceBundle bundle = ResourceBundle.getBundle("yunpan");
        rootDir = new File(bundle.getString("rootDir"));
        if (rootDir.isFile()){
            throw new BusinessException("[Error]:根目录与已存在的文件同名");
        } else if (!rootDir.exists() && !rootDir.mkdirs()) {
            throw new BusinessException("[Error]:下载路径初始化失败");
        }
    }

    @Override
    public void scanDirectory(String agreement, InputStream netIn, OutputStream netOut) throws IOException {
        String filePath = AgreementUtil.getFilename(agreement);//这是用户想访问的路径 此时路径名还是root!所以可以用这个东西还给客户端
        String trueFilePath = filePath.replace("root", rootDir.toString());//这是真正的路径
        File file = new File(trueFilePath);//用户想要访问的路径被封装成File类了!!!!!
        if (file.isFile()){
            String s = AgreementUtil.getAgreement("SCAN", null, "FAILED", "暂不支持在线打开此类文件");
            AgreementUtil.sendAgreement(netOut, s);
        } else {
            //避免暴露服务器存储文件的文件夹
            //封装协议
            String s = AgreementUtil.getAgreement("SCAN", filePath, "OK", null);
            AgreementUtil.sendAgreement(netOut, s);
            OutputStreamWriter osw = new OutputStreamWriter(netOut);
            File[] files = file.listFiles();
            for (File child : files) {
                String childType = child.isFile() ? "文件" : "目录";
                osw.write(childType + "   " + child.getName() + "\r\n");
            }
            osw.flush();
        }

    }

    @Override
    public void downloadFile(String agreement, InputStream netIn, OutputStream netOut) throws IOException {
        //将客户端传过来的名字转换成在服务器中的实际地址
        String filePath = AgreementUtil.getFilename(agreement);//这是传过来的假名(字符串类型)
        File trueFilePath = new File(filePath.replace("root", rootDir.toString()));//在服务器中的真实地址(File类型对象)
        if (trueFilePath.isDirectory() || !trueFilePath.exists()) {
            String s = AgreementUtil.getAgreement("DOWNLOAD", null, "FAILED", "你访问的位置不存在或是个文件夹!");
            AgreementUtil.sendAgreement(netOut, s);
        } else {
            String s = AgreementUtil.getAgreement("DOWNLOAD", filePath, "OK", "下载即将开始!");
            AgreementUtil.sendAgreement(netOut, s);
            InputStream in = new FileInputStream(trueFilePath.toString());
            IOUtil.copy(in, netOut);
            in.close();
        }
    }

    @Override
    public void uploadFile(String agreement, InputStream netIn, OutputStream netOut) throws IOException {
        String filePath = AgreementUtil.getFilename(agreement);
        //filePath是客户端传给服务器的假路径 要先转换成真路径
        File trueFilePath = new File(filePath.replace("root", rootDir.toString()));//trueFilePath是服务器文件的真实路径
        if (trueFilePath.exists()) {//当文件已存在时向客户端返回上传失败
            String s = AgreementUtil.getAgreement("UPLOAD", null, "FAILED", "你选择的路径已存在同名文件! 请重命名后上传");
            AgreementUtil.sendAgreement(netOut, s);
        } else {
            //不存在同名文件的时候返回正常上传协议 随后接收文件
            String s = AgreementUtil.getAgreement("UPLOAD", null, "OK", null);
            AgreementUtil.sendAgreement(netOut, s);
            //等待客户端下一步操作
            BufferedInputStream bis = new BufferedInputStream(netIn);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(trueFilePath));
            int b;
            while ((b = bis.read()) != -1) {
                bos.write(b);
            }
            bos.flush();
            bos.close();
            bis.close();
        }
    }

    @Override
    public void run() {
        try (
             Socket socket = this.socket;
             InputStream netin = socket.getInputStream();
             OutputStream netOut = socket.getOutputStream();
        ) {
            //读协议
            String agreement = AgreementUtil.receiveAgreement(netin);
            //解析协议
            String type = AgreementUtil.getType(agreement);
            switch (type) {
                case "SCAN"://客户端要浏览
                    scanDirectory(agreement, netin, netOut);
                    break;
                case "DOWNLOAD"://客户端要下载
                    downloadFile(agreement, netin, netOut);
                    break;
                case "UPLOAD"://客户端要上传
                    uploadFile(agreement, netin, netOut);
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
