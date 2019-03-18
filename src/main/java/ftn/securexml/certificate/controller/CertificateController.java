package ftn.securexml.certificate.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ftn.securexml.certificate.dto.CertificateDTO;
import ftn.securexml.certificate.service.GenerateCertificateService;


@RestController
@RequestMapping(value = "/certificate")
public class CertificateController {

	@Autowired
	private GenerateCertificateService certificateService;
	
	
	@PostMapping("/create")
	public ResponseEntity<?> addOffice(HttpServletRequest request, @RequestBody CertificateDTO certificate)
	{
		return (certificateService.createCertificate(certificate))? ResponseEntity.status(200).build() : ResponseEntity.badRequest().build();
	}
	
	@GetMapping("/getAll")
	public ResponseEntity<?> getAll(HttpServletRequest request)
	{
		return ResponseEntity.ok(certificateService.getAll());
	}
	
	
	
}
