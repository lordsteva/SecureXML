package ftn.securexml.certificate.service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import ft.securexml.certificate.keystores.KeyStoreReader;
import ft.securexml.certificate.keystores.KeyStoreWriter;
import ftn.securexml.certificate.data.IssuerData;
import ftn.securexml.certificate.data.SubjectData;
import ftn.securexml.certificate.dto.CertificateDTO;
import ftn.securexml.certificate.dto.KeystoreDTO;
import ftn.securexml.certificate.generators.CertificateGenerator;
import ftn.securexml.certificate.generators.KeyGenerator;
import ftn.securexml.model.Certificate;
import ftn.securexml.model.User;
import ftn.securexml.repository.UserRepository;
import ftn.securexml.security.TokenUtils;

@Service
public class GenerateCertificateService {

	@Autowired
	private ftn.securexml.repository.CertificateRepository certificateRepository;

	@Autowired
	private TokenUtils tokenUtils;

	@Autowired
	private UserRepository userRepository;

	public boolean createCertificate(CertificateDTO certificate, String token) {

		// validacija
		if (certificate.getCountry() == null || certificate.getState() == null || certificate.getLocalityName() == null
				|| certificate.getOrganization() == null || certificate.getOrganizationalUnitName() == null
				|| certificate.getCommonName() == null || certificate.getEmail() == null
				|| certificate.getStartDate() == null || certificate.getEndDate() == null)
			return false;

		// validacija datuma
		SimpleDateFormat iso8601Formater = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date startDate = iso8601Formater.parse(certificate.getStartDate());
			Date endDate = iso8601Formater.parse(certificate.getEndDate());
			if (startDate.after(endDate))
				return false;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// pravim ovde par kljuceva jer ako je selfSigned jedino ovako mogu dobiti
		// privatni kljuc za issuera
		KeyGenerator kg = new KeyGenerator();
		KeyPair keyPairSubject = kg.generateKeys();

		// ubacivanje u bazu napravljenog sertifikata
		ftn.securexml.model.Certificate c = new ftn.securexml.model.Certificate();
		certificateRepository.save(c);

		// Serijski broj sertifikata
		String sn = c.getCertificateId().toString();

		// Id se generisao prilikom upisa u bazu, setujemo da mozemo da ga upisemo u
		// KeyStore
		certificate.setId(c.getCertificateId().toString());

		SubjectData subjectData = generateSubjectData(certificate, keyPairSubject, sn, token);
		IssuerData issuerData = null;

		// ako je selfSigned
		if (certificate.getIssuerId() == null) {
			issuerData = generateSelfSignedIssuerData(certificate, keyPairSubject.getPrivate(), token);
		}
		// ako ga neko potpisuje, treba uzeti issuera iz keyStora na osnovu id
		else {
			KeyStoreReader ksr = new KeyStoreReader();
			issuerData = ksr.readIssuerFromStore("appkeystore.jks", certificate.getIssuerId(), "mikimaus".toCharArray(),
					"mikimaus".toCharArray());
		}

		// Generise se sertifikat za subjekta, potpisan od strane issuer-a
		CertificateGenerator cg = new CertificateGenerator();
		X509Certificate cert = cg.generateCertificate(subjectData, issuerData, certificate.isCa());
		// Snimanje sertifikata u keystore
		KeyStoreWriter ksw = new KeyStoreWriter();
		ksw.loadKeyStore("appkeystore.jks", "mikimaus".toCharArray());
		// ksw.loadKeyStore(null, null);
		ksw.write(cert.getSerialNumber().toString(), keyPairSubject.getPrivate(), "mikimaus".toCharArray(), cert);
		ksw.saveKeyStore("appkeystore.jks", "mikimaus".toCharArray());

		return true;
	}

	public List<CertificateDTO> getAllCa() {
		List<CertificateDTO> retVal = new ArrayList<CertificateDTO>();
		List<ftn.securexml.model.Certificate> allAlias = certificateRepository.findAll();
		for (int i = 0; i < allAlias.size(); i++) {
			KeyStoreReader ksr = new KeyStoreReader();
			X509Certificate cer = (X509Certificate) ksr.readCertificate("appkeystore.jks", "mikimaus",
					allAlias.get(i).getCertificateId().toString());
			if(cer.getBasicConstraints()!=-1) {
				if(isValid(Long.valueOf(makeCertDTOFromCert(cer).getIssuerId()))) {
					retVal.add(makeCertDTOFromCert(cer));
				}
			}
		}
		return retVal;
	}

