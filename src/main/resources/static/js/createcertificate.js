
function getToken() {
	return localStorage.getItem('jwtToken');
}
//salje token sa svakim zahtevom
$(document).ajaxSend(function(event, jqxhr, settings) {
	var token = getToken();
	if(settings.url.includes('https'))
		return;
	if (token != null)
		jqxhr.setRequestHeader('Authorization', 'Bearer ' + token);
});

//pokusava da produzi vreme trajanja tokena
function refreshToken(){
	if(getToken())
	$.ajax({
		   url: '/user/refresh',
           type: 'post',
           success: function (data) {
                localStorage.setItem('jwtToken',data.accessToken);
            }
	});
}

$(document).ready(function () {

	setInterval(refreshToken, 60000); //svaki min
    addButtonListeners();
});

$(window).resize(adjust_body_offset);
adjust_body_offset();

function addButtonListeners() {

	$('#submitcreate').click(function() {
        var d = {};
        d.country = $("input[id='country']").val();
        d.state = $("input[id='state']").val();
        d.locality = $("input[id='locality']").val();
        d.organization = $("input[id='organization']").val();
        d.orgunit = $("input[id='orgunit']").val();
        d.common = $("input[id='common']").val();
        d.email = $("input[id='email']").val();
        d.password = $("input[id='password']").val();
        d.company = $("input[id='company']").val();
        d.keyfield = $("#keyfield").val();
        $.ajax({
            url : '/createcertificate',
            type : 'post',
            data : JSON.stringify(d),
            success : function(data) {
                alert("uspeo");
            },
            error : function(data) {
                alert("nije uspeo");
            },
        });
    });
}

function adjust_body_offset() {
	$('#page').css('padding-top', $('.navbar').outerHeight(true) + 'px');

}