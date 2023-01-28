import { Box } from "@mui/material";
import React, { useCallback, useEffect, useState } from "react";
import Name from "./components/Name";
import Nickname from "./components/Nickname";
import UserImg from "./components/UserImg";
import Gender from "./components/Gender";
import Birth from "./components/Birth";
import Interest from "./components/Interest";
import Mbti from "./components/Mbti";
import UserInfo from "./components/UserInfo";
import { Route, Routes, useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import Swal from "sweetalert2";

const JoinIndex = () => {
  let formData = new FormData();
  const nav = useNavigate();
  const location = useLocation();
  //업로드한 프로필 사진 저장
  const [fileForm, setFileForm] = useState("");
  //카카오 회원가입인지
  const [isKakao, setIsKakao] = useState(false);

  const [joinForm, setJoinForm] = useState({
    memberEmail: "",
    memberPwd: null,
    memberName: "",
    memberNickname: "",
    memberGender: "",
    memberPhone: "",
    memberAddr: "",
    memberBirth: "",
    memberMbti: "",
    memberInterest: [],
    memberImg: "",
    memberSaveimg: "",
  });

  //정상적인 회원가입 페이지 접속인지 판단하기위한 state
  const [isValid, setIsValid] = useState(false);

  useEffect(() => {
    //정상적인 회원가입 요청. location.state가 null일 경우, isValid는 false로 유지.
    if (location.state != null) {
      setIsValid(true);
      //일반 회원가입이 아닐 경우,
      if (location.state.member !== "newMember") {
        setIsKakao(true);
        //카카오 계정 로그인 시, 카카오 서버에서 받은 유저 데이터를 회원가입용 객체에 저장
        const joinFormObj = {
          ...joinForm,
          memberEmail: location.state.memberEmail,
          memberNickname: location.state.memberNickname,
          memberImg: location.state.memberImg,
          memberSaveimg: location.state.memberSaveimg,
        };
        setJoinForm(joinFormObj);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.state]);

  //입력값을 회원가입 용 객체에 저장
  const onChange = useCallback(
    (e) => {
      const joinObj = {
        ...joinForm,
        [e.target.name]: e.target.value,
      };
      setJoinForm(joinObj);
    },
    [joinForm]
  );

  //유저 데이터를 서버에 전송
  const sendJoin = (e) => {
    e.preventDefault();
    //유저 데이터를 formdata에 추가
    formData.append("data", new Blob([JSON.stringify(joinForm)], { type: "application/json" }));
    //유저 프로필 사진을 formdata에 추가
    formData.append("uploadImage", fileForm);

    const config = {
      headers: { "Content-Type": "multipart/form-data" },
    };

    axios
      .post("/joinmember", formData, config)
      .then((res) => {
        if (res.data === "ok") {
          swAlert("회원가입이 완료됐습니다.<br/> 로그인 페이지로 이동합니다.", "success", () => {
            nav("/login", { replace: true });
            setIsValid(false);
          });
        } else {
          swAlert("회원 가입 처리과정에 문제가 발생했습니다.", "warning");
        }
      })
      .catch((error) => console.log(error));
  };

  const swAlert = (html, icon = "success", func) => {
    Swal.fire({
      title: "알림",
      html: html,
      icon: icon,
      confirmButtonText: "확인",
      confirmButtonColor: "#ff0456",
    }).then(func);
  };

  return (
    <Box
      display="flex"
      justifyContent="center"
      alignItems="center"
      minHeight="100vh"
      component="form"
      onSubmit={sendJoin}
      //onSubmit={sendTest}
    >
      <Routes>
        <Route
          path="/"
          element={<Nickname onChange={onChange} joinForm={joinForm} swAlert={swAlert} isValid={isValid} location={location} />}
        ></Route>

        <Route path="/userimg" element={<UserImg joinForm={joinForm} setFileForm={setFileForm} swAlert={swAlert} isValid={isValid} />}></Route>
        <Route
          path="/name"
          element={<Name location={location} onChange={onChange} joinForm={joinForm} swAlert={swAlert} isKakao={isKakao} isValid={isValid} />}
        ></Route>
        <Route path="/gender" element={<Gender joinForm={joinForm} setJoinForm={setJoinForm} swAlert={swAlert} isValid={isValid} />}></Route>
        <Route path="/birth" element={<Birth onChange={onChange} joinForm={joinForm} swAlert={swAlert} isValid={isValid} />}></Route>
        <Route path="/interest" element={<Interest joinForm={joinForm} setJoinForm={setJoinForm} swAlert={swAlert} isValid={isValid} />}></Route>
        <Route path="/mbti" element={<Mbti joinForm={joinForm} setJoinForm={setJoinForm} swAlert={swAlert} isValid={isValid} />}></Route>
        <Route
          path="/userinfo"
          element={
            <UserInfo joinForm={joinForm} onChange={onChange} setJoinForm={setJoinForm} isKakao={isKakao} swAlert={swAlert} isValid={isValid} />
          }
        ></Route>
      </Routes>
    </Box>
  );
};

export default JoinIndex;
