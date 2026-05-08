# Post-Publish Monitoring — First 48 Hours

## Immediate Checks (0–2 hours after publish)

- [ ] Play Console shows "In review" or "Available on Google Play"
- [ ] App is searchable by exact name "Ever Launcher" in the Play Store app
- [ ] Store listing renders correctly (screenshots, description, price)
- [ ] Purchase flow works end-to-end (buy → install → open)

## Hour 6 Check

- [ ] **Crashes & ANRs:** Go to Play Console > Quality > Crashes and ANRs — should be 0
- [ ] **Pre-launch report:** Review any accessibility or compatibility warnings
- [ ] **Rating prompt:** Confirm content rating displays correctly

## Day 1 Check (24 hours)

- [ ] **Installs:** Verify download counter is incrementing
- [ ] **Reviews:** Read first reviews; respond to any 1–2 star ratings within 24 hours
- [ ] **Revenue:** Check transaction reports in Play Console > Monetize
- [ ] **Device issues:** Look for any device-specific crash clusters (Samsung, Pixel, etc.)

## Day 2 Check (48 hours)

- [ ] **Retention:** Check 1-day retention if available (need Firebase or Play Console data)
- [ ] **Update readiness:** If any critical bug is found, prepare a 1.0.1 hotfix
- [ ] **Social monitoring:** Search Twitter/Reddit for "Ever Launcher" feedback

## Weekly Routine (Ongoing)

| Day | Action |
|-----|--------|
| Monday | Review crash/ANR dashboard |
| Wednesday | Check new reviews and respond |
| Friday | Review install/revenue trends |

## Red Flags — Act Immediately

| Symptom | Action |
|---------|--------|
| Crash rate > 1% | Pause rollout, investigate, prepare hotfix |
| ANR rate > 0.5% | Check launcher startup performance |
| Review average < 3.5 | Read all reviews, identify common complaint |
| Policy strike or warning | Read the full violation text, fix within deadline |
| Refund rate > 10% | Review onboarding flow and set-default-launcher screen |

## Escalation Contacts

- **Play Console Support:** In-app help > Contact us
- **Developer Advocate (Twitter):** @GooglePlayDev
- **Your lawyer:** For any legal threats or privacy complaints (reference `04-Legal-Documents/`)
