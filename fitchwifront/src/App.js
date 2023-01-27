import React, { useCallback, useEffect, useState } from "react";
import { Route, Routes, useNavigate } from "react-router-dom";
import Home from "./pages/home";
import Share from "./pages/share";
import LoginMember from "./pages/login";
import JoinMember from "./pages/join";
import Talk from "./pages/talk";
import Header from "./layout/Header";
import Together from "./pages/together";
import MemberPage from "./pages/memberpage";
import Search from "./pages/search";
import About from "./pages/about";
import Manager from "./pages/manager";
import "react-calendar/dist/Calendar.css"; // css import
import "./pages/manager/components/facilities/CalendarApp.scss";
import ChannelService from "./components/common/ChannelService";
import KaKaoLoginRedirect from "./pages/login/components/KaKaoLoginRedirect";
import axios from "axios";
import ManagerNav from "./pages/manager/components/ManagerNav";
import Swal from "sweetalert2";

function App() {
  const nav = useNavigate();

  //로그인 상태를 저장하는 state
  const [lstate, setLstate] = useState({
    logid: "",
    nickName: "",
    profileImg: "",
    mbti: "",
    flink: "/login",
  });

  //실행 후 최초 1회, 세션스토리지에 저장된 회원 정보를 가져와서 로그인 상태저장 state에 저장.
  useEffect(() => {
    const id = sessionStorage.getItem("id");
    const nickName = sessionStorage.getItem("nickName");
    const profileImg = sessionStorage.getItem("profileImg");
    const mbti = sessionStorage.getItem("mbti");
    ChannelService.boot({
      pluginKey: "fd5d508e-cd29-47c6-bef9-76dd87ce2f1a",
      memberId: id,
      profile: {
        name: nickName,
        email: id,
      },
    });

    if (id !== null) {
      const newState = {
        logid: id,
        nickName: nickName,
        profileImg: profileImg,
        mbti: mbti,
        flink: "/memberpage",
      };
      setLstate(newState);
    }
  }, []);

  //로그인 성공 시 로그인 상태 변경.
  const sucLogin = useCallback((id, nickName, profileImg, mbti) => {
    const newState = {
      logid: id,
      nickName: nickName,
      profileImg: profileImg,
      mbti: mbti,
      flink: "/memberpage",
    };
    setLstate(newState);
  }, []);

  //sweet alert2 라이브러리 사용한 기본 alert 정의.
  const swAlert = (html, icon = "success", func) => {
    Swal.fire({
      title: "알림",
      html: html,
      icon: icon,
      confirmButtonText: "확인",
      confirmButtonColor: "#ff0456",
    }).then(func);
  };
  //로그아웃
  const onLogout = () => {
    axios.post("/logout", { data: { id: lstate.logid } });

    /*
    //카카오 계정과 함께 로그아웃 시, 요청 URL.
    //본 프로젝트에서는 브라우저의 카카오 계정 로그인 상태는 남겨두는 방식의 로그아웃을 적용함.
    const REST_API_KEY = "bad1b060092a0ed86a3dfe34c2fb99f9";
    const REDIRECT_URI = "http://localhost:3000/";
    const KAKAO_AUTH_URL = `https://kauth.kakao.com/oauth/logout?client_id=${REST_API_KEY}&logout_redirect_uri=${REDIRECT_URI}`;
    window.location.href = KAKAO_AUTH_URL;
    */
    const newState = {
      logid: "",
      mbti: "",
      flink: "/login",
    };
    setLstate(newState);
    //로그아웃 시 로그인 상태 삭제
    sessionStorage.removeItem("id");
    sessionStorage.removeItem("nickName");
    sessionStorage.removeItem("mbti");
    sessionStorage.removeItem("classification");
    sessionStorage.removeItem("profileImg");
    swAlert("로그아웃이 완료됐습니다.", "success", () => {
      nav("/"); //로그아웃 이후 첫 페이지 이동.
    });
  };
  //관리자인지 판별할 state
  const [isManager, setIsManager] = useState(false);

  return (
    <>
      {/* 관리자 로그인일 경우, 관리자용 헤더 출력. */}
      {isManager === true ? <ManagerNav /> : <Header lstate={lstate} onLogout={onLogout} />}
      <Routes>
        <Route path="/*" element={<Home lstate={lstate} />}></Route>
        <Route path="/about" element={<About />}></Route>
        <Route path="/login" element={<LoginMember sucLogin={sucLogin} />}></Route>
        <Route path="/join/*" element={<JoinMember />}></Route>
        <Route path="/share/*" element={<Share />}></Route>
        <Route path="/talk/*" element={<Talk />}></Route>
        <Route path="/together/*" element={<Together />}></Route>
        <Route path="/search/*" element={<Search />}></Route>
        <Route
          path="/memberpage/*"
          element={<MemberPage onLogout={onLogout} lstate={lstate} sucLogin={sucLogin} />}
        ></Route>
        <Route
          path="/manager/*"
          element={<Manager isManager={isManager} setIsManager={setIsManager} />}
        ></Route>
        <Route path="/login/kakao/callback" element={<KaKaoLoginRedirect sucLogin={sucLogin} />}></Route>
      </Routes>
    </>
  );
}

export default App;
