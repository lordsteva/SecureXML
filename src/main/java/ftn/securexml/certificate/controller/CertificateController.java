package ftn.securexml.certificate.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ftn.securexml.certificate.dto.CertificateDTO;
import ftn.securexml.certificate.dto.KeystoreDTO;
import ftn.securexml.certificate.service.GenerateCertificateService;
import ftn.securexml.security.TokenUtils;


@RestController
@RequestMapping(value = "/certificate")
public class CertificateController {

	@Autowired
	private GenerateCertificateService certificateService;

	@Autowired
	private TokenUtils tokenUtils;
	
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
	@GetMapping("/create")
	public ResponseEntity<?> addOffice(HttpServletRequest request)
	{
	  return ResponseEntity.ok("createcertificate.html");
	}
	
	@PostMapping("/create")
	public ResponseEntity<?> addOffice(HttpServletRequest request, @RequestBody CertificateDTO certificate)
	{
		String token=tokenUtils.getToken(request);
		return (certificateService.createCertificate(certificate, token))? ResponseEntity.status(200).build() : ResponseEntity.badRequest().build();
	}
	
	@GetMapping("/getAllCa")
	public ResponseEntity<?> getAllCa(HttpServletRequest request)
	{
		return ResponseEntity.ok(certificateService.getAllCa());
	}
	
	@GetMapping("/getAll")
	public ResponseEntity<?> getAll(HttpServletRequest request)
	{
		return ResponseEntity.ok(certificateService.getAll());
	}

	@GetMapping("/get/{id}")
	public ResponseEntity<?> get(HttpServletRequest request, @PathVariable int id)
	{
		return ResponseEntity.ok(certificateService.getById(id));
	}

	@GetMapping("/getPublic/{id}")
	public ResponseEntity<?> getPublic(HttpServletRequest request, @PathVariable int id)
	{
		return ResponseEntity.ok(certificateService.getPublicKeyById(id).toString());
	}

	@GetMapping("/getPrivate/{id}")
	public ResponseEntity<?> getPrivate(HttpServletRequest request, @PathVariable int id)
	{
		return ResponseEntity.ok(certificateService.getPrivateKeyById(id).toString());
	}

	//povlacenje, samo admin moze
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
	@PostMapping("/revoke/{id}")
	public ResponseEntity<?> revoke(HttpServletRequest request, @PathVariable Long id,@RequestBody String reason)
	{
		return ResponseEntity.ok(certificateService.revoke(id,reason));
	}
	
	//Provera da li je povucen, svako moze da pristupi?
	@GetMapping("/isrevoked/{id}")
	public ResponseEntity<?> isRevoked(@PathVariable Long id){
		Boolean ret=certificateService.isRevoked(id);
		if(ret!=null)
			return ResponseEntity.ok(ret); 
		return ResponseEntity.badRequest().body("Invalid certificate id");
	}

	@GetMapping("/isvalid/{id}")
	public ResponseEntity<?> isValid(@PathVariable Long id){
		Boolean ret = certificateService.isValid(id);;
		if(ret!=null)
			return ResponseEntity.ok(ret);
		return ResponseEntity.badRequest().body("Invalid certificate id");
	}

	@GetMapping("/revokedReason/{id}")
	public ResponseEntity<?> revokedReason(@PathVariable Long id){
		return ResponseEntity.ok().body(certificateService.revokedReason(id));
	}

	@PostMapping("/keystore")
	public ResponseEntity<?> createKeystore(HttpServletRequest request,@RequestBody KeystoreDTO keystoreDTO)
	{
		certificateService.createKeyStore(keystoreDTO);
		return ResponseEntity.ok("ok");
	}

}
