var vm = new Vue({
    el: '#app',
    data: {
        server: {}
    },
    mounted: function () {
        this.load();
    },
    filters: {
        toStatus: function (value) {
            if (!value) return ''
            value = value.toString()
            if (value === 'ONLINE') {
                return '在线'
            }
            if (value === 'OFFLINE') {
                return '离线'
            }
            return value
        }
    },
    methods: {
        load: function () {
            var $vm = this;
            var pos = window.location.toString().lastIndexOf("/");
            var id = window.location.toString().substring(pos + 1)
            axios.get('/admin/server/detail/' + id).then(function (response) {
                $vm.server = response.data.payload;
            }).catch(function (error) {
                console.log(error);
                alert(result.msg || '数据加载失败');
            });
        }
    }
})