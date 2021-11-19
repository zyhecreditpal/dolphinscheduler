package org.apache.dolphinscheduler.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.dao.vo.TableInfoVo;
import org.apache.ibatis.annotations.Param;

public interface TableInfoMapper extends BaseMapper {

    /**
     * user page
     * @param page page
     * @param tableName userName
     * @return user IPage
     */
    IPage<TableInfoVo> queryTablePaging(Page page,
                                        @Param("tableName") String tableName,@Param("database") String database);
}
