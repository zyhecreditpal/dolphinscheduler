package org.apache.dolphinscheduler.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.dolphinscheduler.dao.entity.Lineage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zyh
 * @since 2021-11-19
 */
public interface TableRelationMapper extends BaseMapper<Lineage> {

    List<Lineage> queryTableRelationList(@Param("column") String column, @Param("tableName") String tableName);

}
