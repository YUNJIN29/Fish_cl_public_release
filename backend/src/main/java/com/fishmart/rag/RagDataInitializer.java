package com.fishmart.rag;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(30)
public class RagDataInitializer implements CommandLineRunner {
  private final RagDocumentRepository repository;

  public RagDataInitializer(RagDocumentRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) {
    if (repository.count() > 0) return;

    repository.saveAll(List.of(
        doc(
            "WOAH 水生动物疫病门户：法定疫病与诊断章节索引",
            "WOAH",
            "https://www.woah.org/en/what-we-do/animal-health-and-welfare/animal-diseases/",
            "权威疫病条目",
            "English",
            "国际规范",
            "WOAH,水生动物,法定疫病,诊断手册,国际规范,鱼病,虾病",
            "WOAH 动物疫病门户提供陆生和水生动物疫病条目，包含 WOAH listed diseases、宿主类群以及 Aquatic Code / Aquatic Manual 章节链接，适合作为 RAG 的高可信疾病索引。",
            "用于问诊时，优先把 WOAH 条目作为“是否属于国际关注疫病、是否需要实验室确认、是否涉及报告或检疫”的依据。对鱼类相关条目，可索引 epizootic haematopoietic necrosis、infection with Gyrodactylus salaris、infectious salmon anaemia、koi herpesvirus disease、spring viraemia of carp、viral haemorrhagic septicaemia 等。输出时应提醒：现场肉眼观察只能形成疑似判断，确诊需要按 Aquatic Manual 进行采样和实验室检测。"
        ),
        doc(
            "WOAH 水生动物疾病现场指南：外观症状、采样和报告",
            "WOAH",
            "https://www.woah.org/en/oie-work/aquatic-animals/",
            "现场指南",
            "English",
            "现场识别",
            "WOAH,现场指南,外观症状,采样,报告,生物安全,鉴别诊断",
            "WOAH Aquatic Animal Diseases Field Guide 面向渔业和水产养殖管理者、现场工作人员、兽医服务和水生动物健康人员，强调典型外观症状、照片辅助识别、采样清单和报告流程。",
            "问诊系统引用该类资料时，应把“外观症状”作为初筛线索，而不是最终诊断。报告中要区分疑似诊断、鉴别诊断和确诊需求。若用户描述大量死亡、跨场传播、异常出血、鳃部坏死或虾类急性死亡，应提示隔离、停止转运、加强增氧、保存样品并联系当地水产技术员或监管机构。"
        ),
        doc(
            "WOAH Aquatic Manual / Code：诊断、采样和贸易风险规范",
            "WOAH",
            "https://www.woah.org/en/what-we-do/standards/codes-and-manuals/",
            "标准手册",
            "English",
            "诊断标准",
            "WOAH,Aquatic Manual,Aquatic Code,采样,实验室检测,生物安全",
            "WOAH Aquatic Code 和 Aquatic Manual 提供水生动物健康、国际贸易、疫病诊断方法和标准化检测框架，是 RAG 中“确诊方法”和“规范提醒”的基础资料。",
            "诊断报告应结合用户提供的鱼种、水温、水质、死亡率和症状，给出样品采集建议，如病鱼和濒死鱼优先、同时记录水温溶氧 pH 氨氮亚硝酸盐。若疑似病毒病，应避免承诺特效药，强调检疫、隔离、消毒、减应激和实验室检测。若涉及食用鱼用药，应提醒遵守当地法规、标签剂量和休药期。"
        ),
        doc(
            "WAHIS：全球动物卫生疫情事件数据库",
            "WOAH",
            "https://wahis.woah.org/",
            "疫情数据库",
            "English",
            "流行病学",
            "WAHIS,疫情,地理分布,水生动物,预警,监测",
            "WAHIS 是 WOAH 的全球动物卫生参考数据库，收录成员和非成员国家地区报告并验证的陆生和水生动物 listed diseases、 emerging diseases 及相关事件。",
            "RAG 使用 WAHIS 时，适合回答“某病是否在某地区近期出现、是否属于跨境传播风险、是否需要提高检疫等级”。问诊报告中不要把全球疫情数据直接等同于本地确诊，只能作为背景风险。建议按病名、宿主、国家/地区、报告时间切分并定期更新。"
        ),
        doc(
            "USDA APHIS 水生动物健康状态综述",
            "USDA APHIS",
            "https://www.aphis.usda.gov/livestock-poultry-disease/aquaculture/health-status-reviews",
            "监测综述",
            "English",
            "监测与地区背景",
            "APHIS,水生动物健康,病原监测,WOAH,年度综述",
            "USDA APHIS Aquatic Animal Health Status Reviews 基于 WOAH 疾病监测标准、病原检测数据和监管条件，对重点水生动物病原健康状态进行年度综述。",
            "该资料适合作为 RAG 的监测和地区背景来源，尤其用于鲑鳟、鲤科和重要跨境病原的风险解释。用于中国养殖场景时，应明确其地区适用性限制，不能替代本地监测数据。"
        ),
        doc(
            "草鱼出血病问诊要点",
            "水产病害知识库",
            "internal://diseases/grass-carp-hemorrhage",
            "本地知识条目",
            "中文",
            "鱼类病毒病",
            "草鱼,出血,充血,病毒,高温,疫苗,死亡",
            "草鱼出血病常见于草鱼鱼种阶段，体表、鳍基、口腔、眼眶或肌肉充血发红，水温 27~30°C 风险较高。",
            "问诊时重点追问死亡数量、发病速度、水温、放养密度、近期拉网运输和是否新进苗种。处理上以隔离、增氧、减少应激、稳定水质和疫苗预防为核心。发病后不应承诺病毒病特效药，可建议联系技术员送检，排查细菌性败血症、赤皮病、寄生虫叮咬等鉴别诊断。"
        ),
        doc(
            "白点病与水霉病鉴别",
            "水产病害知识库",
            "internal://diseases/white-spot-saprolegnia",
            "本地知识条目",
            "中文",
            "鱼类寄生虫与真菌",
            "白点,白毛,水霉,蹭缸,低温,温差,体表",
            "白点病多表现为体表、鳍条或鳃部芝麻粒样白点并伴随蹭缸；水霉病多在伤口处形成棉絮状菌丝，常与低温和机械损伤有关。",
            "问诊时应追问白色病灶形态、是否棉絮状、是否呈颗粒点状、是否有近期降温或拉网损伤。处理时先稳定水温和水质，隔离病鱼，避免盲目混用杀虫剂和消毒剂。若鳃部也有白点并呼吸急促，应提高风险分级并建议镜检确认。"
        ),
        doc(
            "烂鳃、浮头与水质应激问诊要点",
            "水产病害知识库",
            "internal://diseases/gill-water-quality",
            "本地知识条目",
            "中文",
            "鳃病与水质",
            "烂鳃,浮头,缺氧,氨氮,亚硝酸盐,鳃丝,水质",
            "浮头、鳃丝腐烂、鳃部黏液增多可能来自细菌性烂鳃、寄生虫感染或水质应激，需要同时检查溶氧、氨氮、亚硝酸盐和 pH。",
            "报告应优先提示增氧和水质检测。凌晨或阴雨天浮头更支持缺氧；鳃丝灰白腐烂、鳃盖张开、食欲下降更支持烂鳃或寄生虫。若氨氮或亚硝酸盐异常，应先少量多次换水、减料、补充有益菌或解毒调水，避免在低氧状态下重药刺激。"
        )
    ));
  }

  private RagDocument doc(String title, String sourceName, String sourceUrl, String sourceType,
                          String language, String category, String tags, String summary, String content) {
    RagDocument doc = new RagDocument();
    doc.setTitle(title);
    doc.setSourceName(sourceName);
    doc.setSourceUrl(sourceUrl);
    doc.setSourceType(sourceType);
    doc.setLanguage(language);
    doc.setCategory(category);
    doc.setTags(tags);
    doc.setSummary(summary);
    doc.setContent(content);
    return doc;
  }
}
