
/** PasswordForm */
function PasswordForm() {
    return (
        <form>
            <h3>Modify Password</h3>

            <div>
                <label htmlFor="oldPassword">Old Password: </label>
                <input type="password" id="oldPassword" name="oldPassword"/>
            </div>

            <div>
                <label htmlFor="newPassword">New Password: </label>
                <input type="password" id="newPassword" name="newPassword"/>
            </div>

            <button type="submit">Apply New Password</button>
        </form>
    );
}

export default PasswordForm;