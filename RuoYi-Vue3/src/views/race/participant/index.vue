<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="参与者" prop="participant">
        <el-input
          v-model="queryParams.participant"
          placeholder="请输入参与者，与user_id绑定"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="参赛者队伍" prop="participantGroup">
        <el-input
          v-model="queryParams.participantGroup"
          placeholder="请输入参赛者队伍，0为单人，1为A队，2为B队"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="绑定房间" prop="participantRoom">
        <el-input
          v-model="queryParams.participantRoom"
          placeholder="请输入绑定房间"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['race:participant:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['race:participant:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['race:participant:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['race:participant:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="participantList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="id" align="center" prop="participantId" />
      <el-table-column label="参与者" align="center" prop="participant" />
      <el-table-column label="参赛者队伍" align="center" prop="participantGroup" />
      <el-table-column label="绑定房间" align="center" prop="participantRoom" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['race:participant:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['race:participant:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改比赛参与人员对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="参与者，与user_id绑定" prop="participant">
          <el-input v-model="form.participant" placeholder="请输入参与者，与user_id绑定" />
        </el-form-item>
        <el-form-item label="参赛者队伍，0为单人，1为A队，2为B队" prop="participantGroup">
          <el-input v-model="form.participantGroup" placeholder="请输入参赛者队伍，0为单人，1为A队，2为B队" />
        </el-form-item>
        <el-form-item label="绑定房间" prop="participantRoom">
          <el-input v-model="form.participantRoom" placeholder="请输入绑定房间" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listParticipant, getParticipant, delParticipant, addParticipant, updateParticipant } from "@/api/race/participant";

export default {
  name: "Participant",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 比赛参与人员表格数据
      participantList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        participant: null,
        participantGroup: null,
        participantRoom: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        participant: [
          { required: true, message: "参与者，与user_id绑定不能为空", trigger: "blur" }
        ],
        participantGroup: [
          { required: true, message: "参赛者队伍，0为单人，1为A队，2为B队不能为空", trigger: "blur" }
        ],
        participantRoom: [
          { required: true, message: "绑定房间不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询比赛参与人员列表 */
    getList() {
      this.loading = true;
      listParticipant(this.queryParams).then(response => {
        this.participantList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        participantId: null,
        participant: null,
        participantGroup: null,
        participantRoom: null
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.participantId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加比赛参与人员";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const participantId = row.participantId || this.ids
      getParticipant(participantId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改比赛参与人员";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.participantId != null) {
            updateParticipant(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addParticipant(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const participantIds = row.participantId || this.ids;
      this.$modal.confirm('是否确认删除比赛参与人员编号为"' + participantIds + '"的数据项？').then(function() {
        return delParticipant(participantIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('race/participant/export', {
        ...this.queryParams
      }, `participant_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
