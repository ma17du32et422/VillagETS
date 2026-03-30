/** Component imports */
import Header from "../components/Header.jsx";
import LoginForm from "../components/LoginForm";
import SignupForm from "../components/SignupForm";
import UsernameForm from "../components/UsernameForm";
import PasswordForm from "../components/PasswordForm";

/** Styling */
import '../assets/ProfilePage.css'

/** If not logged in, the user can log in using a form.
If logged in, the user can modify his username and profile picture.
TO BE HANDLED DYNAMICALLY LATER -> PLUG INTO JS LOGIC AND SETUP STATES TO SHOW/HIDE COMPONENTS*/

/** ProfilePage */
function ProfilePage() {
    return (
        <>
            <header>
                <Header />
            </header>

            <main id="profileMain">

                {/** LoginForm and SignupForm */}
                <section id="login-container">
                    <div id="login">
                        <LoginForm />
                        <SignupForm />
                    </div>
                </section>

                {/** UsernameForm and PasswordForm */}
                <section id="profile-edit-container">
                    <div id="profile-edit">
                        <UsernameForm />
                        <PasswordForm />

                        {/** Profile Picture Selection */}
                        <div id="profile-picture">
                            <h3>Profile Picture</h3>
                            <img alt="alt: pfp devrait être ici" src="http://localhost:5000/uploads/865eacfa-b497-41c9-843f-0ffc4849df06.jpg" height="80sp" /> {/** Image de test */}
                            <input
                                type="file"
                                id="profile-picture-input"
                                accept="image/*"
                                style={{ display: 'none' }}
                                onChange={async (e) => {
                                    const file = e.target.files[0];
                                    if (!file) return;

                                    const formData = new FormData();
                                    formData.append('file', file);

                                    const response = await fetch('https://villagets.lesageserveur.com/upload', {
                                        method: 'POST',
                                        body: formData,
                                    });

                                    const data = await response.json();
                                    console.log('URL du fichier :', data.url); //url du fichier uploadé ici
                                }}
                            />
                            <button onClick={() => document.getElementById('profile-picture-input').click()}>
                                Change Profile Picture
                            </button>
                        </div>

                        <button>Log Out</button>
                    </div>
                </section>
            </main>
        </>
    );
}

export default ProfilePage;