<header id="header" class="wrapper">
  <div id="header-inner" class="inner">
    <h1 id="logo-wrap">
      <a href="/mrpc" id="logo">mrpc</a>
    </h1>
    <nav id="main-nav">
      <ul>
        {{ header_menu('main-nav') }}
        <li class="main-nav-item">
          <a href="https://github.com/{{ config.github }}" class="main-nav-link">GitHub</a>
        </li>
      </ul>
    </nav>
    <div id="lang-select-wrap">
      <label id="lang-select-label"><i class="fa fa-globe"></i><span>{{ lang_name(page.lang) }}</span></label>
      <select id="lang-select">
        {% for lang in site.data.languages %}
          <option data-page-lang="{{ page.lang }}" value="{{ loop.key }}"{% if page.lang === loop.key %} selected{% endif %}>{{ lang_name(loop.key) }}</option>
        {% endfor %}
      </select>
    </div>
    {% if !page.is_home %}
    <div id="search-btn-wrap" class="main-nav-item">
      <input type="text" class="st-default-search-input">
    </div>
    {% endif %}
    <a id="mobile-nav-toggle">
      <span class="mobile-nav-toggle-bar"></span>
      <span class="mobile-nav-toggle-bar"></span>
      <span class="mobile-nav-toggle-bar"></span>
    </a>
  </div>
</header>