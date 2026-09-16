// Dynamically configure backend API URL for local and cloud environments
const API_URL = (typeof window !== "undefined" && window.API_BASE_URL)
    || (typeof localStorage !== "undefined" && localStorage.getItem("customApiUrl"))
    || "http://localhost:8080";

/* =========================
   AUTH TOKEN & SESSION HELPERS
========================= */

function getAuthToken() {
    return localStorage.getItem("token");
}

function getUserEmail() {
    return localStorage.getItem("userEmail");
}

function getUserRole() {
    return (localStorage.getItem("userRole") || "").toUpperCase();
}

function setAuthSession(authData) {
    if (authData && authData.token) {
        localStorage.setItem("token", authData.token);
        localStorage.setItem("userEmail", authData.email || "");
        localStorage.setItem("userRole", authData.role || "");
    }
    updateAuthUI();
    loadDashboardForCurrentRole();
    loadUserProfile();
}

function logoutUser() {
    localStorage.removeItem("token");
    localStorage.removeItem("userEmail");
    localStorage.removeItem("userRole");
    updateAuthUI();

    const loginMsg = document.getElementById("loginMessage");
    if (loginMsg) {
        loginMsg.textContent = "You have been logged out successfully.";
        loginMsg.style.color = "#4b5563";
    }

    // Hide dashboards and profile
    const cd = document.getElementById("client-dashboard");
    const fd = document.getElementById("freelancer-dashboard");
    const prof = document.getElementById("user-profile");
    if (cd) cd.style.display = "none";
    if (fd) fd.style.display = "none";
    if (prof) prof.style.display = "none";
}

function updateAuthUI() {
    const token = getAuthToken();
    const userEmail = getUserEmail();
    const userRole = getUserRole();

    const navUserStatus = document.getElementById("navUserStatus");
    const navLoginLink = document.getElementById("navLoginLink");
    const navRegisterLink = document.getElementById("navRegisterLink");
    const navLogoutBtn = document.getElementById("navLogoutBtn");

    const navClientDashboardLink = document.getElementById("navClientDashboardLink");
    const navFreelancerDashboardLink = document.getElementById("navFreelancerDashboardLink");
    const navProfileLink = document.getElementById("navProfileLink");
    const navPostProjectLink = document.getElementById("navPostProjectLink");
    const navProposalLink = document.getElementById("navProposalLink");

    const clientDashboardSection = document.getElementById("client-dashboard");
    const freelancerDashboardSection = document.getElementById("freelancer-dashboard");
    const profileSection = document.getElementById("user-profile");

    if (token && userEmail) {
        if (navUserStatus) {
            navUserStatus.textContent = `👤 ${userEmail} (${userRole})`;
            navUserStatus.style.display = "inline";
        }
        if (navLoginLink) navLoginLink.style.display = "none";
        if (navRegisterLink) navRegisterLink.style.display = "none";
        if (navLogoutBtn) navLogoutBtn.style.display = "inline-block";

        if (navProfileLink) navProfileLink.style.display = "inline-block";
        if (profileSection) profileSection.style.display = "block";

        if (userRole === "CLIENT") {
            if (navClientDashboardLink) navClientDashboardLink.style.display = "inline-block";
            if (navFreelancerDashboardLink) navFreelancerDashboardLink.style.display = "none";
            if (clientDashboardSection) clientDashboardSection.style.display = "block";
            if (freelancerDashboardSection) freelancerDashboardSection.style.display = "none";
            if (navPostProjectLink) navPostProjectLink.style.display = "inline-block";
            if (navProposalLink) navProposalLink.style.display = "none";
        } else if (userRole === "FREELANCER") {
            if (navClientDashboardLink) navClientDashboardLink.style.display = "none";
            if (navFreelancerDashboardLink) navFreelancerDashboardLink.style.display = "inline-block";
            if (clientDashboardSection) clientDashboardSection.style.display = "none";
            if (freelancerDashboardSection) freelancerDashboardSection.style.display = "block";
            if (navPostProjectLink) navPostProjectLink.style.display = "none";
            if (navProposalLink) navProposalLink.style.display = "inline-block";

            const emailInput = document.getElementById("freelancerEmail");
            if (emailInput) emailInput.value = userEmail;
        }
    } else {
        if (navUserStatus) navUserStatus.style.display = "none";
        if (navLoginLink) navLoginLink.style.display = "inline";
        if (navRegisterLink) navRegisterLink.style.display = "inline";
        if (navLogoutBtn) navLogoutBtn.style.display = "none";

        if (navClientDashboardLink) navClientDashboardLink.style.display = "none";
        if (navFreelancerDashboardLink) navFreelancerDashboardLink.style.display = "none";
        if (navProfileLink) navProfileLink.style.display = "none";

        if (clientDashboardSection) clientDashboardSection.style.display = "none";
        if (freelancerDashboardSection) freelancerDashboardSection.style.display = "none";
        if (profileSection) profileSection.style.display = "none";

        if (navPostProjectLink) navPostProjectLink.style.display = "inline-block";
        if (navProposalLink) navProposalLink.style.display = "inline-block";
    }
}

