
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
    
	$('#create_cer').click(function() {
        $.ajax({
            url : '/create',
            type : 'get',
            success : function(data) {
                window.location.href = data;
            },
            error : function(data) {
                alert("nije uspeo");
            },
        });
    });
    
    $('#show_all').click(function() {
		alert("tbd");
	});

}

function adjust_body_offset() {
	$('#page').css('padding-top', $('.navbar').outerHeight(true) + 'px');

}