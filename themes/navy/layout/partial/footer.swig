<footer id="footer" class="wrapper">
  <div class="inner">
    <div id="footer-copyright">
      &copy; {{ moment(Date.now()).year() }} <a href="https://github.com/biezhi" target="_blank">{{ config.author }}</a><br>
      Documentation licensed under <a href="http://creativecommons.org/licenses/by/4.0/" target="_blank">CC BY 4.0</a>.

      {#<div class="links">#}
        {#<a href="">空中网</a>&nbsp;&nbsp;#}
        {#<a href="">空中金融</a>&nbsp;&nbsp;#}
        {#<a href="">王爵的技术博客</a>#}
      {#</div>#}

    </div>

    <div id="footer-links">
      <a href="http://weibo.com/{{ config.weibo }}" class="footer-link" target="_blank"><i class="fa fa-weibo"></i></a>
      {#<a href="https://twitter.com/{{ config.twitter }}" class="footer-link" target="_blank"><i class="fa fa-twitter"></i></a>#}
      <a href="https://github.com/{{ config.github }}" class="footer-link" target="_blank"><i class="fa fa-github-alt"></i></a>
    </div>
  </div>
</footer>