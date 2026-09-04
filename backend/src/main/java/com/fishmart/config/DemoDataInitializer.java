package com.fishmart.config;

import com.fishmart.catalog.*;
import com.fishmart.content.*;
import com.fishmart.user.Role;
import com.fishmart.user.RoleName;
import com.fishmart.user.RoleRepository;
import com.fishmart.user.User;
import com.fishmart.user.UserRepository;
import com.fishmart.user.UserStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DemoDataInitializer implements CommandLineRunner {
  private final CategoryRepository categoryRepo;
  private final ProductRepository productRepo;
  private final ProductImageRepository productImageRepo;
  private final ArticleRepository articleRepo;
  private final PostRepository postRepo;
  private final UserRepository userRepo;
  private final RoleRepository roleRepo;

  public DemoDataInitializer(CategoryRepository categoryRepo,
                             ProductRepository productRepo,
                             ProductImageRepository productImageRepo,
                             ArticleRepository articleRepo,
                             PostRepository postRepo,
                             UserRepository userRepo,
                             RoleRepository roleRepo) {
    this.categoryRepo = categoryRepo;
    this.productRepo = productRepo;
    this.productImageRepo = productImageRepo;
    this.articleRepo = articleRepo;
    this.postRepo = postRepo;
    this.userRepo = userRepo;
    this.roleRepo = roleRepo;
  }

  @Override
  @Transactional
  public void run(String... args) {
    cleanupLegacyDemoText();
    seedCatalog();
    seedContent();
    normalizeImageUrls();
  }

  private void cleanupLegacyDemoText() {
    // 强制清理数据库中可能存在的旧版演示文本
    String professionalDesc = "本产品为演示数据，展示用途、库存、价格和标签等字段，适用于公开版本的功能预览。";
    for (Product p : productRepo.findAll()) {
      if (p.getDescription() != null && (p.getDescription().contains("演示商品") || p.getDescription().contains("答辩展示"))) {
        p.setDescription(professionalDesc);
        p.setName(p.getName().replace("甄选款", "优质选品"));
        productRepo.save(p);
      }
    }
  }

  private void seedCatalog() {
    // 若为空则创建一套；若已有数据则“补齐图片”让页面看起来像真网站
    if (categoryRepo.count() == 0 || productRepo.count() == 0) {
      Category c1 = makeCategory("鱼药/治理", 10);
      Category c2 = makeCategory("水质调节", 9);
      Category c3 = makeCategory("增氧/过滤", 8);
      Category c4 = makeCategory("优质饲料", 7);
      Category c5 = makeCategory("鱼苗/活体", 6);
      Category c6 = makeCategory("水族器材", 5);
      categoryRepo.saveAll(List.of(c1, c2, c3, c4, c5, c6));

      Product p1 = makeProduct(c1, "复合黄粉（广谱抗菌）", "适用于各种细菌性病害，如烂鳍、烂尾、炸鳞等。", 2500, 100, pickCover(1L), "烂鳍,烂尾,炸鳞,细菌感染", "建议每100升水投放2-3克，连用3天，期间加强氧气。");
      Product p2 = makeProduct(c1, "水霉特效杀菌剂", "针对白毛、水霉等真菌感染有奇效。", 3800, 80, pickCover(2L), "白毛,水霉,棉絮状物", "全池泼洒，次日换水1/3效果更佳。");
      Product p3 = makeProduct(c2, "高活性硝化细菌", "快速建立生化过滤系统，降解氨氮亚硝酸盐。", 4500, 200, pickCover(3L), "水质混浊,氨氮偏高", "新缸开缸必备，每周定期添加，维护水体平衡。");
      Product p4 = makeProduct(c4, "增红高蛋白饲料", "天然虾红素添加，促进鱼体增色，营养均衡。", 5800, 150, pickCover(4L), "食欲减退,体色暗淡", "少食多餐，每日投喂2-3次，以5分钟内吃完为宜。");
      Product p5 = makeProduct(c3, "超静音增氧泵", "低分贝运行，双孔大排气量，保障溶氧充足。", 12800, 30, pickCover(5L), "浮头,缺氧", "24小时开启，定期检查气石是否堵塞。");
      Product p6 = makeProduct(c6, "多功能不锈钢镊子", "水草造景及鱼病手术辅助工具。", 1500, 50, pickCover(6L), null, "使用后请擦干存放，避免酸性水质长期浸泡。");
      productRepo.saveAll(List.of(p1, p2, p3, p4, p5, p6));
    }

    // 为没有主图/多图的商品补齐图片
    for (Product p : productRepo.findAll()) {
      if (p.getCoverImageUrl() == null || p.getCoverImageUrl().isBlank()) {
        p.setCoverImageUrl(pickCover(p.getId()));
        productRepo.save(p);
      }
      if (productImageRepo.findByProductIdOrderBySortOrderAscIdAsc(p.getId()).isEmpty()) {
        attachImages(p, pickGallery(p.getId()));
      }
    }

    // 至少两页：默认列表 size=10，这里确保商品 >= 22
    long count = productRepo.count();
    if (count < 22) {
      List<Category> cats = categoryRepo.findAll();
      if (cats.isEmpty()) return;
      int need = (int) (22 - count);
      for (int i = 1; i <= need; i++) {
        Category c = cats.get((i - 1) % cats.size());
        String name = c.getName() + " 优质选品-" + i;
        Product p = makeProduct(
            c,
            name,
            "本产品为演示数据，展示用途、库存、价格和标签等字段，适用于公开版本的功能预览。",
            1290 + (i % 10) * 300,
            20 + (i % 15),
            pickCover((long) (1000 + i)),
            null,
            "通用养殖建议：保持水质稳定，定期观察鱼类摄食情况。"
        );
        p = productRepo.save(p);
        attachImages(p, pickGallery((long) (1000 + i)));
      }
    }
  }

  private void seedContent() {
    if (articleRepo.count() < 5) {
      articleRepo.deleteAll();
      articleRepo.saveAll(List.of(
          makeArticle("AI 赋能智慧水产：从传统养殖到智能诊断的跨越", 
              "<h2>一、智慧水产的新篇章</h2><p>随着人工智能技术的飞速发展，传统水产养殖正迎来数字化转型。通过 <strong>AI 智能诊断系统</strong>，养殖户只需通过手机拍摄病鱼照片或描述症状，即可在数秒内获得初步诊断建议。</p>" +
              "<h2>二、智能诊断的核心优势</h2><p>1. <strong>快速响应</strong>：24小时在线，解决专家难找、响应慢的问题。<br>2. <strong>精准识别</strong>：基于深度学习模型，系统能识别数十种常见鱼类病害，准确率不断提升。<br>3. <strong>闭环服务</strong>：不仅提供诊断，还能联动知识图谱给出详细的治疗方案与预防措施。</p>" +
              "<h2>三、未来展望</h2><p>平台可继续扩展水产健康管理能力，将传感器监测数据与 AI 诊断深度结合，实现从“病后治疗”到“病前预防”的跨越。</p>", pickArticleCover(1)),
          
          makeArticle("病害知识图谱：如何通过可视化技术追踪疾病传播", 
              "<h2>一、什么是知识图谱？</h2><p>知识图谱（Knowledge Graph）是一种揭示实体之间复杂关系的语义网络。在水产病害场景中，可以将<strong>病害、症状、治疗药物、发病季节、受灾物种</strong>等实体互联，形成一张可检索的知识网络。</p>" +
              "<h2>二、图谱在病害防治中的应用</h2><p>通过可视化技术，养殖户可以直观地看到某种病害（如草鱼出血病）关联的多个症状，以及其在不同温度下的爆发概率。这种全局视角有助于及时切断传播链，制定科学的防疫计划。</p>" +
              "<h2>三、多维数据联动</h2><p>我们的图谱集成了大连海洋大学多年的科研数据，支持缩放、拖拽探索，是养殖户和科研人员不可多得的知识宝库。</p>", pickArticleCover(2)),
          
          makeArticle("春季鱼病高发期：关键预防措施与水质调控建议", 
              "<h2>一、春季养殖面临的挑战</h2><p>随着气温回升，水体中的致病菌和寄生虫开始活跃。此时鱼类刚度过漫长的冬季，体质较弱，极易受到感染。</p>" +
              "<h2>二、核心预防策略</h2><p>1. <strong>水质净化</strong>：及时清理池底有机物，适量使用微生物制剂调理水质。<br>2. <strong>营养增强</strong>：换用高品质开口料，适量添加维生素 C 和免疫增强剂，提高鱼类抵抗力。<br>3. <strong>科学投喂</strong>：遵循“四定”原则（定时、定位、定质、定量），避免残饵污染水体。</p>" +
              "<h2>三、养殖小贴士</h2><p>若发现鱼类出现离群、打转或摄食量骤减，请立即使用首页的<strong>AI 智能诊断</strong>功能进行排查。</p>", pickArticleCover(3)),
          
          makeArticle("灯科鱼混养指南：构建和谐的微景观生态", 
              "<h2>一、混养的基本原则</h2><p>灯科鱼（如红绿灯、宝莲灯）性格温顺，体型娇小，适合构建色彩斑斓的社区缸。混养时应遵循<strong>体型相近、习性兼容、水质一致</strong>的原则。</p>" +
              "<h2>二、推荐混养方案</h2><p>1. <strong>群游组合</strong>：红绿灯 + 蓝三角灯，形成错落有致的视觉层次。<br>2. <strong>功能鱼搭配</strong>：搭配鼠鱼或小精灵鱼，负责清理残饵和藻类。<br>3. <strong>上中下层分布</strong>：上层孔雀鱼，中层灯鱼，下层鲃鱼，充分利用水体空间。</p>" +
              "<h2>三、注意事项</h2><p>避免与大型凶猛鱼类（如地图鱼、罗汉鱼）混养，以免成为后者的“点心”。</p>", pickArticleCover(4)),
          
          makeArticle("锦鲤饲养：水质过滤系统的深度解析", 
              "<h2>一、锦鲤对水质的要求</h2><p>锦鲤食量大、排泄多，对氧气需求极高。一个高效的过滤系统是养好锦鲤的命脉。理想的锦鲤缸应实现<strong>物理过滤、生物过滤与化学过滤</strong>的完美平衡。</p>" +
              "<h2>二、过滤仓的科学布局</h2><p>1. <strong>第一级：物理仓</strong>，使用毛刷或细棉拦截大颗粒杂质。<br>2. <strong>第二级：生化仓</strong>，填充大量生化球或细菌屋，培养硝化细菌分解氨氮。<br>3. <strong>第三级：清水仓</strong>，可放置紫外线杀菌灯，抑制绿藻生长。</p>" +
              "<h2>三、日常维护建议</h2><p>每周坚持更换 1/5 的水体，并定期检查滤材积污情况，切忌一次性大规模清洗所有生化滤材。</p>", pickArticleCover(5))
      ));
    }

    // 补充后续文章，确保数据量充足
    long aCount = articleRepo.count();
    if (aCount < 15) {
      int need = (int) (15 - aCount);
      for (int i = 1; i <= need; i++) {
        Article a = makeArticle(
            "智慧渔业深度观察：" + (100 + i) + " 号专栏文章",
            "<p>这是平台演示用的深度科普内容。我们通过资料整理，总结了水产养殖中的典型案例，旨在提供可落地的技术指导。</p>" +
            "<p>本文重点探讨了<strong>水体溶解氧监测</strong>与<strong>自动化投喂系统</strong>的协同工作原理，帮助您在降低人工成本的同时提升产量。</p>",
            pickArticleCover(100 + i)
        );
        articleRepo.save(a);
      }
    }

    if (postRepo.count() == 0) {
      User demo = userRepo.findByUsername("demo-author").orElseGet(() -> {
        Role userRole = roleRepo.findByName(RoleName.USER).orElseThrow();
        User u = new User();
        u.setUsername("demo-author");
        u.setNickname("水产养殖达人");
        u.setPasswordHash("{noop}disabled-demo-author");
        u.setStatus(UserStatus.BANNED);
        u.getRoles().add(userRole);
        return userRepo.save(u);
      });

      postRepo.saveAll(List.of(
          makePost(demo, "开缸第7天：水还发白怎么办？", "过滤棉与硝化细菌建立需要时间，建议减少喂食、加强曝气..."),
          makePost(demo, "孔雀鱼小鱼怎么养成活率高？", "小鱼隔离、微量多餐、保持水温稳定，注意吸鱼风险..."),
          makePost(demo, "锦鲤缸滤材怎么搭配？", "粗滤→生化→精滤的思路，生化仓尽量大，避免频繁清洗...")
      ));
    }
  }

  private Category makeCategory(String name, int sort) {
    Category c = new Category();
    c.setName(name);
    c.setSortOrder(sort);
    return c;
  }

  private Product makeProduct(Category c, String name, String desc, int priceCents, int stock, String coverImageUrl, String symptomTags, String usageAdvice) {
    Product p = new Product();
    p.setCategory(c);
    p.setName(name);
    p.setDescription(desc);
    p.setPriceCents(priceCents);
    p.setStock(stock);
    p.setCoverImageUrl(coverImageUrl);
    p.setSymptomTags(symptomTags);
    p.setUsageAdvice(usageAdvice);
    p.setStatus(ProductStatus.ON);
    return p;
  }

  private Article makeArticle(String title, String content, String coverImageUrl) {
    Article a = new Article();
    a.setTitle(title);
    a.setCoverImageUrl(coverImageUrl);
    a.setContent(content);
    a.setStatus(ContentStatus.APPROVED);
    return a;
  }

  private Post makePost(User u, String title, String content) {
    Post p = new Post();
    p.setUser(u);
    p.setTitle(title);
    p.setContent(content);
    p.setStatus(ContentStatus.APPROVED);
    return p;
  }

  private void attachImages(Product p, List<String> urls) {
    int i = 0;
    for (String url : urls) {
      ProductImage img = new ProductImage();
      img.setProduct(p);
      img.setImageUrl(url);
      img.setSortOrder(i++);
      productImageRepo.save(img);
    }
  }

  private String pickCover(Long id) {
    long n = id == null ? 0 : id;
    int k = (int) Math.abs(n % 6);
    return switch (k) {
      case 0 -> "/uploads/real_fish_1.jpg";
      case 1 -> "/uploads/real_fish_2.jpg";
      case 2 -> "/uploads/real_fish_3.jpg";
      case 3 -> "/uploads/real_fish_4.jpg";
      case 4 -> "/uploads/real_fish_5.jpg";
      default -> "/uploads/real_fish_6.jpg";
    };
  }

  private List<String> pickGallery(Long id) {
    long n = id == null ? 0 : id;
    String c1 = pickCover(n);
    String c2 = pickCover(n + 1);
    return List.of(c1, c2);
  }

  private String pickArticleCover(int i) {
    int k = Math.abs(i % 3);
    return switch (k) {
      case 0 -> "/uploads/real_aqua_1.jpg";
      case 1 -> "/uploads/real_aqua_2.jpg";
      default -> "/uploads/real_aqua_3.jpg";
    };
  }

  private void normalizeImageUrls() {
    for (Product p : productRepo.findAll()) {
      String u = p.getCoverImageUrl();
      if (u == null || u.isBlank() || !u.startsWith("/uploads/real_")) {
        p.setCoverImageUrl(pickCover(p.getId()));
        productRepo.save(p);
      }
      List<ProductImage> imgs = productImageRepo.findByProductIdOrderBySortOrderAscIdAsc(p.getId());
      for (ProductImage img : imgs) {
        String iu = img.getImageUrl();
        if (iu == null || iu.isBlank() || !iu.startsWith("/uploads/real_")) {
          img.setImageUrl(pickCover((img.getId() == null ? 0 : img.getId()) + 10));
          productImageRepo.save(img);
        }
      }
    }
    for (Article a : articleRepo.findAll()) {
      String u = a.getCoverImageUrl();
      if (u == null || u.isBlank() || !u.startsWith("/uploads/real_")) {
        a.setCoverImageUrl(pickArticleCover((int) (a.getId() == null ? 0 : a.getId())));
        articleRepo.save(a);
      }
    }
  }
}

