package ftn.securexml.certificate.dto;

import java.util.ArrayList;

public class KeystoreDTO {
	ArrayList id_arr = new ArrayList<Long>();
	String name;
	String password;
	
	public KeystoreDTO() {
	}
	
	public ArrayList getId_arr() {
		return id_arr;
	}
	public void setId_arr(ArrayList id_arr) {
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
