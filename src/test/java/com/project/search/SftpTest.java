package com.project.search;


import com.project.search.common.utils.SftpUtil;
import org.junit.jupiter.api.Test;

public class SftpTest {
    @Test
    public void sftpTest(){
        try {
            SftpUtil sftpUtil = new SftpUtil();
            sftpUtil.connectServer("192.168.11.137",22,"mysftp","root");
            sftpUtil.uploadFile("/upload/pic/2b.jpg","D:\\我的文档\\My Pictures\\picture\\2b.jpg");
            sftpUtil.close();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
}
