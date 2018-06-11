var vm = new Vue({
    el: '#app',
    data: {
        server: {},
        isLoading: true
    },
    mounted: function () {
        this.load();
    },
    beforeCreate: function () {
        vueLoding = this.$loading.show();
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

            sendGET({
                url: '/admin/server/detail/' + id,
                success: function (data) {
                    $vm.server = data.payload;
                },
                error: function (error) {
                    console.log(error);
                    alert(result.msg || '数据加载失败');
                }
            });
        },
        deleteNode: function (id, host, port) {
            var $vm = this;
            alertConfirm('确定要删除该节点吗?', function () {
                sendPOST({
                    url: '/admin/server/delete',
                    data: {
                        id: id,
                        host: host,
                        port: port
                    },
                    success: function (result) {
                        if (result.success) {
                            swal(
                                '操作成功!',
                                '已经删除节点',
                                'success'
                            )
                            $vm.load();
                        } else {
                            alert(result.msg || '操作失败')
                        }
                    }
                })
            });
        },
        updateStatus: function (id, host, port, status) {
            var $vm = this;
            var title = (status === 'online') ? '上线' : '下线';
            alertConfirm('确定要' + title + '该节点吗？', function () {
                sendPOST({
                    url: '/api/' + status,
                    data: {id: id, host: host, port: port},
                    success: function (result) {
                        if (result.success) {
                            swal(
                                '操作成功!',
                                '该节点已' + title,
                                'success'
                            )
                            $vm.load();
                        } else {
                            alert(result.msg || '操作失败')
                        }
                    }
                })
            });
        }
    }
});

$(document).ready(function () {
    vm.isLoading = false;
    vueLoding.hide();
});