package moe.brianhsu.live2d.demo.app

import moe.brianhsu.live2d.config.UnifiedConfig
import scala.util.Try

object LanguageManager {
  
  sealed trait Language
  case object English extends Language
  case object Chinese extends Language
  case object Japanese extends Language
  
  // 默认语言
  private val defaultLanguage: Language = English
  
  // 语言资源映射
  private val resources: Map[Language, Map[String, String]] = Map(
    English -> Map(
      // Toolbar
      "toolbar.load_avatar" -> "Load Avatar",
      "toolbar.green_background" -> "Green Background", 
      "toolbar.select_background" -> "Select Background",
      "toolbar.pure_color_background" -> "Pure Color Background",
      "toolbar.transparent_background" -> "Transparent Background",
      "toolbar.language" -> "Language",
      
      // Tabs
      "tabs.normal" -> "Normal",
      "tabs.face_tracking" -> "Face Tracking",
      "tabs.model_control" -> "Model Control",
      
      // Face Tracking
      "face_tracking.open_see_face_settings" -> "OpenSeeFace Settings",
      "face_tracking.disable_eye_blink" -> "Disable Eye Blink",
      "face_tracking.simulate_eye_gaze" -> "Simulate Eye Gaze", 
      "face_tracking.gaze_tracking" -> "Gaze Tracking",
      "face_tracking.auto_start" -> "Auto Start",
      "face_tracking.outline" -> "Outline",
      
      // OpenSeeFace
      "openseeface.camera" -> "Camera:",
      "openseeface.camera_tooltip" -> "Select camera for face tracking",
      "openseeface.fps" -> "FPS:",
      "openseeface.fps_tooltip" -> "Set camera frames per second",
      "openseeface.resolution" -> "Resolution:",
      "openseeface.resolution_tooltip" -> "Set camera resolution",
      "openseeface.port" -> "Port:",
      "openseeface.port_tooltip" -> "Set port for sending tracking data",
      "openseeface.model" -> "Model:",
      "openseeface.model_tooltip" -> "This can be used to select the tracking model. Higher numbers are models with better tracking quality, but slower speed, except for model 4, which is wink optimized. Models 1 and 0 tend to be too rigid for expression and blink detection. Model -2 is roughly equivalent to model 1, but faster. Model -3 is between models 0 and -1.",
      "openseeface.visualize" -> "Visualize:",
      "openseeface.visualize_tooltip" -> "Set this to 1 to visualize the tracking, to 2 to also show face ids, to 3 to add confidence values or to 4 to add numbers to the point display.",
      "openseeface.command" -> "Command:",
      "openseeface.ip" -> "IP:",
      "openseeface.ip_tooltip" -> "Set IP address for sending tracking data",
      "openseeface.camera_id" -> "Camera ID:",
      "openseeface.camera_id_tooltip" -> "Set camera ID (0, 1...)",
      "openseeface.extra_parameters" -> "Extra Parameters:",
      "openseeface.mirror_input" -> "Mirror Input",
      "openseeface.mirror_input_tooltip" -> "Process a mirror image of the input video",
      "openseeface.command_preview" -> "Command Preview:",
      
      // Model Control
      "model_control.facial_expression" -> "Facial Expression",
      "model_control.head_pose" -> "Head Pose",
      "model_control.body_pose" -> "Body Pose", 
      "model_control.other" -> "Other",
      "model_control.save_parameters" -> "Save Parameters",
      "model_control.reset_to_default" -> "Reset to Default",
      
      // Parameters
      "parameter.ParamMouthOpenY" -> "Mouth Open Y",
      "parameter.ParamEyeLSmile" -> "Eye L Smile",
      "parameter.ParamEyeBallX" -> "Eye Ball X",
      "parameter.ParamCheek" -> "Cheek",
      "parameter.ParamEyeROpen" -> "Eye R Open",
      "parameter.ParamBrowLY" -> "Brow L Y",
      "parameter.ParamBrowLForm" -> "Brow L Form",
      "parameter.ParamBrowRAngle" -> "Brow R Angle",
      "parameter.ParamBrowRX" -> "Brow R X",
      "parameter.ParamEyeBallY" -> "Eye Ball Y",
      "parameter.ParamEyeRSmile" -> "Eye R Smile",
      "parameter.ParamBrowRY" -> "Brow R Y",
      "parameter.ParamBrowLAngle" -> "Brow L Angle",
      "parameter.ParamMouthForm" -> "Mouth Form",
      "parameter.ParamBrowLX" -> "Brow L X",
      "parameter.ParamBrowRForm" -> "Brow R Form",
      "parameter.ParamEyeLOpen" -> "Eye L Open",
      "parameter.ParamBodyAngleX" -> "Body Angle X",
      "parameter.ParamBodyAngleY" -> "Body Angle Y",
      "parameter.ParamBodyAngleZ" -> "Body Angle Z",
      "parameter.ParamAngleX" -> "Angle X",
      "parameter.ParamAngleY" -> "Angle Y",
      "parameter.ParamAngleZ" -> "Angle Z",
      "parameter.ParamBreath" -> "Breath",
      "parameter.ParamHairFront" -> "Hair Front",
      "parameter.ParamHairSide" -> "Hair Side",
      "parameter.ParamHairBack" -> "Hair Back",
      "parameter.Param" -> "Param",
      "parameter.Param2" -> "Param 2",
      "parameter.Param5" -> "Param 5",
      "parameter.Param6" -> "Param 6",
      "parameter.Param7" -> "Param 7",
      "parameter.Param8" -> "Param 8",
      "parameter.Param9" -> "Param 9",
      "parameter.Param11" -> "Param 11",
      "parameter.Param12" -> "Param 12",
      "parameter.Param13" -> "Param 13",
      "parameter.Param14" -> "Param 14",
      "parameter.Param15" -> "Param 15",
      "parameter.Param16" -> "Param 16",
      "parameter.Param17" -> "Param 17",
      "parameter.Param19" -> "Param 19",
      "parameter.Param20" -> "Param 20",
      "parameter.Param21" -> "Param 21",
      "parameter.Param22" -> "Param 22",
      
      // Angle Rotation Parameters
      "parameter.Param_Angle_Rotation_1_ArtMesh121" -> "Angle Rotation 1",
      "parameter.Param_Angle_Rotation_2_ArtMesh121" -> "Angle Rotation 2",
      "parameter.Param_Angle_Rotation_3_ArtMesh121" -> "Angle Rotation 3",
      "parameter.Param_Angle_Rotation_4_ArtMesh121" -> "Angle Rotation 4",
      "parameter.Param_Angle_Rotation_5_ArtMesh121" -> "Angle Rotation 5",
      "parameter.Param_Angle_Rotation_6_ArtMesh121" -> "Angle Rotation 6",
      "parameter.Param_Angle_Rotation_7_ArtMesh121" -> "Angle Rotation 7",
      "parameter.Param_Angle_Rotation_8_ArtMesh121" -> "Angle Rotation 8",
      "parameter.Param_Angle_Rotation_9_ArtMesh121" -> "Angle Rotation 9",
      
      // Additional Parameters
      "parameter.Param3" -> "Param 3",
      "parameter.Param4" -> "Param 4",
      "parameter.Param10" -> "Param 10",
      "parameter.Param18" -> "Param 18",
      "parameter.Param23" -> "Param 23",
      "parameter.Param24" -> "Param 24",
      "parameter.Param25" -> "Param 25",
      "parameter.Param26" -> "Param 26",
      "parameter.Param27" -> "Param 27",
      "parameter.Param28" -> "Param 28",
      "parameter.Param29" -> "Param 29",
      "parameter.Param30" -> "Param 30",
      "parameter.Param31" -> "Param 31",
      "parameter.Param32" -> "Param 32",
      "parameter.Param33" -> "Param 33",
      "parameter.Param34" -> "Param 34",
      "parameter.Param35" -> "Param 35",
      "parameter.Param36" -> "Param 36",
      "parameter.Param37" -> "Param 37",
      "parameter.Param38" -> "Param 38",
      "parameter.Param39" -> "Param 39",
      "parameter.Param40" -> "Param 40",
      
      // Effects
      "effects.title" -> "Effects",
      "effects.blink" -> "Blink",
      "effects.breath" -> "Breath",
      "effects.face_direction" -> "Face Direction",
      "effects.click_and_drag" -> "Click and drag",
      "effects.follow_by_mouse" -> "Follow by mouse",
      "effects.lip_sync" -> "Lip Sync",
      
      // Motions
      "motions.title" -> "Motions",
      "motions.loop" -> "Loop",
      "motions.lip_sync" -> "Lip Sync",
      "motions.weight" -> "Weight:",
      "motions.volume" -> "Volume:",
      
      // Expressions
      "expressions.title" -> "Expressions",
      
      // Messages
      "message.cannot_load_background" -> "Cannot load background",
      "message.unsupported_file_type" -> "Unsupported file type.",
      "message.cannot_load_avatar" -> "Cannot load avatar.",
      "message.select_background_color" -> "Select Background Color"
    ),
    
    Chinese -> Map(
      // Toolbar
      "toolbar.load_avatar" -> "加载模型",
      "toolbar.green_background" -> "绿色背景",
      "toolbar.select_background" -> "选择背景", 
      "toolbar.pure_color_background" -> "纯色背景",
      "toolbar.transparent_background" -> "透明背景",
      "toolbar.language" -> "语言",
      
      // Tabs
      "tabs.normal" -> "普通",
      "tabs.face_tracking" -> "面部追踪",
      "tabs.model_control" -> "模型控制",
      
      // Face Tracking
      "face_tracking.open_see_face_settings" -> "OpenSeeFace 设置",
      "face_tracking.disable_eye_blink" -> "禁用眨眼",
      "face_tracking.simulate_eye_gaze" -> "模拟眼球追踪",
      "face_tracking.gaze_tracking" -> "视线追踪", 
      "face_tracking.auto_start" -> "自动开始",
      "face_tracking.outline" -> "轮廓",
      
      // OpenSeeFace
      "openseeface.camera" -> "摄像头:",
      "openseeface.camera_tooltip" -> "选择用于面部追踪的摄像头",
      "openseeface.fps" -> "帧率:",
      "openseeface.fps_tooltip" -> "设置摄像头帧率",
      "openseeface.resolution" -> "分辨率:",
      "openseeface.resolution_tooltip" -> "设置摄像头分辨率",
      "openseeface.port" -> "端口:",
      "openseeface.port_tooltip" -> "设置发送追踪数据的端口",
      "openseeface.model" -> "模型:",
      "openseeface.model_tooltip" -> "用于选择追踪模型。数字越大，追踪质量越好但速度越慢，除了模型4是专门为眨眼优化的。模型1和0对表情和眨眼检测过于僵硬。模型-2大致等同于模型1，但更快。模型-3介于模型0和-1之间。",
      "openseeface.visualize" -> "可视化:",
      "openseeface.visualize_tooltip" -> "设置为1来可视化追踪，设置为2来显示面部ID，设置为3来添加置信度值，或设置为4来在点显示中添加数字。",
      "openseeface.command" -> "命令:",
      "openseeface.ip" -> "IP:",
      "openseeface.ip_tooltip" -> "设置发送追踪数据的IP地址",
      "openseeface.camera_id" -> "摄像头ID:",
      "openseeface.camera_id_tooltip" -> "设置摄像头ID (0, 1...)",
      "openseeface.extra_parameters" -> "额外参数:",
      "openseeface.mirror_input" -> "镜像输入",
      "openseeface.mirror_input_tooltip" -> "处理输入视频的镜像",
      "openseeface.command_preview" -> "命令预览:",
      
      // Model Control
      "model_control.facial_expression" -> "面部表情",
      "model_control.head_pose" -> "头部姿态",
      "model_control.body_pose" -> "身体姿态",
      "model_control.other" -> "其他",
      "model_control.save_parameters" -> "保存参数",
      "model_control.reset_to_default" -> "恢复默认",
      
      // Parameters
      "parameter.ParamMouthOpenY" -> "嘴巴张开Y",
      "parameter.ParamEyeLSmile" -> "左眼微笑",
      "parameter.ParamEyeBallX" -> "眼球X",
      "parameter.ParamCheek" -> "脸颊",
      "parameter.ParamEyeROpen" -> "右眼张开",
      "parameter.ParamBrowLY" -> "左眉Y",
      "parameter.ParamBrowLForm" -> "左眉形状",
      "parameter.ParamBrowRAngle" -> "右眉角度",
      "parameter.ParamBrowRX" -> "右眉X",
      "parameter.ParamEyeBallY" -> "眼球Y",
      "parameter.ParamEyeRSmile" -> "右眼微笑",
      "parameter.ParamBrowRY" -> "右眉Y",
      "parameter.ParamBrowLAngle" -> "左眉角度",
      "parameter.ParamMouthForm" -> "嘴巴形状",
      "parameter.ParamBrowLX" -> "左眉X",
      "parameter.ParamBrowRForm" -> "右眉形状",
      "parameter.ParamEyeLOpen" -> "左眼张开",
      "parameter.ParamBodyAngleX" -> "身体角度X",
      "parameter.ParamBodyAngleY" -> "身体角度Y",
      "parameter.ParamBodyAngleZ" -> "身体角度Z",
      "parameter.ParamAngleX" -> "角度X",
      "parameter.ParamAngleY" -> "角度Y",
      "parameter.ParamAngleZ" -> "角度Z",
      "parameter.ParamBreath" -> "呼吸",
      "parameter.ParamHairFront" -> "前发",
      "parameter.ParamHairSide" -> "侧发",
      "parameter.ParamHairBack" -> "后发",
      "parameter.Param" -> "参数",
      "parameter.Param2" -> "参数2",
      "parameter.Param5" -> "参数5",
      "parameter.Param6" -> "参数6",
      "parameter.Param7" -> "参数7",
      "parameter.Param8" -> "参数8",
      "parameter.Param9" -> "参数9",
      "parameter.Param11" -> "参数11",
      "parameter.Param12" -> "参数12",
      "parameter.Param13" -> "参数13",
      "parameter.Param14" -> "参数14",
      "parameter.Param15" -> "参数15",
      "parameter.Param16" -> "参数16",
      "parameter.Param17" -> "参数17",
      "parameter.Param19" -> "参数19",
      "parameter.Param20" -> "参数20",
      "parameter.Param21" -> "参数21",
      "parameter.Param22" -> "参数22",
      
      // Angle Rotation Parameters
      "parameter.Param_Angle_Rotation_1_ArtMesh121" -> "角度旋转1",
      "parameter.Param_Angle_Rotation_2_ArtMesh121" -> "角度旋转2",
      "parameter.Param_Angle_Rotation_3_ArtMesh121" -> "角度旋转3",
      "parameter.Param_Angle_Rotation_4_ArtMesh121" -> "角度旋转4",
      "parameter.Param_Angle_Rotation_5_ArtMesh121" -> "角度旋转5",
      "parameter.Param_Angle_Rotation_6_ArtMesh121" -> "角度旋转6",
      "parameter.Param_Angle_Rotation_7_ArtMesh121" -> "角度旋转7",
      "parameter.Param_Angle_Rotation_8_ArtMesh121" -> "角度旋转8",
      "parameter.Param_Angle_Rotation_9_ArtMesh121" -> "角度旋转9",
      
      // Additional Parameters
      "parameter.Param3" -> "参数3",
      "parameter.Param4" -> "参数4",
      "parameter.Param10" -> "参数10",
      "parameter.Param18" -> "参数18",
      "parameter.Param23" -> "参数23",
      "parameter.Param24" -> "参数24",
      "parameter.Param25" -> "参数25",
      "parameter.Param26" -> "参数26",
      "parameter.Param27" -> "参数27",
      "parameter.Param28" -> "参数28",
      "parameter.Param29" -> "参数29",
      "parameter.Param30" -> "参数30",
      "parameter.Param31" -> "参数31",
      "parameter.Param32" -> "参数32",
      "parameter.Param33" -> "参数33",
      "parameter.Param34" -> "参数34",
      "parameter.Param35" -> "参数35",
      "parameter.Param36" -> "参数36",
      "parameter.Param37" -> "参数37",
      "parameter.Param38" -> "参数38",
      "parameter.Param39" -> "参数39",
      "parameter.Param40" -> "参数40",
      
      // Effects
      "effects.title" -> "效果",
      "effects.blink" -> "眨眼",
      "effects.breath" -> "呼吸",
      "effects.face_direction" -> "面部方向",
      "effects.click_and_drag" -> "点击拖拽",
      "effects.follow_by_mouse" -> "跟随鼠标",
      "effects.lip_sync" -> "唇形同步",
      
      // Motions
      "motions.title" -> "动作",
      "motions.loop" -> "循环",
      "motions.lip_sync" -> "唇形同步",
      "motions.weight" -> "权重:",
      "motions.volume" -> "音量:",
      
      // Expressions
      "expressions.title" -> "表情",
      
      // Messages
      "message.cannot_load_background" -> "无法加载背景",
      "message.unsupported_file_type" -> "不支持的文件类型。",
      "message.cannot_load_avatar" -> "无法加载模型。",
      "message.select_background_color" -> "选择背景颜色"
    ),
    
    Japanese -> Map(
      // Toolbar
      "toolbar.load_avatar" -> "アバター読み込み",
      "toolbar.green_background" -> "緑背景", 
      "toolbar.select_background" -> "背景選択",
      "toolbar.pure_color_background" -> "単色背景",
      "toolbar.transparent_background" -> "透明背景",
      "toolbar.language" -> "言語",
      
      // Tabs
      "tabs.normal" -> "通常",
      "tabs.face_tracking" -> "顔追跡",
      "tabs.model_control" -> "モデル制御",
      
      // Face Tracking
      "face_tracking.open_see_face_settings" -> "OpenSeeFace設定",
      "face_tracking.disable_eye_blink" -> "まばたき無効",
      "face_tracking.simulate_eye_gaze" -> "視線シミュレート", 
      "face_tracking.gaze_tracking" -> "視線追跡",
      "face_tracking.auto_start" -> "自動開始",
      "face_tracking.outline" -> "アウトライン",
      
      // OpenSeeFace
      "openseeface.camera" -> "カメラ:",
      "openseeface.camera_tooltip" -> "顔追跡用カメラを選択",
      "openseeface.fps" -> "FPS:",
      "openseeface.fps_tooltip" -> "カメラのフレームレートを設定",
      "openseeface.resolution" -> "解像度:",
      "openseeface.resolution_tooltip" -> "カメラの解像度を設定",
      "openseeface.port" -> "ポート:",
      "openseeface.port_tooltip" -> "追跡データ送信用ポートを設定",
      "openseeface.model" -> "モデル:",
      "openseeface.model_tooltip" -> "追跡モデルを選択。数字が大きいほど追跡品質は良いが速度は遅い。モデル4はまばたきに特化。モデル1と0は表情とまばたき検出が硬い。モデル-2はモデル1とほぼ同等だが高速。モデル-3はモデル0と-1の中間。",
      "openseeface.visualize" -> "可視化:",
      "openseeface.visualize_tooltip" -> "1で追跡を可視化、2で顔ID表示、3で信頼度値追加、4で点表示に数字追加。",
      "openseeface.command" -> "コマンド:",
      "openseeface.ip" -> "IP:",
      "openseeface.ip_tooltip" -> "追跡データ送信用IPアドレスを設定",
      "openseeface.camera_id" -> "カメラID:",
      "openseeface.camera_id_tooltip" -> "カメラIDを設定 (0, 1...)",
      "openseeface.extra_parameters" -> "追加パラメータ:",
      "openseeface.mirror_input" -> "入力ミラー",
      "openseeface.mirror_input_tooltip" -> "入力動画のミラー処理",
      "openseeface.command_preview" -> "コマンドプレビュー:",
      
      // Model Control
      "model_control.facial_expression" -> "顔表情",
      "model_control.head_pose" -> "頭部姿勢",
      "model_control.body_pose" -> "体姿勢",
      "model_control.other" -> "その他",
      "model_control.save_parameters" -> "パラメータ保存",
      "model_control.reset_to_default" -> "デフォルトに戻す",
      
      // Parameters
      "parameter.ParamMouthOpenY" -> "口開きY",
      "parameter.ParamEyeLSmile" -> "左目微笑",
      "parameter.ParamEyeBallX" -> "眼球X",
      "parameter.ParamCheek" -> "頬",
      "parameter.ParamEyeROpen" -> "右目開き",
      "parameter.ParamBrowLY" -> "左眉Y",
      "parameter.ParamBrowLForm" -> "左眉形状",
      "parameter.ParamBrowRAngle" -> "右眉角度",
      "parameter.ParamBrowRX" -> "右眉X",
      "parameter.ParamEyeBallY" -> "眼球Y",
      "parameter.ParamEyeRSmile" -> "右目微笑",
      "parameter.ParamBrowRY" -> "右眉Y",
      "parameter.ParamBrowLAngle" -> "左眉角度",
      "parameter.ParamMouthForm" -> "口形状",
      "parameter.ParamBrowLX" -> "左眉X",
      "parameter.ParamBrowRForm" -> "右眉形状",
      "parameter.ParamEyeLOpen" -> "左目開き",
      "parameter.ParamBreath" -> "呼吸",
      "parameter.ParamBodyAngleZ" -> "体角度Z",
      "parameter.ParamHairSide" -> "横髪",
      "parameter.ParamBodyAngleY" -> "体角度Y",
      "parameter.ParamBodyAngleX" -> "体角度X",
      "parameter.ParamHairFront" -> "前髪",
      "parameter.ParamHairBack" -> "後髪",
      "parameter.ParamAngleZ" -> "角度Z",
      "parameter.ParamAngleY" -> "角度Y",
      "parameter.ParamAngleX" -> "角度X",
      "parameter.Param" -> "パラメータ",
      "parameter.Param2" -> "パラメータ2",
      "parameter.Param5" -> "パラメータ5",
      "parameter.Param6" -> "パラメータ6",
      "parameter.Param7" -> "パラメータ7",
      "parameter.Param8" -> "パラメータ8",
      "parameter.Param9" -> "パラメータ9",
      "parameter.Param11" -> "パラメータ11",
      "parameter.Param12" -> "パラメータ12",
      "parameter.Param13" -> "パラメータ13",
      "parameter.Param14" -> "パラメータ14",
      "parameter.Param15" -> "パラメータ15",
      "parameter.Param16" -> "パラメータ16",
      "parameter.Param17" -> "パラメータ17",
      "parameter.Param19" -> "パラメータ19",
      "parameter.Param20" -> "パラメータ20",
      "parameter.Param21" -> "パラメータ21",
      "parameter.Param22" -> "パラメータ22",
      
      // Angle Rotation Parameters
      "parameter.Param_Angle_Rotation_1_ArtMesh121" -> "角度回転1",
      "parameter.Param_Angle_Rotation_2_ArtMesh121" -> "角度回転2",
      "parameter.Param_Angle_Rotation_3_ArtMesh121" -> "角度回転3",
      "parameter.Param_Angle_Rotation_4_ArtMesh121" -> "角度回転4",
      "parameter.Param_Angle_Rotation_5_ArtMesh121" -> "角度回転5",
      "parameter.Param_Angle_Rotation_6_ArtMesh121" -> "角度回転6",
      "parameter.Param_Angle_Rotation_7_ArtMesh121" -> "角度回転7",
      "parameter.Param_Angle_Rotation_8_ArtMesh121" -> "角度回転8",
      "parameter.Param_Angle_Rotation_9_ArtMesh121" -> "角度回転9",
      
      // Additional Parameters
      "parameter.Param3" -> "パラメータ3",
      "parameter.Param4" -> "パラメータ4",
      "parameter.Param10" -> "パラメータ10",
      "parameter.Param18" -> "パラメータ18",
      "parameter.Param23" -> "パラメータ23",
      "parameter.Param24" -> "パラメータ24",
      "parameter.Param25" -> "パラメータ25",
      "parameter.Param26" -> "パラメータ26",
      "parameter.Param27" -> "パラメータ27",
      "parameter.Param28" -> "パラメータ28",
      "parameter.Param29" -> "パラメータ29",
      "parameter.Param30" -> "パラメータ30",
      "parameter.Param31" -> "パラメータ31",
      "parameter.Param32" -> "パラメータ32",
      "parameter.Param33" -> "パラメータ33",
      "parameter.Param34" -> "パラメータ34",
      "parameter.Param35" -> "パラメータ35",
      "parameter.Param36" -> "パラメータ36",
      "parameter.Param37" -> "パラメータ37",
      "parameter.Param38" -> "パラメータ38",
      "parameter.Param39" -> "パラメータ39",
      "parameter.Param40" -> "パラメータ40",
      
      // Effects
      "effects.title" -> "エフェクト",
      "effects.blink" -> "まばたき",
      "effects.breath" -> "呼吸",
      "effects.face_direction" -> "顔方向",
      "effects.click_and_drag" -> "クリック&ドラッグ",
      "effects.follow_by_mouse" -> "マウス追従",
      "effects.lip_sync" -> "リップシンク",
      
      // Motions
      "motions.title" -> "モーション",
      "motions.loop" -> "ループ",
      "motions.lip_sync" -> "リップシンク",
      "motions.weight" -> "重み:",
      "motions.volume" -> "音量:",
      
      // Expressions
      "expressions.title" -> "表情",
      
      // Messages
      "message.cannot_load_background" -> "背景を読み込めません",
      "message.unsupported_file_type" -> "サポートされていないファイルタイプです。",
      "message.cannot_load_avatar" -> "モデルを読み込めません。",
      "message.select_background_color" -> "背景色を選択"
    )
  )
  
