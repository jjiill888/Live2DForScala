#!/bin/bash

# Live2D模型显示问题诊断脚本
# 用于排查模型导入后无法显示的问题

echo "🔍 Live2D模型显示问题诊断工具"
echo "=================================="

# 检查Java版本
echo "1. 检查Java版本..."
java -version
echo ""

# 检查OpenGL支持
echo "2. 检查OpenGL支持..."
if command -v glxinfo &> /dev/null; then
    echo "OpenGL版本:"
    glxinfo | grep "OpenGL version" | head -1
    echo "OpenGL渲染器:"
    glxinfo | grep "OpenGL renderer" | head -1
    echo "OpenGL扩展:"
    glxinfo | grep "GL_ARB_framebuffer_object\|GL_EXT_framebuffer_object" | head -2
else
    echo "⚠️  glxinfo未安装，无法检查OpenGL信息"
    echo "   请安装: sudo apt-get install mesa-utils"
fi
echo ""

# 检查显示环境
echo "3. 检查显示环境..."
echo "DISPLAY: $DISPLAY"
echo "XDG_SESSION_TYPE: $XDG_SESSION_TYPE"
echo "WAYLAND_DISPLAY: $WAYLAND_DISPLAY"
echo ""

# 检查模型文件
echo "4. 检查模型文件..."
if [ -d "models" ]; then
    echo "找到models目录:"
    ls -la models/
    echo ""
    
    # 检查.moc3文件
    echo "检查.moc3文件:"
    find models/ -name "*.moc3" -type f 2>/dev/null | head -5
    echo ""
    
    # 检查纹理文件
    echo "检查纹理文件:"
    find models/ -name "*.png" -o -name "*.jpg" -o -name "*.jpeg" | head -5
    echo ""
    
    # 检查模型配置文件
    echo "检查模型配置文件:"
    find models/ -name "*.model3.json" -o -name "*.model.json" | head -5
    echo ""
else
    echo "⚠️  未找到models目录"
    echo "   请确保模型文件位于正确的目录中"
fi
echo ""

# 检查应用程序日志
echo "5. 检查应用程序状态..."
if [ -f "last_avatar" ]; then
    echo "最后加载的模型:"
    cat last_avatar
    echo ""
fi

# 检查窗口设置
if [ -f "window_settings.txt" ]; then
    echo "窗口设置:"
    cat window_settings.txt
    echo ""
fi

# 检查自动启动设置
if [ -f "auto_start.txt" ]; then
    echo "自动启动设置:"
    cat auto_start.txt
    echo ""
fi

echo "6. 常见问题排查建议:"
echo "===================="
echo ""

echo "🔧 如果模型不显示，请检查以下项目:"
echo ""
echo "1. 模型文件完整性:"
echo "   - 确保.moc3文件存在且未损坏"
echo "   - 确保纹理文件(.png/.jpg)存在"
echo "   - 确保.model3.json配置文件存在"
echo ""

echo "2. OpenGL支持:"
echo "   - 确保显卡驱动支持OpenGL 2.1+"
echo "   - 确保支持FBO (Framebuffer Objects)"
echo "   - 在Wayland环境下尝试设置GDK_BACKEND=x11"
echo ""

echo "3. 显示环境:"
echo "   - 确保DISPLAY环境变量正确设置"
echo "   - 在Wayland下可能需要设置: export GDK_BACKEND=x11"
echo "   - 检查窗口是否被其他窗口遮挡"
echo ""

echo "4. 模型参数:"
echo "   - 检查模型是否被缩放得太小 (zoom参数)"
echo "   - 检查模型位置是否在视窗外 (offsetX/offsetY)"
echo "   - 尝试重置模型参数"
echo ""

echo "5. 应用程序状态:"
echo "   - 检查控制台是否有错误信息"
echo "   - 尝试重新加载模型"
echo "   - 检查模型是否成功加载到内存"
echo ""

echo "6. 调试步骤:"
echo "   - 运行应用程序并查看控制台输出"
echo "   - 尝试加载不同的模型文件"
echo "   - 检查模型文件路径是否正确"
echo "   - 尝试调整窗口大小"
echo ""

echo "📋 如果问题仍然存在，请提供以下信息:"
echo "   - 控制台错误信息"
echo "   - 模型文件结构"
echo "   - 操作系统和显卡信息"
echo "   - 应用程序版本"
echo ""

echo "✅ 诊断完成！"
