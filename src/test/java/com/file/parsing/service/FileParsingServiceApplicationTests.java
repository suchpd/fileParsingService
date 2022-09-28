package com.file.parsing.service;

import com.file.parsing.service.webSocket.WebSocketServer;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileParsingServiceApplicationTests {

    @Autowired
    WebSocketServer webSocketServer;

    @Test
    void contextLoads() {

        webSocketServer.onMessage("hello","");
    }

}
