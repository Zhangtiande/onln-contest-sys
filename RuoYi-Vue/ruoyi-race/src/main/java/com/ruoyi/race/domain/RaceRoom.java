package com.ruoyi.race.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 竞赛房间对象 race_room
 * 
 * @author kjleo
 * @date 2022-12-02
 */
public class RaceRoom extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 房间id */
    private Long roomId;

    /** 裁判 */
    @Excel(name = "裁判")
    private Long roomJudge;

    /** 房间类型，0为单人，1为组队 */
    @Excel(name = "房间类型，0为单人，1为组队")
    private Long roomType;

    /** 是否结束 */
    @Excel(name = "是否结束")
    private Integer status;

    public void setRoomId(Long roomId) 
    {
        this.roomId = roomId;
    }

    public Long getRoomId() 
    {
        return roomId;
    }
    public void setRoomJudge(Long roomJudge) 
    {
        this.roomJudge = roomJudge;
    }

    public Long getRoomJudge() 
    {
        return roomJudge;
    }
    public void setRoomType(Long roomType) 
    {
        this.roomType = roomType;
    }

    public Long getRoomType() 
    {
        return roomType;
    }
    public void setStatus(Integer status) 
    {
        this.status = status;
    }

    public Integer getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("roomId", getRoomId())
            .append("roomJudge", getRoomJudge())
            .append("roomType", getRoomType())
            .append("createTime", getCreateTime())
            .append("status", getStatus())
            .toString();
    }
}
