package com.ruoyi.race.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 题库对象 race_question_bank
 * 
 * @author kjleo
 * @date 2022-12-03
 */
public class RaceQuestionBank extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long questionId;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String question;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String a;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String b;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String c;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String d;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String answer;

    public void setQuestionId(Long questionId) 
    {
        this.questionId = questionId;
    }

    public Long getQuestionId() 
    {
        return questionId;
    }
    public void setQuestion(String question) 
    {
        this.question = question;
    }

    public String getQuestion() 
    {
        return question;
    }
    public void setA(String a) 
    {
        this.a = a;
    }

    public String getA() 
    {
        return a;
    }
    public void setB(String b) 
    {
        this.b = b;
    }

    public String getB() 
    {
        return b;
    }
    public void setC(String c) 
    {
        this.c = c;
    }

    public String getC() 
    {
        return c;
    }
    public void setD(String d) 
    {
        this.d = d;
    }

    public String getD() 
    {
        return d;
    }
    public void setAnswer(String answer) 
    {
        this.answer = answer;
    }

    public String getAnswer() 
    {
        return answer;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("questionId", getQuestionId())
            .append("question", getQuestion())
            .append("a", getA())
            .append("b", getB())
            .append("c", getC())
            .append("d", getD())
            .append("answer", getAnswer())
            .toString();
    }
}
