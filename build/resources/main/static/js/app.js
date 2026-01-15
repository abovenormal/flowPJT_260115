$(document).ready(function() {
    // ============================================
    // 초기화
    // ============================================

    // 초기 데이터 로드 (ApiResponse 래퍼에서 data 추출)
    API.getAll().then(response => UI.update(response.data));

    // WebSocket 연결
    WS.connect(UI.update);

    // Dialog 닫기 버튼
    $('#alertCloseBtn').click(function() {
        document.getElementById('alertDialog').close();
    });

    // ============================================
    // 이벤트 바인딩
    // ============================================

    // 고정 확장자 체크박스 변경
    $('.fixed-ext').change(handleFixedChange);

    // 커스텀 확장자 추가 버튼
    $('#addBtn').click(handleAddClick);

    // 커스텀 확장자 삭제 버튼 (동적 요소)
    $(document).on('click', '.delete-custom-btn', handleDeleteClick);
});

// ============================================
// 이벤트 핸들러
// ============================================

/**
 * 고정 확장자 체크박스 변경 핸들러 (Debounce 적용)
 */
function handleFixedChange() {
    const extName = $(this).val();
    const isChecked = $(this).is(':checked');
    Debounce.add(extName, isChecked);
}

/**
 * 커스텀 확장자 추가 버튼 핸들러
 */
function handleAddClick() {
    const input = $('#customInput');
    const extName = input.val().trim();

    // 빈 값 체크
    if (!extName) {
        Dialog.show("확장자를 입력해주세요.", 'warning');
        input.focus();
        return;
    }

    // 숫자 포함 여부 체크
    if (/\d/.test(extName)) {
        Dialog.show("확장자에 숫자를 포함할 수 없습니다.", 'warning');
        input.focus();
        return;
    }

    // 한글 포함 여부 체크
    if (/[ㄱ-ㅎㅏ-ㅣ가-힣]/.test(extName)) {
        Dialog.show("확장자에 한글을 입력할 수 없습니다. 영문으로 입력해주세요.", 'warning');
        input.focus();
        return;
    }

    API.addCustom(extName)
        .done(() => {
            Dialog.show("커스텀 확장자가 저장되었습니다!", 'success');
            input.val('');
            // WebSocket에서 UI 자동 업데이트됨
        })
        .fail((xhr) => {
            Dialog.show(Dialog.parseError(xhr, "처리 중 오류가 발생했습니다."), 'error');
        });
}

/**
 * 커스텀 확장자 삭제 버튼 핸들러
 */
async function handleDeleteClick() {
    const extName = $(this).data('ext');

    const confirmed = await Dialog.confirm(`'${extName}' 확장자를 삭제하시겠습니까?`);
    if (confirmed) {
        API.deleteCustom(extName)
            .fail((xhr) => {
                Dialog.show(Dialog.parseError(xhr, "삭제에 실패했습니다."), 'error');
            });
        // WebSocket에서 UI 자동 업데이트됨
    }
}
