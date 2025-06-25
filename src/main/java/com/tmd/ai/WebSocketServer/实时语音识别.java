package com.tmd.ai.WebSocketServer;

import com.alibaba.fastjson.JSONObject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/*约束
接口调用方式限制：不支持前端直接调用API，需通过后端中转。*/
/*
建立连接：客户端与服务端建立WebSocket连接。

开启任务：

客户端发送run-task指令以开启任务。

客户端收到服务端返回的task-started事件，标志着任务已成功开启，可以进行后续步骤。

发送音频流：

客户端开始发送音频流，并同时接收服务端持续返回的result-generated事件，该事件包含语音识别结果。

通知服务端结束任务：

客户端发送finish-task指令通知服务端结束任务，并继续接收服务端返回的result-generated事件。

任务结束：

客户端收到服务端返回的task-finished事件，标志着任务结束。

关闭连接：客户端关闭WebSocket连接。
*/
/*
在编写WebSocket客户端代码时，为了同时发送和接收消息，通常采用异步编程。您可以按照以下步骤来编写程序：

建立WebSocket连接：首先，初始化并建立与服务器的WebSocket连接。

异步监听服务器消息：启动一个单独的线程（具体实现方式因编程语言而异）来监听服务器返回的消息，根据消息内容进行相应的操作。

发送消息：在不同于监听服务器消息的线程中（例如主线程，具体实现方式因编程语言而异），向服务器发送消息。

关闭连接：在程序结束前，确保关闭WebSocket连接以释放资源。
*/

