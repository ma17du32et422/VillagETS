/** Component imports */
import Header from "../components/Header.jsx";
import LoginForm from "../components/LoginForm";
import SignupForm from "../components/SignupForm";

/** Styling */
import '../assets/ProfilePage.css'

function LoginPage(){
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
            </main>
        </>
    );
}

export default LoginPage;