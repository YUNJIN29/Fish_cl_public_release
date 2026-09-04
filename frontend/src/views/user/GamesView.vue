<template>
  <section class="games-page">
    <div class="hero">
      <span class="eyebrow">MINI ARCADE</span>
      <h1>休闲游戏</h1>
      <p>学习之余，来一局轻松一下。</p>
    </div>
    <div class="game-grid">
      <article class="game-card game-1024" @click="$router.push('/personal/games/1024')">
        <div class="preview tiles"><i>2</i><i>4</i><i>8</i><i>16</i></div>
        <div><span class="tag">数字益智</span><h2>1024</h2><p>滑动合并相同数字，挑战 1024 与更高分数。</p></div>
        <button>开始游戏 →</button>
      </article>
      <article class="game-card game-tetris" @click="$router.push('/personal/games/tetris')">
        <div class="preview blocks"><i/><i/><i/><i/><i/><i/><i/></div>
        <div><span class="tag">经典街机</span><h2>俄罗斯方块</h2><p>旋转、移动、消除方块，看看你能坚持多久。</p></div>
        <button>开始游戏 →</button>
      </article>
      <article class="game-card" @click="$router.push('/personal/games/snake')">
        <div class="preview snake"><span>● ● ● ●</span><b>●</b></div>
        <div><span class="tag">轻松休闲</span><h2>贪吃蛇</h2><p>控制小蛇吃掉果实，灵活转向，挑战更高分数。</p></div>
        <button>开始游戏 →</button>
      </article>
      <article class="game-card" @click="$router.push('/personal/games/tic-tac-toe')">
        <div class="preview xo"><span>×　○　×<br>○　×　○<br>○　×　·</span></div>
        <div><span class="tag">双人益智</span><h2>井字棋</h2><p>经典三连棋，支持双人对战与电脑对战。</p></div>
        <button>开始游戏 →</button>
      </article>
      <article class="game-card" @click="$router.push('/personal/games/minesweeper')"><div class="preview casual-icon">💣</div><div><span class="tag">逻辑推理</span><h2>扫雷</h2><p>根据数字找出所有安全格，小心隐藏的地雷。</p></div><button>开始游戏 →</button></article>
      <article class="game-card" @click="$router.push('/personal/games/memory')"><div class="preview casual-icon">🃏</div><div><span class="tag">记忆挑战</span><h2>记忆翻牌</h2><p>翻开卡片找到相同图案，挑战更少步数。</p></div><button>开始游戏 →</button></article>
      <article class="game-card" @click="$router.push('/personal/games/whack-a-mole')"><div class="preview casual-icon">🐹</div><div><span class="tag">反应速度</span><h2>打地鼠</h2><p>在倒计时结束前击中更多地鼠，挑战手速。</p></div><button>开始游戏 →</button></article>
      <article class="game-card" @click="$router.push('/personal/games/rps')"><div class="preview casual-icon">✊ ✌️ ✋</div><div><span class="tag">轻松对战</span><h2>石头剪刀布</h2><p>与电脑进行五局三胜对战，看看谁更幸运。</p></div><button>开始游戏 →</button></article>
    </div>
  </section>
</template>

<style scoped>
.games-page{padding:22px 0 48px}.hero{text-align:center;padding:38px 20px 44px}.eyebrow{font-size:11px;letter-spacing:4px;color:var(--c-cyan)}h1{font-size:38px;margin:10px 0 8px;color:var(--c-text)}.hero p,.game-card p{color:var(--c-text-2)}.game-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:24px;max-width:900px;margin:auto}.game-card{position:relative;overflow:hidden;min-height:350px;padding:28px;border:1px solid var(--c-border);border-radius:18px;background:var(--c-bg-card);cursor:pointer;transition:.25s;display:flex;flex-direction:column}.game-card:hover{transform:translateY(-5px);border-color:var(--c-cyan);box-shadow:0 18px 50px var(--c-cyan-glow)}.preview{height:150px;border-radius:13px;background:var(--c-bg-card2);margin-bottom:24px}.tiles{display:grid;grid-template-columns:repeat(2,64px);gap:8px;place-content:center}.tiles i{height:64px;display:grid;place-items:center;border-radius:9px;background:linear-gradient(135deg,var(--c-cyan),var(--c-purple));color:white;font-style:normal;font-size:22px;font-weight:800}.blocks{position:relative;display:grid;grid-template-columns:repeat(4,34px);grid-template-rows:repeat(3,34px);place-content:center}.blocks i{border-radius:4px;background:var(--c-cyan);box-shadow:inset 0 0 0 3px rgba(255,255,255,.18)}.blocks i:nth-child(1){grid-column:2}.blocks i:nth-child(2){grid-column:2}.blocks i:nth-child(3){grid-column:3}.blocks i:nth-child(4){grid-column:4}.blocks i:nth-child(5){grid-column:1}.blocks i:nth-child(6){grid-column:2}.blocks i:nth-child(7){grid-column:3}.tag{font-size:11px;color:var(--c-cyan);letter-spacing:1px}.game-card h2{font-size:25px;margin:8px 0;color:var(--c-text)}.game-card p{line-height:1.7;flex:1}.game-card button{align-self:flex-start;border:0;background:none;color:var(--c-cyan);font-weight:700;cursor:pointer;padding:10px 0}@media(max-width:700px){.game-grid{grid-template-columns:1fr}.games-page{padding-top:0}.hero{padding:22px 12px}.game-card{min-height:320px}}
</style>
