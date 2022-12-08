package com.ruoyi.race.service;

import com.ruoyi.race.domain.RaceParticipant;

import java.util.List;

/**
 * 比赛参与人员Service接口
 * 
 * @author kjleo
 * @date 2022-12-02
 */
public interface IRaceParticipantService 
{
    /**
     * 查询比赛参与人员
     *
     * @param participantId 比赛参与人员主键
     * @return 比赛参与人员
     */
    RaceParticipant selectRaceParticipantByParticipantId(Long participantId);

    /**
     * 查询比赛参与人员列表
     *
     * @param raceParticipant 比赛参与人员
     * @return 比赛参与人员集合
     */
    List<RaceParticipant> selectRaceParticipantList(RaceParticipant raceParticipant);

    /**
     * 新增比赛参与人员
     *
     * @param raceParticipant 比赛参与人员
     * @return 结果
     */
    int insertRaceParticipant(RaceParticipant raceParticipant);

    /**
     * 修改比赛参与人员
     *
     * @param raceParticipant 比赛参与人员
     * @return 结果
     */
    int updateRaceParticipant(RaceParticipant raceParticipant);

    /**
     * 批量删除比赛参与人员
     *
     * @param participantIds 需要删除的比赛参与人员主键集合
     * @return 结果
     */
    int deleteRaceParticipantByParticipantIds(Long[] participantIds);

    /**
     * 删除比赛参与人员信息
     *
     * @param participantId 比赛参与人员主键
     * @return 结果
     */
    int deleteRaceParticipantByParticipantId(Long participantId);


    /**
     * 删除竞赛参与者通过房间id
     *
     * @param roomId 房间id
     */
    void deleteRaceParticipantByRoomId(Long roomId);


    /**
     * 查询比赛房间用户列表
     *
     * @param roomId 房间id
     * @return int
     */
    List<Long> selectRaceUserListByRoom(Long roomId);


    void insertRaceUsers(List<Long> users, Long roomId);
}
