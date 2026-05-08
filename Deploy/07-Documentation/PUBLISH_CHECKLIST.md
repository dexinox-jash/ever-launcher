# Ever Launcher — Google Play Store Publish Checklist

> **Version:** 1.0.0  
> **Target:** Google Play Store (Paid App — One-Time Purchase)  
> **Status:** Production Ready

---

## Pre-Upload Checklist

- [ ] All legal document placeholders filled in (see `04-Legal-Documents/LEGAL_CHECKLIST.md`)
- [ ] Keystore password changed from default (see `02-App-Signing/KEYSTORE_SETUP.md`)
- [ ] `signing.properties` updated with production credentials
- [ ] Release AAB (`01-Build-Artifacts/app-release.aab`) verified and under 150 MB
- [ ] Screenshots captured for phone and tablet (see `03-Store-Listing/`)
- [ ] Feature graphic created (1024×500 px)
- [ ] App icon uploaded (PNG, 512×512 px)

---

## Step 1: Create Play Console App Entry

1. Log in to [Google Play Console](https://play.google.com/console/developers/)
2. Click **Create app**
3. Fill in:
   - **App name:** Ever Launcher
   - **Default language:** English (Canada)
   - **App or game:** App
   - **Free or paid:** Paid
   - **Set price:** $X.XX CAD (set your price)
   - **Declarations:**
     - [ ] This app does not contain ads
     - [ ] This app meets Google Play's Developer Program Policies

---

## Step 2: Store Presence

### Main store listing
Paste the content from `03-Store-Listing/` into the corresponding fields:

| Field | Source File |
|-------|-------------|
| App name (50 chars) | `title.txt` |
| Short description (80 chars) | `short_description.txt` |
| Full description (4,000 chars) | `full_description.txt` |

### Graphics
Upload to **Main store listing > Graphics**:
- [ ] App icon: 512×512 px PNG (from `app/src/main/res/` or regenerate from source)
- [ ] Feature graphic: 1024×500 px PNG/JPG
- [ ] Phone screenshots: minimum 2, recommended 6–8 (1080×1920 or 1080×2400)
- [ ] 7-inch tablet screenshots: minimum 1
- [ ] 10-inch tablet screenshots: minimum 1

### Categorization
- **App category:** Personalization
- **Tags:** Launcher, Focus, Minimalist, Productivity, Digital Wellbeing

### Contact details
- **Email:** [YOUR CONTACT EMAIL]
- **Website:** https://dexinox-jash.github.io/ever-launcher/
- **Phone:** (optional)
- **Address:** [YOUR FULL ADDRESS, CITY, PROVINCE, POSTAL CODE, CANADA]

---

## Step 3: Upload Build

1. Navigate to **Production > Create new release**
2. Upload `Deploy/01-Build-Artifacts/app-release.aab`
3. Verify:
   - Version code: **1**
   - Version name: **1.0.0**
   - Min SDK: **26**
   - Target SDK: **35**
4. Add release notes from `07-Documentation/CHANGELOG.md`
5. Save and review

---

## Step 4: Content Rating

1. Go to **Content rating**
2. Select **Email app** as the closest category (or "Other" if not listed)
3. Answer the questionnaire honestly:
   - No violence
   - No sexual content
   - No profanity
   - No drug references
   - No in-app purchases (other than the one-time app purchase)
4. Submit for rating (typically instant)

**Expected rating:** ESRB E / PEGI 3 / Everyone

---

## Step 5: Targeting & Distribution

1. Go to **Production > Countries / regions**
2. Select **Canada** (start with one country for a soft launch)
3. Optionally add: United States, United Kingdom, Australia, New Zealand
4. Go to **Device catalog** and verify no unexpected exclusions

---

## Step 6: Data Safety Section

Complete the Data Safety form using `05-Permission-Justifications/DATA_SAFETY_FORM_answers.md`.

Summary of answers:
- **Does your app collect or share any user data?** No
- **Is all user data encrypted in transit?** No (no network communication)
- **Does your app provide a way for users to delete their data?** Yes (uninstall)
- **Types of data collected:** None
- **Purpose of data collection:** N/A
- **Sensitive permissions:**
  - `PACKAGE_USAGE_STATS` — app functionality (focus score)
  - `SCHEDULE_EXACT_ALARM` — app functionality (mode transitions)

---

## Step 7: Policy Declarations

### App access
- **Is your app restricted?** No (anyone can use it after purchase)

### Permissions
For each restricted permission, paste the justification from `05-Permission-Justifications/`:
- `PACKAGE_USAGE_STATS` — use `PACKAGE_USAGE_STATS_justification.md`
- `SCHEDULE_EXACT_ALARM` — use `SCHEDULE_EXACT_ALARM_justification.md`
- `<queries>` intent — use `queries_intent_justification.md`

### Ads
- [ ] This app does not contain ads

### Content guidelines
- [ ] The app complies with all Google Play policies

---

## Step 8: Legal Documents

In **App content > Privacy policy**, paste:
```
https://dexinox-jash.github.io/ever-launcher/privacy
```

Ensure the live pages are deployed and match `04-Legal-Documents/privacy_policy.md` exactly.

---

## Step 9: Pricing & Tax

1. Go to **Monetize > Product prices**
2. Set your price in each target currency
3. Review tax settings for each country
4. Confirm you have a valid payment profile in Play Console

---

## Step 10: Pre-Launch Report (Optional but Recommended)

1. Go to **Testing > Internal testing**
2. Create an internal testing track
3. Upload the same AAB
4. Add your own email as a tester
5. Wait for the pre-launch report (crawls the app automatically)
6. Review for crashes, ANRs, or accessibility issues

---

## Step 11: Final Review & Publish

Before clicking **Start rollout to Production**, verify:

- [ ] AAB uploaded and valid
- [ ] Store listing complete (name, description, graphics)
- [ ] Content rating received
- [ ] Data safety form complete
- [ ] Privacy policy URL live
- [ ] Pricing set in all target countries
- [ ] Target countries selected
- [ ] No policy warnings in the dashboard
- [ ] Release notes added

**Click "Start rollout to Production"**

Typical review time: 1–3 business days for a paid launcher app.

---

## Post-Publish (First 48 Hours)

See `07-Documentation/POST_PUBLISH_MONITORING.md` for the exact monitoring routine.

---

## Emergency Contacts & Resources

- **Google Play Developer Support:** [support.google.com/googleplay/android-developer](https://support.google.com/googleplay/android-developer)
- **Play Console Help:** [support.google.com/googleplay/android-developer/topic/9858052](https://support.google.com/googleplay/android-developer/topic/9858052)
- **App rejection appeal form:** Available in Play Console under Policy status
