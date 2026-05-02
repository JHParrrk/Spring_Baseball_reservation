import { globalStyle } from "@vanilla-extract/css";
import { vars } from "./theme.css";

// 인라인 확인 UI (confirm/alert 대체)
globalStyle(".confirm-inline", {
  display: "inline-flex",
  alignItems: "center",
  gap: "6px",
  fontSize: "0.85rem",
  color: "#c62828",
});

globalStyle(".btn-xs-neutral", {
  padding: "3px 8px",
  fontSize: "0.78rem",
  border: "1px solid #ccc",
  borderRadius: "4px",
  background: "#fff",
  color: "#555",
  cursor: "pointer",
});

globalStyle(".btn-xs-neutral:hover", {
  background: "#f5f5f5",
});

// Buttons
globalStyle(".ui-btn-primary", {
  padding: "8px 20px",
  background: vars.color.primary,
  color: "#fff",
  border: "none",
  borderRadius: "6px",
  cursor: "pointer",
  fontSize: "0.9rem",
  fontWeight: 600,
  transition: "background 0.15s",
});

globalStyle(".ui-btn-primary:hover", {
  background: "#0d47a1",
});

globalStyle(".ui-btn-primary:disabled", {
  background: "#90caf9",
  cursor: "not-allowed",
});

globalStyle(".ui-btn-secondary", {
  padding: "8px 16px",
  background: vars.color.backgroundLight,
  color: vars.color.primary,
  border: `1px solid ${vars.color.primary}`,
  borderRadius: "6px",
  cursor: "pointer",
  fontSize: "0.9rem",
  transition: "all 0.15s",
});

globalStyle(".ui-btn-secondary:hover", {
  background: "#e3f2fd",
});

globalStyle(".ui-btn-danger-sm", {
  padding: "4px 10px",
  background: "#e53935",
  color: "#fff",
  border: "none",
  borderRadius: "4px",
  cursor: "pointer",
  fontSize: "0.8rem",
});

globalStyle(".ui-btn-danger-sm:hover", {
  background: "#c62828",
});

globalStyle(".ui-btn-cancel", {
  padding: "10px 20px",
  borderRadius: "8px",
  border: "1.5px solid #ddd",
  background: "#fff",
  color: "#555",
  fontSize: "0.95rem",
  cursor: "pointer",
});

globalStyle(".ui-btn-cancel:hover:not(:disabled)", {
  background: "#f5f5f5",
});

globalStyle(".ui-btn-confirm", {
  padding: "10px 24px",
  borderRadius: "8px",
  border: "none",
  background: vars.color.primary,
  color: "#fff",
  fontSize: "0.95rem",
  fontWeight: 600,
  cursor: "pointer",
  transition: "background 0.2s",
});

globalStyle(".ui-btn-confirm:hover:not(:disabled)", {
  background: "#0d47a1",
});

globalStyle(".ui-btn-confirm:disabled, .ui-btn-cancel:disabled", {
  opacity: 0.5,
  cursor: "not-allowed",
});

// Badges — 예약 상태
globalStyle(".ui-badge-confirmed", {
  background: "#e8f5e9",
  color: "#2e7d32",
  padding: "2px 8px",
  borderRadius: "10px",
  fontSize: "0.78rem",
  fontWeight: 600,
});

globalStyle(".ui-badge-pending", {
  background: "#fff8e1",
  color: "#f57f17",
  padding: "2px 8px",
  borderRadius: "10px",
  fontSize: "0.78rem",
  fontWeight: 600,
});

globalStyle(".ui-badge-cancelled", {
  background: "#fce4ec",
  color: "#c62828",
  padding: "2px 8px",
  borderRadius: "10px",
  fontSize: "0.78rem",
  fontWeight: 600,
});

// Badges — 사용자/역할 상태
globalStyle(".ui-badge-admin", {
  background: "#fce4ec",
  color: "#c62828",
  padding: "2px 8px",
  borderRadius: "10px",
  fontSize: "0.78rem",
  fontWeight: 700,
});

globalStyle(".ui-badge-user", {
  background: "#e3f2fd",
  color: vars.color.primary,
  padding: "2px 8px",
  borderRadius: "10px",
  fontSize: "0.78rem",
});

globalStyle(".ui-badge-active", {
  background: "#e8f5e9",
  color: "#2e7d32",
  padding: "2px 8px",
  borderRadius: "10px",
  fontSize: "0.78rem",
});

globalStyle(".ui-badge-inactive", {
  background: "#f5f5f5",
  color: "#757575",
  padding: "2px 8px",
  borderRadius: "10px",
  fontSize: "0.78rem",
});

globalStyle(".ui-badge-suspended", {
  background: "#fff8e1",
  color: "#f57f17",
  padding: "2px 8px",
  borderRadius: "10px",
  fontSize: "0.78rem",
});

globalStyle(".ui-badge-blacklisted", {
  background: "#212121",
  color: "#fff",
  padding: "2px 8px",
  borderRadius: "10px",
  fontSize: "0.78rem",
});

// Shared state colors
globalStyle(
  ".home-page .match-status.on_sale, .match-detail-page .match-status.on_sale",
  {
    background: "#e8f5e9",
    color: "#2e7d32",
  },
);

globalStyle(
  ".home-page .match-status.upcoming, .match-detail-page .match-status.upcoming",
  {
    background: "#e3f2fd",
    color: vars.color.primary,
  },
);

// Shared center message
globalStyle(
  ".home-page .center-msg, .match-detail-page .center-msg, .my-reservations-page .center-msg",
  {
    textAlign: "center",
    padding: "60px",
    color: "#888",
  },
);

globalStyle(
  ".home-page .center-msg.error, .match-detail-page .center-msg.error, .my-reservations-page .center-msg.error",
  {
    color: "#c62828",
  },
);

// Shared pagination for list pages
globalStyle(".home-page .pagination, .my-reservations-page .pagination", {
  display: "flex",
  justifyContent: "center",
  alignItems: "center",
  gap: "16px",
  marginTop: "32px",
});

globalStyle(
  ".home-page .pagination button, .my-reservations-page .pagination button",
  {
    padding: "8px 20px",
    background: vars.color.primary,
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
    fontSize: "0.9rem",
  },
);

globalStyle(
  ".home-page .pagination button:disabled, .my-reservations-page .pagination button:disabled",
  {
    background: "#bbb",
    cursor: "not-allowed",
  },
);

globalStyle(
  ".home-page .pagination span, .my-reservations-page .pagination span",
  {
    fontSize: "0.95rem",
    color: "#555",
  },
);

// Shared toast result styling
globalStyle(
  ".match-detail-page .toast.success, .my-reservations-page .toast.success",
  {
    background: "#e8f5e9",
    color: "#2e7d32",
    border: "1px solid #a5d6a7",
  },
);

globalStyle(
  ".match-detail-page .toast.error, .my-reservations-page .toast.error",
  {
    background: "#ffebee",
    color: "#c62828",
    border: "1px solid #ef9a9a",
  },
);
