#!/bin/bash
# Smoke tests for Miring Validator
# Run after deployment to verify site functionality and security headers
# Usage: ./smoke-tests/test_miring.sh [BASE_URL]
# Example: ./smoke-tests/test_miring.sh https://qa1.miring.nmdp.org

set -uo pipefail

BASE_URL="${1:-https://qa1.miring.nmdp.org}"
PASS=0
FAIL=0

green() { printf "\033[32m✓ %s\033[0m\n" "$1"; }
red() { printf "\033[31m✗ %s\033[0m\n" "$1"; }

assert_header() {
  local url="$1" header="$2" expected="$3" method="${4:-GET}"
  local actual
  actual=$(curl -sk -X "$method" -o /dev/null -D - "$url" 2>/dev/null | grep -i "^${header}:" | head -1 | tr -d '\r')
  if echo "$actual" | grep -qi "$expected"; then
    green "$method $url — $header contains '$expected'"
    PASS=$((PASS+1))
  else
    red "$method $url — $header expected '$expected', got: '$actual'"
    FAIL=$((FAIL+1))
  fi
}

assert_header_absent() {
  local url="$1" header="$2" forbidden="$3"
  local actual
  actual=$(curl -sk -X POST -d 'xml=test' -o /dev/null -D - "$url" 2>/dev/null | grep -i "^${header}:" | head -1 | tr -d '\r')
  if [ -z "$actual" ]; then
    green "$url — $header not present (good)"
    PASS=$((PASS+1))
  elif echo "$actual" | grep -qi "$forbidden"; then
    red "$url — $header should NOT contain '$forbidden', got: '$actual'"
    FAIL=$((FAIL+1))
  else
    green "$url — $header does not contain '$forbidden'"
    PASS=$((PASS+1))
  fi
}

assert_status() {
  local url="$1" expected="$2" method="${3:-GET}" data="${4:-}"
  local actual
  if [ -n "$data" ]; then
    actual=$(curl -sk -X "$method" -d "$data" -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
  else
    actual=$(curl -sk -X "$method" -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
  fi
  if [ "$actual" = "$expected" ]; then
    green "$method $url — HTTP $expected"
    PASS=$((PASS+1))
  else
    red "$method $url — expected HTTP $expected, got $actual"
    FAIL=$((FAIL+1))
  fi
}

assert_body_contains() {
  local url="$1" method="$2" data="$3" expected="$4" desc="$5"
  local body
  if [ -n "$data" ]; then
    body=$(curl -sk -X "$method" -d "$data" "$url" 2>/dev/null)
  else
    body=$(curl -sk -X "$method" "$url" 2>/dev/null)
  fi
  if echo "$body" | grep -q "$expected"; then
    green "$desc"
    PASS=$((PASS+1))
  else
    red "$desc — '$expected' not found in response"
    FAIL=$((FAIL+1))
  fi
}

echo ""
echo "═══════════════════════════════════════════════════"
echo " Miring Validator Smoke Tests"
echo " Target: $BASE_URL"
echo "═══════════════════════════════════════════════════"
echo ""

# ─── Page availability ───────────────────────────────
echo "── Page Availability ──"
assert_status "$BASE_URL/" "200"
assert_status "$BASE_URL/index.html" "200"
assert_status "$BASE_URL/MoreInfo.html" "200"
assert_status "$BASE_URL/SecurityUpdates.html" "200"
assert_status "$BASE_URL/CHANGELOG.html" "200"
assert_status "$BASE_URL/hml/hml_1_0_1_example_miring.xml" "200"
assert_status "$BASE_URL/rules/Rules.csv" "200"
assert_status "$BASE_URL/javascript/WebScripts.js" "200"

echo ""
echo "── Security Headers (index) ──"
assert_header "$BASE_URL/" "X-Frame-Options" "DENY"
assert_header "$BASE_URL/" "X-Content-Type-Options" "nosniff"
assert_header "$BASE_URL/" "Strict-Transport-Security" "max-age="
assert_header "$BASE_URL/" "Content-Security-Policy" "frame-ancestors"
assert_header "$BASE_URL/" "Permissions-Policy" "camera=()"
assert_header "$BASE_URL/" "Referrer-Policy" "strict-origin"
assert_header "$BASE_URL/" "X-XSS-Protection" "1; mode=block"

echo ""
echo "── Security Headers (validator POST) ──"
assert_header "$BASE_URL/validator/ValidateMiring/" "X-Frame-Options" "DENY" "POST"
assert_header "$BASE_URL/validator/ValidateMiring/" "Strict-Transport-Security" "max-age=" "POST"
assert_header "$BASE_URL/validator/ValidateMiring/" "Cache-Control" "no-store" "POST"
assert_header "$BASE_URL/validator/ValidateMiring/" "Access-Control-Allow-Origin" "*" "POST"
assert_header_absent "$BASE_URL/validator/ValidateMiring/" "Access-Control-Allow-Credentials" "true"

echo ""
echo "── Session Cookie Hardening ──"
assert_header "$BASE_URL/" "Set-Cookie" "HttpOnly"
assert_header "$BASE_URL/" "Set-Cookie" "Secure"

echo ""
echo "── Validator Functionality ──"

# Test: null/empty XML returns fatal
assert_body_contains "$BASE_URL/validator/ValidateMiring/" "POST" "xml=" \
  "fatal" "Empty XML returns fatal severity"

# Test: valid-ish HML returns a miring-report
assert_body_contains "$BASE_URL/validator/ValidateMiring/" "POST" \
  "xml=<?xml version=\"1.0\"?><hml xmlns=\"http://schemas.nmdp.org/spec/hml/1.0.1\" version=\"1.0.1\"><hmlid root=\"2.34.55.3\" extension=\"test1\"/><sample id=\"123\"><typing gene-family=\"HLA\" date=\"2015-01-01\"><allele-assignment date=\"2015-07-28\" allele-db=\"IMGT/HLA\" allele-version=\"3.20.0\"><glstring>HLA-A*01:01</glstring></allele-assignment><typing-method><sbt-sanger locus=\"HLA-A\" test-id=\"GTR000000000.0\" test-id-source=\"NCBI-GTR\"/></typing-method></typing></sample><reporting-center reporting-center-id=\"789\" reporting-center-context=\"NMDP\"/></hml>" \
  "miring-report" "Valid HML returns miring-report XML"

# Test: sample HML file validates
SAMPLE_XML=$(curl -sk "$BASE_URL/hml/hml_1_0_1_example_miring.xml" 2>/dev/null)
assert_body_contains "$BASE_URL/validator/ValidateMiring/" "POST" \
  "xml=$SAMPLE_XML" \
  "miring-compliant" "Sample HML returns miring-compliant field"

echo ""
echo "═══════════════════════════════════════════════════"
echo " Results: $PASS passed, $FAIL failed"
echo "═══════════════════════════════════════════════════"
echo ""

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
