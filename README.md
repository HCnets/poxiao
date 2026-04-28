<div align="center">

# 南工破晓

<p><strong>From first draft to a living campus agent app.</strong></p>

<p>
  <img src="https://img.shields.io/badge/Platform-Android-2F6B4F?style=for-the-badge" alt="Platform Android" />
  <img src="https://img.shields.io/badge/Language-Kotlin-3F6A60?style=for-the-badge" alt="Language Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-6E8F62?style=for-the-badge" alt="UI Jetpack Compose" />
  <img src="https://img.shields.io/badge/State-Agent%20Driven-8A6B3F?style=for-the-badge" alt="State Agent Driven" />
  <img src="https://img.shields.io/badge/Phase-Refactoring%20In%20Progress-5B7C6B?style=for-the-badge" alt="Phase Refactoring In Progress" />
</p>

<p>
  从首版原型，到持续生长的校园智能体应用。<br/>
  <code>南工破晓</code> 不只是一个项目名，它更像一条被逐步打磨出来的迭代轨迹。
</p>

<p>
  <a href="#项目快照">项目快照</a> ·
  <a href="#迭代纪年">迭代纪年</a> ·
  <a href="#智能体特色">智能体特色</a> ·
  <a href="#工程状态">工程状态</a> ·
  <a href="#维护入口">维护入口</a>
</p>

</div>

---

> [!NOTE]
> 这不是一份普通的功能清单，而是一页按照真实迭代过程组织的项目门面。  
> 你既能在这里看到产品如何成形，也能看到工程如何一步一步被清理、拆分和推向可维护状态。

这是一个面向校园学习与个人效率场景的 Android 原生应用，采用 `Kotlin + Jetpack Compose` 构建，并持续以 `Agent` 驱动方式进行重构、维护和文档沉淀。  
它试图把课表、教务、待办、复习、专注、信息流、校园服务与智能体建议，收束进一条更顺手、更安静、也更有氛围感的日常使用路径。  
它要做的不是把功能堆满，而是把大学生活里那些零碎、跳跃、失焦的时刻，重新组织成一个连贯的个人界面。

这个仓库同时也是一条清晰的迭代轨迹: 从首版界面骨架，到工程治理、文档补齐、模块拆分，再到 GitHub 维护与后续持续演进。  
因此它既可以被看作一个校园产品的发布史，也可以被看作一段持续发生的技术演进史。

---

## 项目快照

| 维度 | 当前状态 |
| --- | --- |
| `平台形态` | Android 原生应用 |
| `核心技术` | Kotlin / Jetpack Compose |
| `交互气质` | 森林感中式配色 / 液态玻璃 / 低干扰信息组织 |
| `产品方向` | 校园学习与个人效率整合 |
| `驱动方式` | Agent 辅助重构、维护、文档沉淀 |
| `当前阶段` | 持续模块化与智能体化推进 |

<div align="center">

`校园日常` `森林感中式配色` `液态玻璃` `低干扰信息组织` `智能体辅助` `持续重构`

</div>

---

## 迭代纪年

| 阶段 | 代号 | 产品侧变化 | 工程侧推进 |
| --- | --- | --- | --- |
| `第 1 轮` | <img src="https://img.shields.io/badge/%E7%82%B9%E7%81%AB-Prototype-476C57?style=flat-square" alt="点火 Prototype" /> | 首页、悬浮导航与“更多”页完成首版高保真界面，整体气质确定为森林感中式配色与液态玻璃方向 | 建立 Android 原生工程骨架，完成基础 Gradle 配置与 Compose 项目起步 |
| `第 2 轮` | <img src="https://img.shields.io/badge/%E5%AE%9A%E9%AA%A8-Structure-5D7B5B?style=flat-square" alt="定骨 Structure" /> | AI、课表、待办、番茄钟、记账、信息流、校园导航、设置八大模块形成完整入口关系 | 为 Hiagent、教务、地图、信息流、本地同步等能力预留接口合同与扩展位置 |
| `第 3 轮` | <img src="https://img.shields.io/badge/%E5%8B%98%E7%95%8C-Discovery-6A6854?style=flat-square" alt="勘界 Discovery" /> | 明确当前真正可演进的主工程边界，避免后续迭代跑偏 | 读取根工程、Gradle 配置、资源和附带目录，梳理目录关系、迁移痕迹与维护风险 |
| `第 4 轮` | <img src="https://img.shields.io/badge/%E6%88%90%E6%96%87-Docs-7B5D4E?style=flat-square" alt="成文 Docs" /> | 项目开始具备可展示、可评审、可交接的完整表达能力 | 输出技术规范、架构图、评审材料模板、交付清单，并补齐事实依据与引用关系 |
| `第 5 轮` | <img src="https://img.shields.io/badge/%E5%BC%80%E5%88%80-Split-8A6B3F?style=flat-square" alt="开刀 Split" /> | 用户可见功能保持稳定，备份与教务资料能力开始从大文件中解耦 | 将本地备份恢复外提到 `LocalBackupSupport.kt`，将教务账号资料持久化外提到 `AcademicAccountSupport.kt` |
| `第 6 轮` | <img src="https://img.shields.io/badge/%E6%94%B6%E5%8F%A3-Fix-7C5C52?style=flat-square" alt="收口 Fix" /> | 保证已有页面体验不因拆分出现明显回退 | 修复拆分后的可见性问题，例如 `addKnownAcademicAccountId` 的访问级别错误，保证本地可继续编译维护 |
| `第 7 轮` | <img src="https://img.shields.io/badge/%E4%B8%8A%E9%93%BE-GitHub-4F6B6A?style=flat-square" alt="上链 GitHub" /> | 项目开始具备面向外部维护和持续同步的展示入口 | 补充 `.gitignore`、更新 README、整理维护路线图，并推送 GitHub 仓库 |
| `第 8 轮` | <img src="https://img.shields.io/badge/%E8%A7%A3%E8%80%A6-Decouple-5B7C6B?style=flat-square" alt="解耦 Decouple" /> | 教务账号页、更多页、设置页逐步从巨型入口文件中独立出来，结构更清晰 | 从 `PoxiaoApp.kt` 外提 `AcademicAccountScreen.kt`、`MoreScreen.kt`、`PreferencesScreen.kt`，继续收缩核心大文件 |
| `当前` | <img src="https://img.shields.io/badge/%E7%BB%AD%E8%88%AA-Ongoing-3F6A60?style=flat-square" alt="续航 Ongoing" /> | 校园智能体权限控制、工具编排、上下文建议能力继续保留并完善 | 持续按页面边界与能力边界拆分，向更稳定、更可维护的工程状态推进 |

