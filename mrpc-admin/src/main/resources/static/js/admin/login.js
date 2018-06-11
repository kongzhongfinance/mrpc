var password = document.head.querySelector("[name=_pass_word]").content;

function encrypt(value) {
    return sjcl.encrypt(password, value);
}

function decrypt(encrypted) {
    return sjcl.decrypt(password, encrypted)
}

var vm = new Vue({
    el: '#app',
    data: {
        username: '',
        password: '',
        error: false,
        errorMsg: ''
    },
    mounted: function () {
        var $vm = this;
        var u = localStorage['_MRPC_USERNAME'];
        var p = localStorage['_MRPC_PASSWORD'];
        if (u) {
            $vm.username = decrypt(u);
        }
        if (p) {
            $vm.password = decrypt(p);
        }
    },
    methods: {
        login: function (event) {
            var $vm = this;
            if ($vm.username === '') {
                $vm.error = true;
                $vm.errorMsg = '用户名不能为空';
                return false;
            }
            if ($vm.password === '') {
                $vm.error = true;
                $vm.errorMsg = '密码不能为空';
                return false;
            }
            var data = "username=" + $vm.username + "&password=" + $vm.password;
            $.ajax({
                type: "POST",
                url: "/auth/login",
                data: data,
                dataType: "json",
                success: function (result) {
                    if (result.success) {
                        var u = encrypt($vm.username)
                        var p = encrypt($vm.password)
                        localStorage['_MRPC_USERNAME'] = u;
                        localStorage['_MRPC_PASSWORD'] = p;
                        window.location.href = '/admin/index.html';
                    } else {
                        $vm.error = true;
                        $vm.errorMsg = result.msg;
                    }
                }
            });
        }
    }
});