import {
  isArray,
  isFunction,
  isString
} from "./chunk-3UKXF4U3.js";
import "./chunk-PQECYZMI.js";

// node_modules/@vuepress/shared/dist/index.js
var resolveHeadIdentifier = ([
  tag,
  attrs,
  content
]) => {
  if (tag === "meta" && attrs.name) {
    return `${tag}.${attrs.name}`;
  }
  if (["title", "base"].includes(tag)) {
    return tag;
  }
  if (tag === "template" && attrs.id) {
    return `${tag}.${attrs.id}`;
  }
  return JSON.stringify([tag, attrs, content]);
};
var dedupeHead = (head) => {
  const identifierSet = /* @__PURE__ */ new Set();
  const result = [];
  head.forEach((item) => {
    const identifier = resolveHeadIdentifier(item);
    if (!identifierSet.has(identifier)) {
      identifierSet.add(identifier);
      result.push(item);
    }
  });
  return result;
};
var ensureLeadingSlash = (str) => str.replace(/^\/?/, "/");
var ensureEndingSlash = (str) => /(\.html|\/)$/.test(str) ? str : str + "/";
var formatDateString = (str, defaultDateString = "") => {
  const dateMatch = str.match(/\b(\d{4})-(\d{1,2})-(\d{1,2})\b/);
  if (dateMatch === null) {
    return defaultDateString;
  }
  const [, yearStr, monthStr, dayStr] = dateMatch;
  return [yearStr, monthStr.padStart(2, "0"), dayStr.padStart(2, "0")].join("-");
};
var htmlEscapeMap = {
  "&": "&amp;",
  "<": "&lt;",
  ">": "&gt;",
  "'": "&#39;",
  '"': "&quot;"
};
var htmlEscapeRegexp = /[&<>'"]/g;
var htmlEscape = (str) => str.replace(htmlEscapeRegexp, (char) => htmlEscapeMap[char]);
var htmlUnescapeMap = {
  "&amp;": "&",
  "&#38;": "&",
  "&lt;": "<",
  "&#60;": "<",
  "&gt;": ">",
  "&#62;": ">",
  "&apos;": "'",
  "&#39;": "'",
  "&quot;": '"',
  "&#34;": '"'
};
var htmlUnescapeRegexp = /&(amp|#38|lt|#60|gt|#62|apos|#39|quot|#34);/g;
var htmlUnescape = (str) => str.replace(htmlUnescapeRegexp, (char) => htmlUnescapeMap[char]);
var isLinkFtp = (link) => link.startsWith("ftp://");
var isLinkHttp = (link) => /^(https?:)?\/\//.test(link);
var isLinkExternal = (link, base = "/") => {
  if (isLinkHttp(link) || isLinkFtp(link)) {
    return true;
  }
  if (link.startsWith("/") && !link.startsWith(base) && !link.endsWith(".md")) {
    return true;
  }
  return false;
};
var isLinkMailto = (link) => /^mailto:/.test(link);
var isLinkTel = (link) => /^tel:/.test(link);
var isPlainObject = (val) => Object.prototype.toString.call(val) === "[object Object]";
var removeEndingSlash = (str) => str.replace(/\/$/, "");
var removeLeadingSlash = (str) => str.replace(/^\//, "");
var resolveLocalePath = (locales, routePath) => {
  const localePaths = Object.keys(locales).sort((a, b) => {
    const levelDelta = b.split("/").length - a.split("/").length;
    if (levelDelta !== 0) {
      return levelDelta;
    }
    return b.length - a.length;
  });
  for (const localePath of localePaths) {
    if (routePath.startsWith(localePath)) {
      return localePath;
    }
  }
  return "/";
};
var resolveRoutePathFromUrl = (url, base = "/") => url.replace(/^(https?:)?\/\/[^/]*/, "").replace(new RegExp(`^${base}`), "/");
export {
  dedupeHead,
  ensureEndingSlash,
  ensureLeadingSlash,
  formatDateString,
  htmlEscape,
  htmlUnescape,
  isArray,
  isFunction,
  isLinkExternal,
  isLinkFtp,
  isLinkHttp,
  isLinkMailto,
  isLinkTel,
  isPlainObject,
  isString,
  removeEndingSlash,
  removeLeadingSlash,
  resolveHeadIdentifier,
  resolveLocalePath,
  resolveRoutePathFromUrl
};
//# sourceMappingURL=@vuepress_shared.js.map
