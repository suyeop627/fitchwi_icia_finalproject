package com.fitchwiframe.fitchwiserver.service;

import com.fitchwiframe.fitchwiserver.entity.*;
import com.fitchwiframe.fitchwiserver.repository.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Log
public class AdminService {

  @Autowired
  private FacilitiesRepository facilitiesRepository;
  @Autowired
  private NodayRepository nodayRepository;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private ReportRepository reportRepository;
  @Autowired
  private ReportDetailRepository reportDetailRepository;

  @Autowired
  private ManagerRepository managerRepository;

  @Autowired
  TogetherOpenedRepository togetherOpenedRepository;
  @Autowired
  TogetherRepository togetherRepository;

  //시설 목록 조회
  public Map<String, Object> getFacilitiesList(Integer pageNum, String keyword) {
    log.info("getFacilitiesList()");
//    if (pageNum == null) {
//      pageNum = 1;
//    }
    int listCount = 6;
    //facilitiesCode를 기준으로 내림차순, 6개씩 조회. -> facilitiesCode는 시설 저장시점의 밀리초로 저장되므로, 최신순 정렬됨.
    Pageable pageable = PageRequest.of((pageNum - 1), listCount, Sort.Direction.DESC, "facilitiesCode");


    Page<Facilities> result = null;

    if (keyword.equals("")) {
      //검색어가 없는 경우, 전체 목록을 기준으로 조회
      result = facilitiesRepository.findAll(pageable);

    } else {
      //검색어가 존재할 경우, 해당 검색어를 포함한 목록 조회
      String keywordToSearch = "%" + keyword + "%";
      result = facilitiesRepository.findByFacilitiesNameLike(keywordToSearch, pageable);
    }

    List<Facilities> facilitiesList = result.getContent();
    int totalPage = result.getTotalPages();

    Map<String, Object> mapToReturn = new HashMap<>();
    mapToReturn.put("totalPage", totalPage);
    mapToReturn.put("facilitiesList", facilitiesList);
    //sessionStorage에 저장할 페이지 번호와 검색어도 같이 반환.
    // -> 상세페이지 이동 후 뒤로가기를 했을 때, 해당 페이지 정보를 유지하기 위해 활용
    mapToReturn.put("pageNum", pageNum);
    mapToReturn.put("keyword", keyword);
    return mapToReturn;
  }


