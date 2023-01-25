package com.fitchwiframe.fitchwiserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitchwiframe.fitchwiserver.dto.KakaoProfile;
import com.fitchwiframe.fitchwiserver.entity.*;

import com.fitchwiframe.fitchwiserver.repository.FollowRepository;
import com.fitchwiframe.fitchwiserver.repository.MemberRepository;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Log
public class MemberService {
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private FollowRepository followRepository;

  @Autowired
  private FeedService feedService;

  @Autowired
  private TalkService talkService;
  @Autowired
  private AdminService adminService;
  @Autowired
  private TogetherService togetherService;


  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


  //회원가입
  public String joinMember(Member newMember, MultipartFile pic, HttpSession session) {
    log.info("memberService.joinmember");

    String result = null;
    String cryptPwd = null;
    //pwd가 null이 아닌 경우(일반회원일 경우), 암호화해서 저장
    if (newMember.getMemberPwd() != null) {
      cryptPwd = encoder.encode(newMember.getMemberPwd());
      newMember.setMemberPwd(cryptPwd);

      try {
        if (pic != null) {      //요청에 파일이 있다면, 파일 업로드
          fileUpload(newMember, pic, session);
        } else {              //요청에 파일이 없다면, 기본이미지의 이름 저장
          newMember.setMemberImg("DefaultProfileImage.jpg");
          //카카오 프로필 사진이 url로 저장되므로, 프론트에서 분기를 줄이기 위해서 /images/... 로 저장함.
          // src={memberSaveimg} 의 형태로 카카오 회원 및 일반회원 모두 사용 가능
          newMember.setMemberSaveimg("/images/DefaultProfileImageSystemNameBasic.jpg");
        }

        memberRepository.save(newMember);
        log.info("가입 성공");
        result = "ok";
      } catch (Exception e) {
        e.printStackTrace();
        log.info("가입 실패");
        result = "fail";
      }
    } else {
      try {
        memberRepository.save(newMember);
        log.info("가입 성공");
        result = "ok";
      } catch (Exception e) {
        e.printStackTrace();
        log.info("가입 실패");
        result = "fail";
      }

    }
    return result;
  }


  private void fileUpload(Member member, MultipartFile pic,
                          HttpSession session)
      throws Exception {
    log.info("memberService.fileUpload()");

    String realPath = session.getServletContext().getRealPath("/"); //절대경로 가져오기 (/.../webapp/)
    log.info("realPath : " + realPath);

    realPath += "images/";//webapp 하위의 images 폴더로 경로 지정
    File folder = new File(realPath);
    if (folder.isDirectory() == false) {//webapp 하위에 images폴더가 없다면 생성
      folder.mkdir();
    }

    String orname = pic.getOriginalFilename();//파일의 원래 이름을

    member.setMemberImg(orname);//회원 테이블에 저장

    String sysname = System.currentTimeMillis() //파일의 중복을 피하기 위해 서버에 저장될 이름을 밀리초로 변경
        + orname.substring(orname.lastIndexOf("."));//원래이름의 확장자를 가져와서 저장될 이름에 붙임
    member.setMemberSaveimg("/images/" + sysname);

    File file = new File(realPath + sysname);

    pic.transferTo(file);//저장 경로에 파일 생성

  }


  //중복 확인
  public String checkDuplicatesMemberId(String userId) {
    log.info("memberService.checkDuplicatesMemberId");
    String result = "ok";
    try {
      memberRepository.findById(userId).get();//아이디로 멤버 테이블 조회.
      log.info("불가");
      result = "fail";//조회 된 경우, 아이디 이용불가
    } catch (Exception e) {
      e.printStackTrace();//예외 발생 시 사용 가능
    }
    return result;
  }


