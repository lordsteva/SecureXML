package ftn.securexml.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "certificates")
public class Certificate {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "certificateId", nullable = false)
	private String certificateId;

	@Column(name = "isCa", nullable = false)
	private boolean isCa;
	
	public Certificate() {
		
	}

	public Certificate(boolean isCa) {
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
