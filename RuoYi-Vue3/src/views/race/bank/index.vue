<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="题目" prop="question">
        <el-input
          v-model="queryParams.question"
          placeholder="请输入题目"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="选项A" prop="a">
        <el-input
          v-model="queryParams.a"
          placeholder="请输入选项A"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="选项B" prop="b">
        <el-input
          v-model="queryParams.b"
          placeholder="请输入选项B"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="选项C" prop="c">
        <el-input
          v-model="queryParams.c"
          placeholder="请输入选项C"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="选项D" prop="d">
        <el-input
          v-model="queryParams.d"
          placeholder="请输入选项D"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="答案" prop="answer">
        <el-input
          v-model="queryParams.answer"
          placeholder="请输入答案"
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
          v-hasPermi="['race:bank:add']"
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
          v-hasPermi="['race:bank:edit']"
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
          v-hasPermi="['race:bank:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['race:bank:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="bankList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="题目id" align="center" prop="questionId" />
      <el-table-column label="题目" align="center" prop="question" />
      <el-table-column label="选项A" align="center" prop="a" />
      <el-table-column label="选项B" align="center" prop="b" />
      <el-table-column label="选项C" align="center" prop="c" />
      <el-table-column label="选项D" align="center" prop="d" />
      <el-table-column label="答案" align="center" prop="answer" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['race:bank:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['race:bank:remove']"
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

    <!-- 添加或修改题库对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="题目" prop="question">
          <el-input v-model="form.question" placeholder="请输入题目" />
        </el-form-item>
        <el-form-item label="选项A" prop="a">
          <el-input v-model="form.a" placeholder="请输入选项A" />
        </el-form-item>
        <el-form-item label="选项B" prop="b">
          <el-input v-model="form.b" placeholder="请输入选项B" />
        </el-form-item>
        <el-form-item label="选项C" prop="c">
          <el-input v-model="form.c" placeholder="请输入$选项C" />
        </el-form-item>
        <el-form-item label="选项D" prop="d">
          <el-input v-model="form.d" placeholder="请输入选项D" />
        </el-form-item>
        <el-form-item label="答案" prop="answer">
          <el-input v-model="form.answer" placeholder="请输入答案" />
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
import { listBank, getBank, delBank, addBank, updateBank } from "@/api/race/bank";

export default {
  name: "Bank",
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
      // 题库表格数据
      bankList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        question: null,
        a: null,
        b: null,
        c: null,
        d: null,
        answer: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        question: [
          { required: true, message: "$comment不能为空", trigger: "blur" }
        ],
        a: [
          { required: true, message: "$comment不能为空", trigger: "blur" }
        ],
        b: [
          { required: true, message: "$comment不能为空", trigger: "blur" }
        ],
        c: [
          { required: true, message: "$comment不能为空", trigger: "blur" }
        ],
        d: [
          { required: true, message: "$comment不能为空", trigger: "blur" }
        ],
        answer: [
          { required: true, message: "$comment不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询题库列表 */
    getList() {
      this.loading = true;
      listBank(this.queryParams).then(response => {
        this.bankList = response.rows;
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
        questionId: null,
        question: null,
        a: null,
        b: null,
        c: null,
        d: null,
        answer: null
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
      this.ids = selection.map(item => item.questionId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加题库";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const questionId = row.questionId || this.ids
      getBank(questionId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改题库";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.questionId != null) {
            updateBank(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addBank(this.form).then(response => {
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
      const questionIds = row.questionId || this.ids;
      this.$modal.confirm('是否确认删除题库编号为"' + questionIds + '"的数据项？').then(function() {
        return delBank(questionIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('race/bank/export', {
        ...this.queryParams
      }, `bank_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
