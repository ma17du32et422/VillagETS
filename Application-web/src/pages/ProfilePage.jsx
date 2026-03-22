/** Component imports */
import LoginForm from "../components/LoginForm";
import UsernameForm from "../components/UsernameForm";
import PasswordForm from "../components/PasswordForm";

/** Styling */
import '../assets/ProfilePage.css'


/** If not logged in, the user can log in using a form.
If logged in, the user can modify his username and profile picture.
TO BE HANDLED DYNAMICALLY LATER -> PLUG INTO JS LOGIC AND SETUP STATES TO SHOW/HIDE COMPONENTS*/

/** ProfilePage */
function ProfilePage(){
  return(
    <>
        <header id="header">
            <h1>Profile</h1>
        </header>

        <main id="main">

            {/** LoginForm */}
            <section id="login-container">
                <div id="login">
                    <LoginForm />
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
                        <img alt="Profile Picture"/>
                        <button>Change Profile Picture</button>
                    </div>
                </div>
            </section>
        </main>
    </>
  );
}

export default ProfilePage;