import '../../assets/Message.css';
import ProfileAvatar from '../ProfileAvatar';

export default function Message({ user, selected, onClick }) {
  return (
    <div className={`message ${selected ? 'selected' : ''}`.trim()} onClick={onClick}>
      <div className="pfp-container">
        <ProfileAvatar
          user={user}
          imageProps={{ className: 'pfp' }}
          placeholderProps={{ className: 'message-avatar-placeholder' }}
          anchorClassName="message-avatar-anchor"
        />
      </div>
      <div className="msg-container">
        <b className="user">{user?.pseudo ?? 'Unknown'}</b>
      </div>
    </div>
  );
}
