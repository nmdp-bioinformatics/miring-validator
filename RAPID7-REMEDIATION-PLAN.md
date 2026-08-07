# Miring Validator — Rapid7 DAST Remediation Plan

**Scan Date:** 2026-08-05 06:00 AM  
**Report Generated:** 2026-08-07 16:19  
**Target:** https://qa1.miring.nmdp.org/  
**Findings:** 17 vulnerabilities (0 Critical, 0 High, 13 Medium, 4 Low)  
**Baseline:** dev1.miring.nmdp.org — 46/46 smoke tests passing

---

## Findings Summary

| # | Finding | Count | Severity | CVSS | Action |
|---|---------|-------|----------|------|--------|
| 1 | HTTPHeaders — missing charset | 7 | Medium | 0 | **Fix** |
| 2 | CORS — `Access-Control-Allow-Origin: *` | 1 | Medium | 5 | **Risk Accept** |
| 3 | JavaScriptLeaks — no strict mode | 2 | Medium | 0 | **Fix** |
| 4 | SubresourceIntegrity — MoreInfo.html | 1 | Low | 3.8 | **Fix** |
| 5 | CSPHeaders — allows CDN domains | 4 | Low | 3.8 | **Risk Accept** |
| 6 | XPoweredByHeader — empty header present | 1 | Low | 0 | **Fix** |
| 7 | SessionFixation — route LB cookie | 1 | Low | 3.5 | **Risk Accept** |

---

## Fix #1: HTTPHeaders — Missing charset attribute (7 instances)

**Problem:** Content-Type response headers lack `; charset=UTF-8`. Affects all URLs.  
**Proof:** `Content-Type: application/xml` → should be `Content-Type: application/xml; charset=UTF-8`  
**References:** CWE-79, OWASP2023API-0xa8, OWASP2025-A05

**Fix:** Update `SecurityHeadersFilter.java` to append charset to Content-Type after chain.doFilter() completes. Also update `MiringValidatorService.java` to set explicit charset on the validator response.

**Files changed:**
- `src/main/java/org/nmdp/miring/SecurityHeadersFilter.java`
- `src/main/java/org/nmdp/miring/MiringValidatorService.java`

**Validation:** `curl -sI https://dev1.miring.nmdp.org/index.html | grep Content-Type` should show `charset=UTF-8`

---

## Fix #3: JavaScriptLeaks — strict mode not defined (2 instances)

**Problem:** `/javascript/WebScripts.js` and inline scripts in `MoreInfo.html` don't use strict mode.  
**References:** OWASP2025-A02

**Fix:** Add `'use strict';` to the top of `WebContent/javascript/WebScripts.js` (after the license comment block).

**Files changed:**
- `WebContent/javascript/WebScripts.js`

**Validation:** Smoke tests pass; VALIDATE/SAMPLE/BROWSE buttons still work on dev1.

---

## Fix #4: SubresourceIntegrity — MoreInfo.html (1 instance)

**Problem:** `MoreInfo.html` loads jQuery and Bootstrap JS from CDN without `integrity` or `crossorigin` attributes.  
**References:** OWASP2025-A08, CWE-494, OWASP2021-A08

**Fix:** Add SRI `integrity` and `crossorigin="anonymous"` attributes to CDN script tags in `MoreInfo.html`. Already done for `index.html` in v1.6.0 — this brings `MoreInfo.html` to parity.

**Files changed:**
- `WebContent/MoreInfo.html`

**Validation:** MoreInfo.html loads correctly; rules table renders.

---

## Fix #6: XPoweredByHeader — empty header present (1 instance)

**Problem:** Response includes `x-powered-by:` (empty value). Scanner flags its presence regardless of value.  
**Proof:** `x-powered-by:` followed by `access-control-allow-origin: *` — scanner interprets both as one "X-Powered-By" value.  
**References:** OWASP2017-A6, OWASP2025-A01, OWASP2021-A01, CWE-201

**Fix:** Use a `HttpServletResponseWrapper` to suppress the `X-Powered-By` header entirely rather than setting it to empty string. The wrapper intercepts `setHeader`/`addHeader` calls and silently drops any `X-Powered-By` header the container tries to add.

**Files changed:**
- `src/main/java/org/nmdp/miring/SecurityHeadersFilter.java`

**Validation:** `curl -sI https://dev1.miring.nmdp.org/ | grep -i x-powered-by` should return nothing.

---

## Risk Accept #2: CORS — `Access-Control-Allow-Origin: *`

**Justification:** The Miring Validator is a public, stateless XML validation API. It accepts HML XML via POST and returns validation results. It is intentionally designed to be called from any origin (testing tools, research portals, other NMDP apps). No sensitive data is returned — only XML validation reports.

**Rapid7 Status:** Verified (by design)

---

## Risk Accept #5: CSPHeaders — allows CDN domains

**Justification:** The CSP correctly restricts script/style/font sources to only the specific CDN domains the application requires: `ajax.googleapis.com` (jQuery), `maxcdn.bootstrapcdn.com` (Bootstrap), `cdnjs.cloudflare.com` (Font Awesome). Removing these would break the UI. The CSP is not overly permissive — it does not use `*` and lists only known, trusted CDNs.

**Rapid7 Status:** Verified (by design)

---

## Risk Accept #7: SessionFixation — route cookie

**Justification:** The `route` cookie is injected by the NGINX Ingress load balancer for sticky session affinity — it is not an application-level session cookie. The Miring Validator has no authentication, no user sessions, and no state that can be "fixated." The application is entirely stateless and public. There is no attack surface for session fixation on a stateless public API.

**Rapid7 Status:** Verified (infrastructure, not exploitable)

---

## Deployment Plan

1. Create feature branch: `feature/dast-remediation-v1.7`
2. Implement fixes #1, #3, #4, #6
3. Run smoke tests locally against dev1 (current baseline: 46/46 pass)
4. Push branch — CI pipeline builds and deploys to dev1
5. Run smoke tests against dev1 post-deploy
6. Validate in browser that VALIDATE/SAMPLE/BROWSE buttons work
7. Merge to master → deploys to QA1
8. Run smoke tests against QA1
9. Wait for next Rapid7 scan (scheduled 08/12/26) to confirm findings clear
10. Mark risk-accepted items in Rapid7 UI

---

## Smoke Test Baseline (dev1 — 2026-08-03)

```
Results: 46 passed, 0 failed
```

All page availability, VALIDATE, SAMPLE, BROWSE, DOWNLOAD, security headers, and session cookie tests pass.

---

## Post-Fix Validation Checklist

- [ ] All 46 smoke tests pass on dev1
- [ ] `Content-Type` headers include `; charset=UTF-8` on all endpoints
- [ ] `X-Powered-By` header completely absent from responses
- [ ] `WebScripts.js` loads without console errors
- [ ] MoreInfo.html renders rule table correctly
- [ ] VALIDATE button works (empty, garbage, valid HML)
- [ ] SAMPLE button loads and validates sample file
- [ ] DOWNLOAD RESULTS produces valid XML file
