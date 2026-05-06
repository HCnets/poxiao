<div align="center">

# 🌌 南工破晓 (Poxiao)

> **“将大学生活里那些零碎、跳跃、失焦的时刻，重新组织成一个连贯的个人界面。”**

<p>
  <img src="https://img.shields.io/badge/Version-1.2.9_Header_Fix-FFD700?style=for-the-badge&logo=git&logoColor=black" alt="Version 1.2.9" />
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform Android" />
  <img src="https://img.shields.io/badge/Language-Kotlin_2.x-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Language Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="UI Jetpack Compose" />
</p>

[📖 项目简介](#-项目简介) • [⚡ 核心能力](#-核心能力矩阵) • [🎨 设计美学](#-设计美学) • [🚀 架构演进](#-架构演进与时间线) • [🛠️ 维护指南](#-工程与维护)

</div>

---

## 📖 项目简介

`南工破晓` 是一个面向校园学习与个人效率场景的 Android 原生应用。
它采用 **Kotlin + Jetpack Compose** 构建，并持续以 **Agent (智能体) 驱动** 的方式进行工程重构与演进。

**v1.2.9 (Workspace Header Layout Fix)** 现已发布。本次更新针对科学计算器的顶部信息卡片（Workspace Header）进行了空间优化，将其内部的标题与副标题由纵向排列改为横向紧凑排列，大幅缩减了卡片的垂直高度。此修复有效解决了进入各专业计算模块时，顶部卡片过大导致遮挡下方操作面板和输入区的问题，同时恢复了经典的模块命名体系，进一步提升了专业工具的空间利用率和交互视野。

**v1.2.8 (Pro Toolchain & Gesture Evolution)** 现已发布，标志着全能工具链进入 Pro 时代。全面重构了方程、复数、向量等专业模块，引入极坐标支持与 Durand-Kerner 算法；同时为历史记录注入了侧滑删除等手势交互，实现了对齐国际一流 App 的交互深度。

---

## ⚡ 核心能力矩阵

| 领域 | 核心模块 | 状态与说明 |
| :--- | :--- | :--- |
| 📚 **学习主线** | 课表 / 考试周 / 复习计划 | **联动增强**：复习任务智能嵌入课表，支持冲突一键调优 (🪄)。 |
| 🎯 **执行主线** | 待办 / 专注 / 数据看板 | **闭环写回**：番茄钟专注达成后自动同步复习进度与掌握度成长。 |
| 🤖 **智能助手** | 简报推送 / 魔法调优 | **多智能体 (MAS)**：基于并行专家协程与 DeepSeek 反思协议，实现实时动态推理与服务。 |
| ⚙️ **产品线矩阵** | hitsz / academic / lite | **多版本适配**：支持产品线能力裁剪（哈深/通用教务/轻量版）。 |
| 🧮 **科学工具** | 极致计算器 | **全域进化**：支持 13+ 专业模块，采用 Golden Flow 布局与双主题极致美学。 |

---

## 🎨 设计美学

项目建立了一套完整的 UI/UX 规范，确保在任何场景下都能传达出宁静与专注的质感：

- **🌲 森林感中式配色**：采用更具生命力与宁静感的森系深色调（如 `PineInk`, `ForestDeep`, `Ginkgo`）。
- **🪟 液态玻璃 2.0 (Liquid Glass)**：支持 **星辉雾晶 / 冰川透镜 / 流光霓虹** 三种高定风格；引入基于低通滤波校准的 **陀螺仪物理视幕动效**，全局应用动态雾度与内阴影。
- **🌓 Obsidian Neon 双主题**：
  - **Light (iOS-like)**：极简白色基调，辅以 ForestGreen 森林绿点缀，清爽通透。
  - **Dark (Hacker/Obsidian)**：深邃黑色背景，辅以 0xFF66FFB2 荧光绿霓虹勾勒，科技感十足。
- **📳 全局触觉反馈 (HapticFeedback)**：在计算器按键、列表切换、功能跳转中实装系统级震动反馈，提供真实的物理实体操作感。

---

## 🚀 架构演进与时间线

<details>
<summary><b>⏳ 点击展开完整迭代纪年表</b></summary><br>

| 阶段 | 代号 | 工程推进与成果 |
| :---: | :--- | :--- |
| `01-07` | **奠基** | 完成从单体大文件到模块化结构的初步拆分，建立 Git 归档与规范基线。 |
| `08` | **全域深拆** | **原子级解耦**：完成 Scaffold, Todo, Schedule, More 等核心模块的 Thin Shell 改造。 |
| `09` | **联动增强** | **逻辑闭环** | 实现复习-待办-课表-番茄钟四端联动，支持掌握度智能写回。 |
| `10` | **多产品线** | **架构演进** | 引入 hitsz / academic / lite 产品线方案，支持能力感知的动态裁剪。 |
| `11` | **多智能体** | **内核升级** | 落地基于 `async` 的并行专家网络与 DeepSeek 反思协议，实现主动式助理。 |
| `12` | **极致 UI** | **视觉升维** | 落地 Liquid Glass 2.0，重塑卡片排版留白，增加 SharedElement 共享元素路由过渡与全局 Haptics 触觉物理反馈。 |
| `13` | **v1.2.0** | **稳定版 (Stable)** | **MAS & Liquid Glass 2.0** 正式发布，兼顾智能编排与高定物理动效。 |
| `14` | **v1.2.1** | **稳定版 (Stable)** | **UI 极致打磨** 发布，全面引入骨架屏 (SkeletonPlaceholder) 与状态交错动画。 |
| `15` | **v1.2.2** | **稳定版 (Stable)** | **陀螺仪动效调优** 发布，引入自适应基准面算法，彻底解决 UI 偏移问题。 |
| `16` | **v1.2.3** | **稳定版 (Stable)** | **全局组件大一统** 发布，重构计算器等二级页面，消灭僵硬色块，推行 Z 轴浮岛设计。 |
| `17` | **v1.2.4** | **稳定版 (Stable)** | **人机交互重构** 发布，调整视线焦点与键盘布局，引入实时推演反馈。 |
| `18` | **v1.2.5** | **稳定版 (Stable)** | **空间容器重塑** 发布，首页全面引入 Bento Box (便当盒) 仪表盘网格布局。 |
| `19` | **v1.2.6** | **稳定版 (Stable)** | **Golden Flow 革命** 发布，彻底重构科学计算器，推行固定底部键盘与全局焦点路由。 |
| `20` | **v1.2.7** | **稳定版 (Stable)** | **Obsidian Neon 进化** 发布，全量落地计算器双主题美学与物理级 Haptics 触觉反馈。 |
| `21` | **v1.2.8** | **稳定版 (Stable)** | **Pro Toolchain & Gesture** 发布，实现专业数学工具链逻辑闭环，引入侧滑删除等高阶交互。 |
| `当前` | **v1.2.9** | **稳定版 (Stable)** | **Workspace Header 优化** 发布，修复计算器顶部信息卡片纵向排版导致的面板遮挡问题，提升空间利用率。 |

</details>

---

## 🌿 分支与版本说明

| 分支 / 标签 | 状态 | 适用场景 |
| :--- | :--- | :--- |
| `main` | **[Stable v1.2.9]** | **当前生产线**：包含最新 Golden Flow 布局、Bento 首页与全域联动能力的稳定版。 |
| `archive-monolithic` | **[Legacy]** | **历史归档**：未拆分单文件版镜像。 |
| `v1.1.0-linkage-stable` | **[Milestone]** | **里程碑锚点**：联动增强阶段的精确记录。 |

---

## 🛠️ 工程与维护

### 📦 维护导航
- **主工程目录**: `C:\Users\HCnets\Desktop\AI`
- **版本迭代说明**: [`docs/版本说明.md`](docs/版本说明.md)
- **多校适配规范**: [`docs/技术规范-南工破晓.md`](docs/技术规范-南工破晓.md) (第 17 章)

### 🚧 路线图
- [x] 完成全域 UI/逻辑 深度拆分解耦。
- [x] 实现学习主线全链路数据联动。
- [x] 接入多智能体协同控制 (Multi-Agent System) 与 UI 动效 2.0 升级。
- [ ] 完善多校区 (Multi-School) 适配的具体业务实现。

<br>
<div align="center">
  <sub>Built with ❤️ by AI & Human pair programming. Let the campus app breathe.</sub>
</div>
