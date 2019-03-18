package ftn.securexml.certificate.service;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ft.securexml.certificate.keystores.KeyStoreWriter;
import ftn.securexml.certificate.data.IssuerData;
import ftn.securexml.certificate.data.SubjectData;
import ftn.securexml.certificate.dto.CertificateDTO;
import ftn.securexml.certificate.generators.CertificateGenerator;
import ftn.securexml.certificate.generators.KeyGenerator;

@Service
public class GenerateCertificateService {

	@Autowired
	private ftn.securexml.repository.CertificateRepository certificateRepository;
	
	private Long userId;
	
	public boolean createCertificate(CertificateDTO certificate)
	{
		//pravim ovde par kljuceva jer ako je selfSigned jedino ovako mogu dobiti privatni kljuc za issuera
		KeyGenerator kg=new KeyGenerator();
		KeyPair keyPairSubject = kg.generateKeys();
		
		SubjectData subjectData = generateSubjectData(certificate, keyPairSubject);
		IssuerData issuerData=null;
		
		//ako je selfSigned
		if(certificate.getIssuerId()==null) {
			issuerData = generateSelfSignedIssuerData(certificate, keyPairSubject.getPrivate());
		}
		//ako ga neko porpisuje, treba uzeti isuera iz keyStora na osnovu id
		else{

		}
			    
		//Generise se sertifikat za subjekta, potpisan od strane issuer-a
		CertificateGenerator cg = new CertificateGenerator();
		X509Certificate cert = cg.generateCertificate(subjectData, issuerData);
		
		//Snimanje sertifikata u keystore
		KeyStoreWriter ksw=new KeyStoreWriter();
		ksw.loadKeyStore("appkeystore.jks", "mikimaus".toCharArray());
		ksw.write(cert.getSerialNumber().toString(), keyPairSubject.getPrivate(), "mikimaus".toCharArray(), cert);
		ksw.saveKeyStore("appkeystore.jks", "mikimaus".toCharArray());
		
		return true;
	}
	
	public List<X509Certificate>getAll(){
		List<X509Certificate>retVal=new ArrayList<X509Certificate>();
		
		return null;
	}
	
	private IssuerData generateSelfSignedIssuerData(CertificateDTO certificate, PrivateKey issuerKey) {
		X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);

		//znaci da je self signed, isuer je isto kao i subject
		if(certificate.getIssuerId()==null) {
			builder.addRDN(BCStyle.CN, certificate.getCommonName());
		    builder.addRDN(BCStyle.O, certificate.getOrganization());
		    builder.addRDN(BCStyle.OU, certificate.getOrganizationalUnitName());
		    builder.addRDN(BCStyle.C, certificate.getCountry());
		    builder.addRDN(BCStyle.E, certificate.getEmail());	
		    builder.addRDN(BCStyle.ST, certificate.getState());	
		    builder.addRDN(BCStyle.L, certificate.getLocalityName());		    

		    //UID (USER ID) je ID korisnika
		    builder.addRDN(BCStyle.UID, "654321");
		}
		//Kreiraju se podaci za issuer-a, sto u ovom slucaju ukljucuje:
	    // - privatni kljuc koji ce se koristiti da potpise sertifikat koji se izdaje
	    // - podatke o vlasniku sertifikata koji izdaje nov sertifikat
		return new IssuerData(issuerKey, builder.build());
	}
	
	private SubjectData generateSubjectData(CertificateDTO certificate, KeyPair keyPairSubject) {
		try {			
			//Datumi od kad do kad vazi sertifikat
			SimpleDateFormat iso8601Formater = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = iso8601Formater.parse(certificate.getStartDate());
			Date endDate = iso8601Formater.parse(certificate.getEndDate());
			
			//Serijski broj sertifikata
			String sn=String.valueOf(certificateRepository.findAll().size());
			
			//klasa X500NameBuilder pravi X500Name objekat koji predstavlja podatke o vlasniku
			X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
		    builder.addRDN(BCStyle.CN, certificate.getCommonName());
		    builder.addRDN(BCStyle.O, certificate.getOrganization());
		    builder.addRDN(BCStyle.OU, certificate.getOrganizationalUnitName());
		    builder.addRDN(BCStyle.C, certificate.getCountry());
		    builder.addRDN(BCStyle.E, certificate.getEmail());
		    builder.addRDN(BCStyle.ST, certificate.getState());	
		    builder.addRDN(BCStyle.L, certificate.getLocalityName());	
		    
		    //UID (USER ID) je ID korisnika
		    builder.addRDN(BCStyle.UID, "654321");
		    
		    //Kreiraju se podaci za sertifikat, sto ukljucuje:
		    // - javni kljuc koji se vezuje za sertifikat
		    // - podatke o vlasniku
		    // - serijski broj sertifikata
		    // - od kada do kada vazi sertifikat
		    return new SubjectData(keyPairSubject.getPublic(), builder.build(), sn, startDate, endDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
