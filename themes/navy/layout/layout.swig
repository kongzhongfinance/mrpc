<!DOCTYPE html>
<html lang="{{ page.lang }}">
{{ partial('partial/head') }}
<body class="{% if page.is_home %}home{% endif %}">
  <div id="container">
    {% if page.is_home %}<div class="with-background">{% endif %}
    {{ partial('partial/header') }}
    {{ body }}
  </div>
  {{ partial('partial/footer') }}
  <div id="mobile-nav-dimmer"></div>
  {{ partial('partial/mobile_nav') }}
  {{ partial('partial/after_footer') }}
</body>
</html>