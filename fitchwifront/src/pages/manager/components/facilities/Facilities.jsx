import {
  Button,
  ButtonGroup,
  CircularProgress,
  Container,
  Pagination,
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
import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import FacilitiesSearch from "./FacilitiesSearch";
export default function Facilities({ swAlert }) {
  //한 페이지에 로드될 시설 목록 저장
  const [facilities, setFacilities] = useState([]);
  //검색어 저장
  const [keyword, setKeyword] = useState("");
  //페이지 번호 저장
  const [pageNum, setPageNum] = useState(1);
  //전체 페이지 수 저장
  const [totalPage, setTotalPage] = useState(0);

  //세션 스토리지에 저장된 페이지 번호와 검색어를 가져와서 저장
  //-> 상세조회 또는 시설 수정 등의 페이지 이동후, 뒤로가기했을 때 세션스토리지에 저장된 페이지와 검색어를 기준으로 시설 목록을 다시 불러온다.
  let pageNumInSessionStg = sessionStorage.getItem("pageNum");
  let keywordInSessionStg = sessionStorage.getItem("keyword");

  //요청한 시설 목록을 가져왔는지 판별할 state - 로딩중일 시 스피너 출력 용도
  const [load, setLoad] = useState(false);

  //시설 목록 데이터 가져오기 - 페이지 번호와 검색어를 파라미터로 전달
  //-> 상세조회 또는 시설 수정 등의 페이지 이동후, 뒤로가기했을 때 세션스토리지에 저장된 페이지와 검색어를 기준으로 시설 목록을 다시 불러온다.
  const loadFacilities = (pageNumInSessionStg, keywordInSessionStg) => {
    //로딩 시작
    setLoad(false);
    axios
      .get("/getFacilitiesList", {
        params: { pageNum: pageNumInSessionStg, keyword: keywordInSessionStg },
      })
      .then((result) => {
        //결과의 목록과 전체페이지 수, 현재페이지 번호, 검색어를 state에 저장
        const { facilitiesList, totalPage, pageNum, keyword } = result.data;
        setTotalPage(totalPage);
        setPageNum(pageNum);
        setFacilities(facilitiesList);
        //로딩 완료
        setLoad(true);
        //현재 페이지번호와 검색어를 세션스토리지에 저장한다.
        sessionStorage.setItem("pageNum", pageNum);
        sessionStorage.setItem("keyword", keyword);
      });
  };

  useEffect(() => {
    pageNumInSessionStg !== null
      ? keywordInSessionStg !== null
        ? loadFacilities(pageNumInSessionStg, keywordInSessionStg) //세션 스토리지에 저장된 페이지번호와 검색어가 존재하면 해당 값을 패러미터로 전달하여 목록 조회
        : loadFacilities(pageNumInSessionStg, keyword) //검색어가 없을 경우, keyword의 기본값("")을 전달하여 조회
      : loadFacilities(pageNum, keyword); //페이지 번호와 검색어가 둘다 없을 경우, 기본값으로 조회(pageNum = 1, keyword = "")

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handlepageNum = (value) => {
    loadFacilities(value, keyword);
  };

  const BasicTableRow = styled(TableRow)({
    maxHeight: 68,
    ":hover": { backgroundColor: `#ffd2e2`, transition: `0.3s` },
  });

  //시설 삭제
  const deleteFacilities = (facilitiesCode) => {
    axios.delete(`/deleteFacilities/${facilitiesCode}`).then((result) => {
      //해당 시설에 진행예정인 함께해요가 존재할 경우, 삭제 불가.
      if (result.data === "togetherExist") {
        swAlert("해당 시설에서 진행 예정인<br/> 함께해요가 존재하여<br/> 삭제가 불가능합니다.", "warning");
        loadFacilities(pageNumInSessionStg, keywordInSessionStg);
      } else if (result.data === "ok") {
        swAlert("해당 시설의 정보가 정상적으로 삭제됐습니다.");
        loadFacilities(pageNumInSessionStg, keywordInSessionStg);
      } else {
        swAlert("해당 시설의 정보 삭제가 <br/> 정상적으로 완료되지 않았습니다.<br/>관리자에 문의해주세요.", "warning");
      }
    });
  };

  return (
    <Container component="main" align="center" sx={{ mt: 13 }}>
      <Box>
        <Typography variant="h4">시설 관리</Typography>
        <FacilitiesSearch keyword={keyword} setKeyword={setKeyword} pageNum={pageNum} loadFacilities={loadFacilities} />
        <Link to="/manager/facilities/insertFacilities" style={{ textDecoration: "none" }}>
          <Button variant="contained" style={{ float: "right" }}>
            시설 등록
          </Button>
        </Link>
      </Box>

      <TableContainer component="main" sx={{ width: "100%", height: 480 }}>
        <Table aria-label="simple table">
          <TableHead style={{ borderBottom: "2px solid black" }}>
            <TableRow>
              <TableCell align="center" width="20%">
                시설번호
              </TableCell>
              <TableCell align="center" width="20%">
                시설 명
              </TableCell>
              <TableCell align="center" width="20%">
                담당자명
              </TableCell>
              <TableCell align="center" width="20%">
                상태
              </TableCell>
              <TableCell align="center" width="20%">
                수정/삭제
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {/* 서버에 요청한 데이터를 받기 전까지 출력할 스피너. */}
            {load === false ? (
              <TableRow>
                <TableCell
                  style={{
                    position: "absolute",
                    left: "50%",
                    top: "50%",
                  }}
                >
                  <CircularProgress />
                </TableCell>
              </TableRow>
            ) : (
              facilities.map((facilities, index) => (
                <BasicTableRow key={index}>
                  <TableCell align="center">{facilities.facilitiesCode}</TableCell>

                  <TableCell align="center">
                    {/* 시설 이름 클릭 시, 해당 시설의 상세 페이지 이동 */}
                    <Link
                      to={`/manager/facilities/getFacilitiesInfo/${facilities.facilitiesCode}`}
                      style={{ textDecoration: "none", color: "black" }}
                    >
                      {facilities.facilitiesName}{" "}
                    </Link>
                  </TableCell>

                  <TableCell align="center">{facilities.facilitiesManager}</TableCell>
                  <TableCell align="center">{facilities.facilitiesGrade}</TableCell>
                  <TableCell align="center">
                    <ButtonGroup>
                      {/* 수정하기 페이지 이동 시, 시설 코드를 패러미터로 전달. */}
                      <Link to={`/manager/facilities/updateFacilities/${facilities.facilitiesCode}`} style={{ textDecoration: "none" }}>
                        <Button>수정하기</Button>
                      </Link>
                      {/* 삭제할 시설의 코드를 패러미터로 전달하여 시설 삭제 함수 실행 */}
                      <Button onClick={() => deleteFacilities(facilities.facilitiesCode)}>삭제하기</Button>
                    </ButtonGroup>
                  </TableCell>
                </BasicTableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Stack spacing={2} alignItems="center" mt={1}>
        <Pagination count={totalPage} onChange={(e, value) => handlepageNum(value)} color="primary" page={pageNum} />
      </Stack>
    </Container>
  );
}
