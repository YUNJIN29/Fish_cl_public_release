import { expect, test } from '@playwright/test'

const graph = {
  categories: [{ name: '病害' }, { name: '症状' }, { name: '鱼种' }],
  nodes: [
    { id: 'd1', name: '白点病', category: 0, value: '病害', symbolSize: 30 },
    { id: 's1', name: '体表白点', category: 1, value: '症状', symbolSize: 16 },
    { id: 'f1', name: '锦鲤', category: 2, value: '鱼种', symbolSize: 16 },
  ],
  links: [
    { source: 'd1', target: 's1', name: '典型症状', relationType: '症状关联' },
    { source: 'd1', target: 'f1', name: '危害鱼种', relationType: '鱼种关联' },
  ],
  stats: { diseaseCount: 1, nodeCount: 3, linkCount: 2, categoryCount: 3, relatedDiseaseLinkCount: 0 },
}

test('knowledge graph canvas fills its workspace instead of collapsing into a narrow strip', async ({ page }) => {
  await page.route('**/api/knowledge-graph', route => route.fulfill({ json: graph }))
  await page.goto('/fish/knowledge-graph')

  const chart = page.getByRole('img', { name: /水产病害知识图谱，当前显示 3 个节点和 2 条关系/ })
  await expect(chart).toBeVisible()
  await expect(chart.locator('canvas')).toBeVisible()

  const dimensions = await page.evaluate(() => {
    const workspace = document.querySelector<HTMLElement>('.graph-workspace')!
    const wrap = document.querySelector<HTMLElement>('.chart-wrap')!
    const graphCanvas = document.querySelector<HTMLElement>('.graph-canvas')!
    const canvas = graphCanvas.querySelector<HTMLCanvasElement>('canvas')!
    return {
      viewport: document.documentElement.clientWidth,
      pageFits: document.documentElement.scrollWidth <= document.documentElement.clientWidth,
      workspace: workspace.getBoundingClientRect().width,
      wrap: wrap.getBoundingClientRect().width,
      graphCanvas: graphCanvas.getBoundingClientRect().width,
      canvas: canvas.getBoundingClientRect().width,
    }
  })

  expect(dimensions.pageFits).toBe(true)
  expect(dimensions.wrap).toBeGreaterThan(dimensions.viewport <= 980 ? dimensions.workspace * 0.94 : dimensions.workspace * 0.6)
  expect(dimensions.graphCanvas).toBeGreaterThan(dimensions.wrap * 0.98)
  expect(dimensions.canvas).toBeGreaterThan(dimensions.wrap * 0.98)
})
