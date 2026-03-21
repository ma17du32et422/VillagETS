function ProfilePage(){
  return(
      // If not logged in, the user can log in using a form.
      // If logged in, the user can modify his username and profile picture.
      // TO BE HANDLED DYNAMICALLY LATER
      // TODO: CHECK IF THE ATTRIBUTES ARE FORMATTED PROPERLY FOR REACT
    <>
        <div className='profile'>
            <h1>Profile</h1>
            <h2>Log In</h2>
            <form>
                <div>
                    <label htmlFor="username">Username: </label>
                    <input type="text" id="username" name="username"/>
                </div>
                <div>
                    <label htmlFor="password">Password: </label>
                    <input type="password" id="password" name="password"/>
                </div>
            </form>
            <button>Log In</button>
            <h2>Modify Details</h2>
            <button>Modify User</button>
            <form>
                <div>
                    <label htmlFor="newUsername">New Username: </label>
                    <input type="text" id="newUsername" name="newUsername"/>
                </div>
            </form>
            <button>Apply New User</button>
            <button>Modify Password</button>
            <form>
                <div>
                    <label htmlFor="oldPassword">Old Password: </label>
                    <input type="password" id="oldPassword" name="oldPassword"/>
                </div>
                <div>
                    <label htmlFor="newPassword">New Password: </label>
                    <input type="password" id="newPassword" name="newPassword"/>
                </div>
            </form>
            <button>Apply New Password</button>
            <button>Modify Profile Picture</button>
            <image>Profile Picture Above</image>
        </div>
    </>
  );
}

export default ProfilePage;