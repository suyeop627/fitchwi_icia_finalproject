import { ExpandMore, Remove } from "@mui/icons-material";
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Button,
  CircularProgress,
  Container,
  Divider,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Pagination,
  Select,
  Stack,
  styled,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { Box } from "@mui/system";
import axios from "axios";
import moment from "moment/moment";
import React, { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";

export default function ReportManagement({ swAlert }) {
  //한 페이지에 로드될 신고 대상 목록 저장
  const [reportList, setReportList] = useState([]);
  //페이지 번호 저장
  const [pageNum, setPageNum] = useState(1);
  //정렬 조건 저장
  const [keyword, setKeyword] = useState("reportCode");
  //전체 페이지 수 저장
  const [totalPage, setTotalPage] = useState(0);
  //세션 스토리지에 저장된 페이지 번호와 검색어를 가져와서 저장
  //-> 신고 대상 삭제 또는 제재를 가한 뒤의 재랜더링 시, 세션스토리지에 저장된 페이지와 정렬기준을 기준으로 신고 목록을 다시 불러온다.
  let keywordInSessionStg = sessionStorage.getItem("keyword");
  let pageNumInSessionStg = sessionStorage.getItem("pageNum");

  useEffect(() => {
    pageNumInSessionStg !== null
      ? keywordInSessionStg !== null
        ? //세션스토리지에 페이지번호 존재, 정렬조건 존재
          getReports(pageNumInSessionStg, keywordInSessionStg)
        : //세션스토리지에 페이지번호만 존재
          getReports(pageNumInSessionStg, keyword)
      : //세션스토리지에 페이지번호와 정렬조건이 둘다 없을 경우
        getReports(pageNum, keyword);

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  //신고 목록 조회
  const getReports = (pageNumInSessionStg, keywordInSessionStg) => {
    //로딩 시작
    setLoad(false);
    //세션스토리지에 저장된 페이지 번호와 정렬조건으로, 해당하는 목록 조회
    axios.get("/getReports", { params: { pageNum: pageNumInSessionStg, keyword: keywordInSessionStg } }).then((result) => {
      const { reportList, totalPage, pageNum, keyword } = result.data;

      setReportList(reportList);
      setTotalPage(totalPage);
      setPageNum(pageNum);

      //조회에 사용된 페이지 번호와 정렬조건을 세션스토리지에 저장
      sessionStorage.setItem("pageNum", pageNum);
      sessionStorage.setItem("keyword", keyword);
      //로딩 완료
      setLoad(true);
    });
  };
  //페이지 번호가 바뀔때마다 해당하는 페이지의 신고목록 조회
  const handlepageNum = (value) => {
    getReports(value, keyword);
  };

  //정렬조건 변경시, 해당 조건으로 목록 최신화
  const onSort = (e) => {
    setKeyword(e.target.value);
    getReports(1, e.target.value);
  };

  //유저의 아이디와 이용제한일을 저장할 Map
  const [restrictDateMap, setRestrictDateMap] = useState(new Map());
  console.log(restrictDateMap);
  //select의 값을 변경할때마다, restrictDateMap에 key:유저 아이다, value:이용 제한일을 추가
  const onSelectDate = useCallback((e) => {
    setRestrictDateMap((prev) => new Map(prev).set(e.target.name, e.target.value));
  }, []);

  //피신고 회원에 로그인 제한일 설정
  const onRistrict = (period, memberEmail, reportCode) => {
    //제한일을 선택하지 않은 경우, 알림 출력후 종료
    if (period === undefined) {
      swAlert("이용 제한일을 먼저 선택해주세요.", "warning");
      return;
    }
    //이용 제한일 : select로 선택된 value만큼, 오늘날짜에 가산
    let restrictDate = moment().add(period, "days").format("YYYY-MM-DD ");

    //이용 제한일과 피신고 회원의 아이디로, 이용 제한일 저장 요청
    axios.put(`/restrictMember/${restrictDate}/${memberEmail}`).then((result) => {
      //이용저장일 제한 후, 신고 대상의 처리상태 변경
      updateReportState(reportCode, restrictDate);
      //이용제한일을 저장했던 map 초기화
      setRestrictDateMap(new Map());
    });
  };
  //신고 내역 삭제
  const deleteReport = (reportCode) => {
    axios.delete("/deleteReport", { params: { reportCode: reportCode } }).then((result) => {
      if (result.data === "ok") {
        swAlert("신고 내역을 성공적으로 삭제했습니다.");
      } else {
        swAlert("신고 내역을 삭제하는 데 실패했습니다.", "info");
      }
      getReports(pageNumInSessionStg, keywordInSessionStg);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  };

  //신고 대상 삭제
  const deleteReportTarget = (reportTarget, reportCategory, memberEmail, reportCode) => {
    //reportCategory에 따라서 각각 카테고리에 해당하는 삭제 요청
    //피드 : share, 얘기해요 : talk
    switch (reportCategory) {
      case "share":
        axios
          .get("/getFeedInfo", { params: { feedCode: reportTarget } })
          .then((feed) => {
            axios
              .delete("/deleteFeed", { data: feed.data })
              .then((result) =>
                //삭제 성공시, 해당 신고 대상의 처리 상태 변경
                updateReportState(reportCode, "신고 대상 삭제")
              )
              .catch((error) => console.log(error));
          })
          .catch((error) => console.log(error));

        break;
      case "talk":
        axios
          .get("/getTalk", { params: { talkCode: reportTarget } })
          .then((talk) => {
            axios
              .delete("/deleteTalk", { data: talk.data })
              .then((result) => {
                //삭제 성공시, 해당 신고 대상의 처리 상태 변경
                updateReportState(reportCode, "신고 대상 삭제");
              })
              .catch((error) => {
                swAlert("해당 얘기해요를 삭제하는 데 실패했습니다.", "warning");
              });
          })
          .catch((error) => console.log(error));

        break;
      default:
        break;
    }
  };

  //신고 대상의 처리상태 변경
  const updateReportState = (reportCode, reportTreatment) => {
    axios.put(`/updateReportState/${reportCode}/${reportTreatment}`).then((result) => {
      if (result.data === "ok") {
        swAlert("신고 대상에 대한 처분이 성공적으로 저장됐습니다.");
      } else {
        swAlert("신고 내역 상태변경에 실패했습니다.", "info");
      }

      //상태 변경 후 해당 페이지의 내용 최신화
      getReports(pageNumInSessionStg, keywordInSessionStg);
    });
  };

  const CenterTableCell = styled(TableCell)({
    textAlign: "center",
    width: "33%",
  });
  const [load, setLoad] = useState(false);

  return (
    <Container component="main" align="center" sx={{ mt: 13 }}>
      <Box sx={{ mb: 5 }}>
        <Typography variant="h4">신고 관리</Typography>
      </Box>
      <FormControl sx={{ m: 1, minWidth: 120 }} size="small">
        <InputLabel id="demo-select-small">정렬방식</InputLabel>
        <Select
          labelId="demo-simple-select-label"
          id="demo-simple-select"
          defaultValue={"reportCode"}
          label="sortReport"
          onChange={(e) => onSort(e)}
          size="small"
        >
          <MenuItem value={"reportCode"}>최신순</MenuItem>
          <MenuItem value={"reportState"}>처리상태</MenuItem>
          <MenuItem value={"reportedCount"}>신고건수</MenuItem>
        </Select>
      </FormControl>
      <Accordion>
        <AccordionSummary
          expandIcon={<Remove />}
          aria-controls="panel1a-content"
          id="panel1a-header"
          // backgroundColor="white"
          disabled
          style={{ opacity: "1" }}
        >
          <Typography align="center" sx={{ width: "15%" }}>
            신고 코드
          </Typography>

          <Typography align="center" sx={{ width: "20%" }}>
            신고 대상
          </Typography>

          <Typography align="center" sx={{ width: "20%" }}>
            피신고 회원
          </Typography>

          <Typography align="center" sx={{ width: "15%" }}>
            신고 건수
          </Typography>
          <Typography align="center" sx={{ width: "25%" }}>
            처리 상태
          </Typography>
        </AccordionSummary>
      </Accordion>
      <Box>
        {/* 신고 내역 목록을 가져오기 전에 출력할 스피너(로딩중 표시) */}
        {load === false ? (
          <Box
            style={{
              position: "absolute",
              left: "50%",
              top: "50%",
            }}
          >
            <CircularProgress />
          </Box>
        ) : (
          reportList.map((report, index) => (
            <div key={report.reportCode}>
              <Accordion>
                <AccordionSummary expandIcon={<ExpandMore />} aria-controls="panel1a-content" id="panel1a-header">
                  <Grid container justifyContent="space-evenly">
                    <Grid item xs={2}>
                      {report.reportCode}
                    </Grid>
                    <Grid item xs={2}>
                      {/* 신고 카테고리 클릭시, 해당 게시글 또는 회원의 마이페이지로 이동. */}
                      {report.reportTarget === 0 ? (
                        // 회원 신고일 경우, 마이페이지로 이동
                        <Link
                          style={{ textDecoration: "none", color: "black" }}
                          to={`/${report.reportCategory}`}
                          state={{ memberId: report.memberEmail.memberEmail }}
                        >
                          <Typography>{report.reportCategory}</Typography>
                        </Link>
                      ) : // 게시글 신고일 경우 ,
                      report.reportState === "처분 대기" ? (
                        // 처분 대기 상태일 땐, 카테고리 클릭 시 해당 게시글 상세페이지로 이동
                        <Link to={`/${report.reportCategory}/${report.reportTarget}`} style={{ textDecoration: "none", color: "black" }}>
                          <Typography>{report.reportCategory}</Typography>
                        </Link>
                      ) : (
                        // 신고대상인 게시글을 삭제처리 했을 때, 삭제된 게시글 알림 출력
                        <Typography onClick={() => swAlert("삭제된 게시글입니다.", "info")}>{report.reportCategory}</Typography>
                      )}
                    </Grid>
                    <Grid item xs={3}>
                      {/* 피신고 회원 아이디 클릭 시, 해당 마이페이지 이동 */}
                      <Link
                        style={{ textDecoration: "none", color: "black" }}
                        to={`/memberpage`}
                        state={{ memberId: report.memberEmail.memberEmail }}
                      >
                        <Typography>{report.memberEmail.memberEmail}</Typography>
                      </Link>
                    </Grid>

                    <Grid item xs={1}>
                      <Typography>{report.reportedCount}</Typography>
                    </Grid>

                    <Grid item xs={4}>
                      <Typography>{report.reportState}</Typography>
                    </Grid>
                  </Grid>
                </AccordionSummary>

                <AccordionDetails>
                  <TableContainer component="main">
                    <Table aria-label="simple table">
                      <TableHead style={{ borderBottom: "1.5px solid gray", backgroundColor: "#fcefef" }}>
                        <TableRow>
                          <CenterTableCell>신고일시</CenterTableCell>
                          <CenterTableCell>신고한 유저</CenterTableCell>
                          <CenterTableCell>신고 내용</CenterTableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {report.reportDetailList.map((reportDetail) => (
                          <TableRow key={reportDetail.reportDetailCode} sx={{ backgroundColor: "#f2f2f2" }}>
                            <CenterTableCell>{reportDetail.reportDetailDate}</CenterTableCell>
                            <CenterTableCell>{reportDetail.memberEmail.memberEmail}</CenterTableCell>
                            <CenterTableCell>{reportDetail.reportDetailContent}</CenterTableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                  <Grid container sx={{ mt: 1 }}>
                    <Grid item xs={6}>
                      <Button variant="contained" sx={{ mt: 1, height: 40 }} onClick={() => deleteReport(report.reportCode)}>
                        신고내역 삭제
                      </Button>
                    </Grid>
                    <Grid item xs={6}>
                      {report.reportState === "처분 대기" ? (
                        //신고 대상이 처분 대기 상태일때, 신고 대상이 게시글일 경우 -> 신고 대상 삭제 버튼 출력
                        report.reportCategory === "share" || report.reportCategory === "talk" ? (
                          <Button
                            variant="contained"
                            sx={{ mt: 1, height: 40 }}
                            onClick={() =>
                              deleteReportTarget(report.reportTarget, report.reportCategory, report.memberEmail.memberEmail, report.reportCode)
                            }
                          >
                            신고 대상 삭제
                          </Button>
                        ) : (
                          //신고 대상이 처분 대기 상태일때, 신고 대상이 회원일 경우 -> 로그인 제한일 설정 창 출력
                          <div>
                            <FormControl sx={{ m: 1, minWidth: 120 }} size="small">
                              <InputLabel id="demo-select-small">이용 제한 일</InputLabel>
                              <Select
                                name={report.reportCode + ""}
                                defaultValue={0}
                                label="restrictDate"
                                onChange={(e) => onSelectDate(e)}
                                size="small"
                              >
                                <MenuItem value={0}> 선택</MenuItem>
                                <MenuItem value={2}>1일</MenuItem>
                                <MenuItem value={8}>7일</MenuItem>
                                <MenuItem value={31}>30일</MenuItem>
                                <MenuItem value={91}>90일</MenuItem>
                                <MenuItem value={181}>180일</MenuItem>
                                <MenuItem value={361}>360일</MenuItem>
                              </Select>
                            </FormControl>
                            <Button
                              variant="contained"
                              sx={{ mt: 1, height: 40 }}
                              onClick={() =>
                                onRistrict(restrictDateMap.get(report.reportCode + ""), report.memberEmail.memberEmail, report.reportCode)
                              }
                            >
                              제한하기
                            </Button>
                          </div>
                        )
                      ) : (
                        // 이미 제재가 가해진 신고대상일 경우, 출력할 문장
                        <Typography>이미 처리된 신고 내역입니다.</Typography>
                      )}
                    </Grid>
                  </Grid>
                </AccordionDetails>
              </Accordion>
              <Divider variant="middle" component={"li"} style={{ listStyle: "none" }} />
            </div>
          ))
        )}
      </Box>
      <Stack spacing={2} alignItems="center" mt={3}>
        <Pagination count={totalPage} onChange={(e, value) => handlepageNum(value)} color="primary" page={pageNum} />
      </Stack>
    </Container>
  );
}
