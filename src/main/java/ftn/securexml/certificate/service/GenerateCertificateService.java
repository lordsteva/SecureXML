package ftn.securexml.certificate.service;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ft.securexml.certificate.keystores.KeyStoreReader;
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
		
	public boolean createCertificate(CertificateDTO certificate)
	{
		//pravim ovde par kljuceva jer ako je selfSigned jedino ovako mogu dobiti privatni kljuc za issuera
		KeyGenerator kg=new KeyGenerator();
		KeyPair keyPairSubject = kg.generateKeys();
		
		//Serijski broj sertifikata
		String sn=String.valueOf(certificateRepository.findAll().size()+1);
				
		SubjectData subjectData = generateSubjectData(certificate, keyPairSubject, sn);
		IssuerData issuerData=null;
		
		//ako je selfSigned
		if(certificate.getIssuerId()==null) {
			issuerData = generateSelfSignedIssuerData(certificate, keyPairSubject.getPrivate());
		}
		//ako ga neko potpisuje, treba uzeti issuera iz keyStora na osnovu id
		else{
			KeyStoreReader ksr=new KeyStoreReader();
			issuerData=ksr.readIssuerFromStore("appkeystore.jks", certificate.getIssuerId(), "mikimaus".toCharArray(), "mikimaus".toCharArray());
		}
			    
		//Generise se sertifikat za subjekta, potpisan od strane issuer-a
		CertificateGenerator cg = new CertificateGenerator();
		X509Certificate cert = cg.generateCertificate(subjectData, issuerData);
		
		//Snimanje sertifikata u keystore
		KeyStoreWriter ksw=new KeyStoreWriter();
		ksw.loadKeyStore("appkeystore.jks", "mikimaus".toCharArray());
		//ksw.loadKeyStore(null, null);
		ksw.write(cert.getSerialNumber().toString(), keyPairSubject.getPrivate(), "mikimaus".toCharArray(), cert);
		ksw.saveKeyStore("appkeystore.jks", "mikimaus".toCharArray());
		
		//ubacivanje u bazu napravljenog sertifikata
		ftn.securexml.model.Certificate c=new ftn.securexml.model.Certificate(sn, certificate.isCa());
		certificateRepository.save(c);

		return true;
	}
	
	public List<CertificateDTO>getAllCa(){
		List<CertificateDTO>retVal=new ArrayList<CertificateDTO>();
		List<ftn.securexml.model.Certificate>allAlias=certificateRepository.findByIsCa(true);
		for(int i=0;i<allAlias.size();i++) {
			KeyStoreReader ksr=new KeyStoreReader();
			X509Certificate cer=(X509Certificate)ksr.readCertificate("appkeystore.jks", "mikimaus", allAlias.get(i).getCertificateId());
			retVal.add(makeCertDTOFromCert(cer));
		}
		return retVal;
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
	
	private SubjectData generateSubjectData(CertificateDTO certificate, KeyPair keyPairSubject, String sn) {
		try {			
			//Datumi od kad do kad vazi sertifikat
			SimpleDateFormat iso8601Formater = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = iso8601Formater.parse(certificate.getStartDate());
			Date endDate = iso8601Formater.parse(certificate.getEndDate());
			
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
	
	
	public CertificateDTO makeCertDTOFromCert(X509Certificate cert) {
		CertificateDTO cDTO=new CertificateDTO();
        try {
            X500Name subjName = new JcaX509CertificateHolder(cert).getSubject();

            RDN cn = subjName.getRDNs(BCStyle.CN)[0];
            String cname = IETFUtils.valueToString(cn.getFirst().getValue());
            cDTO.setCommonName(cname);

            RDN on = subjName.getRDNs(BCStyle.O)[0];
            String oname = IETFUtils.valueToString(on.getFirst().getValue());
            cDTO.setOrganization(oname);

            RDN oun = subjName.getRDNs(BCStyle.OU)[0];
            String ouname = IETFUtils.valueToString(oun.getFirst().getValue());
            cDTO.setOrganizationalUnitName(ouname);

            RDN con = subjName.getRDNs(BCStyle.C)[0];
            String conname = IETFUtils.valueToString(con.getFirst().getValue());
            cDTO.setCountry(conname);

            RDN loc = subjName.getRDNs(BCStyle.L)[0];
            String locname = IETFUtils.valueToString(loc.getFirst().getValue());
            cDTO.setLocalityName(locname);
            
            RDN sta = subjName.getRDNs(BCStyle.ST)[0];
            String staname = IETFUtils.valueToString(sta.getFirst().getValue());
            cDTO.setCountry(staname);
            
            RDN en = subjName.getRDNs(BCStyle.E)[0];
            String emname = IETFUtils.valueToString(en.getFirst().getValue());
            cDTO.setEmail(emname);

            cDTO.setId(String.valueOf(((X509Certificate) cert).getSerialNumber()));
            cDTO.setStartDate(((X509Certificate) cert).getNotBefore().toString());
            cDTO.setEndDate(((X509Certificate) cert).getNotAfter().toString());
            return cDTO;
           
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        	return null;
        }

    }
	
	
}