	public List<CertificateDTO> getAll() {
		List<CertificateDTO> retVal = new ArrayList<CertificateDTO>();
		List<ftn.securexml.model.Certificate> allAlias = certificateRepository.findAll();
		for (int i = 0; i < allAlias.size(); i++) {
			KeyStoreReader ksr = new KeyStoreReader();
			X509Certificate cer = (X509Certificate) ksr.readCertificate("appkeystore.jks", "mikimaus",
					allAlias.get(i).getCertificateId().toString());
			retVal.add(makeCertDTOFromCert(cer));
		}
		return retVal;
	}

	public CertificateDTO getById(int i) {
		KeyStoreReader ksr = new KeyStoreReader();
		X509Certificate cer = (X509Certificate) ksr.readCertificate("appkeystore.jks", "mikimaus", Integer.toString(i));
		return makeCertDTOFromCert(cer);
	}

	public PublicKey getPublicKeyById(int i) {
		KeyStoreReader ksr = new KeyStoreReader();
		return ((X509Certificate) ksr.readCertificate("appkeystore.jks", "mikimaus", Integer.toString(i)))
				.getPublicKey();
	}

	public PrivateKey getPrivateKeyById(int i) {
		KeyStoreReader ksr = new KeyStoreReader();
		return ksr.readPrivateKey("appkeystore.jks", "mikimaus", Integer.toString(i), "mikimaus");
	}

	private IssuerData generateSelfSignedIssuerData(CertificateDTO certificate, PrivateKey issuerKey, String token) {
		X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
		// znaci da je self signed, isuer je isto kao i subject
		if (certificate.getIssuerId() == null) {
			builder.addRDN(BCStyle.CN, certificate.getCommonName());
			builder.addRDN(BCStyle.O, certificate.getOrganization());
			builder.addRDN(BCStyle.OU, certificate.getOrganizationalUnitName());
			builder.addRDN(BCStyle.C, certificate.getCountry());
			builder.addRDN(BCStyle.E, certificate.getEmail());
			builder.addRDN(BCStyle.ST, certificate.getState());
			builder.addRDN(BCStyle.L, certificate.getLocalityName());
			if (certificate.getIssuerId() == null) {
				builder.addRDN(BCStyle.UNIQUE_IDENTIFIER, certificate.getId());
			} else {
				builder.addRDN(BCStyle.UNIQUE_IDENTIFIER, certificate.getIssuerId());
			}

			// UID (USER ID) je ID korisnika
			String username = tokenUtils.getUsernameFromToken(token);
			User user = userRepository.findOneByUsername(username);
			builder.addRDN(BCStyle.UID, user.getId().toString());
		}
		// Kreiraju se podaci za issuer-a, sto u ovom slucaju ukljucuje:
		// - privatni kljuc koji ce se koristiti da potpise sertifikat koji se izdaje
		// - podatke o vlasniku sertifikata koji izdaje nov sertifikat
		return new IssuerData(issuerKey, builder.build());
	}

