
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
    getCertificates();

});

$(window).resize(adjust_body_offset);
adjust_body_offset();

function getCertificates(){
	$.ajax({
        url : '/certificate/getAll',
        type : 'get',
        success : function(data) {
            alert("get all ok");
        },
        error : function(data) {
            alert("get all fail");
        },
    });
}

function addButtonListeners() {

	$('#formcr').submit(function(e) {
		e.preventDefault();
        var d = {};
        d.country = $("input[id='country']").val();
        d.state = $("input[id='state']").val();
        d.localityName = $("input[id='locality']").val();
        d.organization = $("input[id='organization']").val();
        d.organizationalUnitName = $("input[id='orgunit']").val();
        d.commonName = $("input[id='common']").val();
        d.email = $("input[id='email']").val();
        d.startDate = $("input[id='datefrom']").val();
        d.endDate = $("input[id='dateto']").val();
        if($('#selfsigned').is(':checked')){
			d.issuerId=null;
        }else{
			d.issuerId=$('select[name="issuerselect"]').val();
        }
        if($('#ca').is(':checked')){
			d.ca=true;
        }else{
			d.ca=false;
        }
        $.ajax({
            url : '/certificate/create',
            type : 'post',
            contentType : 'application/json',
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