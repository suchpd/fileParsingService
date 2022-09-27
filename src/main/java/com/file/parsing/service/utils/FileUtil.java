package com.file.parsing.service.utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileUtil {

    /**
     * 获取文件夹下最新文件中的内容
     * @throws IOException  IO异常
     */
    public static void getLastFileInfo(String folderPath,String tableName) throws IOException {
        String dirPath = String.format(folderPath + "\\%s",tableName);

        File dir = new File(dirPath);
        if(!dir.isDirectory()){
            throw new RuntimeException(dirPath + "is not a folder");
        }

        File[] files = dir.listFiles();
        if(files == null || files.length == 0){
            throw new RuntimeException(dirPath + "is an empty folder");
        }

        //按照修改时间倒叙排序
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (File file : files) {
            System.out.println(file.getName() + simpleDateFormat.format(new Date(file.lastModified())));
        }

        FileInputStream fin = new FileInputStream(files[0]);
        InputStreamReader reader = new InputStreamReader(fin,"GBK");
        BufferedReader buffReader = new BufferedReader(reader);
        String strTmp = "";

        Map<String,String> fileInfo = new HashMap<>();
        while((strTmp = buffReader.readLine())!=null){
//            System.out.println(strTmp);
            if(strTmp.startsWith("标度因数")){
                fileInfo.put("标度因数",strTmp.replace("标度因数","").trim());
            }
            if(strTmp.startsWith("线性度【0,1/2F.S】")){
                fileInfo.put("线性度【0,1/2F.S】",strTmp.replace("线性度【0,1/2F.S】","").trim());
            }
            if(strTmp.startsWith("线性度【0,F.S】")){
                fileInfo.put("线性度【0,F.S】",strTmp.replace("线性度【0,F.S】","").trim());
            }
            if(strTmp.startsWith("滞环误差")){
                fileInfo.put("滞环误差",strTmp.replace("滞环误差","").trim());
            }
            if(strTmp.startsWith("分辨率")){
                fileInfo.put("分辨率",strTmp.replace("分辨率","").trim());
            }
            if(strTmp.startsWith("阈值")){
                fileInfo.put("阈值",strTmp.replace("阈值","").trim());
            }
        }
        buffReader.close();

        System.out.println(fileInfo);
    }
}
