import { Box } from "@mui/material";
import React from "react";
import { Route, Routes } from "react-router-dom";
import Swal from "sweetalert2";
import CheckManagerModal from "./components/CheckManagerModal";
import AddFacilities from "./components/facilities/AddFacilities";
import EditFacilities from "./components/facilities/EditFacilities";
import Facilities from "./components/facilities/Facilities";
import ViewFacilities from "./components/facilities/ViewFacilities";
import Manager from "./components/Manager";
import ReportManagement from "./components/report/ReportManagement";
import TogeterManagement from "./components/togethermanagement/TogetherManagement";

export default function ManagerIndex({ setIsManager, isManager }) {
  // const [isManager, setIsManager] = useState(false);
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
    <Box>
      {isManager === false ? (
        // 관리자로 로그인이 되지 않은경우엔, 로그인 모달 출력.
        <CheckManagerModal setIsManager={setIsManager} swAlert={swAlert} />
      ) : (
        // 로그인이 완료된 경우, 아래의 route로 접근이 가능하다.
        <div>
          <Routes>
            <Route path="/" element={<Manager swAlert={swAlert} />} />
            <Route path="/facilities" element={<Facilities swAlert={swAlert} />} />
            <Route path="/facilities/insertFacilities" element={<AddFacilities swAlert={swAlert} />} />
            <Route path="/facilities/updateFacilities/:facilitiesCode" element={<EditFacilities swAlert={swAlert} />} />
            <Route path="/facilities/getFacilitiesInfo/:facilitiesCode" element={<ViewFacilities swAlert={swAlert} />} />
            <Route path="/report" element={<ReportManagement swAlert={swAlert} />} />
            <Route path="/togetherManagement" element={<TogeterManagement swAlert={swAlert} />} />
          </Routes>
        </div>
      )}
    </Box>
  );
}
