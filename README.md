<div align="center">

# 🌌 南工破晓 (Poxiao)

> **“将大学生活里那些零碎、跳跃、失焦的时刻，重新组织成一个连贯的个人界面。”**

<p>
  <img src="https://img.shields.io/badge/Version-1.2.0_MAS-FFD700?style=for-the-badge&logo=git&logoColor=black" alt="Version 1.2.0" />
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

**v1.2.0 (MAS & Liquid Glass 2.0)** 现已发布，成功落地了多智能体并行调度编排系统，并全面引入了带有陀螺仪物理动效的高定液态玻璃 UI。

---

## ⚡ 核心能力矩阵

| 领域 | 核心模块 | 状态与说明 |
| :--- | :--- | :--- |
| 📚 **学习主线** | 课表 / 考试周 / 复习计划 | **联动增强**：复习任务智能嵌入课表，支持冲突一键调优 (🪄)。 |
| 🎯 **执行主线** | 待办 / 专注 / 数据看板 | **闭环写回**：番茄钟专注达成后自动同步复习进度与掌握度成长。 |
| 🤖 **智能助手** | 简报推送 / 魔法调优 | **多智能体 (MAS)**：基于并行专家协程与 DeepSeek 反思协议，实现实时动态推理与服务。 |
| ⚙️ **产品线矩阵** | hitsz / academic / lite | **多版本适配**：支持产品线能力裁剪（哈深/通用教务/轻量版）。 |

---

## 🎨 设计美学

项目建立了一套完整的 UI/UX 规范，确保在任何场景下都能传达出宁静与专注的质感：

- **🌲 森林感中式配色**：采用更具生命力与宁静感的森系深色调（如 `PineInk`, `ForestDeep`, `Ginkgo`）。
- **🪟 液态玻璃 2.0 (Liquid Glass)**：支持 **星辉雾晶 / 冰川透镜 / 流光霓虹** 三种高定风格；引入基于低通滤波校准的 **陀螺仪物理视差动效**，全局应用动态雾度与内阴影；支持用户无级自定义模糊、发光与透明度。
- **📐 智能联动反馈**：任务完成伴随量化掌握度提升提示，让成长感肉眼可见。

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
| `当前` | **v1.2.0** | **稳定版 (Stable)** | **MAS & Liquid Glass 2.0** 正式发布，兼顾智能编排与高定物理动效。 |

</details>

---

## 🌿 分支与版本说明

| 分支 / 标签 | 状态 | 适用场景 |
| :--- | :--- | :--- |
| `main` | **[Stable v1.2.0]** | **当前生产线**：具备多智能体并发与动态玻璃动效的最新稳定版。 |
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
