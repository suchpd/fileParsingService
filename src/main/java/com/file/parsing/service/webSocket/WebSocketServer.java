package com.file.parsing.service.webSocket;

import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{sid}")
public class WebSocketServer {

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

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
     * @param message 接收的字符串消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        System.out.println(sid + "发送消息为:" + message);
        sendInfo(message, sid);
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
        for (WebSocketServer item : webSocketSet) {
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
        WebSocketServer.onlineCount++;
    }
    /**
     * 原子性的--操作
     */
    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}
