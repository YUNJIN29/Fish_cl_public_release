<template>
  <RouterView />
  <SitePet />
  <CommandPalette />
  <InstallPrompt />
</template>

<script setup lang="ts">
import { useThemeStore } from '@/stores/theme'
import CommandPalette from '@/components/CommandPalette.vue'
import InstallPrompt from '@/components/InstallPrompt.vue'
import SitePet from '@/components/SitePet.vue'
// 初始化主题（store 内部 watch 会立即应用）
useThemeStore()
</script>

<style>
/* ===== 暗色主题变量（默认 / html.dark） ===== */
:root,
html.dark {
  --font-sans: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
  --el-font-family: var(--font-sans);
  --c-bg:        #070c18;
  --c-bg-card:   #0d1426;
  --c-bg-card2:  #101930;
  --c-bg-hover:  #14203a;
  --c-border:    #1a2a4a;
  --c-border-l:  #0f1e38;
  --c-cyan:      #00d4ff;
  --c-cyan-dim:  rgba(0,212,255,0.12);
  --c-cyan-glow: rgba(0,212,255,0.25);
  --c-purple:    #7b6cff;
  --c-green:     #00e676;
  --c-amber:     #ffab40;
  --c-red:       #ff5370;
  --c-text:      #dce8ff;
  --c-text-2:    #7a96be;
  --c-text-3:    #3d5270;
}

/* ===== 亮色主题变量（html 无 .dark 时覆盖） ===== */
html:not(.dark) {
  --c-bg:        #f0f4fa;
  --c-bg-card:   #ffffff;
  --c-bg-card2:  #f7f9fd;
  --c-bg-hover:  #edf2fb;
  --c-border:    #d4dff0;
  --c-border-l:  #e0e8f4;
  --c-cyan:      #0090c8;
  --c-cyan-dim:  rgba(0,144,200,0.08);
  --c-cyan-glow: rgba(0,144,200,0.2);
  --c-purple:    #6355e8;
  --c-green:     #10b866;
  --c-amber:     #e09400;
  --c-red:       #e53056;
  --c-text:      #0f1e35;
  --c-text-2:    #526080;
  --c-text-3:    #8fa3bf;
}

/* ===== 亮色 Element Plus 变量覆盖 ===== */
html:not(.dark) {
  --el-bg-color:               #ffffff;
  --el-bg-color-page:          #f0f4fa;
  --el-bg-color-overlay:       #ffffff;
  --el-border-color:           #d4dff0;
  --el-border-color-light:     #dce8f5;
  --el-border-color-lighter:   #e4edf8;
  --el-text-color-primary:     #0f1e35;
  --el-text-color-regular:     #526080;
  --el-text-color-secondary:   #8fa3bf;
  --el-text-color-placeholder: #b0c0d8;
  --el-color-primary:          #0090c8;
  --el-color-primary-light-3:  rgba(0,144,200,0.6);
  --el-color-primary-light-5:  rgba(0,144,200,0.4);
  --el-color-primary-light-7:  rgba(0,144,200,0.2);
  --el-color-primary-light-8:  rgba(0,144,200,0.1);
  --el-color-primary-light-9:  rgba(0,144,200,0.05);
  --el-color-primary-dark-2:   #007aaa;
  --el-card-bg-color:          #ffffff;
  --el-fill-color:             #f5f8ff;
  --el-fill-color-light:       #f7faff;
  --el-fill-color-blank:       #ffffff;
  --el-box-shadow:             0 2px 16px rgba(0,80,150,0.08);
  --el-box-shadow-light:       0 1px 8px rgba(0,80,150,0.06);
}

/* ===== 亮色 body ===== */
html:not(.dark) body {
  background: #f0f4fa;
  color: #0f1e35;
  font-family: var(--font-sans);
}

