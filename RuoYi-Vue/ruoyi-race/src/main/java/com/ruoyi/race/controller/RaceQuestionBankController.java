package com.ruoyi.race.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.race.domain.RaceQuestionBank;
import com.ruoyi.race.service.IRaceQuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 题库Controller
 * 
 * @author kjleo
 * @date 2022-12-03
 */
@RestController
@RequestMapping("/race/bank")
public class RaceQuestionBankController extends BaseController
{
    @Autowired
    private IRaceQuestionBankService raceQuestionBankService;

    /**
     * 查询题库列表
     */
    @PreAuthorize("@ss.hasPermi('race:bank:list')")
    @GetMapping("/list")
    public TableDataInfo list(RaceQuestionBank raceQuestionBank)
    {
        startPage();
        List<RaceQuestionBank> list = raceQuestionBankService.selectRaceQuestionBankList(raceQuestionBank);
        return getDataTable(list);
    }

    /**
     * 导出题库列表
     */
    @PreAuthorize("@ss.hasPermi('race:bank:export')")
    @Log(title = "题库", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RaceQuestionBank raceQuestionBank)
    {
        List<RaceQuestionBank> list = raceQuestionBankService.selectRaceQuestionBankList(raceQuestionBank);
        ExcelUtil<RaceQuestionBank> util = new ExcelUtil<RaceQuestionBank>(RaceQuestionBank.class);
        util.exportExcel(response, list, "题库数据");
    }

    /**
     * 获取题库详细信息
     */
    @PreAuthorize("@ss.hasPermi('race:bank:query')")
    @GetMapping(value = "/{questionId}")
    public AjaxResult getInfo(@PathVariable("questionId") Long questionId)
    {
        return success(raceQuestionBankService.selectRaceQuestionBankByQuestionId(questionId));
    }

    /**
     * 新增题库
     */
    @PreAuthorize("@ss.hasPermi('race:bank:add')")
    @Log(title = "题库", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RaceQuestionBank raceQuestionBank)
    {
        return toAjax(raceQuestionBankService.insertRaceQuestionBank(raceQuestionBank));
    }

    /**
     * 修改题库
     */
    @PreAuthorize("@ss.hasPermi('race:bank:edit')")
    @Log(title = "题库", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RaceQuestionBank raceQuestionBank)
    {
        return toAjax(raceQuestionBankService.updateRaceQuestionBank(raceQuestionBank));
    }

    /**
     * 删除题库
     */
    @PreAuthorize("@ss.hasPermi('race:bank:remove')")
    @Log(title = "题库", businessType = BusinessType.DELETE)
	@DeleteMapping("/{questionIds}")
    public AjaxResult remove(@PathVariable Long[] questionIds)
    {
        return toAjax(raceQuestionBankService.deleteRaceQuestionBankByQuestionIds(questionIds));
    }
}
