$(function(){
	$("form").submit(function() {
		var cond1 = check_pwd();
		var cond2 = check_username();
		var cond3 = check_data();
		return cond1 && cond2 && cond3;
	});

	$("input").focus(clear_error);
});

function check_null(str) {
	var regu = "^[ ]+$";
	var re = new RegExp(regu);
	return re.test(str);
}

function check_data() {
	var pwd1 = $("#password").val();
	var pwd2 = $("#confirm-password").val();
	if(pwd1 !== pwd2) {
		$("#confirm-password-msg").text("两次输入的密码不一致!")
		$("#confirm-password").addClass("is-invalid");
		return false;
	}
	return true;
}

function check_pwd() {
	var pwd = $("#password").val();
	if (check_null(pwd)) {
		$("#password-msg").text("密码不能为空!")
		$("#password").addClass("is-invalid");
		return false;
	}
	if(pwd.length < 8) {
		$("#password-msg").text("密码长度不能小于8位!");
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

function clear_error() {
	$(this).removeClass("is-invalid");
}
