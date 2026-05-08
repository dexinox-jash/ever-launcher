# Legal Documents — Fill-In Checklist

Before publishing ANY of the four legal documents, replace every placeholder below.
Search for each placeholder with Ctrl+F and replace it globally.

---

## Required Placeholders (All 4 Documents)

| Placeholder | What to put here |
|-------------|-----------------|
| `[YOUR FULL LEGAL NAME OR COMPANY NAME]` | Your legal name (e.g., "Jean Tremblay") or your registered business name (e.g., "1234567 Canada Inc."). If you have a numbered company or sole proprietorship, use the exact registered name. |
| `[YOUR CONTACT EMAIL]` | A real, monitored email address for legal/privacy inquiries (e.g., privacy@everlauncher.app or your personal email). |
| `[YOUR FULL ADDRESS, CITY, PROVINCE, POSTAL CODE, CANADA]` | Your complete mailing address. You MUST include a real physical address for PIPEDA compliance. A P.O. box is acceptable. |
| `[YOUR PROVINCE]` | The Canadian province where you reside or your business is registered (e.g., "Quebec," "Ontario," "British Columbia"). This governs dispute resolution. |
| `[YOUR CITY, YOUR PROVINCE]` | City and province for the arbitration seat in Terms of Service §14.2 (e.g., "Montreal, Quebec"). |
| `[INSERT DATE]` | The date you publish the document (format: Month DD, YYYY, e.g., "April 15, 2026"). Use the same date in ALL documents. |

---

## Steps to Publish

1. **Fill in all placeholders** in all four files.
2. **Convert Markdown to HTML** — use a tool like Pandoc, Marked 2, or a markdown-to-HTML converter.
3. **Host on your domain:**
   - Privacy Policy → `https://everlauncher.app/privacy`
   - Terms of Service → `https://everlauncher.app/terms`
   - EULA → `https://everlauncher.app/eula`
   - Sales Terms → `https://everlauncher.app/sales-terms`
4. **Update the App** — the SettingsScreen.kt already links to the privacy and terms URLs.
5. **Play Console Data Safety Form** — complete it stating:
   - No personal data collected or shared
   - No data encrypted in transit (no network calls)
   - Users can delete all data by uninstalling
   - PACKAGE_USAGE_STATS used for app functionality (focus score/analytics)
   - QUERY_ALL_PACKAGES used for app functionality (launcher display)
6. **Have a Canadian lawyer review** — especially if you are in Quebec (Law 25 has strict enforcement).

---

## Province-Specific Notes

| Province | Extra Requirement |
|----------|------------------|
| **Quebec** | Law 25 requires a Privacy Officer designation. Add your name/email as Privacy Officer in the Privacy Policy. You may also need to register with the Commission d'accès à l'information (CAI) if you process personal info — but since you collect none, this is likely N/A. |
| **Alberta** | PIPA (AB) applies. Your policy already covers it. No extra steps needed given zero data collection. |
| **British Columbia** | PIPA (BC) applies. Same as Alberta — zero collection = minimal obligations. |
| **All other provinces** | PIPEDA applies federally. Your policy covers it. |

---

## What These Documents Protect You From

| Risk | Protection |
|------|-----------|
| Refund demands after 2-hour Google Play window | Sales Terms §6.2 — explicit final sale policy with table |
| Chargebacks | Sales Terms §6.6 — express chargeback rebuttal right |
| Privacy lawsuits (PIPEDA / Law 25) | Privacy Policy — full PIPEDA compliance, zero data collection documented |
| Intellectual property theft / reverse engineering | EULA §3.1 — explicit prohibition |
| "App doesn't work" claims | ToS §7 + EULA §5 — AS-IS disclaimer; ToS §8 — liability capped at purchase price |
| Class action lawsuits (outside Quebec) | ToS §14.5 — class action waiver |
| Liability for third-party apps users open | ToS §11.3 — third-party disclaimer |
| Health/behavioral outcome claims | ToS §7.3 — behavioral disclaimer |
| App removal from Play Store liability | ToS §10.2 — explicit no-liability clause |
| Future version pricing disputes | Sales Terms §7 — version pricing explicitly addressed |
| Data breach liability | Privacy Policy §7 — no data = no breach risk; on-device only |
