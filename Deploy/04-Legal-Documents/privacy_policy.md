# Privacy Policy — EverLauncher

**Effective Date:** April 15, 2026
**Last Updated:** April 15, 2026
**Version:** 1.0

---

## 1. About This Policy

This Privacy Policy describes how **EverLauncher** ("the App," "we," "us," or "our"), developed and published by **Uxtra Creatives** ("Developer"), handles information in connection with your use of the EverLauncher mobile application on Android devices.

This Policy is provided in compliance with Canada's *Personal Information Protection and Electronic Documents Act* (PIPEDA), S.C. 2000, c. 5, Quebec's *Act respecting the protection of personal information in the private sector* (Law 25 / Bill 64), Alberta's *Personal Information Protection Act* (PIPA), British Columbia's *Personal Information Protection Act* (PIPA BC), and all other applicable Canadian federal and provincial privacy legislation.

By downloading, installing, or using EverLauncher, you acknowledge that you have read and understood this Privacy Policy.

---

## 2. Our Core Privacy Commitment

**EverLauncher does not collect, transmit, store, sell, share, or otherwise process any personal information outside of your device.**

All data generated or used by EverLauncher — including your app usage statistics, focus scores, mode settings, and preferences — is stored exclusively in your device's private application storage (Android Room database and DataStore) and never leaves your device through any mechanism operated by us.

EverLauncher contains:
- **No analytics SDK** (no Firebase Analytics, Mixpanel, Amplitude, or similar)
- **No crash reporting SDK** (no Crashlytics, Sentry, Bugsnag, or similar)
- **No advertising SDK** (no AdMob, Meta Audience Network, or similar)
- **No remote server, API endpoint, or backend service** operated by us
- **No network communication** initiated by the App to any server we control

---

## 3. What Data the App Accesses (On-Device Only)

The App requests the following Android system permissions, all of which are used solely on your device:

### 3.1 Usage Access (PACKAGE_USAGE_STATS)
- **What it accesses:** Aggregated statistics about which apps you open and for how long, provided by Android's UsageStatsManager API.
- **Purpose:** To display your daily screen time and unlock count on your personal Focus Score dashboard within the App.
- **Where it goes:** Processed and stored exclusively in the App's private database on your device. This data is never transmitted to us or any third party.
- **How to revoke:** Android Settings → Apps → Special App Access → Usage Access → EverLauncher → Disable.

### 3.2 Installed App Query (QUERY_ALL_PACKAGES)
- **What it accesses:** A list of applications installed on your device that can be launched from a home screen.
- **Purpose:** To display your installed apps in the launcher so you can open them.
- **Where it goes:** Used in real time to populate the app list UI. No list of your installed apps is transmitted, logged remotely, or retained beyond the App's local operation.

### 3.3 No Other Sensitive Permissions
EverLauncher does **not** request and does **not** access:
- Camera, microphone, or location
- Contacts, calendar, or SMS/call logs
- Storage files or media
- Biometric or health data
- Precise or approximate location

---

## 4. Data We Do Not Collect

To be unambiguous, we do **not** collect:

- Your name, email address, phone number, or any contact information
- Device identifiers (IMEI, Advertising ID, Android ID)
- IP addresses or network information
- Crash logs or diagnostic reports sent to us
- Behavioral analytics or event tracking
- Purchase transaction details (these are handled exclusively by Google Play)
- Location data of any kind

---

## 5. Purchase Data (Google Play)

EverLauncher is sold as a one-time paid app through the Google Play Store. The payment transaction, billing information, and purchase records are processed exclusively by **Google LLC** under Google's own Privacy Policy and Terms of Service. We receive only a confirmation from Google that a valid license has been purchased. We do not receive or store your payment card information, billing address, or any financial data.

For questions about your purchase data, please refer to: [Google Play Privacy Policy](https://policies.google.com/privacy)

---

## 6. Children's Privacy

EverLauncher is not directed at children under the age of 13 (or 16 in Quebec). We do not knowingly collect personal information from children. Because EverLauncher collects no personal information from any user at any age, there is no special risk to minors. Parents or guardians with concerns may contact us using the information in Section 10.

---

## 7. Data Security

Because all data remains on your device, its security is governed primarily by your device's own security features (screen lock, encryption). We recommend keeping your Android device updated and using a strong screen lock.

We have designed EverLauncher with the following safeguards:

- **No backup of user data:** The App is configured with `android:allowBackup="false"` and an explicit `dataExtractionRules` configuration that excludes all App data from Android cloud backup and device-to-device transfer.
- **No external storage:** All App data is stored in the App's private internal storage, inaccessible to other apps without root access.
- **No network stack:** The App contains no code that opens network sockets, makes HTTP requests, or communicates with external servers.

---

## 8. Data Retention and Deletion

All data is stored locally on your device. You control your data:

- **Delete specific data:** Use the App's Settings → Clear Data function (if available) or Android Settings → Apps → EverLauncher → Clear Data.
- **Delete all data:** Uninstalling EverLauncher from your device permanently and irreversibly deletes all App data from your device. We retain no copy of your data because we never receive it.

---

## 9. Third-Party Services

EverLauncher does not integrate with or transmit data to any third-party service other than:

- **Google Play Store:** For app distribution, license verification, and payment processing. Governed by Google's Privacy Policy.
- **System Browser:** If you tap Privacy Policy or Terms links within the App, your device opens those URLs in your default browser. No data from EverLauncher is transmitted to the browser beyond the URL itself.

---

## 10. Your Privacy Rights (PIPEDA / Provincial Laws)

Under applicable Canadian privacy legislation, you have the right to:

1. **Know** what personal information we hold about you — In our case, we hold none.
2. **Access** your personal information — Because we hold none, there is nothing to provide, but you can view all App data on-device.
3. **Correct** inaccurate information — All data is stored locally and you may clear it at any time.
4. **Withdraw consent** — You may uninstall the App at any time, which removes all App data from your device.
5. **Lodge a complaint** — You may complain to the Office of the Privacy Commissioner of Canada at [priv.gc.ca](https://www.priv.gc.ca) or your provincial privacy authority.

**Quebec residents (Law 25):** EverLauncher does not engage in profiling, automated decision-making affecting you, or cross-border data transfers. No Privacy Impact Assessment (PIA) is required because no personal information is collected.

To exercise any of these rights or to ask questions about this Policy, contact us at:

**Email:** [YOUR CONTACT EMAIL]
**Mailing Address:** [YOUR FULL ADDRESS, CITY, PROVINCE, POSTAL CODE, CANADA]

We will respond to privacy inquiries within **30 days**, as required by PIPEDA.

---

## 11. Cross-Border Data Transfers

We do not transfer personal information across borders because we do not collect personal information. All App data remains on your device, in the jurisdiction where your device is physically located.

---

## 12. Changes to This Policy

We may update this Privacy Policy from time to time. If we make material changes, we will update the "Last Updated" date at the top of this document and, where technically feasible, notify you through an in-app notice. We encourage you to review this Policy periodically.

Your continued use of the App after any changes to this Policy constitutes your acceptance of the updated Policy.

---

## 13. Contact Us

For any privacy questions, concerns, or requests:

**Developer:** Uxtra Creatives
**Email:** [YOUR CONTACT EMAIL]
**Address:** [YOUR FULL ADDRESS, CITY, PROVINCE, POSTAL CODE, CANADA]
**Website:** https://everlauncher.app

---

*This Privacy Policy was last reviewed by the Developer on April 15, 2026.*
