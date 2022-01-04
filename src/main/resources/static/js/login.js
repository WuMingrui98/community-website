$(function(){
    $("form").submit(function() {
        var cond2 = check_username();
        var cond1 = check_pwd();
        var cond3 = check_code();
        return cond1 && cond2 && cond3;
    });
    $("#kaptcha").click(refresh_kaptcha);
    $("input").focus(clear_error);
});

function refresh_kaptcha() {
    var path = CONTEXT_PATH + "/kaptcha?p=" + Math.random();
    $("#kaptcha").attr("src", path);
}

function check_null(str) {
    var regu = "^[ ]+$";
    var re = new RegExp(regu);
    return re.test(str);
}

function check_pwd() {
    var pwd = $("#password").val();
    if (check_null(pwd)) {
        $("#password-msg").text("密码不能为空!")
        $("#password").addClass("is-invalid");
        return false;
    }
    return true;
}

function check_username() {
    var username = $("#username").val();
    if (check_null(username)) {
        $("#username-msg").text("用户名不能为空!")
        $("#username").addClass("is-invalid");
        return false;
    }
    return true;
}

function check_code() {
    var username = $("#verifycode").val();
    if (check_null(username)) {
        $("#code-msg").text("验证码不能为空!")
        $("#verifycode").addClass("is-invalid");
        return false;
    }
    return true;
}

function clear_error() {
    $(this).removeClass("is-invalid");
}
