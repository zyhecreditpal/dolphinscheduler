package org.apache.dolphinscheduler.dao.vo;

import org.apache.dolphinscheduler.dao.entity.Lineage;

import java.util.List;

public class TableTreeInfoVo {
    private String label;

    private List<TableTreeInfoVo> children;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<TableTreeInfoVo> getChildren() {
        return children;
    }

    public void setChildren(List<TableTreeInfoVo> children) {
        this.children = children;
    }
}
