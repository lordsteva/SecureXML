package ftn.securexml.certificate.service;

import java.io.IOException;
import java.io.StringWriter;
import java.security.cert.Certificate;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ft.securexml.certificate.keystores.KeyStoreReader;
import ftn.securexml.repository.CertificateRepository;


@Service
public class DownloadService {
	@Autowired
	private CertificateRepository certificateRepository;
public String getPem(Long id) throws IOException {
		
		KeyStoreReader ks=new KeyStoreReader();
		Certificate cer=ks.readCertificate("appkeystore.jks", "mikimaus", id.toString());
	    StringWriter sw = new StringWriter();
	    try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
	        pw.writeObject(cer);
	    }
	    return sw.toString();
	}
	
}
