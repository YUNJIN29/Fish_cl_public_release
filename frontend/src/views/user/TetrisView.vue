<template>
  <section class="tetris-page" tabindex="0" @keydown="keyDown">
    <header><button @click="$router.push('/personal/games')">← 游戏大厅</button><div><h1>俄罗斯方块</h1><p>填满一行即可消除</p></div></header>
    <div class="game-wrap">
      <canvas ref="canvas" width="300" height="600"/>
      <aside><div class="stat"><span>分数</span><b>{{ score }}</b></div><div class="stat"><span>消除</span><b>{{ lines }}</b></div><div class="stat"><span>等级</span><b>{{ level }}</b></div><button class="primary" @click="toggle">{{ running ? '暂停' : (over ? '重新开始' : '开始') }}</button><button @click="restart">重开</button><p>← → 移动<br>↑ 旋转<br>↓ 加速<br>空格 直落<br>P 暂停</p></aside>
    </div>
    <div class="mobile"><button @click="move(-1)">←</button><button @click="rotate">↻</button><button @click="move(1)">→</button><button @click="drop">↓</button><button @click="hardDrop">⇊</button></div>
    <GameLeaderboard game="tetris" :score="score" />
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import GameLeaderboard from '@/components/GameLeaderboard.vue'
const W=10,H=20,S=30,canvas=ref<HTMLCanvasElement>(),score=ref(0),lines=ref(0),running=ref(false),over=ref(false),level=computed(()=>Math.floor(lines.value/10)+1)
const colors=['','#00d4ff','#7b6cff','#ffab40','#00e676','#ff5370','#ffc400','#46a0ff']
const shapes=[[[1,1,1,1]],[[2,0,0],[2,2,2]],[[0,0,3],[3,3,3]],[[4,4],[4,4]],[[0,5,5],[5,5,0]],[[0,6,0],[6,6,6]],[[7,7,0],[0,7,7]]]
let arena:number[][]=[],piece:number[][]=[],px=0,py=0,last=0,acc=0,raf=0
const blank=()=>Array.from({length:H},()=>Array(W).fill(0))
function spawn(){piece=shapes[Math.floor(Math.random()*shapes.length)].map(r=>[...r]);px=Math.floor((W-piece[0].length)/2);py=0;if(collide()){over.value=true;running.value=false}}
function collide(){return piece.some((r,y)=>r.some((v,x)=>v&&(arena[py+y]?.[px+x]??1))) }
function merge(){piece.forEach((r,y)=>r.forEach((v,x)=>{if(v)arena[py+y][px+x]=v}));let cleared=0;for(let y=H-1;y>=0;y--){if(arena[y].every(Boolean)){arena.splice(y,1);arena.unshift(Array(W).fill(0));cleared++;y++}}if(cleared){lines.value+=cleared;score.value+=[0,100,300,500,800][cleared]*level.value}spawn()}
function drop(){if(!running.value)return;py++;if(collide()){py--;merge()}acc=0;draw()}
function hardDrop(){if(!running.value)return;while(!collide())py++;py--;score.value+=2;merge();draw()}
function move(n:number){if(!running.value)return;px+=n;if(collide())px-=n;draw()}
function rotate(){if(!running.value)return;const old=piece,oldX=px;piece=piece[0].map((_,i)=>piece.map(r=>r[i]).reverse());for(const n of [0,-1,1,-2,2]){px=oldX+n;if(!collide()){draw();return}}piece=old;px=oldX}
function draw(){const c=canvas.value?.getContext('2d');if(!c)return;c.fillStyle=getComputedStyle(document.documentElement).getPropertyValue('--c-bg-card2');c.fillRect(0,0,300,600);c.strokeStyle='rgba(120,150,190,.12)';for(let x=0;x<=W;x++){c.beginPath();c.moveTo(x*S,0);c.lineTo(x*S,600);c.stroke()}for(let y=0;y<=H;y++){c.beginPath();c.moveTo(0,y*S);c.lineTo(300,y*S);c.stroke()}function cells(m:number[][],ox=0,oy=0){m.forEach((r,y)=>r.forEach((v,x)=>{if(v){c.fillStyle=colors[v];c.fillRect((x+ox)*S+2,(y+oy)*S+2,S-4,S-4);c.fillStyle='rgba(255,255,255,.18)';c.fillRect((x+ox)*S+4,(y+oy)*S+4,S-8,4)}}))}cells(arena);cells(piece,px,py);if(over.value){c.fillStyle='rgba(3,8,18,.78)';c.fillRect(0,245,300,110);c.fillStyle='#fff';c.textAlign='center';c.font='700 26px sans-serif';c.fillText('游戏结束',150,295);c.font='14px sans-serif';c.fillText('点击“重新开始”再来一局',150,325)}}
function loop(t=0){const dt=t-last;last=t;if(running.value){acc+=dt;if(acc>Math.max(130,800-level.value*55))drop()}draw();raf=requestAnimationFrame(loop)}
function restart(){arena=blank();score.value=0;lines.value=0;over.value=false;running.value=true;spawn();nextTick(()=>document.querySelector<HTMLElement>('.tetris-page')?.focus())}
function toggle(){if(over.value)restart();else running.value=!running.value}
function keyDown(e:KeyboardEvent){const keys=['ArrowLeft','ArrowRight','ArrowUp','ArrowDown',' ','p','P'];if(keys.includes(e.key))e.preventDefault();if(e.key==='ArrowLeft')move(-1);if(e.key==='ArrowRight')move(1);if(e.key==='ArrowUp')rotate();if(e.key==='ArrowDown')drop();if(e.key===' ')hardDrop();if(e.key.toLowerCase()==='p')toggle()}
onMounted(()=>{arena=blank();spawn();loop()});onBeforeUnmount(()=>cancelAnimationFrame(raf))
</script>

<style scoped>
.tetris-page{max-width:690px;margin:auto;outline:none;padding:12px 0 40px}header{display:flex;align-items:center;gap:25px;margin-bottom:18px}button{border:1px solid var(--c-border);border-radius:8px;background:var(--c-bg-card);color:var(--c-text-2);padding:10px 14px;cursor:pointer}h1{margin:0;color:var(--c-text);font-size:28px}header p{margin:4px 0;color:var(--c-text-2)}.game-wrap{display:flex;justify-content:center;gap:22px}canvas{width:300px;height:600px;border:1px solid var(--c-border);border-radius:10px;box-shadow:0 16px 45px rgba(0,0,0,.28)}aside{width:150px;display:flex;flex-direction:column;gap:10px}.stat{background:var(--c-bg-card);border:1px solid var(--c-border);border-radius:10px;padding:13px}.stat span{display:block;color:var(--c-text-3);font-size:12px}.stat b{font-size:24px;color:var(--c-cyan)}aside .primary{background:var(--c-cyan);color:#031018;border-color:transparent;font-weight:700}aside p{font-size:12px;line-height:2;color:var(--c-text-3)}.mobile{display:none;grid-template-columns:repeat(5,1fr);gap:7px;margin:12px auto 0;max-width:300px}.mobile button{font-size:20px;padding:10px 0}@media(max-width:600px){.game-wrap{gap:10px;align-items:flex-start}canvas{width:250px;height:500px}aside{width:82px}.stat{padding:8px}.stat b{font-size:18px}aside button{padding:8px 3px}aside p{display:none}.mobile{display:grid}header{flex-wrap:wrap;gap:10px}header button{width:100%;text-align:left}}
</style>
