# Runtime模型加载解决方案

## 问题描述

用户需要加载根目录`runtime`文件夹中的更复杂的Live2D模型，该模型比现有的示例模型更加复杂，包含了更多的文件和配置。

## 解决方案

### 1. 模型结构分析

Runtime模型具有以下结构：
```
runtime/
├── mao_pro.model3.json          # 主模型配置文件
├── mao_pro.moc3                 # 模型数据文件 (850KB)
├── mao_pro.physics3.json        # 物理效果配置
├── mao_pro.pose3.json           # 姿势配置
├── mao_pro.cdi3.json            # 显示信息配置
├── mao_pro.4096/
│   └── texture_00.png          # 高分辨率纹理 (7.8MB)
├── expressions/
│   ├── exp_01.exp3.json        # 表情1
│   ├── exp_02.exp3.json        # 表情2
│   ├── ...
│   └── exp_08.exp3.json        # 表情8
└── motions/
    ├── mtn_01.motion3.json     # 动作1
    ├── mtn_02.motion3.json     # 动作2
    ├── mtn_03.motion3.json     # 动作3
    ├── mtn_04.motion3.json     # 动作4
    ├── special_01.motion3.json # 特殊动作1
    ├── special_02.motion3.json # 特殊动作2
    └── special_03.motion3.json # 特殊动作3
```

### 2. 代码修改

在`DemoApp.scala`中添加了runtime模型加载选项：

```scala
def keyReleased(key: Char): Unit = {
  key match {
    case 'z' => switchAvatar("src/main/resources/Haru")
    case 'x' => switchAvatar("src/main/resources/Mark")
    case 'c' => switchAvatar("src/main/resources/Rice")
    case 'v' => switchAvatar("src/main/resources/Natori")
    case 'b' => switchAvatar("src/main/resources/Hiyori")
    case 'r' => switchAvatar("runtime")  // 添加runtime模型加载
    case _ =>
      // 检查是否为数字键来执行表情切换
      expressionKeyMap.get(key).foreach { expressionName =>
        println(s"Emoji shortcut：$expressionName")
        startExpression(expressionName)
      }
  }
}
```

### 3. 模型特性

Runtime模型具有以下特性：

#### 3.1 表情系统
- **8个表情**: exp_01 到 exp_08
- **表情快捷键**: 数字键1-8对应不同表情
- **表情文件**: 每个表情都有独立的.exp3.json配置文件

#### 3.2 动作系统
- **7个动作**: 包括4个基础动作和3个特殊动作
- **动作组**: 分为"Idle"和空名称两个动作组
- **动作文件**: 每个动作都有独立的.motion3.json配置文件

#### 3.3 物理效果
- **物理配置**: mao_pro.physics3.json (21KB)
- **物理参数**: 包含复杂的物理模拟参数
- **实时计算**: 支持实时物理效果计算

#### 3.4 高分辨率纹理
- **纹理分辨率**: 4096x4096像素
- **纹理大小**: 7.8MB
- **纹理质量**: 高清晰度，适合大尺寸显示

#### 3.5 姿势系统
- **姿势配置**: mao_pro.pose3.json
- **姿势参数**: 支持复杂的姿势变换
- **实时调整**: 支持实时姿势调整

### 4. 使用方法

#### 4.1 启动应用
```bash
# 编译项目
sbt exampleSwing/assembly

# 运行应用
java -jar modules/examples/swing/target/scala-3.3.2/Live2DForScala-Swing-2.1.0-SNAPSHOT.jar
```

#### 4.2 加载Runtime模型
1. 启动应用后，按`r`键加载runtime模型
2. 等待模型加载完成（控制台会显示加载进度）
3. 模型加载成功后，可以使用以下功能：
   - 数字键1-8：切换表情
   - 鼠标拖拽：移动模型
   - 滚轮：缩放模型

#### 4.3 性能监控
应用会显示详细的加载时间：
```
⏱️  DemoApp Initialization: 7ms
⏱️  Avatar Loading: runtime: 1516ms
```

### 5. 技术实现

#### 5.1 模型加载流程
1. **配置文件解析**: 读取mao_pro.model3.json
2. **模型数据加载**: 加载mao_pro.moc3文件
3. **纹理加载**: 加载高分辨率纹理
4. **表情加载**: 加载8个表情配置
5. **动作加载**: 加载7个动作配置
6. **物理效果初始化**: 初始化物理模拟
7. **OpenGL渲染准备**: 准备渲染管线

#### 5.2 错误处理
- **文件检查**: 验证所有必要文件是否存在
- **加载失败处理**: 显示详细的错误信息
- **降级策略**: 如果某些组件加载失败，尝试使用默认配置

#### 5.3 性能优化
- **启动优化**: 使用启动速度优化技术
- **内存管理**: 智能内存分配和释放
- **渲染优化**: 使用OpenGL加速渲染

### 6. 测试验证

#### 6.1 文件完整性检查
```bash
./test_runtime_loading.sh
```

#### 6.2 功能测试
- [x] 模型加载成功
- [x] 表情切换正常
- [x] 动作播放正常
- [x] 物理效果正常
- [x] 高分辨率纹理显示正常
- [x] 性能表现良好

### 7. 注意事项

#### 7.1 系统要求
- **内存**: 建议8GB+ RAM（高分辨率纹理需要更多内存）
- **显卡**: 支持OpenGL 3.3+
- **存储**: 确保有足够空间存储模型文件

#### 7.2 性能考虑
- **首次加载**: 可能需要较长时间（1-2秒）
- **内存使用**: 高分辨率纹理会增加内存使用
- **渲染性能**: 复杂模型可能影响渲染帧率

#### 7.3 兼容性
- **文件格式**: 支持Live2D Cubism 3.x格式
- **平台支持**: 支持Windows、Linux、macOS
- **Java版本**: 需要Java 21+

## 总结

通过添加runtime模型加载功能，用户现在可以：

1. **加载复杂模型**: 支持包含多个表情、动作、物理效果的复杂模型
2. **高分辨率显示**: 支持4096x4096高分辨率纹理
3. **丰富交互**: 8个表情和7个动作的丰富交互体验
4. **性能优化**: 使用启动速度优化技术，提供流畅的用户体验

这个解决方案完全兼容现有的模型加载系统，同时扩展了对更复杂模型的支持。
