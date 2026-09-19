/**
 * ZNC Science (Zahira National College Science Portal) Web Application
 * Connected to Firebase Realtime Database: e-learing-9adc3
 */

// User-provided Firebase Configuration
const firebaseConfig = {
  apiKey: "AIzaSyDDk_xiDvdcAv_B5oSogFH50azgLBVszKY",
  authDomain: "e-learing-9adc3.firebaseapp.com",
  databaseURL: "https://e-learing-9adc3-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "e-learing-9adc3",
  storageBucket: "e-learing-9adc3.firebasestorage.app",
  messagingSenderId: "349368530462",
  appId: "1:349368530462:web:f4cbd2e51995ff7860200f",
  measurementId: "G-RBESG4YNKM"
};

// Initialize Firebase SDK
let db = null;
try {
  if (typeof firebase !== "undefined") {
    if (!firebase.apps.length) {
      firebase.initializeApp(firebaseConfig);
    }
    if (firebase.database) {
      db = firebase.database();
      console.log("Firebase RTDB initialized successfully for e-learing-9adc3");
    }
  }
} catch (e) {
  console.warn("Firebase initialization note:", e);
}

// Global Application State
const STATE = {
  // Whenever the website is opened, all accounts start signed out by default
  currentUser: null,
  currentView: "home",
  adminSubTab: "users",
  adminRedemptionFilter: "All",
  activeChatStream: "Physical Science", // Current stream chat viewing

  // Selected unit for Wednesday Physics Quiz (Units 1 - 8 or 'all')
  selectedUnitId: "all",

  // Synchronized Wednesday Quiz State (Weekly Seed Engine)
  wednesdayConfig: (typeof getActiveWednesdayInfo === "function") ? getActiveWednesdayInfo() : {
    dateString: "2026-09-16",
    paperCode: "WED-20260916",
    seed: 84920194,
    formattedDate: "Wednesday, Sep 16, 2026"
  },
  adminPreviewWednesdayDate: null, // Allows admin to preview future Wednesdays

  // Registered Users (Supports Students with Streams & Teachers without stream restriction)
  users: [
    { id: "admin_jasim", fullName: "M.N.M. Jaasim", email: "mnmjaasim@gmail.com", role: "ADMIN", stream: "All Streams", spPoints: 450, isVerified: true }
  ],

  // Store Items (Admin can Add and Remove)
  redemptionItems: [
    {
      id: "item_1",
      title: "A/L Combined Mathematics 20-Year Classified Past Papers",
      description: "Complete Sri Lankan A/L past examination questions with step-by-step model schemes (English Medium).",
      category: "Past Papers",
      spPrice: 150,
      stock: 15
    },
    {
      id: "item_2",
      title: "A/L Biology Practical Manual & Color Anatomy Schemes",
      description: "NIE syllabus practical guidelines, diagram dissection handbooks, and laboratory experiment notes.",
      category: "Lab Manuals",
      spPrice: 120,
      stock: 12
    },
    {
      id: "item_3",
      title: "Texas Instruments TI-30XS Multiview Scientific Calculator",
      description: "High-precision examination calculator for Advanced Level Science problem sets.",
      category: "Equipment",
      spPrice: 300,
      stock: 5
    },
    {
      id: "item_4",
      title: "ZNC Science Scholar Lapel Badge & Certificate",
      description: "Official Zahira National College Science Section academic badge & faculty commendation certificate.",
      category: "Awards",
      spPrice: 80,
      stock: 25
    }
  ],

  redemptions: [],

  // Stream-segregated Discussions
  messages: [
    {
      id: "msg_1",
      stream: "Physical Science",
      senderId: "admin_jasim",
      senderName: "M.N.M. Jaasim",
      senderRole: "ADMIN",
      content: "Welcome to the Physical Science study group. Discussion on Combined Mathematics and Physics topics is open.",
      timestamp: Date.now() - 3600000
    },
    {
      id: "msg_2",
      stream: "Bio Science",
      senderId: "admin_jasim",
      senderName: "M.N.M. Jaasim",
      senderRole: "ADMIN",
      content: "Welcome to the Bio Science study group. Questions regarding Biology practicals and syllabus theory may be posted here.",
      timestamp: Date.now() - 1800000
    }
  ],

  // Active Quiz Runner State
  activeQuiz: null,
  activeQuizQuestionIdx: 0,
  selectedAnswerIdx: null,
  activeQuizScore: 0,
  quizTimerInterval: null,
  quizSecondsRemaining: 3600,

  // Gemini Multi-Turn AI Tutor State
  gemini: {
    model: "gemini-3.5-flash",
    role: "general",
    isLoading: false,
    history: []
  }
};

// INITIALIZATION
document.addEventListener("DOMContentLoaded", () => {
  initApp();
  setupFirebaseRealtime();
});

// Admin Privileges Helper: strictly restricts the Admin Panel & editing features
function isCurrentUserAdmin() {
  return !!(
    STATE.currentUser &&
    (STATE.currentUser.role === 'ADMIN' || (STATE.currentUser.email && STATE.currentUser.email.toLowerCase() === 'mnmjaasim@gmail.com'))
  );
}

function requireAdmin() {
  if (!isCurrentUserAdmin()) {
    showToast("🔒 Access Denied: Only the Administrator can access this section or edit items.");
    return false;
  }
  return true;
}

// Synchronize and persist users and active session to localStorage
function saveUsersAndSession() {
  try {
    if (STATE.currentUser) {
      const idx = STATE.users.findIndex(u => u.id === STATE.currentUser.id || (u.email && STATE.currentUser.email && u.email.toLowerCase() === STATE.currentUser.email.toLowerCase()));
      if (idx >= 0) {
        STATE.users[idx] = { ...STATE.users[idx], ...STATE.currentUser };
      } else {
        STATE.users.push(STATE.currentUser);
      }
    }
    // Persist all registered users
    localStorage.setItem("znc_registered_users", JSON.stringify(STATE.users));

    // Persist active user session if user kept signed in
    const isRemembered = localStorage.getItem("znc_current_user") || localStorage.getItem("zsp_current_user");
    if (isRemembered && STATE.currentUser) {
      localStorage.setItem("znc_current_user", JSON.stringify(STATE.currentUser));
    }
  } catch (e) {
    console.warn("Save users error:", e);
  }
}

function initApp() {
  // 1. Restore registered users from localStorage to keep balances and accounts persisted across refreshes
  try {
    const savedUsersStr = localStorage.getItem("znc_registered_users");
    if (savedUsersStr) {
      const parsedUsers = JSON.parse(savedUsersStr);
      if (Array.isArray(parsedUsers) && parsedUsers.length > 0) {
        parsedUsers.forEach(pu => {
          if (!pu || !pu.id) return;
          const idx = STATE.users.findIndex(u => u.id === pu.id || (u.email && pu.email && u.email.toLowerCase() === pu.email.toLowerCase()));
          if (idx >= 0) {
            STATE.users[idx] = { ...STATE.users[idx], ...pu };
          } else {
            STATE.users.push(pu);
          }
        });
      }
    }
  } catch (e) {
    console.warn("User registry restore error:", e);
  }

  // 2. Support "Keep me signed in" option: restore session only if saved by user choice
  try {
    const savedUserStr = localStorage.getItem("znc_current_user") || localStorage.getItem("zsp_current_user");
    if (savedUserStr) {
      const savedUser = JSON.parse(savedUserStr);
      if (savedUser && savedUser.id) {
        const found = STATE.users.find(u => u.id === savedUser.id || (u.email && savedUser.email && u.email.toLowerCase() === savedUser.email.toLowerCase()));
        if (found) {
          STATE.currentUser = found;
        } else {
          STATE.currentUser = savedUser;
          STATE.users.push(savedUser);
        }
      } else {
        STATE.currentUser = null;
      }
    } else {
      STATE.currentUser = null;
    }
  } catch (e) {
    STATE.currentUser = null;
  }

  renderAuthHeader();
  renderHomeSubjects();
  renderPhysicsUnitCards();
  checkWednesdayQuizWindow();
  renderLeaderboard();
  renderMilestones();
  renderRedemptionItems();
  renderUserRedemptionHistory();
  renderChatMessages();
  renderAdminUsers();
  renderAdminStoreInventory();
  renderAdminRedemptions();
  renderAdminChatAudit();
  renderWednesdayPaperInfo();
  updateBadgeCounts();
  initGeminiChat();

  // Periodic real-time check of Wednesday 8-10 PM window (every second for countdown)
  setInterval(checkWednesdayQuizWindow, 1000);
}

// ----------------------------------------------------
// ROUTING
// ----------------------------------------------------
function navigateTo(viewId) {
  // Strict guard: only administrator can open the Admin Panel
  if (viewId === "admin") {
    if (!isCurrentUserAdmin()) {
      showToast("🔒 Access Denied: Only the Administrator can access the Admin Panel.");
      if (!STATE.currentUser) {
        openLoginModal();
      } else {
        navigateTo("home");
      }
      return;
    }
  }

  // Profile guard: must be signed in
  if (viewId === "profile") {
    if (!STATE.currentUser) {
      showToast("Please sign in to view your profile.");
      openLoginModal();
      return;
    }
    renderProfileView();
  }

  STATE.currentView = viewId;
  document.querySelectorAll(".app-view").forEach(el => el.classList.add("hidden"));
  const target = document.getElementById("view-" + viewId);
  if (target) target.classList.remove("hidden");

  // Update nav tabs active style
  document.querySelectorAll(".nav-tab").forEach(btn => {
    btn.classList.remove("bg-maroon", "text-white", "border", "border-gold/30");
    btn.classList.add("text-slate-300");
  });
  const activeBtn = document.getElementById("tab-" + viewId);
  if (activeBtn) {
    activeBtn.classList.add("bg-maroon", "text-white", "border", "border-gold/30");
    activeBtn.classList.remove("text-slate-300");
  }

  // If navigating to Discussions, apply stream permissions
  if (viewId === "discussions") {
    setupDiscussionViewPermissions();
  }

  // If navigating to Gemini AI Tutor, render chat and focus input
  if (viewId === "gemini") {
    renderGeminiChat();
    setTimeout(() => {
      const input = document.getElementById("geminiChatInput");
      if (input) input.focus();
    }, 150);
  }

  window.scrollTo({ top: 0, behavior: "smooth" });
}

// ----------------------------------------------------
// AUTH & HEADER CONTROLS
// ----------------------------------------------------
function renderAuthHeader() {
  const container = document.getElementById("authHeaderControls");
  const adminNavTab = document.getElementById("tab-admin");

  // Only show the Admin Portal nav tab if the current user is an Admin
  if (adminNavTab) {
    adminNavTab.classList.toggle("hidden", !isCurrentUserAdmin());
  }

  if (!container) return;

  if (STATE.currentUser) {
    const streamTag = (STATE.currentUser.role === 'STUDENT') 
      ? ` • ${STATE.currentUser.stream}` 
      : ` • ${STATE.currentUser.role}`;

    container.innerHTML = `
      <div class="relative">
        <button onclick="toggleUserDropdown()" id="userProfileBtn" class="flex items-center space-x-2 bg-maroon-dark hover:bg-maroon-dark/80 px-3 py-1.5 rounded-xl border border-gold/30 transition shadow-sm">
          <div class="w-7 h-7 rounded-full bg-gold text-slate-900 font-black flex items-center justify-center text-xs shadow-inner">
            ${STATE.currentUser.fullName.charAt(0)}
          </div>
          <div class="hidden sm:block text-left">
            <p class="text-xs font-bold text-white leading-tight">${STATE.currentUser.fullName}</p>
            <p class="text-[10px] text-gold">${STATE.currentUser.role} ${streamTag} • ${STATE.currentUser.spPoints} SP</p>
          </div>
          <span class="material-symbols-outlined text-[16px] text-gold/80">expand_more</span>
        </button>

        <!-- Dropdown Menu -->
        <div id="userDropdownMenu" class="hidden absolute right-0 mt-2 w-56 bg-white rounded-xl shadow-2xl border border-slate-200 py-1.5 z-50 text-slate-800">
          <div class="px-4 py-2 border-b border-slate-100">
            <p class="text-xs font-bold text-slate-900 truncate">${STATE.currentUser.fullName}</p>
            <p class="text-[11px] text-slate-500 truncate">${STATE.currentUser.email}</p>
            <div class="flex items-center space-x-1 mt-1">
              <span class="text-[10px] font-extrabold uppercase px-2 py-0.5 rounded ${isCurrentUserAdmin() ? 'bg-maroon text-gold font-black' : 'bg-maroon/10 text-maroon'}">${STATE.currentUser.role}</span>
              ${STATE.currentUser.role === 'STUDENT' ? `
                <span class="text-[10px] font-bold px-2 py-0.5 rounded bg-blue-50 text-blue-700 border border-blue-200">${STATE.currentUser.stream}</span>
              ` : `
                <span class="text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-50 text-emerald-700 border border-emerald-200">Staff Access</span>
              `}
            </div>
          </div>
          <button onclick="closeUserDropdown(); navigateTo('profile');" class="w-full px-4 py-2 text-left text-xs font-semibold text-slate-700 hover:bg-slate-50 flex items-center space-x-2">
            <span class="material-symbols-outlined text-[18px] text-slate-500">account_circle</span>
            <span>My Profile</span>
          </button>
          ${isCurrentUserAdmin() ? `
            <button onclick="closeUserDropdown(); navigateTo('admin');" class="w-full px-4 py-2 text-left text-xs font-bold text-maroon hover:bg-maroon/5 flex items-center space-x-2">
              <span class="material-symbols-outlined text-[18px] text-maroon">shield_person</span>
              <span>Administrator Panel</span>
            </button>
          ` : ''}
          <button onclick="handleLogout()" class="w-full px-4 py-2 text-left text-xs font-semibold text-red-600 hover:bg-red-50 flex items-center space-x-2 border-t border-slate-100 mt-1">
            <span class="material-symbols-outlined text-[18px] text-red-500">logout</span>
            <span>Log Out</span>
          </button>
        </div>
      </div>
    `;

    // Update Home displays
    const homeStream = document.getElementById("homeUserStreamDisplay");
    if (homeStream) {
      homeStream.innerText = (STATE.currentUser.role === 'STUDENT') ? STATE.currentUser.stream : "All-Access (Staff)";
    }
    const homeSp = document.getElementById("homeUserSp");
    if (homeSp) homeSp.innerText = `${STATE.currentUser.spPoints} SP`;

    const milestone = getMilestoneForSp(STATE.currentUser.spPoints);
    const homeLevel = document.getElementById("homeUserLevelDisplay");
    if (homeLevel) {
      homeLevel.innerText = `${milestone.current.badge} Level ${milestone.current.level} ${milestone.current.name}`;
    }

    const redSp = document.getElementById("redemptionUserPoints");
    if (redSp) redSp.innerText = `${STATE.currentUser.spPoints} SP`;

  } else {
    // Logged Out State
    container.innerHTML = `
      <button onclick="openLoginModal()" class="flex items-center space-x-1.5 bg-gold hover:bg-gold-dark text-slate-900 font-bold px-4 py-1.5 rounded-xl text-xs transition shadow-sm">
        <span class="material-symbols-outlined text-[18px]">login</span>
        <span>Sign In</span>
      </button>
    `;

    const homeStream = document.getElementById("homeUserStreamDisplay");
    if (homeStream) homeStream.innerText = "Guest (Sign In)";
    const homeSp = document.getElementById("homeUserSp");
    if (homeSp) homeSp.innerText = "0 SP";
    const homeLevel = document.getElementById("homeUserLevelDisplay");
    if (homeLevel) homeLevel.innerText = "Sign in to track";
    const redSp = document.getElementById("redemptionUserPoints");
    if (redSp) redSp.innerText = "0 SP";
  }
}

