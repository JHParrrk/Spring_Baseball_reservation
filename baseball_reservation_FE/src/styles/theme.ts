export type ThemeName = "light" | "dark";

export type ColorKey =
  | "primary"
  | "background"
  | "secondary"
  | "third"
  | "border"
  | "text"
  | "backgroundLight";

export type HeadingSize = "large" | "medium" | "small";
export type ButtonSize = "large" | "medium" | "small";
export type ButtonScheme = "primary" | "normal" | "like";
export type LayoutWidth = "large" | "medium" | "small";
export type MediaQuery = "mobile" | "tablet" | "desktop";

export interface Theme {
  name: ThemeName;
  color: Record<ColorKey, string>;
  heading: Record<HeadingSize, { fontSize: string }>;
  button: Record<ButtonSize, { fontSize: string; padding: string }>;
  buttonScheme: Record<
    ButtonScheme,
    { color: string; backgroundColor: string }
  >;
  borderRadius: { default: string };
  layout: { width: Record<LayoutWidth, string> };
  mediaQuery: Record<MediaQuery, string>;
}

export const light: Theme = {
  name: "light",
  color: {
    primary: "#ff5800",
    background: "#f3f4f6",
    secondary: "#5f5f5f",
    third: "#2e7d32",
    border: "#d1d5db",
    text: "#111827",
    backgroundLight: "#ffffff",
  },
  heading: {
    large: { fontSize: "2rem" },
    medium: { fontSize: "1.5rem" },
    small: { fontSize: "1rem" },
  },
  button: {
    large: { fontSize: "1.25rem", padding: "0.9rem 1.75rem" },
    medium: { fontSize: "1rem", padding: "0.6rem 1.2rem" },
    small: { fontSize: "0.85rem", padding: "0.45rem 0.85rem" },
  },
  buttonScheme: {
    primary: { color: "#ffffff", backgroundColor: "#ff5800" },
    normal: { color: "#111827", backgroundColor: "#ffffff" },
    like: { color: "#ffffff", backgroundColor: "#ff7f50" },
  },
  borderRadius: { default: "8px" },
  layout: {
    width: {
      large: "1200px",
      medium: "960px",
      small: "640px",
    },
  },
  mediaQuery: {
    mobile: "screen and (max-width: 767px)",
    tablet: "screen and (min-width: 768px) and (max-width: 1023px)",
    desktop: "screen and (min-width: 1024px)",
  },
};

export const dark: Theme = {
  ...light,
  name: "dark",
  color: {
    ...light.color,
    background: "#111827",
    secondary: "#9ca3af",
    border: "#374151",
    text: "#f9fafb",
    backgroundLight: "#1f2937",
  },
  buttonScheme: {
    primary: { color: "#ffffff", backgroundColor: "#ff5800" },
    normal: { color: "#f9fafb", backgroundColor: "#1f2937" },
    like: { color: "#ffffff", backgroundColor: "#fb7185" },
  },
};
