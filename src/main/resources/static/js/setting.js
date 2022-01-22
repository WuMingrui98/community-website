$(function () {
    bsCustomFileInput.init();
    $("input").focus(clear_error);
    $("#pwd-form").submit(function () {
        var cond1 = check_new_pwd();
        var cond2 = check_old_pwd();
        var cond3 = check_data();
        return cond1 && cond2 && cond3;
    });

    $("#uploadForm").submit(upload);
});


function upload() {
    $.ajax({
        url: "http://upload-z2.qiniup.com",
        method: "post",
        processData: false,
        contentType: false,
        data: new FormData($("#uploadForm")[0]),
        /*成功后，将头像地址更新进数据库*/
        success: function (data) {
            // 在后端定义了policy的返回体
            if (data && data.code === 0) {
                // 更新头像地址
                $.post(
                    CONTEXT_PATH + "/user/header/url",
                    {filename: $("input[name='key']").val()},
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
                alert("上传失败!");
            }
        }
    });
    return false;
}

function check_new_pwd() {
    var $new_pwd = $("#new-password");
    var $new_pwd_msg = $("#new-password-msg");
    var pwd = $new_pwd.val();
    if (check_null(pwd)) {
        $new_pwd_msg.text("密码不能为空!")
        $new_pwd.addClass("is-invalid");
        return false;
    }
    if (pwd.length < 8) {
        $new_pwd_msg.text("密码长度不能小于8位!");
        $new_pwd.addClass("is-invalid");
        return false;
    }
    return true;
}


function check_old_pwd() {
    var $old_pwd = $("#old-password");
    var $old_pwd_msg = $("#old-password-msg");
    var pwd = $old_pwd.val();
    if (check_null(pwd)) {
        $old_pwd_msg.text("密码不能为空!")
        $old_pwd.addClass("is-invalid");
        return false;
    }
    return true;
}


function check_null(str) {
    var regu = "^[ ]+$";
    var re = new RegExp(regu);
    return re.test(str);
}


function check_data() {
    var $confirm_pwd = $("#confirm-password");
    var pwd1 = $("#new-password").val();
    var pwd2 = $confirm_pwd.val();
    if (pwd1 !== pwd2) {
        $("#confirm-password-msg").text("两次输入的密码不一致!")
        $confirm_pwd.addClass("is-invalid");
        return false;
    }
    return true;
}

function clear_error() {
    $(this).removeClass("is-invalid");
}
