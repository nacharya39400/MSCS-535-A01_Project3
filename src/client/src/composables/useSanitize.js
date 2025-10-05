import DOMPurify from "dompurify";

export function sanitizeString(input) {
  if (typeof input !== "string") return input;
  // Remove any HTML and attributes; returns plain text.
  return DOMPurify.sanitize(input, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] });
}

export function sanitizeNumber(input) {
  if (typeof input === "number") return input;
  const cleaned = sanitizeString(String(input));
  const n = Number.parseFloat(cleaned.replace(/[^0-9.\-]/g, ""));
  return Number.isFinite(n) ? n : 0;
}
