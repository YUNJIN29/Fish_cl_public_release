<template>
  <section class="game-page" tabindex="0" @keydown="onKey">
    <header><button class="back" @click="$router.push('/personal/games')">← 游戏大厅</button><div><h1>1024</h1><p>合并数字，抵达 1024</p></div><div class="stats"><b>{{ score }}</b><span>分数</span><b>{{ best }}</b><span>最高</span></div></header>
    <div class="toolbar"><button @click="newGame">重新开始</button><span>{{ message }}</span></div>
    <div class="board" @touchstart="touchStart" @touchend="touchEnd">
      <div v-for="(value,i) in board.flat()" :key="i" class="cell" :class="`v${value}`">{{ value || '' }}</div>
    </div>
    <p class="hint">电脑：方向键 / WASD　手机：在棋盘上滑动</p>
    <GameLeaderboard game="1024" :score="score" />
  </section>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import GameLeaderboard from '@/components/GameLeaderboard.vue'
const size=4
const board=ref<number[][]>([]),score=ref(0),best=ref(Number(localStorage.getItem('game1024-best')||0)),message=ref('')
let sx=0,sy=0
const empty=()=>Array.from({length:size},()=>Array(size).fill(0))
function add(){const spots:number[][]=[];board.value.forEach((r,y)=>r.forEach((v,x)=>{if(!v)spots.push([y,x])}));if(spots.length){const [y,x]=spots[Math.floor(Math.random()*spots.length)];board.value[y][x]=Math.random()<.9?2:4}}
function newGame(){board.value=empty();score.value=0;message.value='';add();add();nextTick(()=>document.querySelector<HTMLElement>('.game-page')?.focus())}
function compress(line:number[]){const nums=line.filter(Boolean),out:number[]=[];for(let i=0;i<nums.length;i++){if(nums[i]===nums[i+1]){const v=nums[i]*2;out.push(v);score.value+=v;i++}else out.push(nums[i])}return [...out,...Array(size-out.length).fill(0)]}
function move(dir:string){if(message.value)return;const before=JSON.stringify(board.value);let b=board.value.map(r=>[...r]);if(dir==='left')b=b.map(compress);if(dir==='right')b=b.map(r=>compress([...r].reverse()).reverse());if(dir==='up'||dir==='down'){for(let x=0;x<size;x++){let col=b.map(r=>r[x]);if(dir==='down')col.reverse();col=compress(col);if(dir==='down')col.reverse();col.forEach((v,y)=>b[y][x]=v)}}board.value=b;if(JSON.stringify(b)!==before){add();if(score.value>best.value){best.value=score.value;localStorage.setItem('game1024-best',String(best.value))}}if(b.flat().includes(1024))message.value='🎉 你达成了 1024！';else if(!canMove())message.value='本局结束，再试一次吧'}
function canMove(){if(board.value.flat().includes(0))return true;return board.value.some((r,y)=>r.some((v,x)=>r[x+1]===v||board.value[y+1]?.[x]===v))}
function onKey(e:KeyboardEvent){const map:Record<string,string>={ArrowLeft:'left',a:'left',ArrowRight:'right',d:'right',ArrowUp:'up',w:'up',ArrowDown:'down',s:'down'};if(map[e.key]){e.preventDefault();move(map[e.key])}}
function touchStart(e:TouchEvent){sx=e.touches[0].clientX;sy=e.touches[0].clientY}function touchEnd(e:TouchEvent){const dx=e.changedTouches[0].clientX-sx,dy=e.changedTouches[0].clientY-sy;if(Math.max(Math.abs(dx),Math.abs(dy))<25)return;move(Math.abs(dx)>Math.abs(dy)?(dx>0?'right':'left'):(dy>0?'down':'up'))}
onMounted(newGame)
</script>

<style scoped>
.game-page{max-width:620px;margin:auto;outline:none;padding:14px 0 40px}header{display:flex;align-items:center;justify-content:space-between;margin-bottom:20px}.back,.toolbar button{border:1px solid var(--c-border);border-radius:8px;background:var(--c-bg-card);color:var(--c-text-2);padding:9px 13px;cursor:pointer}h1{font-size:40px;line-height:1;margin:0;color:var(--c-cyan)}header p{margin:5px 0;color:var(--c-text-2)}.stats{display:grid;grid-template-columns:repeat(2,58px);text-align:center;gap:2px 8px}.stats b{color:var(--c-text);font-size:20px}.stats span{grid-row:2;color:var(--c-text-3);font-size:11px}.toolbar{display:flex;justify-content:space-between;align-items:center;min-height:38px;margin-bottom:12px;color:var(--c-amber)}.board{display:grid;grid-template-columns:repeat(4,1fr);gap:10px;background:var(--c-bg-card2);border:1px solid var(--c-border);padding:12px;border-radius:16px;touch-action:none}.cell{aspect-ratio:1;display:grid;place-items:center;border-radius:10px;background:var(--c-bg-hover);color:var(--c-text);font-weight:800;font-size:clamp(20px,5vw,34px);transition:.15s}.v2{background:#e8f7fb;color:#385766}.v4{background:#ccecf5;color:#284b5c}.v8{background:#64d7ef;color:#052632}.v16{background:#22b9db;color:white}.v32{background:#7b6cff;color:white}.v64{background:#5948e4;color:white}.v128{background:#ffab40;color:#382000}.v256{background:#ff873d;color:white}.v512{background:#ff5370;color:white}.v1024,.v2048,.v4096{background:linear-gradient(135deg,#ffd740,#ff5370);color:white;box-shadow:0 0 20px #ffab4088}.hint{text-align:center;color:var(--c-text-3);font-size:13px;margin-top:18px}@media(max-width:600px){header{flex-wrap:wrap;gap:14px}.back{width:100%;text-align:left}.board{gap:7px;padding:8px}}
</style>
