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

	public Certificate(String certificateId) {
		super();
		this.certificateId = certificateId;
	}

	public String getCertificateId() {
		return certificateId;
	}

	public void setCertificateId(String certificateId) {
		this.certificateId = certificateId;
	}
	
}
