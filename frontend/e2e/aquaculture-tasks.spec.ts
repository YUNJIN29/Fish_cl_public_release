import { expect, test } from '@playwright/test'

test('authenticated user manages aquaculture tasks without overflow', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('token', 'task-test-token'))
  await page.route('**/api/auth/me', route => route.fulfill({ json: { success: true, message: 'ok', data: { id: 7, username: 'tester', roles: ['USER'] } } }))
  const items:any[]=[{id:1,title:'复测溶氧',category:'WATER_TEST',pondName:'一号池',dueAt:'2026-01-01T01:00:00Z',notes:'增氧后复测',completedAt:null,createdAt:'2026-01-01T00:00:00Z',updatedAt:'2026-01-01T00:00:00Z'}]
  let created:any
  let routinePayload:any
  let calendarExported=false
  await page.route('**/api/water-quality**', route => {
    const path=new URL(route.request().url()).pathname
    const data=path.endsWith('/ponds')?['一号池']:path.endsWith('/trend')||path.endsWith('/targets')||path.endsWith('/overview')?[]:{content:[],totalElements:0}
    return route.fulfill({json:{success:true,message:'ok',data}})
  })
  await page.route('**/api/tasks**', async route => {
    const method=route.request().method(),url=new URL(route.request().url())
    if(method==='GET'&&url.pathname.endsWith('/ponds')){const ponds=[...new Set(items.map(item=>item.pondName).filter(Boolean))];return route.fulfill({json:{success:true,message:'ok',data:ponds}})}
    if(method==='GET'&&url.pathname.endsWith('/calendar.ics')){calendarExported=true;return route.fulfill({status:200,contentType:'text/calendar;charset=UTF-8',headers:{'content-disposition':'attachment; filename="aquaculture-tasks.ics"'},body:'BEGIN:VCALENDAR\r\nVERSION:2.0\r\nEND:VCALENDAR\r\n'})}
    if(method==='GET'&&url.pathname.endsWith('/summary')){const now=Date.now(),soon=now+86400000,open=items.filter(item=>!item.completedAt);return route.fulfill({json:{success:true,message:'ok',data:{openCount:open.length,overdueCount:open.filter(item=>new Date(item.dueAt).getTime()<now).length,dueSoonCount:open.filter(item=>{const due=new Date(item.dueAt).getTime();return due>=now&&due<soon}).length,recurringCount:open.filter(item=>item.repeatDays).length}}})}
    if(method==='POST'&&url.pathname.endsWith('/routine-plan')){routinePayload=await route.request().postDataJSON();const routineItems=routinePayload.presets.map((preset:string,index:number)=>({id:20+index,title:preset==='OBSERVATION'?'巡塘观察':'检测水质',category:preset,pondName:routinePayload.pondName,dueAt:routinePayload.startAt,repeatDays:preset==='OBSERVATION'?1:7,completedAt:null,createdAt:new Date().toISOString(),updatedAt:new Date().toISOString()}));items.unshift(...routineItems);return route.fulfill({json:{success:true,message:'created',data:{created:routineItems,skippedPresets:[]}}})}
    if(method==='POST'){created=await route.request().postDataJSON();const item={...created,id:2,completedAt:null,createdAt:new Date().toISOString(),updatedAt:new Date().toISOString()};items.unshift(item);return route.fulfill({json:{success:true,message:'created',data:item}})}
    if(method==='PATCH'){const id=Number(url.pathname.split('/').slice(-2)[0]),payload=await route.request().postDataJSON(),item=items.find(value=>value.id===id);if(url.pathname.endsWith('/reschedule')){const currentDue=new Date(item.dueAt).getTime(),base=Math.max(currentDue,Date.now());item.dueAt=new Date(base+payload.days*86400000).toISOString();return route.fulfill({json:{success:true,message:'rescheduled',data:item}})}item.completedAt=payload.completed?new Date().toISOString():null;let nextDueAt=null;if(payload.completed&&item.repeatDays&&!item.nextTaskCreated){item.nextTaskCreated=true;const period=item.repeatDays*86400000;nextDueAt=new Date(new Date(item.dueAt).getTime()+period).getTime();while(nextDueAt<=Date.now())nextDueAt+=period;const nextDueIso=new Date(nextDueAt).toISOString();items.unshift({...item,id:3,dueAt:nextDueIso,completedAt:null,nextTaskCreated:false});nextDueAt=nextDueIso}return route.fulfill({json:{success:true,message:'updated',data:{...item,nextDueAt}}})}
    if(method==='DELETE'){const id=Number(url.pathname.split('/').pop()),index=items.findIndex(value=>value.id===id);if(index>=0)items.splice(index,1);return route.fulfill({json:{success:true,message:'deleted',data:null}})}
    const status=url.searchParams.get('status')||'OPEN',scope=url.searchParams.get('scope')||'ALL',now=Date.now(),soon=now+86400000
    const q=(url.searchParams.get('q')||'').toLowerCase(),pond=url.searchParams.get('pondName')||'',category=url.searchParams.get('category')||''
    const filtered=items.filter(item=>status==='ALL'||(status==='COMPLETED'?item.completedAt:!item.completedAt)).filter(item=>status!=='OPEN'||scope==='ALL'||(scope==='OVERDUE'&&new Date(item.dueAt).getTime()<now)||(scope==='DUE_SOON'&&new Date(item.dueAt).getTime()>=now&&new Date(item.dueAt).getTime()<soon)||(scope==='RECURRING'&&item.repeatDays)).filter(item=>(!q||`${item.title} ${item.notes||''}`.toLowerCase().includes(q))&&(!pond||item.pondName===pond)&&(!category||item.category===category))
    return route.fulfill({json:{success:true,message:'ok',data:{content:filtered,totalElements:filtered.length}}})
  })

  await page.goto('/fish/tasks')
  await expect(page.getByRole('heading',{name:'养殖待办'})).toBeVisible()
  await page.getByRole('button',{name:'快速建日常计划'}).click()
  await expect(page.getByRole('heading',{name:'快速建立日常计划'})).toBeVisible()
  await page.getByLabel('水体名称 *').first().fill('二号池')
  await page.locator('.preset-card input[value="FEEDING"]').check({force:true})
  await page.getByRole('button',{name:'创建 3 项计划'}).click()
  await expect.poll(()=>routinePayload?.pondName).toBe('二号池')
  expect(routinePayload.presets).toEqual(expect.arrayContaining(['FEEDING','OBSERVATION','WATER_TEST']))
  await expect(page.getByText('已创建 3 项日常计划')).toBeVisible()
  await expect(page.locator('.task-item').filter({hasText:'巡塘观察'})).toBeVisible()
  const summary=page.getByRole('region',{name:'待办概览'})
  await expect(summary).toContainText('全部待处理4')
  await expect(summary).toContainText('已逾期1')
  await page.getByLabel('搜索任务').fill('复测溶氧')
  await page.getByRole('button',{name:'筛选',exact:true}).click()
  await expect(page.getByText('找到 1 项符合条件的任务')).toBeVisible()
  await expect(page.locator('.task-item')).toHaveCount(1)
  await page.getByRole('button',{name:'重置'}).click()
  await expect(page.locator('.task-item')).toHaveCount(4)
  await page.getByRole('button',{name:'记录复测溶氧的检测结果'}).click()
  await expect(page).toHaveURL(url=>url.pathname==='/fish/water-quality'&&url.searchParams.get('pond')==='一号池'&&url.searchParams.get('taskId')==='1')
  await page.goBack()
  await expect(page.getByRole('heading',{name:'养殖待办'})).toBeVisible()
  const calendarDownload=page.waitForEvent('download')
  await page.getByRole('button',{name:'导出到日历'}).click()
  await calendarDownload
  expect(calendarExported).toBe(true)
  await expect(page.locator('.task-item .overdue-tag').first()).toBeVisible()
  await summary.getByRole('button',{name:/已逾期/}).click()
  await expect(page.getByRole('heading',{name:'逾期任务'})).toBeVisible()
  await expect(summary.getByRole('button',{name:/已逾期/})).toHaveAttribute('aria-pressed','true')
  await summary.getByRole('button',{name:/全部待处理/}).click()
  await page.getByRole('button',{name:'将复测溶氧顺延 1 天'}).click()
  await expect(summary).toContainText('已逾期0')
  expect(new Date(items.find(item=>item.id===1).dueAt).getTime()).toBeGreaterThan(Date.now())
  await page.getByLabel('事项名称 *').fill('检查投喂情况')
  await page.getByLabel('分类 *').selectOption('FEEDING')
  await page.getByLabel('关联水体').fill('育苗池')
  await page.getByLabel('重复周期（天）').fill('7')
  await page.getByRole('button',{name:'添加待办'}).click()
  await expect.poll(()=>created?.title).toBe('检查投喂情况')
  expect(created?.repeatDays).toBe(7)
  const createdCard=page.locator('.task-item').filter({hasText:'检查投喂情况'})
  await expect(createdCard).toBeVisible()
  await expect(createdCard).toContainText('每 7 天')
  await expect(summary).toContainText('周期任务4')
  await createdCard.getByRole('button',{name:'完成待办'}).click()
  await expect.poll(()=>items.filter(item=>item.title==='检查投喂情况').length).toBe(2)
  await expect(page.getByText(/下一期已安排在/)).toBeVisible()
  expect(new Date(items.find(item=>item.id===3).dueAt).getTime()).toBeGreaterThan(Date.now())
  await expect(page.locator('.task-item').filter({hasText:'检查投喂情况'})).toContainText('每 7 天')
  await page.getByRole('button',{name:'已完成'}).click()
  await expect(page.locator('.task-item').filter({hasText:'检查投喂情况'})).toBeVisible()
  expect(await page.evaluate(()=>document.documentElement.scrollWidth<=document.documentElement.clientWidth)).toBeTruthy()
})

test('anonymous task access redirects to login',async({page})=>{await page.goto('/fish/tasks');await expect(page).toHaveURL(url=>url.pathname==='/login'&&url.searchParams.get('redirect')==='/fish/tasks')})
