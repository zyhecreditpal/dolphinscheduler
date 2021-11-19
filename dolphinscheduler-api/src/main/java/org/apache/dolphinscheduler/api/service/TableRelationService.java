package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.Lineage;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TableRelationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        List<Lineage> list = tableRelationMapper.queryTableRelationList(tableName);
        result.put(Constants.DATA_LIST, list);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
