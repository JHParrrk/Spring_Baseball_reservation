import { globalStyle } from "@vanilla-extract/css";
import { vars } from "@/styles/theme.css";

globalStyle(".admin-page", {
  maxWidth: "1100px",
  margin: "80px auto 40px",
  padding: "0 20px",
});

globalStyle(".admin-page .stadium-name-label", {
  flex: "0 0 auto",
});

globalStyle(".admin-page .stadium-name-input", {
  width: "180px",
});

globalStyle(".admin-page .page-title", {
  fontSize: "1.6rem",
  fontWeight: 700,
  color: vars.color.primary,
  marginBottom: "24px",
});

globalStyle(".admin-page .tabs", {
  display: "flex",
  gap: "8px",
  borderBottom: "2px solid #e3f2fd",
  marginBottom: "24px",
});

globalStyle(".admin-page .tab-btn", {
  padding: "10px 24px",
  border: "none",
  background: "none",
  fontSize: "0.95rem",
  cursor: "pointer",
  color: "#666",
  borderBottom: "2px solid transparent",
  marginBottom: "-2px",
  borderRadius: "4px 4px 0 0",
  transition: "all 0.15s",
});

globalStyle(".admin-page .tab-btn:hover", {
  color: vars.color.primary,
  background: "#e3f2fd",
});

globalStyle(".admin-page .tab-btn.active", {
  color: vars.color.primary,
  fontWeight: 700,
  borderBottomColor: vars.color.primary,
});

globalStyle(".admin-page .card", {
  background: vars.color.backgroundLight,
  border: "1px solid #e0e0e0",
  borderRadius: "10px",
  padding: "24px",
  marginBottom: "20px",
  boxShadow: "0 1px 4px rgba(0, 0, 0, 0.06)",
});

globalStyle(".admin-page .card h2", {
  fontSize: "1.05rem",
  fontWeight: 700,
  color: "#333",
  margin: "0 0 16px",
  paddingBottom: "10px",
  borderBottom: "1px solid #f0f0f0",
});

globalStyle(".admin-page .form-grid", {
  display: "grid",
  gridTemplateColumns: "1fr 1fr 1fr auto",
  gap: "12px",
  alignItems: "end",
});

globalStyle(".admin-page .form-grid label", {
  display: "flex",
  flexDirection: "column",
  gap: "4px",
  fontSize: "0.85rem",
  color: "#555",
});

globalStyle(".admin-page .form-grid input", {
  padding: "8px 10px",
  border: "1px solid #ccc",
  borderRadius: "6px",
  fontSize: "0.9rem",
});

globalStyle(".admin-page .form-grid select", {
  padding: "8px 10px",
  border: "1px solid #ccc",
  borderRadius: "6px",
  fontSize: "0.9rem",
  background: vars.color.backgroundLight,
});

globalStyle(".admin-page .auto-gen-grid", {
  display: "grid",
  gridTemplateColumns: "repeat(5, minmax(120px, 1fr))",
  gap: "8px",
});

globalStyle(".admin-page .auto-gen-grid label", {
  display: "flex",
  flexDirection: "column",
  gap: "4px",
  fontSize: "0.8rem",
  color: "#555",
});

globalStyle(".admin-page .auto-gen-grid input", {
  padding: "7px 8px",
  border: "1px solid #ccc",
  borderRadius: "6px",
  fontSize: "0.86rem",
});

globalStyle(".admin-page .template-preview", {
  marginTop: "12px",
  padding: "12px",
  border: "1px solid #e3f2fd",
  borderRadius: "8px",
  background: "#fafdff",
});

globalStyle(".admin-page .template-preview-head", {
  display: "flex",
  gap: "10px",
  alignItems: "baseline",
  marginBottom: "12px",
});

globalStyle(".admin-page .seat-blocks-list", {
  display: "flex",
  flexDirection: "column",
  gap: "8px",
});

globalStyle(".admin-page .seat-block", {
  padding: "10px 12px",
  background: vars.color.backgroundLight,
  border: "1px solid #90caf9",
  borderRadius: "6px",
  transition: "background 0.15s",
});

