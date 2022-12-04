package com.ruoyi.race.controller;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.race.config.WebSocketCustomEncoding;
import com.ruoyi.race.domain.*;
import com.ruoyi.race.service.IRaceParticipantService;
import com.ruoyi.race.service.IRaceQuestionBankService;
import com.ruoyi.race.service.IRaceRoomService;
import io.netty.util.internal.ThreadLocalRandom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 网络套接字控制器
 *
 * @author kjleo
 * @date 2022/12/03
 */
@ServerEndpoint(value = "/ws/{token}", encoders = WebSocketCustomEncoding.class)
@Component
public class WebSocketController {
    private final static Logger logger = LogManager.getLogger(WebSocketController.class);
    public static ConcurrentHashMap<Long, WebSocketController> webSocketMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Long, LoginUser> userMap = new ConcurrentHashMap<>();
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
        if (user == null) {
            sendMessage(new SocketResponseMessage(401, "UnAuthorized"));
            return;
        }
        this.userId = user.getUserId();
        webSocketMap.put(user.getUserId(), this);
        userMap.put(user.getUserId(), user);
        addOnlineCount();
        logger.info("用户{}连接成功,当前在线人数为{}", user.getUsername(), getOnlineCount());
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
                for (Long userId : mes.getUsers()) {
                    if (!webSocketMap.containsKey(userId)) {
                        sendMessage(new SocketResponseMessage(601, "stop"));
                        return;
                    }
                }
                RaceRoom room = raceRoomService.selectRaceRoomByJudge(userId);
                if (room == null) {
                    room = new RaceRoom();
                    room.setRoomJudge(this.userId);
                    room.setRoomType(0L);
                    raceRoomService.insertRaceRoom(room);
                }

                RaceParticipant participant = new RaceParticipant();
                participant.setParticipantRoom(room.getRoomId());
                AnswerRightInfo answerRightInfo = new AnswerRightInfo();
                int i = 0;

                SocketResponseMessage msg = new SocketResponseMessage(0, "join");
                HashMap<String, Long> data = new HashMap<>();
                data.put("roomId", room.getRoomId());
                msg.setData(data);

                sendMessage(msg);

                for (Long userId : mes.getUsers()) {
                    participant.setParticipant(userId);
                    sendMessageByUserId(userId, msg);
                    answerRightInfo.getPlayers()[i++] = userId;
                    roomParticipantService.insertRaceParticipant(participant);
                }
                redisCache.setCacheObject(room.getRoomId().toString(), new AnswerRightInfo());
                break;
            }
            case "invite_group": {
                break;
            }
            case "get_question": {
                SocketResponseMessage msg = new SocketResponseMessage(0);
                AnswerRightInfo ase = redisCache.getCacheObject(mes.getRoomId().toString());
                int questionIdx;
                do {
                    questionIdx = ThreadLocalRandom.current().nextInt(1, questionNumber);
                } while (ase.getQuestions().contains(questionIdx));
                ase.getQuestions().set(mes.getIndex(), questionIdx);
                RaceQuestionBank question = questionBankService.selectRaceQuestionBankByQuestionId((long) questionIdx);
                msg.setData(question);
                for (Long userId : ase.getPlayers()) {
                    sendMessageByUserId(userId, msg);
                }
                sendMessage(msg);
                break;
            }
            case "answer_right": {
                AnswerRightInfo ase = redisCache.getCacheObject(mes.getRoomId().toString());
                HashMap<Integer, Boolean> map = ase.getHasSignAnswer();
                if (map.containsKey(mes.getIndex())) {
                    sendMessage(new SocketResponseMessage(0, "lost"));
                } else {
                    map.put(mes.getIndex(), true);
                    sendMessage(new SocketResponseMessage(0, "get"));
                }
                break;
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