<details>
<summary><strong>展开查看这条时间线的意义</strong></summary>

- 它既记录“用户能看到什么变化”，也记录“仓库内部发生了什么变化”
- 它不是一次性生成原型，而是持续多轮演进
- 它不是只做界面，而是同步推进代码、文档、维护与仓库治理
- 它不是只做静态功能，而是逐步把项目往校园智能体应用方向推进
- 每一轮都尽量遵循低风险、可验证、可交接的迭代方式

</details>

---

## 项目气质

- 全中文高保真界面，强调校园语境而不是通用后台感
- 森林感中式配色，刻意避开常见蓝紫科技风
- 全局液态玻璃 / 毛玻璃质感，结合弥散背景与轻动态模糊
- 以“时间段体验”组织页面，而不是简单堆叠功能入口

## 核心版图

- `学习主线`：课表、考试周、复习计划、课程相关入口
- `执行主线`：待办、专注、学习数据、记录与导出
- `校园主线`：校园服务、地图、信息流、通知触达
- `个人主线`：教务账号、设置偏好、本地备份、权限控制
- `智能体主线`：上下文读取、权限控制、工具编排、动作型建议

---

## 智能体特色

- 校园智能体化：围绕课表、待办、复习、专注、成绩、地图等校园上下文组织能力
- 权限可控：内置智能体权限页，可分别控制可读取的数据范围与可执行的动作范围
- 工具编排：支持基于上下文生成待办建议、专注绑定建议、地图跳转建议等动作型结果
- 持续重构：通过 Agent 辅助完成代码检索、依赖分析、页面外提、诊断修复与文档补齐

## 当前完成度

- 首版 Android 工程结构与 Gradle 配置已建立
- 首页、悬浮导航与“更多”卡片页已完成首轮高保真落地
- AI、课表、待办、番茄钟、记账、信息流、校园导航、设置八大模块已具备信息架构
- Hiagent、教务、地图、信息流、本地同步等接口合同已预留
- 教务账号资料、本地备份恢复、更多页、设置页等能力已开始从 `PoxiaoApp.kt` 按边界拆分

## 发布式摘要

> `现在能看到的`  
> 一个已经拥有统一视觉语言、校园场景主线和智能体入口雏形的 Android 应用。

> `现在在发生的`  
> 以低风险页面外提和能力解耦为主的持续重构，以及围绕文档、仓库、维护流程的同步治理。

> `接下来要做的`  
> 继续补上真实教务接入、Hiagent 鉴权、地图能力、本地数据库与更完整的校园智能体编排链路。

## 工程状态

- 当前主体验证方向明确，适合继续沿页面边界做模块化拆分
- 文档体系已具备，包括技术规范、架构图、评审模板、交付清单与维护路线图
- Agent 已参与真实工程维护流程，而不是只做一次性内容生成
- GitHub 仓库已建立，便于后续长期维护、同步和版本管理

## 近期里程碑

- 已外提 `LocalBackupSupport.kt`，承接本地备份与恢复序列化逻辑
- 已外提 `AcademicAccountSupport.kt`，承接教务账号资料持久化与头像处理逻辑
- 已外提 `AcademicAccountScreen.kt`
- 已外提 `MoreScreen.kt`
- 已外提 `PreferencesScreen.kt`
- 已补齐技术规范、评审模板、交付清单、架构图与 GitHub 维护说明

## 维护入口

- GitHub 仓库：`https://github.com/HCnets/poxiao`
- 日常维护手册：`docs/升级维护路线图.md`
- 当前主工程：`C:\Users\HCnets\Desktop\AI`

<div align="center">

<sub>Design the day. Refactor the code. Let the campus app breathe.</sub>

</div>

## 仍待接入

- `HITA / HITA-L` 源码或哈工深教务对接细节
- `Hiagent` 真实鉴权与接口地址
- 地图 SDK 与定位权限流程
- 本地数据库方案 `Room / SQLite / 远端`
- 开源中文字体文件

## 字体说明

- 当前主题先用系统 `Serif / SansSerif` 占位，保证结构可运行
- 如果要进一步做出更稳、更高级的中文气质，建议把思源宋体 SC 与思源黑体 SC 加入 `app/src/main/res/font/`，再替换 `Type.kt` 中的占位字体族
