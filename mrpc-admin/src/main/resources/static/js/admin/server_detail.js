var vm = new Vue({
    el:'#app',
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
            if(value === 'ONLINE'){
                return '在线';
            }
            if(value === 'OFFLINE'){
                return '离线';
            }
            return value;
        }
    },
    methods : {
        load: function() {
            var $vm = this;
            axios.get('/admin/server/detail/4').then(function (response) {
                $vm.server = response.data.payload;
            }).catch(function (error) {
                console.log(error);
                alert(result.msg || '数据加载失败');
            });
        }
    }
})