function getAuthHeaders(extraHeaders = {}) {
    const headers = { ...extraHeaders };
    const token = getAuthToken();
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }
    return headers;
}

/* =========================
   REGISTER USER
========================= */

document.getElementById("registerForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const name = document.getElementById("registerName").value;
    const email = document.getElementById("registerEmail").value;
    const password = document.getElementById("registerPassword").value;
    const role = document.getElementById("registerRole").value;

    const message = document.getElementById("registerMessage");

    try {
        const response = await fetch(`${API_URL}/users/register`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                name: name,
                email: email,
                password: password,
                role: role
            })
        });

        if (response.ok) {
            message.textContent = "Registration successful! You can now log in.";
            message.style.color = "green";
            document.getElementById("registerForm").reset();
        } else {
            const errorJson = await response.json().catch(() => null);
            let errorMsg = "Registration failed.";
            if (errorJson && errorJson.errors) {
                errorMsg = Object.values(errorJson.errors).join(", ");
            } else if (errorJson && errorJson.message) {
                errorMsg = errorJson.message;
            }
            message.textContent = errorMsg;
            message.style.color = "red";
        }
    } catch (error) {
        message.textContent = "Cannot connect to the server. Make sure Spring Boot is running.";
        message.style.color = "red";
        console.error(error);
    }
});


/* =========================
   LOGIN USER
========================= */