  //시설 추가
  public String insertFacilities(Facilities facilities) {
    log.info("adminService.insertfacilities()");
    String result = "fail";
    try {
      //입력받은 시설 정보를 db에 저장
      facilitiesRepository.save(facilities);

      result = "ok";

    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  //시설 정보 조회
  public Facilities getFacilitiesInfo(Long facilitiesCode) {
    Facilities facilities = new Facilities();
    try {
      //facilitiesCode로 조회된 시설을 반환
      facilities = facilitiesRepository.findById(facilitiesCode).get();

    } catch (Exception e) {
      e.printStackTrace();
    }
    return facilities;

  }


  //시설 삭제
  public String deleteFacilities(Long facilitiesCode) {
    log.info("adminService.deleteFacilities()");
    String result = "fail";
    try {
      //facilitesCode로 시설 조회
      Facilities facilities = facilitiesRepository.findById(facilitiesCode).get();
      List<TogetherOpened> togetherOpenedList = togetherOpenedRepository.findByFacilitiesCode(facilities);

      //해당 시설에서 함께해요 모임이 예정되어 있을 경우, 해당 시설 정보 삭제 불가.
      if (!(togetherOpenedList.isEmpty())) {
        result = "togetherExist";
        return result;
      }



      //해당 시설에 지정돼있는 이용 불가일 삭제
      nodayRepository.deleteAll(nodayRepository.findAllByFacilitiesCode(facilities));

      //시설 삭제
      facilitiesRepository.deleteById(facilitiesCode);
      result = "ok";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  //시설 정보 수정
  public String updateFacilities(Facilities newFacilities, Long facilitiesCode) {
    log.info("adminService.updateFacilities()");
    String result = "fail";
    try {
      //새로운 시설 정보에 기존 facilitiesCode를 저장
      newFacilities.setFacilitiesCode(facilitiesCode);
      //새로운 시설 정보 저장.(기존 시설의 기본키 사용)
      facilitiesRepository.save(newFacilities);
      result = "ok";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  //이용불가일 조회
  public List<String> getNodayList(Long facilitiesCode) {
    log.info("adminService.getNoday()");
    //해당 시설의 이용불가일(yyyy-MM-dd)이 배열의 형태로 저장될 list 생성
    List<String> nodayDateList = new ArrayList<>();
    try {
      //해당 시설의 entity를 기준으로, 이용 불가일 목록 조회
      List<Noday> nodayList = nodayRepository.findAllByFacilitiesCode(facilitiesRepository.findById(facilitiesCode).get());
      //이용불가일 목록이 존재한다면, 각 날짜들을 nodayDateList에 저장 후 반환.
      if (!nodayList.isEmpty()) {
        for (Noday noday : nodayList) {
          nodayDateList.add(noday.getNodayDate());
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return nodayDateList;
  }

  //이용불가일 추가
  public String addNodayList(List<String> noDayToSend, Long facilitiesCode) {
    log.info("adminService.addNodayList()");
    String result = "fail";
    try {
      Facilities facilities = facilitiesRepository.findById(facilitiesCode).get();
      //배열로 전달받은 이용불가일들을 각각 저장
      for (String date : noDayToSend) {
        Noday noday = new Noday();
        noday.setNodayDate(date);
        noday.setFacilitiesCode(facilities);
        nodayRepository.save(noday);

      }
      result = "ok";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  //이용불가일 삭제
  public String deleteNodayList(List<String> noDayToSend, Long facilitiesCode) {
    log.info("adminService.deleteNodayList()");
    String result = "fail";
    try {
      Facilities facilities = facilitiesRepository.findById(facilitiesCode).get();
//해당 시설을 이용중인 함께해요 개설 목록 조회
      List<TogetherOpened> togetherOpenedList = togetherOpenedRepository.findByFacilitiesCode(facilities);

      //삭제하려는 날짜에 함께해요가 예정돼있는 지 확인
      for (String noday : noDayToSend) {

        //삭제하려는 이용 불가일을 이용중인 함께해요가 있을 경우, 삭제 불가.
        for (TogetherOpened to : togetherOpenedList) {
          Together together = togetherRepository.findByTogetherOpenedCodeAndTogetherDate(to, noday);


          if (together != null) {
            result = "togetherExist";
            return result;
          }

        }
      }

//함께해요 예정일이 포함되지 않은 경우,

      //해당 시설의 모든 이용 불가일 목록 조회
      List<Noday> allByFacilitiesCode = nodayRepository.findAllByFacilitiesCode(facilities);

      //삭제하려는 날짜와
      for (String date : noDayToSend) {
        //해당 시설의 이용 불가일이
        for (Noday noday : allByFacilitiesCode) {
          //같은 것이 존재할 경우, 삭제
          if (noday.getNodayDate().equals(date)) {
            nodayRepository.delete(noday);
          }
        }
        result = "ok";
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  //신고하기
  public String report(Report report) {
    log.info(report.toString());
    log.info("adminService.report()");

    String result = "fail";
    log.info(report.toString());

    try {
      //신고 대상 회원 정보 가져오기
      Member targetMember = memberRepository.findById(report.getMemberEmail().getMemberEmail()).get();
      //카테고리(마이페이지, 피드, 얘기해요), 신고대상(게시글의 기본키/회원 신고일 경우 0), 신고대상회원(이메일)이 일치하는 신고내역있는지 조회
      Report existingReport = reportRepository.findByReportCategoryAndReportTargetAndMemberEmail(report.getReportCategory(), report.getReportTarget(), targetMember);
      if (existingReport == null) {//없으면 첫 신고이므로, 새로 저장
        report.setMemberEmail(targetMember);
        Report savedReport = reportRepository.save(report);
        ReportDetail reportDetail = report.getReportDetailList().get(0);
        reportDetail.setReportCode(savedReport);
        reportDetail.setMemberEmail(memberRepository.findById(reportDetail.getMemberEmail().getMemberEmail()).get());
        reportDetailRepository.save(reportDetail);
        result = "ok";
      } else { //있으면 기존 신고내역의 카운트를 1 증가시키고, 새로 저장
        existingReport.setReportedCount(existingReport.getReportedCount() + 1);
        ReportDetail reportDetail = report.getReportDetailList().get(0);
        reportDetail.setReportCode(existingReport);
        reportDetail.setMemberEmail(memberRepository.findById(reportDetail.getMemberEmail().getMemberEmail()).get());
        reportDetailRepository.save(reportDetail);
        result = "ok";
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

  //신고여부 확인
  public String checkReported(String user, Long target, String targetMember) {
    log.info("adminService.checkReported()");
    String result = "fail";


    try {
      Member memberReported = memberRepository.findById(targetMember).get();
      Member memberReporting = memberRepository.findById(user).get();
      //신고받은 회원과 target으로, 모든 신고내역 조회(회원신고 또는 게시글 신고 중에서 이번에 신고받은 것과 일치하는 내역 조회)
      //해당 신고대상이 이미 신고받은 적이 있는지, 아니면 첫신고인지 확인.
      List<Report> reportedList = reportRepository.findAllByMemberEmailAndReportTarget(memberReported, target);
      log.info("reportedList = " + reportedList.isEmpty());
      //이미 신고받은 내역이 없다면, 신고가 없다면 신고 가능
      if (reportedList.isEmpty()) {
        result = "ok";

      } else {//신고받은 내역이 존재할 경우,
        if (target != 0) {//게시글 신고
          //해당 신고내역의 상세 신고 내역 테이블에서 신고하는 회원이 이미 신고한 적이 있는 지 조회
          for (Report report : reportedList) {
            ReportDetail reportDetail = reportDetailRepository.findByReportCodeAndMemberEmail(report, memberReporting);

            //신고내역이 존재한다면 신고 불가.
            if (reportDetail != null) {
              result = "reported";
            } else {
              //없다면 신고 가능
              result = "ok";
            }
          }
        } else {//회원신고
          //앞에서 조회된 신고대상의 신고목록 중에서, 이번에 신고하는 회원이 이미 신고한 적이 있는지 조회
          for (Report report : reportedList) {
            ReportDetail reportDetail = reportDetailRepository.findByReportCodeAndMemberEmail(report, memberReporting);
            if (reportDetail != null) {
              //신고상세에 회원 이메일과 reportCode가 일치하는 게 존재한다면, 신고 불가.
              result = "reported";
            } else {
              //아니면 신고 가능
              result = "ok";
            }
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  //신고 목록 조회
  public Map<String, Object> getReportList(Integer pageNum, String keyword) {
    log.info("adminService.getReportList()");


    if (pageNum == null) {
      pageNum = 1;
    }
    int listCount = 8;
    Pageable pageable = PageRequest.of((pageNum - 1), listCount, Sort.Direction.DESC, keyword);
    Page<Report> result = reportRepository.findAll(pageable);


    List<Report> reportList = result.getContent();
    int totalPage = result.getTotalPages();


    Map<String, Object> mapToReturn = new HashMap<>();
    //페이지 번호를 표기하기위한 총 페이지 수
    mapToReturn.put("totalPage", totalPage);
    //현재 페이지 정보를 저장하기위해 페이지 번호와, 검색 키워드를 같이 반환.-> 세션 스토리지에 저장해서 활용
    mapToReturn.put("pageNum", pageNum);
    mapToReturn.put("keyword", keyword);


    try {
      //각 신고대상의 상세 신고 내역 목록 조회
      for (Report report : reportList) {
        report.getMemberEmail().setMemberPwd(null);
        List<ReportDetail> reportDetailList = reportDetailRepository.findAllByReportCodeOrderByReportDetailDateDesc(report);
        for (ReportDetail reportDetail : reportDetailList) {
          reportDetail.getMemberEmail().setMemberPwd(null);
        }
        report.setReportDetailList(reportDetailList);

      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    mapToReturn.put("reportList", reportList);
    return mapToReturn;
  }

  //회원에 로그인 제한일 설정
  public String restrictMember(String restrictDate, String targetMemberEmail) {
    log.info("restrictMemberEmail()");

    String result = "fail";
    try {
      //제제할 회원 조회
      Member targetMember = memberRepository.findById(targetMemberEmail).get();
      //이용 제한일 저장
      targetMember.setMemberRestriction(restrictDate);

      memberRepository.save(targetMember);
      result = "ok";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;

  }

  //신고내역 삭제
  @Transactional
  public String deleteReport(Long reportCode) {
    String result = "fail";
    log.info("adminService.deleteReport()");
    try {
      //신고 코드로 내역을 삭제할 신고 조회
      Report reportToDelete = reportRepository.findById(reportCode).get();
      //해당 신고내역의 상세 내역 목록 삭제
      reportDetailRepository.deleteAllByReportCode(reportToDelete);
    //신고 내역 삭제
      reportRepository.delete(reportToDelete);
      result = "ok";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  //신고 내역의 처리상태 값 변경
  public String updateReportState(Long reportCode, String reportTreatment) {
    String result = "fail";
    log.info("adminService.updateReportState()");
    try {
      //상태를 변경할 신고 내역 조회
      Report report = reportRepository.findById(reportCode).get();
      //제제 종류에 따라서 처리상태 저장
      if (reportTreatment.equals("신고 대상 삭제")) {
        //게시글을 삭제한 경우
        report.setReportState(reportTreatment);
      } else {//회원의 로그인을 제한한 경우
        report.setReportState(reportTreatment + "까지 이용 제한");
      }

      reportRepository.save(report);
      result = "ok";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  //회원 탈퇴시 해당 회원관련 신고 데이터 삭제
  public void deleteAllByMember(Member member) {
    //내가 신고한 reportdetail 조회 후, 존재한다면 전체 삭제
    List<ReportDetail> reportDetailList = reportDetailRepository.findByMemberEmail(member);

    if (!(reportDetailList.isEmpty())) {
      reportDetailRepository.deleteAll(reportDetailList);
    }

    //내가 신고받은 report 조회 후, 존재한다면 전체 삭제
    List<Report> reportList = reportRepository.findByMemberEmail(member);
    if (!(reportList.isEmpty())) {
      for (Report report : reportList) {
        //나를 신고한 reportdetail 조회 후 삭제
        List<ReportDetail> reportedDetailList = reportDetailRepository.findByReportCode(report);
        reportDetailRepository.deleteAll(reportedDetailList);
      }


      reportRepository.deleteAll(reportList);
    }


  }

  //관리자 로그인
  public String managerLogin(Manager manager) {
    log.info("adminService.managerLogin()");
    String result = "fail";

    try {
      Optional<Manager> byId = managerRepository.findById(manager.getManagerId());
      if (byId.isPresent()) {

        Manager dbManager = byId.get();
        if (dbManager.getManagerPwd().equals(manager.getManagerPwd())) {
          result = "ok";
        } else {
          result = "wrong pwd";
        }

      } else {
        result = "no data";
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;


  }


  ///////////////////////
  public Iterable<Facilities> getAllFacilitiesList() {
    log.info("adminController.getAllFacilitiesList()");
    return facilitiesRepository.findAll();
  }


}
