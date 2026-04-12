/** Component imports */
import Header from "../components/Header.jsx";
import SignupForm from "../components/SignupForm.jsx";

/** Styling */
import '../assets/ProfilePage.css'

function Signup(){
    return (
        <>
            <header>
                <Header />
            </header>

            <main id="profileMain">
                {/** LoginForm and SignupForm */}
                <section id="signup-container">
                    <div id="singup">
                        <SignupForm />
                    </div>
                </section>
            </main>
        </>
    );
}

export default Signup;