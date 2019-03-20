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
	private Long certificateId;

	@Column(name = "isCa", nullable = false)
	private boolean isCa;
	
	@Column(nullable = false)
	private boolean revoked;
	
	@Column
	private String revokeReason;
	
	public Certificate() {
		
	}
	
	public Certificate(boolean isCa) {
		super();
		this.isCa = isCa;
	}
	
	public Long getCertificateId() {
		return certificateId;
	}

	public void setCertificateId(Long certificateId) {
		this.certificateId = certificateId;
	}

	public boolean isCa() {
		return isCa;
	}

	public void setCa(boolean isCa) {
		this.isCa = isCa;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public void setRevoked(boolean revoked) {
		this.revoked = revoked;
	}

	public String getRevokeReason() {
		return revokeReason;
	}

	public void setRevokeReason(String revokeReason) {
		this.revokeReason = revokeReason;
	}
	
	
}
