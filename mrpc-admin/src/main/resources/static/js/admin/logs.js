var vm = new Vue({
    el: '#app',
    data: {
        logPage: {},
        isLoading: true
    },
    beforeCreate: function () {
        vueLoding = this.$loading.show();
    },
    mounted: function () {
        this.load(1);
    },
    methods: {
        load: function (page) {
            var $vm = this;
            sendPOST({
                url: '/admin/log/list',
                data: {page: page},
                success: function (data) {
                    $vm.logPage = data.payload;
                },
                error: function (error) {
                    console.log(error);
                    alert(result.msg || '数据加载失败');
                }
            });
        }
    }
});

$(document).ready(function () {
    vm.isLoading = false;
    vueLoding.hide();
});