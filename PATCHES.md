# c:geo CN Patches

## 当前上游基线

- 已合并 `cgeo/cgeo:release` 的上游提交 `532e1cee2`（2026.10.05）。

## Bug 修复

| 补丁 | 上游状态 |
|---|---|
| Cookie 过期时间持久化、恢复解析及诊断日志 | 当前 release 的 `Cookies.java` 未包含对应修复；Cookie 敏感设置标记不能替代本补丁，因此保留 |
| 空筛选条件导致在线地图搜索崩溃 | release 已完整修复：为常量筛选器增加类型，并在 AL、GC、OC、SU 查询入口及 GC、OC 翻页路径阻止恒假条件发起查询；本地补丁已撤销，改用上游实现 |

## 扩展功能

- **高德与腾讯地图**
  - 在统一地图的 Mapsforge 和 VTM 两种后端中接入高德地图、腾讯地图，提供对应的简体中文名称与来源署名。
  - 通过 `MapCoordinateConverter` 在应用使用的 WGS84 坐标与地图使用的 GCJ-02 坐标之间转换，覆盖地图中心、显示范围、地图覆盖物及点击坐标。
  - 高德使用 `wprd` 系列瓦片服务与 `style=10`，配置四个服务域名；多域名配置分别接入 Mapsforge 的瓦片源和 VTM 实际使用的 `BitmapTileSource`。腾讯瓦片地址支持反向 Y 轴编号。
  - Mapsforge 后端按所选地图源的坐标范围计算中心和缩放级别，应用内部位置保持 WGS84。单点只更新中心、不改变缩放级别；视图尺寸未就绪时延迟执行，并使用执行时的地图源转换范围。

- **Esri 卫星图**
  - 在 Mapsforge 和 VTM 两种后端中接入 Esri World Imagery 卫星图，提供简体中文名称与来源署名。
  - 与其他不需要坐标偏移的地图源一样，使用 Identity（恒等转换），不叠加 GCJ-02 偏移。

## 构建与仓库定制

- **CN APK 构建与发布**
  - 正式版和调试版均通过手动触发的 GitHub Actions 工作流构建，使用 Java 17，分别执行 `assembleBasicRelease` 和 `assembleBasicDebug`。
  - 正式版工作流在构建时设置包名 `cgeo.geocaching.cn`、显示名 `c:geo CN` 和文件共享标识 `cgeo.geocaching.cn.fileprovider`，并校验 APK 的包名及日期版本信息；直接运行普通 Gradle 构建不会自动应用这些修改。
  - 调试版在版本名中加入时分秒，移除 LeakCanary 内存泄漏检测依赖；仍沿用官方调试包的应用标识，不能作为独立应用与官方调试包并存。
  - 两个工作流均从仓库机密配置（Secrets）恢复持久化调试签名文件，避免每次构建重新生成签名；缺少签名配置时直接终止构建。
  - 两类 APK 均保存为 GitHub Actions 构建产物，保留 30 天。正式版另上传至 `oss://nanoweb/geocaching/cgeo_cn/`，上传凭据同样从仓库机密配置读取。
  - OSS 发布按日期命名的 `cgeo-cn-YYYYMMDD.apk` 和固定最新版 `cgeo-cn-release.apk`，两者均允许覆盖；同一天多次构建不会生成独立的历史归档。

- **自动化测试**
  - 精简上游的议题处理、分支合并及测试辅助工作流，为 `patches` 分支保留独立的单元测试流程。
  - 推送到 `patches` 时使用 Java 17 执行 `testBasicDebugUnitTest`。该流程运行本地单元测试，不包含模拟器或真机验证。

- **上游同步**
  - 手动触发同步工作流，将本仓库的 `release` 更新到官方 `cgeo/cgeo:release`；只允许快进更新，分支出现分歧时停止。
  - 同步工作流不自动合入 `patches`。合入时需单独处理冲突，并核对本地 Bug 补丁是否已被上游实现替代。

- **仓库忽略规则**
  - 忽略本地签名文件、开发机配置及 API 凭据文件，保留可共享的模板，减少敏感配置被误提交的风险。
  - 排除构建产物、生成代码、日志、IDE 与工具临时文件，避免本地环境和构建过程产生无关差异。
