import { globalStyle } from "@vanilla-extract/css";

globalStyle(".home-page", {
  maxWidth: "1100px",
  margin: "0 auto",
  padding: "32px 20px",
});

globalStyle(".home-page .page-header", {
  marginBottom: "28px",
});

globalStyle(".home-page .page-header h1", {
  fontSize: "1.8rem",
  fontWeight: 700,
  color: "#1565c0",
});

globalStyle(".home-page .page-header .sub", {
  color: "#666",
  marginTop: "6px",
});

globalStyle(".home-page .match-grid", {
  display: "grid",
  gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))",
  gap: "20px",
});

globalStyle(".home-page .match-card", {
  background: "#fff",
  borderRadius: "12px",
  padding: "24px",
  boxShadow: "0 2px 10px rgba(0, 0, 0, 0.07)",
  textDecoration: "none",
  color: "inherit",
  transition: "transform 0.15s, box-shadow 0.15s",
  display: "block",
});

globalStyle(".home-page .match-card:hover", {
  transform: "translateY(-3px)",
  boxShadow: "0 6px 20px rgba(0, 0, 0, 0.12)",
});

globalStyle(".home-page .match-status", {
  display: "inline-block",
  padding: "3px 10px",
  borderRadius: "20px",
  fontSize: "0.78rem",
  fontWeight: 700,
  marginBottom: "12px",
});

globalStyle(".home-page .match-title", {
  fontSize: "1.1rem",
  fontWeight: 700,
  marginBottom: "14px",
  lineHeight: 1.4,
});

globalStyle(".home-page .match-info", {
  display: "flex",
  flexDirection: "column",
  gap: "6px",
  fontSize: "0.88rem",
  color: "#555",
});
