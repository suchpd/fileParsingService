package com.file.parsing.service.utils;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

import java.io.*;
import java.util.Arrays;

public class SmbFileUtil {
    private static final String USER_DOMAIN = null;
    private static final String USER_ACCOUNT = "Such";
    private static final String USER_PWS = "4152";
    private static final NtlmPasswordAuthentication authentication = new NtlmPasswordAuthentication(USER_DOMAIN, USER_ACCOUNT, USER_PWS);


    /**
     * 遍历指定目录下的文件
     * @param shareDirectory    共享文件夹
     * @throws Exception    异常
     */
    public static SmbFile[] getAllSmbFiles(String shareDirectory) throws Exception {
        SmbFile[] smbFiles = new SmbFile[0];
        SmbFile remoteFile = new SmbFile(shareDirectory, authentication);
        if (remoteFile.exists()) {
            smbFiles = remoteFile.listFiles();
        } else {
            System.out.print("文件不存在");
        }

        return smbFiles;
    }

    /**
     * 下载共享文件夹中的文件
     * @param shareUrl  共享文件夹URL
     * @param localDirectory    下载至本地文件夹的路径
     * @throws Exception    异常
     */
    public static void smbDownload(String shareUrl, String localDirectory) throws Exception {
        SmbFile remoteFile = new SmbFile(shareUrl, authentication);
        if (!remoteFile.exists()) {
            System.out.println("共享文件不存在");
            return;
        }

        downloadFile(remoteFile,localDirectory);
    }

    /**
     * 下载共享文件夹中最新的文件
     * @param shareDirectory    共享文件夹地址
     * @param localDirectory    下载至本地文件夹的路径
     */
    public static void smbDownloadLastFile(String shareDirectory,String localDirectory) throws Exception {
        SmbFile[] smbFiles = getAllSmbFiles(shareDirectory);
        if(smbFiles.length == 0){
            System.out.println("文件夹为空！");
            return;
        }

        //按照修改时间倒叙排序
        Arrays.sort(smbFiles, (o1, o2) -> (int) (o2.getLastModified() - o1.getLastModified()));

        downloadFile(smbFiles[0],localDirectory);
    }

    public static void downloadFile(SmbFile smbFile, String localDirectory) throws IOException {
        // 有文件的时候再初始化输入输出流
        InputStream in = null;
        OutputStream out = null;
        try {
            String fileName = smbFile.getName();
            File localFile = new File(localDirectory + File.separator + fileName);
            File fileParent = localFile.getParentFile();
            if (null != fileParent && !fileParent.exists()) {
                fileParent.mkdirs();
            }
            in = new BufferedInputStream(new SmbFileInputStream(smbFile));
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                out.write(buffer);
                buffer = new byte[1024];
            }
            out.flush(); //刷新缓冲区输出流

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.close();
            in.close();
        }
    }
}
