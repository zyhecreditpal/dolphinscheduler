package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.Lineage;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TableRelationMapper;
import org.apache.dolphinscheduler.dao.vo.TableTreeInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zyh
 * @since 2021-11-19
 */
@Service
public class TableRelationService extends BaseService {

    private Logger logger = LoggerFactory.getLogger(TableRelationService.class);
    @Autowired
    private TableRelationMapper tableRelationMapper;

    /**
     * query tableRelation list
     *
     * @param loginUser login user
     * @return tableRelation list
     */
    public Map<String, Object> queryRelationList(User loginUser,String tableName) {
        Map<String, Object> result = new HashMap<>(5);
        Map<String,Object> data = new HashMap<>(5);
        //右树
        TableTreeInfoVo rely = new TableTreeInfoVo();
        //左树
        TableTreeInfoVo reliedOn = new TableTreeInfoVo();

        queryRelyTree("target_table",tableName,rely);
        queryRelyTree("source_table",tableName,reliedOn);

        data.put("right",rely);
        data.put("left",reliedOn);
        result.put(Constants.DATA_LIST, data);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private TableTreeInfoVo queryRelyTree(String column, String tableName, TableTreeInfoVo vo){
        List<Lineage> lineageList = tableRelationMapper.queryTableRelationList(column,tableName);
        if (!CollectionUtils.isEmpty(lineageList)){
            List<TableTreeInfoVo> next = new ArrayList<>(lineageList.size());
            for (Lineage lineage : lineageList) {
                TableTreeInfoVo infoVo = new TableTreeInfoVo();
                if ("source_table".equals(column)){
                    infoVo.setLabel(lineage.getTargetTable());
                }else {
                    infoVo.setLabel(lineage.getSourceTable());
                }
                next.add(infoVo);
            }
            vo.setChildren(next);
        }
        vo.setLabel(tableName);
        return vo;
    }
}
