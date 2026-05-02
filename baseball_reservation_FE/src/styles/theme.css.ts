import { createGlobalTheme, createTheme } from "@vanilla-extract/css";
import { dark, light } from "./theme";

export const vars = createGlobalTheme(":root", {
  color: {
    primary: light.color.primary,
    background: light.color.background,
    secondary: light.color.secondary,
    third: light.color.third,
    border: light.color.border,
    text: light.color.text,
    backgroundLight: light.color.backgroundLight,
  },
  heading: {
    large: { fontSize: light.heading.large.fontSize },
    medium: { fontSize: light.heading.medium.fontSize },
    small: { fontSize: light.heading.small.fontSize },
  },
  button: {
    large: {
      fontSize: light.button.large.fontSize,
      padding: light.button.large.padding,
    },
    medium: {
      fontSize: light.button.medium.fontSize,
      padding: light.button.medium.padding,
    },
    small: {
      fontSize: light.button.small.fontSize,
      padding: light.button.small.padding,
    },
  },
  buttonScheme: {
    primary: {
      color: light.buttonScheme.primary.color,
      backgroundColor: light.buttonScheme.primary.backgroundColor,
    },
    normal: {
      color: light.buttonScheme.normal.color,
      backgroundColor: light.buttonScheme.normal.backgroundColor,
    },
    like: {
      color: light.buttonScheme.like.color,
      backgroundColor: light.buttonScheme.like.backgroundColor,
    },
  },
  borderRadius: {
    default: light.borderRadius.default,
  },
  layout: {
    width: {
      large: light.layout.width.large,
      medium: light.layout.width.medium,
      small: light.layout.width.small,
    },
  },
});

// Apply by toggling this class on <html> or <body> when dark mode is needed.
export const darkThemeClass = createTheme(vars, {
  color: {
    primary: dark.color.primary,
    background: dark.color.background,
    secondary: dark.color.secondary,
    third: dark.color.third,
    border: dark.color.border,
    text: dark.color.text,
    backgroundLight: dark.color.backgroundLight,
  },
  heading: {
    large: { fontSize: dark.heading.large.fontSize },
    medium: { fontSize: dark.heading.medium.fontSize },
    small: { fontSize: dark.heading.small.fontSize },
  },
  button: {
    large: {
      fontSize: dark.button.large.fontSize,
      padding: dark.button.large.padding,
    },
    medium: {
      fontSize: dark.button.medium.fontSize,
      padding: dark.button.medium.padding,
    },
    small: {
      fontSize: dark.button.small.fontSize,
      padding: dark.button.small.padding,
    },
  },
  buttonScheme: {
    primary: {
      color: dark.buttonScheme.primary.color,
      backgroundColor: dark.buttonScheme.primary.backgroundColor,
    },
    normal: {
      color: dark.buttonScheme.normal.color,
      backgroundColor: dark.buttonScheme.normal.backgroundColor,
    },
    like: {
      color: dark.buttonScheme.like.color,
      backgroundColor: dark.buttonScheme.like.backgroundColor,
    },
  },
  borderRadius: {
    default: dark.borderRadius.default,
  },
  layout: {
    width: {
      large: dark.layout.width.large,
      medium: dark.layout.width.medium,
      small: dark.layout.width.small,
    },
  },
});
