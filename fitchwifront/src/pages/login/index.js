import React from "react";
import Swal from "sweetalert2";
import Login from "./components/Login";

export default function index({ sucLogin }) {
  //sweet alert 라이브러리를 사용한 alert 정의.
  const swAlert = (html, icon = "success", func) => {
    Swal.fire({
      title: "알림",
      html: html,
      icon: icon,
      confirmButtonText: "확인",
      confirmButtonColor: "#ff0456",
    }).then(func);
  };

  return <Login sucLogin={sucLogin} swAlert={swAlert} />;
}
