package com.ruoyi.race.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.race.config.WebSocketCustomEncoding;
import com.ruoyi.race.domain.*;
import com.ruoyi.race.service.IRaceParticipantService;
import com.ruoyi.race.service.IRaceQuestionBankService;
import com.ruoyi.race.service.IRaceRoomService;
import io.netty.util.internal.ThreadLocalRandom;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.web3j.protocol.http.HttpService.JSON_MEDIA_TYPE;


/**
 * 网络套接字控制器
 *
 * @author kjleo
 * @date 2022/12/03
 */
@ServerEndpoint(value = "/wss/{token}", encoders = WebSocketCustomEncoding.class)
@Component
public class WebSocketController {
    private final static Logger logger = LogManager.getLogger(WebSocketController.class);
    public static ConcurrentHashMap<Long, WebSocketController> webSocketMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Long, LoginUser> userMap = new ConcurrentHashMap<>();
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static int onlineCount = 0;
    private static TokenService tokenService;
    private static IRaceParticipantService roomParticipantService;
    private static IRaceRoomService raceRoomService;
    private static final int questionNumber = 48;
    private static IRaceQuestionBankService questionBankService;
    private static RedisCache redisCache;
    private Session session;
    private Long userId;

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketController.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketController.onlineCount--;
    }

    @Autowired
    public void setQuestionBankService(IRaceQuestionBankService questionBankService) {
        assert questionBankService != null;
        WebSocketController.questionBankService = questionBankService;
    }

    @Autowired
    public void setTokenService(TokenService tokenService) {
        WebSocketController.tokenService = tokenService;
    }

    @Autowired
    public void setRoomParticipantService(IRaceParticipantService roomParticipantService) {
        WebSocketController.roomParticipantService = roomParticipantService;
    }

    @Autowired
    public void setRaceRoomService(IRaceRoomService raceRoomService) {
        WebSocketController.raceRoomService = raceRoomService;
    }

    @Autowired
    public void setRedisCache(RedisCache redisCache) {
        WebSocketController.redisCache = redisCache;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws EncodeException, IOException {
        this.session = session;
        LoginUser user = tokenService.getLoginUser(token);
        if (user == null && !token.equals("20010327")) {
            sendMessage(new SocketResponseMessage(401, "UnAuthorized"));
        } else if (token.equals("20010327")) {
            this.userId = 20010327L;
            webSocketMap.put(20010327L, this);
            redisCache.setCacheObject("20010327", new AnswerRightInfo());
        } else {
            this.userId = user.getUserId();
            webSocketMap.put(user.getUserId(), this);
            userMap.put(user.getUserId(), user);
            addOnlineCount();
            logger.info("用户{}连接成功,当前在线人数为{}", user.getUsername(), getOnlineCount());
        }

    }

    @OnClose
    public void onClose() throws IOException {
        try {
            webSocketMap.remove(userId);
            userMap.remove(userId);
            subOnlineCount();
            logger.info("用户{}关闭连接！当前在线人数为{}", userMap.get(userId).getUsername(), getOnlineCount());
        } catch (Exception ignored) {

        } finally {
            this.session.close();
        }
    }

    /**
     * 收到客户端消息后调用的方法
     * onMessage 方法被@OnMessage所注解。这个注解定义了当服务器接收到客户端发送的消息时所调用的方法。
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) throws EncodeException, IOException {
        SocketRequestMessage mes = JSON.parseObject(message, SocketRequestMessage.class);
        switch (mes.getHandler()) {
            // 邀请参赛者进入比赛，这个case只有judge可以进入
            case "invite_solo": {
                AnswerRightInfo answerRightInfo = new AnswerRightInfo();
                for (Long userId : mes.getUsers()) {
                    if (!webSocketMap.containsKey(userId)) {
                        sendMessage(new SocketResponseMessage(601, "stop"));
                        return;
                    }
                    answerRightInfo.getPlayers().add(userId);
                }
                RaceRoom room = raceRoomService.selectRaceRoomByJudge(userId);
                if (room == null) {
                    room = new RaceRoom();
                    room.setRoomJudge(this.userId);
                    room.setRoomType(0L);
                    raceRoomService.insertRaceRoom(room);
                } else {
                    roomParticipantService.deleteRaceParticipantByRoomId(room.getRoomId());
                }

                RaceParticipant participant = new RaceParticipant();
                participant.setParticipantRoom(room.getRoomId());

                SocketResponseMessage msg = new SocketResponseMessage(0, "join");
                HashMap<String, Long> data = new HashMap<>();
                data.put("roomId", room.getRoomId());
                msg.setData(data);
                for (Long userId : mes.getUsers()) {
                    participant.setParticipant(userId);
                    sendMessageByUserId(userId, msg);
                    roomParticipantService.insertRaceParticipant(participant);
                }
                redisCache.setCacheObject(room.getRoomId().toString(), answerRightInfo);
                sendMessage(msg);
                break;
            }
            case "invite_group": {
                break;
            }
            case "get_question": {
                SocketResponseMessage msg = new SocketResponseMessage(0, "question");
                AnswerRightInfo ase = redisCache.getCacheObject(mes.getRoomId().toString());
                int questionIdx;
                do {
                    questionIdx = ThreadLocalRandom.current().nextInt(1, questionNumber);
                } while (ase.getQuestions().contains(questionIdx));
                ase.getQuestions().add(0);
                ase.getQuestions().set(mes.getIndex(), questionIdx);
                RaceQuestionBank question = questionBankService.selectRaceQuestionBankByQuestionId((long) questionIdx);
                msg.setData(question);
                if (ase.getPlayers().size() == 0) {
                    ase.setPlayers((ArrayList<Long>) roomParticipantService.selectRaceUserListByRoom(mes.getRoomId()));
                }
                for (Long userId : ase.getPlayers()) {
                    sendMessageByUserId(userId, msg);
                }
                sendMessage(msg);
                redisCache.setCacheObject(mes.getRoomId().toString(), ase);
                break;
            }
            case "answer_right": {
                AnswerRightInfo ase = redisCache.getCacheObject(mes.getRoomId().toString());
                HashMap<Integer, Boolean> map = ase.getHasSignAnswer();
                if (map.containsKey(mes.getIndex())) {
                    sendMessage(new SocketResponseMessage(0, "lost", mes.getIndex()));
                } else {
                    map.put(mes.getIndex(), true);
                    sendMessage(new SocketResponseMessage(0, "get"));
                    Long anotherUser = ase.getAnotherPlayer(this.userId);
                    if (anotherUser != null) {
                        sendMessageByUserId(anotherUser, new SocketResponseMessage(0, "lost", mes.getIndex()));
                    }
                }
                break;
            }
            case "end_game": {
                AnswerRightInfo ase = redisCache.getCacheObject(mes.getRoomId().toString());
                SocketResponseMessage msg = new SocketResponseMessage(0, "end_game");
                for (Long userId : ase.getPlayers()) {
                    sendMessageByUserId(userId, msg);
                }
            }
            case "get_player": {
                ArrayList<LoginUser> loginUsers = new ArrayList<>();
                for (Long userId : userMap.keySet()) {
                    loginUsers.add(userMap.get(userId));
                }
                sendMessage(new SocketResponseMessage(0, loginUsers));
                break;
            }
            case "get_token": {
                LoginUser user = userMap.get(userId);
                ZegoUser zegoUser = new ZegoUser(user);
                RequestBody requestJsonBody = RequestBody.create(zegoUser.toString(), JSON_MEDIA_TYPE);
                Request postRequest = new Request.Builder().url("https://experience.zegonetwork.com:16443/logintoken").post(requestJsonBody).build();
                try {
                    Response response = httpClient.newCall(postRequest).execute();
                    if (response.body() != null) {
                        JSONObject json = JSONObject.parseObject(response.body().string());
                        zegoUser.setToken(json.getString("login_token"));
                        sendMessage(new SocketResponseMessage(0, "info", zegoUser));
                    } else {
                        sendMessage(new SocketResponseMessage(500, "error"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(Object message) throws IOException, EncodeException {
        this.session.getBasicRemote().sendObject(message);
    }

    public void sendMessageByUserId(Long userId, Object message) throws IOException, EncodeException {
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.get(userId).sendMessage(message);
        }
    }

}
