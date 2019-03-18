
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
	datefrom.min = new Date().toISOString().split("T")[0];
	dateto.min = new Date().toISOString().split("T")[0];

	setInterval(refreshToken, 60000); //svaki min
    addButtonListeners();
});

$(window).resize(adjust_body_offset);
adjust_body_offset();

function addButtonListeners() {

	$('#formcr').submit(function(e) {
		e.preventDefault();
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
        d.datefrom = $("input[id='datefrom']").val();
        d.dateto = $("input[id='dateto']").val();
        if($('#selfsigned').is(':checked')){
			d.issuer=null;
        }else{
			d.issuer=$('select[name="issuerselect"]').val();;
        }
        $.ajax({
            url : '/create',
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

    $('#selfsigned').click(function(){
	    if($(this).is(':checked')){
	        $('#choose_issuer').hide();
	    } else {
	        $('#choose_issuer').show();
	    }
	});
}

function adjust_body_offset() {
	$('#page').css('padding-top', $('.navbar').outerHeight(true) + 'px');

}