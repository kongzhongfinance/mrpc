<footer id="footer">
  <div class="wrapper">
    <div class="inner">
      <div class="friend-links">
        {% for link in site.data.links %}
        <a href="{{ link.url }}" target="_blank">{{ link.label }}</a>
        {% endfor %}
      </div>
      <div id="footer-copyright">
        &copy; {{ moment(Date.now()).year() }} <a href="https://github.com/biezhi" target="_blank">{{ config.author }}</a><br>
        Documentation licensed under <a href="http://creativecommons.org/licenses/by/4.0/" target="_blank">CC BY 4.0</a>.
      </div>
      <div id="footer-links">
        <a href="http://weibo.com/{{ config.weibo }}" class="footer-link" target="_blank"><i class="fa fa-weibo"></i></a>
        <a href="https://github.com/{{ config.github }}" class="footer-link" target="_blank"><i class="fa fa-github-alt"></i></a>
        <a href="https://twitter.com/{{ config.twitter }}" class="footer-link" target="_blank"><i class="fa fa-twitter"></i></a>
      </div>
    </div>
  </div>
</footer>