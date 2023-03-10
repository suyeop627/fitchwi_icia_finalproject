import React, { useEffect, useState } from "react";

import { Avatar, Box, Button, FormControlLabel, TextField, Typography } from "@mui/material";
import { Link, useNavigate } from "react-router-dom";

export default function UserImg({ setFileForm, joinForm, isValid, swAlert }) {
  const [file, setFile] = useState("");
  const nav = useNavigate();
  useEffect(() => {
    if (isValid === false) {
      swAlert("비정상적인 접근입니다.<br/> 메인화면으로 이동합니다.", "warning", () => {
        nav("/");
      });
    }
  });

  //이미지 미리보기 및 파일 업로드
  const imageLoad = (event) => {
    if (event.target.files.length !== 0) {
      //등록된 이미지가 존재한다면, fileForm에 저장.
      setFileForm(event.target.files[0]);
      let reader = new FileReader();
      //onload : 이미지 읽기에 성공했을 실행되는 이벤트 핸들러
      reader.onload = (e) => {
        //읽기에 성공한 이미지를 file state에 저장 -> 미리보기할 이미지
        setFile(e.target.result);
      };
      //readAsDataURL : 이미지를 DataURL 형식으로 읽음
      reader.readAsDataURL(event.target.files[0]);
    } else {
      //이미지 등록을 취소했을 때, file과 fileForm state 초기화.
      setFile("");
      setFileForm("");
    }
  };
  return (
    <Box style={{ textAlign: "center" }}>
      <Typography variant="h5" gutterBottom>
        '<b>{joinForm.memberNickname}</b>'님이시군요!
      </Typography>
      <Typography variant="h4" gutterBottom>
        <b>{joinForm.memberNickname}</b>님을 표현할 수 있는 이미지를
        <br /> 선택해주세요.
      </Typography>
      <Typography variant="h6" mb={5}>
        ( *미등록시 기본 이미지로 등록됩니다.)
      </Typography>
      <Avatar src={file !== "" ? file : "/images/DefaultProfileImageSystemNameBasic.jpg"} sx={{ width: 250, height: 250, m: "auto", mb: 2 }} />
      <Button variant="outlined" sx={{ pl: 5 }}>
        <FormControlLabel
          control={
            <TextField
              onChange={imageLoad}
              type="file"
              label="사진"
              variant="standard"
              style={{ display: "none" }}
              inputProps={{ accept: "image/*" }}
            />
          }
          label="등록하기"
        />
      </Button>
      <br />{" "}
      <Link to="/join/name" style={{ textDecoration: "none" }}>
        <Button sx={{ mt: 5, width: 100 }} variant="contained" style={{ textDecoration: "none" }}>
          다음
        </Button>
      </Link>
    </Box>
  );
}
