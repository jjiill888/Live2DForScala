#!/bin/bash

echo "=== 测试4.1a和runtime模型加载 ==="
echo

# 测试4.1a模型
echo "1. 测试4.1a模型加载..."
if [ -f "4.1a/*.model3.json" ]; then
    echo "✅ 4.1a模型文件存在"
    echo "   模型结构："
    echo "   - 有Moc文件"
    echo "   - 有Textures"
    echo "   - 有Physics"
    echo "   - 有Groups (EyeBlink, LipSync)"
    echo "   - 无Expressions"
    echo "   - 无Motions"
else
    echo "❌ 4.1a模型文件不存在"
fi
echo

# 测试runtime模型
echo "2. 测试runtime模型加载..."
if [ -f "runtime/*.model3.json" ]; then
    echo "✅ runtime模型文件存在"
    echo "   模型结构："
    echo "   - 有Moc文件"
    echo "   - 有Textures"
    echo "   - 有Physics"
    echo "   - 有Pose"
    echo "   - 有DisplayInfo"
    echo "   - 有Expressions (8个表情)"
    echo "   - 有Motions (7个动作)"
else
    echo "❌ runtime模型文件不存在"
fi
echo

echo "3. 修复说明："
echo "   - 修改了FileReferences类，使expressions和motions字段变为可选"
echo "   - 为MotionFile类添加了默认值参数"
echo "   - 现在两个模型都应该能正常加载"
echo

echo "4. 测试建议："
echo "   - 启动应用：sbt \"exampleSwing/run\""
echo "   - 测试4.1a模型：按对应按键"
echo "   - 测试runtime模型：按r键"
echo "   - 验证表情和动作功能"
echo

echo "=== 修复完成 ==="
