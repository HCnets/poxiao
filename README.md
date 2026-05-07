<div align="center">

# 🌌 南工破晓 (Poxiao)

> **“将大学生活里那些零碎、跳跃、失焦的时刻，重新组织成一个连贯的个人界面。”**

<p>
  <img src="https://img.shields.io/badge/Version-1.3.4_Stability_Refinement-FFD700?style=for-the-badge&logo=git&logoColor=black" alt="Version 1.3.4" />
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

**v1.3.0 (Ultimate Calculator)** 现已发布。本次更新对标国际顶级 App 的终极交互形态，科学计算器引入全局撤销/重做堆栈 (Undo/Redo Stack) 以防长公式误删；全面引入键盘侧滑输入 (Swipe-on-Keys) 系统，彻底移除了占用空间的二级高级面板，将高级函数（如 sin, cos, ln, 根号, 幂次等）集成至主键盘的滑动交互中，实现操作零层级跃迁。

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
| `13` | **v1.2.0** | **多智能体与液态玻璃 2.0 (MAS & Liquid Glass 2.0)** | 落地基于协程并行的专家智能体系统 (Multi-Agent System)，UI 引入物理视差陀螺仪动效与风格无级自定义。实现主动式学务助手与高定物理动效。 |
| `14` | **v1.2.1** | **骨架屏与修复 (Skeleton & Fixes)** | 全面引入骨架屏 (SkeletonPlaceholder) 过渡状态，修复多处编译时重声明与模型冲突。优化了应用启动时的空间连贯性。 |
| `15` | **v1.2.2** | **陀螺仪调优 (Gyroscope Refinement)** | 为陀螺仪视差引入 Leaky Integrator 自居中算法，解决初始姿态偏移与抖动问题。提升了 UI 在动态握持下的防抖平滑度。 |
| `16` | **v1.2.3** | **全局组件大一统 (Global Unity)** | 将二级页面老旧 Surface 替换为 LiquidGlass，引入顶层 Z 轴悬浮岛 (Floating Header) 布局。消灭应用内部风格割裂感。 |
| `17` | **v1.2.4** | **人机工程学重构 (Ergonomics Overhaul)** | 对重度输入模块进行视线焦点与按键网格重组，支持不等宽键盘与实时公式推演反馈。提升了计算过程中的交互顺滑度。 |
| `18` | **v1.2.5** | **便当盒布局 (Bento Box Layout)** | 彻底重构首页模块加载引擎，全面引入 Bento Box (便当盒) 交错网格布局。大幅提升首页仪表盘科技感与空间利用率。 |
| `19` | **v1.2.6** | **黄金流交互革命 (Golden Flow)** | 彻底重构科学计算器，采用固定底部键盘布局，引入 `FocusTarget` 全局焦点路由系统。解决了长列表滚动交互的痛点。 |
| `20` | **v1.2.7** | **黑曜石霓虹进化 (Obsidian Neon)** | 全量落地计算器双主题美学 (iOS-like / Obsidian)，为交互组件注入物理级 HapticFeedback。提升了操作的物理反馈质感。 |
| `21` | **v1.2.8** | **专业工具链与手势 (Pro Toolchain)** | 重构方程、复数、向量等专业模块，支持极坐标运算；为历史记录引入侧滑删除 (Swipe-to-Dismiss) 高阶交互。 |
| `22` | **v1.2.9** | **工作区头部修复 (Header Layout Fix)** | 优化顶部信息卡片排版，将标题由纵向改为横向紧凑排列，解决了进入模块时卡片遮挡操作面板的空间冲突问题。 |
| `23` | **v1.3.0** | **终极计算器 (Ultimate Calculator)** | 引入 Swipe-on-Keys 全域侧滑交互、Undo/Redo 堆栈、Base-N 实时预览与智能括号补全。实现了操作零层级跃迁。 |
| `24` | **v1.3.1** | **自然数学引擎 (Natural Math Engine)** | 实现自然数学渲染 (WYSIWYG)，支持分式、根号递归排版。开发物理光标 (Logical Cursor) 逻辑跳转系统。 |
| `25` | **v1.3.2** | **感知增强 (Intelligence Awareness)** | 引入实时函数图像预览 (Graphing Sparkline)，利用物理引擎实装光标刻度感触觉。增强了公式输入的直观物理感知。 |
| `26` | **v1.3.3** | **单位感知计算 (Unit-Aware Computation)** | 科学计算器支持物理单位感知计算，实现 `UnitDimension` 量纲代数系统。支持单位自动推导与兼容性校验。 |
| `27` | **v1.3.5** | **架构稳定性加固 (Stability Refinement)** | 实施递归深度监控 (上限 100 层) 与表达式长度限制。注入全局 `runCatching` 异常拦截器，杜绝 StackOverflow。 |
| `28` | **v1.3.6** | **渲染管线修复 (Pipeline Fix)** | 修正 Compose 渲染管线中的非法 Composable 调用。下沉 UI 防御逻辑至解析层，实现“静默降级”渲染机制。 |
| `29` | **v1.3.7** | **历史记录稳定性 (History Stabilization)** | 引入 `HistoryRecord` 唯一标识符系统 (UID)。解决 `LazyColumn` 在 `reverseLayout` 下使用非稳定 Key 导致的滑动闪退。 |
| `30` | **v1.3.8** | **可视化网格输入 (Natural Grid Input)** | 重构矩阵与向量模块为可视化网格布局，支持物理单元格导航与嵌套渲染。实现类似 Excel 的无缝单元格跳转交互。 |
| `31` | **v1.3.9** | **方程可视化革命 (Equation WYSIWYG)** | 彻底重构方程模块为所见即所得 (WYSIWYG) 布局，支持系数网格的物理焦点导航。 |
| `当前` | **v1.4.0** | **复数可视化与极坐标优化 (Complex WYSIWYG)** | 全面重构复数模块为 WYSIWYG 布局，支持代数式与极坐标式的自然数学渲染与物理焦点导航。 |
| `历史补丁` | **v1.3.9.1** | **导航逻辑修正 (Navigation Patch)** | 修复了 `ScientificCalculatorScreen` 中 `onMoveCursor` 回调的状态引用错误，确保物理焦点导航的稳定性。 |
| `历史补丁` | **v1.3.8.3** | **架构状态补丁 (Architecture State Patch)** | 修复 `FixedKeypadModuleContainer` 中的 `val` 重赋值编译错误。统一状态提升 (State Hoisting) 链路，确保模块间通信逻辑闭合。 |链路，确保模块间通信逻辑闭合。 |

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
