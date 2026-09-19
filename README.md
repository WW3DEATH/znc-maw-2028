# ZSP - 28: Zahira Science Portal

Official digital platform for **Zahira College Mawanella - Science Section** (Grade 12 Physical & Biological Science).

This repository contains both:
1. **The Native Android Application** (Kotlin, Jetpack Compose, Material 3, Room, Firebase RTDB)
2. **The Full Responsive Web Application** (Tailwind CSS, Vanilla JS, Firebase RTDB), pre-configured for instant **1-click deployment to Vercel**!

---

## 🚀 How to Host on Vercel (Step-by-Step)

You can host this entire web application on **Vercel** in under 2 minutes:

### Step 1: Push Repository to GitHub
- Export this project to your GitHub account (or git clone and push).

### Step 2: Import into Vercel
1. Go to [https://vercel.com](https://vercel.com) and log in.
2. Click **"Add New..."** ➔ **"Project"**.
3. Select your GitHub repository (`ZSP-28`).
4. **Framework Preset**: Select **"Other"**.
5. **Root Directory**: Leave as `./` (or select `public` if you only want the static web folder).
   - *Note: `vercel.json` and `package.json` in the root directory are already configured to serve the `public/` web directory automatically.*
6. Click **"Deploy"**!

### Step 3: Access your Live Website
Once deployed, Vercel gives you an instant domain like:
👉 `https://zsp-28.vercel.app`

---

## 🌟 Web Application Features

- 🏛️ **Zahira College Mawanella Branding**: College crest, Zahira Maroon (`#580A18`) and Gold (`#D4AF37`) themes.
- 🧪 **G.C.E. Advanced Level Syllabus Overview**: Combined Mathematics, Physics, Chemistry, and Biology unit breakdown.
- ⏱️ **Wednesday Evening Quizzes**: Interactive quiz interface simulating the real Wednesday 7:30 PM - 10:00 PM examination system.
- 🏆 **Top 10 Science Scholars Leaderboard**: Real-time ranking with SP rewards and honors badges.
- 🎁 **SP Redemption Center**: Students spend earned SP points to request A/L Past Papers, practical laboratory manuals, scientific calculators, and awards.
- 🛡️ **Admin Portal with Redemption Requests**:
  - Review live redemption requests with student names, emails, and academic streams.
  - Approve requests, hand over items, or reject with automatic SP refunds.
  - Student account verification and real-time chat moderation.
- ☁️ **Real-Time Cloud Synchronization**: Wired to Firebase Realtime Database project `e-learing-9adc3`.

---

## 📱 Android Native App

To build and run the Android app locally:
```bash
gradle :app:assembleDebug
```
The APK output is located at: `app/build/outputs/apk/debug/app-debug.apk`.
