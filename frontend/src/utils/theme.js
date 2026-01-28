/**
 * 主题切换工具
 * 支持默认主题、清新绿主题、暗夜黑主题
 */

// 主题配置
const themes = {
  default: {
    '--primary-color': '#409eff',
    '--bg-color': '#ffffff',
    '--text-color': '#303133',
    '--border-color': '#dcdfe6',
    '--header-bg': '#ffffff'
  },
  green: {
    '--primary-color': '#67C23A',
    '--bg-color': '#f0f9ff',
    '--text-color': '#2c3e50',
    '--border-color': '#b3e19d',
    '--header-bg': '#e8f5e9'
  },
  dark: {
    '--primary-color': '#409eff',
    '--bg-color': '#1a1a1a',
    '--text-color': '#e0e0e0',
    '--border-color': '#3a3a3a',
    '--header-bg': '#2d2d2d'
  }
}

/**
 * 应用主题
 * @param {string} themeName - 主题名称: default/green/dark
 */
export function applyTheme(themeName = 'default') {
  const theme = themes[themeName] || themes.default
  const root = document.documentElement
  
  Object.keys(theme).forEach(key => {
    root.style.setProperty(key, theme[key])
  })
  
  // 保存到本地存储
  localStorage.setItem('theme', themeName)
  
  // 暗黑主题特殊处理
  if (themeName === 'dark') {
    document.body.classList.add('dark-theme')
  } else {
    document.body.classList.remove('dark-theme')
  }
}

/**
 * 获取当前主题
 * @returns {string} 当前主题名称
 */
export function getCurrentTheme() {
  return localStorage.getItem('theme') || 'default'
}

/**
 * 初始化主题
 * 在应用启动时调用
 */
export function initTheme() {
  const savedTheme = getCurrentTheme()
  applyTheme(savedTheme)
}

/**
 * 获取主题列表
 * @returns {Array} 主题列表
 */
export function getThemeList() {
  return [
    { value: 'default', label: '默认主题', icon: '🎨' },
    { value: 'green', label: '清新绿', icon: '🌿' },
    { value: 'dark', label: '暗夜黑', icon: '🌙' }
  ]
}
