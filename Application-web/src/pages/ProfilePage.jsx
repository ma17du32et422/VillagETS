/** Component imports */
import Header from "../components/Header.jsx";
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

                {/** UsernameForm and PasswordForm */}
                <section id="profile-edit-container">
                    <div id="profile-edit">
                        <UsernameForm />
                        <PasswordForm />

                        {/** Profile Picture Selection */}
                        <div id="profile-picture">
                            <h3>Profile Picture</h3>
                            <img alt="alt: pfp devrait être ici" src="https://apivillagets.lesageserveur.com/uploads/90c9dce0-039c-408d-b8fd-d0956bf46814.jpg" height="80sp" /> {/** Image de test */}
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
                                    formData.append('nom', file.name);
                                    formData.append('type', "pfp");

                                    const response = await fetch('https://apivillagets.lesageserveur.com/upload', { //http://localhost:5000/upload
                                        method: 'POST',
                                        body: formData,
                                    });

                                    //const data = await response.json();
                                    //console.log('URL du fichier :', data.url); //url du fichier uploadé ici
                                    const text = await response.text();
                                    console.log('Réponse serveur:', text);

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