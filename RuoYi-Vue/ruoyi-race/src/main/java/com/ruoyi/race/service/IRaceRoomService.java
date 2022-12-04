package com.ruoyi.race.service;

import com.ruoyi.race.domain.RaceRoom;

import java.util.List;

/**
 * 竞赛房间Service接口
 *
 * @author kjleo
 * @date 2022-12-02
 */
public interface IRaceRoomService {
    /**
     * 查询竞赛房间
     *
     * @param roomId 竞赛房间主键
     * @return 竞赛房间
     */
    public RaceRoom selectRaceRoomByRoomId(Long roomId);

    /**
     * 查询竞赛房间列表
     *
     * @param raceRoom 竞赛房间
     * @return 竞赛房间集合
     */
    public List<RaceRoom> selectRaceRoomList(RaceRoom raceRoom);

    /**
     * 新增竞赛房间
     *
     * @param raceRoom 竞赛房间
     * @return 结果
     */
    public int insertRaceRoom(RaceRoom raceRoom);

    /**
     * 修改竞赛房间
     *
     * @param raceRoom 竞赛房间
     * @return 结果
     */
    public int updateRaceRoom(RaceRoom raceRoom);

    /**
     * 批量删除竞赛房间
     *
     * @param roomIds 需要删除的竞赛房间主键集合
     * @return 结果
     */
    public int deleteRaceRoomByRoomIds(Long[] roomIds);

    /**
     * 删除竞赛房间信息
     *
     * @param roomId 竞赛房间主键
     * @return 结果
     */
    public int deleteRaceRoomByRoomId(Long roomId);

    /**
     * 比赛房间
     *
     * @param judge 裁判
     * @return int
     */
    public RaceRoom selectRaceRoomByJudge(Long judge);
}
