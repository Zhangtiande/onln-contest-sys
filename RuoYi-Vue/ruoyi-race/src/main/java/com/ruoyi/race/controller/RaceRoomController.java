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
import com.ruoyi.race.domain.RaceRoom;
import com.ruoyi.race.service.IRaceRoomService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 竞赛房间Controller
 * 
 * @author kjleo
 * @date 2022-12-02
 */
@RestController
@RequestMapping("/race/room")
public class RaceRoomController extends BaseController
{
    @Autowired
    private IRaceRoomService raceRoomService;

    /**
     * 查询竞赛房间列表
     */
    @PreAuthorize("@ss.hasPermi('race:room:list')")
    @GetMapping("/list")
    public TableDataInfo list(RaceRoom raceRoom)
    {
        startPage();
        List<RaceRoom> list = raceRoomService.selectRaceRoomList(raceRoom);
        return getDataTable(list);
    }

    /**
     * 导出竞赛房间列表
     */
    @PreAuthorize("@ss.hasPermi('race:room:export')")
    @Log(title = "竞赛房间", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RaceRoom raceRoom)
    {
        List<RaceRoom> list = raceRoomService.selectRaceRoomList(raceRoom);
        ExcelUtil<RaceRoom> util = new ExcelUtil<RaceRoom>(RaceRoom.class);
        util.exportExcel(response, list, "竞赛房间数据");
    }

    /**
     * 获取竞赛房间详细信息
     */
    @PreAuthorize("@ss.hasPermi('race:room:query')")
    @GetMapping(value = "/{roomId}")
    public AjaxResult getInfo(@PathVariable("roomId") Long roomId)
    {
        return success(raceRoomService.selectRaceRoomByRoomId(roomId));
    }

    /**
     * 新增竞赛房间
     */
    @PreAuthorize("@ss.hasPermi('race:room:add')")
    @Log(title = "竞赛房间", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RaceRoom raceRoom)
    {
        return toAjax(raceRoomService.insertRaceRoom(raceRoom));
    }

    /**
     * 修改竞赛房间
     */
    @PreAuthorize("@ss.hasPermi('race:room:edit')")
    @Log(title = "竞赛房间", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RaceRoom raceRoom)
    {
        return toAjax(raceRoomService.updateRaceRoom(raceRoom));
    }

    /**
     * 删除竞赛房间
     */
    @PreAuthorize("@ss.hasPermi('race:room:remove')")
    @Log(title = "竞赛房间", businessType = BusinessType.DELETE)
	@DeleteMapping("/{roomIds}")
    public AjaxResult remove(@PathVariable Long[] roomIds)
    {
        return toAjax(raceRoomService.deleteRaceRoomByRoomIds(roomIds));
    }
}
