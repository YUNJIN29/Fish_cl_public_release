<template>
  <main class="profile-public">
    <nav class="profile-nav">
      <button type="button" @click="$router.push('/')">← 返回入口</button>
      <b>Aquatic Disease Knowledge Platform</b>
    </nav>

    <section class="hero">
      <div>
        <p class="eyebrow">PUBLIC SANITIZED EDITION</p>
        <h1>项目展示页</h1>
        <p>
          公开版本仅保留项目能力说明和模块结构，已移除个人简历、证书、联系方式、备案信息和私有部署资料。
        </p>
      </div>
      <div class="mark">AD</div>
    </section>

    <section class="grid">
      <article>
        <span>01</span>
        <h2>水产病害诊断</h2>
        <p>整合症状记录、环境信息、病害知识库和 AI 辅助分析，生成结构化诊断建议。</p>
      </article>
      <article>
        <span>02</span>
        <h2>知识图谱与 RAG</h2>
        <p>将病害、症状、病原、鱼种和处理建议关联展示，并提供资料片段召回。</p>
      </article>
      <article>
        <span>03</span>
        <h2>养殖记录工具</h2>
        <p>支持水质日志、养殖待办、诊断随访、日历导出和个人中心记录管理。</p>
      </article>
    </section>
  </main>
</template>

<style scoped>
.profile-public{max-width:1120px;margin:auto;padding:28px 16px 80px;color:var(--c-text)}
.profile-nav{min-height:56px;display:flex;align-items:center;justify-content:space-between;gap:12px}
.profile-nav button{min-height:40px;padding:0 14px;border:1px solid var(--c-border);border-radius:8px;background:var(--c-bg-card);color:var(--c-text-2);cursor:pointer}
.profile-nav b{font-size:14px;color:var(--c-text-2)}
.hero{min-height:360px;display:grid;grid-template-columns:minmax(0,1fr) 220px;align-items:center;gap:34px;margin-top:18px;padding:42px;border:1px solid var(--c-border);border-radius:18px;background:radial-gradient(circle at 82% 22%,rgba(0,212,255,.14),transparent 34%),var(--c-bg-card)}
.eyebrow{margin:0 0 12px;color:var(--c-cyan);font:800 11px ui-monospace;letter-spacing:.18em}
h1{margin:0 0 16px;font-size:clamp(38px,6vw,70px);line-height:1.05}
.hero p:last-child{max-width:680px;color:var(--c-text-2);line-height:1.9}
.mark{width:180px;height:180px;display:grid;place-items:center;border-radius:28px;background:linear-gradient(135deg,var(--c-purple),var(--c-cyan));color:white;font-size:54px;font-weight:900;box-shadow:0 24px 70px rgba(0,212,255,.18)}
.grid{display:grid;grid-template-columns:repeat(3,1fr);gap:14px;margin-top:18px}
article{padding:22px;border:1px solid var(--c-border);border-radius:14px;background:var(--c-bg-card)}
article span{color:var(--c-purple);font:800 11px ui-monospace}
article h2{margin:10px 0;color:var(--c-text)}
article p{margin:0;color:var(--c-text-2);line-height:1.8}
@media(max-width:760px){.hero{grid-template-columns:1fr;padding:26px}.mark{display:none}.grid{grid-template-columns:1fr}}
</style>
