
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
	$.ajax({
        url : '/checkIsAdmin',
        type : 'get',
        success : function(data) {
        	let ret=1;
       		$.each(data, function(i, item){
	       		if(item=="ROLE_SYSTEM_ADMIN"){
	       			ret=0;
				}
			});
			if(ret==1){
				window.location.replace("index.html");
			}
        },
        error : function(data) {
            alert("get all fail");
        }
    });
	setInterval(refreshToken, 60000); //svaki min
    addButtonListeners();
    
    $('#logoutlink').click(function() {
		localStorage.setItem('jwtToken', null);
		window.location.href = '/index.html';
	});
    
});

$(window).resize(adjust_body_offset);
adjust_body_offset();

function addButtonListeners() {
    
	$('#create_cer').click(function() {
        $.ajax({
            url : '/certificate/create',
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
		window.location.href = "showAllCertificates.html"
	});

}

function adjust_body_offset() {
	$('#page').css('padding-top', $('.navbar').outerHeight(true) + 'px');

}