document.getElementById("loginForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const email = document.getElementById("loginEmail").value;
    const password = document.getElementById("loginPassword").value;

    const message = document.getElementById("loginMessage");

    try {
        const response = await fetch(`${API_URL}/users/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email: email,
                password: password
            })
        });

        if (response.ok) {
            const data = await response.json();
            setAuthSession(data);

            message.textContent = `Login successful! Welcome back, ${data.email}.`;
            message.style.color = "green";
            document.getElementById("loginForm").reset();
        } else {
            const errorJson = await response.json().catch(() => null);
            message.textContent = (errorJson && errorJson.message) || "Invalid email or password.";
            message.style.color = "red";
        }
    } catch (error) {
        message.textContent = "Cannot connect to the server. Make sure Spring Boot is running.";
        message.style.color = "red";
        console.error(error);
    }
});


/* =========================
   LOAD PROJECTS WITH FILTER & SEARCH (Feature 4)
========================= */

async function loadProjects(searchParams = {}) {
    const projectList = document.getElementById("projectList");

    try {
        let url = `${API_URL}/projects`;
        const queryParts = [];
        if (searchParams.query) queryParts.push(`query=${encodeURIComponent(searchParams.query)}`);
        if (searchParams.category) queryParts.push(`category=${encodeURIComponent(searchParams.category)}`);
        if (searchParams.status) queryParts.push(`status=${encodeURIComponent(searchParams.status)}`);
        if (searchParams.minBudget) queryParts.push(`minBudget=${encodeURIComponent(searchParams.minBudget)}`);
        if (searchParams.maxBudget) queryParts.push(`maxBudget=${encodeURIComponent(searchParams.maxBudget)}`);
        if (searchParams.sortBy) queryParts.push(`sortBy=${encodeURIComponent(searchParams.sortBy)}`);

        if (queryParts.length > 0) {
            url += `?${queryParts.join("&")}`;
        }

        const response = await fetch(url);
        if (!response.ok) {
            throw new Error("Failed to load projects");
        }

        const projects = await response.json();
        projectList.innerHTML = "";

        if (projects.length === 0) {
            projectList.innerHTML = "<p style='color:#64748b; font-size:16px;'>No projects found matching your criteria.</p>";
            return;
        }

        projects.forEach(project => {
            const projectCard = document.createElement("div");
            projectCard.className = "project-card";

            const status = (project.status || "OPEN").toUpperCase();
            let statusBadgeClass = "badge-open";
            if (status === "IN_PROGRESS") statusBadgeClass = "badge-in_progress";
            else if (status === "COMPLETED") statusBadgeClass = "badge-completed";

            const proposalCount = project.proposalCount !== undefined ? project.proposalCount : "";
            const isCompleted = status === "COMPLETED";

            let applyBtnHtml = "";
            if (!isCompleted) {
                applyBtnHtml = `
                    <button class="project-button" onclick="selectProject(${project.id})">
                        Submit Proposal
                    </button>
                `;
            } else {
                applyBtnHtml = `<span style="color:#64748b; font-weight:600; font-size:13px; align-self:center;">Project Completed</span>`;
            }

            projectCard.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:10px;">
                    <h3 style="margin:0;">${escapeHtml(project.title)}</h3>
                    <span class="status-badge ${statusBadgeClass}">${status}</span>
                </div>

                <p>${escapeHtml(project.description)}</p>

                <div style="display:flex; gap:20px; flex-wrap:wrap; font-size:14px; margin-bottom:12px;">
                    <p><strong>Budget:</strong> ₹${project.budget}</p>
                    <p><strong>Category:</strong> ${escapeHtml(project.category)}</p>
                    ${proposalCount !== "" ? `<p><strong>Proposals:</strong> ${proposalCount}</p>` : ""}
                    ${project.clientEmail ? `<p><strong>Client:</strong> ${escapeHtml(project.clientEmail)}</p>` : ""}
                </div>

                <div class="project-actions">
                    ${applyBtnHtml}
                    <button class="project-button secondary" onclick="viewProjectProposals(${project.id})">
                        View Proposals
                    </button>
                    <button class="project-button secondary" onclick="viewProjectReviews(${project.id})">
                        Reviews
                    </button>
                </div>
            `;

            projectList.appendChild(projectCard);
        });

    } catch (error) {
        projectList.innerHTML = `<p style="color:red;">Unable to load projects. Make sure Spring Boot is running.</p>`;
        console.error(error);
    }
}

function triggerProjectSearch() {
    const query = document.getElementById("projectSearchQuery")?.value;
    const category = document.getElementById("projectFilterCategory")?.value;
    const status = document.getElementById("projectFilterStatus")?.value;
    const minBudget = document.getElementById("projectFilterMinBudget")?.value;
    const maxBudget = document.getElementById("projectFilterMaxBudget")?.value;
    const sortBy = document.getElementById("projectSortBy")?.value;

    loadProjects({ query, category, status, minBudget, maxBudget, sortBy });
}

function clearProjectFilters() {
    if (document.getElementById("projectSearchQuery")) document.getElementById("projectSearchQuery").value = "";
    if (document.getElementById("projectFilterCategory")) document.getElementById("projectFilterCategory").value = "";
    if (document.getElementById("projectFilterStatus")) document.getElementById("projectFilterStatus").value = "";
    if (document.getElementById("projectFilterMinBudget")) document.getElementById("projectFilterMinBudget").value = "";
    if (document.getElementById("projectFilterMaxBudget")) document.getElementById("projectFilterMaxBudget").value = "";
    if (document.getElementById("projectSortBy")) document.getElementById("projectSortBy").value = "newest";
    loadProjects();
}


/* =========================
   SELECT PROJECT
========================= */

function selectProject(projectId) {
    const pidInput = document.getElementById("proposalProjectId");
    if (pidInput) pidInput.value = projectId;

    const emailInput = document.getElementById("freelancerEmail");
    const userEmail = getUserEmail();
    if (emailInput && userEmail) emailInput.value = userEmail;

    const sec = document.getElementById("proposal");
    if (sec) sec.scrollIntoView({ behavior: "smooth" });
}


/* =========================
   POST PROJECT
========================= */

