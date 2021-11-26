package org.apache.dolphinscheduler.api.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
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
    @Autowired
    private TableInfoService tableInfoService;

    /**
     * query tableRelation list
     *
     * @param loginUser login user
     * @return tableRelation list
     */
    public Map<String, Object> queryRelationList(User loginUser, String tableName) {
        Map<String, Object> result = new HashMap<>(5);
        Map<String, Object> data = new HashMap<>(5);
        List<Lineage> excelLineage = new ArrayList<>(16);
        //右树
        TableTreeInfoVo rely = new TableTreeInfoVo();
        //左树
        TableTreeInfoVo reliedOn = new TableTreeInfoVo();

        queryRelyTree("target_table", tableName, rely, excelLineage);
        queryRelyTree("source_table", tableName, reliedOn, excelLineage);

        Map<String, String> sourceMap = new HashMap<>();
        Map<String, String> targetMap = new HashMap<>();

        List<Map<String, String>> sourceExcelMap = new ArrayList<>();
        List<Map<String, String>> targetExcelMap = new ArrayList<>();
        if (!CollectionUtils.isEmpty(excelLineage)) {
            for (Lineage lineage : excelLineage) {
                sourceMap.put(lineage.getSourceTable(), lineage.getSourceVertex());
                targetMap.put(lineage.getTargetTable(), lineage.getTargetVertex());
            }

        }

        //excel 字段处理
        if (!CollectionUtils.isEmpty(sourceMap)) {
            for (Map.Entry<String, String> entry : sourceMap.entrySet()) {
                String sourceTableName = entry.getKey();
                String sourceTableFields = entry.getValue();
                if (StringUtils.hasText(sourceTableFields)) {
                    JSONArray jsonArray = JSONArray.parseArray(sourceTableFields);
                    for (Object o : jsonArray) {
                        JSONObject jsonObject = (JSONObject) o;
                        for (Map.Entry<String, Object> field : jsonObject.entrySet()) {
                            String key = field.getKey();
                            String value = (String) field.getValue();
                            Map<String, String> source = new HashMap<>();
                            source.put("sourceTable", sourceTableName);
                            if (StringUtils.hasText(value)) {
                                source.put("sourceTableField", key + "(" + value + ")");
                            } else {
                                source.put("sourceTableField", key);
                            }
                            sourceExcelMap.add(source);
                        }
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(targetMap)) {
            for (Map.Entry<String, String> entry : targetMap.entrySet()) {
                String targetTableName = entry.getKey();
                String targetTableFields = entry.getValue();
                if (StringUtils.hasText(targetTableFields)) {
                    JSONArray jsonArray = JSONArray.parseArray(targetTableFields);
                    for (Object o : jsonArray) {
                        JSONObject jsonObject = (JSONObject) o;
                        for (Map.Entry<String, Object> field : jsonObject.entrySet()) {
                            String key = field.getKey();
                            String value = (String) field.getValue();
                            Map<String, String> source = new HashMap<>();
                            source.put("targetTable", targetTableName);
                            if (StringUtils.hasText(value)) {
                                source.put("targetTableField", key + "(" + value + ")");
                            } else {
                                source.put("targetTableField", key);
                            }
                            targetExcelMap.add(source);
                        }
                    }
                }
            }
        }

        data.put("sourceExcel", sourceExcelMap);
        data.put("targetExcel", targetExcelMap);
        data.put("right", rely);
        data.put("left", reliedOn);
        result.put(Constants.DATA_LIST, data);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private TableTreeInfoVo queryRelyTree(String column, String tableName, TableTreeInfoVo vo, List<Lineage> excelLineage) {
        List<Lineage> lineageList = tableRelationMapper.queryTableRelationList(column, tableName);
        if (!CollectionUtils.isEmpty(lineageList)) {

            //查询表 comment 并重新命名
            tableInfoService.listLine(lineageList, column);
            List<TableTreeInfoVo> next = new ArrayList<>(lineageList.size());
            for (Lineage lineage : lineageList) {
                TableTreeInfoVo infoVo = new TableTreeInfoVo();
                if ("source_table".equals(column)) {
                    infoVo.setLabel(lineage.getTargetTable());
                } else {
                    infoVo.setLabel(lineage.getSourceTable());
                }
                next.add(infoVo);
            }
            vo.setChildren(next);
            excelLineage.addAll(lineageList);
        }
        vo.setLabel(tableName);
        return vo;
    }
}
