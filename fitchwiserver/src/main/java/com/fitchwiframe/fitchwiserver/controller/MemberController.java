package com.fitchwiframe.fitchwiserver.controller;

import com.fitchwiframe.fitchwiserver.entity.Member;
import com.fitchwiframe.fitchwiserver.service.MemberService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Log
public class MemberController {

  @Autowired
  private MemberService memberService;

  //회원가입
  @PostMapping("/joinmember")
  private String joinMember(@RequestPart(value = "data", required = true) Member newMember,
                            @RequestPart(value = "uploadImage", required = false) MultipartFile pic,
                            HttpSession session) {
    log.info("memberController.joinMember()");
    log.info("pic" + pic);

    return memberService.joinMember(newMember, pic, session);
  }

  //중복확인
  @GetMapping("/checkduplicatesmemberId")
  private String checkDuplicatesMemberId(@RequestParam String userId) {
    log.info("memberController.checkDuplicatesMemberId()");
    return memberService.checkDuplicatesMemberId(userId);
  }

  //로그인
  @PostMapping("/loginmember")
  private Map<String, Object> loginMember(@RequestBody Member member) {
    log.info("memberController.loginMember()");
    return memberService.loginMember(member);
  }

  //회원 정보 조회
  @GetMapping("/getMemberInfo")
  private Member getMemberInfo(@RequestParam String userId){
    log.info("getMemberInfo()");
    return memberService.getMemberInfo(userId);
  }
  //회원탈퇴
  @DeleteMapping("/deleteMember")
  private String deleteMemberInfo(@RequestBody Member member, HttpSession session){
    log.info("memberController.deleteMemberInfo()");
    return memberService.deleteMemberInfo(member, session);
  }

  //팔로우 하기
  @GetMapping("/follow")
  private String followMember(@RequestParam String loginId, String pageOwner){
    log.info("membercontroller.followmember");

    return memberService.followMember(loginId,pageOwner);
  }


  //팔로우 끊기
  @DeleteMapping("/unfollow")
  private String unFollowMember(@RequestParam String loginId, String pageOwner){
    log.info("membercontroller.unfollowmember");
    return memberService.unFollowMember(loginId,pageOwner);
  }
  //팔로우 조회
  @GetMapping("/getFollowList")
  private Map<String, List<Member>> getFollowList(@RequestParam String pageOwner){
    log.info("memberController.getFollowList");
    Map<String, List<Member>> followMap = new HashMap<>();
    //내가 팔로우한 사람들
    List<Member> followMemberList = memberService.getFollowingList(pageOwner);
    //나를 팔로우하는 사람들
    List<Member>  followerList = memberService.getFollowerList(pageOwner);
    followMap.put("follow", followMemberList);
    followMap.put("follower", followerList);
    return followMap;
  }


//비밀번호 확인
  @PostMapping("/checkPwd")
  private String checkPwd(@RequestBody Member memberToCheck){
    log.info("memberController.checkPwd()");
    System.out.println("memberToCheck = " + memberToCheck);

    return memberService.checkPwd(memberToCheck);

  }

  //비밀번호 변경
  @PutMapping("/updatePwd")
  private String updatePwd(@RequestBody Member memberToChangePwd){
    log.info("memberController.updatePwd()");
    System.out.println("memberToChangePwd = " + memberToChangePwd);

    return memberService.updatePwd(memberToChangePwd);
  }

  //회원 정보 수정
  @PostMapping("/updateMemberInfo")
  private Member updateMemberInfo(@RequestPart(value = "data", required = true) Member memberToUpdate,
                                  @RequestPart(value = "uploadImage", required = false) MultipartFile pic,
                                  HttpSession session){
    log.info("memberController.updateMemberInfo");
    return memberService.updateMemberInfo(memberToUpdate, pic, session);
  }

  //카카오 로그인
  @GetMapping("/login/kakao/callback")
  public Map<String, Object> kakaoLogin(@RequestParam String code) {
    log.info("memberController.kakaoLogin");
    return  memberService.registerOrLogin(code);
  }

  //카카오 로그아웃
  @PostMapping("/logout")
  public String logoutMember(@RequestBody String accessToken ){
    log.info("memberController.logoutMember");
    log.info(accessToken);
    System.out.println("accessToken = " + accessToken);
    return memberService.logoutMember(accessToken);
  }

  //전화번호로 회원 정보 존재유무 확인
  @PostMapping("/checkPhone")
  public String checkPhone(@RequestBody String memberPhone){
    log.info("memberController.checkPhone");
    System.out.println("memberPhone = " + memberPhone);
    return memberService.checkPhone(memberPhone);
  }

  //전화번호로 회원 정보 조회
  @GetMapping("/getMemberByPhone")
  public String[] getMemberByPhone(@RequestParam String memberPhone){
    log.info("memberController.getMemberByPhone");
    return memberService.getMemberByPhone(memberPhone);
  }



///////////////////////
@GetMapping("/getMemberList")
private List<Member> getMemberList() {
  return memberService.getMemberList();
}

}
