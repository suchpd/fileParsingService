package com.file.parsing.service;

import com.file.parsing.service.utils.FileUtil;
import com.file.parsing.service.utils.SmbFileUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class FileParsingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileParsingServiceApplication.class, args);

        try{

//            //获取文件详情
//            FileUtil.getLastFileInfo("C:\\Users\\13299\\Desktop\\87FD自动采集数据","2022106");
//
//            //遍历共享文件夹
//            SmbFileUtil.getAllSmbFiles("smb://192.168.205.128/MyShare/");
//
//            //下载共享文件夹指定文件
//            SmbFileUtil.smbDownload("smb://192.168.205.128/MyShare/Test.txt","C:\\Users\\13299\\Desktop");

            //下载共享文件夹下最新的文件
//            SmbFileUtil.smbDownloadLastFile("smb://192.168.205.128/MyShare/","C:\\Users\\13299\\Desktop");

            //读取本地文件
            byte[] file = FileUtil.getFile("C:\\Users\\13299\\Desktop\\Test.txt");
            System.out.println(Arrays.toString(file));
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