/* ===== 亮色导航 header ===== */
html:not(.dark) .header {
  --header-bg: rgba(240,244,250,0.96) !important;
  box-shadow: 0 1px 12px rgba(0,80,150,0.07) !important;
}
html:not(.dark) .brand-text {
  background: linear-gradient(135deg, #0077aa, #0090c8) !important;
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
}
html:not(.dark) .brand-icon { color: #0090c8 !important; filter: none !important; animation: none !important; }
html:not(.dark) .brand-badge { color: #0090c8 !important; border-color: rgba(0,144,200,0.4) !important; }
html:not(.dark) .footer {
  background: rgba(240,244,250,0.9) !important;
  border-top: 1px solid #d4dff0 !important;
}

/* ===== 亮色 scrollbar ===== */
html:not(.dark) ::-webkit-scrollbar { width: 5px; height: 5px; }
html:not(.dark) ::-webkit-scrollbar-track { background: #f0f4fa; }
html:not(.dark) ::-webkit-scrollbar-thumb { background: #c0cfe8; border-radius: 3px; }
html:not(.dark) ::-webkit-scrollbar-thumb:hover { background: #0090c8; }

/* ===== 亮色卡片 ===== */
html:not(.dark) .el-card {
  background: #ffffff;
  border: 1px solid #d4dff0;
  border-radius: 10px !important;
  box-shadow: 0 2px 16px rgba(0,80,150,0.07) !important;
}
html:not(.dark) .el-card:hover {
  border-color: rgba(0,144,200,0.4);
  box-shadow: 0 4px 20px rgba(0,144,200,0.1) !important;
}
html:not(.dark) .el-card__header { border-bottom: 1px solid #d4dff0; }

/* ===== 亮色输入框 ===== */
html:not(.dark) .el-input__wrapper {
  background: #f5f8ff !important;
  box-shadow: 0 0 0 1px #d4dff0 inset !important;
}
html:not(.dark) .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #0090c8 inset !important;
}
html:not(.dark) .el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px #0090c8 inset, 0 0 8px rgba(0,144,200,0.12) !important;
}
html:not(.dark) .el-textarea__inner {
  background: #f5f8ff !important;
  border: 1px solid #d4dff0 !important;
  color: #0f1e35 !important;
}
html:not(.dark) .el-textarea__inner:focus {
  border-color: #0090c8 !important;
}

/* ===== 亮色菜单 ===== */
html:not(.dark) .el-menu { background: transparent !important; border: none !important; }
html:not(.dark) .el-menu--horizontal { border-bottom: none !important; }
html:not(.dark) .el-menu--horizontal > .el-menu-item { color: #526080 !important; }
html:not(.dark) .el-menu--horizontal > .el-menu-item:hover,
html:not(.dark) .el-menu--horizontal > .el-menu-item.is-active {
  color: #0090c8 !important; background: transparent !important;
  border-bottom-color: #0090c8 !important;
}
html:not(.dark) .el-menu--horizontal > .el-sub-menu .el-sub-menu__title { color: #526080 !important; }
html:not(.dark) .el-menu--horizontal > .el-sub-menu:hover .el-sub-menu__title,
html:not(.dark) .el-menu--horizontal > .el-sub-menu.is-active .el-sub-menu__title {
  color: #0090c8 !important;
}
html:not(.dark) .el-menu--popup {
  background: #ffffff !important;
  border: 1px solid #d4dff0 !important;
  box-shadow: 0 4px 16px rgba(0,80,150,0.1) !important;
}
html:not(.dark) .el-menu--popup .el-menu-item { color: #526080 !important; }
html:not(.dark) .el-menu--popup .el-menu-item:hover {
  background: rgba(0,144,200,0.06) !important; color: #0090c8 !important;
}

/* ===== 亮色按钮 ===== */
html:not(.dark) .el-button--primary {
  background: linear-gradient(135deg, #0077aa, #0090c8) !important;
  border-color: transparent !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(0,144,200,0.3);
}
html:not(.dark) .el-button--primary:hover { box-shadow: 0 4px 14px rgba(0,144,200,0.45); filter: brightness(1.08); }
html:not(.dark) .el-button--link { color: #0090c8 !important; }

/* ===== 亮色 select/dropdown ===== */
html:not(.dark) .el-select-dropdown {
  background: #ffffff !important; border: 1px solid #d4dff0 !important;
  box-shadow: 0 4px 16px rgba(0,80,150,0.1) !important;
}
html:not(.dark) .el-select-dropdown__item { color: #526080 !important; }
html:not(.dark) .el-select-dropdown__item:hover,
html:not(.dark) .el-select-dropdown__item.is-hovering {
  background: rgba(0,144,200,0.06) !important; color: #0090c8 !important;
}
html:not(.dark) .el-select-dropdown__item.is-selected { color: #0090c8 !important; }

/* ===== 亮色 tag ===== */
html:not(.dark) .el-tag { background: rgba(0,144,200,0.08); border-color: rgba(0,144,200,0.25); color: #0077aa; }
html:not(.dark) .el-tag--info { background: rgba(82,96,128,0.07); border-color: #d4dff0; color: #526080; }

/* ===== 亮色分页 ===== */
html:not(.dark) .el-pagination .el-pager li { background: #fff; border: 1px solid #d4dff0; color: #526080; }
html:not(.dark) .el-pagination .el-pager li.is-active { background: #0090c8; color: #fff; border-color: #0090c8; }
html:not(.dark) .el-pagination button { background: #fff; border: 1px solid #d4dff0; color: #526080; }

/* ===== 亮色 descriptions ===== */
html:not(.dark) .el-descriptions__label { background: #f5f8ff !important; color: #526080 !important; border-color: #d4dff0 !important; }
html:not(.dark) .el-descriptions__content { background: #fff !important; color: #0f1e35 !important; border-color: #d4dff0 !important; }
html:not(.dark) .el-descriptions__body { background: #fff !important; border-color: #d4dff0 !important; }

/* ===== Element Plus 暗色变量覆盖 ===== */
html.dark {
  --el-bg-color:               #0d1426;
  --el-bg-color-page:          #070c18;
  --el-bg-color-overlay:       #111e35;
  --el-border-color:           #1a2a4a;
  --el-border-color-light:     #132038;
  --el-border-color-lighter:   #0e1830;
  --el-border-color-extra-light: #0a1228;
  --el-text-color-primary:     #dce8ff;
  --el-text-color-regular:     #96aec8;
  --el-text-color-secondary:   #5a7090;
  --el-text-color-placeholder: #3d5270;
  --el-color-primary:          #00d4ff;
  --el-color-primary-light-3:  rgba(0,212,255,0.6);
  --el-color-primary-light-5:  rgba(0,212,255,0.4);
  --el-color-primary-light-7:  rgba(0,212,255,0.2);
  --el-color-primary-light-8:  rgba(0,212,255,0.12);
  --el-color-primary-light-9:  rgba(0,212,255,0.06);
  --el-color-primary-dark-2:   #33dcff;
  --el-card-bg-color:          #0d1426;
  --el-fill-color:             #101930;
  --el-fill-color-light:       #0d1b30;
  --el-fill-color-blank:       #0d1426;
  --el-mask-color:             rgba(4,6,14,0.85);
  --el-box-shadow:             0 4px 24px rgba(0,0,0,0.6);
  --el-box-shadow-light:       0 2px 12px rgba(0,0,0,0.4);
  --el-disabled-bg-color:      #0a1020;
  --el-disabled-border-color:  #0f1a30;
  --el-disabled-text-color:    #3d5270;
}

/* ===== 全局基础 ===== */
*, *::before, *::after { box-sizing: border-box; }

html.dark body {
  background: var(--c-bg);
  color: var(--c-text);
  font-family: var(--font-sans);
}

/* scrollbar */
html.dark ::-webkit-scrollbar { width: 5px; height: 5px; }
html.dark ::-webkit-scrollbar-track { background: var(--c-bg); }
html.dark ::-webkit-scrollbar-thumb { background: var(--c-border); border-radius: 3px; }
html.dark ::-webkit-scrollbar-thumb:hover { background: var(--c-cyan); }

/* ===== 卡片 ===== */
html.dark .el-card {
  background: var(--c-bg-card);
  border: 1px solid var(--c-border);
  border-radius: 10px !important;
  box-shadow: 0 4px 20px rgba(0,0,0,0.4) !important;
  transition: border-color 0.25s, box-shadow 0.25s;
}
html.dark .el-card:hover {
  border-color: rgba(0,212,255,0.3);
  box-shadow: 0 4px 28px rgba(0,212,255,0.08) !important;
}
html.dark .el-card__header {
  border-bottom: 1px solid var(--c-border);
  padding: 14px 20px;
}
html.dark .el-card__body { color: var(--c-text); }

/* ===== 输入框 ===== */
html.dark .el-input__wrapper {
  background: #0a1020 !important;
  box-shadow: 0 0 0 1px var(--c-border) inset !important;
  border-radius: 6px;
  transition: box-shadow 0.2s;
}
html.dark .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px rgba(0,212,255,0.4) inset !important;
}
html.dark .el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px var(--c-cyan) inset, 0 0 10px rgba(0,212,255,0.15) !important;
}
html.dark .el-input__inner { color: var(--c-text) !important; background: transparent !important; }
html.dark .el-textarea__inner {
  background: #0a1020 !important;
  border: 1px solid var(--c-border) !important;
  color: var(--c-text) !important;
  border-radius: 6px;
}
html.dark .el-textarea__inner:focus {
  border-color: var(--c-cyan) !important;
  box-shadow: 0 0 10px rgba(0,212,255,0.15);
}

/* ===== Select ===== */
html.dark .el-select .el-input__wrapper { background: #0a1020 !important; }
html.dark .el-select-dropdown { background: #0d1426 !important; border: 1px solid var(--c-border) !important; }
html.dark .el-select-dropdown__item { color: var(--c-text-2) !important; }
html.dark .el-select-dropdown__item:hover,
html.dark .el-select-dropdown__item.is-hovering { background: var(--c-cyan-dim) !important; color: var(--c-cyan) !important; }
html.dark .el-select-dropdown__item.is-selected { color: var(--c-cyan) !important; }

/* ===== 按钮 ===== */
html.dark .el-button--default {
  background: #0a1020;
  border-color: var(--c-border);
  color: var(--c-text-2);
}
html.dark .el-button--default:hover {
  background: var(--c-cyan-dim);
  border-color: var(--c-cyan);
  color: var(--c-cyan);
}
html.dark .el-button--primary {
  background: linear-gradient(135deg, #0090b8, #00d4ff) !important;
  border-color: transparent !important;
  color: #030c14 !important;
  font-weight: 600;
  box-shadow: 0 0 12px rgba(0,212,255,0.3);
}
html.dark .el-button--primary:hover {
  box-shadow: 0 0 20px rgba(0,212,255,0.5);
  filter: brightness(1.1);
}
html.dark .el-button[disabled] { opacity: 0.4 !important; }
html.dark .el-button--link { color: var(--c-cyan) !important; }
html.dark .el-button--link:hover { color: #33e4ff !important; }

/* ===== Tag ===== */
html.dark .el-tag {
  background: rgba(0,212,255,0.08);
  border-color: rgba(0,212,255,0.2);
  color: var(--c-cyan);
}
html.dark .el-tag--info {
  background: rgba(122,150,190,0.1);
  border-color: var(--c-border);
  color: var(--c-text-2);
}
html.dark .el-tag--danger {
  background: rgba(255,83,112,0.1);
  border-color: rgba(255,83,112,0.3);
  color: #ff5370;
}
html.dark .el-tag--warning {
  background: rgba(255,171,64,0.1);
  border-color: rgba(255,171,64,0.3);
  color: #ffab40;
}
html.dark .el-tag--success {
  background: rgba(0,230,118,0.1);
  border-color: rgba(0,230,118,0.3);
  color: #00e676;
}

/* ===== 表格 ===== */
html.dark .el-table {
  background: transparent !important;
  color: var(--c-text) !important;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: #0a1424;
  --el-table-border-color: var(--c-border);
  --el-table-text-color: var(--c-text);
  --el-table-header-text-color: var(--c-cyan);
  --el-table-row-hover-bg-color: var(--c-bg-hover);
}
html.dark .el-table__header th { font-weight: 600; letter-spacing: 0.5px; }

/* ===== 分页 ===== */
html.dark .el-pagination { --el-pagination-bg-color: #0a1020; --el-pagination-button-bg-color: #0a1020; }
html.dark .el-pagination .el-pager li { background: #0a1020; border: 1px solid var(--c-border); color: var(--c-text-2); }
html.dark .el-pagination .el-pager li.is-active { background: var(--c-cyan); color: #030c14; border-color: var(--c-cyan); font-weight: 700; }
html.dark .el-pagination button { background: #0a1020; border: 1px solid var(--c-border); color: var(--c-text-2); }

/* ===== 菜单 ===== */
html.dark .el-menu { background: transparent !important; border: none !important; }
html.dark .el-menu--horizontal { border-bottom: none !important; }
html.dark .el-menu--horizontal > .el-menu-item {
  color: var(--c-text-2) !important;
  border-bottom: 2px solid transparent !important;
  transition: color 0.2s, border-color 0.2s;
}
html.dark .el-menu--horizontal > .el-menu-item:hover,
html.dark .el-menu--horizontal > .el-menu-item.is-active {
  color: var(--c-cyan) !important;
  background: transparent !important;
  border-bottom-color: var(--c-cyan) !important;
}
html.dark .el-menu--horizontal > .el-sub-menu .el-sub-menu__title {
  color: var(--c-text-2) !important;
}
html.dark .el-menu--horizontal > .el-sub-menu:hover .el-sub-menu__title,
html.dark .el-menu--horizontal > .el-sub-menu.is-active .el-sub-menu__title {
  color: var(--c-cyan) !important;
  border-bottom-color: var(--c-cyan) !important;
}
html.dark .el-menu--popup {
  background: #0d1426 !important;
  border: 1px solid var(--c-border) !important;
}
html.dark .el-menu--popup .el-menu-item {
  color: var(--c-text-2) !important;
}
html.dark .el-menu--popup .el-menu-item:hover {
  background: var(--c-cyan-dim) !important;
  color: var(--c-cyan) !important;
}

/* ===== Dropdown ===== */
html.dark .el-dropdown-menu { background: #0d1426 !important; border: 1px solid var(--c-border) !important; }
html.dark .el-dropdown-menu__item { color: var(--c-text-2) !important; }
html.dark .el-dropdown-menu__item:hover { background: var(--c-cyan-dim) !important; color: var(--c-cyan) !important; }

/* ===== Skeleton ===== */
html.dark .el-skeleton__item { background: linear-gradient(90deg, #0d1426 25%, #1a2a4a 50%, #0d1426 75%) !important; background-size: 400% 100% !important; }

/* ===== descriptions ===== */
html.dark .el-descriptions { --el-descriptions-item-bordered-label-background: #0a1424; }
html.dark .el-descriptions__label { color: var(--c-text-2) !important; background: #0a1424 !important; border-color: var(--c-border) !important; }
html.dark .el-descriptions__content { color: var(--c-text) !important; background: var(--c-bg-card) !important; border-color: var(--c-border) !important; }
html.dark .el-descriptions__body { background: var(--c-bg-card) !important; border-color: var(--c-border) !important; }

/* ===== Avatar ===== */
html.dark .el-avatar { background: #1a2a4a; }

/* ===== Empty ===== */
html.dark .el-empty__description { color: var(--c-text-2); }

/* ===== Page header ===== */
html.dark .el-page-header__left .el-icon { color: var(--c-cyan); }
html.dark .el-page-header__breadcrumb { color: var(--c-text-2); }

/* 入口页备案信息 */
.shell footer a { color:inherit; text-decoration:none; }
.shell footer a:hover { color:var(--c-cyan); }
.portal footer .site-notice { max-width:920px; margin:14px auto 0; padding:12px 18px; color:#203d5d; font-size:14px; font-weight:800; line-height:1.8; background:#e8f3fb; border:1px solid #b9dced; border-left:5px solid #0797c7; border-radius:8px; }
.portal .footer-line { display:flex; justify-content:center; gap:8px; flex-wrap:wrap; letter-spacing:0; }
.portal .footer-line a { color:#71869e; text-decoration:none; }
.portal .footer-line a:hover { color:#00d4ff; }
.portal footer p { max-width:920px; margin:8px auto 0; color:#40546b; line-height:1.7; letter-spacing:0; }
.games-page .snake,.games-page .xo{display:grid;place-items:center;font-size:26px;font-weight:800;line-height:1.7}.games-page .snake span{color:var(--c-green);letter-spacing:4px}.games-page .snake b{color:var(--c-red);margin-left:100px}.games-page .xo span{color:var(--c-purple)}
.games-page .casual-icon{display:grid;place-items:center;font-size:48px;letter-spacing:8px}
.portal .ranking{max-width:760px;margin:0 auto 8px;padding:18px 22px;background:#fff;border:1px solid #dbe5f1;border-radius:16px}.portal .ranking-title{text-align:center;margin-bottom:12px}.portal .ranking-title span{font-size:9px;letter-spacing:3px;color:#7667ed}.portal .ranking-title h3{margin:5px 0;font-size:18px}.portal .ranking-list{display:grid;gap:5px}.portal .rank-row{display:grid;grid-template-columns:28px 120px 1fr 65px;align-items:center;gap:10px;padding:7px 10px;border-radius:8px;background:#f7f9fd;font-size:12px}.portal .rank-row>b{color:#7667ed}.portal .rank-row code{color:#37536f}.portal .rank-row span{color:#71869d;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.portal .rank-row strong{text-align:right;color:#087eb4}.portal .ranking-list>p{text-align:center;color:#71869d}@media(max-width:720px){.portal .ranking{padding:15px 10px}.portal .rank-row{grid-template-columns:22px 92px 1fr 52px;gap:5px;padding:7px 5px;font-size:11px}}

/* ===== 通用工具 ===== */
html, body, #app { min-height: 100%; margin: 0; }
.gradient-text {
  background: linear-gradient(135deg, var(--c-cyan), var(--c-purple));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.glow-border {
  border: 1px solid var(--c-cyan) !important;
  box-shadow: 0 0 12px rgba(0,212,255,0.2) !important;
}
.cyber-grid-bg {
  background-image:
    linear-gradient(rgba(0,212,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,212,255,0.03) 1px, transparent 1px);
  background-size: 40px 40px;
}
</style>
