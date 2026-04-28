# 南工破晓

这是一个按需求文档从零搭建的 Android 原生首版骨架，采用 Kotlin + Jetpack Compose。

维护入口：

- GitHub 仓库：`https://github.com/HCnets/poxiao`
- 日常维护手册：`docs/升级维护路线图.md`
- 当前主工程：`C:\Users\HCnets\Desktop\AI`

当前已完成：

- 首版 Android 工程结构与 Gradle 配置
- 全中文高保真首页、悬浮导航与“更多”卡片页
- 森林感中式配色，避免蓝紫路线
- 全局液态玻璃 / 毛玻璃风格、弥散背景与轻动态模糊
- AI、课表、待办、番茄钟、记账、信息流、校园导航、设置八大模块的信息架构
- Hiagent、教务、地图、信息流、本地同步等接口合同预留

当前仍是首版原型，待你补充后续真实能力：

- HITA / HITA-L 源码或哈工深教务对接细节
- Hiagent 真实鉴权与接口地址
- 地图 SDK 与定位权限流程
- 本地数据库方案（Room / SQLite / 远端）
- 开源中文字体文件

字体说明：

- 目前主题先用系统 Serif / SansSerif 占位，保证结构可运行。
- 如果你要完全满足“开源字体且高级”的要求，建议下一步把思源宋体 SC 与思源黑体 SC 字体文件加入 `app/src/main/res/font/`，再替换 `Type.kt` 中的占位字体族。
