import { globalStyle } from "@vanilla-extract/css";
import { vars } from "./theme.css";

globalStyle("*", {
  boxSizing: "border-box",
});

globalStyle("html, body, #app", {
  margin: 0,
  padding: 0,
  minHeight: "100%",
});

globalStyle("body", {
  fontFamily:
    'Pretendard, "Noto Sans KR", "Segoe UI", Roboto, Helvetica, Arial, sans-serif',
  backgroundColor: vars.color.background,
  color: vars.color.text,
  lineHeight: 1.5,
});

globalStyle("button, input, select, textarea", {
  font: "inherit",
});

globalStyle("a", {
  color: "inherit",
  textDecoration: "none",
});
