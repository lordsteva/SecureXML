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
	
	@Column(name = "alias", nullable = false)
	private String alias;

	public Certificate(String certificateId, String alias) {
		super();
		this.certificateId = certificateId;
		this.alias = alias;
	}

	public String getCertificateId() {
		return certificateId;
	}

	public void setCertificateId(String certificateId) {
		this.certificateId = certificateId;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

}
