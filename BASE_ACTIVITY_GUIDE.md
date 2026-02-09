# BaseActivity 使用指南

## 功能特性

### 1. 沉浸式状态栏
- 自动设置透明状态栏
- 支持状态栏文字颜色切换（深色/浅色）
- 适配不同Android版本

### 2. 自适应底部虚拟导航
- 智能检测虚拟导航栏存在
- 自动适配导航栏高度和宽度
- 支持导航栏沉浸式效果
- 导航栏文字颜色自适应

### 3. 公共初始化方法
- `initViews()`: 抽象方法，必须实现视图初始化
- `initData()`: 初始化业务数据
- `initListeners()`: 初始化事件监听器
- 生命周期回调方法支持

### 4. 多语言配置
- 运行时语言切换
- 自动适配系统语言变化
- 支持中英文切换
- Activity重建机制保证语言即时生效

## 使用方法

### 基本使用

```kotlin
class MyActivity : BaseActivity() {
    
    // 配置选项（可选）
    override val enableImmersiveStatusBar: Boolean = true
    override val enableImmersiveNavigationBar: Boolean = true
    override val enableAutoHideKeyboard: Boolean = true
    override val enableMultiLanguage: Boolean = true
    
    private lateinit var textView: TextView
    private lateinit var button: Button
    
    override fun onCreated(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_my)
    }
    
    override fun initViews() {
        // 初始化视图组件
        textView = findViewById(R.id.text_view)
        button = findViewById(R.id.button)
    }
    
    override fun initData() {
        // 初始化数据
        textView.text = "Hello World"
    }
    
    override fun initListeners() {
        // 设置监听器
        button.setOnClickListener {
            showToast("按钮被点击")
        }
    }
}
```

### 多语言切换

```kotlin
// 切换到中文
switchLanguage("zh")

// 切换到英文
switchLanguage("en")

// 监听语言变化
override fun onLanguageChanged() {
    super.onLanguageChanged()
    // 更新UI文本
    updateUIText()
}
```

### 状态栏和导航栏控制

```kotlin
// 设置状态栏文字颜色
setStatusBarMode(true)  // 浅色文字（适用于深色背景）
setStatusBarMode(false) // 深色文字（适用于浅色背景）

// 设置导航栏文字颜色
setNavigationBarMode(true)  // 浅色文字
setNavigationBarMode(false) // 深色文字

// 获取系统UI信息
val statusBarHeight = getStatusBarHeight()
val navBarHeight = getNavigationBarHeight()
val hasNavBar = hasNavigationBar()
```

### 快捷方法

```kotlin
// 显示Toast
showToast("提示信息")

// 启动Activity
startActivity<OtherActivity>()
startActivityForResult<OtherActivity>(REQUEST_CODE)

// 结束Activity
finishActivity()
finishActivityWithResult(RESULT_OK)
```

## 配置选项说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `enableImmersiveStatusBar` | `true` | 是否启用沉浸式状态栏 |
| `enableImmersiveNavigationBar` | `true` | 是否启用沉浸式导航栏 |
| `enableAutoHideKeyboard` | `true` | 是否启用点击空白处隐藏键盘 |
| `enableActivityManager` | `true` | 是否启用Activity管理器 |
| `enableMultiLanguage` | `true` | 是否启用多语言支持 |

## 生命周期回调

```kotlin
override fun onCreated(savedInstanceState: Bundle?) {
    // Activity创建时调用，在setContentView之前
}

override fun onViewInitialized() {
    // 视图初始化完成后调用
}

override fun onDataInitialized() {
    // 数据初始化完成后调用
}

override fun onListenersInitialized() {
    // 监听器初始化完成后调用
}

override fun onLanguageChanged() {
    // 语言切换后调用
}

override fun onConfigurationChanged() {
    // 配置改变时调用
}
```

## 注意事项

1. **抽象方法必须实现**：`initViews()` 和 `initListeners()` 是抽象方法，子类必须实现
2. **语言切换会重启Activity**：调用 `switchLanguage()` 会自动重启当前Activity
3. **沉浸式效果适配**：会自动适配不同Android版本的状态栏和导航栏
4. **内存管理**：内置Activity管理器，自动处理Activity生命周期

## 扩展建议

可以根据项目需求进一步扩展：
- 添加网络状态监听
- 集成权限申请框架
- 添加主题切换功能
- 集成统计分析SDK
- 添加异常处理机制