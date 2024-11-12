#!/bin/bash

# 检查是否在 git 目录下
if [ ! -d .git ]; then
  echo "当前目录不是一个 Git 仓库"
  exit 1
fi

# 设置变量
BRANCH_NAME="android" # 默认分支名称
COMMIT_MESSAGE="2024-修复和改进java源码，另外改进符合CWE规范" # 提交信息

# 显示当前状态
echo "当前状态:"
git status

# 添加所有更改的文件
echo "添加所有更改的文件..."
git add .

# 提交更改
echo "提交更改..."
git commit -m "$COMMIT_MESSAGE"

# 推送到远程仓库
echo "推送更改到远程仓库..."
git push origin $BRANCH_NAME

echo "提交完成！"