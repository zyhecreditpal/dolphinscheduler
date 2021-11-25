package org.apache.dolphinscheduler.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.dao.datasource.aop.DataSource;
import org.apache.dolphinscheduler.dao.datasource.aop.DataSourceType;
import org.apache.dolphinscheduler.dao.vo.TableInfoVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TableInfoMapper extends BaseMapper {

    List<String> queryTables(@Param("database") String database);

    List<Map<String,String>> queryDatabases();

    String queryTableComment(@Param("database") String database,@Param("tableName") String tableName);

}
