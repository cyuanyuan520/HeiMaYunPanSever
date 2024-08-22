package com.itheima.service;

import com.itheima.exception.BusinessException;
import com.itheima.util.AgreementUtil;
import com.itheima.util.IOUtil;

import java.io.*;
import java.net.Socket;
import java.util.ResourceBundle;

/*
    协议定义:   协议+数据
    第一行是协议，第二行开始就是数据
 */
public class FileUpDownServiceImp implements Runnable, FileUpDownService {

    private final ResourceBundle bundle;
    private final File rootDir;
    private Socket socket;

    public FileUpDownServiceImp(Socket socket) {
        this.socket = socket;
        //1 读取配置文件中的端口，根目录等配置信息
        bundle = ResourceBundle.getBundle("yunpan");
        //根目录  rootDir = D:\\img
        rootDir = new File(bundle.getString("rootDir"));
        if (rootDir.isFile()) {
            throw new BusinessException("根目录路径与已存在文件冲突");
        } else if (!rootDir.exists() && !rootDir.mkdirs()) {
            throw new BusinessException("根目录创建失败，请检查配置路径是否正确");
        }
    }

    @Override
    public void run() {
        try (Socket socket = this.socket;
             InputStream netIn = socket.getInputStream();
             OutputStream netOut = socket.getOutputStream();
        ) {
            // 读协议
            final String agreement = AgreementUtil.receiveAgreement(netIn);
            // System.out.println("接收客户端数据：" + agreement);

            // 解析字符串
            String type = AgreementUtil.getType(agreement);
            // System.out.println("解析字符串的数据类型:" + type);
            switch (type) {
                case "SCAN"://客户端要浏览
                    scanDirectory(agreement, netIn, netOut);
                    break;
                case "DOWNLOAD"://客户端要下载
                    System.out.println("downloadpath");
                    downloadFile(agreement, netIn, netOut);
                    break;
                case "UPLOAD"://客户端要上传
                    uploadFile(agreement, netIn, netOut);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 浏览目录
    @Override
    public void scanDirectory(String agreement, InputStream netIn, OutputStream netOut) throws IOException {
        //响应客户端使用
        //获取客户端想要浏览的目录
        String fileName = AgreementUtil.getFileName(agreement);// root
        System.out.println(fileName + "1");
        //root是提供给客户端的虚拟路径，转换为服务端的真实路径
        String fileDir = fileName.replace("root", rootDir.toString());
        File dir = new File(fileDir);
        System.out.println(dir.getAbsolutePath() + "2");

        if (dir.isFile()) {
            System.out.println("我出错了!!");//调试代码
            // 封装协议
            String s = AgreementUtil.getAgreement("SCAN", null, "FAILED", "目录不存在.只能浏览当前子目录");
            // 发送协议
            AgreementUtil.sendAgreement(netOut, s);
        } else {
            System.out.println("我得到了正确的数据!");
            // 封装协议
            String s = AgreementUtil.getAgreement("SCAN", dir.getAbsolutePath(), "OK", null);
            AgreementUtil.sendAgreement(netOut, s);

            //把具体数据随后发送
            //把文件数据按照："文件类型 名称"   发送，每一个子文件一行
            OutputStreamWriter osw = new OutputStreamWriter(netOut);
            File[] children = dir.listFiles();

            for (File child : children) {
                String fileType = child.isFile() ? "文件" : "目录";
                osw.write(fileType + " " + child.getName() + "\r\n");//每个文件一行
            }
            //刷新数据
            osw.flush();
        }
    }

    // 文件上传功能
    @Override
    public void uploadFile(String agreement, InputStream netIn, OutputStream netOut) throws IOException {
        //获得用户想上传的文件名
        System.out.println("[调试]" + "上传即将启动...");
        String fileName = AgreementUtil.getFileName(agreement);
        System.out.println("[调试]" + fileName);
        File[] childs = rootDir.listFiles();
        for (File child : childs) {
            if (child.getName().equals(fileName)){
                //如果已有重名文件: 发送失败协议
                String s1 = AgreementUtil.getAgreement("UPLOAD", null, "FAILED", "已有重名文件, 请修改文件名后上传!");
                AgreementUtil.sendAgreement(netOut, s1);
                return;
            }
        }
        //循环检测完没发现重名的文件:发送成功协议 告诉客户端可以继续上传文件了!
        String s2 = AgreementUtil.getAgreement("UPLOAD", null, "OK", null);
        AgreementUtil.sendAgreement(netOut, s2);
        BufferedInputStream bis = new BufferedInputStream(netIn);//这里有流!
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(rootDir + "/" + fileName));//这里有流!
        int b;
        while ((b = bis.read()) != -1) {
            bos.write(b);
        }
        bos.flush();
        bos.close();
        bis.close();
        //文件上传成功!
        System.out.println("[通知]" + "文件上传成功");

    }

    // 文件下载功能
    @Override
    public void downloadFile(String agreement, InputStream netIn, OutputStream netOut) throws IOException {

        //获取用户想下载的路径
        String filename =AgreementUtil.getFileName(agreement);
        //将root文件夹替换成真实文件夹
        String fileDir = filename.replace("root", rootDir.toString());
        System.out.println(fileDir);//调试代码 记得删除
        File file = new File(fileDir);//用户实际上要下载的东西
        if (!file.isFile()){
            System.out.println("这不是文件!");
            //如果文件是目录 发送报错信息
            String a1 = AgreementUtil.getAgreement("DOWNLOAD", null, "FAILED", "请选择非目录文件下载!");
            AgreementUtil.sendAgreement(netOut, a1);
        } else {
            String a2 = AgreementUtil.getAgreement("DOWNLOAD", null, "OK", null);
            AgreementUtil.sendAgreement(netOut, a2);
            //发送数据(可能用字节缓冲流比较好)
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            BufferedOutputStream bos = new BufferedOutputStream(netOut);
            int b;
            while ((b = bis.read()) != -1) {
                bos.write(b);
            }
            bos.flush();
            System.out.println("数据发送出去了");
        }

    }
}