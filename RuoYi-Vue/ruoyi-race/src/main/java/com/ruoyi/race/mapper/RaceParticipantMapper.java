package com.ruoyi.race.mapper;

import java.util.List;
import com.ruoyi.race.domain.RaceParticipant;

/**
 * 比赛参与人员Mapper接口
 * 
 * @author kjleo
 * @date 2022-12-02
 */
public interface RaceParticipantMapper 
{
    /**
     * 查询比赛参与人员
     * 
     * @param participantId 比赛参与人员主键
     * @return 比赛参与人员
     */
    public RaceParticipant selectRaceParticipantByParticipantId(Long participantId);

    /**
     * 查询比赛参与人员列表
     * 
     * @param raceParticipant 比赛参与人员
     * @return 比赛参与人员集合
     */
    public List<RaceParticipant> selectRaceParticipantList(RaceParticipant raceParticipant);

    /**
     * 新增比赛参与人员
     * 
     * @param raceParticipant 比赛参与人员
     * @return 结果
     */
    public int insertRaceParticipant(RaceParticipant raceParticipant);

    /**
     * 修改比赛参与人员
     * 
     * @param raceParticipant 比赛参与人员
     * @return 结果
     */
    public int updateRaceParticipant(RaceParticipant raceParticipant);

    /**
     * 删除比赛参与人员
     * 
     * @param participantId 比赛参与人员主键
     * @return 结果
     */
    public int deleteRaceParticipantByParticipantId(Long participantId);

    /**
     * 批量删除比赛参与人员
     * 
     * @param participantIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRaceParticipantByParticipantIds(Long[] participantIds);
}