document.getElementById("projectForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const title = document.getElementById("projectTitle").value;
    const description = document.getElementById("projectDescription").value;
    const budget = document.getElementById("projectBudget").value;
    const category = document.getElementById("projectCategory").value;

    const message = document.getElementById("projectMessage");

    try {
        const response = await fetch(`${API_URL}/projects`, {
            method: "POST",
            headers: getAuthHeaders({
                "Content-Type": "application/json"
            }),
            body: JSON.stringify({
                title: title,
                description: description,
                budget: Number(budget),
                category: category
            })
        });

        if (response.ok) {
            const project = await response.json();
            message.textContent = `Project "${project.title}" posted successfully!`;
            message.style.color = "green";
            document.getElementById("projectForm").reset();

            loadProjects();
            if (getUserRole() === "CLIENT") {
                loadClientDashboard();
            }
        } else if (response.status === 401) {
            message.textContent = "Please login to post a project.";
            message.style.color = "red";
        } else {
            const errorJson = await response.json().catch(() => null);
            let errorMsg = "Failed to post project.";
            if (errorJson && errorJson.errors) {
                errorMsg = Object.values(errorJson.errors).join(", ");
            } else if (errorJson && errorJson.message) {
                errorMsg = errorJson.message;
            }
            message.textContent = errorMsg;
            message.style.color = "red";
        }
    } catch (error) {
        message.textContent = "Cannot connect to the server. Make sure Spring Boot is running.";
        message.style.color = "red";
        console.error(error);
    }
});


/* =========================
   SUBMIT PROPOSAL (Feature 5)
========================= */

document.getElementById("proposalForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const projectId = document.getElementById("proposalProjectId").value;
    const freelancerEmail = document.getElementById("freelancerEmail").value;
    const coverLetter = document.getElementById("coverLetter").value;
    const proposedAmount = document.getElementById("proposedAmount").value;
    const estimatedDelivery = document.getElementById("estimatedDelivery") ? document.getElementById("estimatedDelivery").value : "";

    const message = document.getElementById("proposalMessage");

    try {
        const response = await fetch(`${API_URL}/proposals`, {
            method: "POST",
            headers: getAuthHeaders({
                "Content-Type": "application/json"
            }),
            body: JSON.stringify({
                projectId: Number(projectId),
                freelancerEmail: freelancerEmail,
                coverLetter: coverLetter,
                proposedAmount: Number(proposedAmount),
                estimatedDelivery: estimatedDelivery
            })
        });

        if (response.ok) {
            const proposal = await response.json();
            message.textContent = `Proposal submitted successfully! Proposal ID: ${proposal.id}`;
            message.style.color = "green";
            document.getElementById("proposalForm").reset();

            if (getUserRole() === "FREELANCER") {
                loadFreelancerDashboard();
            }
        } else if (response.status === 401) {
            message.textContent = "Please login to submit a proposal.";
            message.style.color = "red";
        } else {
            const errorJson = await response.json().catch(() => null);
            let errorMsg = "Failed to submit proposal.";
            if (errorJson && errorJson.errors) {
                errorMsg = Object.values(errorJson.errors).join(", ");
            } else if (errorJson && errorJson.message) {
                errorMsg = errorJson.message;
            }
            message.textContent = errorMsg;
            message.style.color = "red";
        }
    } catch (error) {
        message.textContent = "Cannot connect to the server. Make sure Spring Boot is running.";
        message.style.color = "red";
        console.error(error);
    }
});


/* =========================
   VIEW PROPOSALS BY PROJECT
========================= */

function viewProjectProposals(projectId) {
    const input = document.getElementById("searchProjectId");
    if (input) input.value = projectId;
    const section = document.getElementById("view-proposals");
    if (section) section.scrollIntoView({ behavior: "smooth" });
    fetchProposalsForProject(projectId);
}

