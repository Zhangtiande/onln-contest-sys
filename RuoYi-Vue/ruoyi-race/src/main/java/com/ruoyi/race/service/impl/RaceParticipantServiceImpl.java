package com.ruoyi.race.service.impl;

import com.ruoyi.race.domain.RaceParticipant;
import com.ruoyi.race.mapper.RaceParticipantMapper;
import com.ruoyi.race.service.IRaceParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 比赛参与人员Service业务层处理
 * 
 * @author kjleo
 * @date 2022-12-02
 */
@Service
public class RaceParticipantServiceImpl implements IRaceParticipantService 
{
    @Autowired
    private RaceParticipantMapper raceParticipantMapper;

    /**
     * 查询比赛参与人员
     * 
     * @param participantId 比赛参与人员主键
     * @return 比赛参与人员
     */
    @Override
    public RaceParticipant selectRaceParticipantByParticipantId(Long participantId)
    {
        return raceParticipantMapper.selectRaceParticipantByParticipantId(participantId);
    }

    /**
     * 查询比赛参与人员列表
     * 
     * @param raceParticipant 比赛参与人员
     * @return 比赛参与人员
     */
    @Override
    public List<RaceParticipant> selectRaceParticipantList(RaceParticipant raceParticipant)
    {
        return raceParticipantMapper.selectRaceParticipantList(raceParticipant);
    }

    /**
     * 新增比赛参与人员
     * 
     * @param raceParticipant 比赛参与人员
     * @return 结果
     */
    @Override
    public int insertRaceParticipant(RaceParticipant raceParticipant)
    {
        return raceParticipantMapper.insertRaceParticipant(raceParticipant);
    }

    /**
     * 修改比赛参与人员
     * 
     * @param raceParticipant 比赛参与人员
     * @return 结果
     */
    @Override
    public int updateRaceParticipant(RaceParticipant raceParticipant)
    {
        return raceParticipantMapper.updateRaceParticipant(raceParticipant);
    }

    /**
     * 批量删除比赛参与人员
     * 
     * @param participantIds 需要删除的比赛参与人员主键
     * @return 结果
     */
    @Override
    public int deleteRaceParticipantByParticipantIds(Long[] participantIds)
    {
        return raceParticipantMapper.deleteRaceParticipantByParticipantIds(participantIds);
    }

    /**
     * 删除比赛参与人员信息
     *
     * @param participantId 比赛参与人员主键
     * @return 结果
     */
    @Override
    public int deleteRaceParticipantByParticipantId(Long participantId) {
        return raceParticipantMapper.deleteRaceParticipantByParticipantId(participantId);
    }

    /**
     * 删除竞赛参与者通过房间id
     *
     * @param roomId 房间id
     */
    @Override
    public void deleteRaceParticipantByRoomId(Long roomId) {
        raceParticipantMapper.deleteRaceParticipantByRoomId(roomId);
    }

    /**
     * 查询比赛房间用户列表
     *
     * @param roomId 房间id
     * @return int
     */
    @Override
    public List<Long> selectRaceUserListByRoom(Long roomId) {
        return raceParticipantMapper.selectRaceUserListByRoom(roomId);
    }
}
