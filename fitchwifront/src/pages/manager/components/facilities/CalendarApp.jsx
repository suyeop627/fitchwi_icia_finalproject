import { Box, Button, Grid, TextField } from "@mui/material";
import axios from "axios";
import moment from "moment/moment";
import React, { useEffect, useState } from "react";
import Calendar from "react-calendar";
import "./CalendarApp.scss";
export default function CalendarApp({ facilitiesCode, swAlert }) {
  //이용 불가 일정의 시작일 저장
  const [startDate, setStartDate] = useState("");
  //이용 불가 일정의 종료일 저장
  const [endDate, setEndDate] = useState("");
  //기존 이용 불가일 목록 저장
  const [noDayList, setNodayList] = useState([]);

  //react calendar에서 날짜 선택 시, 시작일과 종료일 저장
  const onChangeDate = (e) => {
    const startDateFormat = moment(e[0]).format("YYYY-MM-DD");
    const endDateFormat = moment(e[1]).format("YYYY-MM-DD");
    setStartDate(startDateFormat);
    setEndDate(endDateFormat);
  };
  //추가 또는 삭제할 날짜 범위 저장
  const [noDayToSend, setNoDayToSend] = useState([]);

  //시설의 전체 이용불가일 목록 조회 요청
  useEffect(() => {
    axios.get("/getNodayList", { params: { facilitiesCode: facilitiesCode } }).then((result) => {
      setNodayList(result.data);
    });

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [noDayToSend]);

  //이용 불가일 추가
  const addNoday = () => {
    //등록할 이용 불가일정을 모두 선택하지 않고, 등록했을 경우
    if (startDate === "" || endDate === "") {
      swAlert("시작일과 종료일을 모두 선택해주세요.", "warning");
      return;
    }

    //등록하려는 날짜 범위가 기존에 등록되어 있는 날짜인지 판별.
    //=> indexOf() : 배열에서 지정된 요소를 찾을 수 잇는 첫번째 인덱스 반환, 존재하지 않으면 -1 반환.
    let checkArray = noDayList.filter((n) => noDayToSend.indexOf(n) > 0);
    //기존에 등록된 날짜를 포함한 경우, 날짜 초기화 후 함수 종료.
    if (checkArray.length !== 0) {
      swAlert("이미 등록된 날짜를 포함하고 있습니다.", "warning");
      setStartDate("");
      setEndDate("");
      setNoDayToSend([]);
      return;
    }
    //이용 불가일 추가 요청
    axios
      .get("/addNodayList", {
        params: { noDayToSend: encodeURI(noDayToSend), facilitiesCode: facilitiesCode },
      })
      .then((res) => {
        if (res.data === "ok") {
          setNodayList([]);
          swAlert("이용 불가능일 성공적으로 저장됐습니다.");
          setStartDate("");
          setEndDate("");
          setNoDayToSend([]);
        }
      })
      .catch((error) => swAlert(`이용불가일 등록에 실패했습니다. : ${error}`, "warning"));
  };

  //이용 불가일 삭제 요청
  const deleteNoday = () => {
    //삭제할 이용 불가일정을 모두 선택하지 않고, 등록했을 경우
    if (startDate === "" || endDate === "") {
      swAlert("시작일과 종료일을 모두 선택해주세요.", "warning");
      return;
    }

    //삭제하려는 날짜 범위에 이용불가일로 지정되지 않은 날짜가 포함되어있는지 판별.
    let checkArray = noDayToSend.filter((n) => noDayList.indexOf(n) < 0);
    if (checkArray.length !== 0) {
      swAlert("이용 가능한 날짜를 포함하고 있습니다.", "warning");
      setStartDate("");
      setEndDate("");
      setNoDayToSend([]);
      return;
    }
    axios
      .delete("/deleteNodayList", {
        params: { noDayToSend: encodeURI(noDayToSend), facilitiesCode: facilitiesCode },
      })
      .then((res) => {
        if (res.data === "ok") {
          setNodayList([]);
          swAlert("해당 이용 불가 일정이<br/>성공적으로 삭제됐습니다.");
          setStartDate("");
          setEndDate("");
          setNoDayToSend([]);
        }
        //해당 이용불가일에 진행예정인 함께해요 존재시, 삭제 불가
        else if (res.data === "togetherExist") {
          setNodayList([]);
          swAlert("해당 날짜에 진행 예정인 '함께해요'가 존재하여<br/> 삭제가 불가합니다.", "warning");
          setStartDate("");
          setEndDate("");
          setNoDayToSend([]);
        } else {
          swAlert("해당 이용 불가 일정을 삭제하는 데 실패했습니다.", "warning");
        }
      });
  };

  //날짜범위 시작과 끝을 기준으로, 사이 날짜 계산 함수
  const getGapOfDates = (a, b) => {
    //시작일 밀리초
    let startDate = new Date(a).getTime();
    //종료일 밀리초
    let endDate = new Date(b).getTime();
    //1일의 밀리초
    let aDay = 1000 * 60 * 60 * 24;
    //시작일과 종료일 사이에 존재하는 날짜 수 저장
    let range = (endDate - startDate + 1) / aDay;

    let daysArray = [];

    //반환할 배열에 시작일부터, 시작일에 하루씩 더한 값을 날짜 수 만큼 저장.
    for (let i = 0; i < range; i++) {
      daysArray.push(moment(startDate).format("YYYY-MM-DD"));
      startDate += aDay;
    }
    return daysArray;
  };

  //시작일과 종료일이 변경될때마다, 날짜 사이값 계산 후 nodayToSend state에 저장
  useEffect(() => {
    const dates = getGapOfDates(startDate, endDate);

    setNoDayToSend(() => dates);
  }, [startDate, endDate]);

  //날짜 클릭 시 startDate, endDate에 값 저장
  const onClickDay = (v) => {
    //둘다 값이 있을 경우, startDate에 새로 저장, endDate를 ""로 초기화
    if (startDate !== "" && endDate !== "") {
      setStartDate(v);
      setEndDate("");
      return;
    }
    //startDate에 값이 없을 경우, startDate에 저장
    if (startDate === "") {
      setStartDate(v);
    }
  };
  return (
    <div>
      <Calendar
        onChange={onChangeDate}
        selectRange={true}
        className=" CalendrApp"
        formatDay={(locale, date) => moment(date).format("DD")}
        // 날짜 값 중, 이용불가일정리스트(noDayList)에 저장된 값과 일치하는 경우, 해당 날짜 칸에 '❌' 기호를 출력.
        tileContent={({ date, view }) => {
          if (noDayList.find((x) => x === moment(date).format("YYYY-MM-DD"))) {
            return "❌";
          }
        }}
        onClickDay={(v) => onClickDay(moment(v).format("YYYY-MM-DD"))}
      />
      <Grid container mt={3}>
        <Grid item xs={4}>
          <TextField label="시작일" value={startDate || ""} size="small" />
        </Grid>
        <Grid item xs={4}>
          <TextField label="종료일" value={endDate || ""} size="small" />
        </Grid>

        <Grid item xs={4}>
          <Box textAlign="center">
            <Button onClick={addNoday} variant="outlined" color="info">
              등록
            </Button>
            <Button onClick={deleteNoday} variant="outlined">
              삭제
            </Button>
          </Box>
        </Grid>
      </Grid>
    </div>
  );
}
