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


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TableInfoService;
import org.apache.dolphinscheduler.api.service.TableRelationService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.Lineage;
import org.apache.dolphinscheduler.dao.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     * @param searchVal search avlue
     * @return user list page
     */
    @ApiOperation(value = "queryTableList")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", type = "String")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TABLE_LIST_PAGING_ERROR)
    public Result queryTableList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "searchVal", required = false) String searchVal) {
        logger.info("login user {}, list table  searchVal: {}",
                loginUser.getUserName(), searchVal);
        Map<String, Object> result = new HashMap<>(4);
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = tableInfoService.queryTableInfoList(loginUser, searchVal);
        return returnDataList(result);
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

    @GetMapping(value = "/excel")
//    @PostMapping(value = "/excel")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DOWNEXCEL_ERROR)
    public void downExcel(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser, HttpServletResponse response,
//                          @RequestBody JSONObject data) throws IOException {
                          @RequestParam("tableName") String tableName) throws IOException {


//        List<Map<String,String>> sourceExcelMap = data.getObject("sourceExcel",List.class);
//        List<Map<String,String>> targetExcelMap = data.getObject("targetExcel",List.class);

        Map<String, Object> result = tableRelationService.queryRelationList(loginUser,tableName);
        List<Map<String,String>> sourceExcelMap = (List<Map<String, String>>) ((HashMap)result.get("data")).get("sourceExcel");
        List<Map<String,String>> targetExcelMap = (List<Map<String, String>>) ((HashMap)result.get("data")).get("targetExcel");

        int length = sourceExcelMap.size()>=targetExcelMap.size()?sourceExcelMap.size():targetExcelMap.size();
//
        List<Lineage> downList = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            Lineage lineage = new Lineage();
            if (i < sourceExcelMap.size()){
                lineage.setSourceTable(sourceExcelMap.get(i).get("sourceTable"));
                lineage.setSourceVertex(sourceExcelMap.get(i).get("sourceTableField").getBytes(StandardCharsets.UTF_8));
            }else {
                lineage.setSourceTable("");
                lineage.setSourceVertex("".getBytes(StandardCharsets.UTF_8));
            }

            if (i < targetExcelMap.size()){
                lineage.setTargetTable(targetExcelMap.get(i).get("targetTable"));
                lineage.setTargetVertex(targetExcelMap.get(i).get("targetTableField").getBytes(StandardCharsets.UTF_8));
            }else {
                lineage.setTargetTable("");
                lineage.setTargetVertex("".getBytes(StandardCharsets.UTF_8));
            }
            downList.add(lineage);
        }

        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(UUID.fastUUID().toString(true), "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        ExcelWriter writer = ExcelUtil.getWriter();
        ServletOutputStream outputStream = response.getOutputStream();

        writer.addHeaderAlias("sourceTable", "来源表");
        writer.addHeaderAlias("sourceVertex", "来源字段");
        writer.addHeaderAlias("targetTable", "目标表");
        writer.addHeaderAlias("targetVertex", "目标字段");

        writer.setOnlyAlias(true);
        writer.write(downList,true);
        writer.flush(outputStream,true);
        writer.close();
        IoUtil.close(outputStream);
    }

}
