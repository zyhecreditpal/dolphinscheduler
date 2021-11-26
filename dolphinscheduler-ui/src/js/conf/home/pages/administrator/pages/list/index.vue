/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
<template>
  <m-list-construction title="管理员">
    <template slot="content">
      <div class="perject-home-content">
        <div class="row" style="height:100%;">
          <div class="col-md-3 card">
            <div class="chart-title" style="text-align:left">
              <span>血缘关系</span>
            </div>
            <m-process-state-count>
              <ul class="table-ul">
                <li>
                  <x-input clearable placeholder="请输入内容" @keypress.13.native="getList" v-model="searchParams.searchVal">
                  </x-input>
                </li>
                <li class="table-li" v-for="item in totalList" :key="item" @click="getItemtree(item.tableName)">
                  {{item.tableName}}</li>
                <!-- 页脚 -->
                <!-- <div class="page-box" style="padding:10px;" v-if="totalList">
                  <x-page show-total small :current="parseInt(searchParams.pageNo)" :total="total"
                    :page-size="searchParams.pageSize" @on-change="_page" show-sizer :page-size-options="[10,20,30,50]"
                    @on-size-change="_pageSize"></x-page>
                </div> -->
              </ul>
            </m-process-state-count>
          </div>
          <div class="col-md-6" style="padding:10px;">
            <div class="chart-title">
              <span>关系流程图</span>
            </div>
            <div class="box" v-if="tree">
              <vue-okr-tree show-collapsable default-expand-all animate @node-click='gettableList'
                :left-data="testLeftData" only-both-tree direction="horizontal" :data="testData"
                :label-class-name="renderLabelClass" :current-lable-class-name="renderCurrentClass"></vue-okr-tree>
            </div>
          </div>
          <div class="col-md-3 card">
            <div class="chart-title" style="display: flex; justify-content: center; align-items: center;">
              <span v-if="val">统计表({{this.val}})</span>
              <span v-else>统计表</span>
              <x-button style="margin-left:5px" v-if="tableData.length>0" data-toggle="tooltip" shape="circle"
                size="xsmall" type="info" @click="_export" :title="$t('Download')" icon="ans-icon-download">
              </x-button>
            </div>
            <m-define-user-count>
              <x-table height="650" border :data="tableData" v-if="tree&&tableData.length>0">
                <x-table-column v-for="(header, index) in tableHeaders" :key="index" :label="header.label"
                  :prop="header.prop">
                </x-table-column>
              </x-table>
            </m-define-user-count>
          </div>
        </div>
      </div>
      <m-spin :is-spin="isLoading" :is-left="false">
      </m-spin>
    </template>
  </m-list-construction>
