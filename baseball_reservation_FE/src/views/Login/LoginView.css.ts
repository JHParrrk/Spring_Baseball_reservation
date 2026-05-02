import { globalStyle } from "@vanilla-extract/css";

globalStyle(".login-container", {
  display: "flex",
  justifyContent: "center",
  alignItems: "center",
  minHeight: "100vh",
  background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
  fontFamily:
    '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
});

globalStyle(".login-container .login-card", {
  background: "white",
  borderRadius: "16px",
  boxShadow: "0 20px 60px rgba(0, 0, 0, 0.3)",
  padding: "60px 40px",
  textAlign: "center",
  maxWidth: "400px",
  width: "100%",
});

globalStyle(".login-container h1", {
  fontSize: "28px",
  fontWeight: 600,
  color: "#333",
  margin: "0 0 12px 0",
});

globalStyle(".login-container .description", {
  fontSize: "16px",
  color: "#666",
  margin: "0 0 32px 0",
});

globalStyle(".login-container .login-button", {
  width: "100%",
  padding: "14px 24px",
  background: "#ffffff",
  border: "1px solid #dadce0",
  borderRadius: "8px",
  fontSize: "16px",
  fontWeight: 500,
  color: "#3c4043",
  cursor: "pointer",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  gap: "12px",
  transition: "all 0.3s ease",
  marginBottom: "24px",
});

globalStyle(".login-container .login-button:hover", {
  backgroundColor: "#f8f9fa",
  borderColor: "#d2d3d4",
  boxShadow: "0 1px 1px rgba(0, 0, 0, 0.04), 0 1px 3px rgba(0, 0, 0, 0.12)",
});

globalStyle(".login-container .login-button:active", {
  backgroundColor: "#f1f3f4",
});

globalStyle(".login-container .login-button img", {
  width: "20px",
  height: "20px",
});

globalStyle(".login-container .home-button", {
  display: "block",
  width: "100%",
  padding: "12px 24px",
  borderRadius: "8px",
  border: "1px solid #e0e0e0",
  color: "#555",
  textDecoration: "none",
  fontSize: "14px",
  fontWeight: 500,
  marginBottom: "24px",
  transition: "all 0.2s ease",
});

globalStyle(".login-container .home-button:hover", {
  background: "#f7f7f7",
  borderColor: "#d0d0d0",
});

globalStyle(".login-container .info-text", {
  fontSize: "13px",
  color: "#999",
  margin: 0,
  lineHeight: 1.6,
});

globalStyle(".login-container .login-card", {
  "@media": {
    "screen and (max-width: 480px)": {
      padding: "40px 20px",
    },
  },
});

globalStyle(".login-container h1", {
  "@media": {
    "screen and (max-width: 480px)": {
      fontSize: "24px",
    },
  },
});

globalStyle(".login-container .login-button", {
  "@media": {
    "screen and (max-width: 480px)": {
      padding: "12px 16px",
      fontSize: "14px",
    },
  },
});
