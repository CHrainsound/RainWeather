# RainWeather

基于高德定位 + 彩云API 的 Android 天气应用，展示实时天气、24小时气温趋势、3日预报，并支持背景视频随天气/时间自动切换。

---

## 核心功能

- 定位与地址：高德SDK获取经纬度，逆地理编码转为具体地址
- 天气数据：彩云API获取实况、24h逐小时气温、3日预报
- 数据可视化：
  - 24h气温折线图
  - 空气质量圆柱图（支持24h/多日切换）
  - 文字随污染等级变色（优/良/轻度/中度/重度/严重）
- 动态背景：根据天气状况（晴/雨/雪）和时间段（白天/夜晚）自动切换视频
- 下拉刷新：SwipeRefreshLayout 刷新数据
- 本地缓存：缓存天气数据，减少网络请求
- Lottie动画：天气图标动效

---

## 技术栈

- 网络请求：Retrofit + Gson 解析彩云API
- 定位服务：高德地图SDK（定位+逆地理编码）
- 架构模式：ViewModel + LiveData（待优化）
- UI框架：原生Android View + 小米天气风格借鉴
- 动画：Lottie
- 图表：自定义View

---

## 快速开始

### 1. 获取API Key

- 彩云天气API（https://www.caiyunapp.com/）注册获取 token
- 高德开放平台（https://lbs.amap.com/）获取 API Key

### 2. 配置

在 local.properties 或 gradle.properties 中配置：

    CAIYUN_TOKEN=your_token
    AMAP_API_KEY=your_amap_key

### 3. 运行

    git clone https://github.com/CHrainsound/RainWeather
    # Android Studio 打开项目，Sync Gradle，Run

---

## 项目结构

RainWeather/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/rainweather/
│           │   ├── MainActivity.java
│           │   ├── model/
│           │   ├── network/
│           │   ├── viewmodel/
│           │   └── utils/
│           ├── res/
│           └── AndroidManifest.xml
├── gradle/
│   └── wrapper/
├── .gitignore
├── build.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle
└── README.md
---

## 待优化清单

- UI精细化：适配更多屏幕尺寸，深色模式支持


---

## 开发心得

通过这个项目，我完整走通了Android App开发流程：

- 第三方SDK集成（高德定位、彩云API）
- 网络请求框架使用（Retrofit + Gson）
- 数据驱动UI更新（ViewModel + LiveData）
- 图表绘制与动画集成

踩坑记录：

架构设计前期不够清晰，导致后期功能叠加时越写头越大，后续会先画好分层图再动手。

---

## 设计参考

UI视觉风格借鉴自小米天气，感谢其简洁清晰的信息层级设计。

---

![Screenrecorder-2026-02-24-13-35-44-927](https://github.com/user-attachments/assets/d3b4e47e-8dd3-4934-aa18-99afe23434ef)
