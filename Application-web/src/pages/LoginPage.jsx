/** Component imports */
import Header from "../components/Header.jsx";
import LoginForm from "../components/LoginForm";
import SignupForm from "../components/SignupForm";

/** Styling */
import '../assets/ProfilePage.css'
import usePageTitle from "../utils/usePageTitle";


function LoginPage(){
    usePageTitle("Connexion");
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
                    </div>
                </section>
            </main>
        </>
    );
}

export default LoginPage;