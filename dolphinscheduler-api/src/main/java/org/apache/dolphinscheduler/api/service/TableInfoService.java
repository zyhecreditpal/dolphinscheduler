package org.apache.dolphinscheduler.api.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.datasource.DynamicDataSourceContextHolder;
import org.apache.dolphinscheduler.dao.datasource.aop.DataSource;
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
public class TableInfoService extends BaseService{

    private static final Logger logger = LoggerFactory.getLogger(TableInfoService.class);

    @Autowired
    private TableInfoMapper tableInfoMapper;

    static Cache<String,Object> cache = CacheUtil.newFIFOCache(3);

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
    @DataSource(DataSourceType.SLAVE)
    public Map<String, Object> queryTableInfoList(User loginUser, String searchVal) {
        Map<String, Object> result = new HashMap<>(5);

        if (check(result, !isAdmin(loginUser), Status.USER_NO_OPERATION_PERM)) {
            return result;
        }

        List<TableInfoVo> scheduleList = new ArrayList<>(64);

        Object tableList = cache.get("tableList");
        if (Objects.nonNull(tableList)){
            logger.info("表格查询缓存命中");
            scheduleList = (List<TableInfoVo>) tableList;
        }else {
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
            //缓存3分钟
            cache.put("tableList",scheduleList,1000 * 60 *3);
        }
        //用于处理缓存数据被查询时影响元数据的问题
        List<TableInfoVo> newList = new ArrayList<>(16);
        CollectionUtil.addAll(newList,scheduleList);
        Iterator<TableInfoVo> iterator = newList.iterator();
        while (iterator.hasNext()) {
            TableInfoVo next = iterator.next();
            if (!StringUtils.isEmpty(searchVal) && !next.getTableName().contains(searchVal)){
                iterator.remove();
            }
        }
        result.put(Constants.DATA_LIST, newList);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    public String queryTableComment(String database,String tableName){

            DynamicDataSourceContextHolder.setDataSourceType(DataSourceType.SLAVE.name());
            String tableDDL = tableInfoMapper.queryTableComment(database, tableName);
            DynamicDataSourceContextHolder.clearDataSourceType();
            String comment = "COMMENT";
            String newStr = tableDDL.substring(tableDDL.lastIndexOf(")")+1,tableDDL.length());
            if (newStr.contains(comment)){
                String substring = newStr.substring(newStr.lastIndexOf(comment) + comment.length(), newStr.indexOf("STORED"));
                StringBuilder builder = new StringBuilder();
                builder.append(tableName);
                builder.append("(");
                builder.append(substring.replaceAll("'","").trim());
                builder.append(")");
                return builder.toString();
            }

        return tableName;
    }

    public List<Lineage> listLine(List<Lineage> list,String column){

        String[] split = null;
        for (Lineage lineage : list) {
            if ("source_table".equals(column)){
                 split = lineage.getTargetTable().split("\\.");
            }else {
                 split = lineage.getSourceTable().split("\\.");
            }

            try {
                String[] finalSplit = split;
                Future<?> future = executor.submit(() -> {
                    String newTable = queryTableComment(finalSplit[0], finalSplit[1]);
                    return newTable;
                });
                String result = (String) future.get();
                if ("source_table".equals(column)){
                 lineage.setTargetTable(result);
                }else {
                    lineage.setSourceTable(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new ServiceException("关联表查询失败");
            }
        }

    return list;
    }




    public static void main(String[] args) {
        String s = "CREATE TABLE zhibiaoxitong.user_portrait (\n" +
                "  data_dt STRING COMMENT '数据日期',\n" +
                "  cust_id STRING COMMENT '客户内码',\n" +
                "  cust_no STRING COMMENT '客户号',\n" +
                "  cust_nm STRING COMMENT '客户姓名',\n" +
                "  cert_id STRING COMMENT '证件号码',\n" +
                "  age STRING COMMENT '年龄',\n" +
                "  sex STRING COMMENT '性别',\n" +
                "  ab16bal DECIMAL(22,2) COMMENT '活期存款余额',\n" +
                "  ab22bal DECIMAL(22,2) COMMENT '定期存款余额',\n" +
                "  fms_bal DECIMAL(22,2) COMMENT '理财余额',\n" +
                "  dep_bal DECIMAL(22,2) COMMENT '总资产余额',\n" +
                "  identity_label STRING COMMENT '身份特质等级',\n" +
                "  asset_label STRING COMMENT '资产价值等级',\n" +
                "  loyalty_label STRING COMMENT '忠诚度等级',\n" +
                "  activity_label STRING COMMENT '活跃度等级',\n" +
                "  contribution_label STRING COMMENT '贡献度等级',\n" +
                "  credit_risk_label STRING COMMENT '信用风险等级',\n" +
                "  strategy_label STRING COMMENT '策略等级'\n" +
                ")\n" +
                " COMMENT '客户画像结果表'\n" +
                "STORED AS TEXTFILE\n" +
                "LOCATION 'hdfs://ns1/user/hive/warehouse/zhibiaoxitong.db/user_portrait'\n";
        String comment = "COMMENT";
        String newStr = s.substring(s.lastIndexOf(")")+1,s.length());
//        if (newStr.contains(comment)){
//            String substring = newStr.substring(newStr.lastIndexOf(comment) + comment.length(), newStr.indexOf("STORED"));
//            System.out.println(substring.replaceAll("'","").trim());
//        }
        String array = "[{\"start_dt_dds\":\"开链时间\"},\n" +
                "{\"custr_nbr\":\"申请书编号\"}]";
        JSONArray jsonArray = JSONArray.parseArray(array);
        for (Object o : jsonArray) {
            JSONObject jsonObject = (JSONObject) o;
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                System.out.println(key+"--"+value);
            }
        }
//
    }

}
