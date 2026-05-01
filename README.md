<div align="center">

# 🌌 南工破晓 (Poxiao)

> **“将大学生活里那些零碎、跳跃、失焦的时刻，重新组织成一个连贯的个人界面。”**

<p>
  <img src="https://img.shields.io/badge/Version-1.1.0_Linkage-FFD700?style=for-the-badge&logo=git&logoColor=black" alt="Version 1.1.0" />
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

**v1.1.0 (Linkage Alpha)** 现已发布，实现了复习计划、待办事项、教务课表与番茄钟的**全域深度联动**，并引入了多校适配架构。

---

## ⚡ 核心能力矩阵

| 领域 | 核心模块 | 状态与说明 |
| :--- | :--- | :--- |
| 📚 **学习主线** | 课表 / 考试周 / 复习计划 | **联动增强**：复习任务智能嵌入课表，支持冲突一键调优 (🪄)。 |
| 🎯 **执行主线** | 待办 / 专注 / 数据看板 | **闭环写回**：番茄钟专注达成后自动同步复习进度与掌握度成长。 |
| 🤖 **智能助手** | 简报推送 / 魔法调优 | **主动式服务**：首页 Assistant 实时提示行程冲突，提供执行建议。 |
| ⚙️ **工程架构** | 多校适配 / 语义化版本 | **可扩展性**：支持 Gradle Flavor 切换（哈深/通用），职责清晰。 |

---

## 🎨 设计美学

项目建立了一套完整的 UI/UX 规范，确保在任何场景下都能传达出宁静与专注的质感：

- **🌲 森林感中式配色**：采用更具生命力与宁静感的森系深色调（如 `PineInk`, `ForestDeep`, `Ginkgo`）。
- **🪟 液态玻璃质感 (Liquid Glass)**：全局应用轻量级的毛玻璃、弥散光影与轻模糊，界面通透且富有呼吸感。
- **📐 智能联动反馈**：任务完成伴随量化掌握度提升提示，让成长感肉眼可见。

---

## 🚀 架构演进与时间线

<details>
<summary><b>⏳ 点击展开完整迭代纪年表</b></summary><br>

| 阶段 | 代号 | 工程推进与成果 |
| :---: | :--- | :--- |
| `01-07` | **奠基** | 完成从单体大文件到模块化结构的初步拆分，建立 Git 归档与规范基线。 |
| `08` | **全域深拆** | **原子级解耦**：完成 Scaffold, Todo, Schedule, More 等核心模块的 Thin Shell 改造。 |
| `09` | **联动增强** | **逻辑闭环**：实现复习-待办-课表-番茄钟四端联动，支持掌握度智能写回。 |
| `10` | **多校适配** | **架构演进**：引入 Gradle Flavors 管理多校变体，制定 SemVer 语义化版本规范。 |
| `当前` | **v1.1.0** | **稳定版 (Stable)**：全域联动增强版正式入库，作为后续多智能体演进的基石。 |

</details>

---

## 🌿 分支与版本说明

| 分支 / 标签 | 状态 | 适用场景 |
| :--- | :--- | :--- |
| `main` | **[Stable v1.1.0]** | **当前生产线**：具备全域联动与多校适配能力的最新稳定版。 |
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
- [ ] 接入多智能体协同控制 (Multi-Agent System)。
- [ ] 完善多校区 (Multi-School) 适配的具体业务实现。

<br>
<div align="center">
  <sub>Built with ❤️ by AI & Human pair programming. Let the campus app breathe.</sub>
</div>
