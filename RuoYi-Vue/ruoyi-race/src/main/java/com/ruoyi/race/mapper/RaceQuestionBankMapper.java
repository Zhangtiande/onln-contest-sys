package com.ruoyi.race.mapper;

import java.util.List;
import com.ruoyi.race.domain.RaceQuestionBank;

/**
 * 题库Mapper接口
 * 
 * @author kjleo
 * @date 2022-12-03
 */
public interface RaceQuestionBankMapper 
{
    /**
     * 查询题库
     * 
     * @param questionId 题库主键
     * @return 题库
     */
    public RaceQuestionBank selectRaceQuestionBankByQuestionId(Long questionId);

    /**
     * 查询题库列表
     * 
     * @param raceQuestionBank 题库
     * @return 题库集合
     */
    public List<RaceQuestionBank> selectRaceQuestionBankList(RaceQuestionBank raceQuestionBank);

    /**
     * 新增题库
     * 
     * @param raceQuestionBank 题库
     * @return 结果
     */
    public int insertRaceQuestionBank(RaceQuestionBank raceQuestionBank);

    /**
     * 修改题库
     * 
     * @param raceQuestionBank 题库
     * @return 结果
     */
    public int updateRaceQuestionBank(RaceQuestionBank raceQuestionBank);

    /**
     * 删除题库
     * 
     * @param questionId 题库主键
     * @return 结果
     */
    public int deleteRaceQuestionBankByQuestionId(Long questionId);

    /**
     * 批量删除题库
     * 
     * @param questionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRaceQuestionBankByQuestionIds(Long[] questionIds);
}
