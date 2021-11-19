package org.apache.dolphinscheduler.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mysql.jdbc.StringUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * <p>
 * 
 * </p>
 *
 * @author zyh
 * @since 2021-11-19
 */
@TableName("lineage")
public class Lineage implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 表名
     */
    @TableField("source_table")
    private String sourceTable;

    /**
     * 表的描述
     */
    @TableField("source_vertex")
    private byte[] sourceVertex;

    /**
     * 关联表名
     */
    @TableField("target_table")
    private String targetTable;

    /**
     * 关联表描述
     */
    @TableField("target_vertex")
    private byte[] targetVertex;

    @TableField("create_time")
    private Integer createTime;

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public String getSourceVertex() {
        try {
            return StringUtils.toString(sourceVertex,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setSourceVertex(byte[] sourceVertex) {
        this.sourceVertex = sourceVertex;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getTargetVertex() {
        try {
            return StringUtils.toString(targetVertex,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setTargetVertex(byte[] targetVertex) {
        this.targetVertex = targetVertex;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }
}
