package ftn.securexml.certificate.dto;

import java.util.ArrayList;
import java.util.List;

public class KeystoreDTO {
	List<Long> id_arr = new ArrayList<Long>();
	String name;
	String password;
	
	public KeystoreDTO() {
	}
	
	public List<Long> getId_arr() {
		return id_arr;
	}

	public void setId_arr(List<Long> id_arr) {
		this.id_arr = id_arr;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
