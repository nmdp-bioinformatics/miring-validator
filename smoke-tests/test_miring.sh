#!/bin/bash
# Smoke tests for Miring Validator
# Validates site functionality matches production behavior after deploy.
# Tests mirror what the UI buttons do: VALIDATE, SAMPLE, BROWSE (file upload), CLEAR
#
# Usage: ./smoke-tests/test_miring.sh [BASE_URL]
# Default: https://qa1.miring.nmdp.org

set -uo pipefail

BASE_URL="${1:-https://qa1.miring.nmdp.org}"
PASS=0
FAIL=0

green() { printf "\033[32m✓ %s\033[0m\n" "$1"; }
red() { printf "\033[31m✗ %s\033[0m\n" "$1"; }

assert_status() {
  local url="$1" expected="$2" method="${3:-GET}" data="${4:-}"
  local actual
  if [ -n "$data" ]; then
    actual=$(curl -sk -X "$method" --data-urlencode "$data" -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
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

assert_header() {
  local url="$1" header="$2" expected="$3" method="${4:-GET}" data="${5:-}"
  local actual
  if [ -n "$data" ]; then
    actual=$(curl -sk -X "$method" --data-urlencode "$data" -o /dev/null -D - "$url" 2>/dev/null | grep -i "^${header}:" | head -1 | tr -d '\r')
  else
    actual=$(curl -sk -X "$method" -o /dev/null -D - "$url" 2>/dev/null | grep -i "^${header}:" | head -1 | tr -d '\r')
  fi
  if echo "$actual" | grep -qi "$expected"; then
    green "$method $url — $header contains '$expected'"
    PASS=$((PASS+1))
  else
    red "$method $url — $header expected '$expected', got: '${actual:-<empty>}'"
    FAIL=$((FAIL+1))
  fi
}

assert_header_missing() {
  local url="$1" header="$2" method="${3:-POST}" data="${4:-}"
  local actual
  if [ -n "$data" ]; then
    actual=$(curl -sk -X "$method" --data-urlencode "$data" -o /dev/null -D - "$url" 2>/dev/null | grep -i "^${header}:" | head -1 | tr -d '\r')
  else
    actual=$(curl -sk -X "$method" -o /dev/null -D - "$url" 2>/dev/null | grep -i "^${header}:" | head -1 | tr -d '\r')
  fi
  if [ -z "$actual" ]; then
    green "$method $url — $header not present (good)"
    PASS=$((PASS+1))
  else
    red "$method $url — $header should be absent, got: '$actual'"
    FAIL=$((FAIL+1))
  fi
}

assert_response_contains() {
  local desc="$1" url="$2" method="$3" data="$4" expected="$5"
  local body
  if [ -n "$data" ]; then
    body=$(curl -sk -X "$method" --data-urlencode "$data" "$url" 2>/dev/null)
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

assert_response_equals() {
  local desc="$1" url="$2" method="$3" data="$4" xpath_pattern="$5" expected="$6"
  local body
  if [ -n "$data" ]; then
    body=$(curl -sk -X "$method" --data-urlencode "$data" "$url" 2>/dev/null)
  else
    body=$(curl -sk -X "$method" "$url" 2>/dev/null)
  fi
  local actual
  actual=$(echo "$body" | sed -n "s/.*<${xpath_pattern}>\([^<]*\)<\/${xpath_pattern}>.*/\1/p" | head -1)
  if [ "$actual" = "$expected" ]; then
    green "$desc — got '$expected'"
    PASS=$((PASS+1))
  else
    red "$desc — expected '$expected', got '${actual:-<empty>}'"
    FAIL=$((FAIL+1))
  fi
}

echo ""
echo "═══════════════════════════════════════════════════════════"
echo " Miring Validator Smoke Tests"
echo " Target: $BASE_URL"
echo " Baseline: miring.b12x.org (production)"
echo "═══════════════════════════════════════════════════════════"
echo ""

# ─── 1. PAGE AVAILABILITY ────────────────────────────
echo "── 1. Page Availability ──"
assert_status "$BASE_URL/" "200"
assert_status "$BASE_URL/index.html" "200"
assert_status "$BASE_URL/MoreInfo.html" "200"
assert_status "$BASE_URL/SecurityUpdates.html" "200"
assert_status "$BASE_URL/CHANGELOG.html" "200"
assert_status "$BASE_URL/hml/hml_1_0_1_example_miring.xml" "200"
assert_status "$BASE_URL/rules/Rules.csv" "200"
assert_status "$BASE_URL/javascript/WebScripts.js" "200"

echo ""
echo "── 2. SAMPLE Button (loads sample HML and validates) ──"
# This simulates: user clicks SAMPLE → JS fetches the sample file → POSTs it to validator
SAMPLE_XML=$(curl -sk "$BASE_URL/hml/hml_1_0_1_example_miring.xml" 2>/dev/null)
if [ -z "$SAMPLE_XML" ]; then
  red "Could not fetch sample HML file"
  FAIL=$((FAIL+1))
else
  green "Sample HML fetched successfully"
  PASS=$((PASS+1))

  # Validate sample — should match prod: hml-compliant=true, miring-compliant=warnings
  assert_response_equals "Sample: hml-compliant=true" \
    "$BASE_URL/validator/ValidateMiring/" "POST" "xml=$SAMPLE_XML" "hml-compliant" "true"

  assert_response_equals "Sample: miring-compliant=warnings" \
    "$BASE_URL/validator/ValidateMiring/" "POST" "xml=$SAMPLE_XML" "miring-compliant" "warnings"

  # Should contain sample IDs from the file
  assert_response_contains "Sample: contains sample-id 4555-6677-8" \
    "$BASE_URL/validator/ValidateMiring/" "POST" "xml=$SAMPLE_XML" "4555-6677-8"

  assert_response_contains "Sample: contains sample-id 4555-6677-9" \
    "$BASE_URL/validator/ValidateMiring/" "POST" "xml=$SAMPLE_XML" "4555-6677-9"

  # Response should be valid XML with miring-report root
  assert_response_contains "Sample: returns miring-report XML" \
    "$BASE_URL/validator/ValidateMiring/" "POST" "xml=$SAMPLE_XML" "miring-report"

  # Should have validation warnings (the sample is compliant but has warnings)
  assert_response_contains "Sample: contains validation-warnings section" \
    "$BASE_URL/validator/ValidateMiring/" "POST" "xml=$SAMPLE_XML" "validation-warnings"
fi

echo ""
echo "── 3. VALIDATE Button (empty input) ──"
# This simulates: user clicks VALIDATE with empty textarea
assert_response_equals "Empty input: hml-compliant=false" \
  "$BASE_URL/validator/ValidateMiring/" "POST" "xml=" "hml-compliant" "false"

assert_response_equals "Empty input: miring-compliant=false" \
  "$BASE_URL/validator/ValidateMiring/" "POST" "xml=" "miring-compliant" "false"

assert_response_contains "Empty input: returns 'XML is length 0' error" \
  "$BASE_URL/validator/ValidateMiring/" "POST" "xml=" "XML is length 0"

assert_response_contains "Empty input: severity=fatal" \
  "$BASE_URL/validator/ValidateMiring/" "POST" "xml=" 'severity="fatal"'

echo ""
echo "── 4. VALIDATE Button (non-XML garbage input) ──"
# User pastes random text that isn't XML
assert_response_equals "Garbage input: hml-compliant=false" \
  "$BASE_URL/validator/ValidateMiring/" "POST" "xml=this is not xml at all" "hml-compliant" "false"

assert_response_equals "Garbage input: miring-compliant=false" \
  "$BASE_URL/validator/ValidateMiring/" "POST" "xml=this is not xml at all" "miring-compliant" "false"

assert_response_contains "Garbage input: prolog error" \
  "$BASE_URL/validator/ValidateMiring/" "POST" "xml=this is not xml at all" "Content is not allowed in prolog"

echo ""
echo "── 5. VALIDATE Button (valid XML but not HML) ──"
# User pastes well-formed XML that isn't HML
assert_response_equals "Non-HML XML: hml-compliant=false" \
  "$BASE_URL/validator/ValidateMiring/" "POST" 'xml=<?xml version="1.0"?><notHml><stuff/></notHml>' "hml-compliant" "false"

assert_response_equals "Non-HML XML: miring-compliant=false" \
  "$BASE_URL/validator/ValidateMiring/" "POST" 'xml=<?xml version="1.0"?><notHml><stuff/></notHml>' "miring-compliant" "false"

assert_response_contains "Non-HML XML: returns miring-report" \
  "$BASE_URL/validator/ValidateMiring/" "POST" 'xml=<?xml version="1.0"?><notHml><stuff/></notHml>' "miring-report"

echo ""
echo "── 6. BROWSE/Upload (file upload simulation) ──"
# The BROWSE button reads a file and POSTs its content — same as VALIDATE but with file content
# Test with a minimal valid HML 1.0.1 that triggers schema errors
UPLOAD_XML='<?xml version="1.0" encoding="UTF-8"?><hml xmlns="http://schemas.nmdp.org/spec/hml/1.0.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0.1"><hmlid root="2.34.55.3" extension="upload-test"/><sample id="UPLOAD-001"><typing gene-family="HLA" date="2015-01-01"><allele-assignment date="2015-07-28" allele-db="IMGT/HLA" allele-version="3.20.0"><glstring>HLA-A*01:01+HLA-A*01:02</glstring></allele-assignment><typing-method><sbt-sanger locus="HLA-A" test-id="GTR000000000.0" test-id-source="NCBI-GTR"/></typing-method></typing></sample><reporting-center reporting-center-id="789" reporting-center-context="NMDP"/></hml>'

assert_response_contains "Upload sim: returns miring-report" \
  "$BASE_URL/validator/ValidateMiring/" "POST" "xml=$UPLOAD_XML" "miring-report"

assert_response_contains "Upload sim: contains hmlid with our extension" \
  "$BASE_URL/validator/ValidateMiring/" "POST" "xml=$UPLOAD_XML" 'extension="upload-test"'

assert_response_contains "Upload sim: references our sample in report" \
  "$BASE_URL/validator/ValidateMiring/" "POST" "xml=$UPLOAD_XML" "sample"

# Should still produce a structured report even if not fully compliant
assert_response_contains "Upload sim: has miring-compliant field" \
  "$BASE_URL/validator/ValidateMiring/" "POST" "xml=$UPLOAD_XML" "miring-compliant"

assert_response_contains "Upload sim: has hml-compliant field" \
  "$BASE_URL/validator/ValidateMiring/" "POST" "xml=$UPLOAD_XML" "hml-compliant"

echo ""
echo "── 7. Response Format (DOWNLOAD RESULTS button relies on this) ──"
# The DOWNLOAD button takes the results textarea content and saves as file
# It needs to be valid XML — verify the response is well-formed
RESULT=$(curl -sk -X POST --data-urlencode "xml=$SAMPLE_XML" "$BASE_URL/validator/ValidateMiring/" 2>/dev/null)

# Must start with XML declaration
if echo "$RESULT" | head -1 | grep -q '<?xml'; then
  green "Response starts with XML declaration"
  PASS=$((PASS+1))
else
  red "Response missing XML declaration"
  FAIL=$((FAIL+1))
fi

# Must contain closing miring-report tag
if echo "$RESULT" | grep -q '</miring-report>'; then
  green "Response has closing </miring-report> tag"
  PASS=$((PASS+1))
else
  red "Response missing </miring-report> closing tag"
  FAIL=$((FAIL+1))
fi

# Must contain timestamp attribute
if echo "$RESULT" | grep -q 'timestamp='; then
  green "Response contains timestamp"
  PASS=$((PASS+1))
else
  red "Response missing timestamp"
  FAIL=$((FAIL+1))
fi

# Content-Type must be application/xml for JS to parse it
assert_header "$BASE_URL/validator/ValidateMiring/" "Content-Type" "application/xml" "POST" "xml=$SAMPLE_XML"

echo ""
echo "── 8. Security Headers ──"
assert_header "$BASE_URL/" "X-Frame-Options" "DENY"
assert_header "$BASE_URL/" "X-Content-Type-Options" "nosniff"
assert_header "$BASE_URL/" "Strict-Transport-Security" "max-age="
assert_header "$BASE_URL/" "Content-Security-Policy" "frame-ancestors"
assert_header "$BASE_URL/" "Permissions-Policy" "camera=()"
assert_header "$BASE_URL/" "Referrer-Policy" "strict-origin"
assert_header "$BASE_URL/" "X-XSS-Protection" "1; mode=block"
assert_header "$BASE_URL/validator/ValidateMiring/" "X-Frame-Options" "DENY" "POST" "xml=test"
assert_header "$BASE_URL/validator/ValidateMiring/" "Cache-Control" "no-store" "POST" "xml=test"
assert_header "$BASE_URL/validator/ValidateMiring/" "Access-Control-Allow-Origin" "*" "POST" "xml=test"
assert_header_missing "$BASE_URL/validator/ValidateMiring/" "Access-Control-Allow-Credentials" "POST" "xml=test"

echo ""
echo "── 9. Session Cookie ──"
assert_header "$BASE_URL/" "Set-Cookie" "HttpOnly"
assert_header "$BASE_URL/" "Set-Cookie" "Secure"

echo ""
echo "═══════════════════════════════════════════════════════════"
echo " Results: $PASS passed, $FAIL failed"
echo "═══════════════════════════════════════════════════════════"
echo ""

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
