package com.ruoyi.race.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 比赛参与人员对象 race_participant
 * 
 * @author kjleo
 * @date 2022-12-02
 */
public class RaceParticipant extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long participantId;

    /** 参与者，与user_id绑定 */
    @Excel(name = "参与者，与user_id绑定")
    private Long participant;

    /** 参赛者队伍，0为单人，1为A队，2为B队 */
    @Excel(name = "参赛者队伍，0为单人，1为A队，2为B队")
    private Long participantGroup;

    /** 绑定房间 */
    @Excel(name = "绑定房间")
    private Long participantRoom;

    public void setParticipantId(Long participantId) 
    {
        this.participantId = participantId;
    }

    public Long getParticipantId() 
    {
        return participantId;
    }
    public void setParticipant(Long participant) 
    {
        this.participant = participant;
    }

    public Long getParticipant() 
    {
        return participant;
    }
    public void setParticipantGroup(Long participantGroup) 
    {
        this.participantGroup = participantGroup;
    }

    public Long getParticipantGroup() 
    {
        return participantGroup;
    }
    public void setParticipantRoom(Long participantRoom) 
    {
        this.participantRoom = participantRoom;
    }

    public Long getParticipantRoom() 
    {
        return participantRoom;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("participantId", getParticipantId())
            .append("participant", getParticipant())
            .append("participantGroup", getParticipantGroup())
            .append("participantRoom", getParticipantRoom())
            .toString();
    }
}
