
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
    getCertificates();

});

$(window).resize(adjust_body_offset);
adjust_body_offset();

function getCertificates(){
	$.ajax({
        url : '/certificate/getAll',
        type : 'get',
        success : function(data) {
			var str = "";
			$.each(data, function(i, item){
				str += '<tr>'+
						'<td></td>'+
						'<td><b>' + item.id + '</b></td>'+
						'<td>' + 'C = ' + item.country + ', ST = ' + item.state + ', L = ' + item.localityName + ', O = ' + item.organization + ', OU = ' + item.organizationalUnitName + ', CN = ' + item.commonName + ', Email = ' + item.email + '</td>';
				if (item.id === item.issuerId){
					str +=  '<td>Self signed</td>';
				}else{
					$.ajax({
				        url : '/certificate/get/'+item.issuerId,
				        type : 'get',
				        async: false,
				        success : function(data) {
							str += '<td>' + 'C = ' + data.country + ', ST = ' + data.state + ', L = ' + item.localityName + ', O = ' + data.organization + ', OU = ' + data.organizationalUnitName + ', CN = ' + data.commonName + ', Email = ' + data.email + '</td>';
						},
				        error : function(data) {
				            alert("get fail");
				        },
				    });
			    }
				str +=  '<td>' + item.startDate + '</td>'+
						'<td>' + item.endDate + '</td>'+
						'<td>' + item.ca + '</td>'+
						'<td>' + item.id + '</td>'+
						'<td><button type="button" class="btn btn-primary" id="revoke"  onclick="revoke(' + item.id + ')">Revoke</button></td>'+
						'<td><button type="button" class="btn btn-primary" id="get_public" onclick="getPublic(' + item.id + ')">Get key</button></td>'+
						'<td><button type="button" class="btn btn-primary" id="get_private" onclick="getPrivate(' + item.id + ')">Get key</button></td>'+
					'</tr>';
			});
			$("#certtable").append(str);
        },
        error : function(data) {
            alert("get all fail");
        },
    });
}

function addButtonListeners() {
}

function getPublic(id){
	$.ajax({
        url : '/certificate/getPublic/'+id,
        type : 'get',
        success : function(data) {
			alert(JSON.stringify(data));
		},
        error : function(data) {
            alert("get fail");
        },
    });
}

function getPrivate(id){
	$.ajax({
        url : '/certificate/getPrivate/'+id,
        type : 'get',
        success : function(data) {
			alert(JSON.stringify(data));
		},
        error : function(data) {
            alert("get fail");
        },
    });
}

function adjust_body_offset() {
	$('#page').css('padding-top', $('.navbar').outerHeight(true) + 'px');

}