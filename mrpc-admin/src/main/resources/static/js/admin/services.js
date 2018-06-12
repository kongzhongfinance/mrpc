var vm = new Vue({
    el: '#app',
    data: {
        servers: [],
        isLoading: true
    },
    mounted: function () {
        this.load();
    },
    beforeCreate: function () {
        vueLoding = this.$loading.show();
    },
    methods: {
        load: function () {
            var $vm = this;
            sendGET({
                url: '/admin/server/list',
                success: function (data) {
                    $vm.servers = data.payload;
                },
                error: function (error) {
                    console.log(error);
                    alert(result.msg || '数据加载失败');
                }
            });
        },
        rename: function (id, appAlias) {
            var $vm = this;
            swal({
                title: '请输入新的别名',
                input: 'text',
                inputValue: appAlias || '',
                inputAttributes: {
                    autocapitalize: 'off'
                },
                showCancelButton: true,
                confirmButtonText: '修改别名',
                showLoaderOnConfirm: true
            }).then(function (result) {
                if (result.value) {
                    sendPOST({
                        url: '/admin/server/update',
                        data: {id: id, appAlias: result.value},
                        success: function (result) {
                            if (result.success) {
                                swal('操作成功!', '别名修改成功', 'success')
                                $vm.load();
                            } else {
                                alert(result.msg || '操作失败')
                            }
                        }
                    })
                }
            })
        }
    }
});

$(document).ready(function () {

    $.getJSON('/admin/server/mind', function (result) {
        var mind = {
            "meta": {
                "name": "mrpc",
                "version": "0.0.1",
            },
            "format": "node_array",
            "data": result
        };
        var options = {
            container: 'jsmind_container',
            theme: 'asphalt'
        }
        var jm = jsMind.show(options, mind);
        var jmHeight = $("#jsmind_container canvas:eq(0)").attr('height');
        $("#jsmind_container").css('height', jmHeight);
    });

    vm.isLoading = false;
    vueLoding.hide();

});