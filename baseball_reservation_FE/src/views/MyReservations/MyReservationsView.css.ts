import { globalStyle, keyframes } from "@vanilla-extract/css";

const spin = keyframes({
  from: { transform: "rotate(0deg)" },
  to: { transform: "rotate(360deg)" },
});

const fadeIn = keyframes({
  from: { opacity: 0, transform: "translateY(-8px)" },
  to: { opacity: 1, transform: "translateY(0)" },
});

globalStyle(".my-reservations-page", {
  maxWidth: "800px",
  margin: "0 auto",
  padding: "32px 20px",
});

globalStyle(".my-reservations-page .page-header h1", {
  fontSize: "1.8rem",
  fontWeight: 700,
  color: "#1565c0",
  marginBottom: "24px",
});

globalStyle(".my-reservations-page .reservation-list", {
  display: "flex",
  flexDirection: "column",
  gap: "16px",
});

globalStyle(".my-reservations-page .reservation-card", {
  background: "#fff",
  borderRadius: "12px",
  padding: "20px 24px",
  boxShadow: "0 2px 10px rgba(0, 0, 0, 0.07)",
});

globalStyle(".my-reservations-page .card-top", {
  display: "flex",
  justifyContent: "space-between",
  alignItems: "flex-start",
  marginBottom: "14px",
});

globalStyle(".my-reservations-page .match-title", {
  fontSize: "1.05rem",
  fontWeight: 700,
  marginBottom: "6px",
});

globalStyle(".my-reservations-page .meta", {
  display: "flex",
  gap: "16px",
  fontSize: "0.85rem",
  color: "#666",
});

globalStyle(".my-reservations-page .res-status", {
  padding: "4px 12px",
  borderRadius: "20px",
  fontSize: "0.78rem",
  fontWeight: 700,
  whiteSpace: "nowrap",
});

globalStyle(".my-reservations-page .res-status.confirmed", {
  background: "#e8f5e9",
  color: "#2e7d32",
});

globalStyle(".my-reservations-page .res-status.cancelled", {
  background: "#f5f5f5",
  color: "#999",
});

globalStyle(".my-reservations-page .res-status.pending", {
  background: "#fff8e1",
  color: "#f57f17",
});

globalStyle(".my-reservations-page .spinner", {
  display: "inline-block",
  animation: `${spin} 1.5s linear infinite`,
  marginLeft: "4px",
});

globalStyle(".my-reservations-page .pending-msg", {
  fontSize: "0.82rem",
  color: "#f57f17",
});

globalStyle(".my-reservations-page .card-body", {
  borderTop: "1px solid #f0f0f0",
  paddingTop: "14px",
  marginBottom: "14px",
});

globalStyle(".my-reservations-page .seat-info", {
  display: "flex",
  gap: "24px",
  fontSize: "0.9rem",
  color: "#444",
  marginBottom: "8px",
});

globalStyle(".my-reservations-page .price", {
  color: "#c62828",
});

globalStyle(".my-reservations-page .created-at", {
  fontSize: "0.8rem",
  color: "#aaa",
});

globalStyle(".my-reservations-page .card-actions", {
  display: "flex",
  gap: "10px",
});

globalStyle(".my-reservations-page .ui-btn", {
  padding: "7px 18px",
  borderRadius: "6px",
  fontSize: "0.88rem",
  fontWeight: 600,
  cursor: "pointer",
  border: "none",
  transition: "opacity 0.15s",
});

globalStyle(".my-reservations-page .ui-btn:disabled", {
  opacity: 0.5,
  cursor: "not-allowed",
});

globalStyle(".my-reservations-page .ui-btn.ui-btn-reservation-cancel", {
  background: "#fff3e0",
  color: "#e65100",
  border: "1px solid #ffcc80",
});

globalStyle(
  ".my-reservations-page .ui-btn.ui-btn-reservation-cancel:hover:not(:disabled)",
  {
    background: "#ffe0b2",
  },
);

globalStyle(".my-reservations-page .ui-btn.ui-btn-delete", {
  background: "#ffebee",
  color: "#c62828",
  border: "1px solid #ef9a9a",
});

globalStyle(
  ".my-reservations-page .ui-btn.ui-btn-delete:hover:not(:disabled)",
  {
    background: "#ffcdd2",
  },
);

globalStyle(".my-reservations-page .ui-btn.ui-btn-pay", {
  background: "#1565c0",
  color: "#fff",
  border: "none",
  fontSize: "1rem",
  fontWeight: 700,
});

globalStyle(".my-reservations-page .ui-btn.ui-btn-pay:hover", {
  background: "#0d47a1",
});

globalStyle(".my-reservations-page .reservation-card.is-checked", {
  border: "2px solid #1565c0",
  boxShadow: "0 2px 12px rgba(21, 101, 192, 0.18)",
});

globalStyle(".my-reservations-page .card-top-left", {
  display: "flex",
  alignItems: "flex-start",
  gap: "12px",
});

globalStyle(".my-reservations-page .res-checkbox", {
  width: "18px",
  height: "18px",
  marginTop: "4px",
  accentColor: "#1565c0",
  cursor: "pointer",
  flexShrink: 0,
});

globalStyle(".my-reservations-page .pay-bar", {
  position: "fixed",
  bottom: 0,
  left: 0,
  right: 0,
  background: "#1565c0",
  color: "#fff",
  padding: "14px 28px",
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  boxShadow: "0 -4px 20px rgba(0, 0, 0, 0.15)",
  zIndex: 50,
});

globalStyle(".my-reservations-page .pay-bar-info", {
  fontSize: "1rem",
});

globalStyle(".my-reservations-page .pay-bar-info strong", {
  fontSize: "1.1rem",
});

globalStyle(".my-reservations-page .toast", {
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
