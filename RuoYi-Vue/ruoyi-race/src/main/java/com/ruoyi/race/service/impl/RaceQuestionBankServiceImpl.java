package com.ruoyi.race.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.race.mapper.RaceQuestionBankMapper;
import com.ruoyi.race.domain.RaceQuestionBank;
import com.ruoyi.race.service.IRaceQuestionBankService;

/**
 * 题库Service业务层处理
 * 
 * @author kjleo
 * @date 2022-12-03
 */
@Service
public class RaceQuestionBankServiceImpl implements IRaceQuestionBankService 
{
    @Autowired
    private RaceQuestionBankMapper raceQuestionBankMapper;

    /**
     * 查询题库
     * 
     * @param questionId 题库主键
     * @return 题库
     */
    @Override
    public RaceQuestionBank selectRaceQuestionBankByQuestionId(Long questionId)
    {
        return raceQuestionBankMapper.selectRaceQuestionBankByQuestionId(questionId);
    }

    /**
     * 查询题库列表
     * 
     * @param raceQuestionBank 题库
     * @return 题库
     */
    @Override
    public List<RaceQuestionBank> selectRaceQuestionBankList(RaceQuestionBank raceQuestionBank)
    {
        return raceQuestionBankMapper.selectRaceQuestionBankList(raceQuestionBank);
    }

    /**
     * 新增题库
     * 
     * @param raceQuestionBank 题库
     * @return 结果
     */
    @Override
    public int insertRaceQuestionBank(RaceQuestionBank raceQuestionBank)
    {
        return raceQuestionBankMapper.insertRaceQuestionBank(raceQuestionBank);
    }

    /**
     * 修改题库
     * 
     * @param raceQuestionBank 题库
     * @return 结果
     */
    @Override
    public int updateRaceQuestionBank(RaceQuestionBank raceQuestionBank)
    {
        return raceQuestionBankMapper.updateRaceQuestionBank(raceQuestionBank);
    }

    /**
     * 批量删除题库
     * 
     * @param questionIds 需要删除的题库主键
     * @return 结果
     */
    @Override
    public int deleteRaceQuestionBankByQuestionIds(Long[] questionIds)
    {
        return raceQuestionBankMapper.deleteRaceQuestionBankByQuestionIds(questionIds);
    }

    /**
     * 删除题库信息
     * 
     * @param questionId 题库主键
     * @return 结果
     */
    @Override
    public int deleteRaceQuestionBankByQuestionId(Long questionId)
    {
        return raceQuestionBankMapper.deleteRaceQuestionBankByQuestionId(questionId);
    }
}
