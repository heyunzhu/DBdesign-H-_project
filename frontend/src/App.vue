<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">L</span>
        <div>
          <h1>图书借阅管理</h1>
          <p>Library Console</p>
        </div>
      </div>

      <nav class="nav-list">
        <button
          v-for="item in navItems"
          :key="item.key"
          class="nav-item"
          :class="{ active: activeView === item.key }"
          type="button"
          @click="activeView = item.key"
        >
          <span>{{ item.icon }}</span>
          {{ item.label }}
        </button>
      </nav>
    </aside>

    <main class="main">
      <header class="topbar">
        <div>
          <p class="eyebrow">数据库课程项目</p>
          <h2>{{ currentTitle }}</h2>
        </div>
        <div class="server-pill">Backend: localhost:8081</div>
      </header>

      <div v-if="message.text" class="toast" :class="message.type">
        {{ message.text }}
      </div>

      <section v-show="activeView === 'books'" class="panel-grid">
        <div class="panel panel-wide">
          <div class="panel-header">
            <div>
              <h3>图书列表</h3>
              <p>查询、筛选、下架馆藏图书</p>
            </div>
            <button class="primary-button" type="button" @click="loadBooks">刷新</button>
          </div>

          <div class="toolbar">
            <input v-model.trim="bookFilters.keyword" placeholder="书名 / ISBN / 作者" @keyup.enter="loadBooks" />
            <select v-model="bookFilters.status" @change="loadBooks">
              <option value="">全部状态</option>
              <option value="0">在馆可借</option>
              <option value="1">借出中</option>
              <option value="2">停用/下架</option>
            </select>
            <button type="button" @click="loadBooks">查询</button>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>书名</th>
                  <th>作者</th>
                  <th>分类</th>
                  <th>ISBN</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="book in books" :key="book.bookId">
                  <td>{{ book.bookId }}</td>
                  <td>{{ book.bookName }}</td>
                  <td>{{ book.authorName }}</td>
                  <td>{{ book.typeName }}</td>
                  <td>{{ book.isbn }}</td>
                  <td><span class="status" :class="`book-${book.bookStatus}`">{{ bookStatusText(book.bookStatus) }}</span></td>
                  <td class="actions">
                    <button type="button" @click="editBook(book)">编辑</button>
                    <button type="button" @click="disableBook(book.bookId)">下架</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="panel">
          <div class="panel-header">
            <div>
              <h3>{{ editingBookId ? '编辑图书' : '新增图书' }}</h3>
              <p>作者和分类来自基础数据</p>
            </div>
          </div>

          <form class="form" @submit.prevent="saveBook">
            <label>ISBN<input v-model.trim="bookForm.isbn" required /></label>
            <label>书名<input v-model.trim="bookForm.bookName" required /></label>
            <label>作者
              <select v-model.number="bookForm.authorId" required>
                <option disabled value="">选择作者</option>
                <option v-for="author in authors" :key="author.authorId" :value="author.authorId">{{ author.authorName }}</option>
              </select>
            </label>
            <label>分类
              <select v-model.number="bookForm.typeId" required>
                <option disabled value="">选择分类</option>
                <option v-for="type in bookTypes" :key="type.typeId" :value="type.typeId">{{ type.typeName }}</option>
              </select>
            </label>
            <label>出版社<input v-model.trim="bookForm.publisher" /></label>
            <label>出版日期<input v-model="bookForm.publishDate" type="date" /></label>
            <label v-if="editingBookId">状态
              <select v-model.number="bookForm.bookStatus">
                <option :value="0">在馆可借</option>
                <option :value="1">借出中</option>
                <option :value="2">停用/下架</option>
              </select>
            </label>
            <div class="form-actions">
              <button class="primary-button" type="submit">{{ editingBookId ? '保存' : '新增' }}</button>
              <button type="button" @click="resetBookForm">清空</button>
            </div>
          </form>
        </div>
      </section>

      <section v-show="activeView === 'users'" class="panel-grid">
        <div class="panel panel-wide">
          <div class="panel-header">
            <div>
              <h3>用户列表</h3>
              <p>读者、管理员与账号状态</p>
            </div>
            <button class="primary-button" type="button" @click="loadUsers">刷新</button>
          </div>

          <div class="toolbar">
            <input v-model.trim="userFilters.keyword" placeholder="学号/工号 / 姓名 / 部门" @keyup.enter="loadUsers" />
            <select v-model="userFilters.roleId" @change="loadUsers">
              <option value="">全部角色</option>
              <option v-for="role in roles" :key="role.roleId" :value="role.roleId">{{ role.roleName }}</option>
            </select>
            <select v-model="userFilters.status" @change="loadUsers">
              <option value="">全部状态</option>
              <option value="1">正常</option>
              <option value="0">禁用</option>
            </select>
            <button type="button" @click="loadUsers">查询</button>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>学号/工号</th>
                  <th>姓名</th>
                  <th>部门</th>
                  <th>角色</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in users" :key="user.userId">
                  <td>{{ user.userId }}</td>
                  <td>{{ user.userNo }}</td>
                  <td>{{ user.userName }}</td>
                  <td>{{ user.deptName }}</td>
                  <td>{{ user.roleName }}</td>
                  <td><span class="status" :class="user.accountStatus === 1 ? 'ok' : 'off'">{{ user.accountStatus === 1 ? '正常' : '禁用' }}</span></td>
                  <td class="actions">
                    <button type="button" @click="editUser(user)">编辑</button>
                    <button type="button" @click="toggleUserStatus(user)">{{ user.accountStatus === 1 ? '禁用' : '启用' }}</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="panel">
          <div class="panel-header">
            <div>
              <h3>{{ editingUserId ? '编辑用户' : '新增用户' }}</h3>
              <p>新增用户默认正常状态</p>
            </div>
          </div>

          <form class="form" @submit.prevent="saveUser">
            <label>学号/工号<input v-model.trim="userForm.userNo" required /></label>
            <label>姓名<input v-model.trim="userForm.userName" required /></label>
            <label>电话<input v-model.trim="userForm.phone" /></label>
            <label>学院/部门<input v-model.trim="userForm.deptName" /></label>
            <label>角色
              <select v-model.number="userForm.roleId" required>
                <option disabled value="">选择角色</option>
                <option v-for="role in roles" :key="role.roleId" :value="role.roleId">{{ role.roleName }}</option>
              </select>
            </label>
            <label v-if="editingUserId">状态
              <select v-model.number="userForm.accountStatus">
                <option :value="1">正常</option>
                <option :value="0">禁用</option>
              </select>
            </label>
            <div class="form-actions">
              <button class="primary-button" type="submit">{{ editingUserId ? '保存' : '新增' }}</button>
              <button type="button" @click="resetUserForm">清空</button>
            </div>
          </form>
        </div>
      </section>

      <section v-show="activeView === 'borrows'" class="panel-grid">
        <div class="panel">
          <div class="panel-header">
            <div>
              <h3>办理借阅</h3>
              <p>只允许正常用户借阅可借图书</p>
            </div>
          </div>
          <form class="form" @submit.prevent="borrowBook">
            <label>用户
              <select v-model.number="borrowForm.userId" required>
                <option disabled value="">选择用户</option>
                <option v-for="user in activeUsers" :key="user.userId" :value="user.userId">{{ user.userName }}（{{ user.userNo }}）</option>
              </select>
            </label>
            <label>图书
              <select v-model.number="borrowForm.bookId" required>
                <option disabled value="">选择图书</option>
                <option v-for="book in availableBooks" :key="book.bookId" :value="book.bookId">{{ book.bookName }} / {{ book.isbn }}</option>
              </select>
            </label>
            <label>应归还时间<input v-model="borrowForm.dueReturnTime" type="datetime-local" /></label>
            <button class="primary-button" type="submit">借书</button>
          </form>
        </div>

        <div class="panel panel-wide">
          <div class="panel-header">
            <div>
              <h3>借阅记录</h3>
              <p>支持按状态筛选并办理归还</p>
            </div>
            <button class="primary-button" type="button" @click="loadBorrowRecords">刷新</button>
          </div>

          <div class="toolbar">
            <select v-model="borrowFilters.status" @change="loadBorrowRecords">
              <option value="">全部状态</option>
              <option value="0">未归还</option>
              <option value="1">已归还</option>
              <option value="2">逾期</option>
            </select>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户</th>
                  <th>图书</th>
                  <th>借阅时间</th>
                  <th>应还时间</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="record in borrowRecords" :key="record.borrowId">
                  <td>{{ record.borrowId }}</td>
                  <td>{{ record.userName }}</td>
                  <td>{{ record.bookName }}</td>
                  <td>{{ formatTime(record.borrowTime) }}</td>
                  <td>{{ formatTime(record.dueReturnTime) }}</td>
                  <td><span class="status" :class="`borrow-${record.borrowStatus}`">{{ borrowStatusText(record.borrowStatus) }}</span></td>
                  <td class="actions">
                    <button v-if="record.borrowStatus !== 1" type="button" @click="returnBook(record.borrowId)">归还</button>
                    <span v-else class="muted">已完成</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section v-show="activeView === 'stats'" class="panel-grid stats-grid">
        <div class="panel">
          <div class="panel-header">
            <div>
              <h3>分类馆藏数量</h3>
              <p>聚合查询</p>
            </div>
          </div>
          <div class="metric-list">
            <div v-for="item in categoryBooks" :key="item.typeId" class="metric-row">
              <span>{{ item.typeName }}</span>
              <strong>{{ item.bookCount }}</strong>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-header">
            <div>
              <h3>分类借阅排行</h3>
              <p>业务统计查询</p>
            </div>
          </div>
          <div class="metric-list">
            <div v-for="item in borrowRanking" :key="item.typeId" class="metric-row">
              <span>{{ item.typeName }}</span>
              <strong>{{ item.borrowCount }}</strong>
            </div>
          </div>
        </div>

        <div class="panel panel-wide">
          <div class="panel-header">
            <div>
              <h3>逾期未还</h3>
              <p>当前时间维度下的异常记录</p>
            </div>
            <button class="primary-button" type="button" @click="loadStats">刷新</button>
          </div>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>记录ID</th>
                  <th>用户</th>
                  <th>图书</th>
                  <th>应还时间</th>
                  <th>逾期天数</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="record in overdueRecords" :key="record.borrowId">
                  <td>{{ record.borrowId }}</td>
                  <td>{{ record.userName }}</td>
                  <td>{{ record.bookName }}</td>
                  <td>{{ formatTime(record.dueReturnTime) }}</td>
                  <td>{{ record.overdueDays }}</td>
                </tr>
                <tr v-if="overdueRecords.length === 0">
                  <td colspan="5" class="empty-cell">暂无逾期未还记录</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { api } from './api'

