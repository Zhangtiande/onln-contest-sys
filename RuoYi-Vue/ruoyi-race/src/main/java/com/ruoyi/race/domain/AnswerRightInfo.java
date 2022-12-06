package com.ruoyi.race.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 抢答权信息
 *
 * @author kjleo
 * @date 2022/12/03
 */
public class AnswerRightInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * key为题目序号，对应的key有值代表抢答权已被夺取
     */
    private final HashMap<Integer, Boolean> hasSignAnswer;
    private final List<Integer> questions;

    private ArrayList<Long> players;

    public AnswerRightInfo() {
        this.hasSignAnswer = new HashMap<>();
        questions = new ArrayList<>(10);
        players = new ArrayList<>();
    }

    public ArrayList<Long> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Long> players) {
        this.players = players;
    }

    public Long getAnotherPlayer(Long userId) {
        for (Long user : players) {
            if (!userId.equals(user)) return user;
        }
        return null;
    }

    public List<Integer> getQuestions() {
        return questions;
    }


    public HashMap<Integer, Boolean> getHasSignAnswer() {
        return hasSignAnswer;
    }
}
