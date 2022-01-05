$(function(){
    bsCustomFileInput.init();
    $("input").focus(clear_error);
    $("#pwd-form").submit(function() {
        var cond1 = check_new_pwd();
        var cond2 = check_old_pwd();
        var cond3 = check_data();
        return cond1 && cond2 && cond3;
    });
});


function check_new_pwd() {
    var $new_pwd = $("#new-password");
    var $new_pwd_msg = $("#new-password-msg");
    var pwd = $new_pwd.val();
    if (check_null(pwd)) {
        $new_pwd_msg.text("密码不能为空!")
        $new_pwd.addClass("is-invalid");
        return false;
    }
    if(pwd.length < 8) {
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
    if(pwd1 !== pwd2) {
        $("#confirm-password-msg").text("两次输入的密码不一致!")
        $confirm_pwd.addClass("is-invalid");
        return false;
    }
    return true;
}

function clear_error() {
    $(this).removeClass("is-invalid");
}
