package com.csii.pe.gateway.model;

public class RespResult {
	private String respCode;
	private String respMsg;
	private Object respData;
	public RespResult(String respCode,String respMsg,Object respData){
		this.respCode = respCode;
		this.respMsg = respMsg;
		this.respData = respData;
	}
	public String getRespCode() {
		return respCode;
	}
	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}
	public String getRespMsg() {
		return respMsg;
	}
	public void setRespMsg(String respMsg) {
		this.respMsg = respMsg;
	}
	public Object getRespData() {
		return respData;
	}
	public void setRespData(Object respData) {
		this.respData = respData;
	}
}
