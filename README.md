展示基本天气信息，显示位置，缓存数据，根据天气和时间自动切换背景视频，展示24h气温折线图，lottie天气图标，3日预报（api只返回3天悲），下拉刷新数据，空气质量界面文字随污染程度变色,可切换24h/多日空气质量圆柱图
repository通过定位服务获取经纬度，高德逆地理定位服务获得地址，然后网络请求彩云api的天气数据并解析，缓存数据，viewmodel将数据传递给activity刷新ui。
心得：了解了app开发的过程，掌握了第三方api使用方法，gson，retrofit
待优化：架构有点混乱，越写头越大，ui设计借鉴小米天气。
![Screenrecorder-2026-02-24-13-35-44-927](https://github.com/user-attachments/assets/d3b4e47e-8dd3-4934-aa18-99afe23434ef)