	private SubjectData generateSubjectData(CertificateDTO certificate, KeyPair keyPairSubject, String sn,
			String token) {
		try {
			// Datumi od kad do kad vazi sertifikat
			SimpleDateFormat iso8601Formater = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = iso8601Formater.parse(certificate.getStartDate());
			Date endDate = iso8601Formater.parse(certificate.getEndDate());

			// klasa X500NameBuilder pravi X500Name objekat koji predstavlja podatke o
			// vlasniku
			X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
			builder.addRDN(BCStyle.CN, certificate.getCommonName());
			builder.addRDN(BCStyle.O, certificate.getOrganization());
			builder.addRDN(BCStyle.OU, certificate.getOrganizationalUnitName());
			builder.addRDN(BCStyle.C, certificate.getCountry());
			builder.addRDN(BCStyle.E, certificate.getEmail());
			builder.addRDN(BCStyle.ST, certificate.getState());
			builder.addRDN(BCStyle.L, certificate.getLocalityName());
			builder.addRDN(BCStyle.UNIQUE_IDENTIFIER, certificate.getId());

			// UID (USER ID) je ID korisnika
			String username = tokenUtils.getUsernameFromToken(token);
			User user = userRepository.findOneByUsername(username);
			builder.addRDN(BCStyle.UID, user.getId().toString());
			// Kreiraju se podaci za sertifikat, sto ukljucuje:
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
		CertificateDTO cDTO = new CertificateDTO();
		try {
			X500Name subjName = new JcaX509CertificateHolder(cert).getSubject();
			X500Name iss = new JcaX509CertificateHolder(cert).getIssuer();

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
			cDTO.setState(staname);

			RDN en = subjName.getRDNs(BCStyle.E)[0];
			String emname = IETFUtils.valueToString(en.getFirst().getValue());
			cDTO.setEmail(emname);

			RDN issuerId = iss.getRDNs(BCStyle.UNIQUE_IDENTIFIER)[0];
			String issId = IETFUtils.valueToString(issuerId.getFirst().getValue());
			cDTO.setIssuerId(issId);

			cDTO.setId(String.valueOf((cert).getSerialNumber()));
			cDTO.setStartDate(cert.getNotBefore().toString());
			cDTO.setEndDate(cert.getNotAfter().toString());

			if(cert.getBasicConstraints()!=-1) {
				cDTO.setCa(true);
			}
			else {
				cDTO.setCa(false);
			}
			
			return cDTO;

		} catch (CertificateEncodingException e) {
			e.printStackTrace();
			return null;
		}

	}

	public boolean revoke(Long id, String reason) {
		Certificate c = certificateRepository.findByCertificateId(id);
		if (c == null)
			return false;
		c.setRevoked(true);
		c.setRevokeReason(reason);
		certificateRepository.save(c);
		return true;
	}

	public Boolean isRevoked(Long id) {
		Certificate c = certificateRepository.findByCertificateId(id);
		if (c == null)
			return null;
		return c.isRevoked();
	}

	public Boolean isDateOk(Long id) {
		KeyStoreReader ksr = new KeyStoreReader();
		X509Certificate c = (X509Certificate) ksr.readCertificate("appkeystore.jks", "mikimaus", id.toString());
		Date start = c.getNotBefore();
		Date end = c.getNotAfter();
		System.out.println(Calendar.getInstance().getTime());
		if (start.after(end) || start.after(Calendar.getInstance().getTime())
				|| end.before(Calendar.getInstance().getTime())) {
			return false;
		}
		return true;
	}

	public Boolean isValid(Long id) {
		KeyStoreReader ksr = new KeyStoreReader();
		X509Certificate c = (X509Certificate) ksr.readCertificate("appkeystore.jks", "mikimaus", id.toString());
		Long parentId = null;
		try {
			parentId = Long.valueOf(makeCertDTOFromCert(c).getIssuerId());
		} catch (Exception e) {
			parentId = null;
		}
		if (parentId != id) {
			if (!isValid(parentId)) {
				return false;
			}
		}
		return !isRevoked(id) && isDateOk(id);
	}

	public String revokedReason(Long id) {
		Certificate c = certificateRepository.findByCertificateId(id);
		return c.getRevokeReason();
	}

	public boolean createKeyStore(KeystoreDTO keystoreDTO, HttpServletResponse response) throws FileNotFoundException, IOException {

		for (int i = 0; i < keystoreDTO.getId_arr().size(); i++) {
			KeyStoreReader ksr = new KeyStoreReader();
			X509Certificate cer = (X509Certificate) ksr.readCertificate("appkeystore.jks", "mikimaus",
					keystoreDTO.getId_arr().get(i).toString());
			PrivateKey privateKey = ksr.readPrivateKey("appkeystore.jks", "mikimaus",
					keystoreDTO.getId_arr().get(i).toString(), "mikimaus");
			response.setContentType("application/x-jks-file");

			
			String f=keystoreDTO.getName();
			if(!f.endsWith("/jks"))
				f+=".jks";
			response.setHeader("Content-Disposition", "attachment; filename="+f); 
			// Snimanje sertifikata u keystore
			KeyStoreWriter ksw = new KeyStoreWriter();
			if (i == 0) {
				ksw.loadKeyStore(null, null);
			} else {
				ksw.loadKeyStore(keystoreDTO.getName(), keystoreDTO.getPassword().toCharArray());
			}
			ksw.write(cer.getSerialNumber().toString(), privateKey, keystoreDTO.getPassword().toCharArray(), cer);
			ksw.saveKeyStore(keystoreDTO.getName(), keystoreDTO.getPassword().toCharArray());
			InputStream in=(new BufferedInputStream(new FileInputStream(keystoreDTO.getName())));
			
			FileCopyUtils.copy(in, response.getOutputStream());
		}

		return true;
	}

}