const navItems = [
  { key: 'books', label: '图书管理', icon: 'B' },
  { key: 'users', label: '用户管理', icon: 'U' },
  { key: 'borrows', label: '借阅管理', icon: 'R' },
  { key: 'stats', label: '统计查询', icon: 'S' }
]

const activeView = ref('books')
const message = reactive({ text: '', type: 'success' })

const books = ref([])
const users = ref([])
const borrowRecords = ref([])
const authors = ref([])
const bookTypes = ref([])
const roles = ref([])
const categoryBooks = ref([])
const borrowRanking = ref([])
const overdueRecords = ref([])

const bookFilters = reactive({ keyword: '', status: '' })
const userFilters = reactive({ keyword: '', roleId: '', status: '' })
const borrowFilters = reactive({ status: '' })

const editingBookId = ref(null)
const bookForm = reactive({
  isbn: '',
  bookName: '',
  authorId: '',
  typeId: '',
  publisher: '',
  publishDate: '',
  bookStatus: 0
})

const editingUserId = ref(null)
const userForm = reactive({
  userNo: '',
  userName: '',
  phone: '',
  deptName: '',
  roleId: '',
  accountStatus: 1
})

const borrowForm = reactive({
  userId: '',
  bookId: '',
  dueReturnTime: ''
})