function toggleUserDropdown() {
  const menu = document.getElementById("userDropdownMenu");
  if (menu) menu.classList.toggle("hidden");
}

function closeUserDropdown() {
  const menu = document.getElementById("userDropdownMenu");
  if (menu) menu.classList.add("hidden");
}

window.addEventListener("click", (e) => {
  const btn = document.getElementById("userProfileBtn");
  const menu = document.getElementById("userDropdownMenu");
  if (btn && menu && !btn.contains(e.target) && !menu.contains(e.target)) {
    menu.classList.add("hidden");
  }
});

// ----------------------------------------------------
// AUTHENTICATION MODAL (LOGIN & REGISTRATION)
// ----------------------------------------------------
let authCurrentMode = 'signin'; // 'signin' or 'signup'

function openLoginModal(mode = 'signin') {
  setAuthMode(mode);
  const modal = document.getElementById("loginModal");
  if (modal) modal.classList.remove("hidden");
}

function closeLoginModal() {
  const modal = document.getElementById("loginModal");
  if (modal) modal.classList.add("hidden");
}

function setAuthMode(mode) {
  authCurrentMode = mode;
  const title = document.getElementById("authModalTitle");
  const btnSubmit = document.getElementById("btnAuthSubmit");
  const btnTabSignIn = document.getElementById("btnAuthTabSignIn");
  const btnTabSignUp = document.getElementById("btnAuthTabSignUp");
  const fieldName = document.getElementById("fieldFullName");
  const fieldRole = document.getElementById("fieldRoleSelection");
  const fieldStream = document.getElementById("fieldStreamSelection");

  if (mode === 'signin') {
    if (title) title.innerText = "Sign In to ZSP - 28";
    if (btnSubmit) btnSubmit.innerText = "Sign In to Portal";
    btnTabSignIn.className = "flex-1 py-1.5 text-xs font-bold rounded-lg bg-white text-slate-800 shadow-sm";
    btnTabSignUp.className = "flex-1 py-1.5 text-xs font-bold rounded-lg text-slate-600";
    if (fieldName) fieldName.classList.add("hidden");
    if (fieldRole) fieldRole.classList.add("hidden");
    if (fieldStream) fieldStream.classList.add("hidden");
  } else {
    if (title) title.innerText = "Create New Account";
    if (btnSubmit) btnSubmit.innerText = "Create & Register Account";
    btnTabSignUp.className = "flex-1 py-1.5 text-xs font-bold rounded-lg bg-white text-slate-800 shadow-sm";
    btnTabSignIn.className = "flex-1 py-1.5 text-xs font-bold rounded-lg text-slate-600";
    if (fieldName) fieldName.classList.remove("hidden");
    if (fieldRole) fieldRole.classList.remove("hidden");
    onRoleChanged(); // will show/hide stream options based on selected role
  }
}

// When creating an account: if Student -> show ONLY Physical Science & Bio Science. If Teacher -> hide stream.
function onRoleChanged() {
  const roleRadios = document.getElementsByName("authRole");
  let selectedRole = "STUDENT";
  for (let r of roleRadios) {
    if (r.checked) selectedRole = r.value;
  }

  const fieldStream = document.getElementById("fieldStreamSelection");
  if (fieldStream) {
    if (selectedRole === "STUDENT") {
      fieldStream.classList.remove("hidden");
    } else {
      fieldStream.classList.add("hidden");
    }
  }
}

function handleAuthSubmit(e) {
  e.preventDefault();
  const email = document.getElementById("loginEmail").value.trim().toLowerCase();
  const password = document.getElementById("loginPassword").value;

  if (authCurrentMode === 'signin') {
    // Check credentials against state
    let matchedUser = STATE.users.find(u => u.email.toLowerCase() === email);

    // Strict Admin Authentication: only authorized admin email with proper password gets ADMIN privileges
    if (email === "mnmjaasim@gmail.com") {
      if (password !== "mnmjaasim2010" && password !== "admin123") {
        showToast("⚠️ Incorrect Administrator Password. Please enter the correct admin password.");
        return;
      }
      if (!matchedUser) {
        matchedUser = {
          id: "admin_jasim",
          fullName: "M.N.M. Jaasim",
          email: "mnmjaasim@gmail.com",
          role: "ADMIN",
          stream: "All Streams",
          spPoints: 450,
          weeklySp: 0,
          isVerified: true
        };
        STATE.users.unshift(matchedUser);
      } else {
        matchedUser.role = "ADMIN";
        matchedUser.isVerified = true;
      }
    } else {
      // General non-admin user login
      if (!matchedUser) {
        matchedUser = {
          id: "usr_" + Math.random().toString(36).substring(2, 7),
          fullName: email.split("@")[0].toUpperCase(),
          email: email,
          role: "STUDENT",
          stream: "Physical Science",
          spPoints: 50,
          weeklySp: 0,
          isVerified: true
        };
        STATE.users.push(matchedUser);
      } else if (matchedUser.role === 'ADMIN' && matchedUser.email !== 'mnmjaasim@gmail.com') {
        matchedUser.role = 'TEACHER'; // Prevent unauthorized admin roles
      }
    }

    STATE.currentUser = matchedUser;

    // Handle "Keep me signed in" preference
    const keepSignedIn = document.getElementById("authKeepSignedIn")?.checked;
    try {
      if (keepSignedIn) {
        localStorage.setItem("znc_current_user", JSON.stringify(matchedUser));
      } else {
        localStorage.removeItem("znc_current_user");
        localStorage.removeItem("zsp_current_user");
      }
    } catch (e) {}

    saveUsersAndSession();

    closeLoginModal();
    renderAuthHeader();
    renderHomeSubjects();
    renderChatMessages();
    renderLeaderboard();
    showToast(`Welcome back, ${matchedUser.fullName}! (${matchedUser.role}${matchedUser.role === 'STUDENT' ? ' - ' + matchedUser.stream : ''})`);

  } else {
    // SIGN UP FLOW
    const fullName = document.getElementById("authFullName").value.trim();
    if (!fullName) {
      showToast("Please enter your Full Name.");
      return;
    }

    const roleRadios = document.getElementsByName("authRole");
    let role = "STUDENT";
    for (let r of roleRadios) {
      if (r.checked) role = r.value;
    }

    // Role safety: Only mnmjaasim@gmail.com can ever have ADMIN role
    if (email === "mnmjaasim@gmail.com") {
      role = "ADMIN";
    } else if (role === "ADMIN") {
      role = "STUDENT";
    }

    let stream = "All Streams";
    if (role === "STUDENT") {
      const streamRadios = document.getElementsByName("authStream");
      for (let s of streamRadios) {
        if (s.checked) stream = s.value;
      }
    }

    const newUser = {
      id: (role === "ADMIN" ? "admin_" : role === "TEACHER" ? "teacher_" : "student_") + Math.random().toString(36).substring(2, 7),
      fullName: fullName,
      email: email,
      role: role,
      stream: stream,
      spPoints: (role === "ADMIN" ? 450 : role === "TEACHER" ? 100 : 50),
      weeklySp: 0,
      isVerified: (role === "ADMIN" || role === "TEACHER")
    };

    STATE.users.push(newUser);
    STATE.currentUser = newUser;

    // Handle "Keep me signed in" preference for sign up
    const keepSignedIn = document.getElementById("authKeepSignedIn")?.checked;
    try {
      if (keepSignedIn) {
        localStorage.setItem("znc_current_user", JSON.stringify(newUser));
      } else {
        localStorage.removeItem("znc_current_user");
        localStorage.removeItem("zsp_current_user");
      }
    } catch (e) {}

    saveUsersAndSession();

    // Sync user to Firebase
    if (db) {
      try {
        db.ref("users/" + newUser.id).set(newUser);
      } catch (err) {
        console.warn("RTDB user sync note:", err);
      }
    }

    closeLoginModal();
    renderAuthHeader();
    renderHomeSubjects();
    renderChatMessages();
    renderLeaderboard();
    renderAdminUsers();
    showToast(`🎉 Account registered as ${role}! Enrolled stream: ${stream}`);
  }
}

function handleLogout() {
  closeUserDropdown();
  STATE.currentUser = null;
  try {
    localStorage.removeItem("znc_current_user");
    localStorage.removeItem("zsp_current_user");
    sessionStorage.clear();
  } catch (e) {}
  renderAuthHeader();
  renderChatMessages();
  renderUserRedemptionHistory();
  renderHomeSubjects();
  navigateTo("home");
  showToast("You have been signed out of Zahira Science Portal.");
}

// ----------------------------------------------------
// PROFILE VIEW
// ----------------------------------------------------
function renderProfileView() {
  if (!STATE.currentUser) {
    openLoginModal();
    return;
  }

  const nameEl = document.getElementById("profileFullName");
  const avatarEl = document.getElementById("profileAvatar");
  const emailEl = document.getElementById("profileEmail");
  const roleBadge = document.getElementById("profileRoleBadge");
  const accountType = document.getElementById("profileAccountType");
  const streamEl = document.getElementById("profileStream");
  const spEl = document.getElementById("profileSpPoints");
  const milestoneEl = document.getElementById("profileMilestoneDisplay");

  if (nameEl) nameEl.innerText = STATE.currentUser.fullName;
  if (avatarEl) avatarEl.innerText = STATE.currentUser.fullName.charAt(0);
  if (emailEl) emailEl.innerText = STATE.currentUser.email;
  if (roleBadge) roleBadge.innerText = STATE.currentUser.role;
  if (accountType) accountType.innerText = STATE.currentUser.role === 'STUDENT' ? 'G.C.E. A/L Science Student' : 'Teacher / Administrator';
  if (streamEl) streamEl.innerText = STATE.currentUser.role === 'STUDENT' ? STATE.currentUser.stream : 'All Streams (Full Access)';
  if (spEl) spEl.innerText = `${STATE.currentUser.spPoints} SP`;

  const m = getMilestoneForSp(STATE.currentUser.spPoints);
  if (milestoneEl) milestoneEl.innerText = `${m.current.badge} Level ${m.current.level} ${m.current.name}`;
}

// ----------------------------------------------------
// HOME SUBJECTS (FILTERED ACCORDING TO STUDENT STREAM)
// ----------------------------------------------------
function renderHomeSubjects() {
  const container = document.getElementById("subjectCardsContainer");
  if (!container) return;

  const currentStream = (STATE.currentUser && STATE.currentUser.role === 'STUDENT') 
    ? STATE.currentUser.stream 
    : "All";

  // National syllabus subjects
  const allSubjects = [
    { name: "Combined Mathematics", stream: "Physical Science", units: "8 Units Covered", icon: "calculate", color: "bg-blue-50 text-blue-700 border-blue-200" },
    { name: "Physics", stream: "Both", units: "8 Comprehensive Units (100 Q each)", icon: "bolt", color: "bg-amber-50 text-amber-700 border-amber-200" },
    { name: "Chemistry", stream: "Both", units: "10 Units Covered", icon: "science", color: "bg-emerald-50 text-emerald-700 border-emerald-200" },
    { name: "Biology", stream: "Bio Science", units: "9 Units Covered", icon: "psychology", color: "bg-purple-50 text-purple-700 border-purple-200" }
  ];

  // Filter based on user's enrolled stream
  const filtered = allSubjects.filter(s => {
    if (currentStream === "All") return true;
    if (s.stream === "Both") return true;
    return s.stream === currentStream;
  });

  container.innerHTML = filtered.map(s => `
    <div class="p-4 rounded-xl border ${s.color} space-y-2 flex flex-col justify-between">
      <div>
        <div class="flex items-center space-x-2">
          <span class="material-symbols-outlined">${s.icon}</span>
          <h3 class="font-bold text-sm">${s.name}</h3>
        </div>
        <p class="text-xs opacity-80 mt-1">${s.units}</p>
        <span class="inline-block mt-1 text-[10px] font-semibold px-2 py-0.5 rounded bg-white/60">
          Stream: ${s.stream === 'Both' ? 'Physical & Bio' : s.stream}
        </span>
      </div>
      <button onclick="navigateTo('quiz')" class="text-[11px] font-bold underline hover:opacity-75 pt-2 text-left">
        Take Wednesday Quiz →
      </button>
    </div>
  `).join("");
}

// ----------------------------------------------------
// WEDNESDAY 8:00 PM – 10:00 PM COMPETITIVE QUIZ ENGINE
// ----------------------------------------------------
// Returns Sri Lanka time (Asia/Colombo) or local time
function getSriLankaTime() {
  try {
    const slDateStr = new Date().toLocaleString("en-US", { timeZone: "Asia/Colombo" });
    return new Date(slDateStr);
  } catch (e) {
    return new Date();
  }
}

// Checks if current time is Wednesday between 20:00 (8:00 PM) and 22:00 (10:00 PM) only
function isWednesdayQuizWindowOpen() {
  const now = getSriLankaTime();
  const day = now.getDay(); // 3 = Wednesday
  const hour = now.getHours(); // 0 to 23

  // Automatically start at 8:00 PM (20:00) and end at 10:00 PM (22:00) strictly on Wednesday
  return (day === 3 && hour >= 20 && hour < 22);
}

// Calculates exact countdown timing for the Wednesday examination
function getWednesdayCountdownInfo() {
  const now = getSriLankaTime();
  const day = now.getDay();
  const hour = now.getHours();

  if (day === 3 && hour >= 20 && hour < 22) {
    // Currently live! Count down until 10:00 PM close
    const closeTime = new Date(now);
    closeTime.setHours(22, 0, 0, 0);
    const diffMs = Math.max(0, closeTime.getTime() - now.getTime());
    const hours = Math.floor(diffMs / 3600000);
    const mins = Math.floor((diffMs % 3600000) / 60000);
    const secs = Math.floor((diffMs % 60000) / 1000);
    return {
      isOpen: true,
      text: `Closes in ${String(hours).padStart(2, '0')}h ${String(mins).padStart(2, '0')}m ${String(secs).padStart(2, '0')}s`
    };
  } else {
    // Locked. Calculate time until next Wednesday 8:00 PM
    const nextWed = new Date(now);
    let daysToAdd = (3 - day + 7) % 7;
    if (day === 3 && hour >= 22) {
      daysToAdd = 7;
    } else if (day === 3 && hour < 20) {
      daysToAdd = 0;
    } else if (daysToAdd === 0) {
      daysToAdd = 7;
    }
    nextWed.setDate(now.getDate() + daysToAdd);
    nextWed.setHours(20, 0, 0, 0);

    const diffMs = Math.max(0, nextWed.getTime() - now.getTime());
    const days = Math.floor(diffMs / (24 * 3600000));
    const hours = Math.floor((diffMs % (24 * 3600000)) / 3600000);
    const mins = Math.floor((diffMs % 3600000) / 60000);
    const secs = Math.floor((diffMs % 60000) / 1000);

    let text = "";
    if (days > 0) {
      text = `Opens in ${days}d ${hours}h ${mins}m`;
    } else {
      text = `Opens in ${String(hours).padStart(2, '0')}h ${String(mins).padStart(2, '0')}m ${String(secs).padStart(2, '0')}s`;
    }

    return {
      isOpen: false,
      text: text
    };
  }
}

