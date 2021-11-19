package org.apache.dolphinscheduler.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TableInfoMapper;
import org.apache.dolphinscheduler.dao.vo.TableInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TableInfoService extends BaseService{

    private static final Logger logger = LoggerFactory.getLogger(TableInfoService.class);

    @Autowired
    private TableInfoMapper tableInfoMapper;

    /**
     * query table list
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param searchVal search avlue
     * @param pageSize page size
     * @return user list page
     */
    public Map<String, Object> queryTableInfoList(User loginUser, String searchVal, String database,Integer pageNo, Integer pageSize) {
        Map<String, Object> result = new HashMap<>(5);

        if (check(result, !isAdmin(loginUser), Status.USER_NO_OPERATION_PERM)) {
            return result;
        }

        Page<User> page = new Page(pageNo, pageSize);

        IPage<TableInfoVo> scheduleList = tableInfoMapper.queryTablePaging(page, searchVal,database);

        PageInfo<TableInfoVo> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotalCount((int)scheduleList.getTotal());
        pageInfo.setLists(scheduleList.getRecords());
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

}
