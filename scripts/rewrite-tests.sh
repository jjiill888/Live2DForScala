#!/bin/bash

# Scala 3 测试重写脚本
# 此脚本帮助识别和重写使用 ScalaMock 的测试文件

echo "=== Scala 3 测试重写工具 ==="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 查找使用 ScalaMock 的测试文件
echo -e "${YELLOW}1. 查找使用 ScalaMock 的测试文件...${NC}"
MOCK_FILES=$(find modules/core/src/test -name "*.scala" -exec grep -l "MockFactory\|mock\[" {} \;)

if [ -z "$MOCK_FILES" ]; then
    echo -e "${GREEN}✓ 没有找到使用 ScalaMock 的测试文件${NC}"
else
    echo -e "${RED}找到以下使用 ScalaMock 的测试文件：${NC}"
    echo "$MOCK_FILES" | while read -r file; do
        echo "  - $file"
    done
fi

echo ""

# 查找不使用 ScalaMock 的测试文件
echo -e "${YELLOW}2. 查找不使用 ScalaMock 的测试文件...${NC}"
NON_MOCK_FILES=$(find modules/core/src/test -name "*.scala" -exec grep -L "MockFactory\|mock\[" {} \;)

if [ -n "$NON_MOCK_FILES" ]; then
    echo -e "${GREEN}以下测试文件不需要重写：${NC}"
    echo "$NON_MOCK_FILES" | while read -r file; do
        echo "  - $file"
    done
fi

echo ""

# 检查 Scala 3 语法问题
echo -e "${YELLOW}3. 检查可能的 Scala 3 语法问题...${NC}"

# 检查导入语句
echo "检查导入语句..."
IMPORT_ISSUES=$(find modules/core/src/test -name "*.scala" -exec grep -l "import.*{.*,.*,.*,.*,.*}" {} \;)
if [ -n "$IMPORT_ISSUES" ]; then
    echo -e "${YELLOW}以下文件可能有复杂的导入语句需要检查：${NC}"
    echo "$IMPORT_ISSUES" | while read -r file; do
        echo "  - $file"
    done
fi

# 检查类定义
echo "检查类定义..."
CLASS_ISSUES=$(find modules/core/src/test -name "*.scala" -exec grep -l "with.*with" {} \;)
if [ -n "$CLASS_ISSUES" ]; then
    echo -e "${YELLOW}以下文件可能有多个 trait 混入需要检查：${NC}"
    echo "$CLASS_ISSUES" | while read -r file; do
        echo "  - $file"
    done
fi

echo ""

# 生成重写建议
echo -e "${YELLOW}4. 生成重写建议...${NC}"

if [ -n "$MOCK_FILES" ]; then
    echo -e "${RED}需要重写的文件数量：$(echo "$MOCK_FILES" | wc -l)${NC}"
    echo ""
    echo "重写建议："
    echo "1. 为每个使用 ScalaMock 的测试创建测试替身类"
    echo "2. 使用依赖注入替代直接实例化"
    echo "3. 验证测试逻辑保持不变"
    echo ""
    echo "示例重写模式："
    echo "  mock[T] -> new TestT()"
    echo "  (mock.method _).expects().returning() -> testT.verifyCall()"
fi

echo ""

# 测试编译
echo -e "${YELLOW}5. 测试当前编译状态...${NC}"
if command -v sbt &> /dev/null; then
    echo "尝试编译测试..."
    if sbt "core/Test/compile" 2>&1 | grep -q "success"; then
        echo -e "${GREEN}✓ 测试编译成功${NC}"
    else
        echo -e "${RED}✗ 测试编译失败，需要修复${NC}"
    fi
else
    echo -e "${YELLOW}⚠ sbt 未找到，跳过编译测试${NC}"
fi

echo ""
echo -e "${GREEN}=== 重写工具运行完成 ===${NC}"
echo ""
echo "下一步建议："
echo "1. 查看 TEST_REWRITE_GUIDE.md 了解详细重写方法"
echo "2. 从简单的测试文件开始重写"
echo "3. 逐步验证每个重写的测试文件"
echo "4. 最后运行完整的测试套件"
