package com.gmall.manager;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GmallManagerWebApplicationTests {

    @Test
    void contextLoads() throws IOException, MyException {
        String tracker = GmallManagerWebApplicationTests.class.getResource(
                "/tracker.properties").getPath();
        ClientGlobal.init(tracker);

        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();

        StorageClient storageClient = new StorageClient(trackerServer, null);


        String[] uploadInfos = storageClient.upload_file(
                "C:\\Users\\dell\\Desktop\\timg.jpg", "jpg", null);
        StringBuilder url = new StringBuilder("http://120.55.94.181:8888");
        for (String uploadInfo: uploadInfos){
            url.append("/").append(uploadInfo);
        }
        System.out.println(url);
    }

}
