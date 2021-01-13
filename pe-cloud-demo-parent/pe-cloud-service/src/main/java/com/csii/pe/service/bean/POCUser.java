package com.csii.pe.service.bean;

import java.io.Serializable;
import java.util.Date;

public class POCUser implements Serializable {
	private static final long serialVersionUID = 267957280132066682L;
	private int id;
	private String name;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
