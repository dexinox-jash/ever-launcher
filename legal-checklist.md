# Legal Documents — Pre-Publish Checklist

> **CRITICAL:** Do not publish the app until every placeholder in this checklist has been replaced with real information. Publishing with placeholder text is a fast track to app rejection and legal liability.

---

## Universal Placeholders (All 4 Documents)

Search and replace these in **every** legal document before publishing.

| Placeholder | What to put | Example |
|-------------|-------------|---------|
| `[YOUR FULL LEGAL NAME OR COMPANY NAME]` | Your legal name or registered business | "Jean Tremblay" or "1234567 Canada Inc." |
| `[YOUR CONTACT EMAIL]` | Monitored email for legal/privacy inquiries | "privacy@everlauncher.app" |
| `[YOUR FULL ADDRESS, CITY, PROVINCE, POSTAL CODE, CANADA]` | Complete Canadian mailing address | "1234 Rue Saint-Denis, Montreal, Quebec H2X 3J6, Canada" |
| `[YOUR PROVINCE]` | Province for governing law | "Quebec" |
| `[YOUR CITY, YOUR PROVINCE]` | Arbitration seat city/province | "Montreal, Quebec" |
| `[INSERT DATE]` | Date of publication (same in all docs) | "April 15, 2026" |

---

## Document-by-Document Checklist

### 1. privacy_policy.md

- [ ] Line 3: Effective Date replaced
- [ ] Line 4: Last Updated replaced
- [ ] Line 11: Developer name replaced
- [ ] Line 131: Contact email replaced
- [ ] Line 132: Mailing address replaced
- [ ] Line 156: Developer name replaced (Section 13)
- [ ] Line 157: Contact email replaced
- [ ] Line 158: Mailing address replaced
- [ ] Line 163: Review date replaced
- [ ] **Quebec only:** Add Privacy Officer designation in Section 13

### 2. terms_of_service.md

- [ ] Line 3: Effective Date replaced
- [ ] Line 4: Last Updated replaced
- [ ] Line 11: Developer name replaced
- [ ] Line 11: Province replaced
- [ ] Line 131: Contact email replaced
- [ ] Line 132: Mailing address replaced
- [ ] Line 156: Developer name replaced
- [ ] Line 157: Contact email replaced
- [ ] Line 158: Mailing address replaced
- [ ] Line 163: Review date replaced

### 3. eula.md

- [ ] Line 3: Effective Date replaced
- [ ] Line 4: Last Updated replaced
- [ ] Line 11: Developer name replaced
- [ ] Line 11: Province replaced
- [ ] Line 131: Contact email replaced
- [ ] Line 132: Mailing address replaced
- [ ] Line 156: Developer name replaced
- [ ] Line 157: Contact email replaced
- [ ] Line 158: Mailing address replaced
- [ ] Line 163: Review date replaced

### 4. sales_terms.md

- [ ] Line 3: Effective Date replaced
- [ ] Line 4: Last Updated replaced
- [ ] Line 11: Developer name replaced
- [ ] Line 11: Province replaced
- [ ] Line 131: Contact email replaced
- [ ] Line 132: Mailing address replaced
- [ ] Line 156: Developer name replaced
- [ ] Line 157: Contact email replaced
- [ ] Line 158: Mailing address replaced
- [ ] Line 163: Review date replaced

---

## Hosting Checklist

After filling in all placeholders, host the documents at these exact URLs:

| Document | URL | Status |
|----------|-----|--------|
| Privacy Policy | `https://everlauncher.app/privacy` | [ ] Live & verified |
| Terms of Service | `https://everlauncher.app/terms` | [ ] Live & verified |
| EULA | `https://everlauncher.app/eula` | [ ] Live & verified |
| Sales Terms | `https://everlauncher.app/sales-terms` | [ ] Live & verified |

**Note:** The Settings screen inside the app already links to `/privacy` and `/terms`. Make sure those paths resolve.

---

## Legal Review Recommendation

- [ ] Have a Canadian lawyer review all four documents
- [ ] If in Quebec, confirm Law 25 compliance
- [ ] Save final signed/approved versions in a secure location
- [ ] Set calendar reminder to review documents annually

---

## One-Click Verification Commands

```bash
# Verify no placeholders remain in the published docs
grep -r "\[YOUR\|INSERT DATE\]" .
# Expected output: nothing (or only this checklist file)
```
