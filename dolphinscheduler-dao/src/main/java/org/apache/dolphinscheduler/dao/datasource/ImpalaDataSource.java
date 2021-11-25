package org.apache.dolphinscheduler.dao.datasource;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;

public class ImpalaDataSource extends BaseDataSource{
    @Override
    public String driverClassSelector() {
        return Constants.COM_IMPALA_JDBC_DRIVER;
    }

    @Override
    public DbType dbTypeSelector() {
        return DbType.IMPALA;
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:impala://10.10.10.38:21050/zhibiaoxitong;AuthMech=3";
    }

    @Override
    public String getUser() {
        return "zhibiaoxitonguser";
    }

    @Override
    public String getPassword() {
        return "D1TAh@zw30Yb^CfD";
    }
}
