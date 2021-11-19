/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.api.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TableInfoService;
import org.apache.dolphinscheduler.api.service.TableRelationService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

import static org.apache.dolphinscheduler.api.enums.Status.*;


/**
 * tableInfo controller
 */
@Api(tags = "TABLEINFO_TAG", position = 14)
@RestController
@RequestMapping("/tables")
public class TableInfoController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(TableInfoController.class);

    @Autowired
    private TableRelationService tableRelationService;
    @Autowired
    private TableInfoService tableInfoService;

    /**
     * query user list paging
     *
     * @param loginUser login user
     * @param pageNo    page number
     * @param searchVal search avlue
     * @param pageSize  page size
     * @return user list page
     */
    @ApiOperation(value = "queryTableList")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", type = "String"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", type = "String"),
            @ApiImplicitParam(name = "database", value = "DATABASE", type = "String")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TABLE_LIST_PAGING_ERROR)
    public Result queryTableList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam("pageNo") Integer pageNo,
                                 @RequestParam(value = "searchVal", required = false) String searchVal,
                                 @RequestParam(value = "database", required = true) String database,
                                 @RequestParam("pageSize") Integer pageSize) {
        logger.info("login user {}, list table paging, pageNo: {}, searchVal: {}, pageSize: {}",
                loginUser.getUserName(), pageNo, searchVal, pageSize);
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = tableInfoService.queryTableInfoList(loginUser, searchVal, database,pageNo, pageSize);
        return returnDataListPaging(result);
    }


    @GetMapping(value = "/list-relation")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TABLE_RELATION_ERROR)
    public Result listRelation(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser
                            ,@RequestParam(value = "tableName", required = true) String tableName) {
        logger.info("login user {}, table relation list tableName {}", loginUser.getUserName(),tableName);
        Map<String, Object> result = tableRelationService.queryRelationList(loginUser,tableName);
        return returnDataList(result);
    }

}
