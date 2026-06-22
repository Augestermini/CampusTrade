// src/api/index.js
import axios from 'axios'

// ---------- 1. 创建请求工具（带拦截器） ----------
// 如果项目里还没装 axios，终端执行：npm install axios --registry=https://registry.npmmirror.com

const request = axios.create({
  baseURL: '/api',   // 对接 vue.config.js 里的代理
  timeout: 10000
})

// 请求拦截器：每次请求自动带上 token（用于后续联调）
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一处理返回数据
request.interceptors.response.use(
  response => {
    const res = response.data
    // 如果后端返回 code === 401，说明登录失效，跳转到登录页
    if (res.code === 401) {
      localStorage.removeItem('token')
      // 如果你的项目路由里有登录页，可以取消下面这行的注释
      // window.location.href = '/login'
    }
    return res
  },
  error => {
    console.error('请求出错：', error)
    return Promise.reject(error)
  }
)

// ---------- 2. 下面是你的“假数据”接口函数 ----------
// 等后端真实接口写好，只需要替换这些函数的内部实现即可，
// 页面调用的代码（如 import { login } from '@/api'）完全不用改。

// 登录接口
export const login = (data) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 0,
        data: {
          token: 'fake-token-123456',
          username: data.username || '测试用户'
        },
        msg: '登录成功'
      })
    }, 500)
  })
}

// 获取商品列表
export const getGoodsList = (params) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 0,
        data: [
          {
            id: 1,
            title: '二手iPhone 14',
            price: 3999,
            description: '99新，买了3个月，带发票',
            pictureList: ['https://picsum.photos/200/200?random=1'],
            category: '电子产品',
            status: 0,
            createTime: '2026-06-20'
          },
          {
            id: 2,
            title: '考研数学全书',
            price: 30,
            description: '去年考研用的，写了前3章，后面全新',
            pictureList: ['https://picsum.photos/200/200?random=2'],
            category: '书籍',
            status: 0,
            createTime: '2026-06-18'
          },
          {
            id: 3,
            title: '捷安特自行车',
            price: 500,
            description: '大四毕业出，骑了两年，功能完好',
            pictureList: ['https://picsum.photos/200/200?random=3'],
            category: '交通工具',
            status: 0,
            createTime: '2026-06-15'
          }
        ],
        total: 3,
        msg: '获取成功'
      })
    }, 300)
  })
}

// 获取商品详情
export const getGoodsDetail = (id) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 0,
        data: {
          id: id,
          title: '二手iPhone 14',
          price: 3999,
          description: '99新，买了3个月，带发票，盒子配件都在',
          pictureList: [
            'https://picsum.photos/400/400?random=10',
            'https://picsum.photos/400/400?random=11',
            'https://picsum.photos/400/400?random=12'
          ],
          category: '电子产品',
          status: 0,
          createTime: '2026-06-20',
          userId: 1,
          userName: '张三',
          userAvatar: 'https://picsum.photos/100/100?random=99'
        },
        msg: '获取成功'
      })
    }, 300)
  })
}

// 发布商品
export const releaseGoods = (data) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 0,
        data: { id: Date.now() },
        msg: '发布成功'
      })
    }, 500)
  })
}
// 获取商品列表（分页，带分类筛选）
export const findIdleTiem = (params) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 0,
        data: {
          list: [
            {
              id: 1,
              idleName: '二手iPhone 14',
              idlePrice: 3999,
              idlePlace: 'A栋宿舍',
              releaseTime: '2026-06-20 14:30:00',
              pictureList: '["https://picsum.photos/200/200?random=1"]',
              user: {
                nickname: '张三',
                avatar: 'https://picsum.photos/100/100?random=99'
              }
            },
            {
              id: 2,
              idleName: '考研数学全书',
              idlePrice: 30,
              idlePlace: '图书馆',
              releaseTime: '2026-06-18 10:00:00',
              pictureList: '["https://picsum.photos/200/200?random=2"]',
              user: {
                nickname: '李四',
                avatar: 'https://picsum.photos/100/100?random=98'
              }
            },
            {
              id: 3,
              idleName: '捷安特自行车',
              idlePrice: 500,
              idlePlace: 'B栋车棚',
              releaseTime: '2026-06-15 16:20:00',
              pictureList: '["https://picsum.photos/200/200?random=3"]',
              user: {
                nickname: '王五',
                avatar: 'https://picsum.photos/100/100?random=97'
              }
            }
          ],
          count: 3
        },
        msg: '获取成功'
      })
    }, 300)
  })
}

// 按分类获取商品列表
export const findIdleTiemByLable = (params) => {
  // 复用 findIdleTiem 的数据，但可以加一些分类标识
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 0,
        data: {
          list: [
            {
              id: 4,
              idleName: `分类${params.idleLabel}的商品示例`,
              idlePrice: 99,
              idlePlace: 'C栋',
              releaseTime: '2026-06-16 09:00:00',
              pictureList: '["https://picsum.photos/200/200?random=4"]',
              user: {
                nickname: '赵六',
                avatar: 'https://picsum.photos/100/100?random=96'
              }
            }
          ],
          count: 1
        },
        msg: '获取成功'
      })
    }, 300)
  })
}
// 注册接口
export const register = (data) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 0,
        data: { userId: 10001 },
        msg: '注册成功'
      })
    }, 500)
  })
}
// 默认导出所有接口函数
export default {
  login,
  register,
  getGoodsList,
  getGoodsDetail,
  releaseGoods,
  findIdleTiem,
  findIdleTiemByLable
}