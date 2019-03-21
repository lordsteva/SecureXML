
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
    getCertificates();
    
    $('#logoutlink').click(function() {
		localStorage.setItem('jwtToken', null);
		window.location.href = '/index.html';
	});
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
						'<td>' + item.ca + '</td>';
				$.ajax({
			        url : '/certificate/isvalid/'+item.id,
			        type : 'get',
			        async: false,
			        contentType : 'application/json',
			        success : function(data) {
						str += '<td>' + JSON.stringify(data) + '</td>';
						if(data === false){
							$.ajax({
					        url : '/certificate/revokedReason/'+item.id,
					        type : 'get',
					        async: false,
					        contentType : 'application/json',
					        success : function(data) {
								try {
									str += '<td>Revoked: ' + JSON.parse(data).reason + '</td>';
								}catch (e) {
									str += '<td>not valid</td>';
								}
							},
					        error : function(data) {
					            alert("get revoked reason fail");
					        },
					    });
						}else{
							str +='<td><button type="button" class="btn btn-primary" id="revoke" onclick="openModal(' + item.id + ')">Revoke</button></td>';
						}
					},
			        error : function(data) {
			            alert("get revoked fail");
			        },
			    });
				str +=  '<td><button type="button" class="btn btn-primary" id="get_public" onclick="getPublic(' + item.id + ')">Get key</button></td>'+
						'<td><input type="checkbox" name="keystore" class="keystore" value="' + item.id + '"></td>'+
					'</tr>';
			});
			$("#certtable").append(str);
        },
        error : function(data) {
            alert("get all fail");
        },
    });
}

function createKeystore(){
	var arr = [];
	$('input.keystore:checkbox:checked').each(function () {
	    arr.push($(this).val());
	});
	var d = {};
	d.id_arr = arr;
	d.name=$("input[id='keystorename']").val();
	d.password=$("input[id='keystorepass']").val();
	$.ajax({
        url : '/certificate/keystore',
        type : 'post',
        contentType : 'application/json',
        data : JSON.stringify(d),
        success : function(data) {
            let blob = new Blob([data], { type: 'application/jks' })
            let link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            let fileName=d.name
            if(!fileName.endsWith('.jks'))
            	fileName+='.jks'
            link.download = fileName;

            document.body.appendChild(link);

            link.click();

            document.body.removeChild(link);
        },
        error : function(data) {
            alert("nije uspeo");
        },
    });
}

function openModal(id) {
	$("#revokeModal").modal('show');
	$("#modalFooter").html("");
	$("#modalFooter").append('<button type="button" class="btn btn-danger" data-dismiss="modal">Close</button><button class="btn btn-primary" onclick="revoke(' + id + ')">Revoke</button>');
}

function addButtonListeners() {
	$('#revokeCertificate').submit(function(e) {
		revoke()
	});

	$('#formkeystoreid').submit(function(e) {
	e.preventDefault();
		createKeystore();
	});
}

function revoke(id){
	var d = {};
	d.reason = $("input[id='reason']").val();
	$.ajax({
        url : '/certificate/revoke/'+id,
        type : 'post',
        contentType : 'application/json',
        data : JSON.stringify(d),
        success : function(data) {
			window.location.reload(true);
		},
        error : function(data) {
            alert("get1 fail");
        },
    });
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