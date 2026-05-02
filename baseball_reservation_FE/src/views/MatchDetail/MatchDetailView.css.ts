import { globalStyle, keyframes } from "@vanilla-extract/css";

const fadeIn = keyframes({
  from: { opacity: 0, transform: "translateY(-8px)" },
  to: { opacity: 1, transform: "translateY(0)" },
});

globalStyle(".match-detail-page", {
  maxWidth: "900px",
  margin: "0 auto",
  padding: "32px 20px",
});

globalStyle(".match-detail-page .back-link", {
  display: "inline-block",
  color: "#1565c0",
  textDecoration: "none",
  fontSize: "0.9rem",
  marginBottom: "24px",
});

globalStyle(".match-detail-page .back-link:hover", {
  textDecoration: "underline",
});

globalStyle(".match-detail-page .match-header", {
  background: "#fff",
  borderRadius: "12px",
  padding: "24px",
  boxShadow: "0 2px 10px rgba(0, 0, 0, 0.07)",
  marginBottom: "24px",
});

globalStyle(".match-detail-page .match-status", {
  display: "inline-block",
  padding: "3px 10px",
  borderRadius: "20px",
  fontSize: "0.78rem",
  fontWeight: 700,
  marginBottom: "10px",
});

globalStyle(".match-detail-page .match-header h1", {
  fontSize: "1.5rem",
  fontWeight: 700,
  marginBottom: "12px",
});

globalStyle(".match-detail-page .match-meta", {
  display: "flex",
  gap: "20px",
  fontSize: "0.9rem",
  color: "#555",
});

globalStyle(".match-detail-page .booking-panel", {
  position: "fixed",
  bottom: 0,
  left: 0,
  right: 0,
  background: "#fff",
  borderTop: "2px solid #1565c0",
  padding: "16px 24px",
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  boxShadow: "0 -4px 20px rgba(0, 0, 0, 0.1)",
  zIndex: 50,
});

globalStyle(".match-detail-page .booking-info", {
  display: "flex",
  alignItems: "center",
  gap: "16px",
  fontSize: "0.95rem",
});

globalStyle(".match-detail-page .booking-info strong", {
  fontWeight: 700,
  color: "#1565c0",
  whiteSpace: "nowrap",
});

globalStyle(".match-detail-page .seat-list", {
  fontSize: "0.85rem",
  color: "#555",
  maxWidth: "350px",
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
});

globalStyle(".match-detail-page .price", {
  fontSize: "1.1rem",
  fontWeight: 700,
  color: "#c62828",
});

globalStyle(".match-detail-page .reserve-btn", {
  background: "#1565c0",
  color: "#fff",
  border: "none",
  padding: "12px 28px",
  borderRadius: "8px",
  fontSize: "1rem",
  fontWeight: 700,
  cursor: "pointer",
  transition: "background 0.15s",
});

globalStyle(".match-detail-page .reserve-btn:hover", {
  background: "#0d47a1",
});

globalStyle(".match-detail-page .reserve-btn:disabled", {
  background: "#90caf9",
  cursor: "not-allowed",
});

globalStyle(".match-detail-page .toast", {
  position: "fixed",
  top: "80px",
  right: "20px",
  padding: "12px 20px",
  borderRadius: "8px",
  fontSize: "0.95rem",
  fontWeight: 600,
  zIndex: 200,
  animation: `${fadeIn} 0.2s ease`,
});

globalStyle(".match-detail-page .toast.error", {
  display: "flex",
  alignItems: "center",
  gap: "12px",
});

globalStyle(".match-detail-page .toast-link", {
  whiteSpace: "nowrap",
  fontWeight: 700,
  color: "#c62828",
  textDecoration: "underline",
});
