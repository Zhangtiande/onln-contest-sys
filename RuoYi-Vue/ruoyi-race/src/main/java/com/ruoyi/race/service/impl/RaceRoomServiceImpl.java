package com.ruoyi.race.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.race.domain.RaceRoom;
import com.ruoyi.race.mapper.RaceRoomMapper;
import com.ruoyi.race.service.IRaceRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 竞赛房间Service业务层处理
 * 
 * @author kjleo
 * @date 2022-12-02
 */
@Service
public class RaceRoomServiceImpl implements IRaceRoomService 
{
    @Autowired
    private RaceRoomMapper raceRoomMapper;

    /**
     * 查询竞赛房间
     * 
     * @param roomId 竞赛房间主键
     * @return 竞赛房间
     */
    @Override
    public RaceRoom selectRaceRoomByRoomId(Long roomId)
    {
        return raceRoomMapper.selectRaceRoomByRoomId(roomId);
    }

    /**
     * 查询竞赛房间列表
     * 
     * @param raceRoom 竞赛房间
     * @return 竞赛房间
     */
    @Override
    public List<RaceRoom> selectRaceRoomList(RaceRoom raceRoom)
    {
        return raceRoomMapper.selectRaceRoomList(raceRoom);
    }

    /**
     * 新增竞赛房间
     * 
     * @param raceRoom 竞赛房间
     * @return 结果
     */
    @Override
    public int insertRaceRoom(RaceRoom raceRoom)
    {
        raceRoom.setCreateTime(DateUtils.getNowDate());
        return raceRoomMapper.insertRaceRoom(raceRoom);
    }

    /**
     * 修改竞赛房间
     * 
     * @param raceRoom 竞赛房间
     * @return 结果
     */
    @Override
    public int updateRaceRoom(RaceRoom raceRoom)
    {
        return raceRoomMapper.updateRaceRoom(raceRoom);
    }

    /**
     * 批量删除竞赛房间
     * 
     * @param roomIds 需要删除的竞赛房间主键
     * @return 结果
     */
    @Override
    public int deleteRaceRoomByRoomIds(Long[] roomIds)
    {
        return raceRoomMapper.deleteRaceRoomByRoomIds(roomIds);
    }

    /**
     * 删除竞赛房间信息
     *
     * @param roomId 竞赛房间主键
     * @return 结果
     */
    @Override
    public int deleteRaceRoomByRoomId(Long roomId) {
        return raceRoomMapper.deleteRaceRoomByRoomId(roomId);
    }

    /**
     * 比赛房间
     *
     * @param judge 裁判
     * @return int
     */
    @Override
    public RaceRoom selectRaceRoomByJudge(Long judge) {
        return raceRoomMapper.selectRaceRoomByJudge(judge);
    }
}