  //로그인
  public Map<String, Object> loginMember(Member inputMember) {
    log.info("memberService.loginMember()");

    Map<String, Object> result = new HashMap<>();

    Member dbMember = null;
    try {
      dbMember = memberRepository.findById(inputMember.getMemberEmail()).orElseGet(Member::new);//입력받은 아이디로 db에 저장된 회원 정보 조회. 없을 경우, 새 객체 생성
      if (dbMember.getMemberRestriction() != null) { //회원에게 로그인 제한일이 존재한다면

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();//오늘 날짜
        Date restriction = dateFormat.parse(dbMember.getMemberRestriction()); //회원의 이용 제한일을 Date 형식으로 변경


        //compareTo(param) : 인자가 더 클 경우 반환값은 0보다 크고, 인자가 더 작을경우 반환값은 0보다 작다. 0일 경우, 서로 같다.
        if (restriction.compareTo(today) > 0) {//오늘날짜(패러미터)가 더 클 경우
          result.put("state", "reported");//로그인 제한
          result.put("memberRestriction", dbMember.getMemberRestriction());

        } else {//오늘날짜(패러미터)가 restriction보다 작거나 같을 경우, 제한 해제
          result.put("state", "released");
          result.put("memberRestriction", dbMember.getMemberRestriction());
          dbMember.setMemberRestriction(null);//회원의 로그인 제한일을 null로 변경
          memberRepository.save(dbMember);

          result.put("memberEmail", dbMember.getMemberEmail());//기본 정보 담아서 보내기
          result.put("memberNickname", dbMember.getMemberNickname());
          result.put("mbti", dbMember.getMemberMbti());
          result.put("profileImg", dbMember.getMemberSaveimg());
        }
        return result;

      }
      if (dbMember.getMemberEmail() != null) {//입력받은 아이디로 조회한 객체가 존재할 경우
        if (encoder.matches(inputMember.getMemberPwd(), dbMember.getMemberPwd())) { //입력받은 비밀번호와 실제 비밀번호의 일치여부 조회
          result.put("state", "ok");
          result.put("memberEmail", dbMember.getMemberEmail());
          result.put("memberNickname", dbMember.getMemberNickname());
          result.put("mbti", dbMember.getMemberMbti());
          result.put("profileImg", dbMember.getMemberSaveimg());

        } else {//틀린 아이디
          result.put("state", "wrong pwd");
        }
      } else {//조회된 객체가 없을 경우
        result.put("state", "no data");
      }
    } catch (Exception e) {
      e.printStackTrace();

    }
    return result;

  }


  //회원 상세 정보 조회
  public Member getMemberInfo(String userId) {
    log.info("memberService.getMemberInfo()");
    Member findMember = null;
    try {
      findMember = memberRepository.findById(userId).get();
      findMember.setMemberPwd(null); //조회한 멤버가 존재할 경우, 비밀번호를 비우고 보내기
    } catch (Exception e) {
      e.printStackTrace();
    }
    return findMember;
  }

