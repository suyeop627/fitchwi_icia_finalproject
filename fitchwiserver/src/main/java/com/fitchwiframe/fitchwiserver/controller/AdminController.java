package com.fitchwiframe.fitchwiserver.controller;

import com.fitchwiframe.fitchwiserver.entity.Facilities;
import com.fitchwiframe.fitchwiserver.entity.Manager;
import com.fitchwiframe.fitchwiserver.entity.Report;
import com.fitchwiframe.fitchwiserver.service.AdminService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Log
public class AdminController {

  @Autowired
  private AdminService adminService;

//시설 조회
  @GetMapping("/getFacilitiesList")
  public Map<String, Object> getFacilitiesList(@RequestParam Integer pageNum, @RequestParam String keyword) {
    log.info("adminController.getFacilitiesList() pageNum = " + pageNum);
    System.out.println("pageNum = " + pageNum);
    System.out.println("keyword = " + keyword);
    return adminService.getFacilitiesList(pageNum,keyword);
  }

//시설 추가
  @PostMapping("/insertFacilities")
  public String insertFacilities(@RequestBody Facilities facilities) {
    log.info("adminController.insertFacilities();");
    return adminService.insertFacilities(facilities);
  }

  //시설 상세 조회
  @GetMapping("/getFacilitiesInfo/{facilitiesCode}")
  public Facilities getFacilitiesInfo(@PathVariable Long facilitiesCode) {
    log.info("adminController.getFacilitiesInfo()");
    return adminService.getFacilitiesInfo(facilitiesCode);
  }

  //시설 정보 삭제
  @DeleteMapping("/deleteFacilities/{facilitiesCode}")
  public String deleteFacilities(@PathVariable Long facilitiesCode) {
    log.info("adminController.deleteFacilities();");
    return adminService.deleteFacilities(facilitiesCode);
  }

  //시설 정보 수정
  @PutMapping("/updateFacilities/{facilitiesCode}")
  public String updateFacilities(@RequestBody Facilities newFacilities, @PathVariable Long facilitiesCode) {
    log.info("adminController.updateFacilities();");
    return adminService.updateFacilities(newFacilities, facilitiesCode);
  }

  //시설의 이용 불가일 조회
  @GetMapping("/getNodayList")
  public List<String> getNodayList(@RequestParam Long facilitiesCode) {
    log.info("adminFacilities.getNoday()");
    return adminService.getNodayList(facilitiesCode);
  }

  //이용 불가일 추가
  @GetMapping("/addNodayList")
  public String addNodayList(@RequestParam List<String> noDayToSend, @RequestParam Long facilitiesCode) {
    log.info("adminController.addNodayList()");
    return adminService.addNodayList(noDayToSend, facilitiesCode);
  }

  //이용 불가일 삭제
  @DeleteMapping("/deleteNodayList")
  public String deleteNodayList(@RequestParam List<String> noDayToSend, @RequestParam Long facilitiesCode) {
    log.info("adminController.deleteNodayList()");
    return adminService.deleteNodayList(noDayToSend, facilitiesCode);
  }

  //신고하기
  @PostMapping("/report")
  public String report(@RequestBody Report report) {
    log.info("adminController.report()");
    return adminService.report(report);
  }

  //기신고 유무 확인
  @GetMapping("/checkReported")
  public String checkReported(@RequestParam String user, @RequestParam Long target, @RequestParam String targetMember) {
    log.info("adminController.checkReported()");
    return adminService.checkReported(user, target, targetMember);
  }
//신고 내역 조회
  @GetMapping("/getReports")
  public Map<String, Object> getReportList(@RequestParam Integer pageNum, @RequestParam String keyword) {
    log.info("adminController.getReportList() pageNum = "+pageNum);
    return adminService.getReportList(pageNum, keyword);
  }
//신고 받은 회원 로그인 제한
  @PutMapping("/restrictMember/{restrictDate}/{memberEmail}")
  public String restrictMember(@PathVariable  String restrictDate, @PathVariable  String memberEmail) {
    log.info("adminController.restrictMember");
    return adminService.restrictMember(restrictDate, memberEmail);
  }
//신고내역 삭제
  @DeleteMapping("/deleteReport")
  public String deleteReport(Long reportCode) {
    log.info("adminController.deleteReport()");
    return adminService.deleteReport(reportCode);
  }
//신고 내역에 제제 내역 추가
  @PutMapping("/updateReportState/{reportCode}/{reportTreatment}")
  public String updateReportState(@PathVariable Long reportCode,@PathVariable String reportTreatment) {
    log.info("adminController.updateReportState()");
    return adminService.updateReportState(reportCode,reportTreatment);
  }
  //관리자 로그인
  @PostMapping("/managerLogin")
  public String managerLogin(@RequestBody Manager manager){
    System.out.println("manager = " + manager);
    log.info("adminController.managerLogin");
    return adminService.managerLogin(manager);
  }

/////////////////////////


  @GetMapping("/getAllFacilitiesList")
  public Iterable<Facilities> getAllFacilitiesList() {
    log.info("adminController.getAllFacilitiesList()");
    return adminService.getAllFacilitiesList();
  }
}
