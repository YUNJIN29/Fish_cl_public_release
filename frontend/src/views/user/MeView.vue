<template>
  <div class="me-page">
    <template v-if="auth.me">
      <section class="profile-hero">
        <div class="identity">
          <el-avatar :size="84" :src="auth.me.avatarUrl || ''" class="hero-avatar">
            {{ initials }}
          </el-avatar>
          <div class="identity-copy">
            <div class="eyebrow">PERSONAL CENTER</div>
            <h1>{{ auth.me.nickname || auth.me.username }}</h1>
            <div class="identity-meta">
              <span>@{{ auth.me.username }}</span>
              <span>{{ roleText }}</span>
              <span>{{ joinedText }}</span>
            </div>
            <div class="identity-tags">
              <el-tag v-if="auth.me.mainSpecies" effect="dark">{{ auth.me.mainSpecies }}</el-tag>
              <el-tag v-if="auth.me.farmLocation" type="success" effect="plain">{{ auth.me.farmLocation }}</el-tag>
              <el-tag v-if="auth.me.farmName" type="warning" effect="plain">{{ auth.me.farmName }}</el-tag>
            </div>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" :icon="FirstAidKit" @click="router.push('/fish/diagnose')">开始诊断</el-button>
          <el-button :icon="Edit" @click="activeTab = 'profile'">编辑资料</el-button>
          <el-button type="danger" plain :icon="SwitchButton" @click="logout">退出登录</el-button>
        </div>
      </section>

      <el-tabs v-model="activeTab" class="center-tabs">
        <el-tab-pane label="数据总览" name="overview">
          <div v-loading="overviewLoading" class="overview-panel">
            <template v-if="overview">
              <div class="insight-row">
                <section class="completion-panel">
                  <el-progress type="dashboard" :percentage="overview.profileCompletion" :width="118" :stroke-width="12" />
                  <div class="completion-copy">
                    <h2>资料完整度</h2>
                    <p>{{ completionAdvice }}</p>
                    <div v-if="missingFields.length" class="missing-fields">
                      <span v-for="field in missingFields" :key="field">{{ field }}</span>
                    </div>
                    <el-button size="small" type="primary" plain @click="activeTab = 'profile'">继续完善</el-button>
                  </div>
                </section>

                <section class="health-panel">
                  <div class="panel-title">使用建议</div>
                  <p>{{ nextSuggestion }}</p>
                  <div class="suggestion-actions">
                    <el-button size="small" :icon="DocumentChecked" @click="activeTab = 'history'">复看历史</el-button>
                    <el-button size="small" :icon="ArrowRight" @click="router.push('/fish/rag')">RAG 检索</el-button>
                  </div>
                </section>
              </div>

              <section v-if="overview.attentionDiagnoses?.length" class="attention-panel" aria-label="需要关注的病例">
                <div class="attention-head">
                  <div class="attention-heading"><WarningFilled /><div><strong>需要关注的病例</strong><span>最新随访仍有恶化或新增死亡，请优先处理</span></div></div>
                  <el-tag type="danger" effect="dark">{{ overview.attentionDiagnoses.length }} 条</el-tag>
                </div>
                <div class="attention-list">
                  <button v-for="item in overview.attentionDiagnoses" :key="item.diagnosis.id" type="button" class="attention-item" @click="showDetail(item.diagnosis)">
                    <div><strong>{{ item.diagnosis.species || '未填写鱼种' }}</strong><span>{{ summarize(item.diagnosis.description) }}</span></div>
                    <div class="attention-status"><b>{{ conditionText(item.latestObservation.conditionStatus) }}</b><span>新增死亡 {{ item.latestObservation.mortalityCount }} 尾</span><small>{{ formatDate(item.latestObservation.observedAt) }}</small></div>
                  </button>
                </div>
              </section>

              <section v-if="overview.attentionWaterQuality?.length" class="attention-panel water-attention" aria-label="需要关注的水质记录">
                <div class="attention-head">
                  <div class="attention-heading"><WarningFilled /><div><strong>需要关注的水质</strong><span>最新读数偏离个人范围，或水体已超过 7 天未检测</span></div></div>
                  <el-tag type="warning" effect="dark">{{ overview.attentionWaterQuality.length }} 个水体</el-tag>
                </div>
                <div class="attention-list">
                  <button v-for="item in overview.attentionWaterQuality" :key="item.record.id" type="button" class="attention-item" @click="openWaterQuality(item.record.pondName)">
                    <div><strong>{{ item.record.pondName }}</strong><span>{{ item.deviations.length ? `${item.deviations.join('、')}偏离个人范围` : `已有 ${item.ageDays} 天未更新水质` }}</span></div>
                    <div class="attention-status"><b>{{ item.deviations.length ? `${item.deviations.length} 项偏离` : '数据已过期' }}</b><span v-if="item.stale && item.deviations.length">另有 {{ item.ageDays }} 天未测</span><span>{{ waterReadingSummary(item) }}</span><small>{{ formatDate(item.record.observedAt) }}</small></div>
                  </button>
                </div>
              </section>

              <section v-if="overview.upcomingTasks?.length" class="attention-panel task-attention" aria-label="近期养殖待办">
                <div class="attention-head">
                  <div class="attention-heading"><DocumentChecked /><div><strong>近期养殖待办</strong><span>按计划时间展示最早的未完成事项</span></div></div>
                  <el-tag :type="overview.upcomingTasks.some(isTaskOverdue) ? 'danger' : 'info'" effect="dark">{{ overview.stats.openTaskCount }} 项待处理</el-tag>
                </div>
                <div class="attention-list">
                  <article v-for="item in overview.upcomingTasks.slice(0, 3)" :key="item.id" class="task-attention-item">
                    <button type="button" class="task-attention-main" :aria-label="`查看${item.title}`" @click="router.push('/fish/tasks')"><div><strong>{{ item.title }}</strong><span>{{ item.pondName || taskCategoryText(item.category) }}</span></div><div class="attention-status"><b :class="{ overdue: isTaskOverdue(item) }">{{ isTaskOverdue(item) ? '已逾期' : taskCategoryText(item.category) }}</b><small>{{ formatDate(item.dueAt) }}</small></div></button>
                    <button v-if="item.category === 'WATER_TEST'" type="button" class="task-record-result" :aria-label="`记录${item.title}的检测结果`" @click="recordTaskResult(item)">记录结果</button>
                    <button v-else type="button" class="task-quick-complete" :aria-label="`完成${item.title}`" :disabled="taskCompleting === item.id" @click="quickCompleteTask(item)">{{ taskCompleting === item.id ? '处理中…' : '完成' }}</button>
                  </article>
                </div>
              </section>

              <div class="stats-grid">
                <button v-for="item in statCards" :key="item.label" class="stat-card" type="button" @click="item.action">
                  <span class="stat-icon" :class="item.className">
                    <component :is="item.icon" />
                  </span>
                  <span class="stat-value">{{ item.value }}</span>
                  <span class="stat-label">{{ item.label }}</span>
                </button>
              </div>

              <div class="quick-grid">
                <button type="button" @click="router.push('/fish/diagnose')"><FirstAidKit /><span>继续诊断</span></button>
                <button type="button" @click="router.push('/fish/orders')"><ShoppingCart /><span>查看订单</span></button>
                <button type="button" @click="router.push('/fish/posts')"><ChatDotRound /><span>交流发帖</span></button>
                <button type="button" @click="router.push('/fish/diseases')"><DocumentChecked /><span>病害知识库</span></button>
                <button type="button" @click="router.push('/fish/water-quality')"><WarningFilled /><span>水质日志</span></button>
              </div>

              <div class="overview-sections">
                <section class="overview-section">
                  <div class="section-head">
                    <div><span>最近诊断</span><small>快速回看问诊结论</small></div>
                    <el-button text type="primary" @click="activeTab = 'history'">全部</el-button>
                  </div>
                  <div v-if="overview.recentDiagnoses.length" class="mini-list">
                    <button v-for="item in overview.recentDiagnoses" :key="item.id" class="mini-item" type="button" @click="showDetail(item)">
                      <span class="mini-title">{{ summarize(item.description) }}</span>
                      <span class="mini-meta">{{ item.species || '未填写鱼种' }} · {{ formatDate(item.createdAt) }}</span>
                    </button>
                  </div>
                  <div v-else class="empty-state">
                    <FirstAidKit />
                    <p>还没有诊断记录</p>
                    <el-button type="primary" size="small" @click="router.push('/fish/diagnose')">开始第一次诊断</el-button>
                  </div>
                </section>

                <section class="overview-section">
                  <div class="section-head">
                    <div><span>最近订单</span><small>查看水产优选购买记录</small></div>
                    <el-button text type="primary" @click="router.push('/fish/orders')">全部</el-button>
                  </div>
                  <div v-if="overview.recentOrders.length" class="mini-list">
                    <button v-for="item in overview.recentOrders" :key="item.id" class="mini-item" type="button" @click="router.push('/fish/orders')">
                      <span class="mini-title">{{ item.orderNo }} · {{ formatMoney(item.totalCents) }}</span>
                      <span class="mini-meta">{{ statusText(item.status) }} · {{ formatDate(item.createdAt) }}</span>
                    </button>
                  </div>
                  <div v-else class="empty-state">
                    <ShoppingCart />
                    <p>暂无订单</p>
                    <el-button size="small" @click="router.push('/fish/products')">去水产优选</el-button>
                  </div>
                </section>

                <section class="overview-section">
                  <div class="section-head">
                    <div><span>社区动态</span><small>你的交流与经验沉淀</small></div>
                    <el-button text type="primary" @click="router.push('/fish/posts')">全部</el-button>
                  </div>
                  <div v-if="overview.recentPosts.length" class="mini-list">
                    <button v-for="item in overview.recentPosts" :key="item.id" class="mini-item" type="button" @click="router.push('/fish/posts')">
                      <span class="mini-title">{{ item.title }}</span>
                      <span class="mini-meta">{{ formatDate(item.createdAt) }}</span>
                    </button>
                  </div>
                  <div v-else class="empty-state">
                    <ChatDotRound />
                    <p>还没发布内容</p>
                    <el-button size="small" @click="router.push('/fish/posts')">发布交流帖</el-button>
                  </div>
                </section>
              </div>
            </template>
            <el-empty v-else description="暂无个人数据">
              <el-button type="primary" :icon="Refresh" @click="loadOverview">重新加载</el-button>
            </el-empty>
          </div>
        </el-tab-pane>

        <el-tab-pane label="个人资料" name="profile">
          <div class="profile-panel">
            <aside class="profile-preview">
              <el-avatar :size="96" :src="profileForm.avatarUrl || ''">
                {{ profileForm.nickname?.slice(0, 1) || initials }}
              </el-avatar>
              <h2>{{ profileForm.nickname || auth.me.username }}</h2>
              <p>{{ profileForm.bio || '完善简介后，这里会展示你的养殖经验和关注方向。' }}</p>
              <div class="contact-list">
                <span><Location />{{ profileForm.farmLocation || '未填写地区' }}</span>
                <span><Picture />{{ profileForm.mainSpecies || '未填写养殖品种' }}</span>
                <span><Message />{{ profileForm.email || '未填写邮箱' }}</span>
                <span><Phone />{{ profileForm.phone || '未填写电话' }}</span>
              </div>
            </aside>

            <el-form :model="profileForm" label-position="top" class="profile-form">
              <div class="form-section-title">基础资料</div>
              <el-row :gutter="16">
                <el-col :xs="24" :sm="12"><el-form-item label="昵称"><el-input v-model="profileForm.nickname" maxlength="32" show-word-limit /></el-form-item></el-col>
                <el-col :xs="24" :sm="12"><el-form-item label="头像 URL"><el-input v-model="profileForm.avatarUrl" placeholder="https://..." /></el-form-item></el-col>
                <el-col :xs="24" :sm="12"><el-form-item label="邮箱"><el-input v-model="profileForm.email" placeholder="用于接收诊断资料" /></el-form-item></el-col>
                <el-col :xs="24" :sm="12"><el-form-item label="电话"><el-input v-model="profileForm.phone" placeholder="选填" /></el-form-item></el-col>
              </el-row>

              <div class="form-section-title">养殖信息</div>
              <el-row :gutter="16">
                <el-col :xs="24" :sm="12"><el-form-item label="养殖场/店铺"><el-input v-model="profileForm.farmName" placeholder="例如：云锦水产基地" /></el-form-item></el-col>
                <el-col :xs="24" :sm="12"><el-form-item label="所在地区"><el-input v-model="profileForm.farmLocation" placeholder="省/市/县" /></el-form-item></el-col>
                <el-col :span="24"><el-form-item label="主要养殖品种"><el-input v-model="profileForm.mainSpecies" placeholder="草鱼、锦鲤、对虾、加州鲈..." /></el-form-item></el-col>
                <el-col :span="24"><el-form-item label="个人简介"><el-input v-model="profileForm.bio" type="textarea" :rows="4" maxlength="512" show-word-limit /></el-form-item></el-col>
              </el-row>
              <div class="profile-actions">
                <el-button @click="syncProfileForm">恢复当前资料</el-button>
                <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存资料</el-button>
              </div>
            </el-form>
          </div>
          <section class="security-card">
            <div><small>ACCOUNT SECURITY</small><h3>修改登录密码</h3><p>建议使用至少 10 位、同时包含字母和数字的独立密码。</p></div>
            <form class="password-form" @submit.prevent="changePassword">
              <label><span>当前密码</span><input v-model="passwordForm.currentPassword" type="password" autocomplete="current-password" required maxlength="128" /></label>
              <label><span>新密码</span><input v-model="passwordForm.newPassword" type="password" autocomplete="new-password" required minlength="10" maxlength="128" /></label>
              <label><span>确认新密码</span><input v-model="passwordForm.confirmPassword" type="password" autocomplete="new-password" required minlength="10" maxlength="128" /></label>
              <el-button type="primary" native-type="submit" :loading="passwordSaving">更新密码</el-button>
            </form>
          </section>
        </el-tab-pane>

        <el-tab-pane label="诊断历史" name="history">
          <div v-loading="loading" class="history-panel">
            <div class="history-toolbar">
              <div><h2>诊断历史</h2><p>按时间复盘症状、水质和 AI 建议，便于连续观察。</p></div>
              <el-button type="primary" :icon="FirstAidKit" @click="router.push('/fish/diagnose')">再诊断一次</el-button>
            </div>

            <div v-if="history.length > 0" class="history-list">
              <button v-for="item in history" :key="item.id" class="history-item" type="button" @click="showDetail(item)">
                <ProtectedImage v-if="item.imageUrl" :src="item.imageUrl" fit="cover" class="history-thumb" alt="诊断历史病灶缩略图" />
                <div v-else class="history-placeholder"><DocumentChecked /></div>
                <div class="history-main">
                  <div class="history-title-row">
                    <strong>{{ summarize(item.description) }}</strong>
                    <span>{{ formatDate(item.createdAt) }}</span>
                  </div>
                  <div class="history-tags">
                    <span v-if="item.species">{{ item.species }}</span>
                    <span v-if="item.temperature">水温 {{ item.temperature }}°C</span>
                    <span v-if="item.duration">发病 {{ item.duration }}</span>
                    <span v-if="item.waterQuality">水质 {{ item.waterQuality }}</span>
                  </div>
                </div>
              </button>
            </div>
            <div v-else class="large-empty">
              <FirstAidKit />
              <h3>暂无诊断记录</h3>
              <p>完成一次 AI 鱼病诊断后，记录会自动归档到这里。</p>
              <el-button type="primary" @click="router.push('/fish/diagnose')">去诊断</el-button>
            </div>

            <div class="pagination" v-if="total > size">
              <el-pagination small layout="prev, pager, next" :total="total" :page-size="size" v-model:current-page="page" @current-change="loadHistory" />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的收藏" name="favorites">
          <div v-loading="favoritesLoading" class="favorites-panel">
            <div class="history-toolbar">
              <div><h2>我的收藏</h2><p>集中查看收藏的科普文章和社区经验，不必重新搜索。</p></div>
              <el-radio-group v-model="favoriteType" size="small" @change="resetAndLoadFavorites">
                <el-radio-button value="">全部</el-radio-button>
                <el-radio-button value="ARTICLE">文章</el-radio-button>
                <el-radio-button value="POST">帖子</el-radio-button>
              </el-radio-group>
            </div>
            <div v-if="favorites.length" class="favorite-grid">
              <article v-for="item in favorites" :key="item.id" class="favorite-card" :class="{ unavailable: !item.available }">
                <div v-if="item.coverImageUrl" class="favorite-cover" :style="{ backgroundImage: `url(${item.coverImageUrl})` }"></div>
                <div v-else class="favorite-cover placeholder"><Star /></div>
                <div class="favorite-body">
                  <div class="favorite-meta"><el-tag size="small" effect="plain">{{ item.targetType === 'ARTICLE' ? '科普文章' : '社区帖子' }}</el-tag><span>收藏于 {{ formatDate(item.createdAt) }}</span></div>
                  <h3>{{ item.title }}</h3>
                  <p>{{ item.summary }}</p>
                  <div class="favorite-actions">
                    <el-button :disabled="!item.available" type="primary" plain size="small" @click="openFavorite(item)">查看内容</el-button>
                    <el-button type="danger" text size="small" @click="removeFavorite(item)">取消收藏</el-button>
                  </div>
                </div>
              </article>
            </div>
            <div v-else class="large-empty">
              <Star />
              <h3>还没有收藏内容</h3>
              <p>浏览科普文章或社区帖子时点击收藏，就能在这里随时找回。</p>
              <el-button type="primary" @click="router.push('/fish/articles')">去发现内容</el-button>
            </div>
            <div v-if="favoriteTotal > favoriteSize" class="pagination">
              <el-pagination small layout="prev, pager, next" :total="favoriteTotal" :page-size="favoriteSize" v-model:current-page="favoritePage" @current-change="loadFavorites" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <el-dialog v-model="detailVisible" title="诊断报告回顾" width="80%" class="diagnose-dialog">
        <div v-if="current" class="record-detail">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="诊断对象">{{ current.species || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="水温环境">{{ current.temperature ? current.temperature + '°C' : '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="发病时长">{{ current.duration || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ formatDate(current.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="水质情况" :span="2">{{ current.waterQuality || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="用户描述" :span="2">{{ current.description }}</el-descriptions-item>
          </el-descriptions>

          <div v-if="current.imageUrl" class="image-review">
            <div class="result-title">病灶图片</div>
            <ProtectedImage :src="current.imageUrl" fit="contain" class="detail-image" preview alt="诊断报告病灶图片" />
          </div>

          <div class="result-box">
            <div class="result-title">AI 诊断建议</div>
            <div class="result-content" v-html="formatContent(current.result)"></div>
          </div>

          <section class="follow-up-panel">
            <div class="follow-up-head">
              <div>
                <div class="result-title">观察随访</div>
                <p>记录处理后的病情、摄食和死亡变化，形成连续病例。</p>
              </div>
              <el-button type="primary" plain @click="toggleFollowUpForm">
                {{ followUpFormVisible ? '取消填写' : '新增观察' }}
              </el-button>
            </div>

            <el-alert v-if="requiresEscalation" class="follow-up-alert" type="error" :closable="false" show-icon title="需要升级处理">
              <template #default>{{ escalationAdvice }}</template>
            </el-alert>

            <el-form v-if="followUpFormVisible" label-position="top" class="follow-up-form" @submit.prevent="saveFollowUp">
              <div v-if="editingFollowUpId" class="follow-up-editing">正在修改这条观察记录；保存后会重新计算病例风险状态。</div>
              <el-row :gutter="12">
                <el-col :xs="24" :sm="12"><el-form-item label="病情变化（必填）"><el-select v-model="followUpForm.conditionStatus" style="width:100%"><el-option label="已恢复（结束观察）" value="RECOVERED" /><el-option label="好转" value="IMPROVING" /><el-option label="基本稳定" value="STABLE" /><el-option label="继续恶化" value="WORSENING" /></el-select></el-form-item></el-col>
                <el-col :xs="24" :sm="12"><el-form-item label="摄食情况（必填）"><el-select v-model="followUpForm.appetiteStatus" style="width:100%"><el-option label="正常" value="GOOD" /><el-option label="减少" value="REDUCED" /><el-option label="停食" value="NONE" /></el-select></el-form-item></el-col>
                <el-col :xs="24" :sm="12"><el-form-item label="观察时间"><input v-model="followUpForm.observedAt" class="native-datetime" type="datetime-local" required /></el-form-item></el-col>
                <el-col :xs="24" :sm="12"><el-form-item label="新增死亡数量"><el-input-number v-model="followUpForm.mortalityCount" :min="0" :max="100000" style="width:100%" /></el-form-item></el-col>
                <el-col :span="24"><el-form-item label="当前水质"><el-input v-model="followUpForm.waterQuality" maxlength="255" placeholder="如：换水后氨氮下降、溶氧正常" /></el-form-item></el-col>
                <el-col :span="24"><el-form-item label="观察备注"><el-input v-model="followUpForm.notes" type="textarea" :rows="3" maxlength="1000" show-word-limit placeholder="记录病灶、活动、用药或换水后的变化" /></el-form-item></el-col>
              </el-row>
              <div class="follow-up-submit"><el-button type="primary" native-type="submit" :loading="followUpSaving">{{ editingFollowUpId ? '保存修改' : '保存观察记录' }}</el-button></div>
            </el-form>

            <div v-loading="followUpsLoading" class="follow-up-list">
              <article v-for="item in followUps" :key="item.id" class="follow-up-item">
                <div class="follow-up-marker" :class="item.conditionStatus.toLowerCase()"></div>
                <div class="follow-up-copy">
                  <div class="follow-up-meta"><strong>{{ conditionText(item.conditionStatus) }}</strong><span>{{ formatDate(item.observedAt) }}</span></div>
                  <div class="follow-up-tags"><span>摄食：{{ appetiteText(item.appetiteStatus) }}</span><span>新增死亡：{{ item.mortalityCount }} 尾</span><span v-if="item.waterQuality">水质：{{ item.waterQuality }}</span></div>
                  <p v-if="item.notes">{{ item.notes }}</p>
                </div>
                <div class="follow-up-actions"><el-button text type="primary" :aria-label="`编辑${formatDate(item.observedAt)}的观察记录`" @click="editFollowUp(item)">编辑</el-button><el-button text type="danger" aria-label="删除观察记录" @click="removeFollowUp(item)">删除</el-button></div>
              </article>
              <div v-if="!followUpsLoading && followUps.length === 0" class="follow-up-empty">还没有随访记录，建议在处理后 24 小时补充第一次观察。</div>
            </div>
          </section>

          <div class="detail-actions">
            <el-button :icon="DocumentCopy" :loading="followUpsLoading" @click="copyRecord">复制完整病例</el-button>
            <el-button :icon="Printer" :disabled="followUpsLoading" @click="printRecord">打印 / PDF</el-button>
            <el-button type="success" @click="scheduleFollowUpTask">安排下次观察</el-button>
            <el-button @click="detailVisible = false">关闭</el-button>
            <el-button type="primary" @click="diagnoseAgain">再诊断一次</el-button>
          </div>
        </div>
      </el-dialog>
    </template>

    <div v-else class="large-empty">
      <UserFilled />
      <h3>请先登录</h3>
      <p>登录后可以管理资料、查看诊断历史和个人数据。</p>
      <el-button type="primary" @click="router.push('/login')">去登录</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { http } from '@/utils/http'
import ProtectedImage from '@/components/ProtectedImage.vue'
import { copyDiagnosisReport, printDiagnosisReport } from '@/utils/diagnosisReport'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowRight,
  ChatDotRound,
  DocumentChecked,
  DocumentCopy,
  Edit,
  FirstAidKit,
  Location,
  Message,
  Phone,
  Picture,
  Printer,
  Refresh,
  ShoppingCart,
  Star,
  SwitchButton,
  UserFilled,
  WarningFilled,
} from '@element-plus/icons-vue'

type Overview = {
  profileCompletion: number
  stats: {
    diagnoseCount: number
    orderCount: number
    postCount: number
    favoriteCount: number
    openTaskCount: number
  }
  recentDiagnoses: any[]
  recentOrders: any[]
  recentPosts: any[]
  attentionDiagnoses: Array<{ diagnosis: any; latestObservation: FollowUp }>
  attentionWaterQuality: Array<{ record: { id:number; pondName:string; temperature?:number; ph?:number; dissolvedOxygen?:number; ammonia?:number; nitrite?:number; observedAt:string }; deviations:string[]; ageDays:number; stale:boolean }>
  upcomingTasks: Array<{ id:number; title:string; category:string; pondName?:string; dueAt:string; notes?:string }>
}

type FavoriteItem = {
  id: number
  targetType: 'ARTICLE' | 'POST'
  targetId: number
  title: string
  summary: string
  coverImageUrl?: string
  createdAt: string
  available: boolean
  url?: string
}

type FollowUp = {
  id: number
  conditionStatus: 'IMPROVING' | 'STABLE' | 'WORSENING' | 'RECOVERED'
  appetiteStatus: 'GOOD' | 'REDUCED' | 'NONE'
  mortalityCount: number
  waterQuality?: string
  notes?: string
  observedAt: string
  createdAt: string
}

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const activeTab = ref('overview')
const overview = ref<Overview | null>(null)
const overviewLoading = ref(false)
const taskCompleting = ref<number | null>(null)
const history = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = 10
const favorites = ref<FavoriteItem[]>([])
const favoritesLoading = ref(false)
const favoriteType = ref<'' | 'ARTICLE' | 'POST'>('')
const favoriteTotal = ref(0)
const favoritePage = ref(1)
const favoriteSize = 12

const detailVisible = ref(false)
const current = ref<any>(null)
const followUps = ref<FollowUp[]>([])
const followUpsLoading = ref(false)
const followUpSaving = ref(false)
const followUpFormVisible = ref(false)
const followUpForm = ref(newFollowUpForm())
const editingFollowUpId = ref<number | null>(null)
const savingProfile = ref(false)
const passwordSaving = ref(false)
const passwordForm = ref({ currentPassword:'', newPassword:'', confirmPassword:'' })
const profileForm = ref({
  nickname: '',
  avatarUrl: '',
  email: '',
  phone: '',
  farmName: '',
  farmLocation: '',
  mainSpecies: '',
  bio: '',
})

const initials = computed(() => (auth.me?.nickname || auth.me?.username || 'U').slice(0, 1).toUpperCase())
const roleText = computed(() => auth.me?.roles?.includes('ADMIN') ? '管理员' : '普通用户')
const joinedText = computed(() => auth.me?.createdAt ? `加入于 ${new Date(auth.me.createdAt).toLocaleDateString()}` : '已登录用户')
const latestFollowUp = computed(() => followUps.value[0])
const requiresEscalation = computed(() => Boolean(latestFollowUp.value && latestFollowUp.value.conditionStatus !== 'RECOVERED' && (latestFollowUp.value.conditionStatus === 'WORSENING' || latestFollowUp.value.mortalityCount > 0)))
const escalationAdvice = computed(() => latestFollowUp.value?.conditionStatus === 'WORSENING'
  ? '最近一次观察显示病情继续恶化，请立即复测溶氧、pH、氨氮和亚硝酸盐，并尽快联系水产技术人员或兽医。'
  : `最近一次观察新增死亡 ${latestFollowUp.value?.mortalityCount || 0} 尾，请核对总存塘量并持续记录变化。`)

const missingFields = computed(() => {
  const fields = [
    ['头像', profileForm.value.avatarUrl],
    ['邮箱', profileForm.value.email],
    ['电话', profileForm.value.phone],
    ['养殖场', profileForm.value.farmName],
    ['地区', profileForm.value.farmLocation],
    ['品种', profileForm.value.mainSpecies],
    ['简介', profileForm.value.bio],
  ]
  return fields.filter(([, value]) => !value).map(([label]) => label)
})

const completionAdvice = computed(() => {
  const percent = overview.value?.profileCompletion || 0
  if (percent >= 90) return '资料已经很完整，后续 AI 诊断可以更好结合你的养殖背景。'
  if (percent >= 60) return '再补充地区、品种或联系方式，个人中心会更像一份完整档案。'
  return '建议先补齐养殖品种、地区和联系方式，方便诊断历史长期复盘。'
})

const nextSuggestion = computed(() => {
  if (overview.value?.attentionDiagnoses?.length) return `有 ${overview.value.attentionDiagnoses.length} 个病例的最新随访需要关注，建议先处理风险提示。`
  if (overview.value?.attentionWaterQuality?.length) return `有 ${overview.value.attentionWaterQuality.length} 个水体存在读数偏离或数据过期，建议先复测并查看趋势。`
  if (overview.value?.upcomingTasks?.some(isTaskOverdue)) return '有养殖待办已经逾期，建议先核对现场情况并更新完成状态。'
  const stats = overview.value?.stats
  if (!stats || stats.diagnoseCount === 0) return '可以先完成一次 AI 鱼病诊断，系统会自动沉淀为历史记录。'
  if (stats.postCount === 0) return '你已经有诊断记录，可以把典型问题发布到社区，方便后续交流。'
  return '你的数据已经开始积累，建议定期复看诊断记录和水质变化。'
})

function waterReadingSummary(item: Overview['attentionWaterQuality'][number]) {
  const record = item.record
  const values = [record.temperature != null ? `${record.temperature}°C` : '', record.ph != null ? `pH ${record.ph}` : '', record.dissolvedOxygen != null ? `溶氧 ${record.dissolvedOxygen}` : ''].filter(Boolean)
  return values.join(' · ') || '查看最新测量'
}

function openWaterQuality(pondName: string) {
  router.push({ path: '/fish/water-quality', query: { pond: pondName } })
}

function isTaskOverdue(item: Overview['upcomingTasks'][number]) { return new Date(item.dueAt).getTime() < Date.now() }
function taskCategoryText(category: string) { return ({ FEEDING:'投喂',WATER_TEST:'水质检测',WATER_CHANGE:'换水',MEDICATION:'用药/复查',OBSERVATION:'日常观察',OTHER:'其他' } as Record<string,string>)[category] || '其他' }
function recordTaskResult(item: Overview['upcomingTasks'][number]) { router.push({ path:'/fish/water-quality', query:{ pond:item.pondName||'', taskId:String(item.id), taskTitle:item.title } }) }

async function quickCompleteTask(item: Overview['upcomingTasks'][number]) {
  taskCompleting.value = item.id
  try {
    await http.patch(`/api/tasks/${item.id}/completion`, { completed: true })
    ElMessage.success('待办已完成')
    await loadOverview()
  } finally {
    taskCompleting.value = null
  }
}

const statCards = computed(() => {
  const stats = overview.value?.stats || { diagnoseCount: 0, orderCount: 0, postCount: 0, favoriteCount: 0, openTaskCount: 0 }
  return [
    { label: '诊断次数', value: stats.diagnoseCount, icon: FirstAidKit, className: 'cyan', action: () => activeTab.value = 'history' },
    { label: '订单数量', value: stats.orderCount, icon: ShoppingCart, className: 'green', action: () => router.push('/fish/orders') },
    { label: '发帖数量', value: stats.postCount, icon: ChatDotRound, className: 'violet', action: () => router.push('/fish/posts') },
    { label: '收藏数量', value: stats.favoriteCount, icon: Star, className: 'amber', action: () => activeTab.value = 'favorites' },
    { label: '待办事项', value: stats.openTaskCount, icon: DocumentChecked, className: 'violet', action: () => router.push('/fish/tasks') },
  ]
})

async function loadOverview() {
  if (!auth.me) return
  overviewLoading.value = true
  try {
    overview.value = await http.get<Overview>('/api/personal/overview')
  } finally {
    overviewLoading.value = false
  }
}

function syncProfileForm() {
  if (!auth.me) return
  profileForm.value = {
    nickname: auth.me.nickname || '',
    avatarUrl: auth.me.avatarUrl || '',
    email: auth.me.email || '',
    phone: auth.me.phone || '',
    farmName: auth.me.farmName || '',
    farmLocation: auth.me.farmLocation || '',
    mainSpecies: auth.me.mainSpecies || '',
    bio: auth.me.bio || '',
  }
}

async function saveProfile() {
  savingProfile.value = true
  try {
    await auth.updateProfile(profileForm.value)
    syncProfileForm()
    await loadOverview()
    ElMessage.success('个人资料已保存')
  } finally {
    savingProfile.value = false
  }
}

async function changePassword() {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) { ElMessage.warning('两次输入的新密码不一致'); return }
  if (passwordForm.value.newPassword.length < 10 || !/[A-Za-z]/.test(passwordForm.value.newPassword) || !/\d/.test(passwordForm.value.newPassword)) { ElMessage.warning('新密码至少 10 位，并同时包含字母和数字'); return }
  passwordSaving.value = true
  try {
    await http.post('/api/auth/change-password', { currentPassword:passwordForm.value.currentPassword, newPassword:passwordForm.value.newPassword })
    passwordForm.value = { currentPassword:'', newPassword:'', confirmPassword:'' }
    ElMessage.success('密码已更新，请妥善保管新密码')
  } finally { passwordSaving.value = false }
}

function logout() {
  auth.logout()
  router.push('/fish')
}

async function loadHistory() {
  if (!auth.me) return
  loading.value = true
  try {
    const res = await http.get<any>('/api/diagnose/history', { page: page.value - 1 })
    history.value = res.content
    total.value = res.totalElements
  } finally {
    loading.value = false
  }
}

async function loadFavorites() {
  if (!auth.me) return
  favoritesLoading.value = true
  try {
    const res = await http.get<any>('/api/favorites', { targetType: favoriteType.value || undefined, page: favoritePage.value - 1, size: favoriteSize })
    favorites.value = res?.content || []
    favoriteTotal.value = res?.totalElements || 0
  } finally { favoritesLoading.value = false }
}

function resetAndLoadFavorites() { favoritePage.value = 1; loadFavorites() }
function openFavorite(item: FavoriteItem) { if (item.available && item.url) router.push(item.url) }
async function removeFavorite(item: FavoriteItem) {
  await http.post(`/api/favorites?targetType=${item.targetType}&targetId=${item.targetId}`)
  ElMessage.success('已取消收藏')
  await Promise.all([loadFavorites(), loadOverview()])
}

function showDetail(item: any) {
  current.value = item
  detailVisible.value = true
  followUps.value = []
  followUpFormVisible.value = false
  editingFollowUpId.value = null
  followUpForm.value = newFollowUpForm()
  loadFollowUps()
}

function newFollowUpForm() {
  const now = new Date(Date.now() - new Date().getTimezoneOffset() * 60_000)
  return { conditionStatus: 'STABLE', appetiteStatus: 'REDUCED', mortalityCount: 0, waterQuality: '', notes: '', observedAt: now.toISOString().slice(0, 16) }
}

function toggleFollowUpForm() {
  if (followUpFormVisible.value) {
    followUpFormVisible.value = false
    editingFollowUpId.value = null
    followUpForm.value = newFollowUpForm()
  } else {
    editingFollowUpId.value = null
    followUpForm.value = newFollowUpForm()
    followUpFormVisible.value = true
  }
}

function editFollowUp(item: FollowUp) {
  const observed = new Date(item.observedAt)
  const localObserved = new Date(observed.getTime() - observed.getTimezoneOffset() * 60_000).toISOString().slice(0, 16)
  editingFollowUpId.value = item.id
  followUpForm.value = { conditionStatus:item.conditionStatus, appetiteStatus:item.appetiteStatus, mortalityCount:item.mortalityCount, waterQuality:item.waterQuality || '', notes:item.notes || '', observedAt:localObserved }
  followUpFormVisible.value = true
}

async function loadFollowUps() {
  if (!current.value?.id) return
  followUpsLoading.value = true
  try { followUps.value = await http.get<FollowUp[]>(`/api/diagnose/${current.value.id}/follow-ups`) }
  finally { followUpsLoading.value = false }
}

async function saveFollowUp() {
  if (!current.value?.id || !followUpForm.value.observedAt) return
  followUpSaving.value = true
  try {
    const payload = { ...followUpForm.value, observedAt: new Date(followUpForm.value.observedAt).toISOString() }
    if (editingFollowUpId.value) await http.put(`/api/diagnose/${current.value.id}/follow-ups/${editingFollowUpId.value}`, payload)
    else await http.post(`/api/diagnose/${current.value.id}/follow-ups`, payload)
    ElMessage.success(editingFollowUpId.value ? '观察记录已更新' : '观察记录已保存')
    editingFollowUpId.value = null
    followUpForm.value = newFollowUpForm()
    followUpFormVisible.value = false
    await loadFollowUps()
    if (overview.value) await loadOverview()
  } finally { followUpSaving.value = false }
}

async function removeFollowUp(item: FollowUp) {
  if (!current.value?.id) return
  await ElMessageBox.confirm('删除后无法恢复，确定删除这条观察记录吗？', '删除观察记录', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  await http.delete(`/api/diagnose/${current.value.id}/follow-ups/${item.id}`)
  if (editingFollowUpId.value === item.id) { editingFollowUpId.value = null; followUpFormVisible.value = false; followUpForm.value = newFollowUpForm() }
  ElMessage.success('观察记录已删除')
  await loadFollowUps()
}

function conditionText(value: FollowUp['conditionStatus']) { return { IMPROVING: '病情好转', STABLE: '病情稳定', WORSENING: '病情恶化', RECOVERED: '病例已恢复' }[value] }
function appetiteText(value: FollowUp['appetiteStatus']) { return { GOOD: '正常', REDUCED: '减少', NONE: '停食' }[value] }

function scheduleFollowUpTask() {
  if (!current.value) return
  const species = current.value.species || '本次'
  const latest = latestFollowUp.value
  const steps = (current.value.nextSteps || []).slice(0, 3).join('；')
  const notes = [
    `诊断对象：${species}`,
    latest ? `最近观察：${conditionText(latest.conditionStatus)}，摄食${appetiteText(latest.appetiteStatus)}，新增死亡${latest.mortalityCount}尾` : '',
    steps ? `建议复查：${steps}` : '复查症状、摄食、死亡数量和关键水质指标',
  ].filter(Boolean).join('。').slice(0, 500)
  detailVisible.value = false
  router.push({ path:'/fish/tasks', query:{ source:'diagnosis', title:`复查${species}诊断情况`, category:'MEDICATION', notes } })
}

function summarize(text?: string) {
  const raw = text || '图片诊断'
  const plain = raw.replace(/\s+/g, ' ').trim()
  return plain.length > 90 ? plain.slice(0, 90) + '...' : plain
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleString() : '未知时间'
}

function formatMoney(cents?: number) {
  return `￥${((cents || 0) / 100).toFixed(2)}`
}

function statusText(status?: string) {
  const map: Record<string, string> = {
    PENDING: '待处理',
    PAID: '已支付',
    SHIPPED: '已发货',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
  }
  return status ? (map[status] || status) : '未知状态'
}

function formatContent(text: string): string {
  if (!text) return ''
  let html = escapeHtml(text)
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\n/g, '<br>')
  const products = [
    { name: '复合黄粉', id: 1 },
    { name: '水霉特效杀菌剂', id: 2 },
    { name: '高活性硝化细菌', id: 3 },
    { name: '硝化细菌', id: 3 },
    { name: '增红高蛋白饲料', id: 4 },
    { name: '增氧泵', id: 5 },
  ]
  products.forEach(p => {
    const reg = new RegExp(p.name, 'g')
    html = html.replace(reg, `<a href="/fish/products/${p.id}" style="color:var(--c-cyan);font-weight:700"><u>${p.name}</u></a>`)
  })
  return html
}

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function diagnoseAgain() {
  detailVisible.value = false
  router.push('/fish/diagnose')
}

function currentPrintableRecord() {
  return { report: current.value.result, species: current.value.species, temperature: current.value.temperature, duration: current.value.duration, waterQuality: current.value.waterQuality, createdAt: current.value.createdAt, confidence: current.value.confidence, evidence: current.value.evidence, sources: current.value.sources, warnings: current.value.warnings, nextSteps: current.value.nextSteps, hasPrivateImage: Boolean(current.value.imageUrl), observations: followUps.value }
}
async function copyRecord() { if (!current.value) return; await copyDiagnosisReport(currentPrintableRecord()); ElMessage.success(followUps.value.length ? '完整病例已复制（含随访）' : '诊断报告已复制') }
function printRecord() { if (current.value && !printDiagnosisReport(currentPrintableRecord())) ElMessage.warning('浏览器阻止了打印窗口，请允许本站打开新窗口') }

watch(activeTab, (val) => {
  if (val === 'overview' && !overview.value) loadOverview()
  if (val === 'history' && history.value.length === 0) loadHistory()
  if (val === 'favorites' && favorites.value.length === 0) loadFavorites()
})

watch(() => auth.me, () => {
  syncProfileForm()
  if (auth.me && !overview.value) loadOverview()
}, { immediate: true })

onMounted(() => {
  syncProfileForm()
  if (route.query.tab === 'history') activeTab.value = 'history'
  if (route.query.tab === 'profile') activeTab.value = 'profile'
  if (route.query.tab === 'favorites') activeTab.value = 'favorites'
  if (activeTab.value === 'overview') loadOverview()
  if (activeTab.value === 'history') loadHistory()
  if (activeTab.value === 'favorites') loadFavorites()
})
</script>

<style scoped>
.me-page {
  max-width: 1160px;
  margin: 0 auto;
}
.profile-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 22px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: linear-gradient(120deg, rgba(0, 180, 216, 0.14), transparent 42%), var(--c-bg-card);
}
.identity {
  display: flex;
  align-items: center;
  gap: 18px;
  min-width: 0;
}
.hero-avatar {
  flex-shrink: 0;
  border: 2px solid rgba(0, 212, 255, 0.45);
}
.identity-copy {
  min-width: 0;
}
.eyebrow {
  color: var(--c-cyan);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 2px;
}
.identity h1 {
  margin: 6px 0 8px;
  color: var(--c-text);
  font-size: 30px;
  line-height: 1.15;
}
.identity-meta,
.identity-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  color: var(--c-text-3);
  font-size: 13px;
}
.identity-tags {
  margin-top: 10px;
}
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
  flex-shrink: 0;
}
.center-tabs {
  margin-top: 16px;
}
.overview-panel {
  min-height: 390px;
}
.insight-row {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(260px, 0.9fr);
  gap: 14px;
}
.completion-panel,
.health-panel,
.overview-section,
.profile-preview,
.profile-form {
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: var(--c-bg-card);
}
.completion-panel {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 18px;
}
.completion-copy h2,
.history-toolbar h2,
.profile-preview h2 {
  margin: 0;
  color: var(--c-text);
}
.completion-copy p,
.health-panel p,
.history-toolbar p,
.profile-preview p {
  color: var(--c-text-3);
  line-height: 1.7;
}
.missing-fields {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 10px 0 12px;
}
.missing-fields span,
.history-tags span {
  padding: 3px 8px;
  border-radius: 4px;
  border: 1px solid var(--c-border);
  background: var(--c-bg-soft);
  color: var(--c-text-3);
  font-size: 12px;
}
.health-panel {
  padding: 18px;
}
.attention-panel { margin-top: 14px; padding: 16px; border: 1px solid rgba(214,69,69,.38); border-radius: 8px; background: linear-gradient(120deg,rgba(214,69,69,.09),transparent 52%),var(--c-bg-card); }
.attention-head,
.attention-heading,
.attention-item,
.attention-status { display: flex; align-items: center; }
.attention-head { justify-content: space-between; gap: 14px; margin-bottom: 10px; }
.attention-heading { gap: 10px; min-width: 0; }
.attention-heading > svg { width: 25px; height: 25px; color: #d64545; flex-shrink: 0; }
.attention-heading strong,
.attention-heading span { display: block; }
.attention-heading strong { color: var(--c-text); }
.attention-heading span { margin-top: 3px; color: var(--c-text-3); font-size: 12px; }
.attention-list { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 9px; }
.attention-item { min-width: 0; min-height: 78px; justify-content: space-between; gap: 12px; padding: 11px; border: 1px solid var(--c-border); border-radius: 7px; background: var(--c-bg-card); color: inherit; text-align: left; cursor: pointer; transition: border-color .2s,transform .2s; }
.attention-item:hover,
.attention-item:focus-visible { border-color: #d64545; transform: translateY(-1px); outline: none; }
.attention-item > div:first-child { min-width: 0; }
.attention-item strong,
.attention-item span { display: block; }
.attention-item strong { color: var(--c-text); }
.attention-item > div:first-child span { margin-top: 5px; color: var(--c-text-3); font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.attention-status { align-items: flex-end; flex-direction: column; flex-shrink: 0; }
.attention-status b { color: #d64545; font-size: 13px; }
.attention-status span,
.attention-status small { margin-top: 3px; color: var(--c-text-3); font-size: 11px; }
.water-attention { border-color: rgba(200,121,0,.4); background: linear-gradient(120deg,rgba(200,121,0,.1),transparent 52%),var(--c-bg-card); }
.water-attention .attention-heading > svg,
.water-attention .attention-status b { color: #c87900; }
.water-attention .attention-item:hover,
.water-attention .attention-item:focus-visible { border-color: #c87900; }
.task-attention { border-color: rgba(115,87,216,.4); background: linear-gradient(120deg,rgba(115,87,216,.09),transparent 52%),var(--c-bg-card); }
.task-attention .attention-heading > svg,
.task-attention .attention-status b { color: var(--c-purple); }
.task-attention .attention-status b.overdue { color: #d64545; }
.task-attention .attention-item:hover,
.task-attention .attention-item:focus-visible { border-color: var(--c-purple); }
.task-attention-item { min-width: 0; display: grid; grid-template-columns: minmax(0,1fr) auto; border: 1px solid var(--c-border); border-radius: 7px; background: var(--c-bg-card); overflow: hidden; }
.task-attention-main { min-width: 0; min-height: 78px; display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 11px; border: 0; background: transparent; color: inherit; text-align: left; cursor: pointer; }
.task-attention-main:hover,.task-attention-main:focus-visible { background: rgba(115,87,216,.06); outline: none; }
.task-attention-main > div:first-child { min-width: 0; }
.task-attention-main strong,.task-attention-main span { display: block; }
.task-attention-main strong { color: var(--c-text); }
.task-attention-main > div:first-child span { margin-top: 5px; color: var(--c-text-3); font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.task-quick-complete,.task-record-result { min-width: 62px; border: 0; border-left: 1px solid var(--c-border); font-family: inherit; font-size: 12px; font-weight: 800; cursor: pointer; }
.task-quick-complete { background: rgba(115,87,216,.08); color: var(--c-purple); }
.task-record-result { background: var(--c-cyan-dim); color: var(--c-cyan); }
.task-quick-complete:hover,.task-quick-complete:focus-visible,.task-record-result:hover,.task-record-result:focus-visible { background: rgba(115,87,216,.16); outline: 2px solid var(--c-purple); outline-offset: -2px; }
.task-quick-complete:disabled { opacity: .55; cursor: wait; }
.panel-title {
  color: var(--c-text);
  font-weight: 800;
}
.suggestion-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
  margin-top: 14px;
}
.stat-card,
.quick-grid button,
.mini-item,
.history-item {
  border: 1px solid var(--c-border);
  background: var(--c-bg-card);
  color: inherit;
  cursor: pointer;
  transition: border-color 0.2s, transform 0.2s, background 0.2s;
}
.stat-card:hover,
.quick-grid button:hover,
.mini-item:hover,
.history-item:hover {
  border-color: var(--c-cyan);
  transform: translateY(-1px);
}
.stat-card {
  min-height: 112px;
  padding: 14px;
  border-radius: 8px;
  text-align: left;
}
.stat-icon {
  width: 34px;
  height: 34px;
  display: inline-grid;
  place-items: center;
  border-radius: 8px;
  margin-bottom: 12px;
}
.stat-icon svg,
.quick-grid svg,
.empty-state svg,
.large-empty svg,
.history-placeholder svg,
.contact-list svg {
  width: 18px;
  height: 18px;
}
.stat-icon.cyan { color: #009bd6; background: rgba(0, 155, 214, 0.12); }
.stat-icon.green { color: #1f9d55; background: rgba(31, 157, 85, 0.13); }
.stat-icon.violet { color: #7357d8; background: rgba(115, 87, 216, 0.13); }
.stat-icon.amber { color: #c87900; background: rgba(200, 121, 0, 0.14); }
.stat-value {
  display: block;
  color: var(--c-text);
  font-size: 28px;
  line-height: 1;
  font-weight: 900;
}
.stat-label {
  display: block;
  margin-top: 8px;
  color: var(--c-text-3);
  font-size: 13px;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
  margin: 14px 0;
}
.quick-grid button {
  min-height: 52px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--c-text-2);
  font-weight: 700;
}
.overview-sections {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}
.overview-section {
  min-width: 0;
  padding: 14px;
}
.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}
.section-head span {
  display: block;
  color: var(--c-text);
  font-weight: 800;
}
.section-head small {
  display: block;
  margin-top: 4px;
  color: var(--c-text-3);
  font-size: 12px;
}
.mini-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.mini-item {
  width: 100%;
  min-height: 62px;
  padding: 10px;
  border-radius: 7px;
  text-align: left;
}
.mini-title,
.mini-meta {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.mini-title {
  color: var(--c-text-2);
  font-size: 14px;
  font-weight: 700;
}
.mini-meta {
  margin-top: 7px;
  color: var(--c-text-3);
  font-size: 12px;
}
.empty-state,
.large-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 10px;
  color: var(--c-text-3);
}
.empty-state {
  min-height: 165px;
}
.large-empty {
  min-height: 360px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: var(--c-bg-card);
  padding: 28px;
}
.large-empty svg {
  width: 42px;
  height: 42px;
  color: var(--c-cyan);
}
.large-empty h3 {
  margin: 0;
  color: var(--c-text);
}
.profile-panel {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 16px;
}
.profile-preview {
  align-self: start;
  padding: 22px;
  text-align: center;
}
.profile-preview p {
  margin: 10px 0 16px;
  text-align: left;
}
.contact-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: left;
}
.contact-list span {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  color: var(--c-text-2);
  font-size: 13px;
  overflow-wrap: anywhere;
}
.contact-list svg {
  color: var(--c-cyan);
  flex-shrink: 0;
}
.profile-form {
  padding: 20px;
}
.form-section-title {
  margin: 2px 0 14px;
  color: var(--c-text);
  font-weight: 800;
}
.form-section-title:not(:first-child) {
  margin-top: 10px;
}
.profile-actions,
.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.security-card { display: grid; grid-template-columns: minmax(220px,.7fr) minmax(0,1.3fr); gap: 24px; margin-top: 16px; padding: 20px; border: 1px solid var(--c-border); border-radius: 10px; background: var(--c-bg-card); }
.security-card small { color: var(--c-cyan); font: 800 10px ui-monospace; letter-spacing: .16em; }
.security-card h3 { margin: 6px 0; color: var(--c-text); }
.security-card p { margin: 0; color: var(--c-text-3); font-size: 12px; line-height: 1.6; }
.password-form { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)) auto; align-items: end; gap: 10px; }
.password-form label { display: grid; gap: 6px; color: var(--c-text-2); font-size: 11px; font-weight: 700; }
.password-form input { width: 100%; min-height: 42px; padding: 9px 11px; border: 1px solid var(--c-border); border-radius: 7px; outline: 0; background: var(--c-bg-soft); color: var(--c-text); }
.password-form input:focus { border-color: var(--c-cyan); }
@media(max-width:900px){.security-card{grid-template-columns:1fr}.password-form{grid-template-columns:1fr 1fr}.password-form .el-button{min-height:42px}}
@media(max-width:600px){.password-form{grid-template-columns:1fr}.password-form .el-button{min-height:44px}}
.history-panel {
  min-height: 330px;
}
.favorites-panel { min-height: 330px; }
.favorite-grid { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 14px; }
.favorite-card { min-width: 0; display: grid; grid-template-columns: 132px minmax(0,1fr); overflow: hidden; border: 1px solid var(--c-border); border-radius: 9px; background: var(--c-bg-card); }
.favorite-card.unavailable { opacity: .72; }
.favorite-cover { min-height: 176px; background-position: center; background-size: cover; }
.favorite-cover.placeholder { display: grid; place-items: center; background: linear-gradient(135deg,var(--c-bg-soft),var(--c-cyan-dim)); color: var(--c-cyan); }
.favorite-cover.placeholder svg { width: 34px; height: 34px; }
.favorite-body { min-width: 0; padding: 15px; }
.favorite-meta { display: flex; align-items: center; justify-content: space-between; gap: 8px; color: var(--c-text-3); font-size: 11px; }
.favorite-body h3 { margin: 12px 0 8px; color: var(--c-text); font-size: 17px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.favorite-body p { min-height: 42px; margin: 0 0 12px; color: var(--c-text-3); font-size: 13px; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.favorite-actions { display: flex; align-items: center; justify-content: space-between; }
.history-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}
.history-toolbar p {
  margin: 6px 0 0;
}
.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.history-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  border-radius: 8px;
  text-align: left;
}
.history-thumb,
.history-placeholder {
  width: 68px;
  height: 68px;
  border-radius: 7px;
  flex-shrink: 0;
}
.history-placeholder {
  display: grid;
  place-items: center;
  color: var(--c-cyan);
  background: var(--c-bg-soft);
  border: 1px solid var(--c-border);
}
.history-main {
  min-width: 0;
  flex: 1;
}
.history-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.history-title-row strong {
  color: var(--c-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.history-title-row span {
  color: var(--c-text-3);
  font-size: 12px;
  flex-shrink: 0;
}
.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 10px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.result-box {
  margin-top: 16px;
  padding: 14px;
  background: var(--c-bg-soft);
  border-radius: 8px;
}
.image-review {
  margin-top: 16px;
}
.detail-image {
  width: 100%;
  max-height: 360px;
  border-radius: 8px;
  border: 1px solid var(--c-border);
  background: var(--c-bg-soft);
}
.result-title {
  font-weight: 800;
  margin-bottom: 8px;
  color: var(--c-purple);
}
.result-content {
  font-size: 14px;
  line-height: 1.7;
  color: var(--c-text);
}
.follow-up-panel { margin-top: 16px; padding: 14px; border: 1px solid var(--c-border); border-radius: 8px; background: var(--c-bg-card); }
.follow-up-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 14px; }
.follow-up-head p { margin: 2px 0 0; color: var(--c-text-3); font-size: 13px; line-height: 1.6; }
.follow-up-alert { margin-top: 12px; }
.follow-up-form { margin-top: 14px; padding: 14px; border-radius: 8px; background: var(--c-bg-soft); }
.follow-up-editing { margin-bottom: 12px; padding: 9px 11px; border-left: 3px solid var(--c-cyan); border-radius: 4px; background: var(--c-cyan-dim); color: var(--c-text-2); font-size: 12px; }
.native-datetime { width: 100%; min-height: 40px; padding: 0 11px; border: 1px solid var(--el-border-color); border-radius: var(--el-border-radius-base); outline: none; background: var(--el-fill-color-blank); color: var(--el-text-color-regular); font: inherit; }
.native-datetime:focus { border-color: var(--el-color-primary); box-shadow: 0 0 0 1px var(--el-color-primary) inset; }
.follow-up-submit { display: flex; justify-content: flex-end; }
.follow-up-list { min-height: 52px; margin-top: 12px; }
.follow-up-item { position: relative; display: flex; align-items: flex-start; gap: 11px; padding: 12px 0; border-top: 1px solid var(--c-border); }
.follow-up-marker { width: 10px; height: 10px; margin-top: 6px; border-radius: 50%; background: #8a96a3; flex-shrink: 0; box-shadow: 0 0 0 4px rgba(138,150,163,.12); }
.follow-up-marker.improving { background: #1f9d55; box-shadow: 0 0 0 4px rgba(31,157,85,.12); }
.follow-up-marker.recovered { background: #00a878; box-shadow: 0 0 0 4px rgba(0,168,120,.16); }
.follow-up-marker.worsening { background: #d64545; box-shadow: 0 0 0 4px rgba(214,69,69,.12); }
.follow-up-copy { min-width: 0; flex: 1; }
.follow-up-meta { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.follow-up-meta strong { color: var(--c-text); }
.follow-up-meta span { color: var(--c-text-3); font-size: 12px; }
.follow-up-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 7px; }
.follow-up-tags span { padding: 3px 7px; border-radius: 4px; background: var(--c-bg-soft); color: var(--c-text-3); font-size: 12px; }
.follow-up-copy p { margin: 8px 0 0; color: var(--c-text-2); line-height: 1.65; overflow-wrap: anywhere; }
.follow-up-actions { display: flex; flex-shrink: 0; }
.follow-up-empty { padding: 16px 4px 4px; color: var(--c-text-3); font-size: 13px; text-align: center; }
@media (max-width: 980px) {
  .profile-hero,
  .identity {
    align-items: flex-start;
  }
  .profile-hero {
    flex-direction: column;
  }
  .hero-actions {
    justify-content: flex-start;
  }
  .insight-row,
  .profile-panel {
    grid-template-columns: 1fr;
  }
  .stats-grid,
  .quick-grid,
  .overview-sections {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .favorite-grid { grid-template-columns: 1fr; }
  .attention-list { grid-template-columns: 1fr; }
}
@media (max-width: 640px) {
  .profile-hero {
    padding: 16px;
  }
  .identity {
    flex-direction: column;
  }
  .identity h1 {
    font-size: 24px;
  }
  .hero-actions,
  .completion-panel,
  .history-toolbar {
    align-items: stretch;
    flex-direction: column;
  }
  .stats-grid,
  .quick-grid,
  .overview-sections {
    grid-template-columns: 1fr;
  }
  .history-item {
    align-items: flex-start;
  }
  .history-title-row {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
  }
  .history-title-row strong {
    white-space: normal;
  }
  .favorite-card { grid-template-columns: 92px minmax(0,1fr); }
  .favorite-cover { min-height: 170px; }
  .favorite-meta { align-items: flex-start; flex-direction: column; }
  .attention-head,
  .attention-item { align-items: stretch; flex-direction: column; }
  .attention-status { align-items: flex-start; }
  .task-attention-item { grid-template-columns: 1fr; }
  .task-attention-main { align-items: stretch; flex-direction: column; }
  .task-quick-complete,.task-record-result { min-height: 44px; border-left: 0; border-top: 1px solid var(--c-border); }
  .follow-up-head,
  .follow-up-meta { align-items: stretch; flex-direction: column; }
  .follow-up-item { flex-wrap: wrap; }
  .follow-up-item > .el-button { margin-left: 21px; }
  .detail-actions { flex-wrap: wrap; }
}
</style>
