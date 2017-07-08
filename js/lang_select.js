(function(){
  'use strict';

  // 兼容处理
  if (window.location.href.indexOf('docs/zh-cn') > 0 || window.location.href.indexOf('docs/en') > 0) {
    window.location.href = window.location.href.replace('docs/zh-cn', 'zh-cn/docs').replace('docs/en', 'en/docs');
  }

  function changeLang(){
    var lang = this.value;

    if (lang == 'zh-cn' && window.location.pathname == '/') {
      return window.location.pathname = '/';
    }

    window.location.pathname = window.location.pathname.replace(/(zh-cn|en)/g, lang).replace('//', '/');
  }

  document.getElementById('lang-select').addEventListener('change', changeLang);
  document.getElementById('mobile-lang-select').addEventListener('change', changeLang);
})();