</template>
<script>
  import {
    mapActions
  } from 'vuex'
  import {
    VueOkrTree
  } from 'vue-okr-tree';
  import 'vue-okr-tree/dist/vue-okr-tree.css'
  import switchProject from '@/module/mixin/switchProject'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'projects-index-index',
    data() {
      return {
        tree: false,
        isLoading: true,
        x: "",
        y: "",
        testLeftData: [], //左图表
        testData: [], //右图表
        tableData: [], //右列表
        totalList: [], //左列表
        rowCount: 5,
        tableHeaders: [{
            label: '来源表',
            prop: 'sourceTable'
          },
          {
            label: '来源字段',
            prop: 'sourceTableField'
          },
          {
            label: '影响表',
            prop: 'targetTable'
          },
          {
            label: '影响字段',
            prop: 'targetTableField'
          }
        ],
        searchParams: {
          // pageSize: 10,
          // pageNo: 1,
          searchVal: '',
          // database: 'dolphinscheduler'
        },
        val: ''
      }
    },
    mounted() {
      //拖拽
      // let box = document.querySelector('.box')
      // let move = JSON.parse(window.localStorage.getItem("move"))
      // box.style.left = move.x
      // box.style.top = move.y
    },
    //拖拽
    // directives: {
    //   wahaha(el, binding) {
    //     el.onmousedown = function (ev) {
    //       var x = ev.clientX - el.offsetLeft;
    //       var y = ev.clientY - el.offsetTop;
    //       el.onmousemove = function (ev) {
    //         window.localStorage.removeItem("move")
    //         this.x = ev.clientX - x + 'px';
    //         this.y = ev.clientY - y + "px";
    //         el.style.left = this.x
    //         el.style.top = this.y
    //         window.localStorage.setItem("move", JSON.stringify({
    //           x: this.x,
    //           y: this.y
    //         }))
    //       }
    //       // window.localStorage.setItem("move",this.x)  拿不到
    //       el.onmouseup = function (ev) {
    //         el.onmousemove = el.onmouseup = null;
    //       }
    //       return false;
    //     }
    //   }
    // },
    mixins: [switchProject],
    methods: {
      ...mapActions('resource', ['gettables', 'getrelation', 'exportDefinition']),
      getList(flag) {
        this.isLoading = !flag
        this.gettables(this.searchParams).then(res => {
          this.totalList = res;
          this.isLoading = false
          // if (this.searchParams.pageNo > 1 && res.totalList.length == 0) {
          //   this.searchParams.pageNo = this.searchParams.pageNo - 1
          // } else {
          //   this.totalList = res.data;
          //   this.total = res.total
          //   this.isLoading = false
          // }
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.isLoading = false
        })
      },
      gettableList(val) {
        this.getrelation({
          tableName: val.label
        }).then(res => {
          this.tree = false;
          this.testLeftData = []
          this.testData = []
          this.val = ''
          setTimeout(() => {
            this.tree = true;
            this.val = val.label
            this.testLeftData.push(res.left)
            this.testData.push(res.right)
            if (res.targetExcel.length > res.sourceExcel.length) {
              this.tableData = res.targetExcel.map((item, index) => {
                return {
                  ...item,
                  ...res.sourceExcel[index]
                };
              });
            } else if (res.targetExcel.length <= res.sourceExcel.length) {
              this.tableData = res.sourceExcel.map((item, index) => {
                return {
                  ...item,
                  ...res.targetExcel[index]
                };
              });
            }
          }, 300);
        }).catch(e => {
          this.tree = false;
          this.testLeftData = []
          this.testData = []
          this.$message.error(e.msg || '')
        })
      },
      getItemtree(val) {
        this.tree = false;
        this.testLeftData = []
        this.testData = []
        this.tableData = []
        this.val = ''
        this.getrelation({
          tableName: val
        }).then(res => {
          this.tree = true;
          this.testLeftData.push(res.left)
          this.testData.push(res.right)
          console.log(this.testLeftData, this.testData);

        }).catch(e => {
          this.tree = false;
          this.testLeftData = []
          this.testData = []
          this.$message.error(e.msg || '')
        })
      },
      _export() {
        let jsonStr = JSON.stringify(this.tableData);
        this.exportDefinition({
          data: jsonStr,
          fileName: this.val
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      _page(val) {
        this.searchParams.pageNo = val;
        this.getList();
      },
      _pageSize(val) {
        this.searchParams.pageSize = val;
        this.getList();
      },
      renderLabelClass(node) {
        return 'label-class-blue'
      },
      renderCurrentClass(node) {
        return 'label-bg-blue'
      }
    },
    created() {
      this.getList();
    },
    components: {
      VueOkrTree,
      mSecondaryMenu,
      mListConstruction,
    }
  }

</script>

<style lang="scss" rel="stylesheet/scss">
  .perject-home-content {
    padding: 10px 20px;
    position: relative;

    .box {
      max-height: 650px;
      max-width: 915px;
      overflow-y: auto;
      overflow-x: auto;
      // position: absolute;
      // top: 100px;
      // left: 100px;
      // background: red;
    }

    .time-model {
      position: absolute;
      right: 8px;
      top: -40px;

      .ans-input {
        >input {
          width: 344px;
        }
      }
    }

    .table-ul {
      height: 650px;
      overflow-y: auto;

      .table-li {
        height: 45px;
        font-size: 18px;
        line-height: 45px;
        padding-bottom: 5px;
        border-bottom: 1px solid #e4eaf1;
        text-overflow: ellipsis;
        overflow: hidden;
        white-space: nowrap;

        &:hover {
          cursor: pointer;
          opacity: 0.8;
          background: #e4eaf1;
        }
      }
    }

    .chart-title {
      text-align: center;
      height: 60px;
      line-height: 60px;

      span {
        font-size: 22px;
        color: #333;
        font-weight: bold;
      }
    }

    .card {
      height: 100%;
      // box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2);
      transition: 0.3s;
      padding: 10px;
      border-radius: 5px;
    }

    // .card:hover {
    //   box-shadow: 0 8px 16px 0 rgba(0, 0, 0, 0.2);
    // }

    .label-class-blue {
      color: #1989fa;
    }

    .label-bg-blue {
      background: #1989fa;
      color: #fff;
    }
  }

</style>
