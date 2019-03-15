package ftn.securexml.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ftn.securexml.model.Authority;


@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long>{
	public Authority findOneByName(String name);
}
