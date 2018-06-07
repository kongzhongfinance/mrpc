
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