export type SiteDestination = {
  title: string
  description: string
  path: string
  icon: string
  keywords: string
  category: '个人' | '游戏' | '水产智诊平台'
}

export const siteDestinations: SiteDestination[] = [
  { title: '门户首页', description: '从这里浏览全部空间', path: '/', icon: '云', keywords: '首页 门户 home yunjin', category: '个人' },
  { title: '个人简介', description: '经历、能力与个人定位', path: '/profile', icon: '简', keywords: '个人 简历 简介 经历 求职 resume profile', category: '个人' },
  { title: '项目成果', description: '查看职责、技术选择和项目证明', path: '/profile#projects', icon: '项', keywords: '项目 作品 技术 project portfolio', category: '个人' },
  { title: '荣誉与证书', description: '竞赛、资质与可验证材料', path: '/profile#proofs', icon: '证', keywords: '证书 荣誉 获奖 比赛 proof certificate', category: '个人' },
  { title: '游戏中心', description: '进入互动游戏与排行榜', path: '/personal/games', icon: '游', keywords: '游戏 休闲 排行榜 game 2048 俄罗斯方块 贪吃蛇', category: '游戏' },
  { title: '水产智诊平台', description: '水产智能平台主入口', path: '/fish', icon: '鱼', keywords: '水产 水产 智养 fish 养殖', category: '水产智诊平台' },
  { title: 'AI 智能诊断', description: '根据症状和环境信息辅助判断', path: '/fish/diagnose', icon: '诊', keywords: '诊断 症状 图片 AI disease diagnose', category: '水产智诊平台' },
  { title: '病害知识库', description: '分类检索病害与处理建议', path: '/fish/diseases', icon: '知', keywords: '知识库 病害 病毒 细菌 disease knowledge', category: '水产智诊平台' },
  { title: 'RAG 资料检索', description: '查看召回片段、来源和相关度', path: '/fish/rag', icon: '检', keywords: 'RAG 检索 资料 来源 文档 search', category: '水产智诊平台' },
  { title: '知识图谱', description: '探索病害、症状和鱼种关联', path: '/fish/knowledge-graph', icon: '图', keywords: '图谱 关联 节点 graph knowledge', category: '水产智诊平台' },
  { title: '水体用量计算器', description: '换算水体体积、标签剂量与投放量', path: '/fish/calculator', icon: '算', keywords: '计算器 水体 体积 用量 剂量 浓度 mg g calculator dosage', category: '水产智诊平台' },
  { title: '水质日志', description: '持续记录水温、pH、溶氧、氨氮和亚硝酸盐', path: '/fish/water-quality', icon: '水', keywords: '水质 日志 记录 pH 溶氧 氨氮 亚硝酸盐 water quality log', category: '水产智诊平台' },
  { title: '养殖待办', description: '安排投喂、检测、换水和复查事项', path: '/fish/tasks', icon: '办', keywords: '待办 计划 投喂 换水 检测 用药 提醒 task todo schedule', category: '水产智诊平台' },
  { title: '社区内容', description: '阅读和交流养殖经验', path: '/fish/posts', icon: '帖', keywords: '社区 帖子 交流 post forum', category: '水产智诊平台' },
  { title: '科普文章', description: '浏览水产养殖科普内容', path: '/fish/articles', icon: '文', keywords: '文章 科普 内容 article', category: '水产智诊平台' },
]

export function findDestination(path: string) {
  return siteDestinations.find((item) => item.path === path || item.path.split('#')[0] === path)
}
