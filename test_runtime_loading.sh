#!/bin/bash

echo "Testing runtime model loading..."

# 检查runtime文件夹是否存在
if [ ! -d "runtime" ]; then
    echo "❌ runtime folder not found!"
    exit 1
fi

# 检查必要的文件是否存在
required_files=(
    "runtime/mao_pro.model3.json"
    "runtime/mao_pro.moc3"
    "runtime/mao_pro.4096/texture_00.png"
    "runtime/expressions/exp_01.exp3.json"
    "runtime/motions/mtn_01.motion3.json"
)

for file in "${required_files[@]}"; do
    if [ ! -f "$file" ]; then
        echo "❌ Required file not found: $file"
        exit 1
    fi
done

echo "✅ All required files found in runtime folder"
echo "📁 Runtime folder structure:"
echo "   ├── mao_pro.model3.json (main model config)"
echo "   ├── mao_pro.moc3 (model data)"
echo "   ├── mao_pro.4096/texture_00.png (texture)"
echo "   ├── expressions/ (8 expression files)"
echo "   └── motions/ (7 motion files)"

echo ""
echo "🎮 To test runtime model loading:"
echo "   1. Start the application"
echo "   2. Press 'r' key to load runtime model"
echo "   3. Check console output for loading status"
echo ""
echo "📊 Expected model features:"
echo "   - 8 expressions (exp_01 to exp_08)"
echo "   - 7 motions (mtn_01 to mtn_04, special_01 to special_03)"
echo "   - Physics and pose support"
echo "   - High-resolution texture (4096x4096)"