@Component
@ServerEndpoint("/realtime/audio/websocket/{sid}")
@Slf4j
public class 实时语音识别 {
//服务端给客户端发消息
//存放会话对象
    public static  final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();
   // private static final Logger logger = Logger.getLogger(WebSocketServer.class.getName());
    private static final String apikey="";
    static  String  message1="";
    static final Object message=new Object();
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        sessionMap.put(sid, session);
        log.info("[连接建立] 客户端: {} | 当前在线: {}", sid, sessionMap.size());
        sendToSpecificClient(sid);
    }
    public void sendToSpecificClient(String  sid){
        Session session = sessionMap.get(sid);
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText("客户端连接成功，下面准备处理你传递的数据");
            }
            else {
                log.error("客户端[{}]会话已关闭", sid);
                sessionMap.remove(sid); // 清理无效会话
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendToSpecificClient(String  sid,String message){
        Session session = sessionMap.get(sid);
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
            else {
                log.error("客户端[{}]会话已关闭", sid);
                sessionMap.remove(sid); // 清理无效会话
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendToSpecificClient1(String  sid){
        Session session = sessionMap.get(sid);
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText("您的数据已经传递给ai，敬请等待");
            }
            else {
                log.error("客户端[{}]会话已关闭", sid);
                sessionMap.remove(sid); // 清理无效会话
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @OnMessage
    public void onMessage(ByteBuffer  message, @PathParam("sid") String sid) {
       log.info("[接收消息] 获取音频数据: {}", message);
       log.info("[接受消息的人{}", sid);
        // 发送数据给API
        APIWebsocket apiWebsocket = new APIWebsocket();
        Map<String, Object> connect = apiWebsocket.connect();
        Integer success = (Integer)  connect.get("success");
        if (success == 1) {
            log.info("[连接成功]");
            apiWebsocket.sendMessage(message,(Session)connect.get("session"));
            sendToSpecificClient1(sid);
            new Thread(() -> {
                try {
                    // 等待AI返回结果（假设通过message1获取）
                    synchronized (message) {
                        message.wait(); // 等待APIWebsocket通知
                    }
                    sendToSpecificClient(sid, "AI识别结果：" + message1);
                } catch (InterruptedException e) {
                    log.error("结果回传线程异常", e);
                }
            }).start();
        }

    }
    @OnClose
    public void onClose(Session session, @PathParam("sid") String sid) {
        Session remove = sessionMap.remove(sid);
        if (remove != null) {
            log.info("[连接关闭] 客户端: {} | 当前在线: {}", sid, sessionMap.size());
        }
        log.info("[连接关闭] 剩余在线: {}", sessionMap.size());
    }
    @OnError
    public void onError(Throwable error) {
        log.error("[连接错误] 错误信息: {}", error.getMessage());
        System.out.println("发生错误");
    }
}











@Component
@ClientEndpoint(configurator = CustomConfiguration.class)
@Slf4j
class APIWebsocket {
   //作为客户端向ai发送消息
    private Session session;
    @OnOpen
    public void onOpen() throws DeploymentException, IOException {

        //开始链接服务器端，调用WebSocket库函数（具体实现方式因编程语言或库函数而异），将请求头和URL传入以建立WebSocket连接。
        //
        //请求头中需添加如下鉴权信息：
        //
        //
        //{
        //    "Authorization": "bearer <your_dashscope_api_key>", // 将<your_dashscope_api_key>替换成您自己的API Key
        //    "user-agent": "your_platform_info", //可选
        //    "X-DashScope-WorkSpace": workspace, // 可选
        //    "X-DashScope-DataInspection": "enable"
        //}
        //WebSocket URL固定如下：
        //
        //
        //wss://dashscope.aliyuncs.com/api-ws/v1/inference
       log.info("连接成功");

    }
    @OnMessage
    public void onMessage(String message) throws IOException {
        synchronized (实时语音识别.message) {
            JSONObject jsonObject = JSONObject.parseObject(message);
            JSONObject head = jsonObject.getJSONObject("head");
            if(head.getString("event").equals("task-started")){
                log.info("[开始识别]，服务器知道任务已经开启");
                // 开始识别，调用sendmessage发送消息，激活下面的锁
                APIWebsocket.class.notify();
            }else if(head.getString("event").equals("result-generated")){
                log.info("结果开始生成,开始接受");
                JSONObject payload = jsonObject.getJSONObject("payload");
                JSONObject output = payload.getJSONObject("output");
                JSONObject transcription = output.getJSONObject("transcription");
                String string = transcription.getString("text");
                实时语音识别.message1 = string;
                实时语音识别.message.notify();
            }else if(head.getString("event").equals("task-completed")){
                log.info("[任务完成]");
                //任务可以结束了，关闭链接
                APIWebsocket.class.notify();
                session.close();
            }else{
                log.info("[错误消息]{}",head.getString("error_message"));
            }
            //实时语音识别.message1 = message;
            //实时语音识别.message.notify(); // 通知等待的线程
        }
        log.info("[接收消息] 获取数据: {}", message);
    }
    @OnClose
    public void onClose() {
        System.out.println("连接关闭");
    }
    @OnError
    public void onError(Throwable error) {
        System.out.println("发生错误");
    }
    public void sendMessage(ByteBuffer message, Session session) {
        try {
           // session.getBasicRemote().sendText(message);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //先发送run-task指令，等到上面返回task-started消息，再发送音频流，发送完之后。发送finish-task指令
                        //所以需要一个锁来锁住，保证先发送run-task，再发送音频流，最后发送finish-task
                        String string = UUID.randomUUID().toString();
                        String runTaskMessage = "{\"header\":{" +
                                "\"streaming\":\"duplex\"," +
                                "\"task_id\":"+string+"\"\"," +
                                "\"action\":\"run-task\"}," +
                                "\"payload\":{" +
                                "\"model\":\"gummy-realtime-v1\"," +
                                "\"parameters\":{" +
                                "\"sample_rate\":16000," +
                                "\"format\":\"pcm\"," +
                                "\"source_language\":null," +
                                "\"transcription_enabled\":true," +
                                "\"translation_enabled\":true," +
                                "\"translation_target_languages\":[\"en\"]}," +
                                "\"input\":{}," +
                                "\"task\":\"asr\"," +
                                "\"task_group\":\"audio\"," +
                                "\"function\":\"recognition\"}}";
                        session.getBasicRemote().sendText(runTaskMessage);
                        synchronized (this){
                            this.wait();
                        }
                        //被唤醒之后
                        //将数据类型转换成pcm，而且源数据格式就是pcm
                       session.getBasicRemote().sendBinary(message);
                        synchronized (this){
                            this.wait();
                        }
                        session.getBasicRemote().sendText("{\"header\":{" +
                                "\"streaming\":\"duplex\"," +
                                "\"task_id\":"+string+"\"\"," +
                                "\"action\":\"finish-task\"}," +
                                "\"payload\":{" +
                                "\"input\":\"{}\",}");
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        } catch (Exception e){
            e.printStackTrace();
            log.error("发送消息失败{}",e.getMessage());
        }
    }
    public Map<String, Object> connect(){
        try {
            if(session== null||!session.isOpen()) {
                URI uri = new URI("wss://dashscope.aliyuncs.com/api-ws/v1/inference");
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                Session session = container.connectToServer(this, uri);
                this.session = session;
            }
            Map<String, Object> config = new HashMap<>();
            config.put("success",1);
            config.put("session", session);
            return config;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}








@Slf4j
@Component
class CustomConfiguration extends ClientEndpointConfig.Configurator{
    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        super.beforeRequest(headers);
        headers.put("Authorization", Collections.singletonList("Bearer <your_dashscope_api_key>"));
        headers.put("user-agent", Collections.singletonList("your_platform_info"));
        headers.put("X-DashScope-WorkSpace", Collections.singletonList("workspace"));
        headers.put("X-DashScope-DataInspection", Collections.singletonList("enable"));
        log.info("请求头为{}", headers);
    }
}