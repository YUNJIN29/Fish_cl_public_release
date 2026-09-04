<template>
  <section ref="gameEl" class="snake-game" tabindex="0">
    <header><button @click="$router.push('/personal/games')">← 游戏大厅</button><h1>贪吃蛇</h1><b>得分 {{ score }}　最高 {{ best }}</b></header>
    <div class="stage" @touchstart.passive="touchStart" @touchend.passive="touchEnd">
      <canvas ref="cv" width="400" height="400"/>
      <div v-if="!run" class="cover"><h2>{{ over ? '游戏结束' : started ? '已暂停' : '贪吃蛇' }}</h2><button @click="start">{{ over ? '再来一局' : started ? '继续游戏' : '开始游戏' }}</button></div>
    </div>
    <div class="pad"><button @click="turn(0,-1)">↑</button><div><button @click="turn(-1,0)">←</button><button @click="turn(0,1)">↓</button><button @click="turn(1,0)">→</button></div></div>
    <p>方向键 / WASD 控制，空格暂停；手机可滑动或点击方向按钮</p>
    <GameLeaderboard game="snake" :score="score" />
  </section>
</template>
<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
const cv=ref<HTMLCanvasElement>(),gameEl=ref<HTMLElement>(),score=ref(0),best=ref(+(localStorage.getItem('snake-best')||0)),run=ref(false),over=ref(false),started=ref(false)
let s:{x:number,y:number}[]=[],f={x:15,y:10},dx=1,dy=0,nx=1,ny=0,t=0,sx=0,sy=0
function food(){do{f={x:~~(Math.random()*20),y:~~(Math.random()*20)}}while(s.some(p=>p.x===f.x&&p.y===f.y))}
function start(){if(started.value&&!over.value){run.value=true;timer();focus();return}s=[{x:7,y:10},{x:6,y:10},{x:5,y:10}];dx=nx=1;dy=ny=0;score.value=0;over.value=false;started.value=true;run.value=true;food();timer();draw();focus()}
function timer(){clearInterval(t);t=window.setInterval(step,125)}function focus(){nextTick(()=>gameEl.value?.focus())}
function turn(x:number,y:number){if(!run.value)return;if(x!==-dx||y!==-dy){nx=x;ny=y}focus()}
function step(){dx=nx;dy=ny;const h={x:s[0].x+dx,y:s[0].y+dy};if(h.x<0||h.x>19||h.y<0||h.y>19||s.some(p=>p.x===h.x&&p.y===h.y)){run.value=false;over.value=true;clearInterval(t);draw();return}s.unshift(h);if(h.x===f.x&&h.y===f.y){score.value+=10;best.value=Math.max(best.value,score.value);localStorage.setItem('snake-best',String(best.value));food()}else s.pop();draw()}
function draw(){const c=cv.value?.getContext('2d');if(!c)return;c.fillStyle='#eef4fb';c.fillRect(0,0,400,400);c.strokeStyle='#dfe8f2';for(let i=0;i<=20;i++){c.beginPath();c.moveTo(i*20,0);c.lineTo(i*20,400);c.stroke();c.beginPath();c.moveTo(0,i*20);c.lineTo(400,i*20);c.stroke()}c.fillStyle='#ff5370';c.beginPath();c.arc(f.x*20+10,f.y*20+10,7,0,7);c.fill();s.forEach((p,i)=>{c.fillStyle=i?'#19bd70':'#00e676';c.fillRect(p.x*20+1,p.y*20+1,18,18)})}
function key(e:KeyboardEvent){const k=e.key.toLowerCase(),m:Record<string,[number,number]>={arrowup:[0,-1],w:[0,-1],arrowdown:[0,1],s:[0,1],arrowleft:[-1,0],a:[-1,0],arrowright:[1,0],d:[1,0]};if(m[k]){e.preventDefault();turn(...m[k])}if(e.code==='Space'&&started.value&&!over.value){e.preventDefault();run.value=!run.value;run.value?timer():clearInterval(t);focus()}}
function touchStart(e:TouchEvent){sx=e.touches[0].clientX;sy=e.touches[0].clientY}function touchEnd(e:TouchEvent){const x=e.changedTouches[0].clientX-sx,y=e.changedTouches[0].clientY-sy;if(Math.max(Math.abs(x),Math.abs(y))<20)return;Math.abs(x)>Math.abs(y)?turn(x>0?1:-1,0):turn(0,y>0?1:-1)}
onMounted(()=>{s=[{x:7,y:10},{x:6,y:10},{x:5,y:10}];draw();window.addEventListener('keydown',key,{passive:false})});onBeforeUnmount(()=>{clearInterval(t);window.removeEventListener('keydown',key)})
</script>
<style scoped>.snake-game{max-width:520px;margin:auto;text-align:center;outline:none}.snake-game header{display:flex;align-items:center;justify-content:space-between;margin:15px 0}.snake-game button{border:1px solid var(--c-border);background:var(--c-bg-card);color:var(--c-text-2);border-radius:8px;padding:9px 13px}.snake-game h1{color:var(--c-text)}.stage{max-width:400px;aspect-ratio:1;margin:auto;position:relative;touch-action:none}.stage canvas{width:100%;height:100%;border:1px solid var(--c-border);border-radius:12px}.cover{position:absolute;inset:0;display:grid;place-content:center;background:#07101d99;color:white;border-radius:12px}.cover button{background:var(--c-green);color:#031b0d;font-weight:700}.pad{display:none;margin:10px}.pad>button{display:block;margin:0 auto 5px}.snake-game p{color:var(--c-text-3)}@media(max-width:600px){.pad{display:block}.snake-game header{flex-wrap:wrap}.snake-game header button{width:100%;text-align:left}}</style>
