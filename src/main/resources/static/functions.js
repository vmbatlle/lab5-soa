$(document).ready(function () {
    registerSearch();
    registerTemplate();
});

function registerSearch() {
    $("#search").submit(function (ev) {
        event.preventDefault();
        $.get($(this).attr('action'), {q: $("#q").val(), max: $("#max").val()}, function (data) {
            $("#resultsBlock").html(Mustache.render(template, data));
        });
    });

    $("#advanced").submit(function (ev) {
        event.preventDefault();
        data = $(this).serializeArray()
            .filter(v => v.value !== "")
            .reduce(function(o, val){
                o[val.name] = val.value;
                return o;
            }, {});
        console.log(data);
        $.get($(this).attr('action'), data, function (data) {
            $("#resultsBlock").html(Mustache.render(template, data));
            $("#advanced").collapse('hide');
        });
    });
}

function registerTemplate() {
    template = $("#template").html();
    Mustache.parse(template);
}