async function fetchProposalsForProject(projectId) {
    const message = document.getElementById("viewProposalsMessage");
    const proposalsList = document.getElementById("proposalsList");

    message.textContent = "";
    proposalsList.innerHTML = "";

    const cleanedId = String(projectId).trim();
    if (!cleanedId || isNaN(Number(cleanedId)) || Number(cleanedId) <= 0) {
        message.textContent = "Please enter a valid positive Project ID.";
        message.style.color = "red";
        return;
    }

    const numericId = Number(cleanedId);

    try {
        message.textContent = `Loading proposals for Project #${numericId}...`;
        message.style.color = "#4b5563";

        const response = await fetch(`${API_URL}/proposals/project/${numericId}`);
        if (!response.ok) {
            const errorText = await response.text();
            message.textContent = errorText || `Failed to fetch proposals (Status: ${response.status}).`;
            message.style.color = "red";
            return;
        }

        const proposals = await response.json();
        if (!Array.isArray(proposals) || proposals.length === 0) {
            message.textContent = `No proposals found for Project #${numericId}.`;
            message.style.color = "#4b5563";
            return;
        }

        message.textContent = `Found ${proposals.length} proposal(s) for Project #${numericId}:`;
        message.style.color = "green";

        const currentRole = getUserRole();

        proposals.forEach(proposal => {
            const card = document.createElement("div");
            card.className = "proposal-card";

            const status = (proposal.status || "PENDING").toUpperCase();
            let badgeClass = "badge-pending";
            if (status === "ACCEPTED") badgeClass = "badge-accepted";
            else if (status === "REJECTED") badgeClass = "badge-rejected";

            let actionButtonsHtml = "";
            if (status === "PENDING" && currentRole === "CLIENT") {
                actionButtonsHtml = `
                    <div class="proposal-actions">
                        <button class="action-btn accept-btn" onclick="handleAcceptProposal(${proposal.id}, ${proposal.projectId})">
                            Accept
                        </button>
                        <button class="action-btn reject-btn" onclick="handleRejectProposal(${proposal.id}, ${proposal.projectId})">
                            Reject
                        </button>
                    </div>
                `;
            }

            card.innerHTML = `
                <div class="proposal-header">
                    <span class="proposal-id">Proposal #${proposal.id}</span>
                    <span class="status-badge ${badgeClass}">${status}</span>
                </div>
                <div class="proposal-body">
                    <p><strong>Project ID:</strong> ${proposal.projectId}</p>
                    <p><strong>Freelancer:</strong> ${escapeHtml(proposal.freelancerEmail)}</p>
                    <p><strong>Proposed Amount:</strong> ₹${proposal.proposedAmount}</p>
                    ${proposal.estimatedDelivery ? `<p><strong>Estimated Delivery:</strong> ${escapeHtml(proposal.estimatedDelivery)}</p>` : ""}
                    <div class="proposal-cover-letter">
                        <strong>Cover Letter:</strong>
                        <p>${proposal.coverLetter ? escapeHtml(proposal.coverLetter) : "<em>No cover letter provided.</em>"}</p>
                    </div>
                    ${actionButtonsHtml}
                </div>
            `;

            proposalsList.appendChild(card);
        });

    } catch (error) {
        message.textContent = "Cannot connect to the server. Make sure Spring Boot is running.";
        message.style.color = "red";
        console.error(error);
    }
}

