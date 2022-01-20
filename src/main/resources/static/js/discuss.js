$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});


function setTop() {
    var btn = this;
    // 置顶
    if ($(btn).hasClass("btn-danger")) {
        $.post(
            CONTEXT_PATH + "/discuss/top",
            {"id": $("#postId").val()},
            function (data) {
                data = $.parseJSON(data);
                if (data.code === 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
    } else {
        // 取消置顶
        $.post(
            CONTEXT_PATH + "/discuss/notop",
            {"id": $("#postId").val()},
            function (data) {
                data = $.parseJSON(data);
                if (data.code === 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
    }
}

// 加精
function setWonderful() {
    var btn = this;
    // 加精
    if ($(btn).hasClass("btn-danger")) {
        $.post(
            CONTEXT_PATH + "/discuss/wonderful",
            {"id": $("#postId").val()},
            function (data) {
                data = $.parseJSON(data);
                if (data.code === 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
    } else {
        // 取消加精
        $.post(
            CONTEXT_PATH + "/discuss/nowonderful",
            {"id": $("#postId").val()},
            function (data) {
                data = $.parseJSON(data);
                if (data.code === 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
    }
}

// 删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code === 0) {
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    );
}