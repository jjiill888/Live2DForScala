#!/bin/bash

# 测试jlink配置的脚本

echo "=== Live2DForScala JLink 测试脚本 ==="
echo ""

# 检查Java版本
echo "1. 检查Java版本..."
java -version
echo ""

# 检查JAVA_HOME
echo "2. 检查JAVA_HOME环境变量..."
if [ -z "$JAVA_HOME" ]; then
    echo "警告: JAVA_HOME未设置"
else
    echo "JAVA_HOME: $JAVA_HOME"
    if [ -d "$JAVA_HOME" ]; then
        echo "JAVA_HOME目录存在"
    else
        echo "错误: JAVA_HOME目录不存在"
        exit 1
    fi
fi
echo ""

# 检查jlink工具
echo "3. 检查jlink工具..."
if command -v jlink &> /dev/null; then
    echo "jlink工具可用"
    jlink --version
else
    echo "错误: jlink工具不可用"
    exit 1
fi
echo ""

# 测试构建自定义JRE
echo "4. 测试构建自定义JRE..."
echo "构建Linux版本..."
sbt "exampleSWTLinux/jlink" 2>&1 | tee jlink-linux.log

if [ $? -eq 0 ]; then
    echo "Linux JRE构建成功"
    
    # 检查生成的JRE
    JRE_PATH="modules/examples/swt-linux-bundle/target/jlink/jre-linux"
    if [ -d "$JRE_PATH" ]; then
        echo "JRE目录存在: $JRE_PATH"
        echo "JRE大小: $(du -sh $JRE_PATH | cut -f1)"
        echo "JRE内容:"
        ls -la "$JRE_PATH/bin/"
    else
        echo "错误: JRE目录不存在"
    fi
else
    echo "错误: Linux JRE构建失败"
    echo "查看日志: jlink-linux.log"
fi
echo ""

echo "5. 测试发布包创建..."
echo "创建Linux发布包..."
sbt releaselinux 2>&1 | tee release-linux.log

if [ $? -eq 0 ]; then
    echo "Linux发布包创建成功"
    
    # 检查发布包
    RELEASE_PATH="release-pkg/Live2DForScala-SWT-Linux-2.1.0-SNAPSHOT"
    if [ -d "$RELEASE_PATH" ]; then
        echo "发布包目录存在: $RELEASE_PATH"
        echo "发布包内容:"
        ls -la "$RELEASE_PATH/"
        
        if [ -d "$RELEASE_PATH/jre" ]; then
            echo "自带JRE存在"
            echo "JRE大小: $(du -sh $RELEASE_PATH/jre | cut -f1)"
        else
            echo "警告: 自带JRE不存在"
        fi
        
        if [ -f "$RELEASE_PATH/start.sh" ]; then
            echo "启动脚本存在"
            echo "启动脚本内容:"
            cat "$RELEASE_PATH/start.sh"
        else
            echo "警告: 启动脚本不存在"
        fi
    else
        echo "错误: 发布包目录不存在"
    fi
else
    echo "错误: Linux发布包创建失败"
    echo "查看日志: release-linux.log"
fi
echo ""

echo "=== 测试完成 ==="
