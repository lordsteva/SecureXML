package ftn.securexml.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "certificates")
public class Certificate {
	
	@Id
	@Column(name = "certificateId", nullable = false)
	private String certificateId;

	@Column(name = "isCa", nullable = false)
	private boolean isCa;
	
	public Certificate() {
		
	}

	public Certificate(String certificateId, boolean isCa) {
		super();
		this.certificateId = certificateId;
		this.isCa = isCa;
	}

	public String getCertificateId() {
		return certificateId;
	}

	public void setCertificateId(String certificateId) {
		this.certificateId = certificateId;
	}

	public boolean isCa() {
		return isCa;
	}

	public void setCa(boolean isCa) {
		this.isCa = isCa;
	}
	
}
