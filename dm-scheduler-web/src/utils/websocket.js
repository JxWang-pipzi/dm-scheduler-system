/**
 * WebSocket 服务工具
 * 在用户登录后建立 WebSocket 长连接，支持消息收发和自动断线重连。
 * 全局单例模式，由 Layout.vue 统一管理生命周期。
 */

let ws = null;       // WebSocket 实例
let heartbeat = null; // 心跳定时器
let reconnectTimer = null; // 重连定时器
let messageHandlers = []; // 消息回调函数列表

/**
 * 初始化 WebSocket 连接
 * @param {number} userId 当前用户 ID
 * @param {string} role   当前用户角色 (ADMIN / DM / CUSTOMER)
 */
export function connectWebSocket(userId, role) {
    if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
        console.log('[WS] 已存在活跃连接，跳过重复连接');
        return;
    }

    // 动态获取后端 WebSocket 地址
    const host = window.location.hostname || 'localhost';
    const wsPort = '8081'; // 后端服务端口（Spring Boot 实际端口）
    const wsUrl = `ws://${host}:${wsPort}/ws/dm?userId=${userId}&role=${role}`;

    console.log('[WS] 正在连接:', wsUrl);
    ws = new WebSocket(wsUrl);

    ws.onopen = () => {
        console.log('[WS] 连接成功 ✅');
        // 启动心跳保活（每 30 秒发一次 ping）
        startHeartbeat();
    };

    ws.onmessage = (event) => {
        console.log('[WS] 收到消息:', event.data);
        try {
            const data = JSON.parse(event.data);
            // 通知所有已注册的回调函数
            messageHandlers.forEach(handler => {
                try { handler(data); } catch (e) { console.error('[WS] 处理消息回调出错:', e); }
            });
        } catch (e) {
            console.error('[WS] 解析消息失败:', e);
        }
    };

    ws.onclose = (event) => {
        console.log('[WS] 连接关闭，code=' + event.code);
        stopHeartbeat();
        // 非正常关闭时自动重连（排除手动断开的情况）
        if (event.code !== 1000) {
            scheduleReconnect(userId, role);
        }
    };

    ws.onerror = (error) => {
        console.error('[WS] 连接出错:', error);
    };
}

/**
 * 发送消息到后端
 * @param {object} data 要发送的 JSON 对象
 */
export function sendMessage(data) {
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify(data));
        return true;
    }
    console.warn('[WS] 连接未就绪，无法发送消息');
    return false;
}

/**
 * 注册消息回调
 * @param {function} handler 接收到消息时的回调 (data) => {}
 * @returns {function} 取消注册的函数
 */
export function onMessage(handler) {
    messageHandlers.push(handler);
    // 返回一个能取消注册的函数
    return () => {
        messageHandlers = messageHandlers.filter(h => h !== handler);
    };
}

/**
 * 主动断开 WebSocket 连接
 */
export function disconnectWebSocket() {
    stopHeartbeat();
    clearReconnectTimer();
    if (ws) {
        ws.close(1000, '用户主动断开');
        ws = null;
    }
    messageHandlers = [];
    console.log('[WS] 已主动断开连接');
}

/* ==================== 内部工具方法 ==================== */

function startHeartbeat() {
    stopHeartbeat();
    heartbeat = setInterval(() => {
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({ type: 'PING' }));
        }
    }, 30000); // 每 30 秒
}

function stopHeartbeat() {
    if (heartbeat) {
        clearInterval(heartbeat);
        heartbeat = null;
    }
}

function scheduleReconnect(userId, role) {
    clearReconnectTimer();
    reconnectTimer = setTimeout(() => {
        console.log('[WS] 尝试自动重连...');
        connectWebSocket(userId, role);
    }, 3000); // 3 秒后重连
}

function clearReconnectTimer() {
    if (reconnectTimer) {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
    }
}
