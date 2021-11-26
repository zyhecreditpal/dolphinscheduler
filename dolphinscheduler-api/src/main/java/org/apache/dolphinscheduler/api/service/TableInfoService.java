package org.apache.dolphinscheduler.api.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.datasource.DynamicDataSourceContextHolder;
import org.apache.dolphinscheduler.dao.datasource.aop.DataSourceType;
import org.apache.dolphinscheduler.dao.entity.Lineage;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TableInfoMapper;
import org.apache.dolphinscheduler.dao.vo.TableInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class TableInfoService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(TableInfoService.class);

    @Autowired
    private TableInfoMapper tableInfoMapper;

    static Cache<String, Object> cache = CacheUtil.newFIFOCache(3);

    static java.util.concurrent.ExecutorService executor;

    static {
        executor = ExecutorBuilder.create()
                .setCorePoolSize(20)
                .setMaxPoolSize(25)
                .setWorkQueue(new LinkedBlockingQueue<>(100))
                .build();
    }

    /**
     * query table list
     *
     * @param loginUser login user
     * @param searchVal search avlue
     * @return user list page
     */
    public Map<String, Object> queryTableInfoList(User loginUser, String searchVal) {
        Map<String, Object> result = new HashMap<>(5);

        if (check(result, !isAdmin(loginUser), Status.USER_NO_OPERATION_PERM)) {
            return result;
        }

        List<TableInfoVo> scheduleList = new ArrayList<>(64);

        Object tableList = cache.get("tableList");
        if (Objects.nonNull(tableList)) {
            logger.info("表格查询缓存命中");
            scheduleList = (List<TableInfoVo>) tableList;
        } else {
            DynamicDataSourceContextHolder.setDataSourceType(DataSourceType.SLAVE.name());
            List<Map<String, String>> mapList = tableInfoMapper.queryDatabases();

            //查询所有表格
            if (!CollectionUtils.isEmpty(mapList)) {
                for (Map<String, String> map : mapList) {
                    String database = map.get("name");
                    List<String> tables = tableInfoMapper.queryTables(database);
                    if (!CollectionUtils.isEmpty(tables)) {
                        for (String table : tables) {
                            TableInfoVo vo = new TableInfoVo();
                            vo.setTableName(table);
                            scheduleList.add(vo);
                        }
                    }
                }
            }
            DynamicDataSourceContextHolder.clearDataSourceType();
            //缓存3分钟
            cache.put("tableList", scheduleList, 1000 * 60 * 3);
        }
        //用于处理缓存数据被查询时影响元数据的问题
        List<TableInfoVo> newList = new ArrayList<>(16);
        CollectionUtil.addAll(newList, scheduleList);
        Iterator<TableInfoVo> iterator = newList.iterator();
        while (iterator.hasNext()) {
            TableInfoVo next = iterator.next();
            if (!StringUtils.isEmpty(searchVal) && !next.getTableName().contains(searchVal)) {
                iterator.remove();
            }
        }
        result.put(Constants.DATA_LIST, newList);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    public String queryTableComment(String database, String tableName) {

        DynamicDataSourceContextHolder.setDataSourceType(DataSourceType.SLAVE.name());
        String tableDDL = tableInfoMapper.queryTableComment(database, tableName);
        DynamicDataSourceContextHolder.clearDataSourceType();
        String comment = "COMMENT";
        String newStr = tableDDL.substring(tableDDL.lastIndexOf(")") + 1, tableDDL.length());
        if (newStr.contains(comment)) {
            String substring = newStr.substring(newStr.lastIndexOf(comment) + comment.length(), newStr.indexOf("STORED"));
            StringBuilder builder = new StringBuilder();
            builder.append(tableName);
            builder.append("(");
            builder.append(substring.replaceAll("'", "").trim());
            builder.append(")");
            return builder.toString();
        }

        return tableName;
    }

    public List<Lineage> listLine(List<Lineage> list, String column) {

        String[] split = null;
        for (Lineage lineage : list) {
            if ("source_table".equals(column)) {
                split = lineage.getTargetTable().split("\\.");
            } else {
                split = lineage.getSourceTable().split("\\.");
            }

            try {
                String[] finalSplit = split;
                Future<?> future = executor.submit(() -> {
                    String newTable = queryTableComment(finalSplit[0], finalSplit[1]);
                    return newTable;
                });
                String result = (String) future.get();
                if ("source_table".equals(column)) {
                    lineage.setTargetTable(result);
                } else {
                    lineage.setSourceTable(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new ServiceException("关联表查询失败");
            }
        }

        return list;
    }

}
