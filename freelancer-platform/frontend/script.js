const API_URL = "http://localhost:8080";

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

        const data = await response.text();

        if (response.ok) {

            message.textContent = "Registration successful!";
            message.style.color = "green";

            document.getElementById("registerForm").reset();

        } else {

            message.textContent = data || "Registration failed.";
            message.style.color = "red";
        }

    } catch (error) {

        message.textContent =
            "Cannot connect to the server. Make sure Spring Boot is running.";

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

        const data = await response.text();

        if (response.ok) {

            message.textContent = data;
            message.style.color = "green";

        } else {

            message.textContent = data || "Login failed.";
            message.style.color = "red";
        }

    } catch (error) {

        message.textContent =
            "Cannot connect to the server. Make sure Spring Boot is running.";

        message.style.color = "red";

        console.error(error);
    }
});


/* =========================
   LOAD PROJECTS
========================= */

async function loadProjects() {

    const projectList = document.getElementById("projectList");

    try {

        const response = await fetch(`${API_URL}/projects`);

        if (!response.ok) {
            throw new Error("Failed to load projects");
        }

        const projects = await response.json();

        projectList.innerHTML = "";

        if (projects.length === 0) {

            projectList.innerHTML =
                "<p>No projects available.</p>";

            return;
        }

        projects.forEach(project => {

            const projectCard = document.createElement("div");

            projectCard.className = "project-card";

            projectCard.innerHTML = `
                <h3>${project.title}</h3>

                <p>${project.description}</p>

                <p>
                    <strong>Budget:</strong>
                    ₹${project.budget}
                </p>

                <p>
                    <strong>Category:</strong>
                    ${project.category}
                </p>

                <button
                    class="project-button"
                    onclick="selectProject(${project.id})">
                    Submit Proposal
                </button>
            `;

            projectList.appendChild(projectCard);
        });

    } catch (error) {

        projectList.innerHTML = `
            <p style="color:red;">
                Unable to load projects.
                Make sure Spring Boot is running.
            </p>
        `;

        console.error(error);
    }
}


/* =========================
   SELECT PROJECT
========================= */

function selectProject(projectId) {

    document.getElementById("proposalProjectId").value = projectId;

    document.getElementById("proposal").scrollIntoView({
        behavior: "smooth"
    });
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

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                title: title,
                description: description,
                budget: Number(budget),
                category: category
            })
        });

        if (response.ok) {

            const project = await response.json();

            message.textContent =
                `Project "${project.title}" posted successfully!`;

            message.style.color = "green";

            document.getElementById("projectForm").reset();

            loadProjects();

        } else {

            const errorText = await response.text();

            message.textContent =
                errorText || "Failed to post project.";

            message.style.color = "red";
        }

    } catch (error) {

        message.textContent =
            "Cannot connect to the server. Make sure Spring Boot is running.";

        message.style.color = "red";

        console.error(error);
    }
});


/* =========================
   SUBMIT PROPOSAL
========================= */

document.getElementById("proposalForm").addEventListener("submit", async function (event) {

    event.preventDefault();

    const projectId =
        document.getElementById("proposalProjectId").value;

    const freelancerEmail =
        document.getElementById("freelancerEmail").value;

    const coverLetter =
        document.getElementById("coverLetter").value;

    const proposedAmount =
        document.getElementById("proposedAmount").value;

    const message =
        document.getElementById("proposalMessage");

    try {

        const response = await fetch(`${API_URL}/proposals`, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                projectId: Number(projectId),
                freelancerEmail: freelancerEmail,
                coverLetter: coverLetter,
                proposedAmount: Number(proposedAmount)
            })
        });

        if (response.ok) {

            const proposal = await response.json();

            message.textContent =
                `Proposal submitted successfully! Proposal ID: ${proposal.id}`;

            message.style.color = "green";

            document.getElementById("proposalForm").reset();

        } else {

            const errorText = await response.text();

            message.textContent =
                errorText || "Failed to submit proposal.";

            message.style.color = "red";
        }

    } catch (error) {

        message.textContent =
            "Cannot connect to the server. Make sure Spring Boot is running.";

        message.style.color = "red";

        console.error(error);
    }
});


/* =========================
   LOAD PROJECTS ON PAGE LOAD
========================= */

document.addEventListener("DOMContentLoaded", function () {

    loadProjects();

});