async function handleAcceptProposal(proposalId, projectId) {
    const message = document.getElementById("viewProposalsMessage");
    try {
        const response = await fetch(`${API_URL}/proposals/${proposalId}/accept`, {
            method: "PUT",
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            const errorJson = await response.json().catch(() => null);
            const err = (errorJson && errorJson.message) || `Failed to accept proposal (${response.status})`;
            if (message) { message.textContent = err; message.style.color = "red"; }
            return;
        }

        await fetchProposalsForProject(projectId);
        if (getUserRole() === "CLIENT") loadClientDashboard();
        if (message) {
            message.textContent = `Proposal #${proposalId} accepted! Project moved to IN_PROGRESS.`;
            message.style.color = "green";
        }
    } catch (error) {
        console.error(error);
    }
}

async function handleRejectProposal(proposalId, projectId) {
    const message = document.getElementById("viewProposalsMessage");
    if (!confirm("Are you sure you want to reject this proposal?")) return;

    try {
        const response = await fetch(`${API_URL}/proposals/${proposalId}/reject`, {
            method: "PUT",
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            const errorJson = await response.json().catch(() => null);
            const err = (errorJson && errorJson.message) || `Failed to reject proposal (${response.status})`;
            if (message) { message.textContent = err; message.style.color = "red"; }
            return;
        }

        await fetchProposalsForProject(projectId);
        if (message) {
            message.textContent = `Proposal #${proposalId} rejected.`;
            message.style.color = "green";
        }
    } catch (error) {
        console.error(error);
    }
}


/* =========================
   CLIENT DASHBOARD (Feature 2)
========================= */

async function loadClientDashboard() {
    const clientStatsGrid = document.getElementById("clientStatsGrid");
    const projectsList = document.getElementById("clientProjectsList");
    if (!projectsList || !getAuthToken()) return;

    try {
        const response = await fetch(`${API_URL}/dashboard/client`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) return;

        const data = await response.json();

        // Fill Stats
        document.getElementById("clientStatTotal").textContent = data.totalProjects || 0;
        document.getElementById("clientStatOpen").textContent = data.openProjects || 0;
        document.getElementById("clientStatInProgress").textContent = data.inProgressProjects || 0;
        document.getElementById("clientStatCompleted").textContent = data.completedProjects || 0;
        document.getElementById("clientStatProposals").textContent = data.totalProposalsReceived || 0;

        // Render client's projects
        projectsList.innerHTML = "";
        if (!data.projects || data.projects.length === 0) {
            projectsList.innerHTML = `<p style="color:#64748b;">You haven't posted any projects yet. Click "+ Post New Project" to get started.</p>`;
            return;
        }

        data.projects.forEach(project => {
            const card = document.createElement("div");
            card.className = "project-card";

            const status = (project.status || "OPEN").toUpperCase();
            let statusBadge = "badge-open";
            if (status === "IN_PROGRESS") statusBadge = "badge-in_progress";
            else if (status === "COMPLETED") statusBadge = "badge-completed";

            let markCompletedBtn = "";
            let reviewBtn = "";
            if (status === "IN_PROGRESS") {
                markCompletedBtn = `
                    <button class="project-button complete-project-btn" onclick="markProjectCompleted(${project.id})">
                        ✓ Mark Completed
                    </button>
                `;
            } else if (status === "COMPLETED") {
                reviewBtn = `
                    <button class="project-button review-btn" onclick="openReviewModal(${project.id}, '${escapeHtml(project.title)}')">
                        ★ Review Freelancer
                    </button>
                `;
            }

            card.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
                    <h3 style="margin:0;">${escapeHtml(project.title)}</h3>
                    <span class="status-badge ${statusBadge}">${status}</span>
                </div>
                <p>${escapeHtml(project.description)}</p>
                <div style="display:flex; gap:20px; font-size:14px; margin-bottom:12px;">
                    <p><strong>Budget:</strong> ₹${project.budget}</p>
                    <p><strong>Category:</strong> ${escapeHtml(project.category)}</p>
                    <p><strong>Proposals Received:</strong> ${project.proposalCount}</p>
                </div>
                <div class="project-actions">
                    <button class="project-button secondary" onclick="viewProjectProposals(${project.id})">
                        Review Proposals (${project.proposalCount})
                    </button>
                    ${markCompletedBtn}
                    ${reviewBtn}
                </div>
            `;
            projectsList.appendChild(card);
        });

    } catch (e) {
        console.error("Error loading client dashboard", e);
    }
}

async function markProjectCompleted(projectId) {
    if (!confirm("Are you sure you want to mark this project as COMPLETED?")) return;

    try {
        const response = await fetch(`${API_URL}/projects/${projectId}/status`, {
            method: "PUT",
            headers: getAuthHeaders({ "Content-Type": "application/json" }),
            body: JSON.stringify({ status: "COMPLETED" })
        });

        if (response.ok) {
            alert("Project successfully marked as completed!");
            loadClientDashboard();
            loadProjects();
        } else {
            const err = await response.json().catch(() => null);
            alert((err && err.message) || "Failed to complete project");
        }
    } catch (e) {
        console.error(e);
    }
}


/* =========================
   FREELANCER DASHBOARD (Feature 3)
========================= */

async function loadFreelancerDashboard() {
    const list = document.getElementById("freelancerProposalsList");
    if (!list || !getAuthToken()) return;

    try {
        const response = await fetch(`${API_URL}/dashboard/freelancer`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) return;

        const data = await response.json();

        // Fill Stats
        document.getElementById("flStatTotal").textContent = data.totalProposals || 0;
        document.getElementById("flStatPending").textContent = data.pendingProposals || 0;
        document.getElementById("flStatAccepted").textContent = data.acceptedProposals || 0;
        document.getElementById("flStatRejected").textContent = data.rejectedProposals || 0;
        document.getElementById("flStatCompleted").textContent = data.completedProjects || 0;

        list.innerHTML = "";
        if (!data.proposals || data.proposals.length === 0) {
            list.innerHTML = `<p style="color:#64748b;">You have not submitted any proposals yet. Check out Available Projects above!</p>`;
            return;
        }

        data.proposals.forEach(p => {
            const card = document.createElement("div");
            card.className = "proposal-card";

            const propStatus = (p.status || "PENDING").toUpperCase();
            let propBadge = "badge-pending";
            if (propStatus === "ACCEPTED") propBadge = "badge-accepted";
            else if (propStatus === "REJECTED") propBadge = "badge-rejected";

            let reviewClientBtn = "";
            if (propStatus === "ACCEPTED" && p.projectStatus === "COMPLETED") {
                reviewClientBtn = `
                    <button class="project-button review-btn" onclick="openReviewModal(${p.projectId}, '${escapeHtml(p.projectTitle)}')">
                        ★ Review Client
                    </button>
                `;
            }

            card.innerHTML = `
                <div class="proposal-header">
                    <span class="proposal-id">${escapeHtml(p.projectTitle)} (Project #${p.projectId})</span>
                    <span class="status-badge ${propBadge}">Proposal: ${propStatus}</span>
                </div>
                <div class="proposal-body">
                    <p><strong>Project Status:</strong> <span class="status-badge badge-${(p.projectStatus || 'open').toLowerCase()}">${escapeHtml(p.projectStatus || 'OPEN')}</span></p>
                    <p><strong>Proposed Amount:</strong> ₹${p.proposedAmount}</p>
                    ${p.estimatedDelivery ? `<p><strong>Estimated Delivery:</strong> ${escapeHtml(p.estimatedDelivery)}</p>` : ""}
                    <div class="proposal-cover-letter">
                        <strong>My Proposal:</strong>
                        <p>${escapeHtml(p.coverLetter)}</p>
                    </div>
                    ${reviewClientBtn ? `<div class="project-actions" style="margin-top:14px;">${reviewClientBtn}</div>` : ""}
                </div>
            `;
            list.appendChild(card);
        });

    } catch (e) {
        console.error("Error loading freelancer dashboard", e);
    }
}


/* =========================
   USER PROFILE SYSTEM (Feature 1)
========================= */

async function loadUserProfile() {
    if (!getAuthToken()) return;

    try {
        const response = await fetch(`${API_URL}/users/profile`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) return;

        const profile = await response.json();

        document.getElementById("profNameDisplay").textContent = profile.name;
        document.getElementById("profEmailDisplay").textContent = profile.email;
        document.getElementById("profRoleBadge").textContent = profile.role;
        document.getElementById("profRatingDisplay").textContent =
            `★ ${profile.averageRating > 0 ? profile.averageRating.toFixed(1) : "0.0"} (${profile.reviewCount} review${profile.reviewCount === 1 ? "" : "s"})`;

        document.getElementById("profNameInput").value = profile.name || "";
        document.getElementById("profBioInput").value = profile.bio || "";
        document.getElementById("profSkillsInput").value = profile.skills || "";
        document.getElementById("profExperienceInput").value = profile.experience || "";
        document.getElementById("profPortfolioInput").value = profile.portfolioUrl || "";

        // Load reviews received
        loadProfileReviews(profile.email);

    } catch (e) {
        console.error("Error loading user profile", e);
    }
}

async function loadProfileReviews(email) {
    const list = document.getElementById("profileReviewsList");
    if (!list) return;

    try {
        const response = await fetch(`${API_URL}/reviews/user/${encodeURIComponent(email)}`);
        if (!response.ok) return;

        const reviews = await response.json();
        list.innerHTML = "";

        if (reviews.length === 0) {
            list.innerHTML = "<p style='color:#94a3b8; font-size:14px;'>No reviews received yet.</p>";
            return;
        }

        reviews.forEach(r => {
            const item = document.createElement("div");
            item.className = "review-item";
            item.innerHTML = `
                <div class="review-header">
                    <span class="review-author">From: ${escapeHtml(r.reviewerName || r.reviewerEmail)} (Project #${r.projectId})</span>
                    <span class="review-stars">${"★".repeat(r.rating)}${"☆".repeat(5 - r.rating)}</span>
                </div>
                <div class="review-comment">${escapeHtml(r.comment)}</div>
            `;
            list.appendChild(item);
        });
    } catch (e) {
        console.error(e);
    }
}

document.getElementById("profileForm").addEventListener("submit", async function (event) {
    event.preventDefault();
    const msg = document.getElementById("profileMessage");

    const body = {
        name: document.getElementById("profNameInput").value,
        bio: document.getElementById("profBioInput").value,
        skills: document.getElementById("profSkillsInput").value,
        experience: document.getElementById("profExperienceInput").value,
        portfolioUrl: document.getElementById("profPortfolioInput").value
    };

    try {
        const response = await fetch(`${API_URL}/users/profile`, {
            method: "PUT",
            headers: getAuthHeaders({ "Content-Type": "application/json" }),
            body: JSON.stringify(body)
        });

        if (response.ok) {
            msg.textContent = "Profile updated successfully!";
            msg.style.color = "green";
            loadUserProfile();
        } else {
            const err = await response.json().catch(() => null);
            msg.textContent = (err && err.message) || "Failed to update profile";
            msg.style.color = "red";
        }
    } catch (e) {
        msg.textContent = "Error connecting to server";
        msg.style.color = "red";
    }
});


/* =========================
   REVIEWS & RATINGS (Feature 7)
========================= */

function openReviewModal(projectId, projectTitle) {
    const modal = document.getElementById("reviewModal");
    const idField = document.getElementById("reviewProjectId");
    const titleElem = document.getElementById("reviewModalTitle");
    const msg = document.getElementById("reviewFormMessage");

    if (idField) idField.value = projectId;
    if (titleElem) titleElem.textContent = `Review Collaborator for "${projectTitle}"`;
    if (msg) msg.textContent = "";

    if (modal) modal.style.display = "flex";
}

function closeReviewModal() {
    const modal = document.getElementById("reviewModal");
    if (modal) modal.style.display = "none";
}

document.getElementById("submitReviewForm").addEventListener("submit", async function (e) {
    e.preventDefault();
    const projectId = document.getElementById("reviewProjectId").value;
    const rating = document.getElementById("reviewRating").value;
    const comment = document.getElementById("reviewComment").value;
    const msg = document.getElementById("reviewFormMessage");

    try {
        const response = await fetch(`${API_URL}/reviews`, {
            method: "POST",
            headers: getAuthHeaders({ "Content-Type": "application/json" }),
            body: JSON.stringify({
                projectId: Number(projectId),
                rating: Number(rating),
                comment: comment
            })
        });

        if (response.ok) {
            msg.textContent = "Review submitted successfully! Thank you for your feedback.";
            msg.style.color = "green";
            setTimeout(() => {
                closeReviewModal();
                loadDashboardForCurrentRole();
                loadUserProfile();
            }, 1200);
        } else {
            const err = await response.json().catch(() => null);
            msg.textContent = (err && err.message) || "Failed to submit review.";
            msg.style.color = "red";
        }
    } catch (err) {
        msg.textContent = "Error communicating with server.";
        msg.style.color = "red";
    }
});

async function viewProjectReviews(projectId) {
    try {
        const response = await fetch(`${API_URL}/reviews/project/${projectId}`);
        if (!response.ok) return;

        const reviews = await response.json();
        if (reviews.length === 0) {
            alert(`No reviews have been submitted for Project #${projectId} yet.`);
            return;
        }

        const reviewText = reviews.map(r =>
            `• ${r.reviewerName || r.reviewerEmail}: ${"★".repeat(r.rating)} - "${r.comment}"`
        ).join("\n\n");

        alert(`Reviews for Project #${projectId}:\n\n${reviewText}`);
    } catch (e) {
        console.error(e);
    }
}


/* =========================
   ROLE DASHBOARD DISPATCHER
========================= */

function loadDashboardForCurrentRole() {
    const role = getUserRole();
    if (role === "CLIENT") {
        loadClientDashboard();
    } else if (role === "FREELANCER") {
        loadFreelancerDashboard();
    }
}

function escapeHtml(text) {
    if (!text) return "";
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}


/* =========================
   INITIALIZATION ON PAGE LOAD
========================= */

document.addEventListener("DOMContentLoaded", function () {
    updateAuthUI();
    loadProjects();
    loadDashboardForCurrentRole();
    if (getAuthToken()) {
        loadUserProfile();
    }

    const viewProposalsForm = document.getElementById("viewProposalsForm");
    if (viewProposalsForm) {
        viewProposalsForm.addEventListener("submit", function (event) {
            event.preventDefault();
            const searchInput = document.getElementById("searchProjectId");
            fetchProposalsForProject(searchInput.value);
        });
    }
});