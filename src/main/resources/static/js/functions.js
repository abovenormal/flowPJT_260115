const CONFIG = {
    API_BASE: '/api/extensions',
    WS_ENDPOINT: '/ws',
    WS_TOPIC: '/topic/extensions',
    MAX_CUSTOM: 200
};

const Dialog = {
    show(message, type = 'info') {
        const dialog = document.getElementById('alertDialog');
        const iconDiv = document.getElementById('alertIcon');
        const icon = iconDiv.querySelector('.material-icons');
        const title = document.getElementById('alertTitle');
        const msg = document.getElementById('alertMessage');

        const styles = {
            success: { bg: 'bg-green-100 dark:bg-green-900/30', color: 'text-green-600 dark:text-green-400', icon: 'check_circle', title: '성공' },
            error: { bg: 'bg-red-100 dark:bg-red-900/30', color: 'text-red-600 dark:text-red-400', icon: 'error', title: '오류' },
            warning: { bg: 'bg-amber-100 dark:bg-amber-900/30', color: 'text-amber-600 dark:text-amber-400', icon: 'warning', title: '경고' },
            info: { bg: 'bg-blue-100 dark:bg-blue-900/30', color: 'text-blue-600 dark:text-blue-400', icon: 'info', title: '알림' }
        };

        const style = styles[type] || styles.info;
        iconDiv.className = `flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center ${style.bg}`;
        icon.className = `material-icons text-xl ${style.color}`;
        icon.textContent = style.icon;
        title.textContent = style.title;
        msg.textContent = message;

        dialog.showModal();
    },

    confirm(message) {
        return new Promise((resolve) => {
            const dialog = document.getElementById('confirmDialog');
            const msg = document.getElementById('confirmMessage');
            msg.textContent = message;

            const okBtn = document.getElementById('confirmOkBtn');
            const cancelBtn = document.getElementById('confirmCancelBtn');

            const cleanup = () => {
                okBtn.removeEventListener('click', onOk);
                cancelBtn.removeEventListener('click', onCancel);
                dialog.close();
            };

            const onOk = () => { cleanup(); resolve(true); };
            const onCancel = () => { cleanup(); resolve(false); };

            okBtn.addEventListener('click', onOk);
            cancelBtn.addEventListener('click', onCancel);

            dialog.showModal();
        });
    },

    parseError(xhr, defaultMsg) {
        try {
            const response = JSON.parse(xhr.responseText);
            return response.message || defaultMsg;
        } catch (e) {
            return defaultMsg;
        }
    }
};

// API - 서버 통신 (AJAX)
const API = {
    getAll() {
        return $.ajax({
            url: CONFIG.API_BASE,
            method: 'GET'
        });
    },

    addCustom(name) {
        return $.ajax({
            url: `${CONFIG.API_BASE}/add`,
            method: 'POST',
            data: { customExtension: name }
        });
    },

    deleteCustom(name) {
        return $.ajax({
            url: `${CONFIG.API_BASE}/custom/${name}`,
            method: 'DELETE'
        });
    },

    saveFixed(name) {
        return $.ajax({
            url: `${CONFIG.API_BASE}/fixed/${name}`,
            method: 'POST'
        });
    },

    deleteFixed(name) {
        return $.ajax({
            url: `${CONFIG.API_BASE}/fixed/${name}`,
            method: 'DELETE'
        });
    },

    batchUpdateFixed(checked, unchecked) {
        return $.ajax({
            url: `${CONFIG.API_BASE}/fixed/batch`,
            method: 'PATCH',
            contentType: 'application/json',
            data: JSON.stringify({ checked, unchecked })
        });
    }
};

// Debounce - 배치 처리 유틸리티
const Debounce = {
    pendingChanges: { checked: new Set(), unchecked: new Set() },
    timer: null,

    add(name, isChecked) {
        if (isChecked) {
            this.pendingChanges.checked.add(name);
            this.pendingChanges.unchecked.delete(name);
        } else {
            this.pendingChanges.unchecked.add(name);
            this.pendingChanges.checked.delete(name);
        }
        this.schedule();
    },

    schedule() {
        clearTimeout(this.timer);
        this.timer = setTimeout(() => this.flush(), 300);
    },

    flush() {
        const checked = [...this.pendingChanges.checked];
        const unchecked = [...this.pendingChanges.unchecked];

        if (checked.length || unchecked.length) {
            API.batchUpdateFixed(checked, unchecked)
                .fail((xhr) => {
                    Dialog.show(Dialog.parseError(xhr, "저장에 실패했습니다."), 'error');
                    // 실패 시 UI 롤백
                    checked.forEach(ext => $(`.fixed-ext[value="${ext}"]`).prop('checked', false));
                    unchecked.forEach(ext => $(`.fixed-ext[value="${ext}"]`).prop('checked', true));
                });
        }

        this.pendingChanges = { checked: new Set(), unchecked: new Set() };
    }
};

// WS - WebSocket 연결
const WS = {
    client: null,

    connect(onMessage) {
        const socket = new SockJS(CONFIG.WS_ENDPOINT);
        this.client = Stomp.over(socket);
        this.client.debug = null; // 디버그 로그 비활성화

        this.client.connect({}, (frame) => {
            console.log('WebSocket 연결 성공');
            this.client.subscribe(CONFIG.WS_TOPIC, (message) => {
                const data = JSON.parse(message.body);
                console.log('WebSocket 메시지 수신:', data);
                onMessage(data);
            });
        }, (error) => {
            console.error('WebSocket 연결 실패:', error);
            setTimeout(() => this.connect(onMessage), 3000); // 3초 후 재연결
        });
    },

    disconnect() {
        if (this.client) {
            this.client.disconnect();
            this.client = null;
        }
    }
};

// UI - 화면 렌더링
const UI = {
    update(data) {
        // Delta 업데이트 처리
        if (data.type === 'delta') {
            if (data.fixedAdded) {
                data.fixedAdded.forEach(ext => $(`.fixed-ext[value="${ext}"]`).prop('checked', true));
            }
            if (data.fixedRemoved) {
                data.fixedRemoved.forEach(ext => $(`.fixed-ext[value="${ext}"]`).prop('checked', false));
            }
            return;
        }

        $('.fixed-ext').prop('checked', false);
        if (data.fixed && data.fixed.length > 0) {
            data.fixed.forEach((ext) => {
                $(`.fixed-ext[value="${ext}"]`).prop('checked', true);
            });
        }

        $('#customExtensionList').empty();
        if (data.custom) {
            data.custom.forEach((ext) => {
                UI.addChip(ext);
            });
        }
        $('#currentCount').text(data.count || 0);
    },

    addChip(name) {
        const html = `
            <div class="custom-chip inline-flex items-center gap-2 px-3 py-1.5 bg-white dark:bg-slate-700 border border-slate-200 dark:border-slate-600 rounded-md shadow-sm">
                <span class="text-sm font-medium text-slate-700 dark:text-slate-200">${name}</span>
                <button class="delete-custom-btn text-slate-400 hover:text-red-500" data-ext="${name}">
                    <span class="material-icons text-sm">close</span>
                </button>
            </div>`;
        $('#customExtensionList').append(html);
    }
};
