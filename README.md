<div align="center">

# 🌌 南工破晓 (Poxiao)

> **“将大学生活里那些零碎、跳跃、失焦的时刻，重新组织成一个连贯的个人界面。”**

<p>
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform Android" />
  <img src="https://img.shields.io/badge/Language-Kotlin_1.9+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Language Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="UI Jetpack Compose" />
  <img src="https://img.shields.io/badge/State-Agent_Driven-FF6B6B?style=for-the-badge&logo=probot&logoColor=white" alt="State Agent Driven" />
</p>

[📖 项目简介](#-项目简介) • [⚡ 核心能力](#-核心能力矩阵) • [🎨 设计美学](#-设计美学) • [🚀 架构演进](#-架构演进与时间线) • [🛠️ 维护指南](#-工程与维护)

</div>

---

## 📖 项目简介

`南工破晓` 是一个面向校园学习与个人效率场景的 Android 原生应用。
它采用 **Kotlin + Jetpack Compose** 构建，并持续以 **Agent (智能体) 驱动** 的方式进行工程重构与演进。

不同于传统校园 App 的功能堆砌，南工破晓试图打造一个 **低干扰、连续、具备上下文感知** 的工作台。通过引入校园智能体，它将原本割裂的课表、教务、待办、专注和校园服务无缝编排在一起，把“找功能”变成“顺理成章的下一步”。

---

## ⚡ 核心能力矩阵

| 领域 | 核心模块 | 状态与说明 |
| :--- | :--- | :--- |
| 📚 **学习主线** | 课表 / 考试周 / 复习计划 | 已建立信息架构，打造连贯的学业跟踪体验。 |
| 🎯 **执行主线** | 待办 / 专注 / 数据看板 | 结合番茄钟与任务流，提供沉浸式的执行环境。 |
| 🏫 **校园主线** | 校园地图 / 服务 / 信息流 | 整合校园生活所需的高频入口，随时触达。 |
| ⚙️ **个人控制** | 教务账号 / 偏好 / 本地备份 | 强调数据隐私与本地化管控，已完成核心解耦。 |
| 🤖 **智能体引擎** | 上下文读取 / 工具编排 | 核心亮点：根据当前时间、地点、任务自动提供建议与编排动作。 |

---

## 🎨 设计美学

项目建立了一套完整的 UI/UX 规范，确保在任何场景下都能传达出宁静与专注的质感：

- **🌲 森林感中式配色**：刻意避开刺眼的科技蓝，采用更具生命力与宁静感的森系深色调（如 `#0F172A`, `#164E63`, `#65A30D`）。
- **🪟 液态玻璃质感 (Liquid Glass)**：全局应用轻量级的毛玻璃、弥散光影与轻模糊，界面通透且富有呼吸感。
- **📐 低干扰排版**：基于 `8pt` 间距系统严格构建，克制使用强调色，让内容本身成为唯一的视觉焦点。

---

## 🚀 架构演进与时间线

本项目并非一次性堆砌而成，而是经历了一系列严谨的工程治理与模块化重构。从首版巨型入口文件 `PoxiaoApp.kt`，到如今清晰的页面边界。

<details>
<summary><b>⏳ 点击展开完整迭代纪年表</b></summary><br>

| 阶段 | 代号 | 工程推进与成果 |
| :---: | :--- | :--- |
| `01` | **点火** | 建立 Android 工程骨架，完成基础 Gradle 与 Compose 启动，确立高保真视觉基调。 |
| `02` | **定骨** | 形成八大模块入口关系，预留 Hiagent、教务、地图、信息流等关键接口合同。 |
| `03` | **勘界** | 梳理复杂目录关系与迁移痕迹，明确主工程边界与维护风险。 |
| `04` | **成文** | 沉淀技术规范、架构图、评审模板与交付清单，建立标准化的研发基线。 |
| `05` | **开刀** | 开始从巨型文件中解耦本地备份与教务账号资料持久化逻辑（外提 `Support` 类）。 |
| `06` | **收口** | 修复拆分导致的可见性（`internal`/`private`）与编译问题，保障核心体验无回退。 |
| `07` | **上链** | 完善 `.gitignore` 与工程配置，正式推送 GitHub 建立持续维护主阵地。 |
| `08` | **解耦** | 持续拆分 `PoxiaoApp.kt`，外提 `AcademicAccountScreen`, `MoreScreen`, `PreferencesScreen`。 |
| `当前` | **续航** | 校园智能体控制、工具编排和场景化建议持续推进，持续进行模块化治理。 |

</details>

---

## 🛠️ 工程与维护

本项目保持着极高的代码整洁度与文档完备度，随时可进行二次开发、模块扩展或交接。

### 📦 维护导航
- **主工程目录**: `C:\Users\HCnets\Desktop\AI`
- **技术规范文档**: [`docs/技术规范-南工破晓.md`](docs/技术规范-南工破晓.md)
- **系统架构总览**: [`docs/diagrams/poxiao-architecture.puml`](docs/diagrams/poxiao-architecture.puml)
- **版本升级路线图**: [`docs/升级维护路线图.md`](docs/升级维护路线图.md)

### 🚧 近期开发计划
- [ ] 进一步拆分 `PoxiaoApp.kt` 剩余的页面与能力边界（如 `BottomDock` 等）。
- [ ] 推进 `HITA` 源码或哈工深教务的真实对接细节。
- [ ] 落地 `Hiagent` 鉴权流程与校园地图 SDK 接入。

<br>
<div align="center">
  <sub>Built with ❤️ by AI & Human pair programming. Let the campus app breathe.</sub>
</div>