function checkWednesdayQuizWindow() {
  const statusEl = document.getElementById("wednesdayWindowStatusText");
  const liveBadge = document.getElementById("quizWindowLiveBadge");
  const countdownText = document.getElementById("wednesdayCountdownText");
  const countdownBadge = document.getElementById("wednesdayCountdownBadge");
  const launchBtn = document.getElementById("btnLaunchMainQuiz");

  const countdown = getWednesdayCountdownInfo();
  const isOpen = countdown.isOpen;

  if (isOpen) {
    if (statusEl) {
      statusEl.innerHTML = `<span class="text-emerald-600 font-extrabold flex items-center justify-end gap-1.5">
        <span class="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-ping"></span>
        LIVE NOW (8:00 PM – 10:00 PM)
      </span>`;
    }
    if (liveBadge) {
      liveBadge.className = "px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800 border border-emerald-300 flex items-center space-x-1";
      liveBadge.innerHTML = `<span class="material-symbols-outlined text-[14px]">bolt</span><span>LIVE: Wednesday Examination Open!</span>`;
    }
    if (countdownBadge) {
      countdownBadge.className = "text-[11px] font-mono font-bold px-2 py-0.5 rounded bg-emerald-100 text-emerald-800 border border-emerald-300 flex items-center gap-1";
    }
    if (countdownText) {
      countdownText.innerText = countdown.text;
    }
    if (launchBtn) {
      launchBtn.classList.remove("opacity-60");
      launchBtn.innerHTML = `<span class="material-symbols-outlined text-[18px]">play_circle</span><span>Start Examination (100 Questions)</span>`;
    }
  } else {
    if (statusEl) {
      statusEl.innerHTML = `<span class="text-slate-600 font-medium flex items-center justify-end gap-1">
        <span class="material-symbols-outlined text-[14px] text-slate-400">lock</span>
        Locked (Opens Wed 8:00 PM)
      </span>`;
    }
    if (liveBadge) {
      liveBadge.className = "px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-100 text-amber-800 border border-amber-300 flex items-center space-x-1";
      liveBadge.innerHTML = `<span class="material-symbols-outlined text-[14px]">schedule</span><span>Opens Wednesday 8:00 PM – 10:00 PM</span>`;
    }
    if (countdownBadge) {
      countdownBadge.className = "text-[11px] font-mono font-bold px-2 py-0.5 rounded bg-slate-200 text-slate-700 flex items-center gap-1";
    }
    if (countdownText) {
      countdownText.innerText = countdown.text;
    }
    if (launchBtn) {
      launchBtn.innerHTML = `<span class="material-symbols-outlined text-[18px]">lock</span><span>Opens Wednesday 8:00 PM – 10:00 PM</span>`;
    }
  }
}

// Render the All Units Grand Paper and 8 Units selector (Each has 100 questions)
function renderPhysicsUnitCards() {
  const container = document.getElementById("physicsUnitsGrid");
  if (!container || typeof PHYSICS_UNITS === "undefined") return;

  container.innerHTML = PHYSICS_UNITS.map(u => {
    const isSelected = String(STATE.selectedUnitId) === String(u.id);
    const isGrand = u.id === "all" || u.isGrand;

    if (isGrand) {
      return `
        <div onclick="selectPhysicsUnit('${u.id}')" id="unitCard_${u.id}" class="sm:col-span-2 lg:col-span-4 p-4 rounded-2xl border-2 cursor-pointer transition flex flex-col md:flex-row md:items-center justify-between gap-3 shadow-sm ${isSelected ? 'border-maroon bg-gradient-to-r from-maroon/10 via-gold/10 to-amber-500/10 ring-2 ring-maroon/30' : 'border-gold/60 bg-gradient-to-r from-amber-500/5 to-white hover:border-gold'}">
          <div class="space-y-1">
            <div class="flex flex-wrap items-center gap-2">
              <span class="text-[10px] font-black uppercase tracking-wider px-2.5 py-0.5 rounded bg-maroon text-gold">OFFICIAL NATIONAL PAPER</span>
              <span class="text-[10px] font-bold px-2 py-0.5 rounded bg-amber-100 text-amber-900 border border-amber-300">100 Questions (All 8 Units Mixed)</span>
              <span class="text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-100 text-emerald-800 flex items-center gap-1">
                <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> Identical for All Students
              </span>
            </div>
            <h4 class="font-black text-sm text-slate-900">${u.name}</h4>
            <p class="text-xs text-slate-600">${u.desc}</p>
          </div>
          <div class="shrink-0 text-right">
            <span class="inline-block px-4 py-2 rounded-xl text-xs font-bold transition ${isSelected ? 'bg-maroon text-white shadow' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'}">
              ${isSelected ? '✓ Selected Paper' : 'Select Grand Paper'}
            </span>
          </div>
        </div>
      `;
    }

    return `
      <div onclick="selectPhysicsUnit('${u.id}')" id="unitCard_${u.id}" class="p-3.5 rounded-xl border cursor-pointer transition flex flex-col justify-between ${isSelected ? 'border-maroon bg-maroon/5 ring-2 ring-maroon/30' : 'border-slate-200 bg-white hover:border-slate-300'}">
        <div>
          <div class="flex items-center justify-between">
            <span class="text-[11px] font-black uppercase text-maroon">Unit ${u.id}</span>
            <span class="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-700">100 Questions</span>
          </div>
          <h4 class="font-bold text-xs text-slate-900 mt-1">${u.name.replace(`Unit ${u.id}: `, '')}</h4>
          <p class="text-[11px] text-slate-500 line-clamp-2 mt-1">${u.desc}</p>
        </div>
        <div class="mt-2 text-right">
          <span class="text-[11px] font-bold ${isSelected ? 'text-maroon' : 'text-slate-400'}">
            ${isSelected ? '● Selected' : 'Select Unit'}
          </span>
        </div>
      </div>
    `;
  }).join("");
}

function selectPhysicsUnit(unitId) {
  STATE.selectedUnitId = unitId;
  renderPhysicsUnitCards();

  const unit = PHYSICS_UNITS.find(u => String(u.id) === String(unitId));
  const label = document.getElementById("selectedUnitLabel");
  if (label && unit) {
    label.innerText = `${unit.name} (100 Questions)`;
  }
}

function startSelectedUnitQuiz() {
  if (!STATE.currentUser) {
    showToast("Please sign in with your account to sit the Wednesday examination.");
    openLoginModal();
    return;
  }

  // Strictly enforce Wednesday 8:00 PM – 10:00 PM examination window
  if (!isWednesdayQuizWindowOpen()) {
    const countdown = getWednesdayCountdownInfo();
    showToast(`🔒 Examination Locked: The Wednesday Main Competitive Physics Quiz opens strictly every Wednesday from 8:00 PM to 10:00 PM (${countdown.text}).`);
    return;
  }

  const weeklySeed = (STATE.wednesdayConfig && STATE.wednesdayConfig.seed) ? STATE.wednesdayConfig.seed : 84920194;
  
  // Deterministically generate/shuffle 100 questions for this Wednesday using the weekly seed
  const questions = (typeof getSynchronizedWednesdayQuestions === "function")
    ? getSynchronizedWednesdayQuestions(STATE.selectedUnitId, weeklySeed)
    : (ALL_PHYSICS_QUESTIONS[STATE.selectedUnitId] || []);

  if (!questions || questions.length === 0) {
    showToast("Loading unit questions...");
    return;
  }

  const unit = PHYSICS_UNITS.find(u => String(u.id) === String(STATE.selectedUnitId)) || { name: `Unit ${STATE.selectedUnitId}` };

  STATE.activeQuiz = {
    unitId: STATE.selectedUnitId,
    unitTitle: unit.name,
    questions: questions,
    rewardSp: 100, // 100 SP for completing 100 questions
    paperCode: STATE.wednesdayConfig.paperCode || `WED-${weeklySeed}`,
    seed: weeklySeed
  };

  STATE.activeQuizQuestionIdx = 0;
  STATE.selectedAnswerIdx = null;
  STATE.activeQuizScore = 0;
  STATE.quizSecondsRemaining = 3600; // 60 minutes

  // Open active runner
  const runner = document.getElementById("activeQuizRunner");
  if (runner) runner.classList.remove("hidden");

  const runnerPaperEl = document.getElementById("quizRunnerPaperCode");
  if (runnerPaperEl) runnerPaperEl.innerText = STATE.activeQuiz.paperCode;

  renderCurrentPhysicsQuestion();
  startQuizTimer();
  runner.scrollIntoView({ behavior: "smooth" });
  showToast(`Exam Started: ${unit.name} (Synchronized Paper ${STATE.activeQuiz.paperCode})`);
}

function renderCurrentPhysicsQuestion() {
  const q = STATE.activeQuiz.questions[STATE.activeQuizQuestionIdx];
  const unit = PHYSICS_UNITS.find(u => u.id === STATE.activeQuiz.unitId);

  document.getElementById("quizSubjectTag").innerText = unit ? unit.name : "Physics";
  document.getElementById("quizCurrentTitle").innerText = `Question ${STATE.activeQuizQuestionIdx + 1} of 100`;
  document.getElementById("quizProgressCount").innerText = `${STATE.activeQuizQuestionIdx + 1} / 100`;
  document.getElementById("quizQuestionText").innerText = q.q;

  const container = document.getElementById("quizOptionsContainer");
  container.innerHTML = q.options.map((opt, i) => `
    <label class="flex items-center space-x-3 p-3 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 cursor-pointer transition">
      <input type="radio" name="activeQuizOption" value="${i}" onchange="STATE.selectedAnswerIdx = ${i}" class="text-maroon focus:ring-maroon" />
      <span class="text-xs sm:text-sm font-medium text-slate-800">${opt}</span>
    </label>
  `).join("");
}

function submitQuizAnswer() {
  if (STATE.selectedAnswerIdx === null) {
    showToast("Please select an answer choice before submitting.");
    return;
  }

  const currentQ = STATE.activeQuiz.questions[STATE.activeQuizQuestionIdx];
  if (STATE.selectedAnswerIdx === currentQ.correctIndex) {
    STATE.activeQuizScore++;
  }

  // Advance to next question or complete
  STATE.activeQuizQuestionIdx++;
  if (STATE.activeQuizQuestionIdx < STATE.activeQuiz.questions.length) {
    STATE.selectedAnswerIdx = null;
    renderCurrentPhysicsQuestion();
  } else {
    finishQuizAndAwardMilestones();
  }
}

function finishQuizAndAwardMilestones() {
  clearInterval(STATE.quizTimerInterval);

  const totalQ = STATE.activeQuiz.questions.length;
  const score = STATE.activeQuizScore;
  const earnedSp = Math.round((score / totalQ) * 100);

  // Award SP points to lifetime balance
  STATE.currentUser.spPoints = (Number(STATE.currentUser.spPoints) || 0) + earnedSp;

  // Track Weekly SP points for the active exam week (Wednesday cycle)
  const activeWeekKey = (STATE.wednesdayConfig && STATE.wednesdayConfig.paperCode) 
    ? STATE.wednesdayConfig.paperCode 
    : "WED-20260916";
  if (!STATE.currentUser.weeklyScores) {
    STATE.currentUser.weeklyScores = {};
  }
  STATE.currentUser.weeklyScores[activeWeekKey] = (Number(STATE.currentUser.weeklyScores[activeWeekKey]) || 0) + earnedSp;
  STATE.currentUser.weeklySp = STATE.currentUser.weeklyScores[activeWeekKey];

  // Persist user points & weekly scores so they never revert on page refresh!
  saveUsersAndSession();

  // Check milestone level up
  const milestone = getMilestoneForSp(STATE.currentUser.spPoints);

  // Sync score and SP to Firebase
  if (db) {
    try {
      db.ref("quiz_submissions/" + Date.now()).set({
        userId: STATE.currentUser.id,
        userName: STATE.currentUser.fullName,
        unitId: STATE.activeQuiz.unitId,
        paperCode: STATE.activeQuiz.paperCode,
        score: score,
        total: totalQ,
        earnedSp: earnedSp,
        timestamp: Date.now()
      });
      db.ref("users/" + STATE.currentUser.id + "/spPoints").set(STATE.currentUser.spPoints);
      db.ref("users/" + STATE.currentUser.id + "/weeklySp").set(STATE.currentUser.weeklySp);
      db.ref("users/" + STATE.currentUser.id + "/weeklyScores/" + activeWeekKey).set(STATE.currentUser.weeklySp);
    } catch (e) {
      console.warn("RTDB quiz sync error:", e);
    }
  }

  renderAuthHeader();
  renderLeaderboard();
  renderMilestones();

  showToast(`🎉 Exam Completed! Score: ${score}/${totalQ}. You earned +${earnedSp} SP! (Weekly Total: ${STATE.currentUser.weeklySp} SP)`);
  abandonQuiz();
}

function abandonQuiz() {
  clearInterval(STATE.quizTimerInterval);
  STATE.activeQuiz = null;
  const runner = document.getElementById("activeQuizRunner");
  if (runner) runner.classList.add("hidden");

  // Reset timer visual state
  const timerDisplay = document.getElementById("quizTimerDisplay");
  const timerWrapper = document.getElementById("quizTimerWrapper");
  const timerLabel = document.getElementById("quizTimerLabel");
  if (timerDisplay) {
    timerDisplay.classList.remove("text-red-600", "animate-pulse", "font-black");
    timerDisplay.classList.add("text-maroon", "font-bold");
  }
  if (timerWrapper) {
    timerWrapper.classList.remove("bg-red-50", "border-red-400", "ring-2", "ring-red-400", "animate-pulse");
    timerWrapper.classList.add("border-slate-200");
  }
  if (timerLabel) {
    timerLabel.classList.remove("text-red-600", "font-bold");
    timerLabel.classList.add("text-slate-500");
    timerLabel.innerHTML = `<span class="material-symbols-outlined text-[13px]">timer</span><span>Time Remaining</span>`;
  }
}

