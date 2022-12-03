package com.ruoyi.race.domain;

import java.io.Serializable;
import java.util.HashMap;

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

    public AnswerRightInfo() {
        this.hasSignAnswer = new HashMap<>();
    }

    public HashMap<Integer, Boolean> getHasSignAnswer() {
        return hasSignAnswer;
    }
}
