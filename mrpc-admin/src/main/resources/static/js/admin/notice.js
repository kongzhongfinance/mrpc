var vm = new Vue({
    el: '#app',
    data: {
        noticePage: {}
    },
    mounted: function () {
        this.load(1);
    },
    filters: {
        extract: function (value, maxSize) {
            if(value.length > maxSize){
                return value.substring(0, maxSize) + '...';
            }
            return value;
        }
    },
    methods: {
        load: function (page) {
            var $vm = this;
            sendPOST({
                url: '/admin/notice/list',
                data: {page: page},
                success: function (data) {
                    $vm.noticePage = data.payload;
                },
                error: function (error) {
                    console.log(error);
                    alert(result.msg || '数据加载失败');
                }
            });
        }
    }
})