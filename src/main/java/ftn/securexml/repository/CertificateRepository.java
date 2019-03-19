package ftn.securexml.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ftn.securexml.model.Certificate;

public interface CertificateRepository  extends JpaRepository<Certificate, Long>{
	List<Certificate>findAll();
	List<Certificate>findByIsCa(boolean isCa);
}