  // 当前语言
  private var currentLanguage: Language = loadLanguage()
  
  // 初始化时从DemoApp加载语言设置
  def initializeFromDemoApp(): Unit = {
    val savedLanguage = DemoApp.loadLanguage().toLowerCase
    val language = savedLanguage match {
      case "chinese" => Chinese
      case "english" => English
      case "japanese" => Japanese
      case _ => defaultLanguage
    }
    if (currentLanguage != language) {
      currentLanguage = language
    }
  }
  
  // 语言变更监听器
  private var languageChangeListeners: List[() => Unit] = List.empty
  
  def getCurrentLanguage: Language = currentLanguage
  
  def setLanguage(language: Language): Unit = {
    if (currentLanguage != language) {
      currentLanguage = language
      saveLanguage(language)
      notifyLanguageChange()
    }
  }
  
  def getText(key: String): String = {
    resources.get(currentLanguage)
      .flatMap(_.get(key))
      .getOrElse(key) // 如果找不到翻译，返回key本身
  }
  
  def addLanguageChangeListener(listener: () => Unit): Unit = {
    languageChangeListeners = listener :: languageChangeListeners
  }
  
  def removeLanguageChangeListener(listener: () => Unit): Unit = {
    languageChangeListeners = languageChangeListeners.filterNot(_ == listener)
  }
  
  private def notifyLanguageChange(): Unit = {
    languageChangeListeners.foreach(_.apply())
  }
  
  private def saveLanguage(language: Language): Unit = {
    val languageString = language match {
      case English => "english"
      case Chinese => "chinese"
      case Japanese => "japanese"
    }
    DemoApp.saveLanguage(languageString)
  }
  
  private def loadLanguage(): Language = {
    val languageString = DemoApp.loadLanguage()
    languageString.toLowerCase match {
      case "chinese" => Chinese
      case "japanese" => Japanese
      case _ => English
    }
  }
  
  // 获取所有支持的语言
  def getSupportedLanguages: List[(Language, String)] = List(
    (English, "English"),
    (Chinese, "中文"),
    (Japanese, "日本語")
  )
}