const currentTitle = computed(() => navItems.find((item) => item.key === activeView.value)?.label || '')
const availableBooks = computed(() => books.value.filter((book) => book.bookStatus === 0))
const activeUsers = computed(() => users.value.filter((user) => user.accountStatus === 1))

watch(activeView, async (view) => {
  if (view === 'books') await loadBooks()
  if (view === 'users') await loadUsers()
  if (view === 'borrows') await Promise.all([loadBooks(), loadUsers(), loadBorrowRecords()])
  if (view === 'stats') await loadStats()
})

onMounted(async () => {
  await Promise.all([loadBasicData(), loadBooks(), loadUsers(), loadBorrowRecords(), loadStats()])
})

function showMessage(text, type = 'success') {
  message.text = text
  message.type = type
  window.clearTimeout(showMessage.timer)
  showMessage.timer = window.setTimeout(() => {
    message.text = ''
  }, 2600)
}

function toQuery(params) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== '' && value !== null && value !== undefined) query.set(key, value)
  })
  const text = query.toString()
  return text ? `?${text}` : ''
}

async function safeRun(fn, successText) {
  try {
    await fn()
    if (successText) showMessage(successText)
  } catch (error) {
    showMessage(error.message, 'error')
  }
}

async function loadBasicData() {
  await safeRun(async () => {
    const [authorData, typeData, roleData] = await Promise.all([
      api.get('/authors'),
      api.get('/book-types'),
      api.get('/roles')
    ])
    authors.value = authorData || []
    bookTypes.value = typeData || []
    roles.value = roleData || []
  })
}

