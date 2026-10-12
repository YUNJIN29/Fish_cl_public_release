# 水产智诊平台 RAG 数据集建议

本文档记录适合接入“AI 鱼病智能诊断”的权威 RAG 数据源。当前系统已内置一批资料源元数据和中文问诊片段，可通过 `/api/rag/datasets` 与 `/api/rag/search` 查看。

## 已推荐数据源

| 数据源 | 机构 | 类型 | 适合用途 |
| --- | --- | --- | --- |
| WOAH Animal Diseases Portal | World Organisation for Animal Health | 权威疫病条目 | 建立水生动物法定疫病、宿主、病原和诊断手册索引 |
| WOAH Aquatic Animal Diseases Field Guide | World Organisation for Animal Health | 现场识别资料 | 补充外观症状、采样提醒、现场初筛和报告措辞 |
| WOAH Aquatic Manual / Aquatic Code | World Organisation for Animal Health | 诊断标准与国际规范 | 支撑采样、实验室检测、生物安全和贸易风险提醒 |
| WAHIS | World Organisation for Animal Health | 疫情事件数据库 | 补充全球疫情、地区分布、时间趋势和预警背景 |
| Aquatic Animal Health Status Reviews | USDA APHIS | 年度健康状态综述 | 补充重点病原监测、无疫状态和地区背景 |

## 建议导入字段

- `title`：资料标题或章节标题。
- `sourceName`：来源机构，例如 `WOAH`、`USDA APHIS`。
- `sourceUrl`：原始链接。
- `sourceType`：疫病条目、现场指南、标准手册、疫情数据库、监测综述等。
- `language`：资料语言。
- `category`：病毒病、细菌病、寄生虫、水质管理、采样规范等。
- `tags`：鱼种、病名、症状、病原、水质指标、地区。
- `summary`：人工审核后的 1-3 句摘要。
- `content`：用于检索和提示词的正文片段。

## 切分规则

1. 按“病害/章节/主题”切分，不要把整本手册塞进一个 chunk。
2. 每个 chunk 保留来源 URL、章节标题和适用范围。
3. 鉴别诊断、确诊方法、采样要求、用药限制要单独切分，便于问诊时精准召回。
4. 疫情数据必须标注国家/地区和报告时间，不能直接当成本地确诊依据。
5. 外文资料入库前建议保留英文标题，并补一段中文摘要，方便中文问诊检索。

## 后续增强路线

1. 先用当前 `rag_documents` 表做人工精选资料库。
2. 增加后台导入页面，支持 Markdown/CSV 批量入库。
3. 接入 embedding 服务，把 `rag_documents` 拆成 chunk 后写入向量库。
4. 诊断报告中展示“引用资料”和“为什么召回”，让结果可追溯。
