# BaseActivity 封装说明

## 概述
BaseActivity是一个功能完善的Android Activity基类封装，提供了常用的Activity功能和生命周期管理。

## 主要特性

### 1. 生命周期管理
- 自动管理Activity栈
- 完整的生命周期回调
- 统一的初始化流程

### 2. UI相关功能
- 状态栏管理（透明、颜色设置、文字颜色）
- 软键盘自动隐藏
- Toast便捷显示
- View可见性控制

### 3. 导航功能
- Activity启动简化
- 带结果启动Activity
- 返回键处理
- Activity管理器集成

### 4. 工具方法
- 状态栏高度获取
- 键盘显示/隐藏控制
- 点击防抖处理

## 使用方法

### 基本使用
```kotlin
class MyActivity : BaseActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my)
    }
    
    override fun initView() {
        // 初始化视图组件
    }
    
    override fun initListener() {
        // 设置监听器
    }
    
    override fun initData() {
        // 初始化数据
    }
}
```

### 配置选项
```kotlin
class MyActivity : BaseActivity() {
    // 控制是否启用Activity管理
    override val enableActivityManager: Boolean = true
    
    // 控制是否启用点击空白处隐藏键盘
    override val enableHideKeyboardOnTouch: Boolean = true
    
    // 控制是否启用状态栏透明
    override val enableTransparentStatusBar: Boolean = false
}
```

### 启动Activity
```kotlin
// 简单启动
startActivity<OtherActivity>()

// 带参数启动
startActivity<OtherActivity> {
    putExtra("key", "value")
}

// 带结果启动
startActivityForResult<OtherActivity>(REQUEST_CODE) {
    putExtra("data", "sample")
}
```

### 显示提示信息
```kotlin
// 显示Toast
showToast("操作成功")

// 显示加载对话框
showLoading("正在加载...")
hideLoading()
```

## 扩展函数

### Context扩展
```kotlin
// 显示Toast
context.showToast("消息")
context.showToast(R.string.message)

// 启动Activity
context.startActivity<MainActivity>()
context.startActivityWithExtras {
    putString("key", "value")
}

// View可见性控制
view.visible()
view.invisible()
view.gone()
```

### View点击防抖
```kotlin
button.setOnDebounceClickListener {
    // 防抖点击处理
    doSomething()
}
```

## 工具类说明

### ActivityManager
```kotlin
// 添加Activity
ActivityManager.addActivity(activity)

// 移除Activity
ActivityManager.removeActivity(activity)

// 获取当前Activity
val current = ActivityManager.currentActivity()

// 结束指定Activity
ActivityManager.finishActivity(activity)
ActivityManager.finishActivity(MainActivity::class.java)

// 结束所有Activity
ActivityManager.finishAllActivity()

// 退出应用
ActivityManager.exitApp()
```

### StatusBarUtils
```kotlin
// 设置状态栏透明
StatusBarUtils.setTransparentStatusBar(activity)

// 设置状态栏颜色
StatusBarUtils.setStatusBarColor(activity, color)

// 设置状态栏文字模式
StatusBarUtils.setStatusBarLightMode(activity)  // 浅色文字
StatusBarUtils.setStatusBarDarkMode(activity)   // 深色文字

// 隐藏/显示状态栏
StatusBarUtils.hideStatusBar(activity)
StatusBarUtils.showStatusBar(activity)
```

### KeyboardUtils
```kotlin
// 显示键盘
KeyboardUtils.showKeyboard(editText)

// 隐藏键盘
KeyboardUtils.hideKeyboard(activity)
KeyboardUtils.hideKeyboard(view)

// 切换键盘
KeyboardUtils.toggleKeyboard(activity)

// 检查键盘状态
val isShowing = KeyboardUtils.isKeyboardShowing(activity)
```

## 注意事项

1. **内存泄漏防护**：BaseActivity会自动管理Activity引用，避免内存泄漏
2. **生命周期顺序**：initData() → initView() → initListener()
3. **配置灵活性**：可通过重写配置属性来自定义行为
4. **扩展性**：可根据项目需求继续扩展功能

## 最佳实践

1. 所有Activity都应该继承BaseActivity
2. 在initView()中初始化UI组件
3. 在initListener()中设置事件监听
4. 在initData()中进行数据初始化
5. 合理使用配置选项来定制Activity行为