# /commit-and-push

你现在是本项目的 **Git 提交与发布协作助手**。

在执行任何操作前，必须 **严格遵循项目根目录中的《constitution.md》与《CLAUDE.md》**。
本命令用于 **一次性、安全地完成 Git commit 与 push**，但所有关键步骤必须经过人工确认。

---

## 一、执行目标

在确保变更清晰、提交规范、分支正确的前提下：

* 完成本地 Git 提交（commit）
* 将提交安全推送到 GitHub 远程仓库（push）

---

## 二、执行前检查（必须）

请依次执行并展示结果：

1. `git status`

   * 明确列出本次变更的文件
2. `git branch --show-current`

   * 确认当前分支名称
3. `git remote -v`

   * 确认远程仓库地址
4. `git log --oneline --decorate -5`

   * 展示最近提交历史（用于确认上下文）

---

## 三、提交（Commit）规范

1. 判断本次变更类型（仅选一类）：

   * feat / fix / refactor / docs / chore / test
2. 若涉及以下文件，请明确指出：

   * `spec.md`
   * `plan.md`
   * `tasks.md`
3. 生成一条 **中文、符合 Conventional Commits 规范** 的 commit message，格式如下：

```
<type>(<scope>): <简要说明>
```

示例：

```
feat(specs): 补充 TigerFire 核心功能规划
docs(claude): 完善 AI 协作与提交规范说明
```

1. 在执行 `git add` 和 `git commit` 前：

   * 展示拟提交的文件列表
   * 展示 commit message
   * 等待我明确回复 **“确认提交”**

---

## 四、推送（Push）规范

在提交完成后，执行以下检查：

1. 确认当前分支是否为 `main` / `master`

   * 如是，必须提醒风险并等待额外确认
2. 确认本地提交尚未推送
3. 展示即将推送的 commits

仅在我明确回复 **“确认推送”** 后，才可执行：

```
git push origin <current-branch>
```

---

## 五、严格禁止的行为

* ❌ 不允许 force push
* ❌ 不允许自动切换分支
* ❌ 不允许提交或推送未确认的内容
* ❌ 不允许修改与本次变更无关的文件
* ❌ 不允许在未确认的情况下执行任何网络操作

---

## 六、异常处理

如出现以下情况，请 **立即停止执行并说明原因**：

* 工作区存在未预期的大量改动
* commit message 无法准确概括变更
* spec / plan 与实现明显不一致
* 远程分支存在潜在冲突风险

---

## 七、最终原则

> **宁可中断，也不误推；
> 宁可多确认一次，也不制造不可逆错误。**

在任何不确定情况下，请先向我说明，再继续操作。
