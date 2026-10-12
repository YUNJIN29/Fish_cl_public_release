import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useWorkspaceStore } from './workspace'

describe('workspace store', () => {
  beforeEach(() => { localStorage.clear(); setActivePinia(createPinia()) })

  it('loads defaults and persists customized shortcuts', () => {
    const workspace = useWorkspaceStore()
    workspace.load()
    expect(workspace.pinnedPaths).toContain('/profile')
    workspace.togglePin('/fish/rag')
    expect(JSON.parse(localStorage.getItem('yunjin:pinned-paths') || '[]')).toContain('/fish/rag')
  })

  it('offers the latest recognized page as the continuation target', () => {
    localStorage.setItem('yunjin:recent-pages', JSON.stringify([
      { path: '/personal', title: '工作台', visitedAt: 3 },
      { path: '/fish/diseases', title: '知识库', visitedAt: 2 },
      { path: '/unknown', title: '未知', visitedAt: 1 },
    ]))
    const workspace = useWorkspaceStore()
    workspace.load()
    expect(workspace.continuePage?.destination?.title).toBe('病害知识库')
    expect(workspace.recentDestinations).toHaveLength(1)
  })
})
