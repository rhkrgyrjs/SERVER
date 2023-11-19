package form;

import java.io.*;

/*
 * 서버와 유저의 로그인/회원가입 요청/응답 시 주고받을 정보의 포멧을 정해 놓음.
 * 0. 잘못된 요청 
 * 1. 로그인 시 : ID, PW 사용
 * 2. 회원가입 시 : 잡다한 정보 사용
 * 3. ID 중복체크 시 : ID 사용
 * 4. 우편번호 찾기 : 도(광역시), 시(구), 도로명, 상세주소 사용.
 * 5. 프로필 사진 전송 시 : 서버가 프로필 사진 전송 받을 준비를 끝냄.   
 * 6. 본인인증 시(비번 변경할때)
 * 7. 본인인증 이후 비번 변경시 
 */

public class LoginRequestForm implements Serializable
{
	// 직렬화 처리 
	private static final long serialVersionUID = 1L;
	
	private int reqType = 0;
	private String id = null;
	private String pw = null;
	private String nickname = null;
	private String email = null;
	private String phoneNumber = null;
	private String zipcode = null;
	private String zipcode_do = null;
	private String zipcode_si = null;
	private String zipcode_doro = null;
	private String zipcode_num = null;
	private byte[] picBlob = null;
	
	
	public void setReqType(int type) {this.reqType = type;}
	public void setId(String id) {this.id = id;}
	public void setPw(String pw) {this.pw = pw;}
	public void setNickname(String nickname) {this.nickname = nickname;}
	public void setEmail(String email) {this.email = email;}
	public void setPhonenumber(String phonenubmer) {this.phoneNumber = phonenubmer;}
	public void setZipcode(String zipcode) {this.zipcode = zipcode;}
	public void setDo(String zipdo) {this.zipcode_do = zipdo;}
	public void setSi(String zipsi) {this.zipcode_si = zipsi;}
	public void setDoro(String zipdoro) {this.zipcode_doro = zipdoro;}
	public void setZipNum(String zipNum) {this.zipcode_num = zipNum;}
	public void setPicBlob(byte[] blob) {this.picBlob = blob;}
	
	public int getReqType() {return this.reqType;}
	public String getId() {return this.id;}
	public String getPw() {return this.pw;}
	public String getNickName() {return this.nickname;}
	public String getEmail() {return this.email;}
	public String getPhoneNumber() {return this.phoneNumber;}
	public String getZipcode() {return this.zipcode;}
	public String getDo() {return this.zipcode_do;}
	public String getSi() {return this.zipcode_si;}
	public String getDoro() {return this.zipcode_doro;}
	public String getZipNum() {return this.zipcode_num;}
	public byte[] getPicBlob() {return this.picBlob;}
}
