
/** Usernameform */
function UsernameForm() {
    return (
        <form>
            <h3>Modify Username</h3>

            <div>
                <label htmlFor="newUsername">New Username: </label>
                <input type="text" id="newUsername" name="newUsername"/>
            </div>

            <button type="submit">Apply New Username</button>
        </form>
    );
}

export default UsernameForm;