function startQuizTimer() {
  clearInterval(STATE.quizTimerInterval);
  const timerDisplay = document.getElementById("quizTimerDisplay");
  const timerWrapper = document.getElementById("quizTimerWrapper");
  const timerLabel = document.getElementById("quizTimerLabel");

  // Reset to standard appearance
  if (timerDisplay) {
    timerDisplay.classList.remove("text-red-600", "animate-pulse", "font-black");
    timerDisplay.classList.add("text-maroon", "font-bold");
  }
  if (timerWrapper) {
    timerWrapper.classList.remove("bg-red-50", "border-red-400", "ring-2", "ring-red-400", "animate-pulse");
    timerWrapper.classList.add("border-slate-200");
  }
  if (timerLabel) {
    timerLabel.classList.remove("text-red-600", "font-bold");
    timerLabel.classList.add("text-slate-500");
    timerLabel.innerHTML = `<span class="material-symbols-outlined text-[13px]">timer</span><span>Time Remaining</span>`;
  }

  let urgencyAlertFired = false;

  STATE.quizTimerInterval = setInterval(() => {
    STATE.quizSecondsRemaining--;
    if (STATE.quizSecondsRemaining <= 0) {
      clearInterval(STATE.quizTimerInterval);
      showToast("⏱️ Time is up! Calculating examination score...");
      finishQuizAndAwardMilestones();
      return;
    }

    const mins = Math.floor(STATE.quizSecondsRemaining / 60);
    const secs = STATE.quizSecondsRemaining % 60;
    if (timerDisplay) {
      timerDisplay.innerText = `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }

    // URGENCY FEEDBACK: Timer text turns red and pulses when < 5 minutes (300 seconds) remaining
    if (STATE.quizSecondsRemaining < 300) {
      if (timerDisplay && !timerDisplay.classList.contains("text-red-600")) {
        timerDisplay.classList.remove("text-maroon");
        timerDisplay.classList.add("text-red-600", "animate-pulse", "font-black");
      }
      if (timerWrapper && !timerWrapper.classList.contains("border-red-400")) {
        timerWrapper.classList.remove("border-slate-200");
        timerWrapper.classList.add("bg-red-50", "border-red-400", "ring-2", "ring-red-400", "animate-pulse");
      }
      if (timerLabel && !timerLabel.classList.contains("text-red-600")) {
        timerLabel.classList.remove("text-slate-500");
        timerLabel.classList.add("text-red-600", "font-bold");
        timerLabel.innerHTML = `<span class="material-symbols-outlined text-[13px] animate-bounce">warning</span><span>⚠️ Final 5 Minutes!</span>`;
      }
      if (!urgencyAlertFired) {
        urgencyAlertFired = true;
        showToast("⚠️ Urgency Warning: Less than 5 minutes remaining in the quiz!");
      }
    }
  }, 1000);
}

// ----------------------------------------------------
// MILESTONE REWARDS & LEADERBOARD
// ----------------------------------------------------
function renderMilestones() {
  const container = document.getElementById("milestonesCardContainer");
  if (!container || typeof MILESTONE_LEVELS === "undefined") return;

  const userSp = STATE.currentUser ? STATE.currentUser.spPoints : 0;

  container.innerHTML = MILESTONE_LEVELS.map(m => {
    const isUnlocked = userSp >= m.spRequired;
    return `
      <div class="p-4 rounded-xl border ${isUnlocked ? 'border-gold bg-gold/5 shadow-sm' : 'border-slate-200 bg-slate-50 opacity-70'} flex flex-col justify-between space-y-2">
        <div>
          <div class="flex items-center justify-between">
            <span class="text-2xl">${m.badge}</span>
            <span class="text-[10px] font-black uppercase px-2 py-0.5 rounded-full ${isUnlocked ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-600'}">
              ${isUnlocked ? 'UNLOCKED' : 'LOCKED'}
            </span>
          </div>
          <h4 class="font-bold text-xs text-slate-900 mt-2">Level ${m.level}: ${m.name}</h4>
          <p class="text-[11px] text-slate-500 font-semibold">${m.spRequired} SP Required</p>
          <p class="text-[11px] text-slate-700 mt-1">${m.rewardTitle}</p>
        </div>
        <div class="pt-2 border-t border-slate-100">
          <div class="w-full bg-slate-200 h-1.5 rounded-full overflow-hidden">
            <div class="bg-gold h-full" style="width: ${Math.min(100, Math.round((userSp / (m.spRequired || 1)) * 100))}%"></div>
          </div>
        </div>
      </div>
    `;
  }).join("");
}

// Weekly Leaderboard: Shows Top 10 students for the active exam week based on points earned from Wednesday Quiz and units
function renderLeaderboard() {
  const tbody = document.getElementById("leaderboardTableBody");
  if (!tbody) return;

  const activeWeekCode = (STATE.wednesdayConfig && STATE.wednesdayConfig.paperCode) 
    ? STATE.wednesdayConfig.paperCode 
    : "WED-20260916";

  const dateDisplay = document.getElementById("weeklyLeaderboardDateDisplay");
  if (dateDisplay) {
    dateDisplay.innerText = `Active Week: ${STATE.wednesdayConfig.formattedDate || activeWeekCode}`;
  }

  // Sort users by their weekly score in the active exam week, with lifetime SP as tie-breaker
  const sorted = [...STATE.users].sort((a, b) => {
    const wA = (a.weeklyScores && a.weeklyScores[activeWeekCode] !== undefined) 
      ? Number(a.weeklyScores[activeWeekCode]) 
      : (Number(a.weeklySp) || 0);
    const wB = (b.weeklyScores && b.weeklyScores[activeWeekCode] !== undefined) 
      ? Number(b.weeklyScores[activeWeekCode]) 
      : (Number(b.weeklySp) || 0);
    if (wB !== wA) return wB - wA;
    return (Number(b.spPoints) || 0) - (Number(a.spPoints) || 0);
  });

  // RESTRICT TO TOP 10 PERSONS ONLY FOR THE WEEK
  const top10 = sorted.slice(0, 10);

  if (top10.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="p-6 text-center text-xs text-slate-400 italic">No weekly quiz scores logged yet for this week. Sit the Wednesday examination or units quiz to rank in the Top 10!</td></tr>`;
    return;
  }

  tbody.innerHTML = top10.map((u, i) => {
    const milestone = getMilestoneForSp(u.spPoints);
    const weeklyEarned = (u.weeklyScores && u.weeklyScores[activeWeekCode] !== undefined) 
      ? Number(u.weeklyScores[activeWeekCode]) 
      : (Number(u.weeklySp) || 0);

    return `
      <tr class="hover:bg-slate-50 transition">
        <td class="p-3 font-bold text-slate-800">
          ${i === 0 ? "🥇 #1" : i === 1 ? "🥈 #2" : i === 2 ? "🥉 #3" : `#${i + 1}`}
        </td>
        <td class="p-3 font-semibold text-slate-900 flex items-center space-x-2">
          <span>${u.fullName}</span>
          ${STATE.currentUser && (STATE.currentUser.id === u.id || (STATE.currentUser.email && u.email && STATE.currentUser.email.toLowerCase() === u.email.toLowerCase())) ? '<span class="text-[9px] bg-maroon text-white font-bold px-1.5 py-0.2 rounded">YOU</span>' : ''}
        </td>
        <td class="p-3 text-slate-600 text-xs">
          <span class="font-semibold text-slate-800">${u.role}</span>
          <span class="text-slate-400">• ${u.stream}</span>
        </td>
        <td class="p-3 text-xs font-bold text-slate-700">
          ${milestone.current.badge} Level ${milestone.current.level} (${milestone.current.name})
        </td>
        <td class="p-3">
          <span class="font-black text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-lg border border-emerald-200">
            +${weeklyEarned} SP
          </span>
        </td>
        <td class="p-3 font-bold text-slate-700">${u.spPoints || 0} SP</td>
        <td class="p-3 text-right">
          <span class="text-[10px] font-bold px-2 py-0.5 rounded ${u.isVerified ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'}">
            ${u.isVerified ? 'VERIFIED' : 'PENDING'}
          </span>
        </td>
      </tr>
    `;
  }).join("");
}

// ----------------------------------------------------
// REDEMPTION STORE (ADD / REMOVE ITEMS & REQUESTS)
// ----------------------------------------------------
function renderRedemptionItems() {
  const container = document.getElementById("redemptionItemsGrid");
  if (!container) return;

  container.innerHTML = STATE.redemptionItems.map(item => `
    <div class="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm flex flex-col justify-between space-y-4 hover:shadow-md transition">
      <div class="space-y-2">
        <div class="flex justify-between items-start">
          <span class="text-[10px] font-extrabold uppercase px-2 py-0.5 rounded bg-amber-100 text-amber-800">${item.category}</span>
          <span class="text-xs text-slate-500 font-semibold">${item.stock} in stock</span>
        </div>
        <h3 class="font-bold text-slate-900 text-base leading-snug">${item.title}</h3>
        <p class="text-xs text-slate-600 leading-relaxed">${item.description}</p>
      </div>

      <div class="pt-3 border-t flex items-center justify-between">
        <div>
          <span class="text-[10px] uppercase font-bold text-slate-400">Required</span>
          <p class="text-base font-black text-amber-700">${item.spPrice} SP</p>
        </div>
        <button onclick="requestRedemption('${item.id}')" class="bg-maroon hover:bg-maroon-dark text-white font-bold px-4 py-2 rounded-xl text-xs shadow transition">
          Redeem Reward
        </button>
      </div>
    </div>
  `).join("");
}

function requestRedemption(itemId) {
  if (!STATE.currentUser) {
    showToast("Please sign in with your student account to redeem rewards.");
    openLoginModal();
    return;
  }

  const item = STATE.redemptionItems.find(i => i.id === itemId);
  if (!item) return;

  if (Number(STATE.currentUser.spPoints) < item.spPrice) {
    showToast(`Insufficient SP! You need ${item.spPrice} SP, but have ${STATE.currentUser.spPoints} SP.`);
    return;
  }

  if (item.stock <= 0) {
    showToast("Item is currently out of stock. Please wait for administrator restock.");
    return;
  }

  // Deduct points and stock
  STATE.currentUser.spPoints = Math.max(0, (Number(STATE.currentUser.spPoints) || 0) - item.spPrice);
  item.stock = Math.max(0, item.stock - 1);

  // Immediately persist updated balance so points never revert on refresh!
  saveUsersAndSession();

  const newRedemption = {
    id: "red_" + Math.random().toString(36).substring(2, 9),
    userId: STATE.currentUser.id,
    userName: STATE.currentUser.fullName,
    userEmail: STATE.currentUser.email,
    userStream: STATE.currentUser.stream,
    itemId: item.id,
    itemTitle: item.title,
    spSpent: item.spPrice,
    timestamp: Date.now(),
    status: "Pending"
  };

  STATE.redemptions.unshift(newRedemption);

  // Sync to Firebase RTDB
  if (db) {
    try {
      db.ref("redemption_requests/" + newRedemption.id).set(newRedemption);
      db.ref("users/" + STATE.currentUser.id + "/spPoints").set(STATE.currentUser.spPoints);
      db.ref("store_items/" + item.id).set(item);
    } catch (e) {
      console.warn("RTDB sync error:", e);
    }
  }

  renderAuthHeader();
  renderRedemptionItems();
  renderUserRedemptionHistory();
  renderAdminRedemptions();
  renderAdminStoreInventory();
  renderLeaderboard();
  updateBadgeCounts();

  showToast(`✅ Request for "${item.title}" submitted to Administration! Remaining balance: ${STATE.currentUser.spPoints} SP`);
}

function renderUserRedemptionHistory() {
  const container = document.getElementById("userRedemptionHistoryList");
  if (!container) return;

  if (!STATE.currentUser) {
    container.innerHTML = `<p class="text-xs text-slate-500 italic">Please sign in to view your redemption requests.</p>`;
    return;
  }

  const userReds = STATE.redemptions.filter(r => r.userId === STATE.currentUser.id);
  if (userReds.length === 0) {
    container.innerHTML = `<p class="text-xs text-slate-500 italic">You have not submitted any reward redemption requests yet.</p>`;
    return;
  }

  container.innerHTML = userReds.map(r => `
    <div class="p-3.5 rounded-xl border border-slate-200 bg-slate-50 flex flex-col sm:flex-row justify-between sm:items-center gap-2">
      <div>
        <p class="text-xs font-bold text-slate-900">${r.itemTitle}</p>
        <p class="text-[11px] text-slate-500">Requested: ${new Date(r.timestamp).toLocaleDateString()} • Spent: ${r.spSpent} SP</p>
      </div>
      <span class="text-xs font-bold px-3 py-1 rounded-full text-center ${getStatusBadgeClass(r.status)}">
        ${r.status}
      </span>
    </div>
  `).join("");
}

// ----------------------------------------------------
// STREAM-RESTRICTED CHAT DISCUSSIONS
// ----------------------------------------------------
function setupDiscussionViewPermissions() {
  const switcher = document.getElementById("chatAdminStreamSwitcher");
  const badge = document.getElementById("chatStreamBadge");
  const notice = document.getElementById("chatStreamNotice");
  const navLabel = document.getElementById("discussionsNavLabel");

  if (!STATE.currentUser) {
    if (switcher) switcher.classList.add("hidden");
    if (badge) badge.innerText = "Guest Mode (Sign In Required)";
    renderChatMessages();
    return;
  }

  // Teachers and Admins have access to BOTH streams
  if (STATE.currentUser.role === "ADMIN" || STATE.currentUser.role === "TEACHER") {
    if (switcher) switcher.classList.remove("hidden");
    if (badge) badge.innerText = `${STATE.activeChatStream} Channel (Moderator View)`;
    if (notice) notice.innerText = `You have staff access to view and communicate in both Physical Science and Bio Science streams.`;
    if (navLabel) navLabel.innerText = "Discussions (All-Access)";
  } else {
    // Student: Lock to their chosen stream only
    STATE.activeChatStream = STATE.currentUser.stream;
    if (switcher) switcher.classList.add("hidden");
    if (badge) badge.innerText = `${STATE.currentUser.stream} Channel`;
    if (notice) notice.innerText = `You are enrolled in ${STATE.currentUser.stream}. You can view and participate strictly in your stream discussions.`;
    if (navLabel) navLabel.innerText = `${STATE.currentUser.stream} Chat`;
  }

  renderChatMessages();
}

function switchChatStream(streamName) {
  STATE.activeChatStream = streamName;
  const btnPhys = document.getElementById("btnChatPhysical");
  const btnBio = document.getElementById("btnChatBio");

  if (streamName === "Physical Science") {
    if (btnPhys) btnPhys.className = "px-3 py-1 rounded-lg text-xs font-bold bg-maroon text-white";
    if (btnBio) btnBio.className = "px-3 py-1 rounded-lg text-xs font-bold bg-white text-slate-700";
  } else {
    if (btnPhys) btnPhys.className = "px-3 py-1 rounded-lg text-xs font-bold bg-white text-slate-700";
    if (btnBio) btnBio.className = "px-3 py-1 rounded-lg text-xs font-bold bg-maroon text-white";
  }

  setupDiscussionViewPermissions();
}

function renderChatMessages() {
  const box = document.getElementById("chatMessagesBox");
  if (!box) return;

  const currentUserId = STATE.currentUser ? STATE.currentUser.id : null;

  // Filter messages according to currently viewed stream
  const filtered = STATE.messages.filter(m => {
    if (!m.stream) return true; // Legacy message fallback
    return m.stream === STATE.activeChatStream;
  });

  if (filtered.length === 0) {
    box.innerHTML = `<div class="text-center py-12 text-xs text-slate-400">No messages yet in the ${STATE.activeChatStream} discussion room. Be the first to start a conversation!</div>`;
    return;
  }

  box.innerHTML = filtered.map(m => `
    <div class="flex items-start space-x-3 p-3 rounded-xl ${currentUserId && m.senderId === currentUserId ? 'bg-maroon/5 ml-8 border border-maroon/20' : 'bg-white mr-8 border border-slate-200 shadow-sm'}">
      <div class="w-8 h-8 rounded-full ${m.senderRole === 'ADMIN' ? 'bg-maroon text-gold' : m.senderRole === 'TEACHER' ? 'bg-emerald-700 text-white' : 'bg-slate-200 text-slate-700'} font-bold flex items-center justify-center text-xs shrink-0">
        ${m.senderName.charAt(0)}
      </div>
      <div class="flex-1">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-2">
            <span class="text-xs font-bold text-slate-900">${m.senderName}</span>
            <span class="text-[9px] font-extrabold uppercase px-1.5 py-0.2 rounded ${m.senderRole === 'ADMIN' ? 'bg-maroon text-white' : m.senderRole === 'TEACHER' ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-600'}">${m.senderRole || 'STUDENT'}</span>
          </div>
          <span class="text-[10px] text-slate-400">${new Date(m.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
        </div>
        <p class="text-xs text-slate-700 mt-1 leading-relaxed">${m.content}</p>
        <div class="mt-1.5 flex items-center justify-end">
          <button onclick="askGeminiQuick('${encodeURIComponent(m.content)}')" class="inline-flex items-center space-x-1 text-[11px] font-semibold text-purple-700 hover:text-purple-900 bg-purple-50 hover:bg-purple-100 border border-purple-200/60 px-2 py-0.5 rounded-lg transition" title="Ask Gemini AI Tutor to explain or solve this">
            <span class="material-symbols-outlined text-[13px] text-amber-500">auto_awesome</span>
            <span>Ask Gemini Tutor</span>
          </button>
        </div>
      </div>
    </div>
  `).join("");

  box.scrollTop = box.scrollHeight;
}

function postChatMessage(e) {
  e.preventDefault();
  if (!STATE.currentUser) {
    showToast("Please sign in to participate in student discussions.");
    openLoginModal();
    return;
  }

  const input = document.getElementById("chatInputMessage");
  const text = input.value.trim();
  if (!text) return;

  const targetStream = (STATE.currentUser.role === 'ADMIN' || STATE.currentUser.role === 'TEACHER')
    ? STATE.activeChatStream
    : STATE.currentUser.stream;

  const newMsg = {
    id: "msg_" + Date.now(),
    stream: targetStream,
    senderId: STATE.currentUser.id,
    senderName: STATE.currentUser.fullName,
    senderRole: STATE.currentUser.role,
    content: text,
    timestamp: Date.now()
  };

  STATE.messages.push(newMsg);
  input.value = "";

  // Sync to Firebase Realtime Database
  if (db) {
    try {
      db.ref("discussions/" + newMsg.id).set(newMsg);
    } catch (e) {
      console.warn("RTDB discussion sync note:", e);
    }
  }

  renderChatMessages();
  renderAdminChatAudit();
}

// ----------------------------------------------------
// ADMINISTRATOR PANEL (USERS, STORE, REDEMPTIONS, CHAT)
// ----------------------------------------------------
function switchAdminTab(tabKey) {
  if (!requireAdmin()) {
    navigateTo("home");
    return;
  }

  STATE.adminSubTab = tabKey;
  document.querySelectorAll(".admin-subtab").forEach(btn => {
    btn.classList.remove("bg-maroon", "text-white");
    btn.classList.add("text-slate-600", "hover:bg-slate-100");
  });
  const activeBtn = document.getElementById("adminTab-" + tabKey);
  if (activeBtn) {
    activeBtn.classList.add("bg-maroon", "text-white");
    activeBtn.classList.remove("text-slate-600", "hover:bg-slate-100");
  }

  document.querySelectorAll(".admin-panel").forEach(p => p.classList.add("hidden"));
  const panel = document.getElementById("adminPanel-" + tabKey);
  if (panel) panel.classList.remove("hidden");

  if (tabKey === "quiz") {
    renderWednesdayPaperInfo();
  }
}

// 1. User Management & Permanent Account Deletion
function renderAdminUsers() {
  if (!isCurrentUserAdmin()) return;
  const tbody = document.getElementById("adminUsersTableBody");
  if (!tbody) return;

  const searchInput = document.getElementById("adminUserSearchInput");
  const query = searchInput ? searchInput.value.trim().toLowerCase() : "";

  // Show all users without artificial filters; apply search query if typed
  const filtered = STATE.users.filter(u => {
    if (!u) return false;
    if (!query) return true;
    return (u.fullName && u.fullName.toLowerCase().includes(query)) || 
           (u.email && u.email.toLowerCase().includes(query)) ||
           (u.role && u.role.toLowerCase().includes(query)) ||
           (u.stream && u.stream.toLowerCase().includes(query));
  });

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="p-6 text-center text-xs text-slate-400">No users found matching "${query}".</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(u => `
    <tr class="hover:bg-slate-50 transition">
      <td class="p-3 font-semibold text-slate-900">
        <div class="flex items-center space-x-1.5">
          <span>${u.fullName}</span>
          ${STATE.currentUser && STATE.currentUser.id === u.id ? '<span class="text-[9px] bg-maroon text-white font-bold px-1.5 py-0.2 rounded">YOU</span>' : ''}
        </div>
      </td>
      <td class="p-3 text-xs text-slate-500">${u.email}</td>
      <td class="p-3 text-xs font-bold">${u.role}</td>
      <td class="p-3 text-xs text-slate-600">${u.stream}</td>
      <td class="p-3 text-xs font-bold text-gold-dark">
        <div class="flex items-center space-x-1">
          <span>${u.spPoints || 0} SP</span>
          <button onclick="adminAdjustUserSp('${u.id}')" class="text-slate-400 hover:text-amber-600 p-0.5 rounded" title="Adjust SP Points">
            <span class="material-symbols-outlined text-[13px]">edit</span>
          </button>
        </div>
      </td>
      <td class="p-3">
        <span class="text-[10px] font-bold px-2 py-0.5 rounded ${u.isVerified ? 'bg-emerald-100 text-emerald-800' : 'bg-red-100 text-red-800'}">
          ${u.isVerified ? 'VERIFIED' : 'PENDING'}
        </span>
      </td>
      <td class="p-3 text-right space-x-2 whitespace-nowrap">
        ${!u.isVerified ? `
          <button onclick="verifyUser('${u.id}')" class="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded text-[11px] font-bold shadow">
            Verify
          </button>
        ` : ''}
        ${u.id !== 'admin_jasim' && u.email !== 'mnmjaasim@gmail.com' ? `
          <button onclick="adminDeleteUser('${u.id}')" class="px-2.5 py-1 bg-red-600 hover:bg-red-700 text-white rounded text-[11px] font-bold shadow inline-flex items-center space-x-1">
            <span class="material-symbols-outlined text-[14px]">delete</span>
            <span>Delete</span>
          </button>
        ` : `
          <span class="text-xs text-slate-400 italic font-medium">Root Admin</span>
        `}
      </td>
    </tr>
  `).join("");
}

function verifyUser(userId) {
  if (!requireAdmin()) return;
  const u = STATE.users.find(user => user.id === userId);
  if (u) {
    u.isVerified = true;
    saveUsersAndSession();
    if (db) {
      try {
        db.ref("users/" + u.id + "/isVerified").set(true);
      } catch (e) {
        console.warn("RTDB verify user note:", e);
      }
    }
    renderAdminUsers();
    renderLeaderboard();
    updateBadgeCounts();
    showToast(`Verified account for ${u.fullName}!`);
  }
}

// Admin can manually adjust student SP balance
function adminAdjustUserSp(userId) {
  if (!requireAdmin()) return;
  const u = STATE.users.find(user => user.id === userId);
  if (!u) return;

  const input = prompt(`Enter new Science Points (SP) balance for ${u.fullName}:`, u.spPoints || 0);
  if (input === null) return;
  const newSp = parseInt(input, 10);
  if (isNaN(newSp) || newSp < 0) {
    showToast("⚠️ Invalid SP value. Please enter a positive integer.");
    return;
  }

  u.spPoints = newSp;
  if (STATE.currentUser && STATE.currentUser.id === u.id) {
    STATE.currentUser.spPoints = newSp;
  }
  saveUsersAndSession();

  if (db) {
    try {
      db.ref("users/" + u.id + "/spPoints").set(newSp);
    } catch (e) {
      console.warn("RTDB adjust SP error:", e);
    }
  }

  renderAuthHeader();
  renderAdminUsers();
  renderLeaderboard();
  renderMilestones();
  showToast(`Updated SP balance for ${u.fullName} to ${newSp} SP.`);
}

// Admin can permanently delete users
function adminDeleteUser(userId) {
  if (!requireAdmin()) return;
  const u = STATE.users.find(user => user.id === userId);
  if (!u) return;

  if (confirm(`Are you sure you want to permanently delete user "${u.fullName}" (${u.email})? This action cannot be undone.`)) {
    STATE.users = STATE.users.filter(user => user.id !== userId);

    if (STATE.currentUser && STATE.currentUser.id === userId) {
      handleLogout();
    } else {
      saveUsersAndSession();
    }

    // Sync deletion to Firebase
    if (db) {
      try {
        db.ref("users/" + userId).remove();
      } catch (e) {
        console.warn("RTDB remove user note:", e);
      }
    }

    renderAdminUsers();
    renderLeaderboard();
    updateBadgeCounts();
    showToast(`Deleted user ${u.fullName} from database.`);
  }
}

// 2. Redemption Store Inventory Management (Add & Remove Items)
function renderAdminStoreInventory() {
  if (!isCurrentUserAdmin()) return;
  const container = document.getElementById("adminStoreItemsList");
  if (!container) return;

  container.innerHTML = STATE.redemptionItems.map(item => `
    <div class="bg-slate-50 p-4 rounded-xl border border-slate-200 space-y-3 flex flex-col justify-between">
      <div>
        <div class="flex justify-between items-start">
          <span class="text-[10px] font-extrabold uppercase px-2 py-0.5 rounded bg-amber-100 text-amber-800">${item.category}</span>
          <span class="text-xs font-bold text-maroon">${item.spPrice} SP</span>
        </div>
        <h4 class="font-bold text-sm text-slate-900 mt-1">${item.title}</h4>
        <p class="text-xs text-slate-600 mt-1">${item.description}</p>
        <p class="text-[11px] text-slate-500 font-semibold mt-2">Available Stock: ${item.stock} units</p>
      </div>
      <div class="pt-2 border-t border-slate-200 flex justify-end">
        <button onclick="adminRemoveStoreItem('${item.id}')" class="px-3 py-1.5 rounded-lg border border-red-300 text-red-600 hover:bg-red-50 text-xs font-bold flex items-center space-x-1">
          <span class="material-symbols-outlined text-[16px]">delete</span>
          <span>Remove from Store</span>
        </button>
      </div>
    </div>
  `).join("");
}

function handleAdminAddStoreItem(e) {
  e.preventDefault();
  if (!requireAdmin()) return;

  const title = document.getElementById("newStoreTitle").value.trim();
  const category = document.getElementById("newStoreCategory").value;
  const spPrice = parseInt(document.getElementById("newStoreSpPrice").value, 10);
  const stock = parseInt(document.getElementById("newStoreStock").value, 10);
  const desc = document.getElementById("newStoreDesc").value.trim();

  if (!title || !desc || isNaN(spPrice) || isNaN(stock)) {
    showToast("Please complete all store item fields.");
    return;
  }

  const newItem = {
    id: "item_" + Date.now(),
    title: title,
    category: category,
    spPrice: spPrice,
    stock: stock,
    description: desc
  };

  STATE.redemptionItems.unshift(newItem);

  // Sync to Firebase
  if (db) {
    try {
      db.ref("store_items/" + newItem.id).set(newItem);
    } catch (e) {
      console.warn("RTDB store sync note:", e);
    }
  }

  // Clear form
  e.target.reset();

  renderRedemptionItems();
  renderAdminStoreInventory();
  showToast(`✅ Successfully added "${title}" to the Redemption Store!`);
}

function adminRemoveStoreItem(itemId) {
  if (!requireAdmin()) return;
  const item = STATE.redemptionItems.find(i => i.id === itemId);
  if (!item) return;

  if (confirm(`Remove "${item.title}" from the redemption store?`)) {
    STATE.redemptionItems = STATE.redemptionItems.filter(i => i.id !== itemId);

    // Sync removal to Firebase
    if (db) {
      try {
        db.ref("store_items/" + itemId).remove();
      } catch (e) {
        console.warn("RTDB remove item note:", e);
      }
    }

    renderRedemptionItems();
    renderAdminStoreInventory();
    showToast(`Removed "${item.title}" from store inventory.`);
  }
}

// 3. Redemption Requests Oversight
function setAdminRedemptionFilter(filter) {
  STATE.adminRedemptionFilter = filter;
  document.querySelectorAll(".admin-red-filter").forEach(b => {
    b.classList.remove("bg-maroon", "text-white");
    b.classList.add("bg-slate-100", "text-slate-700");
  });
  if (typeof event !== "undefined" && event && event.target) {
    event.target.classList.add("bg-maroon", "text-white");
    event.target.classList.remove("bg-slate-100", "text-slate-700");
  }
  renderAdminRedemptions();
}

function renderAdminRedemptions() {
  const container = document.getElementById("adminRedemptionsListContainer");
  if (!container) return;

  const totalEl = document.getElementById("adminStatTotalRequests");
  const pendingEl = document.getElementById("adminStatPendingRequests");
  const fulfilledEl = document.getElementById("adminStatFulfilledRequests");

  const total = STATE.redemptions.length;
  const pending = STATE.redemptions.filter(r => (r.status || "").toLowerCase() === "pending").length;
  const fulfilled = STATE.redemptions.filter(r => (r.status || "").toLowerCase() === "fulfilled").length;

  if (totalEl) totalEl.innerText = total;
  if (pendingEl) pendingEl.innerText = pending;
  if (fulfilledEl) fulfilledEl.innerText = fulfilled;

  const searchInput = document.getElementById("adminRedemptionSearchInput");
  const query = searchInput ? searchInput.value.trim().toLowerCase() : "";

  const filterLower = (STATE.adminRedemptionFilter || "All").toLowerCase();

  const filtered = STATE.redemptions.filter(r => {
    const statusLower = (r.status || "").toLowerCase();
    let matchesFilter = (filterLower === "all");
    if (!matchesFilter) {
      if (filterLower === "rejected" || filterLower === "cancelled") {
        matchesFilter = (statusLower === "rejected" || statusLower === "cancelled");
      } else {
        matchesFilter = (statusLower === filterLower);
      }
    }
    const matchesQuery = !query ||
      (r.userName && r.userName.toLowerCase().includes(query)) ||
      (r.itemTitle && r.itemTitle.toLowerCase().includes(query)) ||
      (r.userEmail && r.userEmail.toLowerCase().includes(query));
    return matchesFilter && matchesQuery;
  });

  if (filtered.length === 0) {
    container.innerHTML = `<div class="p-8 text-center text-xs text-slate-400">No student redemption requests found for this filter.</div>`;
    return;
  }

  container.innerHTML = filtered.map(r => {
    const targetId = r.id || r.firebaseKey;
    const statusLower = (r.status || "pending").toLowerCase();
    const isCompleted = (statusLower === "fulfilled" || statusLower === "approved");
    const isCancelled = (statusLower === "cancelled" || statusLower === "rejected" || statusLower === "declined");

    return `
      <div class="p-4 rounded-xl border border-slate-200 bg-white space-y-3 shadow-sm">
        <div class="flex justify-between items-start">
          <div>
            <h4 class="font-bold text-sm text-slate-900">${r.userName || 'Student'}</h4>
            <p class="text-xs text-slate-500">${r.userEmail || ''} • Stream: <strong>${r.userStream || 'Physical Science'}</strong></p>
          </div>
          <span class="text-xs font-bold px-2.5 py-1 rounded-full ${getStatusBadgeClass(r.status)}">
            ${isCancelled ? 'Cancelled & Refunded' : isCompleted ? 'Handed Over' : r.status || 'Pending Collection'}
          </span>
        </div>

        <div class="bg-slate-50 p-3 rounded-lg border border-slate-100 flex items-center justify-between">
          <div>
            <p class="font-semibold text-xs text-slate-800">${r.itemTitle || 'Reward Item'}</p>
            <p class="text-[11px] text-slate-500">${r.timestamp ? new Date(r.timestamp).toLocaleString() : 'Recent'}</p>
          </div>
          <span class="font-black text-amber-700 text-sm">${r.spSpent || 0} SP</span>
        </div>

        <div class="flex flex-wrap items-center justify-end gap-2 pt-2 border-t border-slate-100">
          <button type="button" onclick="updateRedemptionStatus('${targetId}', 'Approved')" class="px-3.5 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold shadow flex items-center space-x-1.5 transition">
            <span class="material-symbols-outlined text-[16px]">check_circle</span>
            <span>Approve & Mark Handed Over</span>
          </button>
          <button type="button" onclick="updateRedemptionStatus('${targetId}', 'Cancelled')" class="px-3.5 py-2 rounded-lg border border-red-300 text-red-600 hover:bg-red-50 text-xs font-bold flex items-center space-x-1.5 transition">
            <span class="material-symbols-outlined text-[16px]">cancel</span>
            <span>Cancel & Refund</span>
          </button>
        </div>
      </div>
    `;
  }).join("");
}

function updateRedemptionStatus(redemptionId, action) {
  if (!requireAdmin()) return;

  const red = STATE.redemptions.find(r => r.id === redemptionId || r.firebaseKey === redemptionId);
  if (!red) {
    showToast("⚠️ Redemption request record not found in queue.");
    return;
  }

  const targetKey = red.firebaseKey || red.id;
  const isCancelOrDecline = (action === "Cancelled" || action === "Declined" || action === "Rejected");

  // If cancelling/declining, restore Science Points to student
  if (isCancelOrDecline) {
    const refundSp = Number(red.spSpent) || 0;

    let user = STATE.users.find(u => (red.userId && u.id === red.userId) || (red.userEmail && u.email && u.email.toLowerCase() === red.userEmail.toLowerCase()));
    if (user) {
      user.spPoints = (Number(user.spPoints) || 0) + refundSp;
    }

    if (STATE.currentUser && ((red.userId && STATE.currentUser.id === red.userId) || (red.userEmail && STATE.currentUser.email && STATE.currentUser.email.toLowerCase() === red.userEmail.toLowerCase()))) {
      STATE.currentUser.spPoints = (Number(STATE.currentUser.spPoints) || 0) + refundSp;
    }

    saveUsersAndSession();

    // Sync refunded balance to Firebase RTDB
    if (db && (red.userId || (user && user.id))) {
      try {
        const targetUserId = user ? user.id : red.userId;
        const finalSp = user ? user.spPoints : refundSp;
        db.ref("users/" + targetUserId + "/spPoints").set(finalSp);
      } catch (err) {
        console.warn("RTDB refund SP error:", err);
      }
    }
  }

  // Remove the record of student redemption store request completely from state after approving or declining
  STATE.redemptions = STATE.redemptions.filter(r => r.id !== targetKey && r.firebaseKey !== targetKey && r.id !== redemptionId && r.firebaseKey !== redemptionId);

  // Remove record from Firebase Realtime Database
  if (db) {
    try {
      db.ref("redemption_requests/" + targetKey).remove();
    } catch (e) {
      console.warn("RTDB remove redemption record error:", e);
    }
  }

  renderAuthHeader();
  renderAdminRedemptions();
  renderUserRedemptionHistory();
  renderLeaderboard();
  updateBadgeCounts();

  if (isCancelOrDecline) {
    showToast(`↩️ Request for "${red.itemTitle}" cancelled, ${red.spSpent} SP refunded to ${red.userName || 'student'}, and record removed.`);
  } else {
    showToast(`🎁 Reward "${red.itemTitle}" marked handed over to ${red.userName || 'student'} and record removed.`);
  }
}

// 4. Chat Moderation across Both Streams
function renderAdminChatAudit() {
  if (!isCurrentUserAdmin()) return;
  const box = document.getElementById("adminChatAuditBox");
  if (!box) return;

  box.innerHTML = STATE.messages.map(m => `
    <div class="flex justify-between items-center p-3 rounded-lg border border-slate-200 bg-white hover:bg-slate-50">
      <div>
        <div class="flex items-center space-x-2">
          <span class="text-xs font-bold text-slate-900">${m.senderName}</span>
          <span class="text-[10px] font-bold px-1.5 py-0.2 rounded bg-maroon/10 text-maroon">${m.stream || 'All'}</span>
          <span class="text-[10px] text-slate-400">${new Date(m.timestamp).toLocaleTimeString()}</span>
        </div>
        <p class="text-xs text-slate-700 mt-0.5">"${m.content}"</p>
      </div>
      <button onclick="adminDeleteChatMessage('${m.id}')" class="text-red-600 hover:text-red-800 p-1.5 text-xs rounded-lg hover:bg-red-50" title="Delete message">
        <span class="material-symbols-outlined text-[18px]">delete</span>
      </button>
    </div>
  `).join("");
}

function adminDeleteChatMessage(msgId) {
  if (!requireAdmin()) return;
  STATE.messages = STATE.messages.filter(m => m.id !== msgId);
  if (db) {
    try {
      db.ref("discussions/" + msgId).remove();
    } catch (e) {
      console.warn("RTDB delete message note:", e);
    }
  }
  renderChatMessages();
  renderAdminChatAudit();
  showToast("Message deleted by Administrator.");
}

// ----------------------------------------------------
// FIREBASE REALTIME DATABASE SYNC
// ----------------------------------------------------
const REMOVED_MOCK_EMAILS = [
  "k.perera@zahira.lk",
  "ahamad.rizvi@zahira.lk",
  "sara.fathima@zahira.lk",
  "nifras.mr@zahira.lk",
  "aisha.m@zahira.lk"
];
const REMOVED_MOCK_IDS = [
  "teacher_perera",
  "student_ahamad",
  "student_sara",
  "student_nifras",
  "student_aisha"
];

function setupFirebaseRealtime() {
  if (!db) return;

  const statusEl = document.getElementById("firebaseConnectionStatus");
  if (statusEl) statusEl.innerText = "Firebase Cloud Connected";

  try {
    // Listen for users (excluding the removed mock demo accounts)
    db.ref("users").on("value", snapshot => {
      const data = snapshot.val();
      if (data) {
        const cloudUsers = Object.values(data).filter(u => {
          if (!u) return false;
          const emailLower = (u.email || "").toLowerCase();
          return !REMOVED_MOCK_EMAILS.includes(emailLower) && !REMOVED_MOCK_IDS.includes(u.id);
        });
        cloudUsers.forEach(cu => {
          const idx = STATE.users.findIndex(u => u.id === cu.id || (u.email && cu.email && u.email.toLowerCase() === cu.email.toLowerCase()));
          if (idx >= 0) {
            STATE.users[idx] = cu;
          } else {
            STATE.users.push(cu);
          }
        });
        // Also remove mock demo accounts from Firebase RTDB to keep cloud database pristine
        REMOVED_MOCK_IDS.forEach(mid => {
          try { db.ref("users/" + mid).remove(); } catch(e) {}
        });
        renderLeaderboard();
        renderAdminUsers();
      }
    });

    // Listen for discussions
    db.ref("discussions").on("value", snapshot => {
      const data = snapshot.val();
      if (data) {
        const list = Object.values(data).filter(m => {
          if (!m) return false;
          return !REMOVED_MOCK_IDS.includes(m.senderId);
        });
        if (list.length > 0) {
          STATE.messages = list;
        }
        renderChatMessages();
        renderAdminChatAudit();
      }
    });

    // Listen for store items
    db.ref("store_items").on("value", snapshot => {
      const data = snapshot.val();
      if (data) {
        STATE.redemptionItems = Object.values(data);
        renderRedemptionItems();
        renderAdminStoreInventory();
      }
    });

    // Listen for redemptions
    db.ref("redemption_requests").on("value", snapshot => {
      const list = [];
      snapshot.forEach(child => {
        const val = child.val();
        if (val) {
          list.push({
            ...val,
            id: val.id || child.key,
            firebaseKey: child.key
          });
        }
      });
      STATE.redemptions = list;
      renderAdminRedemptions();
      renderUserRedemptionHistory();
      updateBadgeCounts();
    });

    // Listen for Wednesday Exam Synchronized Configuration
    db.ref("wednesday_quiz_config").on("value", snapshot => {
      const data = snapshot.val();
      if (data && data.seed) {
        STATE.wednesdayConfig = data;
        renderWednesdayPaperInfo();
      } else {
        // If not present in cloud yet, initialize cloud with current calculated seed
        try {
          db.ref("wednesday_quiz_config").set(STATE.wednesdayConfig);
        } catch (err) {
          console.warn("RTDB wednesday_quiz_config set note:", err);
        }
      }
    });

  } catch (e) {
    console.warn("RTDB listeners active in fallback mode:", e);
  }
}

// ----------------------------------------------------
// WEDNESDAY SYNCHRONIZED EXAM ENGINE & ADMIN CONTROLS
// ----------------------------------------------------
function renderWednesdayPaperInfo() {
  const cfg = STATE.wednesdayConfig;
  if (!cfg) return;

  // Student Examination View Indicators
  const codeDisplay = document.getElementById("wednesdayPaperCodeDisplay");
  if (codeDisplay) codeDisplay.innerText = cfg.paperCode || "WED-20260916";

  const seedDisplay = document.getElementById("wednesdaySeedDisplay");
  if (seedDisplay) seedDisplay.innerText = `#${cfg.seed || "84920194"}`;

  // Admin Panel Indicators
  const adminDate = document.getElementById("adminCurrentWedDate");
  if (adminDate) adminDate.innerText = cfg.formattedDate || cfg.dateString || "Wednesday, Sep 16, 2026";

  const adminCode = document.getElementById("adminCurrentPaperCode");
  if (adminCode) adminCode.innerText = cfg.paperCode || "WED-20260916";

  const adminSeed = document.getElementById("adminCurrentSeed");
  if (adminSeed) adminSeed.innerText = `#${cfg.seed || "84920194"}`;

  renderAdminQuestionInspection();
}

function adminForceReseedQuiz() {
  if (!requireAdmin()) return;

  const newSeed = Math.floor(Math.random() * 89999999) + 10000000;
  const currentInfo = (typeof getActiveWednesdayInfo === "function")
    ? getActiveWednesdayInfo(STATE.adminPreviewWednesdayDate)
    : { dateString: "2026-09-16", paperCode: "WED-20260916", formattedDate: "Wednesday, Sep 16, 2026" };

  STATE.wednesdayConfig = {
    dateString: currentInfo.dateString,
    paperCode: `${currentInfo.paperCode}-R${String(newSeed).slice(-4)}`,
    seed: newSeed,
    formattedDate: currentInfo.formattedDate,
    updatedAt: Date.now(),
    updatedBy: STATE.currentUser ? STATE.currentUser.fullName : "Administrator"
  };

  // Broadcast through Firebase Realtime Database so all connected student devices receive the new seed instantly
  if (db) {
    try {
      db.ref("wednesday_quiz_config").set(STATE.wednesdayConfig);
    } catch (e) {
      console.warn("RTDB quiz seed sync:", e);
    }
  }

  renderWednesdayPaperInfo();
  showToast(`Exam Reseeded! Paper Code: ${STATE.wednesdayConfig.paperCode} (Seed: #${newSeed})`);
}

function adminAdvanceNextWednesday() {
  if (!requireAdmin()) return;

  const baseDate = STATE.adminPreviewWednesdayDate ? new Date(STATE.adminPreviewWednesdayDate) : new Date();
  baseDate.setDate(baseDate.getDate() + 7);
  STATE.adminPreviewWednesdayDate = baseDate;

  const newInfo = (typeof getActiveWednesdayInfo === "function")
    ? getActiveWednesdayInfo(baseDate)
    : { dateString: "2026-09-23", paperCode: "WED-20260923", seed: 94819201, formattedDate: "Wednesday, Sep 23, 2026" };

  STATE.wednesdayConfig = {
    ...newInfo,
    updatedAt: Date.now(),
    updatedBy: STATE.currentUser ? STATE.currentUser.fullName : "Administrator"
  };

  if (db) {
    try {
      db.ref("wednesday_quiz_config").set(STATE.wednesdayConfig);
    } catch (e) {
      console.warn("RTDB quiz seed sync:", e);
    }
  }

  renderWednesdayPaperInfo();
  showToast(`Inspecting Next Wednesday Paper: ${newInfo.formattedDate} (${newInfo.paperCode})`);
}

function adminResetCalendarWednesday() {
  if (!requireAdmin()) return;

  STATE.adminPreviewWednesdayDate = null;
  const newInfo = (typeof getActiveWednesdayInfo === "function")
    ? getActiveWednesdayInfo()
    : { dateString: "2026-09-16", paperCode: "WED-20260916", seed: 84920194, formattedDate: "Wednesday, Sep 16, 2026" };

  STATE.wednesdayConfig = {
    ...newInfo,
    updatedAt: Date.now(),
    updatedBy: "System Calendar"
  };

  if (db) {
    try {
      db.ref("wednesday_quiz_config").set(STATE.wednesdayConfig);
    } catch (e) {
      console.warn("RTDB quiz seed sync:", e);
    }
  }

  renderWednesdayPaperInfo();
  showToast(`Restored to Official Calendar Wednesday: ${newInfo.formattedDate}`);
}

function renderAdminQuestionInspection() {
  const select = document.getElementById("adminInspectUnitSelect");
  const container = document.getElementById("adminQuestionInspectorList");
  if (!container) return;

  const unitId = select ? select.value : "all";
  const seed = (STATE.wednesdayConfig && STATE.wednesdayConfig.seed) ? STATE.wednesdayConfig.seed : 84920194;

  const sampleQuestions = (typeof getSynchronizedWednesdayQuestions === "function")
    ? getSynchronizedWednesdayQuestions(unitId, seed).slice(0, 5)
    : [];

  if (sampleQuestions.length === 0) {
    container.innerHTML = `<p class="text-xs text-slate-400 italic">No questions found for selection.</p>`;
    return;
  }

  container.innerHTML = sampleQuestions.map((q, i) => `
    <div class="bg-slate-50 p-3.5 rounded-xl border border-slate-200 text-xs space-y-2">
      <div class="flex items-center justify-between">
        <span class="font-bold text-maroon">Exam Question #${i + 1} (${q.paperCode || STATE.wednesdayConfig.paperCode})</span>
        <span class="text-[10px] font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
          ● Locked for All Students
        </span>
      </div>
      <p class="font-semibold text-slate-900">${q.q}</p>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-1.5 pt-1">
        ${q.options.map((opt, optIdx) => `
          <div class="p-2 rounded-lg border ${optIdx === q.correctIndex ? 'bg-emerald-50 border-emerald-300 text-emerald-900 font-bold' : 'bg-white border-slate-200 text-slate-700'}">
            <span class="font-mono text-[10px] mr-1">${['A','B','C','D'][optIdx]}:</span> ${opt}
            ${optIdx === q.correctIndex ? ' <span class="text-[10px] text-emerald-600 font-extrabold ml-1">✓ (Correct Answer)</span>' : ''}
          </div>
        `).join("")}
      </div>
      <p class="text-[11px] text-slate-500 italic mt-0.5">Scheme/Explanation: ${q.explanation}</p>
    </div>
  `).join("");
}

// ----------------------------------------------------
// UI UTILITIES
// ----------------------------------------------------
function updateBadgeCounts() {
  const pendingRedemptions = STATE.redemptions.filter(r => r.status.toLowerCase() === "pending").length;
  const pendingUsers = STATE.users.filter(u => !u.isVerified).length;

  const navBadge = document.getElementById("navPendingBadge");
  if (navBadge) {
    navBadge.innerText = pendingRedemptions + pendingUsers;
    navBadge.classList.toggle("hidden", (pendingRedemptions + pendingUsers) === 0);
  }

  const adminRedBadge = document.getElementById("adminPendingRedemptionsCount");
  if (adminRedBadge) adminRedBadge.innerText = pendingRedemptions;

  const adminUserBadge = document.getElementById("adminPendingUsersCount");
  if (adminUserBadge) adminUserBadge.innerText = pendingUsers;
}

function getStatusBadgeClass(status) {
  switch ((status || "").toLowerCase()) {
    case "approved":
      return "bg-blue-100 text-blue-800 border border-blue-200";
    case "fulfilled":
      return "bg-emerald-100 text-emerald-800 border border-emerald-200";
    case "rejected":
    case "cancelled":
      return "bg-red-100 text-red-800 border border-red-200";
    default:
      return "bg-amber-100 text-amber-800 border border-amber-200";
  }
}

function showToast(message) {
  const toast = document.getElementById("toastNotification");
  const msgEl = document.getElementById("toastMessage");
  if (!toast || !msgEl) return;

  msgEl.innerText = message;
  toast.classList.remove("hidden", "translate-y-2");
  setTimeout(() => {
    toast.classList.add("hidden", "translate-y-2");
  }, 4000);
}

// ====================================================
// GEMINI MULTI-TURN AI TUTOR CHAT ENGINE
// ====================================================

const GEMINI_ROLES = {
  general: {
    name: "All-Round A/L Science Mentor",
    tagline: "Comprehensive guidance across Physics, Combined Maths, Chemistry & Biology",
    instruction: `You are the official ZNC Science AI Academic Tutor & Syllabus Mentor for Zahira National College (Mawanella) Science Section.
You specialize in the Sri Lankan G.C.E. Advanced Level (A/L) English Medium curriculum for:
- Physics (Mechanics, Waves, Optics, Thermal, Fields, Current Electricity, Electronics, Modern Physics)
- Combined Mathematics (Pure Mathematics: Algebra, Calculus, Trigonometry, Coordinate Geometry, Complex Numbers; Applied Mathematics: Statics, Dynamics, Vectors, Projectiles, Equilibrium, Relative Velocity)
- Chemistry (General, Physical, Inorganic s/p/d block, and Organic Chemistry)
- Biology (Cellular, Plant/Animal Physiology, Genetics, Environmental Biology based on NIE Resource Book)

Guidelines:
1. Always maintain a polite, encouraging, highly structured academic tone.
2. Provide step-by-step mathematical derivations and scientific explanations.
3. Reference relevant fundamental laws (e.g. Newton's Laws, Bernoulli's Principle, Le Chatelier's Principle, Hess's Law).
4. Highlight common A/L examination pitfalls and marking scheme tips.
5. Use bullet points, bold formulas, and numbered steps for high readability.`
  },
  physics: {
    name: "Physics Problem Solver",
    tagline: "Senior A/L Physics Tutor • Formula Derivations & Mechanical Calculations",
    instruction: `You are the ZNC Physics Problem Solver & Senior A/L Physics Tutor for Sri Lankan G.C.E. A/L students.
Specialization:
- Mechanics (Kinematics, Newton's Laws, Circular Motion, Work/Energy/Power, Hydrostatics, Fluid Dynamics & Bernoulli)
- Waves & Oscillations (SHM, Doppler Effect, Wave Superposition, Sound & Light Waves)
- Thermal Physics (Heat transfer, Gas Laws, Thermodynamics first law, Calorimetry)
- Gravitational, Electrostatic, and Magnetic Fields
- Current Electricity (Kirchhoff's Laws, Potentiometer, Wheatstone bridge)
- Electronics & Modern Physics (Semiconductors, Logic gates, Photoelectric effect, Atomic spectra)

Problem-Solving Protocol:
1. Identify given values and requested quantities with proper SI units.
2. Clearly state governing physics principles and standard equations (e.g. F = ma, v² = u² + 2as, P + 1/2ρv² + ρgh = const).
3. Provide rigorous step-by-step substitution and algebraic simplification.
4. Conclude with final answer with magnitude, direction (if vector), and SI units.
5. Add an "A/L Past Paper Insight" tip for exam success.`
  },
  maths: {
    name: "Combined Maths Specialist",
    tagline: "Pure & Applied Mathematics Tutor • Rigorous Proofs & Algebraic Solutions",
    instruction: `You are the ZNC Combined Mathematics Specialist & Master Tutor for Sri Lankan A/L students.
Specialization:
- Pure Mathematics: Algebra, Roots of Quadratics, Polynomials, Remainder Theorem, Binomial Theorem, Mathematical Induction, Complex Numbers & Argand Diagrams, Trigonometry identities & equations, Limits, Differentiation, Integration (by parts, partial fractions, substitution), Coordinate Geometry (Straight lines, Circles).
- Applied Mathematics: Vectors (Scalar & Vector products), Coplanar Forces, Equilibrium of Rigid Bodies, Friction, Newton's Laws of Motion, Work-Energy-Power, Projectiles on horizontal and inclined planes, Direct & Oblique Impacts, Circular Motion, Relative Velocity, Jointed Rods and Frameworks, Probability & Statistics.

Protocol:
1. State definitions, theorems, and conditions clearly.
2. Present structured, line-by-line algebraic proofs and geometric reasoning.
3. Emphasize standard A/L methods preferred by Department of Examinations marking schemes.`
  },
  chemistry: {
    name: "Chemistry Master",
    tagline: "General, Physical, Inorganic & Organic Chemistry Expert",
    instruction: `You are the ZNC Chemistry Academic Tutor for Sri Lankan A/L Chemistry.
Specialization:
- Atomic Structure, Chemical Bonding, and Periodic Trends
- Chemical Calculations (Mole concept, Stoichiometry, Titrations)
- Gaseous State and Real Gases (van der Waals equation)
- Chemical Energetics & Thermodynamics (Hess's Law, Born-Haber cycle, Entropy, Gibbs Free Energy)
- Chemical Kinetics (Rate laws, Collision theory, Arrhenius equation)
- Chemical & Ionic Equilibrium (Kc, Kp, pH, Buffer solutions, Solubility product Ksp, Common ion effect)
- Inorganic Chemistry (Characteristics, reactions, and qualitative analysis of s, p, and 3d block elements)
- Organic Chemistry (Mechanisms: SN1, SN2, Electrophilic Addition, Electrophilic Aromatic Substitution, Carbonyl addition, Aldol condensation, tests for functional groups).

Protocol:
1. Always write balanced chemical equations with state symbols (s, l, g, aq).
2. Explicitly specify reagents, catalysts, and reaction conditions (temperature, pressure) for organic conversions.`
  },
  biology: {
    name: "Biology Resource Book Expert",
    tagline: "NIE Resource Book Specialist • Precise Definitions & Essay Marking Criteria",
    instruction: `You are the ZNC Biology Specialist & NIE Resource Book Advisor for Sri Lankan G.C.E. A/L Biology.
Guidelines:
1. Strictly follow the terminology and definitions presented in the official National Institute of Education (NIE) Sri Lanka G.C.E. A/L Biology Resource Books.
2. Structure essay questions as bulleted marking criteria matching past paper marking schemes.
3. Clearly detail biochemical pathways (Cellular Respiration, Photosynthesis light & dark reactions, DNA Replication, Protein Synthesis).
4. Provide comparative tables when comparing anatomical or physiological structures.`
  }
};

function initGeminiChat() {
  // 1. Restore conversation history from localStorage
  try {
    const saved = localStorage.getItem("znc_gemini_chat_history");
    if (saved) {
      const parsed = JSON.parse(saved);
      if (Array.isArray(parsed) && parsed.length > 0) {
        STATE.gemini.history = parsed;
      }
    }
  } catch (e) {
    console.warn("Gemini history restore error:", e);
  }

  // 2. Restore selected model and role preference
  try {
    const savedModel = localStorage.getItem("znc_gemini_selected_model");
    if (savedModel) {
      STATE.gemini.model = savedModel;
      const modelSelect = document.getElementById("geminiModelSelect");
      if (modelSelect) modelSelect.value = savedModel;
    }

    const savedRole = localStorage.getItem("znc_gemini_selected_role");
    if (savedRole && GEMINI_ROLES[savedRole]) {
      STATE.gemini.role = savedRole;
      const roleSelect = document.getElementById("geminiRoleSelect");
      if (roleSelect) roleSelect.value = savedRole;
      const roleLabel = document.getElementById("activeRoleLabel");
      if (roleLabel) roleLabel.innerText = GEMINI_ROLES[savedRole].name;
    }
  } catch (e) {}

  // 3. Render initial welcome card if history is empty
  renderGeminiChat();
  checkGeminiBackendStatus();
}

function onGeminiRoleChange(newRole) {
  if (GEMINI_ROLES[newRole]) {
    STATE.gemini.role = newRole;
    try {
      localStorage.setItem("znc_gemini_selected_role", newRole);
    } catch (e) {}
    const roleLabel = document.getElementById("activeRoleLabel");
    if (roleLabel) roleLabel.innerText = GEMINI_ROLES[newRole].name;
    showToast(`Tutor switched to: ${GEMINI_ROLES[newRole].name}`);
  }
}

function onGeminiModelChange(newModel) {
  STATE.gemini.model = newModel;
  try {
    localStorage.setItem("znc_gemini_selected_model", newModel);
  } catch (e) {}
  showToast(`Active model: ${newModel}`);
}

function renderGeminiChat() {
  const container = document.getElementById("geminiChatMessages");
  if (!container) return;

  const currentUserName = STATE.currentUser ? STATE.currentUser.fullName : "Student";

  // If history is empty, display welcoming overview card
  if (!STATE.gemini.history || STATE.gemini.history.length === 0) {
    container.innerHTML = `
      <div class="bg-gradient-to-br from-white via-slate-50 to-purple-50/40 p-6 rounded-2xl border border-slate-200/80 shadow-xs space-y-4">
        <div class="flex items-center space-x-3">
          <div class="w-12 h-12 rounded-2xl bg-gradient-to-tr from-maroon via-purple-900 to-amber-500 p-0.5 shadow-md flex items-center justify-center shrink-0">
            <div class="w-full h-full bg-slate-900 rounded-[14px] flex items-center justify-center text-amber-300">
              <span class="material-symbols-outlined text-2xl">auto_awesome</span>
            </div>
          </div>
          <div>
            <h3 class="font-extrabold text-base text-slate-900">Assalamu Alaikum, ${currentUserName}!</h3>
            <p class="text-xs text-slate-600">I am your official ZNC Science AI Academic Tutor powered by Google Gemini.</p>
          </div>
        </div>

        <p class="text-xs text-slate-600 leading-relaxed">
          I am trained on the complete Sri Lankan G.C.E. Advanced Level Science curriculum. You can ask me to solve physics calculations, derive mathematical equations, explain reaction mechanisms, clarify resource book concepts, or provide exam strategies.
        </p>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
          <div class="p-3 rounded-xl bg-white border border-slate-200 shadow-2xs">
            <p class="text-xs font-bold text-maroon flex items-center gap-1.5">
              <span class="material-symbols-outlined text-[16px]">psychology</span> Multi-Turn Context
            </p>
            <p class="text-[11px] text-slate-500 mt-1">I remember our previous queries in this thread so you can ask follow-up questions naturally.</p>
          </div>
          <div class="p-3 rounded-xl bg-white border border-slate-200 shadow-2xs">
            <p class="text-xs font-bold text-amber-700 flex items-center gap-1.5">
              <span class="material-symbols-outlined text-[16px]">memory</span> Dynamic Model Selection
            </p>
            <p class="text-[11px] text-slate-500 mt-1">Use <strong>gemini-3.1-pro</strong> for complex math proofs, <strong>gemini-3.5-flash</strong> for general syllabus queries, or <strong>gemini-3.1-flash-lite</strong> for speed.</p>
          </div>
        </div>

        <div class="pt-2 border-t border-slate-200/60 flex flex-wrap items-center gap-1.5">
          <span class="text-[11px] font-bold text-slate-400 mr-1">Quick Starters:</span>
          <button onclick="sendQuickGeminiPrompt('Explain the Doppler effect in sound waves with moving source vs moving observer equations.')" class="text-[11px] bg-slate-100 hover:bg-amber-100 text-slate-700 font-medium px-2.5 py-1 rounded-lg transition">
            🔊 Doppler Effect Equations
          </button>
          <button onclick="sendQuickGeminiPrompt('What is the method of integration by parts in Combined Mathematics? Give an example.')" class="text-[11px] bg-slate-100 hover:bg-amber-100 text-slate-700 font-medium px-2.5 py-1 rounded-lg transition">
            📐 Integration by Parts
          </button>
          <button onclick="sendQuickGeminiPrompt('Explain the SN1 and SN2 reaction mechanisms in organic alkyl halides with stereochemistry.')" class="text-[11px] bg-slate-100 hover:bg-amber-100 text-slate-700 font-medium px-2.5 py-1 rounded-lg transition">
            🧪 SN1 vs SN2 Mechanisms
          </button>
        </div>
      </div>
    `;
    return;
  }

  // Render message history thread
  let html = "";
  for (let i = 0; i < STATE.gemini.history.length; i++) {
    const msg = STATE.gemini.history[i];
    const isUser = msg.role === "user";

    if (isUser) {
      const userInitials = (STATE.currentUser ? STATE.currentUser.fullName.charAt(0) : "S").toUpperCase();
      html += `
        <div class="flex items-start justify-end space-x-2.5 pl-6 sm:pl-16">
          <div class="flex flex-col items-end">
            <div class="bg-gradient-to-r from-maroon to-maroon-dark text-white p-3.5 sm:p-4 rounded-2xl rounded-tr-xs shadow-sm max-w-2xl border border-gold/20">
              <p class="text-xs sm:text-sm whitespace-pre-wrap leading-relaxed">${escapeHtml(msg.text)}</p>
            </div>
            <div class="flex items-center space-x-2 mt-1 mr-1 text-[10px] text-slate-400 font-medium">
              <span>${formatChatTime(msg.timestamp)}</span>
              <span>•</span>
              <span>You</span>
            </div>
          </div>
          <div class="w-8 h-8 rounded-full bg-maroon text-gold font-bold text-xs flex items-center justify-center border border-gold/40 shrink-0 shadow-xs">
            ${userInitials}
          </div>
        </div>
      `;
    } else {
      const formattedContent = formatGeminiMarkdown(msg.text);
      const modelTag = msg.modelUsed || STATE.gemini.model;
      const msgId = msg.id || `msg_ai_${i}`;

      html += `
        <div class="flex items-start space-x-2.5 pr-6 sm:pr-16">
          <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-amber-400 to-amber-600 p-0.5 shrink-0 shadow-xs">
            <div class="w-full h-full bg-slate-900 rounded-[7px] flex items-center justify-center">
              <span class="material-symbols-outlined text-amber-300 text-base">auto_awesome</span>
            </div>
          </div>
          <div class="flex-1 max-w-3xl">
            <div class="bg-white p-4 sm:p-5 rounded-2xl rounded-tl-xs shadow-xs border border-slate-200/90 text-slate-800">
              <div class="flex items-center justify-between border-b border-slate-100 pb-2 mb-3">
                <div class="flex items-center space-x-2">
                  <span class="text-xs font-bold text-slate-900">Gemini Academic Tutor</span>
                  <span class="text-[10px] font-extrabold uppercase px-2 py-0.2 rounded-full bg-purple-100 text-purple-800 border border-purple-200">
                    ${modelTag}
                  </span>
                </div>
                <button onclick="copyGeminiMessage('${msgId}')" title="Copy response to clipboard" class="text-slate-400 hover:text-slate-700 text-xs flex items-center gap-1 font-semibold p-1 hover:bg-slate-100 rounded transition">
                  <span class="material-symbols-outlined text-[15px]">content_copy</span>
                  <span class="text-[10px] hidden sm:inline">Copy</span>
                </button>
              </div>

              <!-- Message Body -->
              <div id="${msgId}_content" class="text-xs sm:text-sm text-slate-800 space-y-2 leading-relaxed">
                ${formattedContent}
              </div>
            </div>

            <div class="flex items-center space-x-2 mt-1 ml-1 text-[10px] text-slate-400 font-medium">
              <span>${formatChatTime(msg.timestamp)}</span>
              <span>•</span>
              <span>ZNC Science Portal</span>
            </div>
          </div>
        </div>
      `;
    }
  }

  // Thinking / Loading Indicator
  if (STATE.gemini.isLoading) {
    html += `
      <div class="flex items-start space-x-2.5 pr-6 sm:pr-16">
        <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-amber-400 to-amber-600 p-0.5 shrink-0 shadow-xs animate-pulse">
          <div class="w-full h-full bg-slate-900 rounded-[7px] flex items-center justify-center">
            <span class="material-symbols-outlined text-amber-300 text-base">psychology</span>
          </div>
        </div>
        <div class="bg-white p-4 rounded-2xl rounded-tl-xs shadow-xs border border-slate-200 text-slate-700 flex items-center space-x-3">
          <div class="flex space-x-1">
            <div class="w-2 h-2 rounded-full bg-purple-600 animate-bounce" style="animation-delay: 0s"></div>
            <div class="w-2 h-2 rounded-full bg-purple-600 animate-bounce" style="animation-delay: 0.2s"></div>
            <div class="w-2 h-2 rounded-full bg-purple-600 animate-bounce" style="animation-delay: 0.4s"></div>
          </div>
          <span class="text-xs font-semibold text-slate-600">Gemini is analyzing syllabus and drafting step-by-step answer...</span>
        </div>
      </div>
    `;
  }

  container.innerHTML = html;
  scrollGeminiToBottom();
}

function scrollGeminiToBottom() {
  const scrollContainer = document.getElementById("geminiChatMessagesContainer");
  if (scrollContainer) {
    setTimeout(() => {
      scrollContainer.scrollTop = scrollContainer.scrollHeight;
    }, 50);
  }
}

function handleGeminiFormSubmit(e) {
  if (e) e.preventDefault();
  const input = document.getElementById("geminiChatInput");
  if (!input) return;
  const text = input.value.trim();
  if (!text) return;
  input.value = "";
  sendGeminiMessage(text);
}

function handleGeminiInputKeyDown(e) {
  if (e.key === "Enter" && !e.shiftKey) {
    e.preventDefault();
    handleGeminiFormSubmit();
  }
}

function sendQuickGeminiPrompt(text) {
  navigateTo("gemini");
  sendGeminiMessage(text);
}

function openGeminiChatQuick() {
  navigateTo("gemini");
  setTimeout(() => {
    const input = document.getElementById("geminiChatInput");
    if (input) {
      input.focus();
      input.scrollIntoView({ behavior: "smooth" });
    }
  }, 150);
}

function askGeminiQuick(encodedQuestionText) {
  try {
    const text = decodeURIComponent(encodedQuestionText);
    navigateTo("gemini");
    const prompt = `Student Discussion Question:\n"${text}"\n\nPlease provide a clear, step-by-step academic explanation, state all relevant physics/chemistry/maths formulas, and give exam tips for Sri Lankan A/L students.`;
    sendGeminiMessage(prompt);
  } catch (e) {
    console.error("askGeminiQuick error:", e);
  }
}

function askGeminiAboutQuizQuestion() {
  if (!STATE.activeQuiz || !STATE.activeQuiz.questions) return;
  const q = STATE.activeQuiz.questions[STATE.activeQuizQuestionIdx];
  if (!q) return;

  const unit = PHYSICS_UNITS.find(u => u.id === STATE.activeQuiz.unitId);
  const unitName = unit ? unit.name : "Physics";

  const prompt = `Physics Quiz Question from ${unitName}:\n"${q.q}"\n\nOptions:\n${q.options.map((o, idx) => `${idx + 1}) ${o}`).join("\n")}\n\nPlease explain the underlying physics principles, relevant equations, and concept steps required to solve this question correctly without directly giving away the letter option.`;

  navigateTo("gemini");
  sendGeminiMessage(prompt);
}

async function checkGeminiBackendStatus() {
  const statusBadge = document.getElementById("geminiStatusBadge");
  try {
    const res = await fetch("/api/gemini/status");
    if (res.ok) {
      const data = await res.json();
      if (statusBadge) {
        statusBadge.innerHTML = `<span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-ping"></span> Live A/L AI`;
        statusBadge.className = "px-2 py-0.5 rounded-full text-[10px] font-black uppercase tracking-wider bg-emerald-500/20 text-emerald-300 border border-emerald-400/40 flex items-center gap-1";
      }
      return true;
    }
  } catch (e) {
    console.warn("Backend status check error:", e);
  }

  const customKey = localStorage.getItem("znc_custom_gemini_api_key");
  if (statusBadge) {
    if (customKey) {
      statusBadge.innerHTML = `<span class="w-1.5 h-1.5 rounded-full bg-blue-400"></span> Client Key Active`;
      statusBadge.className = "px-2 py-0.5 rounded-full text-[10px] font-black uppercase tracking-wider bg-blue-500/20 text-blue-300 border border-blue-400/40 flex items-center gap-1";
    } else {
      statusBadge.innerHTML = `<span class="w-1.5 h-1.5 rounded-full bg-amber-400"></span> Standalone / Direct`;
      statusBadge.className = "px-2 py-0.5 rounded-full text-[10px] font-black uppercase tracking-wider bg-amber-500/20 text-amber-300 border border-amber-400/40 flex items-center gap-1";
    }
  }
  return false;
}

function promptGeminiApiKey() {
  const current = localStorage.getItem("znc_custom_gemini_api_key") || "";
  const key = prompt(
    "Optional Client-Side Google Gemini API Key:\n\n" +
    "If your server is in static hosting mode or you prefer using your own quota, enter your Gemini API Key below.\n" +
    "(Leave empty to use the Portal server proxy)",
    current
  );

  if (key !== null) {
    if (key.trim()) {
      localStorage.setItem("znc_custom_gemini_api_key", key.trim());
      showToast("Custom Gemini API Key saved for client-side queries!");
    } else {
      localStorage.removeItem("znc_custom_gemini_api_key");
      showToast("Reset to server default Gemini connection.");
    }
    checkGeminiBackendStatus();
  }
}

async function callDirectGeminiApi(apiKey, messages, model, systemInstruction) {
  const formattedContents = [];
  for (const msg of messages) {
    if (!msg || !msg.text) continue;
    const role = (msg.role === "model" || msg.role === "assistant") ? "model" : "user";
    formattedContents.push({
      role: role,
      parts: [{ text: String(msg.text).trim() }]
    });
  }
  if (formattedContents.length === 0) throw new Error("No message text provided.");
  if (formattedContents[formattedContents.length - 1].role !== "user") {
    formattedContents.push({ role: "user", parts: [{ text: "Please continue or summarize." }] });
  }

  const payload = {
    contents: formattedContents,
    generationConfig: {
      temperature: 0.7,
      maxOutputTokens: 2048,
      topP: 0.95
    }
  };
  if (systemInstruction) {
    payload.systemInstruction = { parts: [{ text: systemInstruction }] };
  }

  const apiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`;
  const resp = await fetch(apiUrl, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });

  const contentType = resp.headers.get("content-type") || "";
  if (!contentType.includes("application/json")) {
    const raw = await resp.text();
    throw new Error(`Google API returned ${resp.status}: ${raw.substring(0, 120)}`);
  }

  const data = await resp.json();
  if (!resp.ok) {
    throw new Error(data.error ? data.error.message : `API returned HTTP ${resp.status}`);
  }
  const candidate = data.candidates?.[0];
  if (!candidate) throw new Error("No candidate returned by Gemini.");
  const text = candidate.content?.parts?.map(p => p.text || "").join("\n") || "";
  if (!text.trim()) throw new Error("Empty text received from Gemini.");
  return text;
}

async function sendGeminiMessage(userText) {
  if (!userText || STATE.gemini.isLoading) return;

  const newMsg = {
    id: "msg_user_" + Date.now(),
    role: "user",
    text: userText,
    timestamp: Date.now()
  };

  STATE.gemini.history.push(newMsg);
  STATE.gemini.isLoading = true;
  renderGeminiChat();

  // Save updated history
  try {
    localStorage.setItem("znc_gemini_chat_history", JSON.stringify(STATE.gemini.history));
  } catch (e) {}

  // Update send button state
  const btnSend = document.getElementById("btnGeminiSend");
  const sendIcon = document.getElementById("geminiSendIcon");
  const sendText = document.getElementById("geminiSendText");
  if (btnSend) btnSend.disabled = true;
  if (sendIcon) sendIcon.innerText = "hourglass_top";
  if (sendText) sendText.innerText = "Thinking...";

  // Prepare payload with multi-turn conversation history (exclude error messages)
  const activeRoleConfig = GEMINI_ROLES[STATE.gemini.role] || GEMINI_ROLES.general;
  const messagesPayload = STATE.gemini.history
    .filter(m => !m.id || !m.id.startsWith("msg_ai_err_"))
    .map(m => ({
      role: m.role === "user" ? "user" : "model",
      text: m.text
    }));

  try {
    let replyText = null;
    let modelUsed = STATE.gemini.model;

    const customApiKey = (localStorage.getItem("znc_custom_gemini_api_key") || "").trim();

    let serverError = null;
    // 1. Try server endpoint first
    try {
      const response = await fetch("/api/gemini", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          messages: messagesPayload,
          model: STATE.gemini.model,
          systemInstruction: activeRoleConfig.instruction
        })
      });

      const contentType = response.headers.get("content-type") || "";
      if (contentType.includes("application/json")) {
        const data = await response.json();
        if (data.success && data.reply) {
          replyText = data.reply;
          modelUsed = data.modelUsed || STATE.gemini.model;
        } else {
          serverError = new Error(data.error || `Server error (${response.status})`);
        }
      } else {
        const rawText = await response.text();
        serverError = new Error(`Server returned HTTP ${response.status} (${rawText.trim() || response.statusText})`);
      }
    } catch (netErr) {
      serverError = netErr;
    }

    // 2. If server failed and custom API key is present, fallback to direct Google Generative Language API
    if (!replyText) {
      if (customApiKey) {
        try {
          replyText = await callDirectGeminiApi(customApiKey, messagesPayload, STATE.gemini.model, activeRoleConfig.instruction);
          modelUsed = `${STATE.gemini.model} (Direct)`;
        } catch (directErr) {
          throw new Error(`Server proxy error: ${serverError?.message || 'Failed'}. Direct API error: ${directErr.message}`);
        }
      } else if (serverError) {
        throw serverError;
      }
    }

    if (replyText) {
      STATE.gemini.history.push({
        id: "msg_ai_" + Date.now(),
        role: "model",
        text: replyText,
        modelUsed: modelUsed,
        timestamp: Date.now()
      });
    }
  } catch (err) {
    console.error("Gemini Tutor Error:", err);
    STATE.gemini.history.push({
      id: "msg_ai_err_" + Date.now(),
      role: "model",
      text: `⚠️ **AI Tutor Notice**: ${err.message}\n\n*Tip*: If this persists, verify your server is running (\`node server.js\`) or click the 🔑 API Key button to supply a personal Gemini key.`,
      modelUsed: STATE.gemini.model,
      timestamp: Date.now()
    });
  } finally {
    STATE.gemini.isLoading = false;
    try {
      localStorage.setItem("znc_gemini_chat_history", JSON.stringify(STATE.gemini.history));
    } catch (e) {}

    if (btnSend) btnSend.disabled = false;
    if (sendIcon) sendIcon.innerText = "send";
    if (sendText) sendText.innerText = "Send";

    renderGeminiChat();
  }
}

function clearGeminiChat() {
  if (confirm("Are you sure you want to clear your conversation history with Gemini AI Tutor?")) {
    STATE.gemini.history = [];
    try {
      localStorage.removeItem("znc_gemini_chat_history");
    } catch (e) {}
    renderGeminiChat();
    showToast("Conversation history cleared.");
  }
}

function exportGeminiChat() {
  if (!STATE.gemini.history || STATE.gemini.history.length === 0) {
    showToast("No conversation notes to export.");
    return;
  }

  let textNotes = `ZNC SCIENCE PORTAL — GEMINI ACADEMIC TUTOR NOTES\n`;
  textNotes += `Export Date: ${new Date().toLocaleString()}\n`;
  textNotes += `Student: ${STATE.currentUser ? STATE.currentUser.fullName : "Guest"}\n`;
  textNotes += `Tutor Role: ${GEMINI_ROLES[STATE.gemini.role]?.name || "Science Mentor"}\n`;
  textNotes += `========================================================\n\n`;

  STATE.gemini.history.forEach((m, idx) => {
    const sender = m.role === "user" ? "YOU" : `GEMINI AI TUTOR (${m.modelUsed || "Gemini"})`;
    textNotes += `[${sender} - ${new Date(m.timestamp).toLocaleTimeString()}]\n`;
    textNotes += `${m.text}\n\n--------------------------------------------------------\n\n`;
  });

  navigator.clipboard.writeText(textNotes)
    .then(() => {
      showToast("📋 Conversation notes copied to clipboard!");
    })
    .catch(() => {
      showToast("Could not copy notes to clipboard.");
    });
}

function copyGeminiMessage(msgId) {
  const contentEl = document.getElementById(msgId + "_content");
  if (!contentEl) return;
  const text = contentEl.innerText;
  navigator.clipboard.writeText(text)
    .then(() => showToast("Answer copied to clipboard!"))
    .catch(() => showToast("Failed to copy answer."));
}

function escapeHtml(text) {
  if (!text) return "";
  return text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

function formatChatTime(timestamp) {
  if (!timestamp) return "";
  return new Date(timestamp).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
}

function formatGeminiMarkdown(text) {
  if (!text) return "";

  let escaped = escapeHtml(text);

  // Code blocks: ```code```
  escaped = escaped.replace(/```([a-zA-Z0-9_-]*)\n([\s\S]*?)```/g, (match, lang, code) => {
    return `<div class="my-2.5 rounded-xl overflow-hidden border border-slate-700 bg-slate-900 shadow-xs">
      <div class="bg-slate-800 px-3 py-1 text-[10px] font-mono text-slate-400 font-bold uppercase tracking-wider flex justify-between items-center">
        <span>${lang || 'CODE / FORMULA'}</span>
      </div>
      <pre class="p-3 text-xs font-mono text-emerald-300 overflow-x-auto leading-relaxed"><code>${code.trim()}</code></pre>
    </div>`;
  });

  // Inline code: `code`
  escaped = escaped.replace(/`([^`]+)`/g, '<code class="bg-slate-100 text-purple-900 px-1.5 py-0.5 rounded text-[11px] font-mono font-bold border border-slate-200">$1</code>');

  // Bold: **text**
  escaped = escaped.replace(/\*\*([^*]+)\*\*/g, '<strong class="font-bold text-slate-900">$1</strong>');

  // Italic: *text*
  escaped = escaped.replace(/(^|[^*])\*([^*]+)\*/g, '$1<em class="italic text-slate-800">$2</em>');

  // Headings
  escaped = escaped.replace(/^### (.*$)/gim, '<h4 class="font-black text-xs sm:text-sm text-maroon mt-3 mb-1">$1</h4>');
  escaped = escaped.replace(/^## (.*$)/gim, '<h3 class="font-black text-sm text-slate-900 mt-3.5 mb-1.5 border-b pb-1">$1</h3>');

  // Lines to paragraphs / lists
  const lines = escaped.split("\n");
  let inList = false;
  let formatted = [];

  for (let line of lines) {
    const trimmed = line.trim();
    if (trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("• ")) {
      if (!inList) {
        formatted.push('<ul class="list-disc pl-5 my-1.5 space-y-1 text-slate-800">');
        inList = true;
      }
      formatted.push(`<li>${trimmed.substring(2)}</li>`);
    } else if (/^\d+\.\s/.test(trimmed)) {
      if (inList) {
        formatted.push('</ul>');
        inList = false;
      }
      formatted.push(`<div class="pl-2 my-1 text-slate-800 font-semibold">${trimmed}</div>`);
    } else {
      if (inList) {
        formatted.push('</ul>');
        inList = false;
      }
      if (trimmed === "") {
        formatted.push('<div class="h-1.5"></div>');
      } else {
        formatted.push(`<p class="my-1 leading-relaxed text-slate-800">${line}</p>`);
      }
    }
  }

  if (inList) {
    formatted.push('</ul>');
  }

  return formatted.join("");
}

