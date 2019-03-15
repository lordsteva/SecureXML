
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
	/*$.validator.methods.phoneCheck = function(value, element) {
		return this.optional(element)
		|| /([0-9]{3,3}\/[0-9]{3,3}-[0-9]{2,2}-[0-9]{2,2})$/
		.test(value);
	}
*/
/*	$('#rent').click(function () {
		location.hash = 'rentacar';
	});

	$("#air").click(function (event) {

		location.hash = "airlineSearch";
	});

	$('#hotel').click(function() {
		location.hash = 'hotel';
	});

	$(window).on('hashchange', function () {
//		if (location.hash === '#rentacar') {
//		showRentACar();
//		} else

		if (location.hash.includes("hotel")) {
			showHotel();}
//		} else if (location.hash.includes("cart")) {
//		showCart();
	});
*/
	addButtonListeners();



});

$(window).resize(adjust_body_offset);
adjust_body_offset();

function addButtonListeners() {	
	
	regLogValidation();

	$('#login').click(function() {

		if ($('#loginForm').valid()) {
			var d = {};
			d.username = $('#loginModal #username').val();
			d.password = $('#loginModal #password').val();
			$.ajax({
				url : 'user/login',
				type : 'post',
				contentType : 'application/json',
				data : JSON.stringify(d),
				success : function(data) {
					localStorage.setItem('jwtToken',data.accessToken);
					window.location.replace("/"+"?token="+getToken());
				},
				statusCode : {
					401 : function(data) {
						var string = data.responseJSON.message;
						var flag = string.localeCompare("Bad credentials");
						if (flag == 0)
							string = "Neispravno korisničko ime i/ili lozinka";
						else
							string = "Korisnički nalog nije aktiviran"
						$('#btn-error').show().html(string).fadeOut(5000);
					}
				}
			});
		}
	});
	$('#register').click(

			function() {
				if ($('#registerForm').valid()) {
					var d = {};
					d.username = $('#regusername').val();
					d.password = $('#regpassword').val();
					d.firstName = $('#regfirstName').val();
					d.lastName = $('#reglastName').val();
					d.phone = $('#regphone').val();
					d.email = $('#regemail').val();
					d.city = $('#regcity').val();

					$.ajax({
						url : '/user/register',
						type : 'post',
						contentType : 'application/json',
						data : JSON.stringify(d),

						success : function(data) {
							if (data == true) {
								$("#registerModal").modal('hide');

								$("#successModal").modal('show');

								// window.location.replace("");
							} else {
								$('#regbtn-error').show().html(
										'Registracija nije uspela').fadeOut(
										5000);

							}
						}
					});

				}
			});

}

function regLogValidation(){
	$('#loginForm').validate({
		rules : {
			username : "required",
			password : "required"
		},
		messages : {
			username : "Ime nije uneto",

			password : "Lozinka nije uneta"

		}
	});
	$('#registerForm')
			.validate(
					{
						rules : {
							username : {
								required : true,
								//remote : "user/checkusername/"
							},
							password : "required",
							email : {
								required : true,
								email : true,
							//	remote : "user/checkemail/"
							},
							firstName : {
								required : true
							},
							lastName : {
								required : true
							},
							password2 : {
								required : true,
								equalTo : "#regpassword"
							},
							phone : {
								required : true,
								phoneCheck : true
							},
							city : {
								required : true
							}

						},
						messages : {
							username : {
								required : "Ime nije uneto",
								remote : "Korisničko ime je zaueto"
							},

							password : "Lozinka nije uneta",

							password2 : {
								required : "Unesite ponovo lozinku",
								equalTo : "Lozinke se ne poklapaju"
							},
							firstName : {
								required : "Ime nije uneto",
								letters : "Dozvoljena su samo slova srpske latinice i razmaci"
							},
							lastName : {
								required : "Prezime nije uneto",
								letters : "Dozvoljena su samo slova srpske latinice i razmaci"
							},
							email : {
								required : "Email adresa nije uneta",
								email : "Neispravan format adrese",
								remote : "Već postoji nalog koji je registrovan na ovu email adresu"
							},
							phone : {
								required : "Telefon nije unet",
								phoneCheck : "Neispravan format (123/456-78-99)"
							},

							city : {
								required : "Grad nije unet"
							}
						}
					});
	
}

function adjust_body_offset() {
	$('#page').css('padding-top', $('.navbar').outerHeight(true) + 'px');

}