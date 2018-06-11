
$.fn.bootstrapTableEx = function(opt){
    var defaults = {
        url: '',
        dataField: "rows",
        method: 'get',
        dataType: 'json',
        selectItemName: 'id',
        clickToSelect: true,
        pagination: true,
        smartDisplay: false,
        pageSize: 10,
        pageList: [10, 20, 30, 40, 50],
        paginationLoop: false,
        sidePagination: 'server',
        queryParamsType : null,
        columns: []
    }
    var option = $.extend({}, defaults, opt);
    if(!option.pagination){
        option.responseHandler = function(res) {
            return res.rows;
        }
    }
    $(this).bootstrapTable(option);
}

function alertConfirm(msg, callback) {
    swal({
        title: '警告消息',
        html: msg,
        type: 'question',
        showCloseButton: true,
        showCancelButton: true,
        focusConfirm: false,
        confirmButtonText: '确定',
        cancelButtonText: '取消'
    }).then(function (result) {
        if(result.value){
            callback()
        }
    })
}

function sendGET(options) {
    axios.get(options.url).then(function (response) {
        options.success(response.data)
    }).catch(function (error) {
        options.error(error)
    });
}
function sendPOST(options) {
    axios.post(options.url, options.data || {}).then(function (response) {
        options.success(response.data)
    }).catch(function (error) {
        options.error(error)
    });
}

Vue.filter('truncate', function (value, size, append) {
    if (value && value.length >= size) {
        return value.substring(0, size) + (append || '...');
    }
    return value
});