  //회원 탈퇴
  public String deleteMemberInfo(Member member, HttpSession session) {
    String result = "fail";
    try {
      //모임이 예정된 함께해요가 있을 경우 탈퇴 불가.
      if (!togetherService.isAvailableToDeleteMember(member)) {
        return "togetherExist";
      }
      //피드삭제
      feedService.deleteAllByMember(member, session);
      //얘기해요 삭제
      talkService.deleteAllByMember(member, session);
      //신고 내역 삭제
      adminService.deleteAllByMember(member);


//팔로워내역 삭제
      List<Follow> allByFollowId = followRepository.findAllByFollowId(member.getMemberEmail());
      if (!(allByFollowId.isEmpty())) {
        followRepository.deleteAll(allByFollowId);
      }
      //팔로우 내역 삭제
      List<Follow> allByMemberEmail = followRepository.findAllByMemberEmail(member);
      if (!(allByMemberEmail.isEmpty())) {
        followRepository.deleteAll(allByMemberEmail);
      }


//회원 프로필 사진 삭제
      deleteFile(member.getMemberSaveimg(), session);
      memberRepository.deleteById(member.getMemberEmail());
      result = "ok";
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }


  //회원 프로필 이미지 삭제
  private void deleteFile(String filsSysname, HttpSession session) {
    // member.getMemberSaveimg에는 '/images/'가 같이 저장돼있으므로, '/images/' 부분을 제외하고 불러오기
    String realSysname = filsSysname.substring(8);


    //기본이미지 사용중일 경우 삭제 처리 안함
    if (realSysname.equals("DefaultProfileImageSystemNameBasic.jpg")) {
      return;
    }


    String realPath = session.getServletContext().getRealPath("/");

    realPath += "images/";

    //삭제할 파일을 메모리에 생성
    File fileToDelete = new File(realPath + realSysname);

    //해당 파일이 실제로 존재한다면 삭제
    if (fileToDelete.exists()) {
      if (fileToDelete.delete()) {
        log.info("파일 삭제 성공");
      } else {
        log.info("파일을 삭제하였습니다.");
      }
    } else {
      log.info("파일이 존재하지 않습니다.");
    }
  }


  //팔로우하기
  public String followMember(String loginId, String pageOwner) {
    log.info("memberService.followMember");
    String result = "fail";

    //새로운 follor 객체를 만들어서 팔로우 대상 회원의 아이디를 저장
    Follow follow = new Follow();
    follow.setFollowId(pageOwner);
    try {
      Member loginMember = memberRepository.findById(loginId).get(); //로그인한 회원의 member 객체 조회
      follow.setMemberEmail(loginMember);//follor 객체에 저장
      followRepository.save(follow);
      result = "ok";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;

  }

  //팔로우 끊기
  public String unFollowMember(String loginId, String pageOwner) {
    String result = "fail";
    try {
      Member loginMember = memberRepository.findById(loginId).get();//로그인한 회원의 member 객체 조회
//로그인한 회원과, 팔로우를 끊으려는 대상의 아이디를 함께 가진 레코드 조회 후 삭제
      Follow follow = followRepository.findByMemberEmailAndFollowId(loginMember, pageOwner);
      followRepository.delete(follow);
      result = "ok";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;

  }

  //팔로우 조회
  //1. 팔로우하는 회원 조회
  public List<Member> getFollowingList(String pageOwner) {
    log.info("memberService.getFollowingList");
    //마이페이지의 주인(member)의 아이디로 member 객체조회
    Member member = memberRepository.findById(pageOwner).get();
///조회된 member 객체로 Follow 테이블 조회
    List<Follow> followList = followRepository.findAllByMemberEmail(member);
// 팔로우하는 회원들의 정보가 담길 새로운 Member List 생성
    List<Member> followMemberList = new ArrayList<>();
    for (Follow follow : followList) {
      //List<Follow>에 저장된 팔로우 대상들의 member 객체를 조회하고, 반환할 followMemberList에 저장
      Member member1 = memberRepository.findById(follow.getFollowId()).get();
      followMemberList.add(member1);
    }
//불필요한 정보 제거 후 반환(Dto를 활용하는 게 나았을 듯)
    return extractMemberData(followMemberList);

  }

  //2.팔로워 조회
  public List<Member> getFollowerList(String pageOwner) {
    log.info("memberService.getFollowerList");
    //마이페이지의 주인(member) 아이디로 Follow 테이블 조회
    List<Follow> followerList = followRepository.findAllByFollowId(pageOwner);
    //팔로워들의 레코드가 담길 List 생성
    List<Member> followerMemberList = new ArrayList<>();
    for (Follow follow : followerList) {
      //List<Follow> 에 저장된 팔로워들의 entity를 반환할 followerMemberList에 저장
      Member member1 = follow.getMemberEmail();//entity
      followerMemberList.add(member1);
    }
    //불필요한 정보 제거 후 반환
    return extractMemberData(followerMemberList);
  }

  //반환할 Member의 불필요한 정보 제거. -> Dto를 활용하는 방식이 더 적절했을 듯.
  private List<Member> extractMemberData(List<Member> memberList) {
    List<Member> memberListForReturn = new ArrayList<>();

    if (memberList != null) {

      for (Member member : memberList) {

        member.setMemberPwd(null);
        member.setMemberBirth("");
        member.setMemberPhone("");
        member.setMemberAddr("");
        memberListForReturn.add(member);
      }
    }


    return memberListForReturn;
  }

  //비밀번호 확인
  public String checkPwd(Member memberToCheck) {
    log.info("memberService.checkPwd()");
    String result = "";

    try {
      //입력받은 아이디로 회원 정보 조회
      Member dbMember = memberRepository.findById(memberToCheck.getMemberEmail()).get();
      if (encoder.matches(memberToCheck.getMemberPwd(), dbMember.getMemberPwd())) { //db에 저장된 회원의 비밀번호와 일치 여부 확인
        result = "ok";
      } else {
        result = "fail";
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  //비밀번호 변경
  public String updatePwd(Member memberToChangePwd) {
    log.info("memberService.checkPwd()");
    String result = "fail";
    try {
      //입력받은 아이디로 회원 정보 조회
      Member dbMember = memberRepository.findById(memberToChangePwd.getMemberEmail()).get();
      //db에 저장된 회원 비밀번호와 일치여부 확인
      if (encoder.matches(memberToChangePwd.getMemberPwd(), dbMember.getMemberPwd())) {
        //기존 비밀번호와 동일할 경우, 이후 처리없이 반환
        result = "same";

        return result;
      }
      //기존 비밀번호와 다를 경우, 새로운 비밀번호를 암호화
      String cryptPwd = encoder.encode(memberToChangePwd.getMemberPwd());
      //dbMember의 비밀번호를 변경한 뒤 저장.
      dbMember.setMemberPwd(cryptPwd);
      memberRepository.save(dbMember);
      result = "ok";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  //회원 정보 수정
  public Member updateMemberInfo(Member memberToUpdate, MultipartFile pic, HttpSession session) {
    log.info("memberService.updateMemberInfo();");
    //회원 정보 수정 후, 헤더에 회원 정보를 최신화하기 위해 반환할 인스턴스 생성
    Member updatedMember = new Member();


    try {

      //새로 저장할 회원 객체의 키값(아이디)로 기존 회원 테이블 조회
      Member dbMember = memberRepository.findById(memberToUpdate.getMemberEmail()).get();
      //기존 회원 객체의 비밀번호가 null이 아닐 경우(카카오 회원이 아닐경우), 기존 비밀번호를 새로 저장할 회원 객체에 저장
      if (dbMember.getMemberPwd() != null) {
        memberToUpdate.setMemberPwd(dbMember.getMemberPwd());
      }
      //기본 이미지 사용시(요청에 multipartfile이 없을 경우)
      if (pic == null) {
        if (memberToUpdate.getMemberImg().equals("")) {//업로드했던 이미지 사용-> 기본이미지 사용
          deleteFile(memberToUpdate.getMemberSaveimg(), session);
          memberToUpdate.setMemberImg("DefaultProfileImage.jpg");
          memberToUpdate.setMemberSaveimg("/images/" + "DefaultProfileImageSystemNameBasic.jpg");
        }//기본이미지->기본이미지 : 처리 x
      } else {//새이미지사용(기존 이미지 제거 후 새로운 이미지 업로드)
        deleteFile(memberToUpdate.getMemberSaveimg(), session);
        fileUpload(memberToUpdate, pic, session);
      }

      Member savedMember = memberRepository.save(memberToUpdate);

      //헤더 정보를 최신화 할 데이터 반환
      updatedMember.setMemberEmail(savedMember.getMemberEmail());
      updatedMember.setMemberNickname(savedMember.getMemberNickname());
      updatedMember.setMemberSaveimg(savedMember.getMemberSaveimg());
      updatedMember.setMemberMbti(savedMember.getMemberMbti());
    } catch (Exception e) {
      e.printStackTrace();
    }

    return updatedMember;
  }

  //카카오 로그인
  //토큰 받기
  public String getToken(String code) {

//access token을 받기 위한 요청 만들기
    //헤더
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

    //바디
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>(); //Map vs MultiValueMap : key값의 중복 가능 여부.
    //인가코드와 redireect_uri, rest_api key(client_id)를 body에 담기
    body.add("grant_type", "authorization_code");
    body.add("client_id", "bad1b060092a0ed86a3dfe34c2fb99f9");
    body.add("redirect_uri", "http://localhost:3000/login/kakao/callback");
    body.add("code", code);

//위에서 만든 헤더와 바디 정보를 담은 httpEntity 생성
    HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, httpHeaders);
    RestTemplate restTemplate = new RestTemplate();
    //카카오 개발자 사이트에서 지정한 형식에 맞춰 요청 보내고, 응답객체에 결과 받기
    ResponseEntity<String> response = restTemplate.exchange("https://kauth.kakao.com/oauth/token", HttpMethod.POST, tokenRequest, String.class);

    String accessToken = null;
    try {
      //응답받은 ResponseEntity에서 body정보를 가져오기.
      String responseBody = response.getBody();
      //ObjectMapper: JSON을 읽기, 쓰기, 변환 기능을 제공하는 객체
      ObjectMapper objectMapper = new ObjectMapper();
      //ObjectMapper로, 응답받은 body에서 'access_token'을 문자로 저장
      accessToken = objectMapper.readTree(responseBody).get("access_token").asText();

    } catch (Exception e) {
      e.printStackTrace();
    }

    return accessToken;
  }

  //카카오 계정 정보 가져오기
  public JsonNode getMemberInfoFromKaKao(String accessToken) {
    //카카오 개발자 사이트에서 지정한 형식대로 헤더 정보 생성
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
    httpHeaders.add("Authorization", "Bearer " + accessToken);

//생성한 헤더정보를 담은 HttpEntity 생성
    HttpEntity<MultiValueMap<String, String>> memberInfoRequest = new HttpEntity<>(httpHeaders);
    RestTemplate restTemplate = new RestTemplate();
    //카카오 개발자 사이트에서 지정한 형식대로 요청 보내고, response를 응답객체에 저장
    ResponseEntity<String> response = restTemplate.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.POST, memberInfoRequest, String.class);



    /*
    //JsonNode(key:value 형태로, 불변적이다. <-> objectNode는 가변적임)
    path(), get()을 통해 key 값을 이용한 value 가져오기
    asText(), asInt() 등의 메소드를 통해 형변환 가능
    path()나 get()을 이용해 key값을 이용해 value 값을 가져올 수 있다.
    참고로 get은 해당 값이 존재하지 않을 경우 null을 가져오므로 보통은 path를 자주 사용한다.
    path()의 경우 null 대신 MissingNode를 반환한다.
    */


    JsonNode jsonNode = null;
    try {
      String responseBody = response.getBody();
      ObjectMapper objectMapper = new ObjectMapper();
      //objectMapper.readTree(JSON) : json을 jsonNode 객체로 변환
      jsonNode = objectMapper.readTree(responseBody);


    } catch (Exception e) {
      e.printStackTrace();
    }

    return jsonNode;
  }

  //카카오 로그인한 계정이 등록된 회원인지 아닌지 판단.
  public Map<String, Object> registerOrLogin(String code, HttpSession session) {
    Map<String, Object> resultMap = new HashMap<>();
    JsonNode jsonNode = null;
    KakaoProfile kakaoProfile = null;
    String accessToken = getToken(code);
    try {
      jsonNode = getMemberInfoFromKaKao(accessToken);

      ObjectMapper objectMapper = new ObjectMapper();
      //objectMapper.treeToValue(jsonNode, class) : jsonNode를 자바 객체로 변환
      kakaoProfile = objectMapper.treeToValue(jsonNode, KakaoProfile.class);


      Member member = new Member();
      //카카오에서 받은 회원 이메일을 문자열로 저장
      String memberEmail = kakaoProfile.getKakao_account().getEmail();

      //해당 이메일이 db에 저장되어 있는지 확인
      Optional<Member> optionalMember = memberRepository.findById(memberEmail);

      //카카오 계정이 db에 저장된 회원일 경우.
      if (optionalMember.isPresent()) {
        Member dbMember = optionalMember.get();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();
        Date restriction = null;
        if (dbMember.getMemberRestriction() != null) { //로그인 제한이 걸려있는 회원일 경우.
          restriction = dateFormat.parse(dbMember.getMemberRestriction());


          if (restriction.compareTo(today) > 0) {//회원이지만, 로그인 제한 걸려있음
            resultMap.put("isPresent", "ok");
            resultMap.put("state", "reported");
            resultMap.put("memberRestriction", dbMember.getMemberRestriction());

          } else {//회원이며, 로그인 제한일이 지난 경우
            resultMap.put("isPresent", "ok");
            resultMap.put("state", "released");
            resultMap.put("memberRestriction", dbMember.getMemberRestriction());
            resultMap.put("memberEmail", dbMember.getMemberEmail());
            resultMap.put("memberNickname", dbMember.getMemberNickname());
            resultMap.put("mbti", dbMember.getMemberMbti());
            resultMap.put("profileImg", dbMember.getMemberSaveimg());
            session.setAttribute("at", accessToken);

            //로그인 제한일을 null로 변경후 저장
            dbMember.setMemberRestriction(null);
            memberRepository.save(dbMember);
          }
          return resultMap;
        }
        //로그인 제한이 없으며, 로그인 가능
        resultMap.put("isPresent", "ok");
        resultMap.put("member", dbMember);
        resultMap.put("state", "ok");
        resultMap.put("memberEmail", dbMember.getMemberEmail());
        resultMap.put("memberNickname", dbMember.getMemberNickname());
        resultMap.put("mbti", dbMember.getMemberMbti());
        resultMap.put("profileImg", dbMember.getMemberSaveimg());
        session.setAttribute("at", accessToken);

        return resultMap;
      }
      //저장된 회원 정보가 없는 경우, 회원 가입에 활용될 정보를 담아 반환.
      member.setMemberEmail(kakaoProfile.getKakao_account().getEmail());
      member.setMemberNickname(kakaoProfile.getProperties().getNickname());
      member.setMemberImg(kakaoProfile.getProperties().getProfile_image());
      member.setMemberSaveimg(kakaoProfile.getProperties().getProfile_image());
      resultMap.put("isPresent", "no");
      resultMap.put("member", member);
      return resultMap;
    } catch (Exception e) {
      e.printStackTrace();

      resultMap.put("isPresent", "fail");
      resultMap.put("member", new Member());
      return resultMap;

    }
  }

  //카카오 회원 로그아웃
  public String logoutMember(HttpSession session) {
    String result = "fail";
    String accessToken = (String) session.getAttribute("at");
    //카카오회원이 아닐경우 이하 처리 하지 않고 return
    if (accessToken == null) {
      result = "ok";
      return result;
    }
//카카오에 로그아웃 요청을 위한 헤더 정보 생성
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
    httpHeaders.add("Authorization", "Bearer " + accessToken);

//httpEntity에 헤더 정보 담기
    HttpEntity<MultiValueMap<String, String>> memberInfoRequest = new HttpEntity<>(httpHeaders);
    RestTemplate restTemplate = new RestTemplate();

    //카카오에서 지정한 형식대로 로그아웃 요청 보내기
    ResponseEntity<String> response = restTemplate.exchange("https://kapi.kakao.com/v1/user/logout", HttpMethod.POST, memberInfoRequest, String.class);

//로그아웃 요청에 대한 응답이 200일 경우, 로그아웃 처리 완료
    if (response.getStatusCode().equals(HttpStatus.OK)) {
      session.removeAttribute("at");

      result = "ok";
    }


    return result;
  }


  //입력받은 전화번호가 이미 사용중인지 여부 확인
  public String checkPhone(String memberPhone) {
    log.info("memberService.checkPhone");
    log.info(memberPhone);

    String result = "fail";
    Member byMemberPhone = memberRepository.findByMemberPhone(memberPhone);
    if (byMemberPhone == null) {
      result = "ok";
    }
    return result;
  }

  //계정찾기 : 전화번호로 회원의 아이디 조회
  public String[] getMemberByPhone(String memberPhone) {
    log.info("memberService.getMemberByPhone");
    String[] result = new String[2];

    Member byMemberPhone = memberRepository.findByMemberPhone(memberPhone);
    if (byMemberPhone == null) {
      //해당 연락처로 저장된 회원이 없는 경우
      result[0] = "no data";
    } else {
      if (byMemberPhone.getMemberPwd() == null) {
        //카카오 회원일경우
        result[0] = "kakao";
        result[1] = byMemberPhone.getMemberEmail();
      } else {
        //일반 회원일경우
        result[0] = "common";
        result[1] = byMemberPhone.getMemberEmail();
      }
    }

    return result;
  }


  ///////////////
  public List<Member> getMemberList() {
    log.info("memberService.getMemberList()");

    List<Member> memberList = new ArrayList<>();
    try {
      memberList = (List<Member>) memberRepository.findAll();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return memberList;
  }
}
