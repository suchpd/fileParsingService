package com.file.parsing.service.webSocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.file.parsing.service.services.SaveFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/uploadFile/{sid}")
@Component
public class WebSocketUploadServer {
    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;
    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    private static CopyOnWriteArraySet<WebSocketUploadServer> webSocketSet = new CopyOnWriteArraySet<>();
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    /**
     * 注入文件保存的接口
     */
    private static SaveFile saveFile;
    @Autowired
    public void setSaveFileI(SaveFile saveFile) {
        WebSocketUploadServer.saveFile = saveFile;
    }
    /**
     * 保证文件对象和文件路径的唯一性
     */
    private HashMap docUrl;
    /**
     * 结束标识判断
     */
    private String endupload = "over";
    /**
     * 连接建立成功时调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        this.session = session;
        //加入set中
        webSocketSet.add(this);
        //在线人数加1
        addOnlineCount();
        System.out.println(sid + "连接成功" + "----当前在线人数为：" + onlineCount);
    }
    /**
     * 连接关闭时调用的方法
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        //在线人数减1
        subOnlineCount();
        //从set中删除
        webSocketSet.remove(this);
        System.out.println(sid + "已关闭连接" + "----剩余在线人数为：" + onlineCount);
    }
    /**
     * 接收客户端发送的消息时调用的方法
     *
     * @param message 接收的字符串消息。该消息应当为json字符串
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        //前端传过来的消息都是一个json
        JSONObject jsonObject = JSON.parseObject(message);
        //消息类型
        String type = jsonObject.getString("type");
        //消息内容
        String data = jsonObject.getString("data");
        //判断类型是否为文件名
        if ("fileName".equals(type)) {
            System.out.println("传输文件为:" + data);
            //此处的 “.”需要进行转义
            /*String[] split = data.split("\\.");*/
            try {
                Map<String, Object> map = saveFile.docPath(data);
                docUrl = (HashMap) map;
                this.sendMessage("ok");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if ("fileCount".equals(type)){
            System.out.println("传输第"+data+"份");
        }
        //判断是否结束
        else if (endupload.equals(type)) {
            System.out.println("===============>传输成功");
            //返回一个文件下载地址
            String path = (String) docUrl.get("nginxPath");
            //返回客户端文件地址
            try {
                this.sendMessage(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 该方法用于接收字节流数组
     *
     * @param message 文件字节流数组
     * @param session 会话
     */
    @OnMessage
    public void onMessage(byte[] message, Session session) {
        //群发消息
        try {
            //将流写入文件
            saveFile.saveFileFromBytes(message,docUrl);
            //文件写入成功，返回一个ok
            this.sendMessage("ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 服务器主动提推送消息
     *
     * @param message 消息内容
     * @throws IOException io异常抛出
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
    /**
     * 群发消息功能
     *
     * @param message 消息内容
     * @param sid     房间号
     */
    public static void sendInfo(String message, @PathParam("sid") String sid) {
        System.out.println("推送消息到窗口" + sid + "，推送内容:" + message);
        for (WebSocketUploadServer item : webSocketSet) {
            try {
                //这里可以设定只推送给这个sid的，为null则全部推送
                item.sendMessage(message);
            } catch (IOException e) {
                System.out.println("消息发送失败" + e.getMessage());
                return;
            }
        }
    }
    /**
     * 原子性的++操作
     */
    public static synchronized void addOnlineCount() {
        WebSocketUploadServer.onlineCount++;
    }
    /**
     * 原子性的--操作
     */
    public static synchronized void subOnlineCount() {
        WebSocketUploadServer.onlineCount--;
    }

}
