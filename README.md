# 南工破晓

面向校园学习与个人效率场景的 Android 应用，采用 `Kotlin + Jetpack Compose` 构建，并持续以 `Agent` 驱动方式进行重构、维护和文档沉淀。

## 项目简介

`南工破晓` 想解决的是校园日常里常见的几个问题：信息分散、入口零碎、操作跳转频繁。  
项目把课表、教务、待办、复习、专注、信息流、校园服务与智能体建议，整理进一个更统一、更安静的使用路径里。

## 核心能力

| 模块 | 说明 |
| --- | --- |
| `学习` | 课表、考试周、复习计划、课程相关入口 |
| `执行` | 待办、专注、学习记录、导出能力 |
| `校园` | 校园服务、地图、信息流、通知触达 |
| `个人` | 教务账号、设置偏好、本地备份、权限控制 |
| `智能体` | 上下文读取、工具编排、动作型建议 |

## 当前状态

| 维度 | 状态 |
| --- | --- |
| `平台` | Android 原生应用 |
| `技术栈` | Kotlin / Jetpack Compose |
| `工程状态` | 持续模块化拆分中 |
| `仓库状态` | 已建立 GitHub 维护流程 |

## 近期进展

- 已完成首页、悬浮导航与“更多”页的首轮高保真实现
- 已形成 AI、课表、待办、番茄钟、记账、信息流、校园导航、设置等模块入口
- 已从 `PoxiaoApp.kt` 外提 `LocalBackupSupport.kt`
- 已从 `PoxiaoApp.kt` 外提 `AcademicAccountSupport.kt`
- 已从 `PoxiaoApp.kt` 外提 `AcademicAccountScreen.kt`
- 已从 `PoxiaoApp.kt` 外提 `MoreScreen.kt`
- 已从 `PoxiaoApp.kt` 外提 `PreferencesScreen.kt`
- 已补齐技术规范、架构图、评审模板、交付清单与维护路线图

## 工程说明

- 当前主工程定位明确，适合继续沿页面边界和能力边界拆分
- 文档体系已经具备，便于后续维护、评审和交接
- GitHub 仓库已建立，适合持续同步和版本管理

## 维护入口

- GitHub 仓库：`https://github.com/HCnets/poxiao`
- 技术规范：`docs/技术规范-南工破晓.md`
- 架构图：`docs/diagrams/poxiao-architecture.puml`
- 维护路线图：`docs/升级维护路线图.md`
- 当前主工程：`C:\Users\HCnets\Desktop\AI`

## 后续计划

- 继续拆分 `PoxiaoApp.kt` 中剩余的大块页面与能力边界
- 补上真实教务接入、Hiagent 鉴权与地图能力
- 明确本地数据库方案与字体资源方案