globalStyle(".admin-page .seat-block:hover", {
  background: "#f5faff",
});

globalStyle(".admin-page .block-info", {
  margin: 0,
  fontSize: "0.85rem",
  color: "#666",
});

globalStyle(".admin-page .row-form", {
  display: "flex",
  gap: "10px",
  alignItems: "center",
  flexWrap: "wrap",
});

globalStyle(".admin-page .row-form input, .admin-page .row-form select", {
  padding: "8px 10px",
  border: "1px solid #ccc",
  borderRadius: "6px",
  fontSize: "0.9rem",
});

globalStyle(".admin-page .filter-row", {
  display: "flex",
  gap: "10px",
  alignItems: "center",
  flexWrap: "wrap",
  marginBottom: "16px",
});

globalStyle(".admin-page .filter-row input, .admin-page .filter-row select", {
  padding: "8px 10px",
  border: "1px solid #ccc",
  borderRadius: "6px",
  fontSize: "0.9rem",
});

globalStyle(".admin-page .data-table", {
  width: "100%",
  borderCollapse: "collapse",
  fontSize: "0.88rem",
  marginBottom: "12px",
});

globalStyle(".admin-page .data-table th", {
  background: "#f5f5f5",
  padding: "10px 8px",
  textAlign: "left",
  color: "#555",
  fontWeight: 600,
  borderBottom: "2px solid #e0e0e0",
});

globalStyle(".admin-page .data-table td", {
  padding: "9px 8px",
  borderBottom: "1px solid #f0f0f0",
  verticalAlign: "middle",
});

globalStyle(".admin-page .data-table td input", {
  width: "100%",
  padding: "5px 8px",
  border: "1px solid #ddd",
  borderRadius: "4px",
  fontSize: "0.85rem",
});

globalStyle(".admin-page .select-sm", {
  padding: "4px 8px",
  border: "1px solid #ccc",
  borderRadius: "4px",
  fontSize: "0.83rem",
});

globalStyle(".admin-page .pagination", {
  display: "flex",
  gap: "12px",
  alignItems: "center",
  justifyContent: "center",
  marginTop: "12px",
});

globalStyle(".admin-page .pagination button", {
  padding: "6px 16px",
  border: "1px solid #ccc",
  borderRadius: "6px",
  background: vars.color.backgroundLight,
  cursor: "pointer",
});

globalStyle(".admin-page .pagination button:disabled", {
  opacity: 0.4,
  cursor: "not-allowed",
});

globalStyle(".admin-page .msg-ok", {
  color: "#2e7d32",
  fontSize: "0.88rem",
  marginTop: "8px",
});

globalStyle(".admin-page .msg-error", {
  color: "#c62828",
  fontSize: "0.88rem",
  marginTop: "8px",
});

globalStyle(".admin-page .loading", {
  color: "#888",
  padding: "20px 0",
  textAlign: "center",
});

globalStyle(".admin-page .empty", {
  color: "#aaa",
  textAlign: "center",
  padding: "20px 0",
});

globalStyle(".admin-page .mb-s", {
  marginBottom: "10px",
});

globalStyle(".admin-page .preview-list", {
  display: "flex",
  flexDirection: "column",
  gap: "8px",
  padding: "12px",
  background: "#f0f7ff",
  border: "1px solid #90caf9",
  borderRadius: "8px",
});

globalStyle(".admin-page .preview-item", {
  display: "flex",
  justifyContent: "space-between",
  alignItems: "center",
  gap: "12px",
  padding: "10px 12px",
  background: vars.color.backgroundLight,
  border: "1px solid #e3f2fd",
  borderRadius: "6px",
  transition: "background 0.15s",
});

globalStyle(".admin-page .preview-item:hover", {
  background: "#fafafa",
});

globalStyle(".admin-page .preview-text", {
  margin: 0,
  fontSize: "0.95rem",
  color: vars.color.primary,
  fontWeight: 500,
  flex: 1,
});
