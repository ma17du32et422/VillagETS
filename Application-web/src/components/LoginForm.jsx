
/** LoginForm */
function LoginForm() {
    return (
        <form>
            <h2>Log In</h2>

            <div>
                <label htmlFor="username">Username: </label>
                <input type="text" id="username" name="username"/>
            </div>

            <div>
                <label htmlFor="password">Password: </label>
                <input type="password" id="password" name="password"/>
            </div>

            <button type={"submit"}>Log In</button>
        </form>
    );
}

export default LoginForm;