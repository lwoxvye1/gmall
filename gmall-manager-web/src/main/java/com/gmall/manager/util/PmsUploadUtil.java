package com.gmall.manager.util;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;


public class PmsUploadUtil {
    final static String PREFIX = "http://120.55.94.181:8888";

    public static String uploadImage(MultipartFile multipartFile) {
        StringBuilder imgUrl = new StringBuilder(PREFIX);
        String tracker = PmsUploadUtil.class.getResource(
                "/tracker.properties").getPath();
        try {
            ClientGlobal.init(tracker);
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageClient storageClient = new StorageClient(trackerServer, null);

            byte[] bytes = multipartFile.getBytes();
            String originalFilename = multipartFile.getOriginalFilename();
            String extName = originalFilename.substring(
                    originalFilename.lastIndexOf(".") + 1);

            String[] uploadInfos = storageClient.upload_file(
                    bytes, extName, null);
            for (String uploadInfo : uploadInfos) {
                imgUrl.append("/").append(uploadInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imgUrl.toString();
    }

}
