(function(){
    [].forEach.call(document.querySelectorAll('.nav-tabs a'), function(el) {
        el.addEventListener('click', function(e){
            e.preventDefault();
            [].forEach.call(document.querySelectorAll('.nav-tabs li.active'), function(el) {
                el.classList.remove('active');
            });
            this.parentNode.classList.add('active');

            [].forEach.call(document.querySelectorAll('.tab-pane.active'), function(el) {
                el.classList.remove('active');
            });
            document.querySelector(this.getAttribute('target')).classList.add('active');
        })
    });
})();