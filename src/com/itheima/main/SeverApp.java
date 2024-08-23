package com.itheima.main;

import com.itheima.service.YunPanServiceImp;

public class SeverApp {
    public static void main(String[] args) {
        YunPanServiceImp yunPanSever = new YunPanServiceImp();
        yunPanSever.start();
    }
}