async function loadBooks() {
  await safeRun(async () => {
    books.value = await api.get(`/books${toQuery(bookFilters)}`) || []
  })
}

async function saveBook() {
  await safeRun(async () => {
    const data = {
      isbn: bookForm.isbn,
      bookName: bookForm.bookName,
      authorId: bookForm.authorId,
      typeId: bookForm.typeId,
      publisher: bookForm.publisher,
      publishDate: bookForm.publishDate || null
    }
    if (editingBookId.value) {
      await api.put(`/books/${editingBookId.value}`, { ...data, bookStatus: bookForm.bookStatus })
    } else {
      await api.post('/books', data)
    }
    resetBookForm()
    await loadBooks()
  }, editingBookId.value ? '图书已保存' : '图书已新增')
}

function editBook(book) {
  editingBookId.value = book.bookId
  Object.assign(bookForm, {
    isbn: book.isbn,
    bookName: book.bookName,
    authorId: book.authorId,
    typeId: book.typeId,
    publisher: book.publisher || '',
    publishDate: book.publishDate || '',
    bookStatus: book.bookStatus
  })
}

function resetBookForm() {
  editingBookId.value = null
  Object.assign(bookForm, {
    isbn: '',
    bookName: '',
    authorId: '',
    typeId: '',
    publisher: '',
    publishDate: '',
    bookStatus: 0
  })
}

async function disableBook(bookId) {
  if (!window.confirm('确认下架这本图书？')) return
  await safeRun(async () => {
    await api.delete(`/books/${bookId}`)
    await loadBooks()
  }, '图书已下架')
}

async function loadUsers() {
  await safeRun(async () => {
    users.value = await api.get(`/users${toQuery(userFilters)}`) || []
  })
}

async function saveUser() {
  await safeRun(async () => {
    const data = {
      userNo: userForm.userNo,
      userName: userForm.userName,
      phone: userForm.phone,
      deptName: userForm.deptName,
      roleId: userForm.roleId
    }
    if (editingUserId.value) {
      await api.put(`/users/${editingUserId.value}`, { ...data, accountStatus: userForm.accountStatus })
    } else {
      await api.post('/users', data)
    }
    resetUserForm()
    await loadUsers()
  }, editingUserId.value ? '用户已保存' : '用户已新增')
}

function editUser(user) {
  editingUserId.value = user.userId
  Object.assign(userForm, {
    userNo: user.userNo,
    userName: user.userName,
    phone: user.phone || '',
    deptName: user.deptName || '',
    roleId: user.roleId,
    accountStatus: user.accountStatus
  })
}

function resetUserForm() {
  editingUserId.value = null
  Object.assign(userForm, {
    userNo: '',
    userName: '',
    phone: '',
    deptName: '',
    roleId: '',
    accountStatus: 1
  })
}

async function toggleUserStatus(user) {
  const nextStatus = user.accountStatus === 1 ? 0 : 1
  await safeRun(async () => {
    await api.put(`/users/${user.userId}/status`, { accountStatus: nextStatus })
    await loadUsers()
  }, nextStatus === 1 ? '用户已启用' : '用户已禁用')
}

async function loadBorrowRecords() {
  await safeRun(async () => {
    borrowRecords.value = await api.get(`/borrows${toQuery(borrowFilters)}`) || []
  })
}

async function borrowBook() {
  await safeRun(async () => {
    await api.post('/borrows', {
      userId: borrowForm.userId,
      bookId: borrowForm.bookId,
      dueReturnTime: borrowForm.dueReturnTime || null
    })
    Object.assign(borrowForm, { userId: '', bookId: '', dueReturnTime: '' })
    await Promise.all([loadBooks(), loadBorrowRecords()])
  }, '借阅办理成功')
}

async function returnBook(borrowId) {
  await safeRun(async () => {
    await api.put(`/borrows/${borrowId}/return`)
    await Promise.all([loadBooks(), loadBorrowRecords(), loadStats()])
  }, '归还办理成功')
}

async function loadStats() {
  await safeRun(async () => {
    const [categoryData, overdueData, rankingData] = await Promise.all([
      api.get('/statistics/category-books'),
      api.get('/statistics/overdue-records'),
      api.get('/statistics/borrow-ranking')
    ])
    categoryBooks.value = categoryData || []
    overdueRecords.value = overdueData || []
    borrowRanking.value = rankingData || []
  })
}

function bookStatusText(status) {
  return ['在馆可借', '借出中', '停用/下架'][status] || '未知'
}

function borrowStatusText(status) {
  return ['未归还', '已归还', '逾期'][status] || '未知'
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}
</script>
