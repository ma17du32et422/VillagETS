
/** SignupForm */
function SignupForm() {
    return (
        <form>
            <h2>Sign Up</h2>

            <div>
                <label htmlFor="signUsername">Username: </label>
                <input type="text" id="signUsername" name="signUsername"/>
            </div>

            <div>
                <label htmlFor="signPassword">Password: </label>
                <input type="password" id="signPassword" name="signPassword"/>
            </div>

            <div>
                <label htmlFor="confirmPassword">Confirm Password: </label>
                <input type="password" id="confirmPassword" name="confirmPassword"/>
            </div>

            <button type={"submit"}>Sign Up</button>
        </form>
    );
}

export default SignupForm;