package ftn.securexml.certificate.service;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import ft.securexml.certificate.keystores.KeyStoreReader;
import ft.securexml.certificate.keystores.KeyStoreWriter;
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
public void downloadKS(Map<String, Object> data, HttpServletResponse response) {
/*	String password=(String) data.get("password");
	List<String> ids=(List<String>) data.get("ids");
	KeyStore ret= KeyStore.getInstance("JKS", "SUN");
	ret.load(null, password.toCharArray());
	ret.
	certificateService.getById(idd.intValue());
	String s;

	s = downloadService.getPem(idd);
	
	byte[] ret= IOUtils.toByteArray(s);
	response.setContentType("application/x-crt-file");

	response.setContentLength(ret.length);//length in bytes
	
	response.setHeader("Content-Disposition", "attachment; filename="+idd+".crt"); 
	//response.setHeader(“Content-Disposition”, “inline; filename=” + fileName);
	
    FileCopyUtils.copy(ret, response.getOutputStream());
	*/
}
	
}
