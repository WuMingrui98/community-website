function data_uv() {
    $.post(
        CONTEXT_PATH + "/data/uv",
        {"start":$("#start_uv").val(), "end":$("#end_uv").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code === 0) {
                $("#value_uv").text(data.msg);
            } else {
                alert(data.msg);
            }
        }
    )
}

function data_dau() {
    $.post(
        CONTEXT_PATH + "/data/dau",
        {"start":$("#start_dau").val(), "end":$("#end_dau").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code === 0) {
                $("#value_dau").text(data.msg);
            } else {
                alert(data.msg);
            }
        }
    )
}