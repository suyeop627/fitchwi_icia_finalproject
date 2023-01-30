import * as React from "react";
import PropTypes from "prop-types";
import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import CssBaseline from "@mui/material/CssBaseline";
import Divider from "@mui/material/Divider";
import Drawer from "@mui/material/Drawer";
import IconButton from "@mui/material/IconButton";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import ListItemButton from "@mui/material/ListItemButton";
import ListItemText from "@mui/material/ListItemText";
import MenuIcon from "@mui/icons-material/Menu";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import { Link } from "react-router-dom";

const drawerWidth = 240;

function ManagerNav(props) {
  const { window } = props;
  const [mobileOpen, setMobileOpen] = React.useState(false);

  const handleDrawerToggle = () => {
    setMobileOpen((prevState) => !prevState);
  };

  const removeSessionItem = () => {
    sessionStorage.removeItem("pageNum");
    sessionStorage.removeItem("keyword");
  };

  //창의 크기가 작아질 경우 출력할 메뉴(햄버거 버튼 클릭시 사이드 메뉴 출력)
  const drawer = (
    <Box onClick={handleDrawerToggle} sx={{ textAlign: "center" }}>
      <Typography variant="h6">
        FITCHWI&nbsp;<sub style={{ fontSize: "14px" }}>Manager</sub>
      </Typography>
      <Divider />
      <List style={{ textDecoration: "none", textAlign: "center" }}>
        <ListItem sx={{ justifyContent: "space-around" }}>
          <Link to="/manager/facilities" style={{ textDecoration: "none", color: "black" }}>
            <ListItemButton onClick={() => removeSessionItem()}>
              <ListItemText primary="시설관리" />
            </ListItemButton>
          </Link>
        </ListItem>
        <ListItem sx={{ justifyContent: "space-around" }}>
          <Link to="/manager/report" style={{ textDecoration: "none", color: "black" }}>
            <ListItemButton sx={{ textAlign: "center" }} onClick={() => removeSessionItem()}>
              <ListItemText primary="신고관리" />
            </ListItemButton>
          </Link>
        </ListItem>
        <ListItem sx={{ justifyContent: "space-around" }}>
          <Link to="/manager/togetherManagement" style={{ textDecoration: "none", color: "black" }}>
            <ListItemButton sx={{ textAlign: "center" }} onClick={() => removeSessionItem()}>
              <ListItemText primary="  함께해요 취소 관리" />
            </ListItemButton>
          </Link>
        </ListItem>
      </List>
    </Box>
  );

  const container = window !== undefined ? () => window().document.body : undefined;

  return (
    <Box sx={{ display: "flex", mb: 10 }}>
      <CssBaseline />
      <AppBar component="nav">
        <Toolbar>
          <IconButton color="inherit" aria-label="open drawer" edge="start" onClick={handleDrawerToggle} sx={{ mr: 2, display: { sm: "none" } }}>
            <MenuIcon />
          </IconButton>
          {/* 크기가 xs일 경우, display :none 처리.  */}
          <Typography variant="h5" component="div" sx={{ display: { xs: "none", sm: "block" }, color: "white" }}>
            FITCHWI&nbsp;<sub style={{ fontSize: "14px" }}>Manager</sub>
          </Typography>
          <Box
            sx={{
              flexGrow: 0.95,
              display: { xs: "none", sm: "block" },
            }}
          >
            {/* 크기가 xs일 경우, display :none 처리.  */}
            <Box style={{ display: "flex", justifyContent: "space-evenly" }}>
              <Link to="/manager/facilities" style={{ textDecoration: "none", color: "white" }}>
                <Typography onClick={() => removeSessionItem()}>시설관리</Typography>
              </Link>
              <Link to="/manager/report" style={{ textDecoration: "none", color: "white" }}>
                <Typography onClick={() => removeSessionItem()}> 신고관리 </Typography>
              </Link>
              <Link to="/manager/togetherManagement" style={{ textDecoration: "none", color: "white" }}>
                <Typography onClick={() => removeSessionItem()}> 함께해요 취소 관리 </Typography>
              </Link>
            </Box>
          </Box>
        </Toolbar>
      </AppBar>
      <Box component="nav">
        {/* 크기가 xs일 경우에만 출력  - 창 크기가 작아지면 Drawer로 메뉴 출력*/}
        <Drawer
          container={container}
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true, // Better open performance on mobile.
          }}
          sx={{
            display: { xs: "block", sm: "none" },
            "& .MuiDrawer-paper": { boxSizing: "border-box", width: drawerWidth },
          }}
        >
          {drawer}
        </Drawer>
      </Box>
    </Box>
  );
}

ManagerNav.propTypes = {
  window: PropTypes.func,
};

export default ManagerNav;
