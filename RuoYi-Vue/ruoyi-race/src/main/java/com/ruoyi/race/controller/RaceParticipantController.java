package com.ruoyi.race.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.race.domain.RaceParticipant;
import com.ruoyi.race.service.IRaceParticipantService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 比赛参与人员Controller
 * 
 * @author kjleo
 * @date 2022-12-02
 */
@RestController
@RequestMapping("/race/participant")
public class RaceParticipantController extends BaseController
{
    @Autowired
    private IRaceParticipantService raceParticipantService;

    /**
     * 查询比赛参与人员列表
     */
    @PreAuthorize("@ss.hasPermi('race:participant:list')")
    @GetMapping("/list")
    public TableDataInfo list(RaceParticipant raceParticipant)
    {
        startPage();
        List<RaceParticipant> list = raceParticipantService.selectRaceParticipantList(raceParticipant);
        return getDataTable(list);
    }

    /**
     * 导出比赛参与人员列表
     */
    @PreAuthorize("@ss.hasPermi('race:participant:export')")
    @Log(title = "比赛参与人员", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RaceParticipant raceParticipant)
    {
        List<RaceParticipant> list = raceParticipantService.selectRaceParticipantList(raceParticipant);
        ExcelUtil<RaceParticipant> util = new ExcelUtil<RaceParticipant>(RaceParticipant.class);
        util.exportExcel(response, list, "比赛参与人员数据");
    }

    /**
     * 获取比赛参与人员详细信息
     */
    @PreAuthorize("@ss.hasPermi('race:participant:query')")
    @GetMapping(value = "/{participantId}")
    public AjaxResult getInfo(@PathVariable("participantId") Long participantId)
    {
        return success(raceParticipantService.selectRaceParticipantByParticipantId(participantId));
    }

    /**
     * 新增比赛参与人员
     */
    @PreAuthorize("@ss.hasPermi('race:participant:add')")
    @Log(title = "比赛参与人员", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RaceParticipant raceParticipant)
    {
        return toAjax(raceParticipantService.insertRaceParticipant(raceParticipant));
    }

    /**
     * 修改比赛参与人员
     */
    @PreAuthorize("@ss.hasPermi('race:participant:edit')")
    @Log(title = "比赛参与人员", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RaceParticipant raceParticipant)
    {
        return toAjax(raceParticipantService.updateRaceParticipant(raceParticipant));
    }

    /**
     * 删除比赛参与人员
     */
    @PreAuthorize("@ss.hasPermi('race:participant:remove')")
    @Log(title = "比赛参与人员", businessType = BusinessType.DELETE)
	@DeleteMapping("/{participantIds}")
    public AjaxResult remove(@PathVariable Long[] participantIds)
    {
        return toAjax(raceParticipantService.deleteRaceParticipantByParticipantIds(participantIds));
    }
}
